import { Link, NavLink } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';

const linkClass = ({ isActive }) =>
  `px-3 py-2 rounded-md text-sm font-medium transition ${
    isActive ? 'bg-brand-accent text-white' : 'text-gray-300 hover:bg-gray-700 hover:text-white'
  }`;

export default function Navbar() {
  const { employee, logout } = useAuth();

  return (
    <nav className="bg-brand text-white">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex min-h-14 flex-wrap items-center justify-between gap-3 py-3">
          <Link to="/" className="font-bold tracking-tight">
            Bank of Georgia <span className="font-normal text-gray-400">Admin</span>
          </Link>
          <div className="flex flex-wrap items-center gap-3">
            <div className="flex flex-wrap items-center gap-1">
              <NavLink to="/" end className={linkClass}>Dashboard</NavLink>
              <NavLink to="/products" className={linkClass}>Products</NavLink>
              <NavLink to="/customers" className={linkClass}>Customers</NavLink>
              <NavLink to="/accounts" className={linkClass}>Accounts</NavLink>
            </div>
            <div className="flex items-center gap-3 border-l border-white/15 pl-3">
              <span className="text-sm text-gray-300">{employee?.name}</span>
              <button
                type="button"
                onClick={logout}
                className="rounded-md border border-white/20 px-3 py-2 text-sm font-medium text-gray-100 transition hover:bg-white/10"
              >
                Sign out
              </button>
            </div>
          </div>
        </div>
      </div>
    </nav>
  );
}
