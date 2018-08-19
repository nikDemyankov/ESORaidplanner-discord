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

import cats.effect.IO

import collection.JavaConverters._
import util.Try

import sx.blah.discord.api.events.IListener
import sx.blah.discord.api.{ClientBuilder, IDiscordClient}
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent
import sx.blah.discord.util.RequestBuffer

/**
 * The Discord bot that sends and receives messages.
 *
 * @param discord The Discord client.
 * @param client  The ESO raid planner client.
 */
final class Bot private(discord: IDiscordClient, client: Client) {

  /** The message event handler. */
  private val OnMessage: IListener[MessageEvent] =
    (event: MessageEvent) => received(event).unsafeRunSync()

  /**
   * Handles receiving a message event.
   *
   * @param event The message event.
   * @return The result of the attempt to handle the event.
   */
  private def received(event: MessageEvent): IO[Unit] =
    if (event.getMessage.getAuthor == discord.getOurUser || !event.getMessage.getContent.trim.startsWith("!")) {
      IO.pure(())
    } else {
      val userHandle = s"${event.getAuthor.getName}#${event.getAuthor.getDiscriminator}"
      val userId = event.getAuthor.getLongID
      val channelId = event.getChannel.getLongID
      val serverId = event.getGuild.getLongID

      @annotation.tailrec
      def scan(remaining: Vector[String], messages: Vector[Message]): Vector[Message] = remaining match {
        case cmd +: next if cmd equalsIgnoreCase "!setup" =>
          Try(next.headOption.map(_.toInt)).toOption.flatten match {
            case Some(guildId) =>
              scan(next.tail, messages :+ Message.Setup(userHandle, userId, serverId, channelId, Some(guildId)))
            case None =>
              scan(next, messages :+ Message.Setup(userHandle, userId, serverId, channelId, None))
          }
        case cmd +: next if cmd equalsIgnoreCase "!events" =>
          scan(next, messages :+ Message.Events(userHandle, userId, serverId, channelId))
        case cmd +: eventId +: CharacterClass(cls) +: CharacterRole(role) +: next if cmd equalsIgnoreCase "!signup" =>
          Try(eventId.toInt).toOption match {
            case Some(eid) =>
              scan(next, messages :+ Message.Signup(userHandle, userId, serverId, channelId, eid, cls, role))
            case None =>
              scan(next, messages)
          }
        case cmd +: eventId +: next if cmd equalsIgnoreCase "!signoff" =>
          Try(eventId.toInt).toOption match {
            case Some(eid) =>
              scan(next, messages :+ Message.Signoff(userHandle, userId, serverId, channelId, eid))
            case None =>
              scan(next, messages)
          }
        case cmd +: next
          if cmd.equalsIgnoreCase("!help") || cmd.equalsIgnoreCase("!commands") =>
          scan(next, messages :+ Message.Help(userHandle, userId, serverId, channelId))
        case _ +: next =>
          scan(next, messages)
        case _ =>
          messages
      }

      def deliver(remaining: Vector[Message]): IO[Unit] = remaining match {
        case head +: tail =>
          client.send(head)
            .flatMap(r => if (r.nonEmpty) request(event.getChannel.sendMessage(r)) else IO.pure(()))
            .flatMap(_ => deliver(tail))
        case _ =>
          IO.pure(())
      }

      deliver(scan(
        event.getMessage.getContent.split("""\s+""").iterator.filterNot(_.isEmpty).toVector,
        Vector.empty
      ))
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

  /**
   * Attempts to create a new Discord bot.
   *
   * @param builder The configuration for the Discord bot.
   * @param client  The HTTP client that connects to the ESO raid planner service.
   * @return The result of attempting to create a new Discord bot.
   */
  def apply(builder: ClientBuilder, client: Client): IO[Bot] = IO {
    val discord = builder.build()
    val bot = new Bot(discord, client)
    val dispatcher = discord.getDispatcher
    dispatcher.registerListener(bot.OnMessage)
    discord.login()
    bot
  }

}
