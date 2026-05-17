import { createBrowserRouter } from 'react-router-dom';
import App from './App.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import ComingSoon from './pages/ComingSoon.jsx';
import Home from './pages/Home.jsx';
import Login from './pages/Login.jsx';
import ProductForm from './pages/ProductForm.jsx';
import Products from './pages/Products.jsx';

export const router = createBrowserRouter([
  {
    path: '/login',
    element: <Login />,
  },
  {
    path: '/',
    element: (
      <ProtectedRoute>
        <App />
      </ProtectedRoute>
    ),
    children: [
      { index: true, element: <Home /> },
      { path: 'products', element: <Products /> },
      { path: 'products/new', element: <ProductForm /> },
      { path: 'products/:id/edit', element: <ProductForm /> },
      {
        path: 'customers',
        element: (
          <ComingSoon
            title="Customers"
            description="Customer directory and profile views come next in the admin sprint."
          />
        ),
      },
      {
        path: 'accounts',
        element: (
          <ComingSoon
            title="Accounts"
            description="Account list, account detail, and transaction history follow the customer directory."
          />
        ),
      },
    ],
  },
]);
