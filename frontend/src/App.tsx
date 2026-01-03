import { AuthenticatedApp } from "@/components/AuthenticatedApp";
import { DashboardRedirect } from "@/components/auth/DashboardRedirect";
import { UnifiedAuthProvider } from "@/context/UnifiedAuthContext";
import { ShopProvider } from "@/context/ShopContext";
import { LandingPage } from "@/pages/LandingPage";
import { RegisterPage } from "@/pages/auth/RegisterPage";
import { EmbeddedLoginPage } from "@/pages/auth/EmbeddedLoginPage";
import { CloudTenantRegisterPage } from "@/pages/cloud/CloudTenantRegisterPage";
import { RegistrationSuccessPage } from "@/pages/cloud/RegistrationSuccessPage";
import { Route, Routes } from "react-router-dom";
import configService from "@/config/runtime-config";

function App() {
  const isEmbedded = configService.isEmbeddedMode;

  return (
    <UnifiedAuthProvider>
      <ShopProvider>
        <Routes>
          {/* Public Routes - No authentication required */}
          <Route path="/" element={<LandingPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/redirect" element={<DashboardRedirect />} />

          {/* Cloud tenant registration - Public routes */}
          <Route path="/cloud/register" element={<CloudTenantRegisterPage />} />
          <Route path="/cloud/register/success" element={<RegistrationSuccessPage />} />

          {/* Embedded mode login page */}
          {isEmbedded && <Route path="/login" element={<EmbeddedLoginPage />} />}

          {/* All authenticated routes - single wildcard to AuthenticatedApp */}
          <Route path="/*" element={<AuthenticatedApp />} />
        </Routes>
      </ShopProvider>
    </UnifiedAuthProvider>
  );
}

export default App;
