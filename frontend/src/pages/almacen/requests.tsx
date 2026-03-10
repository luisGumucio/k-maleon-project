// Almacenero requests page — gestiona solicitudes pendientes de sucursales.
// Puede completar o rechazar. No puede crear (las crean los encargados).
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  Alert,
  Button,
  Grid,
  Popconfirm,
  Select,
  Space,
  Spin,
  Table,
  Tag,
  Typography,
  message,
} from "antd";
import { CheckOutlined, CloseOutlined } from "@ant-design/icons";
import { apiUrl, fetchJson } from "../../providers/data";
import type { TransferRequestItem } from "../../types/inventory";
import { useState } from "react";

const { useBreakpoint } = Grid;

const STATUS_LABELS: Record<string, { label: string; color: string }> = {
  pending: { label: "Pendiente", color: "orange" },
  completed: { label: "Completada", color: "green" },
  rejected: { label: "Rechazada", color: "red" },
};

export const AlmacenRequestsPage = () => {
  const screens = useBreakpoint();
  const isMobile = !screens.md;
  const queryClient = useQueryClient();
  const [filterStatus, setFilterStatus] = useState<string | undefined>("pending");

  const params = new URLSearchParams();
  if (filterStatus) params.set("status", filterStatus);
  const qs = params.toString();

  const { data: requests = [], isLoading, isError } = useQuery<TransferRequestItem[]>({
    queryKey: ["transfer-requests", filterStatus],
    queryFn: () =>
      fetchJson(`${apiUrl}/inventory/transfer-requests${qs ? `?${qs}` : ""}`),
  });

  const { mutate: complete } = useMutation({
    mutationFn: (id: string) =>
      fetchJson(`${apiUrl}/inventory/transfer-requests/${id}/complete`, { method: "POST" }),
    onSuccess: () => {
      message.success("Solicitud completada");
      queryClient.invalidateQueries({ queryKey: ["transfer-requests"] });
      queryClient.invalidateQueries({ queryKey: ["inventory-stock"] });
    },
    onError: (err: Error) =>
      message.error(
        err.message.includes("insufficient")
          ? "Stock insuficiente para completar la solicitud"
          : "Error al completar la solicitud"
      ),
  });

  const { mutate: reject } = useMutation({
    mutationFn: (id: string) =>
      fetchJson(`${apiUrl}/inventory/transfer-requests/${id}/reject`, { method: "POST" }),
    onSuccess: () => {
      message.success("Solicitud rechazada");
      queryClient.invalidateQueries({ queryKey: ["transfer-requests"] });
    },
    onError: () => message.error("Error al rechazar la solicitud"),
  });

  const columns = [
    {
      title: "Fecha",
      dataIndex: "createdAt",
      key: "createdAt",
      render: (v: string) =>
        new Date(v).toLocaleString("es-CL", { dateStyle: "short", timeStyle: "short" }),
    },
    { title: "Producto", dataIndex: "itemName", key: "itemName" },
    {
      title: "Cantidad",
      key: "qty",
      render: (_: unknown, r: TransferRequestItem) => `${r.quantity} ${r.unitSymbol}`,
    },
    { title: "Sucursal", dataIndex: "locationName", key: "locationName" },
    {
      title: "Estado",
      dataIndex: "status",
      key: "status",
      render: (s: string) => {
        const m = STATUS_LABELS[s] ?? { label: s, color: "default" };
        return <Tag color={m.color}>{m.label}</Tag>;
      },
    },
    {
      title: "Notas",
      dataIndex: "notes",
      key: "notes",
      render: (v: string | null) => v ?? <span style={{ color: "#bfbfbf" }}>—</span>,
    },
    {
      title: "Acciones",
      key: "actions",
      render: (_: unknown, r: TransferRequestItem) => {
        if (r.status !== "pending") return null;
        return (
          <Space>
            <Popconfirm
              title="¿Completar esta solicitud?"
              description="Se realizará la transferencia de stock automáticamente."
              onConfirm={() => complete(r.id)}
              okText="Sí, completar"
              cancelText="No"
            >
              <Button icon={<CheckOutlined />} size="small" type="primary">
                {isMobile ? "" : "Completar"}
              </Button>
            </Popconfirm>
            <Popconfirm
              title="¿Rechazar esta solicitud?"
              onConfirm={() => reject(r.id)}
              okText="Sí, rechazar"
              cancelText="No"
            >
              <Button icon={<CloseOutlined />} size="small" danger>
                {isMobile ? "" : "Rechazar"}
              </Button>
            </Popconfirm>
          </Space>
        );
      },
    },
  ];

  if (isError)
    return (
      <Alert
        type="error"
        message="No se pudo cargar las solicitudes."
        style={{ margin: 24 }}
      />
    );

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
          Solicitudes de Transferencia
        </Typography.Title>
      </div>

      <div style={{ marginBottom: 16 }}>
        <Select
          allowClear
          placeholder="Filtrar por estado"
          value={filterStatus}
          style={{ width: isMobile ? "100%" : 180 }}
          options={Object.entries(STATUS_LABELS).map(([k, v]) => ({
            label: v.label,
            value: k,
          }))}
          onChange={setFilterStatus}
        />
      </div>

      {isLoading ? (
        <Spin size="large" style={{ display: "block", marginTop: 60 }} />
      ) : (
        <Table
          dataSource={requests}
          columns={columns}
          rowKey="id"
          size={isMobile ? "small" : "middle"}
          scroll={isMobile ? undefined : { x: "max-content" }}
          pagination={{ pageSize: 20 }}
          locale={{ emptyText: "No hay solicitudes registradas" }}
        />
      )}
    </div>
  );
};
