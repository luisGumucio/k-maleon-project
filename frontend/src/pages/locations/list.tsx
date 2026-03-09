import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
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
  message,
  Card,
  Row,
  Col,
  Tooltip,
} from "antd";
import {
  DeleteOutlined,
  EditOutlined,
  PlusOutlined,
  SearchOutlined,
  EnvironmentOutlined,
  ShopOutlined,
  BankOutlined,
} from "@ant-design/icons";
import { apiUrl, fetchJson } from "../../providers/data";
import type { Location } from "../../types/inventory";

const { Title, Text } = Typography;

export const LocationList = () => {
  const queryClient = useQueryClient();
  const [form] = Form.useForm();
  
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Location | null>(null);
  const [searchText, setSearchText] = useState("");

  const { data: locations = [], isLoading } = useQuery<Location[]>({
    queryKey: ["locations"],
    queryFn: () => fetchJson(`${apiUrl}/locations`),
  });

  const { mutate: saveLocation, isPending: saving } = useMutation({
    mutationFn: (values: { name: string; type: string }) =>
      editing
        ? fetchJson(`${apiUrl}/locations/${editing.id}`, {
            method: "PUT",
            body: JSON.stringify(values),
          })
        : fetchJson(`${apiUrl}/locations`, {
            method: "POST",
            body: JSON.stringify(values),
          }),
    onSuccess: () => {
      message.success(editing ? "Ubicación actualizada exitosamente" : "Ubicación creada exitosamente");
      queryClient.invalidateQueries({ queryKey: ["locations"] });
      handleClose();
    },
    onError: () => message.error("Error al guardar la ubicación"),
  });

  const { mutate: deleteLocation } = useMutation({
    mutationFn: (id: string) =>
      fetchJson(`${apiUrl}/locations/${id}`, { method: "DELETE" }),
    onSuccess: () => {
      message.success("Ubicación desactivada exitosamente");
      queryClient.invalidateQueries({ queryKey: ["locations"] });
    },
    onError: () => message.error("Error al eliminar la ubicación"),
  });

  const handleClose = () => {
    setOpen(false);
    setEditing(null);
    form.resetFields();
  };

  const openEdit = (record: Location) => {
    setEditing(record);
    form.setFieldsValue({ name: record.name, type: record.type });
    setOpen(true);
  };

  const filteredLocations = locations.filter((loc) =>
    loc.name.toLowerCase().includes(searchText.toLowerCase())
  );

  const columns = [
    { 
      title: "Nombre de la Ubicación", 
      dataIndex: "name", 
      key: "name",
      render: (text: string, record: Location) => (
        <Space>
          {record.type === "warehouse" ? (
            <BankOutlined style={{ color: "#1890ff" }} />
          ) : (
            <ShopOutlined style={{ color: "#52c41a" }} />
          )}
          <Text strong>{text}</Text>
        </Space>
      )
    },
    {
      title: "Tipo",
      dataIndex: "type",
      key: "type",
      render: (type: string) => (
        <Tag color={type === "warehouse" ? "blue" : "green"} style={{ padding: "4px 12px", borderRadius: "16px", fontSize: "13px" }}>
          {type === "warehouse" ? "BODEGA CENTRAL" : "SUCURSAL"}
        </Tag>
      ),
    },
    {
      title: "Acciones",
      key: "actions",
      align: "right" as const,
      render: (_: unknown, record: Location) => (
        <Space size="middle">
          <Tooltip title="Editar Ubicación">
            <Button
              type="text"
              icon={<EditOutlined style={{ color: "#1890ff" }} />}
              onClick={() => openEdit(record)}
            />
          </Tooltip>
          <Tooltip title="Desactivar Ubicación">
            <Popconfirm
              title="¿Desactivar esta ubicación?"
              description="Ya no se la podrá seleccionar para compras o transferencias."
              onConfirm={() => deleteLocation(record.id)}
              okText="Sí, desactivar"
              cancelText="Cancelar"
              okButtonProps={{ danger: true }}
            >
              <Button type="text" danger icon={<DeleteOutlined />} />
            </Popconfirm>
          </Tooltip>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: "24px", maxWidth: "1200px", margin: "0 auto" }}>
      <Row justify="space-between" align="middle" style={{ marginBottom: "24px" }}>
        <Col>
          <Title level={3} style={{ margin: 0, display: "flex", alignItems: "center", gap: "10px" }}>
            <EnvironmentOutlined style={{ color: "#1890ff" }} /> Ubicaciones
          </Title>
          <Text type="secondary">Gestiona las Bodegas Centrales y Sucursales de tu negocio.</Text>
        </Col>
        <Col>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            size="large"
            onClick={() => setOpen(true)}
            style={{ borderRadius: "6px" }}
          >
            Nueva Ubicación
          </Button>
        </Col>
      </Row>

      <Card
        bordered={false}
        style={{ borderRadius: "8px", boxShadow: "0 1px 2px -2px rgba(0,0,0,0.16), 0 3px 6px 0 rgba(0,0,0,0.12), 0 5px 12px 4px rgba(0,0,0,0.09)" }}
      >
        <div style={{ marginBottom: "16px", display: "flex", justifyContent: "flex-end" }}>
          <Input
            placeholder="Buscar por nombre..."
            prefix={<SearchOutlined style={{ color: "rgba(0,0,0,.25)" }} />}
            style={{ width: 300, borderRadius: "6px" }}
            onChange={(e) => setSearchText(e.target.value)}
            allowClear
          />
        </div>

        <Table
          dataSource={filteredLocations}
          columns={columns}
          rowKey="id"
          loading={isLoading}
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            pageSizeOptions: ["10", "20", "50"],
            showTotal: (total) => `Total: ${total} ubicaciones`,
          }}
          locale={{
            emptyText: searchText ? (
              <div style={{ padding: "40px 0" }}>
                <SearchOutlined style={{ fontSize: "48px", color: "#d9d9d9", marginBottom: "16px" }} />
                <Typography.Title level={5} style={{ color: "#8c8c8c" }}>
                  No se encontraron ubicaciones para "{searchText}"
                </Typography.Title>
              </div>
            ) : (
              <div style={{ padding: "40px 0" }}>
                <EnvironmentOutlined style={{ fontSize: "48px", color: "#d9d9d9", marginBottom: "16px" }} />
                <Typography.Title level={5} style={{ color: "#8c8c8c" }}>
                  Aún no tienes ubicaciones registradas
                </Typography.Title>
                <Text type="secondary">
                  Comienza agregando tu primera Bodega Central o Sucursal.
                </Text>
              </div>
            ),
          }}
        />
      </Card>

      <Modal
        title={
          <Space>
            {editing ? <EditOutlined /> : <PlusOutlined />}
            {editing ? "Editar Ubicación" : "Nueva Ubicación"}
          </Space>
        }
        open={open}
        onCancel={handleClose}
        onOk={() => form.submit()}
        okText={editing ? "Guardar Cambios" : "Crear Ubicación"}
        cancelText="Cancelar"
        confirmLoading={saving}
        destroyOnClose
        width={420}
        centered
        style={{ padding: "24px" }}
      >
        <Form 
          form={form} 
          layout="vertical" 
          onFinish={saveLocation}
          style={{ marginTop: "24px" }}
        >
          <Form.Item
            label={<Text strong>Nombre</Text>}
            name="name"
            rules={[
              { required: true, message: "El nombre es obligatorio" },
              { min: 3, message: "Debe tener al menos 3 caracteres" }
            ]}
          >
            <Input placeholder="Ejemplo: Bodega Norte, Sucursal Centro" size="large" />
          </Form.Item>
          
          <Form.Item
            label={<Text strong>Tipo de Ubicación</Text>}
            name="type"
            rules={[{ required: true, message: "Debes seleccionar un tipo" }]}
          >
            <Select
              size="large"
              placeholder="Selecciona el tipo"
              options={[
                { 
                  label: <span><BankOutlined style={{ color: "#1890ff", marginRight: "8px" }} /> Bodega Central</span>, 
                  value: "warehouse" 
                },
                { 
                  label: <span><ShopOutlined style={{ color: "#52c41a", marginRight: "8px" }} /> Sucursal</span>, 
                  value: "branch" 
                },
              ]}
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};
