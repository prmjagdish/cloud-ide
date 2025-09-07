import React from "react";
import LogoSection from "../topnavbar/logosection";
import RunControls from "../topnavbar/RunControls";
import DateTimeDisplay from "../topnavbar/DateTimeDisplay";

const TopNavbar = () => {
  return (
    <div className="w-full flex h-10 items-center justify-between bg-[#252526] text-white px-4 py-2 shadow-md">
     <LogoSection />
     <RunControls />
     <DateTimeDisplay />
    </div>
  );
};

export default TopNavbar;
