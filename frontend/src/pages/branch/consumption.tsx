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
import { useNavigate } from "react-router";
import { apiUrl, fetchJson } from "../../providers/data";
import type { Item, UnitConversion } from "../../types/inventory";

// In a real app this would come from the auth token.
const BRANCH_LOCATION_ID = import.meta.env.VITE_MOCK_BRANCH_LOCATION_ID as string | undefined;

const { useBreakpoint } = Grid;

export const BranchConsumptionPage = () => {
  const screens = useBreakpoint();
  const isMobile = !screens.md;
  const navigate = useNavigate();
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

  const { mutate: submit, isPending } = useMutation({
    mutationFn: (values: {
      itemId: string;
      unitId: string;
      quantity: number;
      notes?: string;
    }) =>
      fetchJson(`${apiUrl}/inventory/consumption`, {
        method: "POST",
        body: JSON.stringify({
          ...values,
          locationFromId: BRANCH_LOCATION_ID,
        }),
      }),
    onSuccess: () => {
      message.success("Consumo registrado correctamente");
      navigate("/my/stock");
    },
    onError: (err: Error) =>
      message.error(
        err.message.includes("insufficient")
          ? "Stock insuficiente para registrar el consumo"
          : "Error al registrar el consumo"
      ),
  });

  return (
    <div style={{ padding: isMobile ? 16 : 24, maxWidth: 560 }}>
      <Typography.Title level={isMobile ? 4 : 3} style={{ marginBottom: 24 }}>
        Registrar Consumo
      </Typography.Title>

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
            label="Cantidad consumida"
            name="quantity"
            rules={[{ required: true, message: "Requerido" }]}
          >
            <InputNumber style={{ width: "100%" }} min={0.000001} precision={6} placeholder="ej: 5" />
          </Form.Item>

          <Form.Item label="Notas (opcional)" name="notes">
            <Input.TextArea rows={2} placeholder="Ej: Consumo del día" />
          </Form.Item>

          <div style={{ display: "flex", gap: 8, justifyContent: "flex-end" }}>
            <Button onClick={() => navigate(-1)}>Cancelar</Button>
            <Button type="primary" htmlType="submit" loading={isPending}>
              Registrar consumo
            </Button>
          </div>
        </Form>
      </Card>
    </div>
  );
};
