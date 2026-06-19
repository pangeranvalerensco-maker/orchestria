import { Navigate, Route, Routes } from "react-router";

import { AppLayout } from "./layouts/AppLayout";
import { PermissionRoute } from "./routes/PermissionRoute";
import { ProtectedRoute } from "./routes/ProtectedRoute";
import { LoginPage } from "./pages/LoginPage";
import { DashboardPage } from "./pages/DashboardPage";
import { MyRequestsPage } from "./pages/MyRequestsPage";
import { CreateRequestPage } from "./pages/CreateRequestPage";
import { RequestDetailPage } from "./pages/RequestDetailPage";
import { RequestSettlementPage } from "./pages/RequestSettlementPage";
import { ApprovalPage } from "./pages/ApprovalPage";
import { FinanceDisbursementsPage } from "./pages/FinanceDisbursementsPage";
import { SettlementVerificationPage } from "./pages/SettlementVerificationPage";
import { ReportsPage } from "./pages/ReportsPage";

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
          <Route path="/requests/:id/settlement" element={<RequestSettlementPage />} />

          <Route
            element={(
              <PermissionRoute
                anyOf={[
                  "request.approve.division",
                  "request.approve.pub",
                  "request.approve.pembina",
                ]}
              />
            )}
          >
            <Route path="/approvals" element={<ApprovalPage />} />
          </Route>

          <Route element={<PermissionRoute anyOf={["finance.disburse"]} />}>
            <Route path="/finance/disbursements" element={<FinanceDisbursementsPage />} />
          </Route>

          <Route element={<PermissionRoute anyOf={["finance.settlement.verify"]} />}>
            <Route path="/finance/settlements" element={<SettlementVerificationPage />} />
          </Route>

          <Route element={<PermissionRoute anyOf={["request.read.all"]} />}>
            <Route path="/reports" element={<ReportsPage />} />
          </Route>
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}

export default App;
