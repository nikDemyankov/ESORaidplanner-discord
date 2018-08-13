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

import org.http4s.server.blaze.BlazeBuilder

final class Server private(bot: Bot, builder: BlazeBuilder[IO]) {

  def run(): IO[ExitCode] = builder
    .bindHttp(8080, "localhost")
    //.mountService(???, "/")
    .serve.compile.drain.map(_ => ExitCode.Success)

}


object Server {

  def apply(bot: Bot, builder: BlazeBuilder[IO], address: String, port: Int): IO[Server] =
    IO.pure(new Server(bot, builder))

}
