import { AuthenticatedApp } from "@/components/AuthenticatedApp";
import { DashboardRedirect } from "@/components/auth/DashboardRedirect";
import { ManualAuthProvider } from "@/context/ManualAuthContext";
import { ShopProvider } from "@/context/ShopContext";
import { LandingPage } from "@/pages/LandingPage";
import { RegisterPage } from "@/pages/auth/RegisterPage";
import { Route, Routes } from "react-router-dom";

function App() {
  return (
    <ManualAuthProvider>
      <ShopProvider>
        <Routes>
          {/* Public Routes - No authentication required */}
          <Route path="/" element={<LandingPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/redirect" element={<DashboardRedirect />} />

          {/* All authenticated routes - single wildcard to AuthenticatedApp */}
          <Route path="/*" element={<AuthenticatedApp />} />
        </Routes>
      </ShopProvider>
    </ManualAuthProvider>
  );
}

export default App;
