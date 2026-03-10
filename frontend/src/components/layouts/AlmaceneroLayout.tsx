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
  HistoryOutlined,
} from "@ant-design/icons";
import { Outlet, Route, Routes } from "react-router";
import { Header } from "../header";
import { dataProvider } from "../../providers/data";
import { authProvider } from "../../providers/auth";
import { i18nProvider } from "../../i18n/provider";
import { useRole } from "../../contexts/role";
import { AlmacenStockPage } from "../../pages/almacen/stock";
import { AlmacenPurchasePage } from "../../pages/almacen/purchase";
import { AlmacenTransferPage } from "../../pages/almacen/transfer";
import { AlmacenRequestsPage } from "../../pages/almacen/requests";
import { AlmacenMovementsPage } from "../../pages/almacen/movements";

interface AlmaceneroLayoutProps {
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

export const AlmaceneroLayout: React.FC<AlmaceneroLayoutProps> = ({
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
          name: "almacen-stock",
          list: "/almacen/stock",
          meta: { label: "Bodega", icon: <AppstoreOutlined /> },
        },
        {
          name: "almacen-requests",
          list: "/almacen/requests",
          meta: { label: "Solicitudes", icon: <CheckSquareOutlined /> },
        },
        {
          name: "almacen-movements",
          list: "/almacen/movements",
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
          <Route index element={<NavigateToResource resource="almacen-stock" />} />
          <Route path="/almacen/stock" element={<AlmacenStockPage />} />
          <Route path="/almacen/purchase" element={<AlmacenPurchasePage />} />
          <Route path="/almacen/transfer" element={<AlmacenTransferPage />} />
          <Route path="/almacen/requests" element={<AlmacenRequestsPage />} />
          <Route path="/almacen/movements" element={<AlmacenMovementsPage />} />
          <Route path="*" element={<ErrorComponent />} />
        </Route>
      </Routes>

      <RefineKbar />
      <UnsavedChangesNotifier />
      <DocumentTitleHandler />
    </Refine>
  );
};
