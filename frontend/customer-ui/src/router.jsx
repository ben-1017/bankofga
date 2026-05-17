import { createBrowserRouter, Navigate } from 'react-router-dom';
import App from './App.jsx';
import Home from './pages/Home.jsx';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import RedirectIfAuthed from './auth/RedirectIfAuthed.jsx';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    children: [
      { index: true, element: <Home /> },
      { path: 'login', element: <RedirectIfAuthed><Login /></RedirectIfAuthed> },
      { path: 'register', element: <RedirectIfAuthed><Register /></RedirectIfAuthed> },
      { path: '*', element: <Navigate to="/" replace /> },
    ],
  },
]);
