import React from 'react';
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Routes, Route } from "react-router-dom";
import './index.css'
import App from './App.jsx'
import IDELayout from './layouts/IDELayout';

createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/ide" element={<IDELayout />} />
      </Routes>
    </BrowserRouter>
  </React.StrictMode>
);