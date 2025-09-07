import React from "react";
import RunControls from "../topnavbar/RunControls";
import DateTimeDisplay from "../topnavbar/DateTimeDisplay";
import ProjectDropdown from "../topnavbar/ProjectDropdown";

const TopNavbar = () => {
  return (
    <div className="w-full flex h-10 items-center justify-between bg-[#252526] text-white px-4 py-2 shadow-md">
      <div className="flex items-center gap-11">
        <span className="font-bold text-lg">☁️</span>
        <ProjectDropdown />
      </div>

      <RunControls />

      <DateTimeDisplay />
    </div>
  );
};

export default TopNavbar;
