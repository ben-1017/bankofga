import { createBrowserRouter } from 'react-router-dom';
import App from './App.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import AccountDetail from './pages/AccountDetail.jsx';
import Accounts from './pages/Accounts.jsx';
import CustomerDetail from './pages/CustomerDetail.jsx';
import Customers from './pages/Customers.jsx';
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
      { path: 'customers', element: <Customers /> },
      { path: 'customers/:id', element: <CustomerDetail /> },
      { path: 'accounts', element: <Accounts /> },
      { path: 'accounts/:id', element: <AccountDetail /> },
    ],
  },
]);
