// TODO: rework to promises with proper error handling

const https = require('https');

const HOST = 'esoraidplanner.com';
const PATH = 'https://esoraidplanner.com/api/discord';

const getAuthToken = token => Buffer.from(token).toString('base64');

const sendRequest = ({ data, token, method }, callback) => {
  const requestData = JSON.stringify(data);
  const authToken = getAuthToken(token);

  const options = {
    host: HOST,
    path: `${PATH}/${method}`,
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Content-Length': requestData.length,
      Authorization: `Basic ${authToken}`,
    },
  };

  let serverResponse = '';
  const request = https.request(options, res => {
    res.on('data', chunk => {
      serverResponse += chunk;
    });
    res.on('end', () => {
      callback(serverResponse);
    });
  });

  request.write(requestData);
};

module.exports = { sendRequest };
