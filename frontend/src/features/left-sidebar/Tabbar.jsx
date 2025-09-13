  import React from "react";
  import { Folder, Search, TerminalSquare } from "lucide-react";

  const Tabbar = ({ activeTab, setActiveTab, showTerminal }) => {
    const tabs = [
      { id: "projects", icon: <Folder size={17} /> },
      { id: "terminal", icon: <TerminalSquare size={20} /> },
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
                ? "bg-[#373737]"
                : "hover:bg-[#373737]"
            }`}
          >
            {tab.icon}
          </button>
        ))}
      </div>
    );
  };

  export default Tabbar;
