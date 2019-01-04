exports.run = (client, message, args) => {
  const https = require('https');

  var interim = '';

  const data = JSON.stringify({
    discord_user_id: message.author.id,
    discord_channel_id: message.channel.id,
    discord_server_id: message.guild.id,
    discord_handle: message.author.tag,
  });

  var auth = 'Basic ' + Buffer.from(client.config.networkToken).toString('base64');

  const options = {
    host: 'esoraidplanner.com',
    path: 'https://esoraidplanner.com/api/discord/help',
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
      try {
        var m = JSON.parse(interim);
        var e = m.embeds.pop();
        message.channel.send({ embed: e });
      } catch (e) {
        message.channel.send(interim);
      }
    });
  });

  req.write(data);
};
