import React, { useState } from "react";
import { Tree } from "react-arborist";
import { Folder, FolderOpen, File as FileIcon } from "lucide-react";

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
          { id: "Editor.js", name: "Editor.js" },
          { id: "Terminal.js", name: "Terminal.js" },
        ],
      },
      { id: "package.json", name: "package.json" },
    ],
  },
];

function FileNode({ node, style }) {
  return (
    <div
      style={style}
      className="flex items-center px-2 py-1 cursor-pointer hover:bg-gray-800 rounded-md"
    >
      {node.data.children ? (
        node.isOpen ? (
          <FolderOpen size={16} className="text-yellow-300 mr-2" />
        ) : (
          <Folder size={16} className="text-yellow-300 mr-2" />
        )
      ) : (
        <FileIcon size={16} className="text-blue-300 mr-2" />
      )}
      <span className="flex-1 text-sm text-gray-200">{node.data.name}</span>
    </div>
  );
}

export default function FileExplorer() {
  const [treeData, setTreeData] = useState(initialData);

  return (
    <div className="h-full bg-[#1E1E1E] text-gray-200 border-r border-gray-800 flex flex-col">
      <div className="flex items-center justify-between p-2 border-b border-gray-800">
        <span className="font-semibold text-sm text-gray-200">EXPLORER</span>
      </div>

      <div className="flex-1 overflow-auto p-2">
        <Tree
          data={treeData}
          width="100%"
          height={500}
          onChange={setTreeData}
          rowHeight={24}
        >
          {FileNode}
        </Tree>
      </div>
    </div>
  );
}
