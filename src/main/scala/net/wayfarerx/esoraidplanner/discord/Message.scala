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
   * @param metadata The metadata associated with this message.
   * @param guildId The ID of the guild.
   */
  case class Setup(
    metadata: Metadata,
    guildId: Option[Int]
  ) extends Message

  /**
   * Represents a request to list available events.
   *
   * @param metadata The metadata associated with this message.
   */
  case class Events(
    metadata: Metadata
  ) extends Message

  /**
   * Represents a user volunteering for an event.
   *
   * @param metadata The metadata associated with this message.
   * @param eventId The ID of the event.
   * @param character Either the user's character class & role or the name of a preset.
   */
  case class Signup(
    metadata: Metadata,
    eventId: Int,
    character: Either[(CharacterClass, CharacterRole), CharacterPreset]
  ) extends Message

  /**
   * Represents a user abandoning an event.
   *
   * @param metadata The metadata associated with this message.
   * @param eventId The ID of the event.
   */
  case class Signoff(
    metadata: Metadata,
    eventId: Int
  ) extends Message

  /**
   * Represents a user querying their status in an event.
   *
   * @param metadata The metadata associated with this message.
   * @param eventId The ID of the event.
   */
  case class Status(
    metadata: Metadata,
    eventId: Int
  ) extends Message

  /**
   * Represents a request to list the current state of an event.
   *
   * @param metadata The metadata associated with this message.
   * @param eventId The ID of the event.
   */
  case class Signups(
    metadata: Metadata,
    eventId: Int
  ) extends Message

  /**
   * Represents a request to show the help message.
   *
   * @param metadata The metadata associated with this message.
   */
  case class Help(
    metadata: Metadata
  ) extends Message

  /**
   * Represents metadata associated with a message.
   *
   * @param userHandle The Discord handle of the user.
   * @param userId The Discord ID of the user.
   * @param channelId The Discord ID of the channel.
   * @param serverId The Discord ID of the server.
   */
  case class Metadata(
    userHandle: String,
    userId: Long,
    channelId: Long,
    serverId: Long
  )

  /**
   * Represents a user setting up a guild.
   */
  case object LastActivity extends Message

}
