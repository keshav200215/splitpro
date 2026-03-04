import { useState } from "react";
import axios from "axios";

function AddMemberModal({ groupId, token, members, onClose }) {
  const [query, setQuery] = useState("");
  const [results, setResults] = useState([]);

  /* =========================
     SEARCH USERS
  ========================= */

  const searchUsers = async (value) => {
    setQuery(value);

    if (value.length < 2) {
      setResults([]);
      return;
    }

    try {
      const res = await axios.get(
        `http://localhost:8080/api/users/search?q=${value}`,
        { headers: { Authorization: `Bearer ${token}` } }
      );

      // remove users already in group
      const filtered = res.data.filter(
        (user) => !members.some((m) => m.email === user.email)
      );

      setResults(filtered);
    } catch (err) {
      console.error("Search failed");
    }
  };

  /* =========================
     ADD MEMBER
  ========================= */

  const addMember = async (email) => {
    try {
      await axios.post(
        `http://localhost:8080/api/groups/${groupId}/members`,
        { email },
        { headers: { Authorization: `Bearer ${token}` } }
      );

      onClose();
    } catch (err) {
      alert("Failed to add member");
    }
  };

  const alreadyMember = members.some((m) => m.email === query);

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
          onChange={(e) => searchUsers(e.target.value)}
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