import apiClient from "./apiClient";

// Login
export const login = async ({ username, password }) => {
  try {
    const res = await apiClient.post("/auth/login", { username, password });
    const { token } = res.data;
    localStorage.setItem("token", token);
    return token;
  } catch (error) {
    console.error("Login failed:", error);
    throw error;
  }
};

// Logout
export const logout = () => {
  localStorage.removeItem("token");
  // optionally call backend /auth/logout
};

// Register
export const registerUser = async ({ email, password }) => {
  try {
    const res = await apiClient.post("/auth/register", {
      username: email, // map email to username
      password,
    });
    return res.data;
  } catch (error) {
    console.error("Signup failed:", error);
    throw error;
  }
};
