import React from "react";
import { FileText, Search, TerminalSquare } from "lucide-react";

const Tabbar = ({ activeTab, setActiveTab, showTerminal }) => {
  const tabs = [
    { id: "files", icon: <FileText size={15} /> },
    { id: "search", icon: <Search size={15} /> },
    { id: "terminal", icon: <TerminalSquare size={15} /> },
  ];

  return (
    <div className="w-12 border-r h-screen border-gray-800 text-white flex flex-col items-center py-2 space-y-4">
      {tabs.map((tab) => (
        <button
          key={tab.id}
          onClick={() => setActiveTab(tab.id)}
          className={`p-2 rounded ${
            (tab.id === "terminal" && showTerminal) ||
            (tab.id !== "terminal" && activeTab === tab.id)
              ? "bg-gray-700"
              : "hover:bg-gray-800"
          }`}
        >
          {tab.icon}
        </button>
      ))}
    </div>
  );
};

export default Tabbar;
