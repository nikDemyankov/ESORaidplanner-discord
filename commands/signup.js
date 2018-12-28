exports.run = (client, message, args) => {

    if (undefined === args[0] || undefined === args[1] || undefined === args[2]) {
        message.channel.send(message.author.toString() + ' Please use the correct command format to sign up. `!signup event_id class role`');
        return;
    }

    const classes = {
        "dk": 1,
        "dragonknight": 1,
        "d": 1,
        "sorc": 2,
        "sorcerer": 2,
        "s": 2,
        "nb": 3,
        "nightblade": 3,
        "n": 3,
        "warden": 4,
        "w": 4,
        "templar": 6,
        "temp": 6,
        "t": 6,
    };

    const roles = {
        "tank": 1,
        "t": 1,
        "healer": 2,
        "heal": 2,
        "h": 2,
        "magickadd": 3,
        "magicka": 3,
        "mdd": 3,
        "m": 3,
        "staminadd": 4,
        "stamdd": 4,
        "sdd": 4,
        "s": 4,
        "other": 5,
        "o": 5,
    };

    if (classes[args[1]] === undefined) {
        message.channel.send(message.author.toString() + ' Please use a valid class to sign up.');
        return;
    }

    if (roles[args[2]] === undefined) {
        message.channel.send(message.author.toString() + ' Please use a valid role to sign up.');
        return;
    }

    const https = require('https');

    var interim = '';

    const data = JSON.stringify({
        discord_user_id: message.author.id,
        discord_channel_id: message.channel.id,
        discord_server_id: message.guild.id,
        event_id: args[0],
        class: classes[args[1]],
        role: roles[args[2]]
    });

    var auth = "Basic " + Buffer.from(client.config.networkToken).toString("base64");

    const options = {
        host: "esoraidplanner.com",
        path: "https://esoraidplanner.com/api/discord/signup",
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