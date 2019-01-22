const messageEvent = require('../message');

const command = {
  run: jest.fn(),
};

const message = {
  author: {
    bot: false,
  },
  guild: {
    me: 'my-guild',
  },
  content: '!some_command hello world',
  channel: {
    permissionsFor: jest.fn(() => ({ has: () => true })),
    send: jest.fn(),
  },
};

const client = {
  config: {
    prefix: '!',
  },
  commands: {
    get: jest.fn(() => command),
  },
};

beforeEach(() => {
  jest.clearAllMocks();
});

describe('Got message from the bot', () => {
  const botMessage = {
    ...message,
    author: {
      bot: true,
    },
  };

  beforeEach(() => {
    messageEvent(client, botMessage);
  });

  it('Should be ignored', () => {
    expect(command.run).not.toHaveBeenCalled();
  });
});

describe('Got message with invalid prefix', () => {
  const badMessage = {
    ...message,
    content: '#invalid_prefix hello world',
  };

  beforeEach(() => {
    messageEvent(client, badMessage);
  });

  it('Should be ignored', () => {
    expect(command.run).not.toHaveBeenCalled();
  });
});

describe('Got unknown command', () => {
  const clientWithNoCommands = {
    ...client,
    commands: {
      get: jest.fn(() => undefined),
    },
  };

  beforeEach(() => {
    messageEvent(clientWithNoCommands, message);
  });

  it('Should try to get command handler from the client', () => {
    expect(clientWithNoCommands.commands.get).toHaveBeenCalledTimes(1);
    expect(clientWithNoCommands.commands.get).toHaveBeenCalledWith('some_command');
  });

  it('Should ignore the unknown command', () => {
    expect(command.run).not.toHaveBeenCalled();
  });
});

describe('Does not have a `SEND_MESSAGES` permission', () => {
  const permissionCheck = {
    has: jest.fn(permission => permission !== 'SEND_MESSAGES'),
  };

  const channelNoPermission = {
    ...message,
    channel: {
      permissionsFor: jest.fn(() => permissionCheck),
      send: jest.fn(),
    },
  };

  beforeEach(() => {
    messageEvent(client, channelNoPermission);
  });

  it('Should check if permission is set', () => {
    expect(permissionCheck.has).toHaveBeenCalledTimes(1);
    expect(permissionCheck.has).toHaveBeenCalledWith('SEND_MESSAGES', false);
  });

  it('Should not execute command', () => {
    expect(command.run).not.toHaveBeenCalled();
  });
});

describe('Does not have a `EMBED_LINKS` permission', () => {
  const permissionCheck = {
    has: jest.fn(permission => permission !== 'EMBED_LINKS'),
  };

  const channelNoPermission = {
    ...message,
    channel: {
      permissionsFor: jest.fn(() => permissionCheck),
      send: jest.fn(),
    },
  };

  beforeEach(() => {
    messageEvent(client, channelNoPermission);
  });

  it('Should check if permission is set', () => {
    expect(permissionCheck.has).toHaveBeenLastCalledWith('EMBED_LINKS', false);
  });

  it('Should send error message to the channel', () => {
    expect(channelNoPermission.channel.send).toHaveBeenCalledWith(
      'I do not have the correct rights in this server. I need `send messages` and `embed links` permissions.',
    );
  });

  it('Should not execute command', () => {
    expect(command.run).not.toHaveBeenCalled();
  });
});

describe('Got valid command message', () => {
  beforeEach(() => {
    messageEvent(client, message);
  });

  const commandProp = index => command.run.mock.calls[0][index];

  it('Should execute command once', () => {
    expect(command.run).toHaveBeenCalledTimes(1);
  });

  it('Should execute command with correct client object', () => {
    const actual = commandProp(0);
    expect(actual).toEqual(client);
  });

  it('Should execute command with correct message object', () => {
    const actual = commandProp(1);
    expect(actual).toEqual(message);
  });

  it('Should pass message arguments to command', () => {
    const actual = commandProp(2);
    const expected = ['hello', 'world'];
    expect(actual).toEqual(expected);
  });
});
