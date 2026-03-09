import { Edit, useForm } from "@refinedev/antd";
import { Form, Input } from "antd";

export const SupplierEdit = () => {
  const { formProps, saveButtonProps } = useForm();

  return (
    <Edit saveButtonProps={saveButtonProps}>
      <Form {...formProps} layout="vertical">
        <Form.Item
          label="Nombre"
          name="name"
          rules={[{ required: true, message: "El nombre es requerido" }]}
        >
          <Input placeholder="Nombre del proveedor" />
        </Form.Item>
        <Form.Item
          label="Email"
          name="email"
          rules={[{ type: "email", message: "Ingresa un email válido" }]}
        >
          <Input placeholder="correo@ejemplo.com" />
        </Form.Item>
        <Form.Item label="Teléfono" name="phone">
          <Input placeholder="+52 55 1234 5678" />
        </Form.Item>
      </Form>
    </Edit>
  );
};
