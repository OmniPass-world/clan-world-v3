import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import './styles/global.css'
import NewLandingPage from './pages/NewLandingPage'
import LegacyLandingPage from './pages/LegacyLandingPage'
import LorePage from './pages/LorePage'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<NewLandingPage />} />
        <Route path="/legacy" element={<LegacyLandingPage />} />
        <Route path="/lore" element={<LorePage />} />
      </Routes>
    </BrowserRouter>
  </StrictMode>,
)
