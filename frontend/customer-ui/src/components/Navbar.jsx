import { Link, NavLink } from 'react-router-dom';
import { useAuth } from '../auth/useAuth.js';

const linkClass = ({ isActive }) =>
  `px-3 py-2 rounded-md text-sm font-medium transition ${
    isActive ? 'bg-brand-accent text-white' : 'text-gray-300 hover:bg-gray-700 hover:text-white'
  }`;

export default function Navbar() {
  const { user, logout } = useAuth();
  return (
    <nav className="bg-brand text-white">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex h-14 items-center justify-between">
          <Link to="/" className="font-bold tracking-tight">
            Bank of Georgia <span className="text-gray-400 font-normal">— Customer</span>
          </Link>
          <div className="flex items-center gap-1">
            {user ? (
              <>
                <NavLink to="/" end className={linkClass}>Dashboard</NavLink>
                <NavLink to="/accounts" className={linkClass}>Accounts</NavLink>
                <NavLink to="/transactions" className={linkClass}>Transactions</NavLink>
                <NavLink to="/notifications" className={linkClass}>Notifications</NavLink>
                <NavLink to="/profile" className={linkClass}>Profile</NavLink>
                <button
                  type="button"
                  onClick={logout}
                  className="ml-2 rounded-md px-3 py-2 text-sm font-medium text-gray-300 hover:bg-gray-700 hover:text-white"
                >
                  Sign out
                </button>
              </>
            ) : (
              <>
                <NavLink to="/login" className={linkClass}>Sign in</NavLink>
                <NavLink to="/register" className={linkClass}>Register</NavLink>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}
