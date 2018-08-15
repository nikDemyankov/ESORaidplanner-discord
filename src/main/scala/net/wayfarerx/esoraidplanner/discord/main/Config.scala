/*
 * Config.scala
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

import java.io.FileInputStream
import java.util.Properties

import collection.JavaConverters._
import cats.effect.IO

/**
 * Utility that loads settings from configuration files.
 */
object Config {

  /**
   * Attempts to load setting values from the specified resource.
   *
   * @param path The path to the file to load.
   * @return The result of attempting to load setting values from the specified resource.
   */
  def apply(path: String): IO[List[String]] = for {
    data <- IO(new FileInputStream(path)).bracket { stream =>
      IO {
        val p = new Properties()
        p.load(stream)
        p.asScala.toMap map { case (k, v) => k.toLowerCase -> v }
      }
    }(s => IO(s.close()))
  } yield data.toList flatMap {
    case (key, value) if value.trim.isEmpty => List(s"--$key")
    case (key, value) => List(s"--$key", value)
  }

}
