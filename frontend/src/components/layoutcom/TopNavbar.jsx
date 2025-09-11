import React from "react";
import RunControls from "../../features/topnavbar/RunControls";
import DateTimeDisplay from "../../features/topnavbar/DateTimeDisplay";
import ProjectDropdown from "../../features/topnavbar/ProjectDropdown";
import UserProfileMenu from "@/features/user/UserProfileMenu";

const TopNavbar = () => {
  return (
    <div className="w-full flex h-10 items-center justify-between bg-[#252526] text-white px-4 py-2 shadow-md border-gray-800 border-b">
      <div className="flex items-center gap-11">
        <span className="font-bold text-lg">☁️</span>
        <ProjectDropdown />
      </div>

      <RunControls />

      <div className="flex items-center gap-11">
        <DateTimeDisplay />
        <UserProfileMenu />
      </div>
    </div>
  );
};

export default TopNavbar;
