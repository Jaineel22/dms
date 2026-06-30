import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';

import App from './App.jsx';
import './assets/styles/index.css';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <App />
      {/* Global toast container — positioned top-right, auto-dismiss 4s */}
      <Toaster
        position="top-right"
        reverseOrder={false}
        gutter={8}
        toastOptions={{
          duration: 4000,
          style: {
            background: '#ffffff',
            color: '#0f172a',
            fontSize: '0.875rem',
            fontFamily: 'Inter, sans-serif',
            boxShadow: '0 4px 12px rgb(0 0 0 / 0.15)',
            borderRadius: '0.5rem',
            padding: '12px 16px',
          },
          success: {
            iconTheme: {
              primary: '#16a34a',
              secondary: '#ffffff',
            },
          },
          error: {
            iconTheme: {
              primary: '#dc2626',
              secondary: '#ffffff',
            },
            duration: 5000,
          },
        }}
      />
    </BrowserRouter>
  </React.StrictMode>
);