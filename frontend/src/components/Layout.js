import { Link, useNavigate } from "react-router-dom";

export default function Layout({ children }) {
  const navigate = useNavigate();

  const logout = () => {
    localStorage.removeItem("token");
    navigate("/");
  };

  return (
    <div>
      <header className="bg-primary text-white shadow">
        <div className="max-w-5xl mx-auto flex justify-between items-center p-4">
          <h1 className="text-xl font-bold">SplitPro</h1>

          <nav className="space-x-6">
            <Link to="/dashboard" className="hover:underline">
              Dashboard
            </Link>
            <Link to="/groups" className="hover:underline">
              Groups
            </Link>
            <button
              onClick={logout}
              className="bg-white text-primary px-3 py-1 rounded"
            >
              Logout
            </button>
          </nav>
        </div>
      </header>

      <main className="max-w-5xl mx-auto p-6">
        {children}
      </main>
    </div>
  );
}