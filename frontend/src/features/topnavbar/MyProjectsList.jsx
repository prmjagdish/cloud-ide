import React from "react";
import { DropdownMenuItem } from "@/components/ui/dropdown-menu";

const dummyProjects = [
  "Portfolio Website",
  "E-commerce Store",
  "Chat Application",
  "Blog Platform",
  "Cloud IDE Project",
];

const MyProjectsList = () => {
  return (
    <>
      {dummyProjects.map((proj, index) => (
        <DropdownMenuItem
          key={index}
          className="hover:bg-[#373737] hover:text-white focus:bg-[#373737] focus:text-white focus:outline-none"
        >
          {proj}
        </DropdownMenuItem>
      ))}
    </>
  );
};

export default MyProjectsList;
