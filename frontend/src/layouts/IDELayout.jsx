import React, { useState } from "react";
import Split from "react-split";
import TopNavbar from "@/components/layoutcom/TopNavbar";
import LeftSidebar from "@/components/layoutcom/FileExplorerWrapper";
import EditorArea from "@/components/layoutcom/EditorWrapper";
import Terminal from "@/components/layoutcom/TerminalWrapper";
import Tabbar from "@/components/layoutcom/Tabbar";

const IDELayout = () => {
  const [activeTab, setActiveTab] = useState("files");
  const [showTerminal, setShowTerminal] = useState(true);

  const handleTabClick = (tabId) => {
    if (tabId === "terminal") {
      setShowTerminal((prev) => !prev);
    } else {
      setActiveTab(tabId);
    }
  };

  return (
    <div className="h-screen flex flex-col overflow-hidden bg-[#1E1E1E] text-gray-200">
      <TopNavbar />
      <div className="flex flex-1 overflow-hidden">
        <div className="w-12 bg-[#252526] text-white flex flex-col">
          <Tabbar
            activeTab={activeTab}
            setActiveTab={handleTabClick}
            showTerminal={showTerminal}
          />
        </div>

        <Split
          className="flex-1 flex flex-col overflow-hidden"
          direction="vertical"
          sizes={showTerminal ? [70, 30] : [100, 0]}
          minSize={showTerminal ? [100, 80] : [100, 0]}
          gutterSize={4} // smaller gutter
          gutterAlign="center"
          snapOffset={0}
          cursor="row-resize"
          style={{ display: "flex" }}
        >
          <Split
            className="flex flex-1 overflow-hidden"
            direction="horizontal"
            sizes={[20, 80]}
            minSize={[150, 300]}
            gutterSize={4} // smaller gutter
            gutterAlign="center"
            snapOffset={0}
            cursor="col-resize"
            style={{ display: "flex" }}
          >
            <div className="flex flex-col h-full overflow-hidden bg-[#252526]">
              {activeTab === "files" && <LeftSidebar />}
              {activeTab === "search" && (
                <div className="p-2">🔍 Search Panel</div>
              )}
              {activeTab !== "files" && activeTab !== "search" && (
                <div className="p-2">No Sidebar</div>
              )}
            </div>

            <div className="flex-1 h-full overflow-hidden border-b border-gray-800  ">
              <EditorArea />
            </div>
          </Split>

          {showTerminal ? <Terminal /> : <div style={{ height: 0 }} />}
        </Split>
      </div>
    </div>
  );
};

export default IDELayout;
