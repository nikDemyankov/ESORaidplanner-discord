/*
 * CharacterPreset.scala
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
 * Represents a named character preset on ESO Raidplanner.
 *
 * @param name The name of the character preset.
 */
case class CharacterPreset(name: String)

/**
 * Extractor for character presets.
 */
object CharacterPreset {

  /**
   * Attempts to extract a character preset name from a string.
   *
   * @param string The string to match to a character preset name.
   * @return The character preset if one was found.
   */
  def unapply(string: String): Option[CharacterPreset] = {
    val normalized = string.trim
    if (normalized.isEmpty) None else normalized charAt 0 match {
      case quote@('`' | '\'' | '"') => normalized.indexOf(quote & 0x0000FFFF, 1) match {
        case end if end > 1 => Some(CharacterPreset(normalized.substring(1, end)))
        case _ => None
      }
      case _ => None
    }
  }

}
