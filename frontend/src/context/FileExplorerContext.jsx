import React, { createContext, useContext, useState } from "react";
// import { getFolderStructure } from "@/utils/apis/project";

const FileExplorerContext = createContext();

export const FileExplorerProvider = ({ children }) => {
  const [selectedProject, setSelectedProject] = useState(null);
  const [folderData, setFolderData] = useState([]);

  const loadFolderStructure = async (projectId) => {
    try {
      console.log("loadfolder structure fun called", projectId);
      setSelectedProject(projectId);
      // const data = await getFolderStructure({ projectId });
      // setFolderData(data);
      // console.log("from context",data); 
    } catch (err) {
      console.error("Error fetching folder structure:", err);
      setFolderData([]);
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
