const events = require('../events');
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
    expect(events.run).toBeDefined();
  });

  it('`run` should be a function', () => {
    expect(typeof events.run).toBe('function');
  });
});

describe('Call `run` with success', () => {
  beforeEach(() => {
    events.run(client, message, []);
  });

  it('Request is executed once', () => {
    expect(api.sendRequest).toHaveBeenCalledTimes(1);
  });

  it('Correct data is send', () => {
    const expected = {
      data: {
        discord_channel_id: 'some-channel',
        discord_handle: 'some-author-tag',
        discord_server_id: 'some-guild',
        discord_user_id: 'some-author',
      },
      method: 'events',
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
