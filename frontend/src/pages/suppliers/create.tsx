import { Create, useForm } from "@refinedev/antd";
import { Form, Input } from "antd";

export const SupplierCreate = () => {
  const { formProps, saveButtonProps } = useForm();

  return (
    <Create saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item
          label="Nombre"
          name="name"
          rules={[{ required: true, message: "El nombre es requerido" }]}
        >
          <Input placeholder="Nombre del proveedor" />
        </Form.Item>
      </Form>
    </Create>
  );
};
