import { Refine } from "@refinedev/core";
import {
  ErrorComponent,
  ThemedLayout,
  ThemedSider,
  useNotificationProvider,
} from "@refinedev/antd";
import routerProvider, {
  UnsavedChangesNotifier,
  DocumentTitleHandler,
  NavigateToResource,
} from "@refinedev/react-router";
import { RefineKbar } from "@refinedev/kbar";
import { Button } from "antd";
import {
  ArrowLeftOutlined,
  AppstoreOutlined,
  CheckSquareOutlined,
  PullRequestOutlined,
  HistoryOutlined
} from "@ant-design/icons";
import { Outlet, Route, Routes } from "react-router";
import { Header } from "../header";
import { dataProvider } from "../../providers/data";
import { authProvider } from "../../providers/auth";
import { i18nProvider } from "../../i18n/provider";
import { useRole } from "../../contexts/role";
import { BranchStockPage } from "../../pages/branch/stock";
import { BranchConsumptionPage } from "../../pages/branch/consumption";
import { BranchRequestPage } from "../../pages/branch/request";
import { BranchMovementsPage } from "../../pages/branch/movements";

interface BranchLayoutProps {
  fromSuperAdmin?: boolean;
}

const BackToSuperAdminButton = () => {
  const { setViewingAs } = useRole();
  return (
    <Button
      icon={<ArrowLeftOutlined />}
      type="link"
      onClick={() => setViewingAs(null)}
      style={{ marginBottom: 8, paddingLeft: 0 }}
    >
      Volver al Dashboard
    </Button>
  );
};

export const BranchLayout: React.FC<BranchLayoutProps> = ({
  fromSuperAdmin = false,
}) => {
  return (
    <Refine
      dataProvider={dataProvider}
      authProvider={authProvider}
      i18nProvider={i18nProvider}
      notificationProvider={useNotificationProvider}
      routerProvider={routerProvider}
      resources={[
        {
          name: "branch-stock",
          list: "/my/stock",
          meta: { label: "Mi Stock", icon: <AppstoreOutlined /> },
        },
        {
          name: "branch-consumption",
          list: "/my/consumption",
          meta: { label: "Registrar Consumo", icon: <CheckSquareOutlined /> },
        },
        {
          name: "branch-request",
          list: "/my/request",
          meta: { label: "Solicitar Transferencia", icon: <PullRequestOutlined /> },
        },
        {
          name: "branch-movements",
          list: "/my/movements",
          meta: { label: "Historial", icon: <HistoryOutlined /> },
        },
      ]}
      options={{
        syncWithLocation: true,
        warnWhenUnsavedChanges: true,
      }}
    >
      <Routes>
        <Route
          element={
            <ThemedLayout
              Header={() => <Header sticky />}
              Sider={(props) => (
                <ThemedSider
                  {...props}
                  fixed
                  render={({ items, logout }) => (
                    <>
                      {fromSuperAdmin && <BackToSuperAdminButton />}
                      {items}
                      {logout}
                    </>
                  )}
                />
              )}
            >
              <Outlet />
            </ThemedLayout>
          }
        >
          <Route index element={<NavigateToResource resource="branch-stock" />} />
          <Route path="/my/stock" element={<BranchStockPage />} />
          <Route path="/my/consumption" element={<BranchConsumptionPage />} />
          <Route path="/my/request" element={<BranchRequestPage />} />
          <Route path="/my/movements" element={<BranchMovementsPage />} />
          <Route path="*" element={<ErrorComponent />} />
        </Route>
      </Routes>

      <RefineKbar />
      <UnsavedChangesNotifier />
      <DocumentTitleHandler />
    </Refine>
  );
};
