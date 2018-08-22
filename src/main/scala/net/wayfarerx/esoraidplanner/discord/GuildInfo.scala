/*
 * GuildInfo.scala
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

import java.text.SimpleDateFormat
import java.time.Instant
import java.util.{Date, TimeZone}

import cats.syntax.either._
import io.circe._
import io.circe.parser._

/**
 * Guild metadata stored by ESO Raidplanner.
 *
 * @param id                  The ID of the Raidplanner guild.
 * @param discordId           The ID of the Discord server.
 * @param discordLastActivity The instant that the most recent Discord to Raidplanner activity occurred.
 */
case class GuildInfo(id: Int, discordId: Long, discordLastActivity: Instant)

/**
 * Factory for guild metadata.
 */
object GuildInfo {

  /** The instant encoder. */
  private implicit val instantEncoder: Encoder[Instant] =
    Encoder.encodeString.contramap[Instant](i => dateFormat.format(new Date(i.toEpochMilli)))


  /** The instant decoder. */
  private implicit val instantDecoder: Decoder[Instant] =
    Decoder.decodeString.emap { str =>
      Either.catchNonFatal(Instant.ofEpochMilli(dateFormat.parse(str).getTime)).leftMap(t => t.getMessage)
    }

  /** The main encoder. */
  implicit val encoder: Encoder[GuildInfo] =
    Encoder.forProduct3("id", "discord_id", "discord_last_activity") { info =>
      (info.id, info.discordId, info.discordLastActivity)
    }

  /** The main decoder. */
  implicit val decoder: Decoder[GuildInfo] =
    Decoder.forProduct3("id", "discord_id", "discord_last_activity")(GuildInfo.apply)

  /** The date format to use. */
  private def dateFormat = {
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    sdf.getCalendar.setTimeZone(TimeZone.getTimeZone("UTC"))
    sdf
  }

  /**
   * Attempts to decode a sequence of guild metadata objects from a raw JSON string.
   *
   * @param string The string to decode.
   * @return The result of attempting to decode a sequence of guild metadata objects from a raw JSON string.
   */
  def decodeAll(string: String): Either[Throwable, Vector[GuildInfo]] =
    parse(string) flatMap (j => j.as[Vector[GuildInfo]])

}
