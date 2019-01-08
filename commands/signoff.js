const api = require('../external-api');

exports.run = (client, message, args) => {
  if (undefined === args[0]) {
    const author = message.author.toString();
    message.channel.send(
      `${author} Please use the correct command format to sign off. \`!signoff event_id\``,
    );
    return;
  }

  const data = {
    discord_user_id: message.author.id,
    discord_channel_id: message.channel.id,
    discord_server_id: message.guild.id,
    discord_handle: message.author.tag,
    event_id: args[0],
  };
  const options = { data, token: client.config.networkToken, method: 'signoff' };
  api.sendRequest(options, response => message.channel.send(response));
};
