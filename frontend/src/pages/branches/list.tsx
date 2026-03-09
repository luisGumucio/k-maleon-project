import { useState } from "react";
import {
  Button,
  Form,
  Input,
  Modal,
  Popconfirm,
  Space,
  Table,
  Typography,
  Grid,
  message,
} from "antd";
import { PlusOutlined, EditOutlined, DeleteOutlined } from "@ant-design/icons";

const { useBreakpoint } = Grid;

type Branch = {
  id: number;
  name: string;
  address: string;
};

let nextId = 1;

export const BranchList = () => {
  const screens = useBreakpoint();
  const isMobile = !screens.md;

  const [data, setData] = useState<Branch[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Branch | null>(null);
  const [form] = Form.useForm();

  const openCreate = () => {
    setEditing(null);
    form.resetFields();
    setModalOpen(true);
  };

  const openEdit = (record: Branch) => {
    setEditing(record);
    form.setFieldsValue(record);
    setModalOpen(true);
  };

  const handleSubmit = (values: { name: string; address: string }) => {
    if (editing) {
      setData((prev) =>
        prev.map((b) => (b.id === editing.id ? { ...b, ...values } : b))
      );
      message.success("Sucursal actualizada");
    } else {
      setData((prev) => [...prev, { id: nextId++, ...values }]);
      message.success("Sucursal creada");
    }
    setModalOpen(false);
  };

  const handleDelete = (id: number) => {
    setData((prev) => prev.filter((b) => b.id !== id));
    message.success("Sucursal eliminada");
  };

  const columns = [
    { title: "Nombre", dataIndex: "name", key: "name" },
    { title: "Dirección", dataIndex: "address", key: "address" },
    {
      title: "Acciones",
      key: "actions",
      render: (_: unknown, record: Branch) => (
        <Space>
          <Button
            icon={<EditOutlined />}
            size="small"
            onClick={() => openEdit(record)}
          />
          <Popconfirm
            title="¿Eliminar sucursal?"
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
          Sucursales
        </Typography.Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
          Nueva sucursal
        </Button>
      </div>

      <Table
        dataSource={data}
        columns={columns}
        rowKey="id"
        size={isMobile ? "small" : "middle"}
        pagination={{ pageSize: 10 }}
        locale={{ emptyText: "No hay sucursales registradas" }}
      />

      <Modal
        title={editing ? "Editar sucursal" : "Nueva sucursal"}
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
            <Input placeholder="Ej: Sucursal Norte" />
          </Form.Item>
          <Form.Item
            label="Dirección"
            name="address"
            rules={[{ required: true, message: "Ingresa la dirección" }]}
          >
            <Input placeholder="Ej: Calle 456" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};
