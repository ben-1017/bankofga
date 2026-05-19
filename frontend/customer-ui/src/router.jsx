import { createBrowserRouter, Navigate } from 'react-router-dom';
import App from './App.jsx';
import Home from './pages/Home.jsx';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import Dashboard from './pages/Dashboard.jsx';
import Accounts from './pages/Accounts.jsx';
import AccountDetail from './pages/AccountDetail.jsx';
import OpenAccount from './pages/OpenAccount.jsx';
import Deposit from './pages/Deposit.jsx';
import Withdraw from './pages/Withdraw.jsx';
import TransactionHistory from './pages/TransactionHistory.jsx';
import Profile from './pages/Profile.jsx';
import Notifications from './pages/Notifications.jsx';
import RequireAuth from './auth/RequireAuth.jsx';
import RedirectIfAuthed from './auth/RedirectIfAuthed.jsx';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    children: [
      { index: true, element: <Home /> },
      { path: 'login', element: <RedirectIfAuthed><Login /></RedirectIfAuthed> },
      { path: 'register', element: <RedirectIfAuthed><Register /></RedirectIfAuthed> },
      { path: 'dashboard', element: <RequireAuth><Dashboard /></RequireAuth> },
      { path: 'accounts', element: <RequireAuth><Accounts /></RequireAuth> },
      { path: 'accounts/new', element: <RequireAuth><OpenAccount /></RequireAuth> },
      { path: 'accounts/:id', element: <RequireAuth><AccountDetail /></RequireAuth> },
      { path: 'accounts/:id/deposit', element: <RequireAuth><Deposit /></RequireAuth> },
      { path: 'accounts/:id/withdraw', element: <RequireAuth><Withdraw /></RequireAuth> },
      { path: 'accounts/:id/transactions', element: <RequireAuth><TransactionHistory /></RequireAuth> },
      { path: 'profile', element: <RequireAuth><Profile /></RequireAuth> },
      { path: 'notifications', element: <RequireAuth><Notifications /></RequireAuth> },
      { path: '*', element: <Navigate to="/" replace /> },
    ],
  },
]);
