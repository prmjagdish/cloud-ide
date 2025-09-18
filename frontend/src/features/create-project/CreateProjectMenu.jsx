import React from "react";
import {
  DropdownMenuSub,
  DropdownMenuSubTrigger,
  DropdownMenuSubContent,
  DropdownMenuItem,
} from "@/components/ui/dropdown-menu";
import { Plus, FileCode } from "lucide-react";

const CreateProjectMenu = ({ onOpenJavaModal }) => {

  const handleSelect = (event) => {
    console.log("🔹 DropdownMenuItem selected:", event);

    // Prevent the dropdown's default "select -> close" behavior from interfering
    if (event && typeof event.preventDefault === "function") {
      event.preventDefault();
    }

    // Ensure we open the modal after the dropdown internal logic finishes.
    // Zero-delay setTimeout defers the call to the next tick.
    setTimeout(() => {
      console.log("🔸 calling onOpenJavaModal()");
      onOpenJavaModal();
    }, 0);
  };

  return (
    <DropdownMenuSub>
      <DropdownMenuSubTrigger className="flex items-center gap-2 
             data-[highlighted]:bg-[#333333] data-[highlighted]:text-white
             data-[state=open]:bg-[#333333] data-[state=open]:text-white
             transition-colors duration-200">
        <Plus className="w-4 h-4" /> Create Project
      </DropdownMenuSubTrigger>

      <DropdownMenuSubContent className="bg-[#252526] text-white border-gray-800">
        <DropdownMenuItem
          className="flex items-center gap-2 data-[highlighted]:bg-[#333333] data-[highlighted]:text-white"
          onSelect={handleSelect}
        >
          <FileCode className="w-4 h-4" /> Java Project
        </DropdownMenuItem>
      </DropdownMenuSubContent>
    </DropdownMenuSub>
  );
};

export default CreateProjectMenu;
