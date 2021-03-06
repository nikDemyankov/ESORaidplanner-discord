const signup = require('../signup');
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

const requestOptions = contentLength => ({
  headers: {
    Authorization: 'Basic c29tZS10b2tlbg==',
    'Content-Length': contentLength,
    'Content-Type': 'application/json',
  },
  host: 'esoraidplanner.com',
  method: 'POST',
  path: 'https://esoraidplanner.com/api/discord/signup',
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
    expect(signup.run).toBeDefined();
  });

  it('`run` should be a function', () => {
    expect(typeof signup.run).toBe('function');
  });
});

describe('Event id is not set in the arguments', () => {
  beforeEach(() => {
    signup.run(client, message, []);
  });

  it('Error message is send to the channel', () => {
    expect(message.channel.send).toHaveBeenCalledTimes(1);
  });

  it('Correct error message is send', () => {
    expect(message.channel.send).toHaveBeenCalledWith(
      'some-author Please use the correct command format to sign up. `!signup event_id class role`',
    );
  });

  it('Request is not executed', () => {
    expect(https.request).not.toHaveBeenCalled();
  });
});

describe('Arguments have invalid class', () => {
  beforeEach(() => {
    signup.run(client, message, [123, 'invalid-role', 'tank']);
  });

  it('Error message is send to the channel', () => {
    expect(message.channel.send).toHaveBeenCalledTimes(1);
  });

  it('Correct error message is send', () => {
    expect(message.channel.send).toHaveBeenCalledWith(
      'some-author Please use a valid class to sign up.',
    );
  });

  it('Request is not executed', () => {
    expect(https.request).not.toHaveBeenCalled();
  });
});

describe('Arguments have invalid role', () => {
  beforeEach(() => {
    signup.run(client, message, 'warden', 'invalid-role');
  });

  it('Error message is send to the channel', () => {
    expect(message.channel.send).toHaveBeenCalledTimes(1);
  });

  it('Correct error message is send', () => {
    expect(message.channel.send).toHaveBeenCalledWith(
      'some-author Please use a valid class to sign up.',
    );
  });

  it('Request is not executed', () => {
    expect(https.request).not.toHaveBeenCalled();
  });
});

describe('Call `run` successfully with preset persona', () => {
  beforeEach(() => {
    signup.run(client, message, [123, '"some character"']);
  });

  it('Request is executed once', () => {
    expect(https.request).toHaveBeenCalledTimes(1);
  });

  it('Request executed with correct options', () => {
    const optionsCall = https.request.mock.calls[0][0];
    const expected = requestOptions(178);
    expect(optionsCall).toEqual(expected);
  });

  it('Data is send once', () => {
    expect(request.write).toHaveBeenCalledTimes(1);
  });

  it('Correct data is send', () => {
    const data =
      '{"discord_user_id":"some-author","discord_channel_id":"some-channel","discord_server_id":"some-guild","discord_handle":"some-author-tag","event_id":123,"preset":"some character"}';

    expect(request.write).toHaveBeenCalledWith(data);
  });

  it('Should send server response to the channel only once', () => {
    expect(message.channel.send).toHaveBeenCalledTimes(1);
  });

  it('Should send server response to the channel', () => {
    expect(message.channel.send).toHaveBeenCalledWith('some server response');
  });
});

describe('Call `run` successfully with valid character class and role', () => {
  beforeEach(() => {
    signup.run(client, message, [123, 'warden', 'tank']);
  });

  it('Request is executed once', () => {
    expect(https.request).toHaveBeenCalledTimes(1);
  });

  it('Request executed with correct options', () => {
    const optionsCall = https.request.mock.calls[0][0];
    const expected = requestOptions(171);
    expect(optionsCall).toEqual(expected);
  });

  it('Data is send once', () => {
    expect(request.write).toHaveBeenCalledTimes(1);
  });

  it('Correct data is send', () => {
    const data =
      '{"discord_user_id":"some-author","discord_channel_id":"some-channel","discord_server_id":"some-guild","discord_handle":"some-author-tag","event_id":123,"class":4,"role":1}';

    expect(request.write).toHaveBeenCalledWith(data);
  });

  it('Should send server response to the channel only once', () => {
    expect(message.channel.send).toHaveBeenCalledTimes(1);
  });

  it('Should send server response to the channel', () => {
    expect(message.channel.send).toHaveBeenCalledWith('some server response');
  });
});
