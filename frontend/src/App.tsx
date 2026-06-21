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
import { OrganizationDirectoryPage } from "./pages/OrganizationDirectoryPage";
import { OrganizationAdminPage } from "./pages/OrganizationAdminPage";
import { DivisionTasksPage } from "./pages/DivisionTasksPage";
import { DivisionTaskDetailPage } from "./pages/DivisionTaskDetailPage";
import { ArchivePage } from "./pages/ArchivePage";
import { AssetDirectoryPage } from "./pages/AssetDirectoryPage";
import { AssetDetailPage } from "./pages/AssetDetailPage";
import { MyBorrowingsPage } from "./pages/MyBorrowingsPage";
import { AssetOperationsPage } from "./pages/AssetOperationsPage";
import { AssetBorrowingDetailPage } from "./pages/AssetBorrowingDetailPage";
import { PicketSchedulesPage } from "./pages/PicketSchedulesPage";
import { PicketReportPage } from "./pages/PicketReportPage";
import { EnglishActivityPage } from "./pages/EnglishActivityPage";
import { EnglishManagementPage } from "./pages/EnglishManagementPage";
import { PublicLayout } from "./layouts/PublicLayout";
import { PublicHomePage } from "./pages/public/PublicHomePage";
import { PublicAboutPage } from "./pages/public/PublicAboutPage";
import { PublicOrganizationPage } from "./pages/public/PublicOrganizationPage";
import { PublicActivitiesPage } from "./pages/public/PublicActivitiesPage";
import { PublicContentManagementPage } from "./pages/PublicContentManagementPage";
import { useAuth } from "./auth/useAuth";

function NotFoundRedirect() {
  const { token } = useAuth();
  return token ? <Navigate to="/dashboard" replace /> : <Navigate to="/" replace />;
}

function App() {
  return (
    <Routes>
      <Route element={<PublicLayout />}>
        <Route path="/" element={<PublicHomePage />} />
        <Route path="/about" element={<PublicAboutPage />} />
        <Route path="/public/organization" element={<PublicOrganizationPage />} />
        <Route path="/activities" element={<PublicActivitiesPage />} />
      </Route>

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

          <Route element={<PermissionRoute anyOf={["organization.read"]} />}>
            <Route path="/organization" element={<OrganizationDirectoryPage />} />
          </Route>

          <Route element={<PermissionRoute anyOf={["archive.manage"]} />}>
            <Route path="/archive" element={<ArchivePage />} />
          </Route>

          <Route element={<PermissionRoute anyOf={["organization.manage"]} />}>
            <Route path="/admin/organization" element={<OrganizationAdminPage />} />
          </Route>

          <Route element={<PermissionRoute anyOf={["public.content.manage"]} />}>
            <Route path="/public-content-management" element={<PublicContentManagementPage />} />
          </Route>

          <Route element={<PermissionRoute anyOf={["division.task.read"]} />}>
            <Route path="/division-tasks" element={<DivisionTasksPage />} />
            <Route path="/division-tasks/:id" element={<DivisionTaskDetailPage />} />
          </Route>

          <Route element={<PermissionRoute anyOf={["asset.read"]} />}>
            <Route path="/assets" element={<AssetDirectoryPage />} />
            <Route path="/assets/:id" element={<AssetDetailPage />} />
          </Route>

          <Route element={<PermissionRoute anyOf={["asset.borrow.read.own"]} />}>
            <Route path="/my-borrowings" element={<MyBorrowingsPage />} />
          </Route>

          <Route element={<PermissionRoute anyOf={["asset.borrow.read.all"]} />}>
            <Route path="/asset-operations" element={<AssetOperationsPage />} />
          </Route>

          <Route element={<PermissionRoute anyOf={["asset.borrow.read.own", "asset.borrow.read.all"]} />}>
            <Route path="/asset-borrowings/:id" element={<AssetBorrowingDetailPage />} />
          </Route>
          <Route element={<PermissionRoute anyOf={["cleanliness.schedule.read"]} />}>
            <Route path="/picket-schedules" element={<PicketSchedulesPage />} />
          </Route>
          <Route element={<PermissionRoute anyOf={["cleanliness.report.read"]} />}>
            <Route path="/picket-reports" element={<PicketReportPage />} />
          </Route>
          <Route element={<PermissionRoute anyOf={["english.activity.read", "english.deposit.read.own"]} />}>
            <Route path="/english-activities" element={<EnglishActivityPage />} />
          </Route>
          <Route element={<PermissionRoute anyOf={["english.activity.manage", "english.deposit.read.all", "english.deposit.verify", "english.report.read"]} />}>
            <Route path="/english-management" element={<EnglishManagementPage />} />
          </Route>
        </Route>
      </Route>
      
      <Route path="*" element={<NotFoundRedirect />} />
    </Routes>
  );
}

export default App;
