import React from "react";
import {
  DropdownMenuSub,
  DropdownMenuSubTrigger,
  DropdownMenuSubContent,
  DropdownMenuItem,
} from "@/components/ui/dropdown-menu";
import { Plus, FileCode } from "lucide-react";

const languages = ["Java", "Python", "C++", "JavaScript"];

const CreateProjectMenu = () => {
  return (
    <DropdownMenuSub>
      <DropdownMenuSubTrigger className="flex items-center gap-2">
        <Plus className="w-4 h-4" /> Create Project
      </DropdownMenuSubTrigger>
      <DropdownMenuSubContent className="bg-[#252526] text-white border-gray-800">
        {languages.map((lang, i) => (
          <DropdownMenuItem
            key={i}
            className="flex items-center gap-2 hover:bg-[#373737] hover:text-white focus:bg-[#373737] focus:text-white focus:outline-none"
          >
            <FileCode className="w-4 h-4" /> {lang} Project
          </DropdownMenuItem>
        ))}
      </DropdownMenuSubContent>
    </DropdownMenuSub>
  );
};

export default CreateProjectMenu;
