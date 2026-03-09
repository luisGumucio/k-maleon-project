import { useQuery, useMutation } from "@tanstack/react-query";
import {
  Alert,
  Button,
  Card,
  Form,
  Grid,
  Input,
  InputNumber,
  Select,
  Typography,
  message,
} from "antd";
import { useNavigate } from "react-router";
import { apiUrl, fetchJson } from "../../providers/data";
import type { Item, Location, UnitConversion } from "../../types/inventory";

const { useBreakpoint } = Grid;

export const AdjustmentPage = () => {
  const screens = useBreakpoint();
  const isMobile = !screens.md;
  const navigate = useNavigate();
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

  const activeLocations = locations.filter((l) => l.active);

  const { mutate: submit, isPending } = useMutation({
    mutationFn: (values: {
      itemId: string;
      unitId: string;
      quantity: number;
      locationToId: string;
      notes?: string;
    }) =>
      fetchJson(`${apiUrl}/inventory/adjustment`, {
        method: "POST",
        body: JSON.stringify(values),
      }),
    onSuccess: () => {
      message.success("Ajuste registrado correctamente");
      navigate("/stock");
    },
    onError: () => message.error("Error al registrar el ajuste"),
  });

  return (
    <div style={{ padding: isMobile ? 16 : 24, maxWidth: 560 }}>
      <Typography.Title level={isMobile ? 4 : 3} style={{ marginBottom: 16 }}>
        Ajuste de Stock
      </Typography.Title>

      <Alert
        type="warning"
        message="Un ajuste sobrescribe el stock actual de la ubicación seleccionada para este producto. Úsalo solo para correcciones de inventario."
        style={{ marginBottom: 24 }}
        showIcon
      />

      <Card>
        <Form form={form} layout="vertical" onFinish={submit}>
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
            label="Nueva cantidad"
            name="quantity"
            rules={[{ required: true, message: "Requerido" }]}
          >
            <InputNumber style={{ width: "100%" }} min={0} precision={6} placeholder="ej: 100" />
          </Form.Item>

          <Form.Item
            label="Ubicación"
            name="locationToId"
            rules={[{ required: true, message: "Requerido" }]}
          >
            <Select
              placeholder="Selecciona la ubicación"
              options={activeLocations.map((l) => ({ label: l.name, value: l.id }))}
            />
          </Form.Item>

          <Form.Item label="Motivo del ajuste" name="notes">
            <Input.TextArea rows={2} placeholder="Ej: Conteo físico de inventario" />
          </Form.Item>

          <div style={{ display: "flex", gap: 8, justifyContent: "flex-end" }}>
            <Button onClick={() => navigate(-1)}>Cancelar</Button>
            <Button type="primary" htmlType="submit" loading={isPending} danger>
              Registrar ajuste
            </Button>
          </div>
        </Form>
      </Card>
    </div>
  );
};
