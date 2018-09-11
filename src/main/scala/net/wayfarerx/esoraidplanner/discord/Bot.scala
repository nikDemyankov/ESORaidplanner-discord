/*
 * Bot.scala
 *
 * Copyright (C) 2018 wayfarerx <x@wayfarerx.net> (@thewayfarerx)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.wayfarerx.esoraidplanner.discord

import java.time.Instant

import collection.JavaConverters._
import concurrent.duration._
import util.Try
import cats.effect.IO
import org.log4s._
import sx.blah.discord.api.events.IListener
import sx.blah.discord.api.{ClientBuilder, IDiscordClient}
import sx.blah.discord.handle.impl.events.ReadyEvent
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.impl.events.shard.LoginEvent
import sx.blah.discord.handle.obj.{IChannel, IMessage}
import sx.blah.discord.util.RequestBuffer

/**
 * The Discord bot that sends and receives messages.
 *
 * @param discord  The Discord client.
 * @param lookback The maximum length of time to look back in message histories.
 * @param client   The ESO raid planner client.
 */
final class Bot private(discord: IDiscordClient, lookback: FiniteDuration, client: Client) {

  import Bot._

  /** The logger to use. */
  private val logger = getLogger

  /** True if this bot has received a ready event. */
  @volatile
  private var ready = false

  /** The message event handler. */
  private val OnMessage: IListener[MessageReceivedEvent] =
    (event: MessageReceivedEvent) => received(event.getMessage, recovering = false).unsafeRunSync()

  /** The login event handler. */
  private val OnLogin: IListener[LoginEvent] =
    (_: LoginEvent) => if (ready) login().unsafeRunSync()

  /** The login event handler. */
  private val OnReady: IListener[ReadyEvent] =
    (_: ReadyEvent) => if (!ready) {
      ready = true
      login().unsafeRunSync()
    }

  /**
   * Handles receiving a message event.
   *
   * @param message    The message to receive.
   * @param recovering True if this is an old message.
   * @return The result of the attempt to handle the event.
   */
  private def received(message: IMessage, recovering: Boolean): IO[Unit] = {
    val me = discord.getOurUser
    if (message.getAuthor.getLongID == me.getLongID)
      IO.pure(())
    else if (message.getContent.trim.startsWith("!")) {
      val tokens = message.getContent.split("""\s+""").iterator.filterNot(_.isEmpty).toVector
      Commands get tokens.head.substring(1).toLowerCase map { cmd =>
        (cmd.parse(Message.Metadata(
          s"${message.getAuthor.getName}#${message.getAuthor.getDiscriminator}",
          message.getAuthor.getLongID,
          message.getChannel.getLongID,
          message.getGuild.getLongID
        ), tokens.tail) match {
          case Left(errorMessage) =>
            if (recovering) IO.pure(()) else request(message.reply(errorMessage))
          case Right(msg) =>
            client.send(msg).flatMap(r => if (r.nonEmpty) request(message.getChannel.sendMessage(r)) else IO.pure(()))
        }) map (_ => ())
      } getOrElse IO.pure(())
    } else if (!recovering &&
      message.getMentions.size == 1 &&
      message.getMentions.asScala.exists(_.getLongID == me.getLongID)) {
      Fortunes() flatMap (f => request(message.getChannel.sendMessage(f))) map (_ => ())
    } else
      IO.pure(())
  }

  /** Handles receiving a login event. */
  def login(): IO[Unit] = {

    def inspectGuilds(remaining: Vector[GuildInfo]): IO[Unit] = remaining match {
      case info +: next =>
        request(discord.getGuildByID(info.discordId)) map (Option(_)) flatMap {
          case Some(guild) =>
            request(guild.getChannels.asScala.toVector) flatMap (inspectChannels(info, _))
          case None =>
            IO(logger.warn(s"Invalid Discord guild ID: ${info.discordId}"))
        } flatMap (_ => inspectGuilds(next))
      case _ =>
        IO.pure(())
    }

    def inspectChannels(info: GuildInfo, remaining: Vector[IChannel]): IO[Unit] = remaining match {
      case channel +: next =>
        val since = implicitly[Ordering[Instant]].max(
          info.discordLastActivity,
          Instant.ofEpochMilli((Deadline.now - lookback).time.toMillis)
        )
        request(channel.getMessageHistoryTo(since)).flatMap { history =>
          handleMessages(history.iterator.asScala.filterNot(_.getAuthor == discord.getOurUser).toVector)
        }.flatMap (_ => inspectChannels(info, next)).redeem(_ => (), _ => ())
      case _ =>
        IO.pure(())
    }

    def handleMessages(remaining: Vector[IMessage]): IO[Unit] = remaining match {
      case message +: next =>
        received(message, recovering = true) flatMap (_ => handleMessages(next))
      case _ =>
        IO.pure(())
    }

    for {
      string <- client.send(Message.LastActivity)
      guilds <- GuildInfo.decodeAll(string) match {
        case Right(v) => IO.pure(v)
        case Left(t) => IO.raiseError(t)
      }
      result <- inspectGuilds(guilds)
    } yield result
  }

  /**
   * Attempts to return the channels in the specified server.
   *
   * @param serverId The ID of the server to list the channels of.
   * @return The result of attempting to return the channels in the specified server.
   */
  def channels(serverId: Long): IO[Vector[(Long, String)]] =
    request(discord.getGuildByID(serverId)) map (Option(_)) flatMap {
      case Some(guild) => request(guild.getChannels).map(_.iterator.asScala.map(c => c.getLongID -> c.getName).toVector)
      case None => IO.raiseError(new IllegalArgumentException(s"Unknown server ID: $serverId"))
    }

  /**
   * Attempts to send a push notification to a channel.
   *
   * @param notification The notification to send.
   * @return The result of attempting to push notification to a channel.
   */
  def push(notification: Notification): IO[Unit] =
    for {
      channel <- request(discord.getChannelByID(notification.channelId))
      _ <- request(channel.sendMessage(notification.message))
    } yield ()

  /**
   * Sends a request to Discord and manages any rate limit exceptions.
   *
   * @tparam T The type of result returned by the request.
   * @param action The action that performs the request.
   * @return The result of attempting the request.
   */
  private def request[T](action: => T): IO[T] =
    IO(RequestBuffer.request(() => action).get())

  /** Shuts down this Discord Bot. */
  def dispose(): IO[Unit] =
    IO(if (discord.isLoggedIn) discord.logout())

}

/**
 * Factory for Discord bots.
 */
object Bot {

  /** The map of all commands by their names. */
  private val Commands = Seq(Command.Setup, Command.Events, Command.Signup, Command.Signoff, Command.Help)
    .flatMap(c => c.names map (_ -> c)).toMap

  /**
   * Attempts to create a new Discord bot.
   *
   * @param builder  The configuration for the Discord bot.
   * @param lookback The maximum length of time to look back in message histories.
   * @param client   The HTTP client that connects to the ESO raid planner service.
   * @return The result of attempting to create a new Discord bot.
   */
  def apply(builder: ClientBuilder, lookback: FiniteDuration, client: Client): IO[Bot] =
    for (discord <- IO(builder.build())) yield {
      val bot = new Bot(discord, lookback, client)
      val dispatcher = discord.getDispatcher
      dispatcher.registerListener(bot.OnMessage)
      dispatcher.registerListener(bot.OnLogin)
      dispatcher.registerListener(bot.OnReady)
      discord.login()
      bot
    }

  /**
   * Base type for command parsers.
   */
  sealed abstract class Command(val names: String*) {

    /**
     * Attempts to parse a command from the specified arguments.
     *
     * @param metadata The message metadata.
     * @param args     The arguments to parse.
     * @return The resulting message or an error message.
     */
    def parse(metadata: Message.Metadata, args: Vector[String]): Either[String, Message]

  }

  /**
   * Definitions of the supported commands.
   */
  object Command {

    /** The invalid guild ID error message fragment. */
    private def invalidGuildId(guildId: String): String =
      s""""$guildId" is not a valid guild ID"""

    /** The missing event ID error message fragment. */
    private def missingEventId: String = "the event ID is missing"

    /** The invalid event ID error message fragment. */
    private def invalidEventId(eventId: String): String =
      s""""$eventId" is not a valid event ID"""

    /** The missing class error message fragment. */
    private def missingClass: String = "the class is missing"

    /** The invalid class error message fragment. */
    private def invalidClass(cls: String): String =
      s""""$cls" is not a valid class"""

    /** The missing role error message fragment. */
    private def missingRole: String = "the role is missing"

    /** The invalid role error message fragment. */
    private def invalidRole(role: String): String =
      s""""$role" is not a valid role"""

    /** The usage error message fragment. */
    private def usage: String = "Type `!help` for usage instructions"

    /**
     * Constructs an error message from error message fragments.
     *
     * @param fragments The error message fragments to use.
     * @return The final error message.
     */
    private def errorMessage(fragments: String*): String = fragments match {
      case Seq(single) => s"$single. $usage."
      case init :+ last => s"${init mkString ", "} & $last. $usage."
      case _ => s"$usage."
    }

    /**
     * The !setup command.
     */
    object Setup extends Command("setup") {
      override def parse(metadata: Message.Metadata, args: Vector[String]): Either[String, Message] =
        args.headOption match {
          case Some(AsInt(guildId)) => Right(Message.Setup(metadata, Some(guildId)))
          case Some(guildId) => Left(errorMessage(invalidGuildId(guildId)))
          case None => Right(Message.Setup(metadata, None))
        }
    }

    /**
     * The !events command.
     */
    object Events extends Command("events") {
      override def parse(metadata: Message.Metadata, args: Vector[String]): Either[String, Message] =
        Right(Message.Events(metadata))
    }

    /**
     * The !signup command.
     */
    object Signup extends Command("signup") {
      override def parse(metadata: Message.Metadata, args: Vector[String]): Either[String, Message] =
        args take 3 match {

          case Vector(AsInt(eventId), CharacterClass(cls), CharacterRole(role)) =>
            Right(Message.Signup(metadata, eventId, cls, role))
          case Vector(AsInt(_), cls, CharacterRole(_)) =>
            Left(errorMessage(invalidClass(cls)))
          case Vector(AsInt(_), CharacterClass(_), role) =>
            Left(errorMessage(invalidRole(role)))
          case Vector(AsInt(_), cls, role) =>
            Left(errorMessage(invalidClass(cls), invalidRole(role)))
          case Vector(eventId, CharacterClass(_), CharacterRole(_)) =>
            Left(errorMessage(invalidEventId(eventId)))
          case Vector(eventId, cls, CharacterRole(_)) =>
            Left(errorMessage(invalidEventId(eventId), invalidClass(cls)))
          case Vector(eventId, CharacterClass(_), role) =>
            Left(errorMessage(invalidEventId(eventId), invalidRole(role)))
          case Vector(eventId, cls, role) =>
            Left(errorMessage(invalidEventId(eventId), invalidClass(cls), invalidRole(role)))

          case Vector(AsInt(_), CharacterClass(_)) =>
            Left(errorMessage(missingRole))
          case Vector(AsInt(_), CharacterRole(_)) =>
            Left(errorMessage(missingClass))
          case Vector(AsInt(_), cls) =>
            Left(errorMessage(invalidClass(cls), missingRole))
          case Vector(CharacterClass(_), CharacterRole(_)) =>
            Left(errorMessage(missingEventId))
          case Vector(eventId, CharacterClass(_)) =>
            Left(errorMessage(invalidEventId(eventId), missingRole))
          case Vector(eventId, CharacterRole(_)) =>
            Left(errorMessage(invalidEventId(eventId), missingClass))
          case Vector(eventId, cls) =>
            Left(errorMessage(invalidEventId(eventId), invalidClass(cls), missingRole))

          case Vector(AsInt(_)) =>
            Left(errorMessage(missingClass, missingRole))
          case Vector(CharacterClass(_)) =>
            Left(errorMessage(missingEventId, missingRole))
          case Vector(CharacterRole(_)) =>
            Left(errorMessage(missingEventId, missingClass))
          case Vector(eventId) =>
            Left(errorMessage(invalidEventId(eventId), missingClass, missingRole))

          case _ =>
            Left(errorMessage(missingEventId, missingClass, missingRole))

        }
    }

    /**
     * The !signoff command.
     */
    object Signoff extends Command("signoff") {
      override def parse(metadata: Message.Metadata, args: Vector[String]): Either[String, Message] =
        args.headOption match {
          case Some(AsInt(eventId)) => Right(Message.Signoff(metadata, eventId))
          case Some(eventId) => Left(errorMessage(invalidEventId(eventId)))
          case None => Left(errorMessage(missingEventId))
        }
    }

    /**
     * The !signups command.
     */
    object Signups extends Command("signups", "roster") {
      override def parse(metadata: Message.Metadata, args: Vector[String]): Either[String, Message] =
        args.headOption match {
          case Some(AsInt(eventId)) => Right(Message.Signups(metadata, eventId))
          case Some(eventId) => Left(errorMessage(invalidEventId(eventId)))
          case None => Left(errorMessage(missingEventId))
        }
    }

    /**
     * The !help command.
     */
    object Help extends Command("help", "commands") {
      override def parse(metadata: Message.Metadata, args: Vector[String]): Either[String, Message] =
        Right(Message.Help(metadata))
    }

    /**
     * A utility for extracting integer parameters.
     */
    private object AsInt {

      /** Extracts an integer parameter. */
      def unapply(arg: String): Option[Int] =
        Try(arg.toInt).toOption

    }

  }

}
