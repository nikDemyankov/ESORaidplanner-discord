const Discord = require('discord.js');
const Enmap = require('enmap');
const R = require('ramda');
const fs = require('fs');
const events = require('./events');
const config = require('./config.json');

const client = new Discord.Client();
// We also need to make sure we're attaching the config to the CLIENT so it's accessible everywhere!
client.config = config;

client.on('ready', () => {
  client.user.setActivity('planning for ' + client.guilds.size + ' guilds');
  console.log('Bot ready!');
});
client.on('guildCreate', guild => {
  client.user.setActivity('planning for ' + client.guilds.size + ' guilds');
});
client.on('guildDelete', guild => {
  client.user.setActivity('planning for ' + client.guilds.size + ' guilds');
});

R.mapObjIndexed((eventHandler, eventName) => {
  client.on(eventName, eventHandler(client));
}, events);

client.commands = new Enmap();

fs.readdir('./commands/', (err, files) => {
  if (err) return console.error(err);
  files.forEach(file => {
    if (!file.endsWith('.js')) return;
    let props = require(`./commands/${file}`);
    let commandName = file.split('.')[0];
    console.log(`Attempting to load command ${commandName}`);
    client.commands.set(commandName, props);
  });
});

client.login(config.token);
