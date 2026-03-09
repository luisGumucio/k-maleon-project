import { useEffect, useState } from "react";
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
import { fetchJson, apiUrl } from "../../providers/data";

const { useBreakpoint } = Grid;

type UserRole = "super_admin" | "admin" | "inventory_admin" | "almacenero" | "encargado_sucursal";

type AppUser = {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  createdAt: string;
};

const ALL_ROLE_OPTIONS: { label: string; value: UserRole }[] = [
  { label: "Super Admin", value: "super_admin" },
  { label: "Admin", value: "admin" },
  { label: "Inventory Admin", value: "inventory_admin" },
  { label: "Almacenero", value: "almacenero" },
  { label: "Encargado de Sucursal", value: "encargado_sucursal" },
];

const ROLE_COLORS: Record<string, string> = {
  super_admin: "red",
  admin: "blue",
  inventory_admin: "green",
  almacenero: "default",
  encargado_sucursal: "orange",
};

export const UserList = () => {
  const screens = useBreakpoint();
  const isMobile = !screens.md;

  const currentRole = localStorage.getItem("kmaleon_role") ?? "admin";
  const isInventoryAdmin = currentRole === "inventory_admin";

  const [data, setData] = useState<AppUser[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<AppUser | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm();

  const selectedRole = Form.useWatch("role", form);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const result = await fetchJson(`${apiUrl}/users`);
      setData(Array.isArray(result) ? result : result.content ?? []);
    } catch {
      message.error("Error al cargar usuarios");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const openCreate = () => {
    setEditing(null);
    form.resetFields();
    if (isInventoryAdmin) {
      form.setFieldValue("role", "almacenero");
    }
    setModalOpen(true);
  };

  const openEdit = (record: AppUser) => {
    setEditing(record);
    form.setFieldsValue({ name: record.name });
    setModalOpen(true);
  };

  const handleSubmit = async (values: {
    name: string;
    email?: string;
    role?: UserRole;
    password?: string;
    locationId?: string;
    newPassword?: string;
  }) => {
    setSubmitting(true);
    try {
      if (editing) {
        const body: Record<string, string> = { name: values.name };
        if (values.newPassword) body.password = values.newPassword;
        await fetchJson(`${apiUrl}/users/${editing.id}`, {
          method: "PUT",
          body: JSON.stringify(body),
        });
        message.success("Usuario actualizado");
      } else {
        const body: Record<string, string> = {
          name: values.name!,
          email: values.email!,
          password: values.password!,
          role: isInventoryAdmin ? "almacenero" : values.role!,
        };
        if (values.locationId) body.locationId = values.locationId;
        await fetchJson(`${apiUrl}/users`, {
          method: "POST",
          body: JSON.stringify(body),
        });
        message.success("Usuario creado");
      }
      setModalOpen(false);
      fetchUsers();
    } catch (err: unknown) {
      message.error(err instanceof Error ? err.message : "Error al guardar usuario");
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await fetchJson(`${apiUrl}/users/${id}`, { method: "DELETE" });
      message.success("Usuario eliminado");
      setData((prev) => prev.filter((u) => u.id !== id));
    } catch {
      message.error("Error al eliminar usuario");
    }
  };

  const columns = [
    { title: "Nombre", dataIndex: "name", key: "name" },
    { title: "Email", dataIndex: "email", key: "email", responsive: ["md"] as ("md")[] },
    {
      title: "Rol",
      dataIndex: "role",
      key: "role",
      render: (role: string) => (
        <Tag color={ROLE_COLORS[role] ?? "default"}>{role}</Tag>
      ),
    },
    {
      title: "Fecha de creación",
      dataIndex: "createdAt",
      key: "createdAt",
      responsive: ["lg"] as ("lg")[],
      render: (val: string) => val ? new Date(val).toLocaleDateString("es-CL") : "—",
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
            description="Esta acción desactivará al usuario."
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

  const roleOptions = isInventoryAdmin
    ? ALL_ROLE_OPTIONS.filter((r) => r.value === "almacenero")
    : ALL_ROLE_OPTIONS;

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
        loading={loading}
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
        confirmLoading={submitting}
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

          {!editing && (
            <>
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
                label="Contraseña"
                name="password"
                rules={[{ required: true, message: "Ingresa la contraseña" }]}
              >
                <Input.Password placeholder="Contraseña" />
              </Form.Item>

              {!isInventoryAdmin && (
                <Form.Item
                  label="Rol"
                  name="role"
                  rules={[{ required: true, message: "Selecciona un rol" }]}
                >
                  <Select options={roleOptions} placeholder="Selecciona un rol" />
                </Form.Item>
              )}

              {selectedRole === "encargado_sucursal" && (
                <Form.Item
                  label="Sucursal"
                  name="locationId"
                  rules={[{ required: true, message: "Selecciona la sucursal" }]}
                >
                  <Input placeholder="ID de la sucursal" />
                </Form.Item>
              )}
            </>
          )}

          {editing && (
            <Form.Item
              label="Nueva contraseña (opcional)"
              name="newPassword"
            >
              <Input.Password placeholder="Dejar vacío para no cambiar" />
            </Form.Item>
          )}
        </Form>
      </Modal>
    </div>
  );
};
