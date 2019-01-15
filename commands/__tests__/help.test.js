const help = require('../help');
const api = require('../../external-api');

jest.mock('../../external-api', () => ({
  sendRequest: jest.fn((options, callback) => callback('some server response')),
}));

const client = {
  config: {
    networkToken: 'some-token',
  },
};

const message = {
  author: {
    id: 'some-author',
    tag: 'some-author-tag',
  },
  channel: {
    id: 'some-channel',
    send: jest.fn(),
  },
  guild: {
    id: 'some-guild',
  },
};

beforeEach(() => {
  jest.clearAllMocks();
});

describe('Object structure', () => {
  it('`run` property should be defined', () => {
    expect(help.run).toBeDefined();
  });

  it('`run` should be a function', () => {
    expect(typeof help.run).toBe('function');
  });
});

describe('Call `run` with success', () => {
  beforeEach(() => {
    help.run(client, message, []);
  });

  it('Request is executed once', () => {
    expect(api.sendRequest).toHaveBeenCalledTimes(1);
  });

  it('Correct data is send', () => {
    const expected = {
      data: {
        discord_user_id: 'some-author',
        discord_channel_id: 'some-channel',
        discord_server_id: 'some-guild',
        discord_handle: 'some-author-tag',
      },
      method: 'help',
      token: 'some-token',
    };

    const actual = api.sendRequest.mock.calls[0][0];

    expect(actual).toEqual(expected);
  });

  it('Should send server response to the channel only once', () => {
    expect(message.channel.send).toHaveBeenCalledTimes(1);
  });

  it('Should send server response to the channel', () => {
    expect(message.channel.send).toHaveBeenCalledWith('some server response');
  });
});
