import { useQuery } from "@tanstack/react-query";
import {
  Alert,
  Grid,
  Select,
  Spin,
  Table,
  Tag,
  Typography,
} from "antd";
import { apiUrl, fetchJson } from "../../providers/data";
import type { InventoryMovement } from "../../types/inventory";
import { useState } from "react";

// In a real app this would come from the auth token.
const BRANCH_LOCATION_ID = import.meta.env.VITE_MOCK_BRANCH_LOCATION_ID as string | undefined;

const { useBreakpoint } = Grid;

const MOVEMENT_LABELS: Record<string, { label: string; color: string }> = {
  purchase: { label: "Compra", color: "green" },
  transfer: { label: "Transferencia", color: "blue" },
  adjustment: { label: "Ajuste", color: "orange" },
  consumption: { label: "Consumo", color: "red" },
};

export const BranchMovementsPage = () => {
  const screens = useBreakpoint();
  const isMobile = !screens.md;
  const [filterType, setFilterType] = useState<string | undefined>();

  const params = new URLSearchParams();
  if (BRANCH_LOCATION_ID) params.set("locationId", BRANCH_LOCATION_ID);
  if (filterType) params.set("movementType", filterType);
  const qs = params.toString();

  const { data: movements = [], isLoading, isError } = useQuery<InventoryMovement[]>({
    queryKey: ["branch-movements", BRANCH_LOCATION_ID, filterType],
    queryFn: () => fetchJson(`${apiUrl}/inventory/movements${qs ? `?${qs}` : ""}`),
  });

  const columns = [
    {
      title: "Fecha",
      dataIndex: "createdAt",
      key: "createdAt",
      render: (v: string) =>
        new Date(v).toLocaleString("es-CL", { dateStyle: "short", timeStyle: "short" }),
    },
    {
      title: "Tipo",
      dataIndex: "movementType",
      key: "movementType",
      render: (t: string) => {
        const m = MOVEMENT_LABELS[t] ?? { label: t, color: "default" };
        return <Tag color={m.color}>{m.label}</Tag>;
      },
    },
    { title: "Producto", dataIndex: "itemName", key: "itemName" },
    {
      title: "Cantidad",
      key: "qty",
      render: (_: unknown, r: InventoryMovement) => `${r.quantity} ${r.unitSymbol}`,
    },
    {
      title: "Cant. base",
      key: "qtyBase",
      render: (_: unknown, r: InventoryMovement) => `${r.quantityBase} ${r.baseUnitSymbol}`,
    },
    {
      title: "Notas",
      dataIndex: "notes",
      key: "notes",
      render: (v: string | null) => v ?? <span style={{ color: "#bfbfbf" }}>—</span>,
    },
  ];

  const mobileColumns = columns.filter((c) =>
    ["createdAt", "movementType", "itemName", "qty"].includes(c.key as string)
  );

  if (isError)
    return (
      <Alert
        type="error"
        message="No se pudo cargar el historial de movimientos."
        style={{ margin: 24 }}
      />
    );

  return (
    <div style={{ padding: isMobile ? 16 : 24 }}>
      <Typography.Title level={isMobile ? 4 : 3} style={{ marginBottom: 16 }}>
        Historial de Movimientos
      </Typography.Title>

      <div style={{ marginBottom: 16 }}>
        <Select
          allowClear
          placeholder="Filtrar por tipo"
          style={{ width: isMobile ? "100%" : 180 }}
          options={Object.entries(MOVEMENT_LABELS).map(([k, v]) => ({
            label: v.label,
            value: k,
          }))}
          onChange={setFilterType}
        />
      </div>

      {isLoading ? (
        <Spin size="large" style={{ display: "block", marginTop: 60 }} />
      ) : (
        <Table
          dataSource={movements}
          columns={isMobile ? mobileColumns : columns}
          rowKey="id"
          size={isMobile ? "small" : "middle"}
          pagination={{ pageSize: 30 }}
          locale={{ emptyText: "No hay movimientos registrados" }}
        />
      )}
    </div>
  );
};
