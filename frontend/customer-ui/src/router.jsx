import { createBrowserRouter, Navigate } from 'react-router-dom';
import App from './App.jsx';
import Home from './pages/Home.jsx';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import Dashboard from './pages/Dashboard.jsx';
import Accounts from './pages/Accounts.jsx';
import AccountDetail from './pages/AccountDetail.jsx';
import OpenAccount from './pages/OpenAccount.jsx';
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
      { path: '*', element: <Navigate to="/" replace /> },
    ],
  },
]);
