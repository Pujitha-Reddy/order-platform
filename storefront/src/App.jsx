import { Routes, Route } from 'react-router-dom';
import Header from './components/Header';
import ShopPage from './pages/ShopPage';
import ProductDetailPage from './pages/ProductDetailPage';
import CartPage from './pages/CartPage';
import AuthPage from './pages/AuthPage';
import OrderHistoryPage from './pages/OrderHistoryPage';
import OrderTrackingPage from './pages/OrderTrackingPage';

export default function App() {
  return (
    <>
      <Header />
      <Routes>
        <Route path="/" element={<ShopPage />} />
        <Route path="/products/:productId" element={<ProductDetailPage />} />
        <Route path="/cart" element={<CartPage />} />
        <Route path="/login" element={<AuthPage />} />
        <Route path="/orders" element={<OrderHistoryPage />} />
        <Route path="/orders/:orderId" element={<OrderTrackingPage />} />
      </Routes>
    </>
  );
}