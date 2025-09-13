import apiClient from "./apiClient";

//  No need to pass userId, backend extracts it from JWT
const getProjectsByUser = async () => {
  try {
    const response = await apiClient.get("/projects");
    return response.data; // project list
  } catch (error) {
    console.error("Error fetching projects:", error);
    throw error;
  }
};

const createProject = async ({ name, buildTool, userId}) => {
  try {
    const response = await apiClient.post(
      "/bootstrap",
      {
        name: name,
        buildTool: buildTool,
        language: "JAVA",
      },
      {
        headers: {
          "userId": userId
        },
      }
    );
    return response.data;
  } catch (error) {
    console.log(error);
  }
};

export default { getProjectsByUser, createProject };
