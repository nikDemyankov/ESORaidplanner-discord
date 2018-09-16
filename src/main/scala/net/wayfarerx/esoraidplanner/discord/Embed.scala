/*
 * Embed.scala
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

import cats.syntax.either._
import io.circe.generic.auto._
import io.circe.{Json, parser}
//import io.circe.syntax._

/**
 * Envelope for embedded content model.
 *
 * @param username   The username posting the content.
 * @param content    The content associated with this post.
 * @param avatar_url The URL of the poster's avatar.
 * @param embeds     The embeds to process.
 */
case class Embed(
  username: String,
  content: String,
  avatar_url: String,
  embeds: Vector[Embed.Embedded]
)

/**
 * Definitions of the embed model.
 */
object Embed {

  /**
   * Attempts to parse an embed from a JSON string.
   *
   * @param json The JSON to parse.
   * @return The result of attempting to parse the JSON.
   */
  def apply(json: String): Either[String, Embed] =
    parser.parse(json).leftMap(_.message).flatMap(apply)

  /**
   * Attempts to extract an embed from a JSON structure.
   *
   * @param json The JSON structure to extract from.
   * @return The result of attempting to extract an embed.
   */
  def apply(json: Json): Either[String, Embed] =
    json.as[Embed].leftMap(_.message)

  /**
   * A single embedded item.
   *
   * @param title The title of the embedded item.
   * @param description The description of the embedded item.
   * @param url The URL of the embedded item.
   * @param color The color of the embedded item.
   * @param author The author of the embedded item.
   * @param fields The fields of the embedded item.
   * @param footer The footer of the embedded item.
   */
  case class Embedded(
    title: String,
    description: String,
    url: String,
    color: Int,
    author: Author,
    fields: Vector[Field],
    footer: Footer
  )

  /**
   * An author of an embedded item.
   *
   * @param name The name of the author.
   * @param url The URL of the author.
   * @param icon_url The URL of the author's icon.
   */
  case class Author(
    name: String,
    url: String,
    icon_url: String
  )

  /**
   * An embedded field.
   *
   * @param name The name of the field.
   * @param value The value of the field.
   * @param inline True if the field is inline.
   */
  case class Field(
    name: String,
    value: String,
    inline: Boolean
  )

  /**
   * A footer of an embedded item.
   *
   * @param text The text of the footer.
   * @param icon_url The icon URL of the footer.
   */
  case class Footer(
    text: String,
    icon_url: String
  )

}
