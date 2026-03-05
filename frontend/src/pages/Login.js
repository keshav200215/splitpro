import { useState } from "react";
import axios from "axios";
import { useNavigate, Link } from "react-router-dom";
import API from "../config";

function Login() {

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  const isValidEmail = (email) => {
    return /\S+@\S+\.\S+/.test(email);
  };

  const handleLogin = async () => {

    if (!email || !password) {
      alert("Please fill all fields");
      return;
    }

    if (!isValidEmail(email)) {
      alert("Please enter a valid email");
      return;
    }

    localStorage.removeItem("token");

    try {
      const response = await axios.post(
        "${API}/auth/login",
        { email, password }
      );

      const token = response.data;

      if (!token || token.length < 20) {
        alert("Invalid email or password");
        return;
      }

      localStorage.setItem("token", token);
      navigate("/dashboard");

    } catch (error) {
      alert("Invalid email or password");
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-r from-blue-100 to-indigo-200">

      <div className="bg-white p-10 rounded-xl shadow-lg w-96">

        <h1 className="text-3xl font-bold text-blue-600 text-center mb-2">
          SplitPro
        </h1>

        <p className="text-center text-gray-500 mb-6">
          Track shared expenses easily
        </p>

        <input
          className="w-full border rounded-lg px-3 py-2 mb-4 focus:outline-none focus:ring-2 focus:ring-blue-400"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />

        {!isValidEmail(email) && email.length > 0 && (
          <p className="text-red-500 text-sm mb-2">
            Enter a valid email
          </p>
        )}

        <input
          type="password"
          className="w-full border rounded-lg px-3 py-2 mb-6 focus:outline-none focus:ring-2 focus:ring-blue-400"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

        <button
          onClick={handleLogin}
          className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition"
        >
          Login
        </button>

        <p className="text-center text-sm text-gray-500 mt-6">
          Don't have an account?{" "}
          <Link className="text-blue-600 hover:underline" to="/signup">
            Signup
          </Link>
        </p>

      </div>

    </div>
  );
}

export default Login;