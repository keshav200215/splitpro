import { useState } from "react";
import axios from "axios";
import API from "../config";

export default function AddExpenseModal({
  groupId,
  token,
  members,
  onClose,
}) {
  const [description, setDescription] = useState("");
  const [amount, setAmount] = useState("");
  const [paidBy, setPaidBy] = useState("");
  const [selectedMembers, setSelectedMembers] = useState([]);
  const [equalSplit, setEqualSplit] = useState(true);
  const [manualSplits, setManualSplits] = useState({});

  const toggleMember = (userId) => {
    if (selectedMembers.includes(userId)) {
      setSelectedMembers(selectedMembers.filter((id) => id !== userId));
    } else {
      setSelectedMembers([...selectedMembers, userId]);
    }
  };

  const submitExpense = async () => {
    if (!paidBy || !amount || selectedMembers.length === 0) {
      alert("Fill all fields");
      return;
    }

    let splits = [];

    if (equalSplit) {
      const splitAmount = Number(amount) / selectedMembers.length;
      splits = selectedMembers.map((userId) => ({
        userId,
        amount: splitAmount,
      }));
    } else {
      splits = selectedMembers.map((userId) => ({
        userId,
        amount: Number(manualSplits[userId] || 0),
      }));
    }

    await axios.post(
      `${API}/api/groups/${groupId}/expenses`,
      {
        description,
        amount: Number(amount),
        paidByUserId: Number(paidBy),
        splits,
      },
      {
        headers: { Authorization: `Bearer ${token}` },
      }
    );

    onClose();
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-30 flex justify-center items-center">
      <div className="bg-white p-6 rounded shadow w-[420px] max-h-[90vh] overflow-y-auto">
        <h3 className="text-lg font-semibold mb-4">Add Expense</h3>

        <input
          className="border w-full p-2 mb-3 rounded"
          placeholder="Description"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />

        <input
          type="number"
          className="border w-full p-2 mb-3 rounded"
          placeholder="Total Amount"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
        />

        {/* Paid By */}
        <label className="text-sm font-medium">Paid By</label>
        <select
          className="border w-full p-2 mb-4 rounded"
          value={paidBy}
          onChange={(e) => setPaidBy(e.target.value)}
        >
          <option value="">Select member</option>
          {members.map((member) => (
            <option key={member.id} value={member.id}>
              {member.name}
            </option>
          ))}
        </select>

        {/* Participants */}
        <label className="text-sm font-medium mb-2 block">
          Participants
        </label>

        {members.map((member) => (
          <div key={member.id} className="flex items-center mb-2">
            <input
              type="checkbox"
              checked={selectedMembers.includes(member.id)}
              onChange={() => toggleMember(member.id)}
              className="mr-2"
            />
            <span>{member.name}</span>
          </div>
        ))}

        {/* Equal Split Toggle */}
        <div className="flex items-center mt-4 mb-3">
          <input
            type="checkbox"
            checked={equalSplit}
            onChange={() => setEqualSplit(!equalSplit)}
            className="mr-2"
          />
          <span>Split equally</span>
        </div>

        {/* Manual Split */}
        {!equalSplit &&
          selectedMembers.map((userId) => {
            const member = members.find((m) => m.id === userId);
            return (
              <div key={userId} className="mb-2">
                <label className="text-sm">{member?.name}</label>
                <input
                  type="number"
                  className="border w-full p-2 rounded"
                  onChange={(e) =>
                    setManualSplits({
                      ...manualSplits,
                      [userId]: e.target.value,
                    })
                  }
                />
              </div>
            );
          })}

        <div className="flex justify-end gap-3 mt-4">
          <button onClick={onClose}>Cancel</button>
          <button
            onClick={submitExpense}
            className="bg-primary text-white px-4 py-2 rounded"
          >
            Add
          </button>
        </div>
      </div>
    </div>
  );
}