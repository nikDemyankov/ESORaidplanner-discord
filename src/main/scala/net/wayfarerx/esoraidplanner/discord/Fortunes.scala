/*
 * Fortunes.scala
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

import scala.io.Source

import cats.effect.IO

/**
 * An in-memory cache of ESO loading screen tips.
 */
object Fortunes {

  /** The cached fortunes. */
  @volatile
  private var _fortunes: Vector[String] = Vector.empty

  /** The accessor for fortunes. */
  private lazy val fortunes: IO[Vector[String]] = IO(_fortunes) flatMap { _f =>
    if (_f.nonEmpty) IO.pure(_f) else
      IO(Source.fromResource("fortunes.txt", getClass.getClassLoader)).bracket { source =>
        IO {
          val f = source.getLines.toVector
          _fortunes = f
          f
        }
      }(s => IO(s.close()))
  }

  /** Returns a random fortune. */
  def apply(): IO[String] =
    fortunes map (f => f(Math.floor(Math.random() * f.length).toInt))

}
