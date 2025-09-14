import React from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import "./index.css";
import App from "./App.jsx";
import IDELayout from "@/layouts/IDELayout";
import Login from "@/pages/Login";
import SignUp from "@/pages/SignUp";
import { AuthProvider } from "@/context/AuthContext";
import { FileExplorerProvider } from "@/context/FileExplorerContext";

createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route
            path="/ide"
            element={
              <FileExplorerProvider>
                <IDELayout />
              </FileExplorerProvider>
            }
          />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<SignUp />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  </React.StrictMode>
);
