import React, { useState, useEffect } from "react"; 
import { Tree } from "react-arborist";
import FileNode from "@/features/file-explorer/FileNode";
import { useFileExplorer } from "@/context/FileExplorerContext";

export default function FileExplorer() {
  const { folderData, loading, error } = useFileExplorer();
  const [treeData, setTreeData] = useState(folderData);
  const [editingId, setEditingId] = useState(null);

  useEffect(() => {
    setTreeData(folderData);
  }, [folderData]);

  console.log("Data from FileExplorer:", treeData);

  if (loading) {
    return (
      <div className="flex items-center justify-center p-4">
        <span>Loading folder structure...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center p-4 text-red-500">
        <span>Error: {error}</span>
      </div>
    );
  }

  if (!treeData || treeData.length === 0) {
    return (
      <div className="flex items-center justify-center p-4 text-gray-500">
        <span>No files found. Select a project.</span>
      </div>
    );
  }

  return (
    <div className="h-full bg-[#1E1E1E] text-gray-200 border-r border-gray-800 flex flex-col">
      <div className="flex items-center justify-between p-2 border-b border-gray-800">
        <span className="font-semibold text-sm text-gray-200">Explorer</span>
      </div>

      <div className="flex-1 overflow-auto p-2">
        <Tree
          data={treeData.children}
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