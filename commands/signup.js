const character = require('../character');
const api = require('../external-api');

exports.run = (client, message, args) => {
  let data;
  const author = message.author.toString();

  if (undefined !== args[1] && args[1].startsWith('"')) {
    const characterName = args[1].replace(/"/g, '');

    data = {
      discord_user_id: message.author.id,
      discord_channel_id: message.channel.id,
      discord_server_id: message.guild.id,
      discord_handle: message.author.tag,
      event_id: args[0],
      preset: characterName,
    };
  } else {
    if (undefined === args[0] || undefined === args[1] || undefined === args[2]) {
      message.channel.send(
        `${author} Please use the correct command format to sign up. \`!signup event_id class role\``,
      );
      return;
    }

    const characterClass = character.class[args[1]];
    if (characterClass === undefined) {
      message.channel.send(`${author} Please use a valid class to sign up.`);
      return;
    }

    const characterRole = character.role[args[2]];
    if (characterRole === undefined) {
      message.channel.send(`${author} Please use a valid role to sign up.`);
      return;
    }

    data = {
      discord_user_id: message.author.id,
      discord_channel_id: message.channel.id,
      discord_server_id: message.guild.id,
      discord_handle: message.author.tag,
      event_id: args[0],
      class: characterClass,
      role: characterRole,
    };
  }

  const options = { data, token: client.config.networkToken, method: 'signup' };
  api.sendRequest(options, response => message.channel.send(response));
};
