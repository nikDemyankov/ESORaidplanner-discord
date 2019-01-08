const help = require('../help');
const https = require('https');

jest.mock('https');

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

const requestOptions = {
  headers: {
    Authorization: 'Basic c29tZS10b2tlbg==',
    'Content-Length': 137,
    'Content-Type': 'application/json',
  },
  host: 'esoraidplanner.com',
  method: 'POST',
  path: 'https://esoraidplanner.com/api/discord/help',
};

const response = {
  on: jest.fn((eventName, callback) => callback('some server response')),
};

const request = {
  write: jest.fn(),
};

https.request.mockImplementation((options, responseCallback) => {
  responseCallback(response);
  return request;
});

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
    expect(https.request).toHaveBeenCalledTimes(1);
  });

  it('Request executed with correct options', () => {
    const optionsCall = https.request.mock.calls[0][0];
    expect(optionsCall).toEqual(requestOptions);
  });

  it('Data is send once', () => {
    expect(request.write).toHaveBeenCalledTimes(1);
  });

  it('Correct data is send', () => {
    const data =
      '{"discord_user_id":"some-author","discord_channel_id":"some-channel","discord_server_id":"some-guild","discord_handle":"some-author-tag"}';

    expect(request.write).toHaveBeenCalledWith(data);
  });

  it('Should send server response to the channel only once', () => {
    expect(message.channel.send).toHaveBeenCalledTimes(1);
  });

  it('Should send server response to the channel', () => {
    expect(message.channel.send).toHaveBeenCalledWith('some server response');
  });
});
