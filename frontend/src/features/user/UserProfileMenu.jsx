import React from "react";
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
} from "@/components/ui/dropdown-menu";
import { User, CreditCard, HelpCircle, Settings, LogOut } from "lucide-react";
import { useAuth } from "@/context/AuthContext";

const UserProfileMenu = () => {

  const { logout } = useAuth();

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <button className="flex items-center justify-center w-10 h-10 rounded-full bg-[#252526] hover:bg-[#373737] transition ml-4">
          <User size={20} />
        </button>
      </DropdownMenuTrigger>

      <DropdownMenuContent
        align="end"
        sideOffset={5}
        className="w-56 bg-[#252526] border border-gray-800 rounded-lg p-1 shadow-lg"
      >
        <div className="flex items-center gap-2 px-2 py-2 text-gray-300 font-medium text-sm border-b border-gray-800">
          <User className="flex-shrink-0" size={16} />
          <span className="truncate max-w-[calc(100%-24px)]">{"userEdddddddddmailhkjgfiufuw"}</span>
        </div>

        <DropdownMenuItem
          className="flex items-center gap-2 text-gray-300 text-sm
                              data-[highlighted]:bg-[#373737] data-[highlighted]:text-gray-300"
        >
          <CreditCard size={16} /> Upgrade Plan
        </DropdownMenuItem>

        <DropdownMenuItem
          className="flex items-center gap-2 text-gray-300 text-sm
                              data-[highlighted]:bg-[#373737] data-[highlighted]:text-gray-300"
        >
          <HelpCircle size={16} /> Help
        </DropdownMenuItem>

        <DropdownMenuItem
          className="flex items-center gap-2 text-gray-300 text-sm
                              data-[highlighted]:bg-[#373737] data-[highlighted]:text-gray-300"
        >
          <Settings size={16} /> Settings
        </DropdownMenuItem>

        <DropdownMenuItem
        onClick={logout}
          className="flex items-center gap-2 text-gray-300 text-sm
                              data-[highlighted]:bg-[#373737] data-[highlighted]:text-gray-300"
        >
          <LogOut size={16} /> Log Out
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
};

export default UserProfileMenu;
