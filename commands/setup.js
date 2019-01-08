const api = require('../external-api');

exports.run = (client, message, args) => {
  let data = {
    discord_user_id: message.author.id,
    discord_channel_id: message.channel.id,
    discord_server_id: message.guild.id,
    discord_handle: message.author.tag,
  };
  if (null !== args[0]) {
    data.guild_id = args[0];
  }

  const options = { data, token: client.config.networkToken, method: 'setup' };
  api.sendRequest(options, response => message.channel.send(response));
};
