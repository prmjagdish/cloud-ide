import React from "react";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { ChevronDown } from "lucide-react";
import UploadProjectItem from "./UploadProjectItem";
import MyProjectsList from "./MyProjectsList";
import CreateProjectWrapper from "../create-project/CreateProjectWrapper";

const ProjectDropdown = () => {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          className="bg-[#252526] hover:bg-[#333333] text-white font-medium py-2 px-4 
          rounded-md transition-colors duration-200 flex items-center gap-2"
        >
          Projects <ChevronDown className="w-3 h-3" />
        </Button>
      </DropdownMenuTrigger>

      <DropdownMenuContent
        className="bg-[#252526] text-white border-gray-800 max-h-64 overflow-auto"
      >
        <CreateProjectWrapper />
        <UploadProjectItem />
        <DropdownMenuSeparator className="bg-gray-800" />
        <DropdownMenuLabel className="text-gray-400">My Projects</DropdownMenuLabel>
        <MyProjectsList />
      </DropdownMenuContent>
    </DropdownMenu>
  );
};

export default ProjectDropdown;
