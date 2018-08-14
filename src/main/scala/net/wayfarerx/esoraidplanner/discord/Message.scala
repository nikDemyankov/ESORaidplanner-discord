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
   * Represents a user volunteering for an event.
   *
   * @param userId The Discord ID of the user.
   * @param guildId The Discord ID of the guild.
   * @param eventId The ID of the event.
   * @param characterClass The user's character class.
   * @param characterRole The user's character role.
   */
  case class SignUp(
    userId: Long,
    guildId: Long,
    eventId: String,
    characterClass: CharacterClass,
    characterRole: CharacterRole
  ) extends Message

  /**
   * Represents a user abandoning an event.
   *
   * @param userId The Discord ID of the user.
   * @param guildId The Discord ID of the guild.
   * @param eventId The ID of the event.
   */
  case class SignOff(
    userId: Long,
    guildId: Long,
    eventId: String
  ) extends Message

}
