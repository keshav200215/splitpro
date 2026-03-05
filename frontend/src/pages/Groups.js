import { useEffect, useState } from "react";
import axios from "axios";
import { Link } from "react-router-dom";
import Layout from "../components/Layout";
import API from "../config";

function Groups() {
  const [groups, setGroups] = useState([]);
  const [newGroupName, setNewGroupName] = useState("");

  const token = localStorage.getItem("token");

  const fetchGroups = async () => {
    try {
      const response = await axios.get(
        `${API}/api/groups`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setGroups(response.data);
    } catch (error) {
      alert("Failed to fetch groups");
    }
  };

  const createGroup = async () => {
    if (!newGroupName.trim()) return;

    try {
      await axios.post(
        `${API}/api/groups`,
        { name: newGroupName },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setNewGroupName("");
      fetchGroups();
    } catch (error) {
      alert("Failed to create group");
    }
  };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => {
    fetchGroups();
  }, []);

  return (
    <Layout>
      <div className="mb-8">
        <h2 className="text-2xl font-bold mb-4">Your Groups</h2>

        <div className="flex gap-3 mb-6">
          <input
            className="border rounded px-4 py-2 flex-1 focus:outline-none focus:ring-2 focus:ring-primary"
            placeholder="New Group Name"
            value={newGroupName}
            onChange={(e) => setNewGroupName(e.target.value)}
          />
          <button
            onClick={createGroup}
            className="bg-primary text-white px-5 py-2 rounded hover:opacity-90"
          >
            Create
          </button>
        </div>

        <div className="grid md:grid-cols-2 gap-6">
          {groups.map((group) => (
            <Link
              key={group.id}
              to={`/groups/${group.id}`}
              className="bg-white shadow-md rounded-lg p-6 hover:shadow-lg transition"
            >
              <h3 className="text-xl font-semibold mb-2">{group.name}</h3>
              <p className="text-gray-500 text-sm">
                Created by User {group.createdBy}
              </p>
            </Link>
          ))}
        </div>
      </div>
    </Layout>
  );
}

export default Groups;