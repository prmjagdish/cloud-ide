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
    console.log("name from project.js", name);
    const response = await apiClient.post("/projects/bootstrap", {
      name,
      buildTool: "MAVEN",
      language: "JAVA",
    });
    console.log(response.data);
    return response.data; 
  } catch (error) {
    console.log("create:", error);
    throw error; 
  }
};

export const getFolderStructure = async ({ projectId }) => {
  try {
    console.log("API called with projectId:", projectId);
    const response = await apiClient.get(`/p/${projectId}/folder/structure`);
    console.log("API response:", response.data);
    return response.data;
  } catch (error) {
    console.error("Error in getFolderStructure:", error);
    if (error.response) {
      throw new Error(
        `Server error: ${error.response.status} - ${
          error.response.data?.message || "Unknown error"
        }`
      );
    } else if (error.request) {
      throw new Error("Network error: No response from server");
    } else {
      throw new Error(`Request error: ${error.message}`);
    }
  }
};

//  @PutMapping("/{projectId}/rename")
//     public ProjectResponse renameProject(@PathVariable UUID projectId,
//                                          @RequestBody String newName,
//                                          @RequestHeader("userId") UUID userId) {
 export const renameProject = async ({newname, projectId}) => {
  try {
    const response = await apiClient.put(`/${projectId}/rename`,newname
    )
    return response.data;
  } catch (error) {
    console.log("project rename error:",error)
  }
 }