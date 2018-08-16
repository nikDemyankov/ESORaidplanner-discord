/*
 * Message.scala
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

/**
 * Base type for messages sent to ESO Raid Planner.
 */
sealed trait Message

/**
 * Definitions of the ESO Raid Planner messages.
 */
object Message {

  /**
   * Represents a user setting up a guild.
   *
   * @param userHandle The Discord handle of the user.
   * @param userId The Discord ID of the user.
   * @param serverId The Discord ID of the server.
   * @param channelId The Discord ID of the channel.
   * @param guildId The ID of the guild.
   */
  case class Setup(
    userHandle: String,
    userId: Long,
    serverId: Long,
    channelId: Long,
    guildId: Option[Int]
  ) extends Message

  /**
   * Represents a request to list available events.
   *
   * @param userHandle The Discord handle of the user.
   * @param userId The Discord ID of the user.
   * @param serverId The Discord ID of the server.
   * @param channelId The Discord ID of the channel.
   */
  case class Events(
    userHandle: String,
    userId: Long,
    serverId: Long,
    channelId: Long
  ) extends Message

  /**
   * Represents a user volunteering for an event.
   *
   * @param userHandle The Discord handle of the user.
   * @param userId The Discord ID of the user.
   * @param serverId The Discord ID of the server.
   * @param channelId The Discord ID of the channel.
   * @param eventId The ID of the event.
   * @param characterClass The user's character class.
   * @param characterRole The user's character role.
   */
  case class Signup(
    userHandle: String,
    userId: Long,
    serverId: Long,
    channelId: Long,
    eventId: Int,
    characterClass: CharacterClass,
    characterRole: CharacterRole
  ) extends Message

  /**
   * Represents a user abandoning an event.
   *
   * @param userHandle The Discord handle of the user.
   * @param userId The Discord ID of the user.
   * @param serverId The Discord ID of the server.
   * @param channelId The Discord ID of the channel.
   * @param eventId The ID of the event.
   */
  case class Signoff(
    userHandle: String,
    userId: Long,
    serverId: Long,
    channelId: Long,
    eventId: Int
  ) extends Message

  /**
   * Represents a request to show the help message.
   *
   * @param userHandle The Discord handle of the user.
   * @param userId The Discord ID of the user.
   * @param serverId The Discord ID of the server.
   * @param channelId The Discord ID of the channel.
   */
  case class Help(
    userHandle: String,
    userId: Long,
    serverId: Long,
    channelId: Long
  ) extends Message

}
