/*
 * Messages.scala
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

/**
 * Messages used  by the main package.
 */
object Messages {

  /** The message component that describes the program to run. */
  private def program = "java -jar esoraidplanner-discord.jar"

  /** The message that describes how to print the help message. */
  private def useHelp = s"Run `$program --help` for usage instructions."

  /**
   * Generates the help message.
   *
   * @param sections The sections of optional settings.
   * @return The help message.
   */
  def helpMessage(sections: (String, Vector[Setting[_]])*): String = {
    val padding = " " * 3
    val newLine = String.format("%n")

    def settingUsage(setting: Setting[_]): String =
      setting.alias.map(a => s"-$a | --${setting.name}").getOrElse(s"--${setting.name}") +
        (if (setting.parameterized) " <value>" else "")

    def describeSettings(title: String, settings: Vector[Setting[_]]): String = {
      val items = settings map { setting =>
        padding + settingUsage(setting) -> setting.description
      }
      val indent = items.map(_._1.length).max + 2
      s" # $title" +
        newLine * 2 +
        items.map { case (usage, description) =>
          breakLines(usage + " " * (indent - usage.length) + description, indent)
        }.mkString(newLine)
    }

    def breakLines(content: String, indent: Int, maxLength: Int = 120): String =
      if (content.length <= maxLength) content else {
        content.lastIndexOf(' ', maxLength - 1) match {
          case index if index >= content.takeWhile(_ == ' ').length =>
            content.substring(0, index) + newLine +
              breakLines(" " * indent + content.substring(index + 1), indent, maxLength)
          case _ => content
        }
      }

    s"Usage: $program <setting ...>" +
      newLine * 2 +
      sections.map { case (title, section) => describeSettings(title, section) }.mkString(newLine * 2)
  }

  /** The message returned when an invalid command-line argument is encountered. */
  def invalidCommandLineArgument(arg: String): String =
    s"""Invalid command-line argument: "$arg". $useHelp"""

  /** The message returned when an invalid name is encountered for a setting. */
  def invalidSettingName(name: String): String =
    s"""Invalid name for setting: "$name". $useHelp"""

  /** The message returned when an invalid alias is encountered for a setting. */
  def invalidSettingAlias(alias: Char): String =
    s"""Invalid alias for setting: "$alias". $useHelp"""

  /** The message returned when a value is missing for a setting. */
  def missingSettingValue(setting: Setting[_]): String =
    s"""Missing value for setting: "${setting.name}". $useHelp"""

  /** The message returned when an invalid value is encountered for a setting. */
  def invalidSettingValue(setting: Setting[_], value: String): String =
    s"""Invalid value for setting "${setting.name}": "$value". $useHelp"""

  /** The message returned when an invalid value is encountered for a setting. */
  def missingRequiredSettings(settings: Vector[Setting[_]]): String =
    s"The following settings are required but were not specified: ${
      settings map (_.name) match {
        case init :+ tail if init.nonEmpty => s"""${init.map(i => s""""$i"""").mkString(", ")} & "$tail""""
        case head +: _ => s""""$head""""
        case _ => ""
      }
    }. $useHelp"

}
