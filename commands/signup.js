const character = require('../character');

exports.run = (client, message, args) => {
  if (undefined !== args[1] && args[1].startsWith('"')) {
    var characterName = args[1].replace(/"/g, '');

    var data = JSON.stringify({
      discord_user_id: message.author.id,
      discord_channel_id: message.channel.id,
      discord_server_id: message.guild.id,
      discord_handle: message.author.tag,
      event_id: args[0],
      preset: characterName,
    });
  } else {
    if (undefined === args[0] || undefined === args[1] || undefined === args[2]) {
      message.channel.send(
        message.author.toString() +
          ' Please use the correct command format to sign up. `!signup event_id class role`',
      );
      return;
    }

    const characterClass = character.class[args[1]];
    if (characterClass === undefined) {
      message.channel.send(message.author.toString() + ' Please use a valid class to sign up.');
      return;
    }

    const characterRole = character.role[args[2]];
    if (characterRole === undefined) {
      message.channel.send(message.author.toString() + ' Please use a valid role to sign up.');
      return;
    }

    var data = JSON.stringify({
      discord_user_id: message.author.id,
      discord_channel_id: message.channel.id,
      discord_server_id: message.guild.id,
      discord_handle: message.author.tag,
      event_id: args[0],
      class: characterClass,
      role: characterRole,
    });
  }

  const https = require('https');
  var interim = '';
  var auth = 'Basic ' + Buffer.from(client.config.networkToken).toString('base64');

  const options = {
    host: 'esoraidplanner.com',
    path: 'https://esoraidplanner.com/api/discord/signup',
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Content-Length': data.length,
      Authorization: auth,
    },
  };

  var req = https.request(options, function(res) {
    res.on('data', chunk => {
      interim += chunk;
    });
    res.on('end', () => {
      message.channel.send(interim);
    });
  });

  req.write(data);
};
