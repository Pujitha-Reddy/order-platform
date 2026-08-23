import axios from 'axios';

export const orderApi = axios.create({
  baseURL: 'http://localhost:8081/api',
});

export const inventoryApi = axios.create({
  baseURL: 'http://localhost:8082/api',
});

export const WS_URL = 'ws://localhost:8085/ws/orders';

function attachToken(config) {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
}

orderApi.interceptors.request.use(attachToken);