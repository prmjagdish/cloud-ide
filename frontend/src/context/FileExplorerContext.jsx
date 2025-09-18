import React, { createContext, useContext, useState } from "react";
import { getFolderStructure } from "@/utils/apis/project"; // UNCOMMENTED THIS

const FileExplorerContext = createContext();

export const FileExplorerProvider = ({ children }) => {
  const [selectedProject, setSelectedProject] = useState(null);
  const [folderData, setFolderData] = useState([]);
  const [loading, setLoading] = useState(false); // Added loading state
  const [error, setError] = useState(null); // Added error state

  const loadFolderStructure = async (projectId) => {
    try {
      console.log("loadfolder structure fun called", projectId);
      setLoading(true);
      setError(null);
      setSelectedProject(projectId);
      
      const data = await getFolderStructure({ projectId });
      setFolderData(data.children);
      console.log("from context", data);
    } catch (err) {
      console.error("Error fetching folder structure:", err);
      setError(err.message || "Failed to load folder structure");
      setFolderData([]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <FileExplorerContext.Provider
      value={{
        selectedProject,
        folderData,
        loading,
        error,
        loadFolderStructure,
      }}
    >
      {children}
    </FileExplorerContext.Provider>
  );
};

export const useFileExplorer = () => useContext(FileExplorerContext);