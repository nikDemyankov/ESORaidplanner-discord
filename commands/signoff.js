exports.run = (client, message, args) => {

    if (undefined === args[0]) {
        message.channel.send(message.author.toString() + ' Please use the correct command format to sign up. `!signoff event_id`');
        return;
    }

    const https = require('https');

    var interim = '';

    const data = JSON.stringify({
        discord_user_id: message.author.id,
        discord_channel_id: message.channel.id,
        event_id: args[0],
    });

    var auth = "Basic " + Buffer.from(client.config.networkToken).toString("base64");

    const options = {
        host: "esoraidplanner.com",
        path: "https://esoraidplanner.com/api/discord/signoff",
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Content-Length': data.length,
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

    req.write(data);
};