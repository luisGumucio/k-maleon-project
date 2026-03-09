import { useState } from "react";
import {
  Button,
  Form,
  Input,
  Modal,
  Popconfirm,
  Select,
  Space,
  Table,
  Tag,
  Typography,
  Grid,
  message,
} from "antd";
import { PlusOutlined, EditOutlined, DeleteOutlined } from "@ant-design/icons";

const { useBreakpoint } = Grid;

type UserRole = "super_admin" | "admin" | "inventory_admin" | "almacenero";

type AppUser = {
  id: number;
  name: string;
  email: string;
  role: UserRole;
  createdAt: string;
};

const ROLE_OPTIONS: { label: string; value: UserRole }[] = [
  { label: "Super Admin", value: "super_admin" },
  { label: "Admin", value: "admin" },
  { label: "Inventory Admin", value: "inventory_admin" },
  { label: "Almacenero", value: "almacenero" },
];

const ROLE_COLORS: Record<UserRole, string> = {
  super_admin: "red",
  admin: "blue",
  inventory_admin: "green",
  almacenero: "default",
};

let nextId = 1;

export const UserList = () => {
  const screens = useBreakpoint();
  const isMobile = !screens.md;

  const [data, setData] = useState<AppUser[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<AppUser | null>(null);
  const [form] = Form.useForm();

  const openCreate = () => {
    setEditing(null);
    form.resetFields();
    setModalOpen(true);
  };

  const openEdit = (record: AppUser) => {
    setEditing(record);
    form.setFieldsValue({ name: record.name, email: record.email, role: record.role });
    setModalOpen(true);
  };

  const handleSubmit = (values: {
    name: string;
    email: string;
    role: UserRole;
    password?: string;
  }) => {
    if (editing) {
      setData((prev) =>
        prev.map((u) =>
          u.id === editing.id
            ? { ...u, name: values.name, email: values.email, role: values.role }
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
          role: values.role,
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
      render: (role: UserRole) => (
        <Tag color={ROLE_COLORS[role]}>{role}</Tag>
      ),
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
      render: (_: unknown, record: AppUser) => (
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
          <Form.Item
            label="Rol"
            name="role"
            rules={[{ required: true, message: "Selecciona un rol" }]}
          >
            <Select options={ROLE_OPTIONS} placeholder="Selecciona un rol" />
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
