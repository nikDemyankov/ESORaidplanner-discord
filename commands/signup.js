const character = require('../character');
const api = require('../external-api');

const invalidCommandFormatMessage = author =>
  `${author} Please use the correct command format to sign up. \`!signup event_id class role\``;

const invalidClassMessage = author => `${author} Please use a valid class to sign up.`;

const invalidRoleMessage = author => `${author} Please use a valid role to sign up.`;

const getData = (message, args) => {
  const author = message.author.toString();
  if (undefined === args[0] || undefined === args[1]) {
    throw new Error(invalidCommandFormatMessage(author));
  }

  const data = {
    discord_user_id: message.author.id,
    discord_channel_id: message.channel.id,
    discord_server_id: message.guild.id,
    discord_handle: message.author.tag,
    event_id: args[0],
  };

  if (args[1] && args[1].startsWith('"')) {
    const characterName = args[1].replace(/"/g, '');

    return {
      ...data,
      preset: characterName,
    };
  }

  if (undefined === args[2]) {
    throw new Error(invalidCommandFormatMessage(author));
  }

  const characterClass = character.class[args[1]];
  if (undefined === characterClass) {
    throw new Error(invalidClassMessage(author));
  }

  const characterRole = character.role[args[2]];
  if (undefined === characterRole) {
    throw new Error(invalidRoleMessage(author));
  }

  return {
    ...data,
    class: characterClass,
    role: characterRole,
  };
};

exports.run = (client, message, args) => {
  try {
    const data = getData(message, args);
    const options = { data, token: client.config.networkToken, method: 'signup' };

    api.sendRequest(options, response => message.channel.send(response));
  } catch (e) {
    message.channel.send(e.message);
  }
};
