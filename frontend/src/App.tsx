import { Refine } from "@refinedev/core";
import { RefineKbar, RefineKbarProvider } from "@refinedev/kbar";

import {
  ErrorComponent,
  ThemedLayout,
  ThemedSider,
  useNotificationProvider,
} from "@refinedev/antd";
import "@refinedev/antd/dist/reset.css";

import routerProvider, {
  DocumentTitleHandler,
  NavigateToResource,
  UnsavedChangesNotifier,
} from "@refinedev/react-router";
import { App as AntdApp } from "antd";
import { BrowserRouter, Outlet, Route, Routes } from "react-router";
import { Header } from "./components/header";
import { ColorModeContextProvider } from "./contexts/color-mode";
import { SupplierCreate, SupplierList } from "./pages/suppliers";
import {
  OperationList,
  OperationShow,
  OperationCreate,
  OperationEdit,
} from "./pages/operations";
import {
  ShipmentList,
  ShipmentCreate,
  ShipmentEdit,
  ShipmentShow,
} from "./pages/shipments";
import { ShipmentItemList, ShipmentItemShow } from "./pages/shipment-items";
import { AccountBalance } from "./pages/account";
import { AuditList } from "./pages/audit";
import { dataProvider } from "./providers/data";
import "./i18n";
import { i18nProvider } from "./i18n/provider";

function App() {
  return (
    <BrowserRouter>
      <RefineKbarProvider>
        <ColorModeContextProvider>
          <AntdApp>
            <Refine
              dataProvider={dataProvider}
              i18nProvider={i18nProvider}
              notificationProvider={useNotificationProvider}
              routerProvider={routerProvider}
              resources={[
                {
                  name: "operations",
                  list: "/operations",
                  show: "/operations/show/:id",
                  create: "/operations/create",
                  edit: "/operations/edit/:id",
                  meta: { label: "Operaciones" },
                },
                {
                  name: "suppliers",
                  list: "/suppliers",
                  create: "/suppliers/create",
                  meta: { label: "Proveedores" },
                },
                {
                  name: "shipments",
                  list: "/shipments",
                  show: "/shipments/show/:id",
                  create: "/shipments/create",
                  edit: "/shipments/edit/:id",
                  meta: { label: "Rastreo" },
                },
                {
                  name: "shipment-items",
                  list: "/shipment-items",
                  show: "/shipment-items/show/:id",
                  meta: { label: "Contenidos" },
                },
                {
                  name: "account",
                  list: "/account",
                  meta: { label: "Cuenta" },
                },
                // {
                //   name: "audit-log",
                //   list: "/audit-log",
                //   meta: { label: "Audit Log" },
                // },
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
                      Sider={(props) => <ThemedSider {...props} fixed />}
                    >
                      <Outlet />
                    </ThemedLayout>
                  }
                >
                  <Route index element={<NavigateToResource resource="operations" />} />
                  <Route path="/operations">
                    <Route index element={<OperationList />} />
                    <Route path="show/:id" element={<OperationShow />} />
                    <Route path="create" element={<OperationCreate />} />
                    <Route path="edit/:id" element={<OperationEdit />} />
                  </Route>
                  <Route path="/suppliers">
                    <Route index element={<SupplierList />} />
                    <Route path="create" element={<SupplierCreate />} />
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
                  <Route path="/account" element={<AccountBalance />} />
                  <Route path="/audit-log" element={<AuditList />} />
                  <Route path="*" element={<ErrorComponent />} />
                </Route>
              </Routes>

              <RefineKbar />
              <UnsavedChangesNotifier />
              <DocumentTitleHandler />
            </Refine>
          </AntdApp>
        </ColorModeContextProvider>
      </RefineKbarProvider>
    </BrowserRouter>
  );
}

export default App;
