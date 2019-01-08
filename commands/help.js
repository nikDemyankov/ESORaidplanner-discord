const api = require('../external-api');

const responseHandler = message => response => {
  try {
    const m = JSON.parse(response);
    const e = m.embeds.pop();
    message.channel.send({ embed: e });
  } catch (e) {
    message.channel.send(response);
  }
};

exports.run = (client, message, args) => {
  const data = {
    discord_user_id: message.author.id,
    discord_channel_id: message.channel.id,
    discord_server_id: message.guild.id,
    discord_handle: message.author.tag,
  };
  const options = { data, token: client.config.networkToken, method: 'help' };

  api.sendRequest(options, responseHandler(message));
};
