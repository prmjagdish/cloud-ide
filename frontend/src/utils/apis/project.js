import apiClient from "./apiClient";

export const getProjectsByUser = async () => {
  try {
    const response = await apiClient.get("/projects");
    return response.data; 
  } catch (error) {
    console.error("Error fetching projects:", error);
    throw error;
  }
};

export const createProject = async ({ name }) => {
  try {
    console.log("name from project.js",name);
    const response = await apiClient.post(
      "/projects/bootstrap",
      {
        name,
        buildTool: "MAVEN ",
        language: "JAVA",
      }
    );
    console.log(response.data);
  } catch (error) {
    console.log("create :" , error);
  }
};

