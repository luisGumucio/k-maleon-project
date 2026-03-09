import { useQuery } from "@tanstack/react-query";
import {
  Alert,
  Grid,
  Spin,
  Table,
  Tooltip,
  Typography,
} from "antd";
import { WarningOutlined } from "@ant-design/icons";
import { apiUrl, fetchJson } from "../../providers/data";
import type { ItemStock, StockLocationEntry } from "../../types/inventory";

// In a real app this would come from the auth token.
// For now it's read from the env var VITE_MOCK_BRANCH_LOCATION_ID.
const BRANCH_LOCATION_ID = import.meta.env.VITE_MOCK_BRANCH_LOCATION_ID as string | undefined;

const { useBreakpoint } = Grid;

export const BranchStockPage = () => {
  const screens = useBreakpoint();
  const isMobile = !screens.md;

  const { data: stockList = [], isLoading, isError } = useQuery<ItemStock[]>({
    queryKey: ["inventory-stock"],
    queryFn: () => fetchJson(`${apiUrl}/inventory/stock`),
  });

  // Filter to only this branch's location entry
  const branchStock = stockList
    .map((item) => {
      const entry = BRANCH_LOCATION_ID
        ? item.locations.find((l) => l.locationId === BRANCH_LOCATION_ID)
        : item.locations[0]; // fallback: first location
      return entry ? { item, entry } : null;
    })
    .filter((x): x is { item: ItemStock; entry: StockLocationEntry } => x !== null);

  if (isLoading) return <Spin size="large" style={{ display: "block", marginTop: 80 }} />;

  if (isError)
    return (
      <Alert
        type="error"
        message="No se pudo cargar el stock. Verifica que el backend esté activo."
        style={{ margin: 24 }}
      />
    );

  const columns = [
    {
      title: "Producto",
      key: "itemName",
      render: (_: unknown, r: { item: ItemStock; entry: StockLocationEntry }) =>
        r.item.itemName,
    },
    {
      title: "Disponible",
      key: "quantity",
      render: (_: unknown, r: { item: ItemStock; entry: StockLocationEntry }) => (
        <span>
          {r.entry.lowStock && (
            <Tooltip title="Stock bajo">
              <WarningOutlined style={{ color: "#faad14", marginRight: 6 }} />
            </Tooltip>
          )}
          {r.entry.quantity} {r.item.baseUnitSymbol}
        </span>
      ),
    },
    {
      title: "Mínimo",
      key: "minQuantity",
      render: (_: unknown, r: { item: ItemStock; entry: StockLocationEntry }) =>
        `${r.entry.minQuantity} ${r.item.baseUnitSymbol}`,
    },
  ];

  if (isMobile) {
    return (
      <div style={{ padding: 16 }}>
        <Typography.Title level={4} style={{ marginBottom: 16 }}>
          Mi Stock
        </Typography.Title>
        {branchStock.length === 0 ? (
          <Typography.Text type="secondary">No hay stock registrado en esta sucursal</Typography.Text>
        ) : (
          branchStock.map(({ item, entry }) => (
            <div
              key={item.itemId}
              style={{
                border: "1px solid #f0f0f0",
                borderRadius: 8,
                padding: 12,
                marginBottom: 8,
              }}
            >
              <div style={{ display: "flex", justifyContent: "space-between" }}>
                <Typography.Text strong>{item.itemName}</Typography.Text>
                {entry.lowStock && (
                  <Tooltip title="Stock bajo">
                    <WarningOutlined style={{ color: "#faad14" }} />
                  </Tooltip>
                )}
              </div>
              <div style={{ display: "flex", justifyContent: "space-between", marginTop: 6 }}>
                <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                  Disponible
                </Typography.Text>
                <Typography.Text strong style={{ fontSize: 12 }}>
                  {entry.quantity} {item.baseUnitSymbol}
                </Typography.Text>
              </div>
              <div style={{ display: "flex", justifyContent: "space-between" }}>
                <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                  Mínimo
                </Typography.Text>
                <Typography.Text style={{ fontSize: 12 }}>
                  {entry.minQuantity} {item.baseUnitSymbol}
                </Typography.Text>
              </div>
            </div>
          ))
        )}
      </div>
    );
  }

  return (
    <div style={{ padding: 24 }}>
      <Typography.Title level={3} style={{ marginBottom: 16 }}>
        Mi Stock
      </Typography.Title>
      <Table
        dataSource={branchStock}
        columns={columns}
        rowKey={(r) => r.item.itemId}
        size="middle"
        pagination={{ pageSize: 20 }}
        locale={{ emptyText: "No hay stock registrado en esta sucursal" }}
        rowClassName={(r) => (r.entry.lowStock ? "ant-table-row-danger" : "")}
      />
    </div>
  );
};
