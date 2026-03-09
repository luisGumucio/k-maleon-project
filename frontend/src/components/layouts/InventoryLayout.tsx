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
  DashboardOutlined,
  AppstoreOutlined,
  HistoryOutlined,
  InboxOutlined,
  FormatPainterOutlined,
  ShoppingOutlined,
  EnvironmentOutlined,
  SwapOutlined,
  TagsOutlined,
  BankOutlined,
  PullRequestOutlined
} from "@ant-design/icons";
import { Outlet, Route, Routes } from "react-router";
import { Header } from "../header";
import { dataProvider } from "../../providers/data";
import { i18nProvider } from "../../i18n/provider";
import { useRole } from "../../contexts/role";
import { InventoryDashboard } from "../../pages/inventory/dashboard";
import { StockList } from "../../pages/stock/list";
import { UnitList } from "../../pages/units/list";
import { LocationList } from "../../pages/locations/list";
import { ItemList } from "../../pages/items/list";
import { PurchasePage } from "../../pages/inventory/purchase";
import { TransferPage } from "../../pages/inventory/transfer";
import { AdjustmentPage } from "../../pages/inventory/adjustment";
import { MovementsPage } from "../../pages/inventory/movements";
import { RequestsPage } from "../../pages/inventory/requests";

interface InventoryLayoutProps {
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

export const InventoryLayout: React.FC<InventoryLayoutProps> = ({
  fromSuperAdmin = false,
}) => {
  return (
    <Refine
      dataProvider={dataProvider}
      i18nProvider={i18nProvider}
      notificationProvider={useNotificationProvider}
      routerProvider={routerProvider}
      resources={[
        {
          name: "inventory-dashboard",
          list: "/inventory/dashboard",
          meta: { label: "Dashboard", icon: <DashboardOutlined /> },
        },
        {
          name: "stock",
          list: "/stock",
          meta: { label: "Stock", icon: <AppstoreOutlined /> },
        },
        {
          name: "inventory-purchase",
          list: "/inventory/purchase",
          meta: { label: "Compra", hide: true, icon: <ShoppingOutlined /> },
        },
        {
          name: "inventory-transfer",
          list: "/inventory/transfer",
          meta: { label: "Transferencia", hide: true, icon: <SwapOutlined /> },
        },
        {
          name: "inventory-adjustment",
          list: "/inventory/adjustment",
          meta: { label: "Ajuste", hide: true, icon: <FormatPainterOutlined /> },
        },
        {
          name: "inventory-movements",
          list: "/inventory/movements",
          meta: { label: "Historial", icon: <HistoryOutlined /> },
        },
        {
          name: "inventory-requests",
          list: "/inventory/requests",
          meta: { label: "Solicitudes", icon: <PullRequestOutlined /> },
        },
        {
          name: "items",
          list: "/items",
          meta: { label: "Productos", icon: <TagsOutlined /> },
        },
        {
          name: "units",
          list: "/units",
          meta: { label: "Unidades", icon: <InboxOutlined /> },
        },
        {
          name: "locations",
          list: "/locations",
          meta: { label: "Ubicaciones", icon: <EnvironmentOutlined /> },
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
          <Route
            index
            element={<NavigateToResource resource="inventory-dashboard" />}
          />
          <Route path="/inventory/dashboard" element={<InventoryDashboard />} />
          <Route path="/stock" element={<StockList />} />
          <Route path="/inventory/purchase" element={<PurchasePage />} />
          <Route path="/inventory/transfer" element={<TransferPage />} />
          <Route path="/inventory/adjustment" element={<AdjustmentPage />} />
          <Route path="/inventory/movements" element={<MovementsPage />} />
          <Route path="/inventory/requests" element={<RequestsPage />} />
          <Route path="/items" element={<ItemList />} />
          <Route path="/units" element={<UnitList />} />
          <Route path="/locations" element={<LocationList />} />
          <Route path="*" element={<ErrorComponent />} />
        </Route>
      </Routes>

      <RefineKbar />
      <UnsavedChangesNotifier />
      <DocumentTitleHandler />
    </Refine>
  );
};
