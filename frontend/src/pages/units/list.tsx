import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  Button,
  Form,
  Input,
  Modal,
  Popconfirm,
  Table,
  Typography,
  message,
  Space,
  Card,
  Row,
  Col,
  Tooltip,
} from "antd";
import {
  DeleteOutlined,
  PlusOutlined,
  SearchOutlined,
  EditOutlined,
  ExperimentOutlined,
} from "@ant-design/icons";
import { apiUrl, fetchJson } from "../../providers/data";
import type { Unit } from "../../types/inventory";

const { Title, Text } = Typography;

export const UnitList = () => {
  const queryClient = useQueryClient();
  const [form] = Form.useForm();
  
  const [open, setOpen] = useState(false);
  const [editingUnit, setEditingUnit] = useState<Unit | null>(null);
  const [searchText, setSearchText] = useState("");

  const { data: units = [], isLoading } = useQuery<Unit[]>({
    queryKey: ["units"],
    queryFn: () => fetchJson(`${apiUrl}/units`),
  });

  const { mutate: createUnit, isPending: creating } = useMutation({
    mutationFn: (values: { name: string; symbol: string }) =>
      fetchJson(`${apiUrl}/units`, {
        method: "POST",
        body: JSON.stringify(values),
      }),
    onSuccess: () => {
      message.success("Unidad creada exitosamente");
      queryClient.invalidateQueries({ queryKey: ["units"] });
      closeModal();
    },
    onError: () => message.error("Error al crear la unidad"),
  });

  const { mutate: updateUnit, isPending: updating } = useMutation({
    mutationFn: (values: { id: string; name: string; symbol: string }) =>
      fetchJson(`${apiUrl}/units/${values.id}`, {
        method: "PUT",
        body: JSON.stringify({ name: values.name, symbol: values.symbol }),
      }),
    onSuccess: () => {
      message.success("Unidad actualizada exitosamente");
      queryClient.invalidateQueries({ queryKey: ["units"] });
      closeModal();
    },
    onError: () => message.error("Error al actualizar la unidad"),
  });

  const { mutate: deleteUnit } = useMutation({
    mutationFn: (id: string) =>
      fetchJson(`${apiUrl}/units/${id}`, { method: "DELETE" }),
    onSuccess: () => {
      message.success("Unidad eliminada exitosamente");
      queryClient.invalidateQueries({ queryKey: ["units"] });
    },
    onError: (err: Error) =>
      message.error(
        err.message.includes("conversions")
          ? "No se puede eliminar: tiene conversiones asociadas"
          : "Error al eliminar la unidad"
      ),
  });

  const handleEdit = (unit: Unit) => {
    setEditingUnit(unit);
    form.setFieldsValue({
      name: unit.name,
      symbol: unit.symbol,
    });
    setOpen(true);
  };

  const closeModal = () => {
    setOpen(false);
    setEditingUnit(null);
    form.resetFields();
  };

  const handleSubmit = (values: { name: string; symbol: string }) => {
    if (editingUnit) {
      updateUnit({ id: editingUnit.id, ...values });
    } else {
      createUnit(values);
    }
  };

  const filteredUnits = units.filter(
    (unit) =>
      unit.name.toLowerCase().includes(searchText.toLowerCase()) ||
      unit.symbol.toLowerCase().includes(searchText.toLowerCase())
  );

  const columns = [
    {
      title: "Nombre",
      dataIndex: "name",
      key: "name",
      render: (text: string) => <Text strong>{text}</Text>,
    },
    {
      title: "Símbolo",
      dataIndex: "symbol",
      key: "symbol",
      render: (text: string) => (
        <Text
          keyboard
          style={{ backgroundColor: "#f0f2f5", padding: "4px 8px", borderRadius: "4px" }}
        >
          {text}
        </Text>
      ),
    },
    {
      title: "Acciones",
      key: "actions",
      align: "right" as const,
      render: (_: unknown, record: Unit) => (
        <Space size="middle">
          <Tooltip title="Editar">
            <Button
              type="text"
              icon={<EditOutlined style={{ color: "#1890ff" }} />}
              onClick={() => handleEdit(record)}
            />
          </Tooltip>
          <Tooltip title="Eliminar">
            <Popconfirm
              title="¿Eliminar esta unidad?"
              description={`Se eliminará "${record.name}" de forma permanente.`}
              onConfirm={() => deleteUnit(record.id)}
              okText="Sí, eliminar"
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
            <ExperimentOutlined style={{ color: "#1890ff" }} /> Unidades de Medida
          </Title>
          <Text type="secondary">Gestiona las unidades base y de conversión para tu inventario.</Text>
        </Col>
        <Col>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            size="large"
            onClick={() => setOpen(true)}
            style={{ borderRadius: "6px" }}
          >
            Nueva Unidad
          </Button>
        </Col>
      </Row>

      <Card
        bordered={false}
        style={{ borderRadius: "8px", boxShadow: "0 1px 2px -2px rgba(0,0,0,0.16), 0 3px 6px 0 rgba(0,0,0,0.12), 0 5px 12px 4px rgba(0,0,0,0.09)" }}
      >
        <div style={{ marginBottom: "16px", display: "flex", justifyContent: "flex-end" }}>
          <Input
            placeholder="Buscar por nombre o símbolo..."
            prefix={<SearchOutlined style={{ color: "rgba(0,0,0,.25)" }} />}
            style={{ width: 300, borderRadius: "6px" }}
            onChange={(e) => setSearchText(e.target.value)}
            allowClear
          />
        </div>

        <Table
          dataSource={filteredUnits}
          columns={columns}
          rowKey="id"
          loading={isLoading}
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            pageSizeOptions: ["10", "20", "50"],
            showTotal: (total) => `Total: ${total} unidades`,
          }}
          locale={{
            emptyText: searchText ? (
              <div style={{ padding: "40px 0" }}>
                <SearchOutlined style={{ fontSize: "48px", color: "#d9d9d9", marginBottom: "16px" }} />
                <Typography.Title level={5} style={{ color: "#8c8c8c" }}>
                  No se encontraron resultados para "{searchText}"
                </Typography.Title>
              </div>
            ) : (
              <div style={{ padding: "40px 0" }}>
                <ExperimentOutlined style={{ fontSize: "48px", color: "#d9d9d9", marginBottom: "16px" }} />
                <Typography.Title level={5} style={{ color: "#8c8c8c" }}>
                  Aún no tienes unidades de medida
                </Typography.Title>
                <Text type="secondary">
                  Comienza agregando tu primera unidad, como "kilo" o "unidad".
                </Text>
              </div>
            ),
          }}
        />
      </Card>

      <Modal
        title={
          <Space>
            {editingUnit ? <EditOutlined /> : <PlusOutlined />}
            {editingUnit ? "Editar Unidad" : "Nueva Unidad de Medida"}
          </Space>
        }
        open={open}
        onCancel={closeModal}
        onOk={() => form.submit()}
        okText={editingUnit ? "Guardar Cambios" : "Crear Unidad"}
        cancelText="Cancelar"
        confirmLoading={creating || updating}
        destroyOnClose
        width={480}
        centered
        style={{ padding: "24px" }}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          style={{ marginTop: "24px" }}
        >
          <Form.Item
            label={<Text strong>Nombre de la Unidad</Text>}
            name="name"
            rules={[
              { required: true, message: "Por favor, ingresa el nombre de la unidad" },
              { min: 2, message: "El nombre debe tener al menos 2 caracteres" },
            ]}
          >
            <Input placeholder="Ejemplo: Kilo, Caja, Litro, Unidad" size="large" />
          </Form.Item>
          
          <Form.Item
            label={<Text strong>Símbolo o Abreviatura</Text>}
            name="symbol"
            rules={[
              { required: true, message: "Por favor, ingresa el símbolo" },
              { max: 5, message: "El símbolo no debería ser mayor a 5 caracteres" },
            ]}
          >
            <Input placeholder="Ejemplo: kg, cx, L, un" size="large" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};
