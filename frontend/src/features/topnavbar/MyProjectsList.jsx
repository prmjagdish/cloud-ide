import React, { useEffect,useState } from "react";
import { DropdownMenuItem } from "@/components/ui/dropdown-menu";
import getProjectsByUser from "../../utils/apis/project"
import { DropdownMenu, DropdownMenuContent , DropdownMenuTrigger } from "@radix-ui/react-dropdown-menu"

// const dummyProjects = [
//   "Portfolio Website",
//   "E-commerce Store",
//   "Chat Application",
//   "Blog Platform",
//   "Cloud IDE Project",
// ];

const MyProjectsList = () => {
  const [projects, setProjects] = useState([]);
  const [selectedProject, setSelectedProject] = useState("");

  const userId = "77387d80-c9a3-48cc-b332-6f9970ebd752";

  useEffect(() => {
    if (!userId) return;

    getProjectsByUser(userId)
      .then((data) => setProjects(data))
      .catch((err) => console.error(err));
  }, [userId]);

  return (
    <DropdownMenu>
      <DropdownMenuTrigger className="p-2 border rounded-lg w-full text-left">
        {selectedProject ? selectedProject.name : "No project"}
      </DropdownMenuTrigger>

      <DropdownMenuContent className="bg-[#1a1a1a] text-white rounded-md shadow-md w-56">
        {projects.map((proj) => (
          <DropdownMenuItem
            key={proj.id}
            className="hover:bg-[#373737] hover:text-white focus:bg-[#373737] focus:text-white focus:outline-none p-2"
            onSelect={() => setSelectedProject(proj)} // 👈 handle selection
          >
            {proj.name}
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  );
};

export default MyProjectsList;
