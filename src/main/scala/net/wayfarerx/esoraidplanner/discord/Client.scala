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

import org.http4s.client.blaze.BlazeClientConfig

final class Client private(url: URL) {

  def dispose(): IO[Unit] = ???

}

object Client {

  def apply(config: BlazeClientConfig, url: URL): IO[Client] = {

    IO.pure(new Client(url))

    ???
  }

}
