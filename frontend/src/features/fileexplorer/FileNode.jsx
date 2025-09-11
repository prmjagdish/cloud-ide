import React from "react";
import {
  Folder,
  FolderOpen,
  File as FileIcon,
  Edit,
  Trash,
} from "lucide-react";
import { getFileOrFolderIcon } from "@/components/ui/FileIcon";

export default function FileNode({ node, style, editingId, setEditingId }) {
  const isFolder = !!node.data.children;

  return (
    <div
      style={style}
      className="group flex items-center px-2 py-1 cursor-pointer hover:bg-[#373737] rounded-md"
      onDoubleClick={() => isFolder && node.toggle()}
    >
      {/* Icon */}
      {isFolder
        ? node.isOpen
          ? <FolderOpen size={16} className="text-yellow-300 mr-2 ml-2" />
          : <Folder size={16} className="text-yellow-300 mr-2 ml-2" />
        : getFileOrFolderIcon(node)}


      {/* Name or Input */}
      {editingId === node.id ? (
        <input
          autoFocus
          defaultValue={node.data.name}
          onBlur={(e) => {
            node.data.name = e.target.value || node.data.name;
            node.tree.update(node);
            setEditingId(null);
          }}
          onKeyDown={(e) => {
            if (e.key === "Enter") e.target.blur();
            if (e.key === "Escape") setEditingId(null);
          }}
          className="bg-gray-700 text-gray-200 text-sm px-1 rounded w-full"
        />
      ) : (
        <span className="flex-1 text-sm text-gray-200">{node.data.name}</span>
      )}

      {/* Hover Actions */}
      <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition">
        {isFolder && (
          <>
            {/* New File */}
            <button
              onClick={(e) => {
                e.stopPropagation();
                node.tree.create({
                  parentId: node.id,
                  type: "file",
                  name: "newFile.js",
                });
              }}
              className="p-1 hover:bg-gray-700 rounded"
              title="Add File"
            >
              <FileIcon size={14} />
            </button>

            {/* New Folder */}
            <button
              onClick={(e) => {
                e.stopPropagation();
                node.tree.create({
                  parentId: node.id,
                  type: "folder",
                  name: "newFolder",
                  children: [],
                });
              }}
              className="p-1 hover:bg-gray-700 rounded"
              title="Add Folder"
            >
              <Folder size={14} />
            </button>
          </>
        )}

        {/* Rename */}
        <button
          onClick={(e) => {
            e.stopPropagation();
            setEditingId(node.id);
          }}
          className="p-1 hover:bg-gray-700 rounded"
          title="Rename"
        >
          <Edit size={14} />
        </button>

        {/* Delete */}
        <button
          onClick={(e) => {
            e.stopPropagation();
            node.delete();
          }}
          className="p-1 hover:bg-gray-700 rounded"
          title="Delete"
        >
          <Trash size={14} />
        </button>
      </div>
    </div>
  );
}
