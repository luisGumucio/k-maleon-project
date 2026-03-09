import { Form, Input, InputNumber, Modal, message } from "antd";
import { ShipmentItem } from "../../types/shipment-item";
import { apiUrl, fetchJson } from "../../providers/data";

type Props = {
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
  shipmentId: string;
  item?: ShipmentItem;
};

type FormValues = {
  description: string;
  quantity?: number;
  unitPrice?: number;
  amount: number;
};

export const ItemFormModal = ({ open, onClose, onSuccess, shipmentId, item }: Props) => {
  const [form] = Form.useForm<FormValues>();
  const isEditing = !!item;

  const handleFinish = async (values: FormValues) => {
    try {
      const body = {
        shipmentId,
        description: values.description,
        quantity: values.quantity ?? null,
        unitPrice: values.unitPrice ?? null,
        amount: values.amount,
      };

      if (isEditing) {
        await fetchJson(`${apiUrl}/shipment-items/${item.id}`, {
          method: "PUT",
          body: JSON.stringify(body),
        });
      } else {
        await fetchJson(`${apiUrl}/shipment-items`, {
          method: "POST",
          body: JSON.stringify(body),
        });
      }

      message.success(isEditing ? "Ítem actualizado" : "Ítem agregado");
      form.resetFields();
      onSuccess();
    } catch {
      message.error("Error al guardar el ítem");
    }
  };

  const handleCancel = () => {
    form.resetFields();
    onClose();
  };

  return (
    <Modal
      title={isEditing ? "Editar ítem" : "Agregar ítem"}
      open={open}
      onOk={() => form.submit()}
      onCancel={handleCancel}
      okText={isEditing ? "Guardar" : "Agregar"}
      cancelText="Cancelar"
      destroyOnClose
    >
      <Form
        form={form}
        layout="vertical"
        onFinish={handleFinish}
        initialValues={
          item
            ? {
                description: item.description,
                quantity: item.quantity ?? undefined,
                unitPrice: item.unitPrice ?? undefined,
                amount: item.amount,
              }
            : undefined
        }
      >
        <Form.Item
          name="description"
          label="Descripción"
          rules={[{ required: true, message: "Requerido" }]}
        >
          <Input placeholder="Ej: Converse mujer" />
        </Form.Item>

        <Form.Item name="quantity" label="Cantidad">
          <InputNumber min={0} precision={0} style={{ width: "100%" }} placeholder="Ej: 100" />
        </Form.Item>

        <Form.Item name="unitPrice" label="Precio unitario">
          <InputNumber
            min={0}
            precision={2}
            prefix="$"
            style={{ width: "100%" }}
            placeholder="Ej: 5.00"
          />
        </Form.Item>

        <Form.Item
          name="amount"
          label="Importe"
          rules={[{ required: true, message: "Requerido" }]}
        >
          <InputNumber
            min={0}
            precision={2}
            prefix="$"
            style={{ width: "100%" }}
            placeholder="Ej: 500.00"
          />
        </Form.Item>
      </Form>
    </Modal>
  );
};
