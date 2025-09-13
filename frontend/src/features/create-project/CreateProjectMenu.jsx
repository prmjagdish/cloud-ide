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
      <DropdownMenuSubTrigger className="flex items-center gap-2">
        <Plus className="w-4 h-4" /> Create Project
      </DropdownMenuSubTrigger>

      <DropdownMenuSubContent className="bg-[#252526] text-white border-gray-800">
        <DropdownMenuItem
          className="flex items-center gap-2 hover:bg-[#373737] hover:text-white"
          onSelect={handleSelect}
        >
          <FileCode className="w-4 h-4" /> Java Project
        </DropdownMenuItem>

        {/*
        // Alternate simple approach (uncomment if you prefer): render a plain button
        // inside the dropdown content instead of using DropdownMenuItem.
        <div className="px-2 py-1">
          <button
            onClick={() => {
              console.log("button inside dropdown clicked");
              onOpenJavaModal();
            }}
            className="w-full text-left flex items-center gap-2 px-2 py-1 rounded hover:bg-[#373737]"
          >
            <FileCode className="w-4 h-4" /> Java Project
          </button>
        </div>
        */}
      </DropdownMenuSubContent>
    </DropdownMenuSub>
  );
};

export default CreateProjectMenu;
