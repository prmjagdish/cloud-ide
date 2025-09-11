import axios from "axios";

// create axios instance
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
});

const getProjectsByUser = async (userId) => {
  try {
    const responce = await apiClient.get("/projects", {
      headers: {
        userId: userId,
      },
    });
    return responce.data;
  } catch (error) {
    console.error("Error fetching projects:", error);
    throw error;
  }
};

export default getProjectsByUser;
