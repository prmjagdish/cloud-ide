import React, { useState } from "react";
import { Tree } from "react-arborist";
import FileNode from "@/features/fileexplorer/FileNode";

const initialData = [
  {
    id: "root",
    name: "root",
    children: [
      {
        id: "src",
        name: "src",
        children: [
          { id: "App.js", name: "App.js" },
          { id: "Editor.jsx", name: "Editor.jsx" },
          { id: "Terminal.ts", name: "Terminal.ts" },
        ],
        
      },
      { id: "package.json", name: "package.json" },
      { id: "README.md", name: "README.md" },
    ],
  },
];

export default function FileExplorer() {
  const [treeData, setTreeData] = useState(initialData);
  const [editingId, setEditingId] = useState(null);

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
