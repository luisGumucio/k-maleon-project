import { useForm, useSelect } from "@refinedev/antd";
import { Form, Input, Select, InputNumber, DatePicker, Row, Col } from "antd";
import dayjs from "dayjs";
import { Operation } from "../../types/operation";
import { dollarsToCents, centsToDollars } from "../../utils/money";

export const useOperationForm = (action: "create" | "edit") => {
  const { formProps, saveButtonProps, query } = useForm<Operation>({
    action,
    redirect: "show",
    onMutationSuccess: () => {},
  });

  const operation = query?.data?.data;

  const initialValues = operation
    ? {
        ...operation,
        totalAmount: centsToDollars(operation.totalAmount),
        startDate: operation.startDate ? dayjs(operation.startDate) : undefined,
        endDate: operation.endDate ? dayjs(operation.endDate) : undefined,
      }
    : undefined;

  const wrappedFormProps = {
    ...formProps,
    onFinish: (values: Record<string, unknown>) => {
      const payload = {
        ...values,
        totalAmount: dollarsToCents(values.totalAmount as number),
        startDate: values.startDate
          ? (values.startDate as dayjs.Dayjs).format("YYYY-MM-DD")
          : undefined,
        endDate: values.endDate
          ? (values.endDate as dayjs.Dayjs).format("YYYY-MM-DD")
          : undefined,
      };
      return formProps.onFinish?.(payload);
    },
  };

  return { formProps: wrappedFormProps, saveButtonProps, initialValues };
};

type OperationFormFieldsProps = {
  formProps: ReturnType<typeof useOperationForm>["formProps"];
  initialValues?: ReturnType<typeof useOperationForm>["initialValues"];
};

export const OperationFormFields = ({ formProps, initialValues }: OperationFormFieldsProps) => {
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
          <Form.Item
            label="Contenedor"
            name="container"
            rules={[{ required: true, message: "El contenedor es requerido" }]}
          >
            <Input placeholder="Ej: CARU5170029" />
          </Form.Item>
        </Col>
      </Row>

      <Row gutter={16}>
        <Col xs={24} sm={12}>
          <Form.Item label="Descripción" name="description">
            <Input.TextArea rows={2} placeholder="Descripción de la mercadería" />
          </Form.Item>
        </Col>
        <Col xs={24} sm={12}>
          <Form.Item label="Origen" name="origin">
            <Input placeholder="Ej: Shanghai" />
          </Form.Item>
        </Col>
      </Row>

      <Row gutter={16}>
        <Col xs={24} sm={8}>
          <Form.Item
            label="Monto acordado (USD)"
            name="totalAmount"
            rules={[{ required: true, message: "El monto es requerido" }]}
          >
            <InputNumber
              style={{ width: "100%" }}
              min={0.01}
              precision={2}
              prefix="$"
              placeholder="0.00"
            />
          </Form.Item>
        </Col>
        <Col xs={24} sm={8}>
          <Form.Item
            label="Fecha inicio"
            name="startDate"
            rules={[{ required: true, message: "La fecha de inicio es requerida" }]}
          >
            <DatePicker style={{ width: "100%" }} format="DD/MM/YYYY" />
          </Form.Item>
        </Col>
        <Col xs={24} sm={8}>
          <Form.Item label="Fecha fin" name="endDate">
            <DatePicker style={{ width: "100%" }} format="DD/MM/YYYY" />
          </Form.Item>
        </Col>
      </Row>

      <Row gutter={16}>
        <Col xs={24} sm={12}>
          <Form.Item label="Estado" name="status" initialValue="active">
            <Select>
              <Select.Option value="active">Activa</Select.Option>
              <Select.Option value="completed">Completada</Select.Option>
              <Select.Option value="cancelled">Cancelada</Select.Option>
            </Select>
          </Form.Item>
        </Col>
        <Col xs={24} sm={12}>
          <Form.Item label="Notas" name="notes">
            <Input.TextArea rows={2} placeholder="Notas adicionales" />
          </Form.Item>
        </Col>
      </Row>
    </Form>
  );
};
