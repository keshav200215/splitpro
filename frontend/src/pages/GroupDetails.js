import { useEffect, useState } from "react";
import axios from "axios";
import { useParams } from "react-router-dom";
import Layout from "../components/Layout";
import AddMemberModal from "../components/AddMemberModal";
import AddExpenseModal from "../components/AddExpenseModal";

function GroupDetails() {
  const { groupId } = useParams();
  const token = localStorage.getItem("token");

  const [members, setMembers] = useState([]);
  const [balances, setBalances] = useState([]);
  const [expenses, setExpenses] = useState([]);
  const [currentUser, setCurrentUser] = useState(null);

  const [showMemberModal, setShowMemberModal] = useState(false);
  const [showExpenseModal, setShowExpenseModal] = useState(false);

  const fetchAll = async () => {
    try {
      const userRes = await axios.get(
        "http://localhost:8080/api/users/me",
        { headers: { Authorization: `Bearer ${token}` } }
      );

      const membersRes = await axios.get(
        `http://localhost:8080/api/groups/${groupId}/members`,
        { headers: { Authorization: `Bearer ${token}` } }
      );

      const balanceRes = await axios.get(
        `http://localhost:8080/api/groups/${groupId}/balances`,
        { headers: { Authorization: `Bearer ${token}` } }
      );

      const expenseRes = await axios.get(
        `http://localhost:8080/api/groups/${groupId}/expenses`,
        { headers: { Authorization: `Bearer ${token}` } }
      );

      setCurrentUser(userRes.data);
      setMembers(membersRes.data);
      setBalances(balanceRes.data);
      setExpenses(expenseRes.data);
    } catch (err) {
      console.error("Failed to load group data");
    }
  };

  useEffect(() => {
    fetchAll();
  }, []);

  const getUserName = (id) => {
    const user = members.find((m) => m.id === id);
    return user ? user.name : `User ${id}`;
  };

  /* ===============================
     SETTLE
  =============================== */

  const settle = async (balance) => {
    await axios.post(
      `http://localhost:8080/api/groups/${groupId}/settle?fromUserId=${balance.fromUserId}&toUserId=${balance.toUserId}&amount=${balance.amount}`,
      {},
      { headers: { Authorization: `Bearer ${token}` } }
    );

    fetchAll();
  };

  /* ===============================
     DELETE EXPENSE
  =============================== */

  const deleteExpense = async (expenseId) => {
    await axios.delete(
      `http://localhost:8080/api/groups/${groupId}/expenses/${expenseId}`,
      { headers: { Authorization: `Bearer ${token}` } }
    );

    fetchAll();
  };

  /* ===============================
     EDIT EXPENSE (simple prompt)
  =============================== */

  const editExpense = async (expense) => {
    const newDescription = prompt(
      "Edit description",
      expense.description
    );
    const newAmount = prompt("Edit amount", expense.amount);

    if (!newDescription || !newAmount) return;

    await axios.put(
      `http://localhost:8080/api/groups/${groupId}/expenses/${expense.id}`,
      {
        description: newDescription,
        amount: Number(newAmount),
        splits: [],
      },
      { headers: { Authorization: `Bearer ${token}` } }
    );

    fetchAll();
  };

  return (
    <Layout>
      <div>
        {/* HEADER */}
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-bold">Group Overview</h2>

          <div className="flex gap-3">
            <button
              onClick={() => setShowMemberModal(true)}
              className="bg-gray-200 px-4 py-2 rounded"
            >
              Add Member
            </button>

            <button
              onClick={() => setShowExpenseModal(true)}
              className="bg-primary text-white px-4 py-2 rounded"
            >
              Add Expense
            </button>
          </div>
        </div>

        {/* MEMBERS */}
        <div className="mb-8">
          <h3 className="text-lg font-semibold mb-3">Members</h3>
          <div className="flex flex-wrap gap-3">
            {members.map((member) => (
              <div
                key={member.id}
                className="bg-gray-100 px-4 py-2 rounded-full text-sm"
              >
                {member.name}
              </div>
            ))}
          </div>
        </div>

        {/* PER USER SUMMARY */}
        <h3 className="text-xl font-semibold mb-4">
          Per-User Summary
        </h3>

        {members.map((member) => {
          let total = 0;

          balances.forEach((b) => {
            if (b.fromUserId === member.id) total -= b.amount;
            if (b.toUserId === member.id) total += b.amount;
          });

          return (
            <div
              key={member.id}
              className="bg-white p-4 rounded shadow mb-2"
            >
              <p>
                {member.name}:{" "}
                <span
                  className={
                    total > 0
                      ? "text-green-600"
                      : total < 0
                      ? "text-red-600"
                      : ""
                  }
                >
                  ₹{total.toFixed(2)}
                </span>
              </p>
            </div>
          );
        })}

        {/* BALANCES */}
        <h3 className="text-xl font-semibold mt-8 mb-4">
          Balances
        </h3>

        {balances.length === 0 && (
          <div className="bg-white p-6 rounded shadow mb-6">
            <p className="text-green-600 font-medium">
              All settled 🎉
            </p>
          </div>
        )}

        {currentUser &&
          balances.map((balance, index) => {
            const isDebtor =
              balance.fromUserId === currentUser.id;
            const isCreditor =
              balance.toUserId === currentUser.id;

            return (
              <div
                key={index}
                className="bg-white p-6 rounded shadow mb-4 flex justify-between items-center"
              >
                <div>
                  {isDebtor && (
                    <p className="text-red-600 font-medium">
                      You owe {getUserName(balance.toUserId)} ₹
                      {balance.amount.toFixed(2)}
                    </p>
                  )}

                  {isCreditor && (
                    <p className="text-green-600 font-medium">
                      {getUserName(balance.fromUserId)} owes you ₹
                      {balance.amount.toFixed(2)}
                    </p>
                  )}
                </div>

                {isDebtor && (
                  <button
                    onClick={() => settle(balance)}
                    className="bg-green-600 text-white px-4 py-2 rounded text-sm"
                  >
                    Settle
                  </button>
                )}
              </div>
            );
          })}

        {/* ACTIVITY TIMELINE */}
        <h3 className="text-xl font-semibold mt-10 mb-4">
          Activity Timeline
        </h3>

        {expenses
          .sort(
            (a, b) =>
              new Date(b.createdAt) - new Date(a.createdAt)
          )
          .map((expense) => (
            <div
              key={expense.id}
              className={`p-5 rounded shadow mb-3 ${
                expense.settlement
                  ? "bg-green-50 border border-green-300"
                  : "bg-white"
              }`}
            >
              <div className="flex justify-between items-center">
                <div>
                  <p className="font-medium">
                    {expense.description}
                  </p>

                  <p className="text-sm text-gray-500">
                    ₹{expense.amount.toFixed(2)} • Paid by{" "}
                    {expense.paidByName}
                  </p>

                  <p className="text-xs text-gray-400 mt-1">
                    {new Date(
                      expense.createdAt
                    ).toLocaleString()}
                  </p>
                </div>

                <div className="flex gap-3">
                  <button
                    onClick={() => editExpense(expense)}
                    className="text-blue-600 text-sm"
                  >
                    Edit
                  </button>

                  <button
                    onClick={() =>
                      deleteExpense(expense.id)
                    }
                    className="text-red-600 text-sm"
                  >
                    Delete
                  </button>
                </div>
              </div>
            </div>
          ))}

        {/* MODALS */}
        {showMemberModal && (
          <AddMemberModal
            groupId={groupId}
            token={token}
            members={members}
            onClose={() => {
              setShowMemberModal(false);
              fetchAll();
            }}
          />
        )}

        {showExpenseModal && (
          <AddExpenseModal
            groupId={groupId}
            token={token}
            members={members}
            onClose={() => {
              setShowExpenseModal(false);
              fetchAll();
            }}
          />
        )}
      </div>
    </Layout>
  );
}

export default GroupDetails;