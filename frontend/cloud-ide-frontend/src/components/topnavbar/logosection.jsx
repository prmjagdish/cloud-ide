import React from "react";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

const dummyProjects = [
  "Portfolio Website",
  "E-commerce Store",
  "Chat Application",
  "Blog Platform",
  "Cloud IDE Project",
];

const LogoSection = () => {
  return (
    <div className="flex items-center gap-10">
      <span className="font-bold text-lg">☁️</span>
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="secondary" className="bg-[#252526] text-white">
            Projects
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuItem>Create Project</DropdownMenuItem>
          <DropdownMenuItem>Upload Project</DropdownMenuItem>
          <DropdownMenuSeparator />
          <DropdownMenuLabel>My Projects</DropdownMenuLabel>
          {dummyProjects.map((proj, index) => (
            <DropdownMenuItem key={index}>{proj}</DropdownMenuItem>
          ))}
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
  );
};

export default LogoSection;
