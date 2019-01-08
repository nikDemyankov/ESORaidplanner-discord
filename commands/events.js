const api = require('../external-api');

exports.run = (client, message, args) => {
  const data = {
    discord_user_id: message.author.id,
    discord_channel_id: message.channel.id,
    discord_server_id: message.guild.id,
    discord_handle: message.author.tag,
  };
  const options = { data, token: client.config.networkToken, method: 'events' };

  api.sendRequest(options, response => message.channel.send(response));
};
