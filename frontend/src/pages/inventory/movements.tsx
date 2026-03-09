import { useState } from "react";
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
import type { InventoryMovement, Item, Location } from "../../types/inventory";

const { useBreakpoint } = Grid;

const MOVEMENT_LABELS: Record<string, { label: string; color: string }> = {
  purchase: { label: "Compra", color: "green" },
  transfer: { label: "Transferencia", color: "blue" },
  adjustment: { label: "Ajuste", color: "orange" },
  consumption: { label: "Consumo", color: "red" },
};

export const MovementsPage = () => {
  const screens = useBreakpoint();
  const isMobile = !screens.md;

  const [filterItemId, setFilterItemId] = useState<string | undefined>();
  const [filterType, setFilterType] = useState<string | undefined>();

  const { data: items = [] } = useQuery<Item[]>({
    queryKey: ["items"],
    queryFn: () => fetchJson(`${apiUrl}/items`),
  });

  const { data: locations = [] } = useQuery<Location[]>({
    queryKey: ["locations"],
    queryFn: () => fetchJson(`${apiUrl}/locations`),
  });

  const params = new URLSearchParams();
  if (filterItemId) params.set("itemId", filterItemId);
  if (filterType) params.set("movementType", filterType);
  const qs = params.toString();

  const { data: movements = [], isLoading, isError } = useQuery<InventoryMovement[]>({
    queryKey: ["inventory-movements", filterItemId, filterType],
    queryFn: () => fetchJson(`${apiUrl}/inventory/movements${qs ? `?${qs}` : ""}`),
  });

  const locationName = (id: string | null) =>
    id ? (locations.find((l) => l.id === id)?.name ?? id) : "—";

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
      render: (_: unknown, r: InventoryMovement) =>
        `${r.quantity} ${r.unitSymbol}`,
    },
    {
      title: "Cant. base",
      key: "qtyBase",
      render: (_: unknown, r: InventoryMovement) =>
        `${r.quantityBase} ${r.baseUnitSymbol}`,
    },
    {
      title: "Origen",
      key: "from",
      render: (_: unknown, r: InventoryMovement) => locationName(r.locationFromId),
    },
    {
      title: "Destino",
      key: "to",
      render: (_: unknown, r: InventoryMovement) => locationName(r.locationToId),
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

      <div
        style={{
          display: "flex",
          gap: 12,
          marginBottom: 16,
          flexWrap: "wrap",
        }}
      >
        <Select
          allowClear
          placeholder="Filtrar por producto"
          style={{ width: isMobile ? "100%" : 220 }}
          options={items.map((i) => ({ label: i.name, value: i.id }))}
          onChange={setFilterItemId}
          showSearch
          optionFilterProp="label"
        />
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
          scroll={isMobile ? undefined : { x: "max-content" }}
          pagination={{ pageSize: 30 }}
          locale={{ emptyText: "No hay movimientos registrados" }}
        />
      )}
    </div>
  );
};
