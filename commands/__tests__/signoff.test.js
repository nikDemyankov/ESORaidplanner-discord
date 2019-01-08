const signoff = require('../signoff');
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

const requestOptions = {
  headers: {
    Authorization: 'Basic c29tZS10b2tlbg==',
    'Content-Length': 152,
    'Content-Type': 'application/json',
  },
  host: 'esoraidplanner.com',
  method: 'POST',
  path: 'https://esoraidplanner.com/api/discord/signoff',
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
    expect(https.request).not.toHaveBeenCalled();
  });
});

describe('Call `run` successfully', () => {
  beforeEach(() => {
    signoff.run(client, message, [123]);
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
      '{"discord_user_id":"some-author","discord_channel_id":"some-channel","discord_server_id":"some-guild","discord_handle":"some-author-tag","event_id":123}';

    expect(request.write).toHaveBeenCalledWith(data);
  });

  it('Should send server response to the channel only once', () => {
    expect(message.channel.send).toHaveBeenCalledTimes(1);
  });

  it('Should send server response to the channel', () => {
    expect(message.channel.send).toHaveBeenCalledWith('some server response');
  });
});
