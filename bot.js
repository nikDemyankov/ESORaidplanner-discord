const Discord = require('discord.js');
const Enmap = require('enmap');
const R = require('ramda');
const events = require('./events');
const commands = require('./commands');
const config = require('./config.json');

const client = new Discord.Client();
// We also need to make sure we're attaching the config to the CLIENT so it's accessible everywhere!
client.config = config;

client.on('ready', () => {
  client.user.setActivity(`planning for ${client.guilds.size} guilds`);
  console.log('Bot ready!');
});
client.on('guildCreate', guild => {
  client.user.setActivity(`planning for ${client.guilds.size} guilds`);
});
client.on('guildDelete', guild => {
  client.user.setActivity(`planning for ${client.guilds.size} guilds`);
});

// set event handlers
R.mapObjIndexed((eventHandler, eventName) => {
  client.on(eventName, eventHandler(client));
}, events);

client.commands = new Enmap();

// set command handlers
R.mapObjIndexed((commandHandler, commandName) => {
  console.log(`Attempting to load command ${commandName}: `, commandHandler);
  client.commands.set(commandName, commandHandler);
}, commands);

client.login(config.token);
