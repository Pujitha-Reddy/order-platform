import { orderApi } from './client';

export async function createOrder({ customerId, items }) {
  const response = await orderApi.post('/orders', {
    customerId,
    items,
  });
  return response.data;
}

export async function getOrder(orderId) {
  const response = await orderApi.get(`/orders/${orderId}`);
  return response.data;
}

export async function registerUser({ email, password, displayName }) {
  const response = await orderApi.post('/auth/register', { email, password, displayName });
  return response.data;
}

export async function loginUser({ email, password }) {
  const response = await orderApi.post('/auth/login', { email, password });
  return response.data;
}

export async function getMyOrders() {
  const response = await orderApi.get('/orders/mine');
  return response.data;
}