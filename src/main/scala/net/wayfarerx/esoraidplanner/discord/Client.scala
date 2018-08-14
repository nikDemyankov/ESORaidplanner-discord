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

import cats.effect.IO
import org.http4s.client.{Client => HttpClient}
import org.http4s.client.blaze.{BlazeClientConfig, Http1Client}

/**
 * The HTTP client that sends messages to ESO raid planner.
 *
 * @param client The underlying HTTP client.
 * @param url The base URL to use for all client operations.
 */
final class Client private(client: HttpClient[IO], url: URL) {

  /**
   * Attempts to send a message to ESO raid planner.
   *
   * @param message The message to send.
   * @return The result of attempting to send a message to ESO raid planner.
   */
  def send(message: Message): IO[Unit] = {
    // TODO Need ESO Raid Planner REST API details.
    IO(println(s"Sending message to ESO Raid Planner ($url): $message"))
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
   * @param config The HTTP client configuration.
   * @param url The base URL to use for all client operations.
   * @return The result of attempting to create a new HTTP client.
   */
  def apply(config: BlazeClientConfig, url: URL): IO[Client] =
    Http1Client[IO](config) map (new Client(_, url))

}
