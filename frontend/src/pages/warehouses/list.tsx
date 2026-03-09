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

type Warehouse = {
  id: number;
  name: string;
  location: string;
};

const MOCK_DATA: Warehouse[] = [];

let nextId = 1;

export const WarehouseList = () => {
  const screens = useBreakpoint();
  const isMobile = !screens.md;

  const [data, setData] = useState<Warehouse[]>(MOCK_DATA);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Warehouse | null>(null);
  const [form] = Form.useForm();

  const openCreate = () => {
    setEditing(null);
    form.resetFields();
    setModalOpen(true);
  };

  const openEdit = (record: Warehouse) => {
    setEditing(record);
    form.setFieldsValue(record);
    setModalOpen(true);
  };

  const handleSubmit = (values: { name: string; location: string }) => {
    if (editing) {
      setData((prev) =>
        prev.map((w) => (w.id === editing.id ? { ...w, ...values } : w))
      );
      message.success("Bodega actualizada");
    } else {
      setData((prev) => [...prev, { id: nextId++, ...values }]);
      message.success("Bodega creada");
    }
    setModalOpen(false);
  };

  const handleDelete = (id: number) => {
    setData((prev) => prev.filter((w) => w.id !== id));
    message.success("Bodega eliminada");
  };

  const columns = [
    { title: "Nombre", dataIndex: "name", key: "name" },
    { title: "Ubicación", dataIndex: "location", key: "location" },
    {
      title: "Acciones",
      key: "actions",
      render: (_: unknown, record: Warehouse) => (
        <Space>
          <Button
            icon={<EditOutlined />}
            size="small"
            onClick={() => openEdit(record)}
          />
          <Popconfirm
            title="¿Eliminar bodega?"
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
          Bodegas
        </Typography.Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
          Nueva bodega
        </Button>
      </div>

      <Table
        dataSource={data}
        columns={columns}
        rowKey="id"
        size={isMobile ? "small" : "middle"}
        pagination={{ pageSize: 10 }}
        locale={{ emptyText: "No hay bodegas registradas" }}
      />

      <Modal
        title={editing ? "Editar bodega" : "Nueva bodega"}
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
            <Input placeholder="Ej: Bodega Central" />
          </Form.Item>
          <Form.Item
            label="Ubicación"
            name="location"
            rules={[{ required: true, message: "Ingresa la ubicación" }]}
          >
            <Input placeholder="Ej: Av. Principal 123" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};
