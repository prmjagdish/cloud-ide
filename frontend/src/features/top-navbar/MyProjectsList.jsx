import React, { useEffect, useState } from "react";
import { DropdownMenuItem } from "@/components/ui/dropdown-menu";
import { getProjectsByUser } from "@/utils/apis/project";
import { useFileExplorer } from "@/context/FileExplorerContext";

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
      console.error("Failed to load folder structure for project:", projectId, err);
    }
  };

  if (loading) {
    return (
      <DropdownMenuItem disabled>
        Loading...
      </DropdownMenuItem>
    );
  }

  if (projects.length === 0) {
    return (
      <DropdownMenuItem disabled>
        No projects found
      </DropdownMenuItem>
    );
  }

  return (
    <>
      {projects.map((proj) => (
        <DropdownMenuItem
          key={proj.id}
          onSelect={(e) => handleProjectClick(e, proj.id)}           className="hover:bg-[#373737] hover:text-white focus:bg-[#373737] focus:text-white focus:outline-none p-2 cursor-pointer"
        >
          {proj.name}
        </DropdownMenuItem>
      ))}
    </>
  );
};

export default MyProjectsList;
