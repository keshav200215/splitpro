import { useEffect, useState } from "react";
import axios from "axios";
import { Link } from "react-router-dom";
import Layout from "../components/Layout";
import API from "../config";

function Dashboard() {
  const [user, setUser] = useState(null);
  const [groups, setGroups] = useState([]);

  const token = localStorage.getItem("token");

  const fetchData = async () => {
    try {
      const userRes = await axios.get(
        `${API}/api/users/me`,
        { headers: { Authorization: `Bearer ${token}` } }
      );

      const groupRes = await axios.get(
        `${API}/api/groups`,
        { headers: { Authorization: `Bearer ${token}` } }
      );

      setUser(userRes.data);
      setGroups(groupRes.data);
    } catch (error) {
      console.error("Failed to fetch dashboard data");
    }
  };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => {
    fetchData();
  }, []);

  return (
    <Layout>
      <div>
        {/* Welcome Section */}
        <div className="mb-8">
          <h2 className="text-3xl font-bold mb-2">
            Welcome back, {user?.name} 👋
          </h2>
          <p className="text-gray-500">
            Manage your shared expenses with SplitPro
          </p>
        </div>

        {/* Stats Section */}
        <div className="grid md:grid-cols-3 gap-6 mb-10">
          <div className="bg-white p-6 rounded-lg shadow">
            <p className="text-gray-500 text-sm">Total Groups</p>
            <h3 className="text-2xl font-bold mt-2">
              {groups.length}
            </h3>
          </div>

          <div className="bg-white p-6 rounded-lg shadow">
            <p className="text-gray-500 text-sm">Active Groups</p>
            <h3 className="text-2xl font-bold mt-2">
              {groups.length}
            </h3>
          </div>

          <div className="bg-white p-6 rounded-lg shadow">
            <p className="text-gray-500 text-sm">Account Status</p>
            <h3 className="text-2xl font-bold mt-2 text-green-600">
              Active
            </h3>
          </div>
        </div>

        {/* Groups Preview */}
        <div>
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-xl font-semibold">
              Your Groups
            </h3>
            <Link
              to="/groups"
              className="text-primary font-medium hover:underline"
            >
              View All →
            </Link>
          </div>

          {groups.length === 0 ? (
            <div className="bg-white p-6 rounded shadow">
              <p className="text-gray-500">
                You haven't created any groups yet.
              </p>
              <Link
                to="/groups"
                className="text-primary font-medium mt-2 inline-block"
              >
                Create your first group →
              </Link>
            </div>
          ) : (
            <div className="grid md:grid-cols-2 gap-6">
              {groups.slice(0, 4).map((group) => (
                <Link
                  key={group.id}
                  to={`/groups/${group.id}`}
                  className="bg-white shadow rounded-lg p-6 hover:shadow-lg transition"
                >
                  <h4 className="text-lg font-semibold mb-2">
                    {group.name}
                  </h4>
                  <p className="text-gray-500 text-sm">
                    Created by {group.createdByName}
                  </p>
                </Link>
              ))}
            </div>
          )}
        </div>
      </div>
    </Layout>
  );
}

export default Dashboard;