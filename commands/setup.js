exports.run = (client, message, args) => {
    const https = require('https');

    var interim = '';

    var data = {
        discord_user_id: message.author.id,
        discord_channel_id: message.channel.id,
        discord_server_id: message.guild.id,
    };

    if (null !== args[0]) {
        data.guild_id = args[0];
    }

    var auth = "Basic " + Buffer.from(client.config.networkToken).toString("base64");
    const jsondata = JSON.stringify(data);

    const options = {
        host: "esoraidplanner.com",
        path: "https://esoraidplanner.com/api/discord/setup",
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Content-Length': jsondata.length,
            "Authorization": auth
        }
    };

    var req = https.request(options, function (res) {
        res.on('data', (chunk) => {
            interim += chunk;
        });
        res.on('end', () => {
            message.channel.send(interim);
        });
    });

    req.write(jsondata);
};