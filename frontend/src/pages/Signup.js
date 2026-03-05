import { useState } from "react";
import axios from "axios";
import { useNavigate, Link } from "react-router-dom";
import API from "../config";

function Signup() {

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const navigate = useNavigate();

  const isValidEmail = (email) => {
    return /\S+@\S+\.\S+/.test(email);
  };

  const handleSignup = async () => {

    if (!name || !email || !password) {
      alert("Please fill all fields");
      return;
    }

    if (!isValidEmail(email)) {
      alert("Enter a valid email");
      return;
    }

    if (password.length < 6) {
      alert("Password must be at least 6 characters");
      return;
    }

    try {

      await axios.post(`${API}/auth/signup`, {
        name,
        email,
        password,
      });

      alert("Signup successful!");
      navigate("/");

    } catch (error) {
      alert("Signup failed");
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-r from-blue-100 to-indigo-200">

      <div className="bg-white p-10 rounded-xl shadow-lg w-96">

        <h1 className="text-3xl font-bold text-blue-600 text-center mb-2">
          Create Account
        </h1>

        <p className="text-center text-gray-500 mb-6">
          Join SplitPro
        </p>

        <input
          className="w-full border rounded-lg px-3 py-2 mb-2 focus:outline-none focus:ring-2 focus:ring-blue-400"
          placeholder="Name"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />

        {name.length === 0 && (
          <p className="text-red-500 text-sm mb-2"></p>
        )}

        <input
          className="w-full border rounded-lg px-3 py-2 mb-2 focus:outline-none focus:ring-2 focus:ring-blue-400"
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
          className="w-full border rounded-lg px-3 py-2 mb-2 focus:outline-none focus:ring-2 focus:ring-blue-400"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

        {password.length > 0 && password.length < 6 && (
          <p className="text-red-500 text-sm mb-4">
            Password must be at least 6 characters
          </p>
        )}

        <button
          disabled={!name || !isValidEmail(email) || password.length < 6}
          onClick={handleSignup}
          className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition disabled:bg-gray-400 mt-4"
        >
          Signup
        </button>

        <p className="text-center text-sm text-gray-500 mt-6">
          Already have an account?{" "}
          <Link className="text-blue-600 hover:underline" to="/">
            Login
          </Link>
        </p>

      </div>
    </div>
  );
}

export default Signup;