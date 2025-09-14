import React, { useState } from "react";
import { Tree } from "react-arborist";
import FileNode from "@/features/file-explorer/FileNode";
import { useFileExplorer } from "@/context/FileExplorerContext";

export default function FileExplorer() {
  const { folderData } = useFileExplorer();
  const [treeData, setTreeData] = useState(folderData);
  const [editingId, setEditingId] = useState(null);

  console.log("Data from FileExplorer :", treeData);

  return (
    <div className="h-full bg-[#1E1E1E] text-gray-200 border-r border-gray-800 flex flex-col">
      <div className="flex items-center justify-between p-2 border-b border-gray-800">
        <span className="font-semibold text-sm text-gray-200">Explorer</span>
      </div>

      <div className="flex-1 overflow-auto p-2">
        <Tree
          data={treeData}
          width="100%"
          height={500}
          onChange={setTreeData}
          rowHeight={24}
          openByDefault={false}
        >
          {(props) => (
            <FileNode
              {...props}
              editingId={editingId}
              setEditingId={setEditingId}
            />
          )}
        </Tree>
      </div>
    </div>
  );
}
