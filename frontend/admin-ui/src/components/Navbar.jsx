import { Link, NavLink } from 'react-router-dom';

const linkClass = ({ isActive }) =>
  `px-3 py-2 rounded-md text-sm font-medium transition ${
    isActive ? 'bg-brand-accent text-white' : 'text-gray-300 hover:bg-gray-700 hover:text-white'
  }`;

export default function Navbar() {
  return (
    <nav className="bg-brand text-white">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex h-14 items-center justify-between">
          <Link to="/" className="font-bold tracking-tight">
            Bank of Georgia <span className="text-gray-400 font-normal">— Admin</span>
          </Link>
          <div className="flex items-center gap-1">
            <NavLink to="/" end className={linkClass}>Dashboard</NavLink>
            <NavLink to="/products" className={linkClass}>Products</NavLink>
            <NavLink to="/customers" className={linkClass}>Customers</NavLink>
            <NavLink to="/accounts" className={linkClass}>Accounts</NavLink>
          </div>
        </div>
      </div>
    </nav>
  );
}
