import { useQuery, useMutation } from "@tanstack/react-query";
import {
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
import { useNavigate, useSearchParams } from "react-router";
import { apiUrl, fetchJson } from "../../providers/data";
import type { Item, Location, UnitConversion } from "../../types/inventory";

const { useBreakpoint } = Grid;

export const TransferPage = () => {
  const screens = useBreakpoint();
  const isMobile = !screens.md;
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [form] = Form.useForm();

  const preselectedItemId = searchParams.get("itemId") ?? undefined;

  const { data: items = [] } = useQuery<Item[]>({
    queryKey: ["items"],
    queryFn: () => fetchJson(`${apiUrl}/items`),
  });

  const { data: locations = [] } = useQuery<Location[]>({
    queryKey: ["locations"],
    queryFn: () => fetchJson(`${apiUrl}/locations`),
  });

  const selectedItemId = Form.useWatch("itemId", form);
  const locationFromId = Form.useWatch("locationFromId", form);

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
  const destinationLocations = activeLocations.filter((l) => l.id !== locationFromId);

  const { mutate: submit, isPending } = useMutation({
    mutationFn: (values: {
      itemId: string;
      unitId: string;
      quantity: number;
      locationFromId: string;
      locationToId: string;
      notes?: string;
    }) =>
      fetchJson(`${apiUrl}/inventory/transfer`, {
        method: "POST",
        body: JSON.stringify(values),
      }),
    onSuccess: () => {
      message.success("Transferencia registrada correctamente");
      navigate("/stock");
    },
    onError: (err: Error) =>
      message.error(
        err.message.includes("insufficient")
          ? "Stock insuficiente para la transferencia"
          : "Error al registrar la transferencia"
      ),
  });

  return (
    <div style={{ padding: isMobile ? 16 : 24, maxWidth: 560 }}>
      <Typography.Title level={isMobile ? 4 : 3} style={{ marginBottom: 24 }}>
        Transferencia de Stock
      </Typography.Title>

      <Card>
        <Form
          form={form}
          layout="vertical"
          initialValues={{ itemId: preselectedItemId }}
          onFinish={submit}
        >
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
            label="Cantidad"
            name="quantity"
            rules={[{ required: true, message: "Requerido" }]}
          >
            <InputNumber style={{ width: "100%" }} min={0.000001} precision={6} placeholder="ej: 10" />
          </Form.Item>

          <Form.Item
            label="Origen"
            name="locationFromId"
            rules={[{ required: true, message: "Requerido" }]}
          >
            <Select
              placeholder="Selecciona la ubicación de origen"
              options={activeLocations.map((l) => ({ label: l.name, value: l.id }))}
              onChange={() => form.setFieldValue("locationToId", undefined)}
            />
          </Form.Item>

          <Form.Item
            label="Destino"
            name="locationToId"
            rules={[{ required: true, message: "Requerido" }]}
          >
            <Select
              placeholder={locationFromId ? "Selecciona el destino" : "Primero selecciona el origen"}
              options={destinationLocations.map((l) => ({ label: l.name, value: l.id }))}
              disabled={!locationFromId}
            />
          </Form.Item>

          <Form.Item label="Notas (opcional)" name="notes">
            <Input.TextArea rows={2} placeholder="Ej: Pedido sucursal norte" />
          </Form.Item>

          <div style={{ display: "flex", gap: 8, justifyContent: "flex-end" }}>
            <Button onClick={() => navigate(-1)}>Cancelar</Button>
            <Button type="primary" htmlType="submit" loading={isPending}>
              Registrar transferencia
            </Button>
          </div>
        </Form>
      </Card>
    </div>
  );
};
