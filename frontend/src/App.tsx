import { Navigate, Route, Routes } from "react-router";

import { AppLayout } from "./layouts/AppLayout";
import { ProtectedRoute } from "./routes/ProtectedRoute";
import { LoginPage } from "./pages/LoginPage";
import { DashboardPage } from "./pages/DashboardPage";
import { MyRequestsPage } from "./pages/MyRequestsPage";
import { CreateRequestPage } from "./pages/CreateRequestPage";
import { RequestDetailPage } from "./pages/RequestDetailPage";
import { ApprovalPage } from "./pages/ApprovalPage";
import { FinanceDisbursementsPage } from "./pages/FinanceDisbursementsPage";

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/requests" element={<MyRequestsPage />} />
          <Route path="/requests/new" element={<CreateRequestPage />} />
          <Route path="/requests/:id" element={<RequestDetailPage />} />
          <Route path="/approvals" element={<ApprovalPage />} />
          <Route path="/finance/disbursements" element={<FinanceDisbursementsPage />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}

export default App;
