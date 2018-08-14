/*
 * Server.scala
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

import cats.effect.{ExitCode, IO}

import org.http4s._
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
import org.http4s.server.blaze.BlazeBuilder

/**
 * The HTTP server that handles incoming push notifications.
 *
 * @param builder The underlying HTTP server builder.
 * @param address The address to bind to.
 * @param port The port to bind to.
 * @param bot The bot to forward notifications to.
 */
final class Server private(builder: BlazeBuilder[IO], address: String, port: Int, bot: Bot) {

  /** The HTML for the notification test page. */
  private val html =
    """
      |<html>
      |  <head>
      |    <title>Push notification test page.</title>
      |  </head>
      |  <body>
      |    <form action="/push" method="post">
      |      Channel ID: <input type="text" name="channel-id" /><br />
      |      Message: <input type="text" name="message" /><br />
      |      <input type = "submit" value ="Send Notification" />
      |    </form>
      |  </body>
      |</html>
    """.stripMargin.getBytes("UTF-8")

  /** The definition of the HTTP service. */
  private val service = HttpRoutes.of[IO] {

    case GET -> Root / "push" =>
      Ok(html) map (_.withContentType(`Content-Type`(MediaType.text.html)))

    case req@POST -> Root / "push" =>
      req.decode[UrlForm] { data =>
        (for {
          channelId <- data getFirst "channel-id"
          message <- data getFirst "message"
        } yield Notification(channelId.toLong, message)) match {
          case Some(notification) =>
            bot.push(notification) flatMap (_ => Ok("Notification sent!"))
          case None =>
            BadRequest(s"Invalid data: $data")
        }
      }

  }

  /** Runs the server waiting for the JVM to exit. */
  def run(): IO[ExitCode] = builder
    .bindHttp(port, address)
    .mountService(service, "/")
    .serve.compile.drain.map(_ => ExitCode.Success)

}

/**
 * Factory for HTTP servers.
 */
object Server {

  /**
   * Attempts to create a new HTTP server.
   *
   * @param builder The underlying HTTP server builder.
   * @param address The address to bind to.
   * @param port The port to bind to.
   * @param bot The bot to forward notifications to.
   * @return The result of attempting to create a new HTTP server.
   */
  def apply(builder: BlazeBuilder[IO], address: String, port: Int, bot: Bot): IO[Server] =
    IO.pure(new Server(builder, address, port, bot))

}
