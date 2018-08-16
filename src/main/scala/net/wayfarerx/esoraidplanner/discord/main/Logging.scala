/*
 * Logging.scala
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

import ch.qos.logback.classic.{BasicConfigurator, LoggerContext}

import org.slf4j.Logger

/**
 * Logging configurator that defers to the command-line configuration.
 */
final class Logging extends BasicConfigurator {

  /* Use the program's logging level. */
  override def configure(loggerContext: LoggerContext): Unit = {
    super.configure(loggerContext)
    loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).setLevel(Program.loggingLevel)
  }

}
