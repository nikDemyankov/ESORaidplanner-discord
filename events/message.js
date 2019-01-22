const R = require('ramda');

const messageHandler = (client, message) => {
  // Ignore all bots
  if (message.author.bot) return;

  // Ignore messages not starting with the prefix (in config.json)
  if (message.content.indexOf(client.config.prefix) !== 0) return;

  // Our standard argument/command name definition.
  const args = message.content
    .toLowerCase()
    .slice(client.config.prefix.length)
    .trim()
    .split(/ +/g);
  const command = args.shift().toLowerCase();

  // Grab the command data from the client.commands Enmap
  const cmd = client.commands.get(command);

  // If that command doesn't exist, silently exit and do nothing
  if (!cmd) return;

  if (!message.channel.permissionsFor(message.guild.me).has('SEND_MESSAGES', false)) {
    return;
  }

  if (!message.channel.permissionsFor(message.guild.me).has('EMBED_LINKS', false)) {
    message.channel.send(
      'I do not have the correct rights in this server. I need `send messages` and `embed links` permissions.',
    );
    return;
  }

  // Run the command
  cmd.run(client, message, args);
};

module.exports = R.curry(messageHandler);
