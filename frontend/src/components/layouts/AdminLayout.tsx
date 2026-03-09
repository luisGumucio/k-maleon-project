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
import { ArrowLeftOutlined } from "@ant-design/icons";
import { Outlet, Route, Routes } from "react-router";
import { Header } from "../header";
import { dataProvider } from "../../providers/data";
import { i18nProvider } from "../../i18n/provider";
import { useRole } from "../../contexts/role";
import { AdminDashboard } from "../../pages/admin/dashboard";
import { SupplierCreate, SupplierEdit, SupplierList } from "../../pages/suppliers";
import {
  OperationList,
  OperationShow,
  OperationCreate,
  OperationEdit,
} from "../../pages/operations";
import {
  ShipmentList,
  ShipmentCreate,
  ShipmentEdit,
  ShipmentShow,
} from "../../pages/shipments";
import { ShipmentItemList, ShipmentItemShow } from "../../pages/shipment-items";
import { AccountBalance } from "../../pages/account";
import { AuditList } from "../../pages/audit";

interface AdminLayoutProps {
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

export const AdminLayout: React.FC<AdminLayoutProps> = ({
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
          name: "admin-dashboard",
          list: "/admin/dashboard",
          meta: { label: "Dashboard" },
        },
        {
          name: "account",
          list: "/account",
          meta: { label: "Cuenta" },
        },
        {
          name: "suppliers",
          list: "/suppliers",
          create: "/suppliers/create",
          edit: "/suppliers/edit/:id",
          meta: { label: "Proveedores" },
        },
        {
          name: "operations",
          list: "/operations",
          show: "/operations/show/:id",
          create: "/operations/create",
          edit: "/operations/edit/:id",
          meta: { label: "Operaciones" },
        },
        {
          name: "shipments",
          list: "/shipments",
          show: "/shipments/show/:id",
          create: "/shipments/create",
          edit: "/shipments/edit/:id",
          meta: { label: "Rastreos" },
        },
        {
          name: "shipment-items",
          list: "/shipment-items",
          show: "/shipment-items/show/:id",
          meta: { label: "Contenidos" },
        },
        {
          name: "audit-log",
          list: "/audit-log",
          meta: { label: "Audit Log" },
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
          <Route index element={<NavigateToResource resource="admin-dashboard" />} />
          <Route path="/admin/dashboard" element={<AdminDashboard />} />
          <Route path="/account" element={<AccountBalance />} />
          <Route path="/suppliers">
            <Route index element={<SupplierList />} />
            <Route path="create" element={<SupplierCreate />} />
            <Route path="edit/:id" element={<SupplierEdit />} />
          </Route>
          <Route path="/operations">
            <Route index element={<OperationList />} />
            <Route path="show/:id" element={<OperationShow />} />
            <Route path="create" element={<OperationCreate />} />
            <Route path="edit/:id" element={<OperationEdit />} />
          </Route>
          <Route path="/shipments">
            <Route index element={<ShipmentList />} />
            <Route path="show/:id" element={<ShipmentShow />} />
            <Route path="create" element={<ShipmentCreate />} />
            <Route path="edit/:id" element={<ShipmentEdit />} />
          </Route>
          <Route path="/shipment-items">
            <Route index element={<ShipmentItemList />} />
            <Route path="show/:id" element={<ShipmentItemShow />} />
          </Route>
          <Route path="/audit-log" element={<AuditList />} />
          <Route path="*" element={<ErrorComponent />} />
        </Route>
      </Routes>

      <RefineKbar />
      <UnsavedChangesNotifier />
      <DocumentTitleHandler />
    </Refine>
  );
};
