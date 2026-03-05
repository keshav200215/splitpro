import { useState, useEffect } from "react";
import axios from "axios";
import API from "../config";

function AddMemberModal({ groupId, token, members, onClose }) {

  const [query, setQuery] = useState("");
  const [debouncedQuery, setDebouncedQuery] = useState("");
  const [results, setResults] = useState([]);

  /* =========================
     DEBOUNCE INPUT
  ========================= */

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedQuery(query);
    }, 400);

    return () => clearTimeout(timer);
  }, [query]);

  /* =========================
     SEARCH USERS
  ========================= */

  useEffect(() => {

    if (debouncedQuery.length < 2) {
      setResults([]);
      return;
    }

    const searchUsers = async () => {
      try {

        const res = await axios.get(
          `${API}/api/users/search?q=${debouncedQuery}`,
          {
            headers: { Authorization: `Bearer ${token}` }
          }
        );

        const filtered = res.data.filter(
          (user) =>
            !members.some((m) => m.email === user.email)
        );

        setResults(filtered);

      } catch (err) {
        console.error("Search failed", err);
      }
    };

    searchUsers();

  }, [debouncedQuery, token, members]);

  /* =========================
     ADD MEMBER
  ========================= */

  const addMember = async (email) => {
    try {

      await axios.post(
        `${API}/api/groups/${groupId}/members`,
        { email },
        { headers: { Authorization: `Bearer ${token}` } }
      );

      onClose();

    } catch (err) {
      alert("Failed to add member");
    }
  };

  const alreadyMember = members.some(
    (m) => m.email === query
  );

  return (
    <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-40 z-50">

      <div className="bg-white p-6 rounded-lg shadow-lg w-96">

        <h2 className="text-xl font-semibold mb-4">
          Add Member
        </h2>

        {/* SEARCH INPUT */}

        <input
          placeholder="Search name or email"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          className="w-full border p-2 rounded mb-3"
        />

        {/* SEARCH RESULTS */}

        <div className="max-h-40 overflow-y-auto">

          {results.map((user) => (
            <div
              key={user.id}
              onClick={() => addMember(user.email)}
              className="p-2 hover:bg-gray-100 cursor-pointer rounded"
            >
              <p className="text-sm font-medium">
                {user.name}
              </p>

              <p className="text-xs text-gray-500">
                {user.email}
              </p>
            </div>
          ))}

        </div>

        {/* INVITE OPTION */}

        {query.includes("@") &&
          results.length === 0 &&
          !alreadyMember && (
            <div
              onClick={() => addMember(query)}
              className="mt-3 p-2 bg-gray-100 rounded cursor-pointer text-sm"
            >
              Invite <b>{query}</b>
            </div>
          )}

        {/* FOOTER */}

        <div className="flex justify-end mt-4">
          <button
            onClick={onClose}
            className="px-4 py-2 bg-gray-200 rounded"
          >
            Close
          </button>
        </div>

      </div>
    </div>
  );
}

export default AddMemberModal;