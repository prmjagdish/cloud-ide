import React from "react";
import { DropdownMenuItem } from "@/components/ui/dropdown-menu";
import { Upload } from "lucide-react";

const UploadProjectItem = () => {
  return (
    <DropdownMenuItem className="flex items-center gap-2 hover:bg-[#373737] hover:text-white focus:bg-[#373737] focus:text-white focus:outline-none">
      <Upload className="w-4 h-4" /> Upload Project
    </DropdownMenuItem>
  );
};

export default UploadProjectItem;
