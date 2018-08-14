/*
 * CharacterClass.scala
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
 * Base type for character classes.
 */
sealed trait CharacterClass {

  /** The name of this class. */
  def name: String

}

/**
 * Definitions of the supported character classes.
 */
object CharacterClass {

  /** All of the supported character classes. */
  val All: Vector[CharacterClass] = Vector(Dragonknight, Nightblade, Sorcerer, Templar, Warden)

  /** The index of all character classes by lower case name. */
  private val index = All.map(cc => cc.name.toLowerCase -> cc).toMap

  /**
   * Attempts to extract a character class from a string.
   *
   * @param string The string to match to a character class's name.
   * @return The character class if one was found.
   */
  def unapply(string: String): Option[CharacterClass] =
    index get string.trim.toLowerCase

  /** Represents the Dragonknight class. */
  case object Dragonknight extends CharacterClass {
    override def name: String = "Dragonknight"
  }

  /** Represents the Nightblade class. */
  case object Nightblade extends CharacterClass {
    override def name: String = "Nightblade"
  }

  /** Represents the Sorcerer class. */
  case object Sorcerer extends CharacterClass {
    override def name: String = "Sorcerer"
  }

  /** Represents the Templar class. */
  case object Templar extends CharacterClass {
    override def name: String = "Templar"
  }

  /** Represents the Warden class. */
  case object Warden extends CharacterClass {
    override def name: String = "Warden"
  }

}
