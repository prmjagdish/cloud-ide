import React, { useEffect, useState } from "react";
import { DropdownMenuItem } from "@/components/ui/dropdown-menu";
import { getProjectsByUser } from "@/utils/apis/project"; 

const MyProjectsList = () => {
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const data = await getProjectsByUser();
        console.log("Projects:", data);
        setProjects(data);
      } catch (err) {
        console.error("Failed to load projects:", err);
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  if (loading) {
    return <DropdownMenuItem className="p-2 text-gray-400">Loading...</DropdownMenuItem>;
  }

  if (projects.length === 0) {
    return <DropdownMenuItem className="p-2 text-gray-400">No projects found</DropdownMenuItem>;
  }

  return (
    <>
      {projects.map((proj) => (
        <DropdownMenuItem
          key={proj.id}
          className="hover:bg-[#373737] hover:text-white focus:bg-[#373737] focus:text-white focus:outline-none p-2 cursor-pointer"
        >
          {proj.name}
        </DropdownMenuItem>
      ))}
    </>
  );
};

export default MyProjectsList;
