// ─────────────────────────────────────────────────────────────────────────────
// App.jsx
// Root component — delegates all routing and auth to AppRouter.
// BrowserRouter is provided by main.jsx (one level up).
// ─────────────────────────────────────────────────────────────────────────────

import React from 'react';
import AppRouter from './routes/AppRouter';
import './App.css';

function App() {
  return <AppRouter />;
}

export default App;