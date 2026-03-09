import { useState } from "react";
import {
  Button,
  Form,
  Input,
  Modal,
  Popconfirm,
  Space,
  Table,
  Tag,
  Typography,
  Grid,
  message,
} from "antd";
import { PlusOutlined, EditOutlined, DeleteOutlined } from "@ant-design/icons";

const { useBreakpoint } = Grid;

type InventoryUser = {
  id: number;
  name: string;
  email: string;
  role: "almacenero";
  createdAt: string;
};

let nextId = 1;

export const InventoryUserList = () => {
  const screens = useBreakpoint();
  const isMobile = !screens.md;

  const [data, setData] = useState<InventoryUser[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<InventoryUser | null>(null);
  const [form] = Form.useForm();

  const openCreate = () => {
    setEditing(null);
    form.resetFields();
    setModalOpen(true);
  };

  const openEdit = (record: InventoryUser) => {
    setEditing(record);
    form.setFieldsValue({ name: record.name, email: record.email });
    setModalOpen(true);
  };

  const handleSubmit = (values: {
    name: string;
    email: string;
    password?: string;
  }) => {
    if (editing) {
      setData((prev) =>
        prev.map((u) =>
          u.id === editing.id
            ? { ...u, name: values.name, email: values.email }
            : u
        )
      );
      message.success("Usuario actualizado");
    } else {
      setData((prev) => [
        ...prev,
        {
          id: nextId++,
          name: values.name,
          email: values.email,
          role: "almacenero",
          createdAt: new Date().toISOString(),
        },
      ]);
      message.success("Usuario creado");
    }
    setModalOpen(false);
  };

  const handleDelete = (id: number) => {
    setData((prev) => prev.filter((u) => u.id !== id));
    message.success("Usuario eliminado");
  };

  const columns = [
    { title: "Nombre", dataIndex: "name", key: "name" },
    { title: "Email", dataIndex: "email", key: "email" },
    {
      title: "Rol",
      dataIndex: "role",
      key: "role",
      render: () => <Tag color="blue">almacenero</Tag>,
    },
    {
      title: "Fecha de creación",
      dataIndex: "createdAt",
      key: "createdAt",
      render: (val: string) => new Date(val).toLocaleDateString("es-CL"),
    },
    {
      title: "Acciones",
      key: "actions",
      render: (_: unknown, record: InventoryUser) => (
        <Space>
          <Button
            icon={<EditOutlined />}
            size="small"
            onClick={() => openEdit(record)}
          />
          <Popconfirm
            title="¿Eliminar usuario?"
            onConfirm={() => handleDelete(record.id)}
            okText="Sí"
            cancelText="No"
          >
            <Button icon={<DeleteOutlined />} size="small" danger />
          </Popconfirm>
        </Space>
      ),
    },
  ];

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
          Usuarios
        </Typography.Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
          Nuevo usuario
        </Button>
      </div>

      <Table
        dataSource={data}
        columns={columns}
        rowKey="id"
        size={isMobile ? "small" : "middle"}
        pagination={{ pageSize: 10 }}
        locale={{ emptyText: "No hay usuarios registrados" }}
      />

      <Modal
        title={editing ? "Editar usuario" : "Nuevo usuario"}
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={() => form.submit()}
        okText="Guardar"
        cancelText="Cancelar"
        destroyOnClose
        width={isMobile ? "100%" : 420}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item
            label="Nombre"
            name="name"
            rules={[{ required: true, message: "Ingresa el nombre" }]}
          >
            <Input placeholder="Nombre completo" />
          </Form.Item>
          <Form.Item
            label="Email"
            name="email"
            rules={[
              { required: true, message: "Ingresa el email" },
              { type: "email", message: "Email inválido" },
            ]}
          >
            <Input placeholder="correo@ejemplo.com" />
          </Form.Item>
          {!editing && (
            <Form.Item
              label="Contraseña"
              name="password"
              rules={[{ required: true, message: "Ingresa la contraseña" }]}
            >
              <Input.Password placeholder="Contraseña" />
            </Form.Item>
          )}
        </Form>
      </Modal>
    </div>
  );
};
