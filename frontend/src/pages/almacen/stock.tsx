import { useQuery } from "@tanstack/react-query";
import {
  Alert,
  Button,
  Grid,
  Spin,
  Table,
  Tooltip,
  Typography,
} from "antd";
import { WarningOutlined, ShoppingCartOutlined, SwapOutlined } from "@ant-design/icons";
import { useNavigate } from "react-router";
import { apiUrl, fetchJson } from "../../providers/data";
import type { ItemStock, StockLocationEntry } from "../../types/inventory";

const { useBreakpoint } = Grid;

export const AlmacenStockPage = () => {
  const screens = useBreakpoint();
  const isMobile = !screens.md;
  const navigate = useNavigate();

  const { data: stockList = [], isLoading, isError } = useQuery<ItemStock[]>({
    queryKey: ["inventory-stock"],
    queryFn: () => fetchJson(`${apiUrl}/inventory/stock`),
  });

  // Solo ubicaciones de tipo warehouse
  const warehouseStock = stockList
    .map((item) => {
      const entries = item.locations.filter((l) => l.locationId && l.quantity > 0);
      return entries.length > 0 ? { item, entries } : null;
    })
    .filter((x): x is { item: ItemStock; entries: StockLocationEntry[] } => x !== null);

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
      render: (_: unknown, r: { item: ItemStock; entries: StockLocationEntry[] }) =>
        r.item.itemName,
    },
    {
      title: "Total disponible",
      key: "quantity",
      render: (_: unknown, r: { item: ItemStock; entries: StockLocationEntry[] }) => {
        const total = r.entries.reduce((sum, e) => sum + Number(e.quantity), 0);
        const hasLowStock = r.entries.some((e) => e.lowStock);
        return (
          <span>
            {hasLowStock && (
              <Tooltip title="Stock bajo en alguna bodega">
                <WarningOutlined style={{ color: "#faad14", marginRight: 6 }} />
              </Tooltip>
            )}
            {total} {r.item.baseUnitSymbol}
          </span>
        );
      },
    },
    {
      title: "Acciones",
      key: "actions",
      render: (_: unknown, r: { item: ItemStock; entries: StockLocationEntry[] }) => (
        <div style={{ display: "flex", gap: 8 }}>
          <Button
            size="small"
            icon={<SwapOutlined />}
            onClick={() => navigate(`/almacen/transfer?itemId=${r.item.itemId}`)}
          >
            {isMobile ? "" : "Transferir"}
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div style={{ padding: isMobile ? 16 : 24 }}>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: 16,
        }}
      >
        <Typography.Title level={isMobile ? 4 : 3} style={{ margin: 0 }}>
          Stock Bodega
        </Typography.Title>
        <Button
          type="primary"
          icon={<ShoppingCartOutlined />}
          onClick={() => navigate("/almacen/purchase")}
        >
          {isMobile ? "Compra" : "Registrar Compra"}
        </Button>
      </div>

      <Table
        dataSource={warehouseStock}
        columns={columns}
        rowKey={(r) => r.item.itemId}
        size={isMobile ? "small" : "middle"}
        pagination={{ pageSize: 20 }}
        locale={{ emptyText: "No hay stock registrado en bodega" }}
        rowClassName={(r) => (r.entries.some((e) => e.lowStock) ? "ant-table-row-danger" : "")}
      />
    </div>
  );
};
