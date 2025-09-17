import React, { createContext, useContext, useState } from "react";
import { getFolderStructure } from "@/utils/apis/project";

const FileExplorerContext = createContext();

export const FileExplorerProvider = ({ children }) => {
  const [selectedProject, setSelectedProject] = useState(null);
  const [folderData, setFolderData] = useState([]);

  const loadFolderStructure = async (projectId) => {
  console.log("Function called with projectId:", projectId);
  try {
    setSelectedProject(projectId);
    console.log("State set, calling API...");
    const data = await getFolderStructure({ projectId });
    console.log("API returned:", data);
    setFolderData(data);
    console.log("State updated with folderData");
  } catch (err) {
    console.error("Error fetching folder structure:", err);
  }
};


  return (
    <FileExplorerContext.Provider
      value={{ selectedProject, folderData, loadFolderStructure }}
    >
      {children}
    </FileExplorerContext.Provider>
  );
};

export const useFileExplorer = () => useContext(FileExplorerContext);
