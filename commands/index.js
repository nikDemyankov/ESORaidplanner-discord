// Contains all commands

const eventsCommand = require('./events');
const helpCommand = require('./help');
const setupCommand = require('./setup');
const signoffCommand = require('./signoff');
const signupCommand = require('./signup');
const signupsCommand = require('./signups');
const statusCommand = require('./status');

module.exports = {
  events: eventsCommand,
  help: helpCommand,
  setup: setupCommand,
  signoff: signoffCommand,
  signup: signupCommand,
  signups: signupsCommand,
  status: statusCommand,
};
