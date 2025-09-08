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
import CreateProjectMenu from "./CreateProjectMenu";
import UploadProjectItem from "./UploadProjectItem";
import MyProjectsList from "./MyProjectsList";

const ProjectDropdown = () => {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          variant="ghost"
          className="bg-[#252526] text-sm rounded-none flex items-center gap-2 hover:bg-transparent"
        >
          Projects <ChevronDown className="w-3 h-3" />
        </Button>
      </DropdownMenuTrigger>

      <DropdownMenuContent className="bg-[#252526] text-white border-gray-800">
        <CreateProjectMenu />
        <UploadProjectItem />
        <DropdownMenuSeparator className="bg-gray-800" />
        <DropdownMenuLabel className="text-gray-400">My Projects</DropdownMenuLabel>
        <MyProjectsList />
      </DropdownMenuContent>
    </DropdownMenu>
  );
};

export default ProjectDropdown;
