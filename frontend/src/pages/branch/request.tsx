import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  Alert,
  Button,
  Card,
  Form,
  Grid,
  Input,
  InputNumber,
  Select,
  Spin,
  Table,
  Tag,
  Typography,
  message,
} from "antd";
import { useNavigate } from "react-router";
import { apiUrl, fetchJson } from "../../providers/data";
import type { Item, UnitConversion, TransferRequestItem } from "../../types/inventory";

// In a real app this would come from the auth token.
const BRANCH_LOCATION_ID = import.meta.env.VITE_MOCK_BRANCH_LOCATION_ID as string | undefined;

const { useBreakpoint } = Grid;

const STATUS_LABELS: Record<string, { label: string; color: string }> = {
  pending: { label: "Pendiente", color: "orange" },
  completed: { label: "Completada", color: "green" },
  rejected: { label: "Rechazada", color: "red" },
};

export const BranchRequestPage = () => {
  const screens = useBreakpoint();
  const isMobile = !screens.md;
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [form] = Form.useForm();

  const { data: items = [] } = useQuery<Item[]>({
    queryKey: ["items"],
    queryFn: () => fetchJson(`${apiUrl}/items`),
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

  const params = BRANCH_LOCATION_ID
    ? `?locationId=${BRANCH_LOCATION_ID}`
    : "";

  const { data: myRequests = [], isLoading, isError } = useQuery<TransferRequestItem[]>({
    queryKey: ["my-transfer-requests", BRANCH_LOCATION_ID],
    queryFn: () => fetchJson(`${apiUrl}/inventory/transfer-requests${params}`),
  });

  const { mutate: create, isPending } = useMutation({
    mutationFn: (values: { itemId: string; unitId: string; quantity: number; notes?: string }) =>
      fetchJson(`${apiUrl}/inventory/transfer-requests`, {
        method: "POST",
        body: JSON.stringify({
          ...values,
          locationId: BRANCH_LOCATION_ID,
        }),
      }),
    onSuccess: () => {
      message.success("Solicitud enviada correctamente");
      queryClient.invalidateQueries({ queryKey: ["my-transfer-requests"] });
      form.resetFields();
    },
    onError: () => message.error("Error al enviar la solicitud"),
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
  ];

  return (
    <div style={{ padding: isMobile ? 16 : 24 }}>
      <Typography.Title level={isMobile ? 4 : 3} style={{ marginBottom: 24 }}>
        Solicitar Transferencia
      </Typography.Title>

      <Card title="Nueva solicitud" style={{ marginBottom: 32, maxWidth: 560 }}>
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

          <Form.Item label="Notas (opcional)" name="notes">
            <Input.TextArea rows={2} placeholder="Ej: Urgente para fin de semana" />
          </Form.Item>

          <div style={{ display: "flex", gap: 8, justifyContent: "flex-end" }}>
            <Button onClick={() => navigate(-1)}>Cancelar</Button>
            <Button type="primary" htmlType="submit" loading={isPending}>
              Enviar solicitud
            </Button>
          </div>
        </Form>
      </Card>

      <Typography.Title level={5} style={{ marginBottom: 12 }}>
        Mis solicitudes anteriores
      </Typography.Title>

      {isError && (
        <Alert type="error" message="No se pudo cargar el historial de solicitudes." style={{ marginBottom: 16 }} />
      )}

      {isLoading ? (
        <Spin size="large" style={{ display: "block", marginTop: 40 }} />
      ) : (
        <Table
          dataSource={myRequests}
          columns={columns}
          rowKey="id"
          size={isMobile ? "small" : "middle"}
          pagination={{ pageSize: 10 }}
          locale={{ emptyText: "No hay solicitudes anteriores" }}
        />
      )}
    </div>
  );
};
