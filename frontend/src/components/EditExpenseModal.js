import { useState } from "react";
import axios from "axios";

function EditExpenseModal({ expense, groupId, token, members, onClose }) {

  const [description, setDescription] = useState(expense.description);
  const [amount, setAmount] = useState(expense.amount);
  const [paidByUserId, setPaidByUserId] = useState(expense.paidById);

  const [splits, setSplits] = useState(
    members.map((m) => ({
      userId: m.id,
      amount: (expense.amount / members.length).toString()
    }))
  );

  const toggleParticipant = (userId) => {

    const exists = splits.find((s) => s.userId === userId);

    if (exists) {
      setSplits(splits.filter((s) => s.userId !== userId));
    } else {
      setSplits([
        ...splits,
        { userId, amount: "" }
      ]);
    }
  };

  const updateSplitAmount = (userId, value) => {

    setSplits(
      splits.map((s) =>
        s.userId === userId
          ? { ...s, amount: value }
          : s
      )
    );
  };

  const handleUpdate = async () => {

    const formattedSplits = splits.map((s) => ({
      userId: s.userId,
      amount: Number(s.amount || 0)
    }));

    const totalSplit = formattedSplits.reduce(
      (sum, s) => sum + s.amount,
      0
    );

    if (Math.abs(totalSplit - Number(amount)) > 0.01) {
      alert("Split amounts must equal total amount");
      return;
    }

    try {

      await axios.put(
        `http://localhost:8080/api/groups/${groupId}/expenses/${expense.id}`,
        {
          description,
          amount: Number(amount),
          paidByUserId,
          splits: formattedSplits
        },
        {
          headers: { Authorization: `Bearer ${token}` }
        }
      );

      onClose();

    } catch (err) {
      console.error(err);
      alert("Failed to update expense");
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-40 flex justify-center items-center">

      <div className="bg-white rounded-lg p-6 w-[450px]">

        <h2 className="text-xl font-semibold mb-4">
          Edit Expense
        </h2>

        <input
          className="border p-2 w-full mb-3 rounded"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />

        <input
          className="border p-2 w-full mb-3 rounded"
          type="number"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
        />

        {/* Paid By */}

        <label className="text-sm font-medium">Paid By</label>

        <select
          className="border p-2 w-full mb-4 rounded"
          value={paidByUserId}
          onChange={(e) =>
            setPaidByUserId(Number(e.target.value))
          }
        >
          {members.map((m) => (
            <option key={m.id} value={m.id}>
              {m.name}
            </option>
          ))}
        </select>

        {/* Participants */}

        <label className="text-sm font-medium mb-2 block">
          Participants
        </label>

        {members.map((m) => {

          const split = splits.find(
            (s) => s.userId === m.id
          );

          return (
            <div
              key={m.id}
              className="flex items-center gap-3 mb-2"
            >

              <input
                type="checkbox"
                checked={!!split}
                onChange={() => toggleParticipant(m.id)}
              />

              <span className="w-[120px]">
                {m.name}
              </span>

              {split && (
                <input
                  type="number"
                  className="border p-1 w-[100px] rounded"
                  placeholder="0"
                  value={split.amount}
                  onChange={(e) =>
                    updateSplitAmount(
                      m.id,
                      e.target.value
                    )
                  }
                />
              )}

            </div>
          );
        })}

        <div className="flex justify-end gap-3 mt-4">

          <button
            onClick={onClose}
            className="px-4 py-2 bg-gray-200 rounded"
          >
            Cancel
          </button>

          <button
            onClick={handleUpdate}
            className="px-4 py-2 bg-blue-600 text-white rounded"
          >
            Update
          </button>

        </div>

      </div>

    </div>
  );
}

export default EditExpenseModal;