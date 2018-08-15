/*
 * Program.scala
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
package main

import java.net.URL

import language.existentials
import util.{Failure, Success, Try}

import cats.effect.{ExitCode, IO, IOApp}

import ch.qos.logback.classic.Level

object Program extends IOApp {

  /** The current logging level. */
  @volatile
  private var _loggingLevel: Level = Level.WARN

  /** The pattern that matches named settings. */
  private val NamedSettingPattern =
    """--([a-zA-Z][a-zA-Z0-9\-]+[a-zA-Z0-9])""".r

  /** The pattern that matches aliased settings. */
  private val AliasedSettingPattern =
    """-([a-zA-Z]+)""".r

  /** The virtual setting that loads a configuration file. */
  private val ConfigSetting = Setting.option[String](
    "config", "Loads a configuration file."
  )((p, _) => Some(p))

  /** The virtual setting that displays the help message. */
  private val HelpSetting = Setting.flag[Unit](
    "help", 'h', "Shows this message and exits."
  )(Some(_))

  /** The collection of all supported settings. */
  private val AllSettings: Vector[Setting[_]] =
    (ConfigSetting +: Configuration.Settings :+ HelpSetting) ++ Bots.Settings ++ Clients.Settings ++ Servers.Settings

  /** The collection of all required settings. */
  private val RequiredSettings: Vector[Setting[_]] =
    AllSettings filter (_.required)

  /** Returns the current logging level. */
  private[main] def loggingLevel: Level = _loggingLevel

  /* Run the application in the IO context. */
  override def run(args: List[String]): IO[ExitCode] =
    loadSettings(args) flatMap { settings =>
      if (settings.contains(HelpSetting)) help() else for {
        config <- Configuration.configure(settings)
        _ <- IO(if (config.quiet ^ config.verbose) {
          if (config.quiet) _loggingLevel = Level.ERROR
          else _loggingLevel = Level.INFO
        })
        clientConfig <- Clients.configure(settings)
        botConfig <- Bots.configure(settings)
        serverConfig <- Servers.configure(settings)
        clientUrl <- IO(new URL(config.clientUrl))
        result <- Client(clientConfig, config.clientToken, clientUrl).bracket { client =>
          Bot(botConfig.withToken(config.botToken), client).bracket { bot =>
            Server(serverConfig, config.serverAddress, config.serverPort, bot) flatMap (_.run())
          }(_.dispose())
        }(_.dispose())
      } yield result
    }

  /**
   * Attempts to load all of the settings, including from any default or specified resources.
   *
   * @param args The command-line arguments
   * @return The result of the attempt to load all of the settings.
   */
  private def loadSettings(args: List[String]): IO[Map[Setting[_], String]] = {
    val settingsByName = Map(AllSettings.map(k => k.name -> k): _*)
    val settingsByAlias = Map(AllSettings.flatMap(k => k.alias map (_ -> k)): _*)

    def parse(remaining: List[String], results: Map[Setting[_], String]): IO[Map[Setting[_], String]] =
      remaining match {
        case NamedSettingPattern(name) +: next => settingsByName get name.toLowerCase match {
          case Some(setting) =>
            if (setting.parameterized) next.headOption match {
              case Some(value) if setting == ConfigSetting =>
                Config(value) flatMap { config => parse(config ::: next.tail, results) }
              case Some(value) =>
                parse(next.tail, results + (setting -> value))
              case None =>
                IO.raiseError(new IllegalArgumentException(Messages.missingSettingValue(setting)))
            } else parse(next, results + (setting -> ""))
          case None =>
            IO.raiseError(new IllegalArgumentException(Messages.invalidSettingName(name)))
        }
        case AliasedSettingPattern(aliases) +: next =>
          extract(aliases.toLowerCase, next, results) match {
            case Success((newRemaining, newResults)) => parse(newRemaining, newResults)
            case Failure(exception) => IO.raiseError(exception)
          }
        case invalid +: _ =>
          IO.raiseError(new IllegalArgumentException(Messages.invalidCommandLineArgument(invalid)))
        case _ =>
          IO.pure(results)
      }

    @annotation.tailrec
    def extract(
      aliases: String,
      remaining: List[String],
      results: Map[Setting[_], String]
    ): Try[(List[String], Map[Setting[_], String])] =
      if (aliases.isEmpty) Success(remaining -> results) else {
        val alias = aliases.charAt(0)
        settingsByAlias get alias match {
          case Some(setting) =>
            if (setting.parameterized) remaining.headOption match {
              case Some(value) => extract(aliases.tail, remaining.tail, results + (setting -> value))
              case None => Failure(new IllegalArgumentException(Messages.missingSettingValue(setting)))
            } else extract(aliases.tail, remaining, results + (setting -> ""))
          case None =>
            Failure(new IllegalArgumentException(Messages.invalidSettingAlias(alias)))
        }
      }

    parse(args, Map.empty) flatMap { parsed =>
      if (parsed.contains(HelpSetting)) IO.pure(parsed) else
        RequiredSettings.filterNot(parsed.keySet) match {
          case Vector() => IO.pure(parsed)
          case missing => IO.raiseError(new IllegalArgumentException(Messages.missingRequiredSettings(missing)))
        }
    }
  }

  /**
   * Attempts to print the help message.
   *
   * @return The result of the attempt to print the help message.
   */
  private def help(): IO[ExitCode] = IO(println(Messages.helpMessage(
    "Required settings." -> RequiredSettings,
    "Optional settings." -> (ConfigSetting +: Configuration.Settings :+ HelpSetting).filterNot(_.required),
    "Discord bot settings." -> Bots.Settings.filterNot(_.required),
    "HTTP client settings." -> Clients.Settings.filterNot(_.required),
    "HTTP server settings." -> Servers.Settings.filterNot(_.required)
  ))) map (_ => ExitCode.Success)

  /**
   * Global configuration elements.
   *
   * @param botToken The token used for authentication with Discord.
   */
  private case class Configuration(
    botToken: String,
    clientToken: String,
    clientUrl: String = "https://esoraidplanner.com/",
    serverAddress: String = "localhost",
    serverPort: Int = 7224,
    quiet: Boolean = false,
    verbose: Boolean = false
  )

  /**
   * Factory and provider for configurations.
   */
  private object Configuration extends Setting.Provider[Configuration] {

    /* The default (and invalid) configuration value. */
    override def Default: Configuration = Configuration("", "")

    /* The list of all global settings. */
    override val Settings: Vector[Setting[Configuration]] = Vector(

      Setting.option[Config]("bot-token", Some('b'),
        "The token used for authentication with Discord.", required = true)(
        (config, value) => Some(config.copy(botToken = value))),

      Setting.option[Config]("client-token", Some('c'),
        "The authorization token for connecting to ESO Raidplanner.", required = true)(
        (config, value) => Some(config.copy(clientToken = value))),

      Setting.option[Config]("client-url", 'u',
        "The base URL to use for all client operations.")(
        (config, value) => Some(config.copy(clientUrl = value))),

      Setting.option[Config]("server-address", 'a',
        "The address to bind server operations to.")(
        (config, value) => Some(config.copy(serverAddress = value))),

      Setting.option[Config]("server-port", 'p',
        "The port to bind server operations to.")(
        Setting.Ints((config, value) => Some(config.copy(serverPort = value)))),

      Setting.flag("quiet", 'q',
        "Decreases the amount of logging.")(
        config => Some(config.copy(quiet = true))),

      Setting.flag("verbose", 'v',
        "Increases the amount of logging.")(
        config => Some(config.copy(verbose = true)))

    )

  }

}
