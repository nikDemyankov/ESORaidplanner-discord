const setup = require('../setup');
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

const requestOptions = contentLength => ({
  headers: {
    Authorization: 'Basic c29tZS10b2tlbg==',
    'Content-Length': contentLength,
    'Content-Type': 'application/json',
  },
  host: 'esoraidplanner.com',
  method: 'POST',
  path: 'https://esoraidplanner.com/api/discord/setup',
});

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
    expect(setup.run).toBeDefined();
  });

  it('`run` should be a function', () => {
    expect(typeof setup.run).toBe('function');
  });
});

describe('Call `run` successfully without guild_id', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    setup.run(client, message, []);
  });

  it('Request is executed once', () => {
    expect(https.request).toHaveBeenCalledTimes(1);
  });

  it('Request executed with correct options', () => {
    const optionsCall = https.request.mock.calls[0][0];
    const expected = requestOptions(137);

    expect(optionsCall).toEqual(expected);
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

describe('Call `run` successfully with guild_id', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    setup.run(client, message, [123]);
  });

  it('Request is executed once', () => {
    expect(https.request).toHaveBeenCalledTimes(1);
  });

  it('Request executed with correct options', () => {
    const optionsCall = https.request.mock.calls[0][0];
    const expected = requestOptions(152);

    expect(optionsCall).toEqual(expected);
  });

  it('Data is send once', () => {
    expect(request.write).toHaveBeenCalledTimes(1);
  });

  it('Correct data is send', () => {
    const data =
      '{"discord_user_id":"some-author","discord_channel_id":"some-channel","discord_server_id":"some-guild","discord_handle":"some-author-tag","guild_id":123}';

    expect(request.write).toHaveBeenCalledWith(data);
  });

  it('Should send server response to the channel only once', () => {
    expect(message.channel.send).toHaveBeenCalledTimes(1);
  });

  it('Should send server response to the channel', () => {
    expect(message.channel.send).toHaveBeenCalledWith('some server response');
  });
});
