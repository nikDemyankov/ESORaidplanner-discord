/*
 * Bots.scala
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

import sx.blah.discord.api.ClientBuilder

/**
 * Configuration factory for the Discord bot.
 */
object Bots extends Setting.Provider[ClientBuilder] {

  /* The default configuration value. */
  override def Default: Config = new ClientBuilder()
    .setDaemon(true)
    .withRecommendedShardCount(true)
    .setMaxReconnectAttempts(Int.MaxValue)
    .set5xxRetryCount(Int.MaxValue)

  /* The list of all Discord bot settings. */
  override val Settings: Vector[Setting[ClientBuilder]] = Vector(

    Setting.option[Config]("bot-max-missed-pings",
      "Max number of heartbeats Discord can not respond to before a reconnect is initiated.")(
      Setting.Ints((config, value) => Some(config.withPingTimeout(value)))),

    Setting.option[Config]("bot-shard-count",
      "The number of shards the bot should create and manage.")(
      Setting.Ints((config, value) => Some(config.withShards(value)))),

    Setting.flag[Config]("bot-use-recommended-shard-count",
      "Tells the bot to request the number of shards to login with from Discord.")(
      config => Some(config.withRecommendedShardCount(true))),

    Setting.flag[Config]("bot-ignore-recommended-shard-count",
      "Tells the bot to ignore the recommended number of shards to login with from Discord.")(
      config => Some(config.withRecommendedShardCount(false))),

    Setting.option[Config]("bot-max-reconnect-attempts",
      "Max number of attempts shards managed by the bot will make to reconnect to Discord.")(
      Setting.Ints((config, value) => Some(config.setMaxReconnectAttempts(value)))),

    Setting.option[Config]("bot-max-message-cache-count",
      "Max number of messages which are cached for each channel.")(
      Setting.Ints((config, value) => Some(config.setMaxMessageCacheCount(value)))),

    Setting.option[Config]("bot-5xx-retry-count",
      "Max number of retries that should be attempted for HTTP requests that result in a 5xx response.")(
      Setting.Ints((config, value) => Some(config.set5xxRetryCount(value)))),

    Setting.option[Config]("bot-minimum-dispatch-threads",
      "Min number of threads which must be alive at any given time in the bot's dispatcher.")(
      Setting.Ints((config, value) => Some(config.withMinimumDispatchThreads(value)))),

    Setting.option[Config]("bot-maximum-dispatch-threads",
      "Max number of threads which must be alive at any given time in the bot's dispatcher.")(
      Setting.Ints((config, value) => Some(config.withMaximumDispatchThreads(value)))),

    Setting.option[Config]("bot-idle-dispatch-thread-timeout",
      "Amount of time extra threads in the bot's dispatcher are allowed to be idle before they are killed.")(
      Setting.FiniteDurations((config, value) => Some(config.withIdleDispatchThreadTimeout(value.length, value.unit)))),

    Setting.option[Config]("bot-event-overflow-capacity",
      "Number of events the bot's dispatcher can overflow by without calling the back-pressure handler.")(
      Setting.Ints((config, value) => Some(config.withEventOverflowCapacity(value))))

  )

}
