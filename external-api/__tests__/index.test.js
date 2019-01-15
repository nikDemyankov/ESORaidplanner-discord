const api = require('../index');

const https = require('https');

jest.mock('https');

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

describe('Request successfull', () => {
  const resultCallback = jest.fn();

  beforeEach(() => {
    api.sendRequest(
      { token: 'qwe-qwe-qwe', data: { foo: 'bar' }, method: 'some-method' },
      resultCallback,
    );
  });

  it('Request is executed once', () => {
    expect(https.request).toHaveBeenCalledTimes(1);
  });

  it('Request executed with correct options', () => {
    const optionsCall = https.request.mock.calls[0][0];
    const expected = {
      headers: {
        Authorization: 'Basic cXdlLXF3ZS1xd2U=',
        'Content-Length': 13,
        'Content-Type': 'application/json',
      },
      host: 'esoraidplanner.com',
      method: 'POST',
      path: 'https://esoraidplanner.com/api/discord/some-method',
    };
    expect(optionsCall).toEqual(expected);
  });

  it('Data is send once', () => {
    expect(request.write).toHaveBeenCalledTimes(1);
  });

  it('Correct data is send', () => {
    const data = '{"foo":"bar"}';

    expect(request.write).toHaveBeenCalledWith(data);
  });

  it('Result callback is called once', () => {
    expect(resultCallback).toHaveBeenCalledTimes(1);
  });

  it('Result callbaxk is called with server response', () => {
    expect(resultCallback).toHaveBeenLastCalledWith('some server response');
  });
});
