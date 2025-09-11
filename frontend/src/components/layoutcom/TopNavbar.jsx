import React from "react";
import RunControls from "../../features/topnavbar/RunControls";
import DateTimeDisplay from "../../features/topnavbar/DateTimeDisplay";
import ProjectDropdown from "../../features/topnavbar/ProjectDropdown";
import UserProfileMenu from "@/features/user/UserProfileMenu";
import { Button } from "../ui/button";
import { Link } from "react-router-dom";
import { useAuth } from "@/context/AuthContext";

const TopNavbar = () => {
  const { isAuthenticated } = useAuth();

  return (
    <div className="w-full flex h-10 items-center justify-between bg-[#252526] text-white px-4 py-2 shadow-md border-gray-800 border-b">
      <div className="flex items-center gap-11">
        <span className="font-bold text-lg">☁️</span>
        <ProjectDropdown />
      </div>

      <RunControls />

      {isAuthenticated ? (
        <div className="flex items-center gap-11">
          <DateTimeDisplay />
          <UserProfileMenu />
        </div>
      ) : (
        <div className="flex items-center gap-4">
          <Link to="/login">
            <Button className="bg-[#252526] hover:bg-[#333333] text-white font-medium py-2 px-4 border border-gray-600 rounded-md transition-colors duration-200">
              Login
            </Button>
          </Link>
          <Link to="/signup">
            <Button className="bg-[#252526] hover:bg-[#333333] text-white font-medium py-2 px-4 border border-gray-600 rounded-md transition-colors duration-200">
              Sign Up
            </Button>
          </Link>
        </div>
      )}
    </div>
  );
};

export default TopNavbar;
