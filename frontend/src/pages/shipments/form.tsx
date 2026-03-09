import { useForm, useSelect } from "@refinedev/antd";
import { Form, Input, Select, InputNumber, DatePicker, Row, Col } from "antd";
import dayjs from "dayjs";
import { Shipment } from "../../types/shipment";

export const useShipmentForm = (action: "create" | "edit", documentUrl?: string | null) => {
  const { formProps, saveButtonProps, query } = useForm<Shipment>({
    action,
    redirect: action === "create" ? "list" : "show",
  });

  const shipment = query?.data?.data;

  const initialValues = shipment
    ? {
        ...shipment,
        departureDate: shipment.departureDate ? dayjs(shipment.departureDate) : undefined,
        arrivalDate: shipment.arrivalDate ? dayjs(shipment.arrivalDate) : undefined,
      }
    : undefined;

  const wrappedFormProps = {
    ...formProps,
    onFinish: (values: Record<string, unknown>) => {
      const payload = {
        ...values,
        departureDate: values.departureDate
          ? (values.departureDate as dayjs.Dayjs).format("YYYY-MM-DD")
          : null,
        arrivalDate: values.arrivalDate
          ? (values.arrivalDate as dayjs.Dayjs).format("YYYY-MM-DD")
          : null,
        ...(documentUrl !== undefined ? { documentUrl: documentUrl ?? null } : {}),
      };
      return formProps.onFinish?.(payload);
    },
  };

  return { formProps: wrappedFormProps, saveButtonProps, initialValues, shipment };
};

type ShipmentFormFieldsProps = {
  formProps: ReturnType<typeof useShipmentForm>["formProps"];
  initialValues?: ReturnType<typeof useShipmentForm>["initialValues"];
};

export const ShipmentFormFields = ({ formProps, initialValues }: ShipmentFormFieldsProps) => {
  const { selectProps: supplierSelectProps } = useSelect({
    resource: "suppliers",
    optionLabel: "name",
    optionValue: "id",
  });

  return (
    <Form {...formProps} layout="vertical" initialValues={initialValues}>
      <Row gutter={16}>
        <Col xs={24} sm={12}>
          <Form.Item
            label="Proveedor"
            name="supplierId"
            rules={[{ required: true, message: "Selecciona un proveedor" }]}
          >
            <Select {...supplierSelectProps} placeholder="Selecciona un proveedor" />
          </Form.Item>
        </Col>
        <Col xs={24} sm={12}>
          <Form.Item label="N° Contenedor" name="containerNumber">
            <Input placeholder="Ej: CARU5170029" />
          </Form.Item>
        </Col>
      </Row>

      <Row gutter={16}>
        <Col xs={24} sm={12}>
          <Form.Item label="Fecha de partida" name="departureDate">
            <DatePicker style={{ width: "100%" }} format="DD/MM/YYYY" />
          </Form.Item>
        </Col>
        <Col xs={24} sm={12}>
          <Form.Item label="Fecha de llegada" name="arrivalDate">
            <DatePicker style={{ width: "100%" }} format="DD/MM/YYYY" />
          </Form.Item>
        </Col>
      </Row>

      <Row gutter={16}>
        <Col xs={24} sm={12}>
          <Form.Item label="Cantidad" name="quantity">
            <InputNumber style={{ width: "100%" }} min={1} precision={0} placeholder="0" />
          </Form.Item>
        </Col>
      </Row>

      <Row gutter={16}>
        <Col xs={24}>
          <Form.Item label="Detalles del producto" name="productDetails">
            <Input.TextArea rows={3} placeholder="Descripción de la mercadería" />
          </Form.Item>
        </Col>
      </Row>
    </Form>
  );
};
