import React, { useEffect, useState } from "react";
import { MoreVertical } from "lucide-react";
import { getProjectsByUser } from "@/utils/apis/project";
import { useFileExplorer } from "@/context/FileExplorerContext";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuTrigger,
  DropdownMenuItem,
} from "@/components/ui/dropdown-menu";

const MyProjectsList = () => {
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const { loadFolderStructure } = useFileExplorer();

  useEffect(() => {
    (async () => {
      try {
        const data = await getProjectsByUser();
        setProjects(data);
      } catch (err) {
        console.error("Failed to load projects:", err);
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const handleProjectClick = async (e, projectId) => {
    e.preventDefault();
    try {
      await loadFolderStructure(projectId);
    } catch (err) {
      console.error("Failed to load folder structure:", projectId, err);
    }
  };

  const handleDelete = (projectId) => {
    console.log("Delete project:", projectId);
  };

  const handleRename = (projectId) => {
    console.log("Rename project:", projectId);
  };

  if (loading) {
    return (
      <DropdownMenuItem disabled className="text-gray-400 italic">
        Loading...
      </DropdownMenuItem>
    );
  }

  if (projects.length === 0) {
    return (
      <DropdownMenuItem disabled className="text-gray-400 italic">
        No projects found
      </DropdownMenuItem>
    );
  }

  return (
    <>
      {projects.map((proj) => (
        <div
          key={proj.id}
          className="flex items-center justify-between px-3 py-1 rounded-md 
          hover:bg-[#333333] hover:text-white transition-colors duration-200 cursor-pointer text-sm"
          onClick={(e) => handleProjectClick(e, proj.id)}
        >
          <span className="truncate">{proj.name}</span>

          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <button
                className="p-1.5 rounded-md hover:bg-[#444444] focus:outline-none transition-colors duration-200"
                onClick={(e) => e.stopPropagation()}
              >
                <MoreVertical size={16} />
              </button>
            </DropdownMenuTrigger>

            <DropdownMenuContent
              side="right"
              align="start"
              className="bg-[#252526] text-white border border-gray-800 rounded-md shadow-lg"
            >
              <DropdownMenuItem
                className="data-[highlighted]:bg-[#333333] data-[highlighted]:text-white text-sm"
                onClick={() => handleRename(proj.id)}
              >
                Rename
              </DropdownMenuItem>
              <DropdownMenuItem
                className="data-[highlighted]:bg-[#333333] data-[highlighted]:text-red-400 text-sm"
                onClick={() => handleDelete(proj.id)}
              >
                Delete
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      ))}
    </>
  );
};

export default MyProjectsList;
