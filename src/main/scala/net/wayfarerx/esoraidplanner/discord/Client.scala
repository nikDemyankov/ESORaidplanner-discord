/*
 * Client.scala
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

import java.net.URL

import util.control.NoStackTrace

import cats.effect.IO

import fs2.text

import org.http4s.{Header, Status, Uri, UrlForm}
import org.http4s.client.{Client => HttpClient}
import org.http4s.dsl.io._
import org.http4s.client.dsl.io._
import org.http4s.client.blaze.{BlazeClientConfig, Http1Client}

/**
 * The HTTP client that sends messages to ESO raid planner.
 *
 * @param client    The underlying HTTP client.
 * @param authToken The authorization token to use.
 * @param setup     The URI for the setup operation.
 * @param events    The URI for the events operation.
 * @param signup    The URI for the signup operation.
 * @param signoff   The URI for the signoff operation.
 * @param status   The URI for the status operation.
 * @param help   The URI for the help operation.
 * @param lastActivity   The URI for the last activity operation.
 */
final class Client private(
  client: HttpClient[IO],
  authToken: String,
  setup: Uri,
  events: Uri,
  signup: Uri,
  signoff: Uri,
  status: Uri,
  help: Uri,
  lastActivity: Uri
) {

  import Client.Recoverable

  /**
   * Attempts to send a message to ESO raid planner.
   *
   * @param message The message to send.
   * @return The result of attempting to send a message to ESO raid planner.
   */
  def send(message: Message): IO[String] = {

    def render(metadata: Message.Metadata): Vector[(String, String)] = Vector(
      "discord_handle" -> metadata.userHandle,
      "discord_user_id" -> metadata.userId.toString,
      "discord_channel_id" -> metadata.channelId.toString,
      "discord_server_id" -> metadata.serverId.toString
    )

    message match {
      case Message.Setup(metadata, guildId) =>
        post(setup, UrlForm.fromSeq(render(metadata) ++ guildId.map(id => "guild_id" -> id.toString).toVector))
      case Message.Events(metadata) =>
        post(events, UrlForm.fromSeq(render(metadata)))
      case Message.Signup(metadata, eventId, Left((characterClass, characterRole))) =>
        post(signup, UrlForm.fromSeq(render(metadata) ++ Vector(
          "event_id" -> eventId.toString,
          "class" -> classId(characterClass).toString,
          "role" -> roleId(characterRole).toString
        )))
      case Message.Signup(metadata, eventId, Right(preset)) =>
        post(signup, UrlForm.fromSeq(render(metadata) ++ Vector(
          "event_id" -> eventId.toString,
          "preset" -> preset.name
        )))
      case Message.Signoff(metadata, eventId) =>
        post(signoff, UrlForm.fromSeq(render(metadata) :+ ("event_id" -> eventId.toString)))
      case Message.Status(metadata, eventId) =>
        post(status, UrlForm.fromSeq(render(metadata) :+ ("event_id" -> eventId.toString)))
      case Message.Help(metadata) =>
        post(help, UrlForm.fromSeq(render(metadata)))
      case Message.LastActivity =>
        post(lastActivity, UrlForm())
    }
  }

  /**
   * Attempts to post a message to ESO raid planner.
   *
   * @param uri  The uri to post to.
   * @param form The form to post.
   * @return The result of attempting to post a message to ESO raid planner.
   */
  private def post(uri: Uri, form: UrlForm): IO[String] =
    client.expectOr[String](POST(uri, form, Header("Authorization", s"Basic $authToken"))) { res =>
      if (res.status.responseClass == Status.ServerError) {
        IO.pure(new Recoverable("sorry, I'm not feeling too great at the moment. Try that again in a minute or two."))
      } else res.body.through(text.utf8Decode).compile.toVector map (txt => new Recoverable(txt.mkString))
    } redeemWith( {
      case e: Recoverable => IO.pure(e.getMessage)
      case e => IO.raiseError(e)
    }, IO.pure)

  /** Returns the class ID for the specified class. */
  private def classId(cls: CharacterClass) = cls match {
    case CharacterClass.Dragonknight => 1
    case CharacterClass.Nightblade => 3
    case CharacterClass.Sorcerer => 2
    case CharacterClass.Templar => 6
    case CharacterClass.Warden => 4
  }

  /** Returns the role ID for the specified role. */
  private def roleId(role: CharacterRole) = role match {
    case CharacterRole.Tank => 1
    case CharacterRole.Healer => 2
    case CharacterRole.MagickaDPS => 3
    case CharacterRole.StaminaDPS => 4
    case CharacterRole.Other => 5
  }

  /** Releases any resources associated with this client. */
  def dispose(): IO[Unit] =
    client.shutdown

}

/**
 * Factory for HTTP clients.
 */
object Client {

  /**
   * Attempts to create a new HTTP client.
   *
   * @param config    The HTTP client configuration.
   * @param authToken The authorization token to use.
   * @param url       The base URL to use for all client operations.
   * @return The result of attempting to create a new HTTP client.
   */
  def apply(config: BlazeClientConfig, authToken: String, url: URL): IO[Client] = {

    def resolve(path: String): IO[Uri] = for {
      relativeUrl <- IO(new URL(url, path))
      relativeUri <- Uri.fromString(relativeUrl.toExternalForm) match {
        case Left(e) => IO.raiseError(e)
        case Right(uri) => IO.pure(uri)
      }
    } yield relativeUri

    for {
      setup <- resolve("/api/discord/setup")
      events <- resolve("/api/discord/events")
      signup <- resolve("/api/discord/signup")
      signoff <- resolve("/api/discord/signoff")
      status <- resolve("/api/discord/status")
      help <- resolve("/api/discord/help")
      lastActivity <- resolve("/api/discord/last-activity")
      httpClient <- Http1Client[IO](config)
    } yield new Client(httpClient, authToken, setup, events, signup, signoff, status, help, lastActivity)
  }

  /** A signal that an HTTP request was not successful. */
  private class Recoverable(message: String) extends RuntimeException(message) with NoStackTrace

}
