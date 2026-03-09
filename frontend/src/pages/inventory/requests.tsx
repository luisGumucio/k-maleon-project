import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  Alert,
  Button,
  Card,
  Form,
  Grid,
  Input,
  InputNumber,
  Modal,
  Popconfirm,
  Select,
  Space,
  Spin,
  Table,
  Tag,
  Typography,
  message,
} from "antd";
import { CheckOutlined, CloseOutlined, PlusOutlined } from "@ant-design/icons";
import { apiUrl, fetchJson } from "../../providers/data";
import type { Item, Location, UnitConversion, TransferRequestItem } from "../../types/inventory";

const { useBreakpoint } = Grid;

const STATUS_LABELS: Record<string, { label: string; color: string }> = {
  pending: { label: "Pendiente", color: "orange" },
  completed: { label: "Completada", color: "green" },
  rejected: { label: "Rechazada", color: "red" },
};

// --- Create Request Modal ---

const CreateRequestModal = ({
  open,
  onClose,
}: {
  open: boolean;
  onClose: () => void;
}) => {
  const queryClient = useQueryClient();
  const [form] = Form.useForm();

  const { data: items = [] } = useQuery<Item[]>({
    queryKey: ["items"],
    queryFn: () => fetchJson(`${apiUrl}/items`),
  });

  const { data: locations = [] } = useQuery<Location[]>({
    queryKey: ["locations"],
    queryFn: () => fetchJson(`${apiUrl}/locations`),
  });

  const selectedItemId = Form.useWatch("itemId", form);

  const { data: conversions = [] } = useQuery<UnitConversion[]>({
    queryKey: ["conversions", selectedItemId],
    queryFn: () => fetchJson(`${apiUrl}/items/${selectedItemId}/conversions`),
    enabled: !!selectedItemId,
  });

  const selectedItem = items.find((i) => i.id === selectedItemId);

  const unitOptions = selectedItem
    ? [
        {
          label: `${selectedItem.baseUnitName} (${selectedItem.baseUnitSymbol})`,
          value: selectedItem.baseUnitId,
        },
        ...conversions.map((c) => ({
          label: `${c.fromUnitName} (${c.fromUnitSymbol})`,
          value: c.fromUnitId,
        })),
      ]
    : [];

  // Only branches can request transfers
  const branchLocations = locations.filter((l) => l.type === "branch" && l.active);

  const { mutate: create, isPending } = useMutation({
    mutationFn: (values: {
      itemId: string;
      unitId: string;
      quantity: number;
      locationId: string;
      notes?: string;
    }) =>
      fetchJson(`${apiUrl}/inventory/transfer-requests`, {
        method: "POST",
        body: JSON.stringify(values),
      }),
    onSuccess: () => {
      message.success("Solicitud creada correctamente");
      queryClient.invalidateQueries({ queryKey: ["transfer-requests"] });
      form.resetFields();
      onClose();
    },
    onError: () => message.error("Error al crear la solicitud"),
  });

  return (
    <Modal
      title="Nueva solicitud de transferencia"
      open={open}
      onCancel={() => { form.resetFields(); onClose(); }}
      onOk={() => form.submit()}
      okText="Enviar solicitud"
      cancelText="Cancelar"
      confirmLoading={isPending}
      destroyOnClose
      width={480}
    >
      <Form form={form} layout="vertical" onFinish={create}>
        <Form.Item
          label="Producto"
          name="itemId"
          rules={[{ required: true, message: "Requerido" }]}
        >
          <Select
            showSearch
            optionFilterProp="label"
            placeholder="Selecciona un producto"
            options={items.filter((i) => i.active).map((i) => ({ label: i.name, value: i.id }))}
            onChange={() => form.setFieldValue("unitId", undefined)}
          />
        </Form.Item>

        <Form.Item
          label="Unidad"
          name="unitId"
          rules={[{ required: true, message: "Requerido" }]}
        >
          <Select
            placeholder={selectedItemId ? "Selecciona la unidad" : "Primero selecciona un producto"}
            options={unitOptions}
            disabled={!selectedItemId}
          />
        </Form.Item>

        <Form.Item
          label="Cantidad solicitada"
          name="quantity"
          rules={[{ required: true, message: "Requerido" }]}
        >
          <InputNumber style={{ width: "100%" }} min={0.000001} precision={6} placeholder="ej: 20" />
        </Form.Item>

        <Form.Item
          label="Sucursal solicitante"
          name="locationId"
          rules={[{ required: true, message: "Requerido" }]}
        >
          <Select
            placeholder="Selecciona la sucursal"
            options={branchLocations.map((l) => ({ label: l.name, value: l.id }))}
          />
        </Form.Item>

        <Form.Item label="Notas (opcional)" name="notes">
          <Input.TextArea rows={2} placeholder="Ej: Urgente para fin de semana" />
        </Form.Item>
      </Form>
    </Modal>
  );
};

// --- Main component ---

export const RequestsPage = () => {
  const screens = useBreakpoint();
  const isMobile = !screens.md;
  const queryClient = useQueryClient();
  const [createOpen, setCreateOpen] = useState(false);
  const [filterStatus, setFilterStatus] = useState<string | undefined>();

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
      render: (_: unknown, r: TransferRequestItem) =>
        `${r.quantity} ${r.unitSymbol}`,
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
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => setCreateOpen(true)}
        >
          {isMobile ? "Nueva" : "Nueva solicitud"}
        </Button>
      </div>

      <div style={{ marginBottom: 16 }}>
        <Select
          allowClear
          placeholder="Filtrar por estado"
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

      <CreateRequestModal open={createOpen} onClose={() => setCreateOpen(false)} />
    </div>
  );
};
