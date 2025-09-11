import { File as FileIcon, Folder, FolderOpen } from "lucide-react";

export function getFileOrFolderIcon(node) {
  const isFolder = !!node.data.children;

  if (isFolder) {
    return node.isOpen ? (
      <FolderOpen size={16} className="text-yellow-300 mr-2 ml-2" />
    ) : (
      <Folder size={16} className="text-yellow-300 mr-2 ml-2" />
    );
  }

  return <FileIcon size={16} className="text-gray-300 mr-2 ml-2" />;
}
