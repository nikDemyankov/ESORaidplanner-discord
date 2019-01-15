const signoff = require('../signoff');
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
    toString: () => 'some-author',
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
    expect(signoff.run).toBeDefined();
  });

  it('`run` should be a function', () => {
    expect(typeof signoff.run).toBe('function');
  });
});

describe('Arguments are empty', () => {
  beforeEach(() => {
    signoff.run(client, message, []);
  });

  it('Error message is send to the channel', () => {
    expect(message.channel.send).toHaveBeenCalledTimes(1);
  });

  it('Correct error message is send', () => {
    expect(message.channel.send).toHaveBeenCalledWith(
      'some-author Please use the correct command format to sign off. `!signoff event_id`',
    );
  });

  it('Request is not executed', () => {
    expect(api.sendRequest).not.toHaveBeenCalled();
  });
});

describe('Call `run` successfully', () => {
  beforeEach(() => {
    signoff.run(client, message, [123]);
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
        event_id: 123,
      },
      method: 'signoff',
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
