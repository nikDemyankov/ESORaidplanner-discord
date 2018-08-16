/*
 * Clients.scala
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

import org.http4s.client.blaze.BlazeClientConfig

/**
 * Configuration factory for the HTTP client.
 */
object Clients extends Setting.Provider[BlazeClientConfig] {

  /* The default configuration value. */
  override def Default: Config = BlazeClientConfig.defaultConfig .copy(
    lenientParser = true
  )

  /* The list of all HTTP client settings. */
  override val Settings: Vector[Setting[Config]] = Vector(

    Setting.option[Config]("client-response-header-timeout",
      "Duration between the submission of a request and the completion of the response header.")(
      Setting.Durations((config, value) => Some(config.copy(responseHeaderTimeout = value)))),

    Setting.option[Config]("client-idle-timeout",
      "Duration that a connection can wait without traffic being read or written before timeout.")(
      Setting.Durations((config, value) => Some(config.copy(idleTimeout = value)))),

    Setting.option[Config]("client-request-timeout",
      "Maximum duration from the submission of a request through reading the body before a timeout.")(
      Setting.Durations((config, value) => Some(config.copy(requestTimeout = value)))),

    Setting.option[Config]("client-max-total-connections",
      "Maximum connections the client will have at any specific time.")(
      Setting.Ints((config, value) => Some(config.copy(maxTotalConnections = value)))),

    Setting.option[Config]("client-max-wait-queue-limit",
      "Maximum number requests waiting for a connection at any specific time.")(
      Setting.Ints((config, value) => Some(config.copy(maxWaitQueueLimit = value)))),

    Setting.flag[Config]("client-check-endpoint-identification",
      "Enables verifying that the certificate presented matches the hostname of the request.")(
      config => Some(config.copy(checkEndpointIdentification = true))),

    Setting.flag[Config]("client-ignore-endpoint-identification",
      "Disables verifying that the certificate presented matches the hostname of the request.")(
      config => Some(config.copy(checkEndpointIdentification = false))),

    Setting.option[Config]("client-max-response-line-size",
      "Maximum length of the request line.")(
      Setting.Ints((config, value) => Some(config.copy(maxResponseLineSize = value)))),

    Setting.option[Config]("client-max-header-length",
      "Maximum length of headers.")(
      Setting.Ints((config, value) => Some(config.copy(maxHeaderLength = value)))),

    Setting.option[Config]("client-max-chunk-size",
      "Maximum size of chunked content chunks.")(
      Setting.Ints((config, value) => Some(config.copy(maxChunkSize = value)))),

    Setting.flag[Config]("client-lenient-parser",
      "A lenient parser will accept illegal chars but replaces them with \uFFFD (0xFFFD).")(
      config => Some(config.copy(lenientParser = true))),

    Setting.flag[Config]("client-strict-parser",
      "A strict parser will not accept illegal chars.")(
      config => Some(config.copy(lenientParser = false))),

    Setting.option[Config]("client-buffer-size",
      "Internal buffer size of the HTTP client.")(
      Setting.Ints((config, value) => Some(config.copy(bufferSize = value))))

  )

}
