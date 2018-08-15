# ESORaidplanner-discord

A Discord bot for ESO Raidplanner.

## Building

ESORaidplanner-discord uses [SBT](https://www.scala-sbt.org/) to produce a single executable JAR file. In the project's
root directory run `sbt assembly` to generate `target/scala-2.12/esoraidplanner-discord.jar`.

## Running

```
Usage: java -jar esoraidplanner-discord.jar <setting ...>
```

### Required Settings

```
-t | --bot-token <value>     The token used for authentication with Discord.
-c | --client-token <value>  The authorization token for connecting to ESO Raidplanner.
```

### Optional Settings

```
-u | --client-url <value>      The base URL to use for all client operations.
-a | --server-address <value>  The address to bind server operations to.
-p | --server-port <value>     The port to bind server operations to.
-q | --quiet                   Decreases the amount of logging.
-v | --verbose                 Increases the amount of logging.
-h | --help                    Shows this message and exits.
```

### Discord Bot Settings

```
--bot-max-missed-pings <value>              Max number of heartbeats Discord can not respond to before a reconnect
                                            is initiated.
--bot-shard-count <value>                   The number of shards the bot should create and manage.
--bot-use-recommended-shard-count           Tells the bot to request the number of shards to login with from Discord.
--bot-ignore-recommended-shard-count        Tells the bot to ignore the recommended number of shards to login with
                                            from Discord.
--bot-max-reconnect-attempts <value>        Max number of attempts shards managed by the bot will make to reconnect
                                            to Discord.
--bot-max-message-cache-count <value>       Max number of messages which are cached for each channel.
--bot-5xx-retry-count <value>               Max number of retries that should be attempted for HTTP requests that
                                            result in a 5xx response.
--bot-minimum-dispatch-threads <value>      Min number of threads which must be alive at any given time in the bot's
                                            dispatcher.
--bot-maximum-dispatch-threads <value>      Max number of threads which must be alive at any given time in the bot's
                                            dispatcher.
--bot-idle-dispatch-thread-timeout <value>  Amount of time extra threads in the bot's dispatcher are allowed to be
                                            idle before they are killed.
--bot-event-overflow-capacity <value>       Number of events the bot's dispatcher can overflow by without calling
                                            the back-pressure handler.
```

### HTTP Client Settings

```
--client-response-header-timeout <value>  Duration between the submission of a request and the completion of the
                                          response header.
--client-idle-timeout <value>             Duration that a connection can wait without traffic being read or written
                                          before timeout.
--client-request-timeout <value>          Maximum duration from the submission of a request through reading the body
                                          before a timeout.
--client-max-total-connections <value>    Maximum connections the client will have at any specific time.
--client-max-wait-queue-limit <value>     Maximum number requests waiting for a connection at any specific time.
--client-check-endpoint-identification    Enables verifying that the certificate presented matches the hostname of
                                          the request.
--client-ignore-endpoint-identification   Disables verifying that the certificate presented matches the hostname of
                                          the request.
--client-max-response-line-size <value>   Maximum length of the request line.
--client-max-header-length <value>        Maximum length of headers.
--client-max-chunk-size <value>           Maximum size of chunked content chunks.
--client-lenient-parser                   A lenient parser will accept illegal chars but replaces them with ?
                                          (0xFFFD).
--client-strict-parser                    A strict parser will not accept illegal chars.
--client-buffer-size <value>              Internal buffer size of the HTTP client.
```

### HTTP Server Settings

```
--server-max-request-line-length <value>  Maximum HTTP request line length to parse.
--server-max-headers-length <value>       Maximum data that compose HTTP headers.
--server-idle-timeout <value>             Period of time a connection can remain idle before the connection is timed
                                          out and disconnected.
--server-connector-pool-size <value>      Number of worker threads for the new socket server group.
--server-buffer-size <value>              Buffer size to use for IO operations.
```

## The Discord Bot

In any Discord channel that ESORaidplanner-discord connects to users can edit their participation status in ESO
Raidplanner events. The bot currently supports two commands:

 - `!setup [guild]` Links an ESO Raidplanner guild to this Discord server where
   - `[guild]` is the optional ESO Raidplanner guild ID.

 - `!events` Lists the events in the linked ESO Raidplanner guild.

 - `!signup <event> <class> <role>` Signs up for an event where
   - `<event>` is the ESO Raidplanner event ID
   - `<class>` is one of
     - `Dragonknight`
     - `Nightblade`
     - `Sorcerer`
     - `Templar`
     - `Warden`
   - `<role>` is one of
     - `Tank`
     - `Healer`
     - `MagickaDPS`
     - `StaminaDPS`
     - `Other`
     
 - `!signout <event>` where
   - `<event>` is the ESO Raidplanner event ID

## Push Notifications

ESORaidplanner-discord contains an embedded HTTP server (located at [http://localhost:7224](http://localhost:7224) by
default) that enables external push notification delivery to Discord channels. See
[http://localhost:7224/push](http://localhost:7224/push) for an example of how push notifications are published.