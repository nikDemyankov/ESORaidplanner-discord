/*
 * Setting.scala
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

import concurrent.duration._
import util.{Failure, Success, Try}

import cats.effect.IO

/**
 * Base type for all command-line settings.
 *
 * @tparam T The type of configuration that this setting applies to.
 */
sealed trait Setting[T] {

  /** The name of this setting. */
  def name: String

  /** The optional alias of this setting. */
  def alias: Option[Char]

  /** The description of this setting. */
  def description: String

  /** True if this setting must be specified. */
  def required: Boolean

  /** True if this setting takes a parameter. */
  def parameterized: Boolean

  /**
   * Applies this setting to a configuration object.
   *
   * @param config The configuration object to modify.
   * @param value  The value to apply with this setting.
   * @return The resulting configuration object if it could be modified.
   */
  def apply(config: T, value: String): Option[T]

}

/**
 * Factory for settings.
 */
object Setting {

  /**
   * Creates a setting that has no alias and takes no parameter.
   *
   * @tparam T The type of configuration that the setting applies to.
   * @param name        The name of the setting.
   * @param description The description of the setting.
   * @param f           The function that modifies the configuration object.
   * @return The new setting.
   */
  def flag[T](name: String, description: String)(f: T => Option[T]): Setting[T] =
    flag(name, None, description)(f)

  /**
   * Creates a setting that has an alias but takes no parameter.
   *
   * @tparam T The type of configuration that the setting applies to.
   * @param name        The name of the setting.
   * @param alias       The alias of the setting.
   * @param description The description of the setting.
   * @param f           The function that modifies the configuration object.
   * @return The new setting.
   */
  def flag[T](name: String, alias: Char, description: String)(f: T => Option[T]): Setting[T] =
    flag(name, Some(alias), description)(f)

  /**
   * Creates a setting that has an optional alias but takes no parameter.
   *
   * @tparam T The type of configuration that the setting applies to.
   * @param name        The name of the setting.
   * @param alias       The optional alias of the setting.
   * @param description The description of the setting.
   * @param f           The function that modifies the configuration object.
   * @return The new setting.
   */
  def flag[T](name: String, alias: Option[Char], description: String)(f: T => Option[T]): Setting[T] =
    new Impl[T](name, alias, description, false, false, (c, _) => f(c))

  /**
   * Creates a setting that has no alias but takes a parameter.
   *
   * @tparam T The type of configuration that the setting applies to.
   * @param name        The name of the setting.
   * @param description The description of the setting.
   * @param f           The function that takes a parameter and modifies the configuration object.
   * @return The new setting.
   */
  def option[T](name: String, description: String)(f: (T, String) => Option[T]): Setting[T] =
    option(name, None, description)(f)

  /**
   * Creates a setting that has an alias and takes a parameter.
   *
   * @tparam T The type of configuration that the setting applies to.
   * @param name        The name of the setting.
   * @param alias       The alias of the setting.
   * @param description The description of the setting.
   * @param f           The function that takes a parameter and modifies the configuration object.
   * @return The new setting.
   */
  def option[T](name: String, alias: Char, description: String)(f: (T, String) => Option[T]): Setting[T] =
    option(name, Some(alias), description)(f)

  /**
   * Creates a setting that has an optional alias and takes a parameter.
   *
   * @tparam T The type of configuration that the setting applies to.
   * @param name        The name of the setting.
   * @param alias       The optional alias of the setting.
   * @param description The description of the setting.
   * @param required    True if this option is required (defaults to false).
   * @param f           The function that takes a parameter and modifies the configuration object.
   * @return The new setting.
   */
  def option[T](name: String, alias: Option[Char], description: String, required: Boolean = false)(
    f: (T, String) => Option[T]): Setting[T] =
    new Impl[T](name, alias, description, required, true, f)

  /**
   * A provider for collections of settings.
   *
   * @tparam T The type of configuration that the settings apply to.
   */
  trait Provider[T] {

    /** Alias for the configuration type. */
    final type Config = T

    /** The default configuration value. */
    def Default: T

    /** The list of all provided settings. */
    val Settings: Vector[Setting[T]]

    /**
     * Attempts to create a configuration using this provider.
     *
     * @param values The settings & values to create a configuration with.
     * @return The result of attempting to create a configuration.
     */
    final def configure(values: Map[Setting[_], String]): IO[T] = {

      @annotation.tailrec
      def scan(remaining: Vector[Setting[T]], result: T): Try[T] = remaining match {
        case setting +: next => values get setting match {
          case Some(value) => setting(result, value) match {
            case Some(newResult) => scan(next, newResult)
            case None => Failure(new IllegalArgumentException(Messages.invalidSettingValue(setting, value)))
          }
          case None => scan(next, result)
        }
        case _ => Success(result)
      }

      scan(Settings, Default) match {
        case Success(value) => IO.pure(value)
        case Failure(exception) => IO.raiseError(exception)
      }
    }

  }

  /**
   * Utility to help with parsing ints.
   */
  object Ints {

    /**
     * Wraps an int handler with a string handler.
     *
     * @tparam T The type of configuration that the setting applies to.
     * @param f The function that takes an int and modifies the configuration object.
     * @return A string handler wrapping the specified int handler.
     */
    def apply[T](f: (T, Int) => Option[T]): (T, String) => Option[T] =
      (config, string) => Try(string.toInt).toOption flatMap (f(config, _))

  }

  /**
   * Utility to help with parsing durations.
   */
  object Durations {

    /**
     * Wraps a duration handler with a string handler.
     *
     * @tparam T The type of configuration that the setting applies to.
     * @param f The function that takes a duration and modifies the configuration object.
     * @return A string handler wrapping the specified duration handler.
     */
    def apply[T](f: (T, Duration) => Option[T]): (T, String) => Option[T] =
      (config, string) => Try(Duration(string)).toOption flatMap (f(config, _))

  }

  /**
   * Utility to help with parsing finite durations.
   */
  object FiniteDurations {

    /**
     * Wraps a duration handler with a string handler.
     *
     * @tparam T The type of configuration that the setting applies to.
     * @param f The function that takes a duration and modifies the configuration object.
     * @return A string handler wrapping the specified duration handler.
     */
    def apply[T](f: (T, FiniteDuration) => Option[T]): (T, String) => Option[T] =
      (config, string) => Try(Duration(string)).toOption collect {
        case finite: FiniteDuration => finite
      } flatMap (f(config, _))

  }

  /**
   * Implementation of all settings.
   *
   * @param name          The name of this setting.
   * @param alias         The optional alias of this setting.
   * @param description   The description of this setting.
   * @param required      True if this setting must be specified.
   * @param parameterized True if this setting takes a parameter.
   * @param f             The function that might take a parameter but always modifies the configuration object.
   * @tparam T The type of configuration that this setting applies to.
   */
  private final class Impl[T](
    override val name: String,
    override val alias: Option[Char],
    override val description: String,
    override val required: Boolean,
    override val parameterized: Boolean,
    f: (T, String) => Option[T]
  ) extends Setting[T] {

    /* Apply the configuration function. */
    override def apply(config: T, setting: String): Option[T] = f(config, setting)

  }

}