import {
  Navigate,
  Route,
  Routes,
} from "react-router";

import {
  CreateRequestPage,
} from "./pages/CreateRequestPage";
import {
  RequestDetailPage,
} from "./pages/RequestDetailPage";
import { AppLayout } from "./layouts/AppLayout";
import { DashboardPage } from "./pages/DashboardPage";
import { MyRequestsPage } from "./pages/MyRequestsPage";
import { LoginPage } from "./pages/LoginPage";
import { ProtectedRoute } from "./routes/ProtectedRoute";
import { ApprovalPage } from "./pages/ApprovalPage";


function App() {
  return (
    <Routes>
      <Route
        path="/login"
        element={<LoginPage />}
      />

      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route
            index
            element={
              <Navigate
                to="/dashboard"
                replace
              />
            }
          />

          <Route
            path="/dashboard"
            element={<DashboardPage />}
          />
          <Route
            path="/requests"
            element={<MyRequestsPage />}
          />
          <Route
            path="/requests/new"
            element={<CreateRequestPage />}
          />
          <Route
            path="/requests/:id"
            element={<RequestDetailPage />}
          />
          <Route
            path="/approvals"
            element={<ApprovalPage />}
          />
        </Route>
      </Route>

      <Route
        path="*"
        element={
          <Navigate
            to="/dashboard"
            replace
          />
        }
      />
    </Routes>
  );
}

export default App;