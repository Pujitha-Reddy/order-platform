import { inventoryApi } from './client';

export async function listProducts() {
  const response = await inventoryApi.get('/products');
  return response.data;
}

export async function getProduct(productId) {
  const response = await inventoryApi.get(`/products/${productId}`);
  return response.data;
}