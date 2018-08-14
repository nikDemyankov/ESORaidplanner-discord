/*
 * CharacterRole.scala
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
 * Base type for character roles.
 */
sealed trait CharacterRole {

  /** The name of this role. */
  def name: String

}

/**
 * Definitions of the supported character roles.
 */
object CharacterRole {

  /** All of the supported character roles. */
  val All: Vector[CharacterRole] = Vector(Tank, Healer, MagickaDPS, StaminaDPS, Other)

  /** The index of all character roles by lower case name. */
  private val index = All.map(cc => cc.name.toLowerCase -> cc).toMap

  /**
   * Attempts to extract a character role from a string.
   *
   * @param string The string to match to a character role's name.
   * @return The character role if one was found.
   */
  def unapply(string: String): Option[CharacterRole] =
    index get string.trim.toLowerCase

  /** Represents the Tank role. */
  case object Tank extends CharacterRole {
    override def name: String = "Tank"
  }

  /** Represents the Healer role. */
  case object Healer extends CharacterRole {
    override def name: String = "Healer"
  }

  /** Represents the MagickaDPS role. */
  case object MagickaDPS extends CharacterRole {
    override def name: String = "MagickaDPS"
  }

  /** Represents the StaminaDPS role. */
  case object StaminaDPS extends CharacterRole {
    override def name: String = "StaminaDPS"
  }

  /** Represents the Other role. */
  case object Other extends CharacterRole {
    override def name: String = "Other"
  }

}
