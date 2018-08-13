/*
 * Servers.scala
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
package main

import concurrent.ExecutionContext.Implicits.global

import cats.effect.IO

import org.http4s.server.blaze.BlazeBuilder

/**
 * Configuration factory for the HTTP server.
 */
object Servers extends Setting.Provider[BlazeBuilder[IO]] {

  /* The default configuration value. */
  override def Default: Config = BlazeBuilder[IO]

  /* The list of all HTTP server settings. */
  override val Settings: Vector[Setting[BlazeBuilder[IO]]] = Vector(

    Setting.option[Config]("server-max-request-line-length",
      "Maximum HTTP request line length to parse.")(
      Setting.Ints((config, value) => Some(config.withLengthLimits(maxRequestLineLen = value)))),

    Setting.option[Config]("server-max-headers-length",
      "Maximum data that compose HTTP headers.")(
      Setting.Ints((config, value) => Some(config.withLengthLimits(maxHeadersLen = value)))),

    Setting.option[Config]("server-idle-timeout",
      "Period of time a connection can remain idle before the connection is timed out and disconnected.")(
      Setting.Durations((config, value) => Some(config.withIdleTimeout(value)))),

    Setting.option[Config]("server-connector-pool-size",
      "Number of worker threads for the new socket server group.")(
      Setting.Ints((config, value) => Some(config.withConnectorPoolSize(value)))),

    Setting.option[Config]("server-buffer-size",
      "Buffer size to use for IO operations.")(
      Setting.Ints((config, value) => Some(config.withBufferSize(value)))),

    Setting.flag[Config]("server-nio1",
      "Use NIO1 socket server group.")(
      config => Some(config.withNio2(false))),

    Setting.flag[Config]("server-nio2",
      "Use NIO2 socket server group.")(
      config => Some(config.withNio2(true))),

    Setting.flag[Config]("server-http1",
      "Disable HTTP2 server features.")(
      config => Some(config.enableHttp2(false))),

    Setting.flag[Config]("server-http2",
      "Enable HTTP2 server features.")(
      config => Some(config.enableHttp2(true)))

  )

}
