import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  Button,
  Form,
  Input,
  InputNumber,
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
  Tabs,
  Tooltip,
} from "antd";
import {
  DeleteOutlined,
  EditOutlined,
  PlusOutlined,
  SearchOutlined,
  AppstoreOutlined,
  SwapOutlined,
  WarningOutlined,
  SaveOutlined,
} from "@ant-design/icons";
import { apiUrl, fetchJson } from "../../providers/data";
import type { Item, Unit, UnitConversion, Location, ItemStock } from "../../types/inventory";

const { Title, Text } = Typography;

// --- Configuración de Stock Mínimo ---

const MinStockTable = ({ item }: { item: Item }) => {
  const queryClient = useQueryClient();

  // Traer todas las ubicaciones activas (bodegas y sucursales)
  const { data: locations = [], isLoading: loadingLocations } = useQuery<Location[]>({
    queryKey: ["locations"],
    queryFn: () => fetchJson(`${apiUrl}/locations`),
  });

  // Traer el stock actual para saber si ya hay registros de minQuantity
  const { data: stockList = [], isLoading: loadingStock } = useQuery<ItemStock[]>({
    queryKey: ["inventory-stock"],
    queryFn: () => fetchJson(`${apiUrl}/inventory/stock`),
  });

  const { mutate: updateMinQuantity, isPending: updating } = useMutation({
    mutationFn: (values: { locationId: string; minQuantity: number }) =>
      fetchJson(`${apiUrl}/inventory/items/${item.id}/min-quantity`, {
        method: "PUT",
        body: JSON.stringify(values),
      }),
    onSuccess: () => {
      message.success("Stock mínimo actualizado");
      queryClient.invalidateQueries({ queryKey: ["inventory-stock"] });
    },
    onError: () => message.error("Error al actualizar stock mínimo. Puede que no haya inventario inicial en esta ubicación."),
  });

  // Buscar si este item tiene ya un registro de stock para poder mostrar el minQuantity real
  const itemStockData = stockList.find((s) => s.itemId === item.id);

  // Mapeamos todas las locaciones para mostrar en la tabla
  const dataSource = locations.map((loc) => {
    // Si hay datos de stock para este item y esta locacion, sacamos el minQuantity
    const stockEntry = itemStockData?.locations.find((l) => l.locationId === loc.id);
    return {
      key: loc.id,
      locationId: loc.id,
      locationName: loc.name,
      locationType: loc.type,
      // Si no existe el entry en stock, asumimos 0 (o "No inicializado")
      currentMin: stockEntry ? stockEntry.minQuantity : 0,
      hasStockRecord: !!stockEntry,
    };
  });

  const columns = [
    {
      title: "Ubicación",
      key: "locationName",
      render: (_: unknown, r: any) => (
        <Space>
          <Tag color={r.locationType === "warehouse" ? "blue" : "green"}>
            {r.locationType === "warehouse" ? "BODEGA" : "SUCURSAL"}
          </Tag>
          <Text strong>{r.locationName}</Text>
        </Space>
      ),
    },
    {
      title: `Cantidad Mínima (${item.baseUnitSymbol})`,
      key: "minQuantity",
      render: (_: unknown, r: any) => {
        if (!r.hasStockRecord) {
          return (
            <Tooltip title="Debes ingresar al menos 1 movimiento de inventario en esta sucursal (ej: compra o ajuste a 0) antes de configurar un límite.">
              <Text type="secondary" italic>No inicializado</Text>
            </Tooltip>
          );
        }
        
        return (
          <Space>
            <InputNumber
              min={0}
              defaultValue={r.currentMin}
              onChange={(val) => {
                if (val !== null && val !== r.currentMin) {
                   updateMinQuantity({ locationId: r.locationId, minQuantity: Number(val) });
                }
              }}
              addonAfter={item.baseUnitSymbol}
            />
          </Space>
        );
      },
    },
  ];

  return (
    <Card 
      type="inner" 
      style={{ margin: "16px", backgroundColor: "#fafafa" }}
      bodyStyle={{ padding: "0" }}
    >
      <div style={{ padding: "16px", borderBottom: "1px solid #f0f0f0" }}>
        <Text strong><WarningOutlined style={{ color: "#faad14" }} /> Configuración de Alertas por Ubicación</Text>
        <div style={{ marginTop: "8px" }}>
          <Text type="secondary" style={{ fontSize: "13px" }}>
            El sistema te alertará en color naranja cuando el stock disponible caiga por debajo del límite que configures aquí.
          </Text>
        </div>
      </div>
      
      <Table
        dataSource={dataSource}
        columns={columns}
        rowKey="locationId"
        loading={loadingLocations || loadingStock || updating}
        size="small"
        pagination={false}
      />
    </Card>
  );
};

// --- Conversions sub-table ---

const ConversionsTable = ({ item }: { item: Item }) => {
  const queryClient = useQueryClient();
  const [form] = Form.useForm();
  const [open, setOpen] = useState(false);

  const { data: units = [] } = useQuery<Unit[]>({
    queryKey: ["units"],
    queryFn: () => fetchJson(`${apiUrl}/units`),
  });

  const { data: conversions = [], isLoading } = useQuery<UnitConversion[]>({
    queryKey: ["conversions", item.id],
    queryFn: () => fetchJson(`${apiUrl}/items/${item.id}/conversions`),
  });

  const { mutate: addConversion, isPending: adding } = useMutation({
    mutationFn: (values: { fromUnitId: string; toUnitId: string; factor: number }) =>
      fetchJson(`${apiUrl}/items/${item.id}/conversions`, {
        method: "POST",
        body: JSON.stringify(values),
      }),
    onSuccess: () => {
      message.success("Conversión agregada exitosamente");
      queryClient.invalidateQueries({ queryKey: ["conversions", item.id] });
      setOpen(false);
      form.resetFields();
    },
    onError: (err: Error) =>
      message.error(
        err.message.includes("already exists")
          ? "Ya existe una conversión para esa unidad"
          : "Error al agregar conversión"
      ),
  });

  const { mutate: deleteConversion } = useMutation({
    mutationFn: (convId: string) =>
      fetchJson(`${apiUrl}/items/${item.id}/conversions/${convId}`, {
        method: "DELETE",
      }),
    onSuccess: () => {
      message.success("Conversión eliminada exitosamente");
      queryClient.invalidateQueries({ queryKey: ["conversions", item.id] });
    },
    onError: () => message.error("Error al eliminar conversión"),
  });

  const unitOptions = units.map((u) => ({ label: `${u.name} (${u.symbol})`, value: u.id }));

  const columns = [
    {
      title: "Desde (Entrada)",
      key: "from",
      render: (_: unknown, r: UnitConversion) => (
        <Tag color="blue">{r.fromUnitName} ({r.fromUnitSymbol})</Tag>
      ),
    },
    {
      title: "",
      key: "icon",
      width: 50,
      render: () => <SwapOutlined style={{ color: "#bfbfbf" }} />,
    },
    {
      title: "Hacia (Base)",
      key: "to",
      render: (_: unknown, r: UnitConversion) => (
        <Tag color="cyan">{r.toUnitName} ({r.toUnitSymbol})</Tag>
      ),
    },
    { 
      title: "Factor de Multiplicación", 
      dataIndex: "factor", 
      key: "factor",
      render: (factor: number) => <Text strong>x {factor}</Text>
    },
    {
      title: "Acciones",
      key: "del",
      align: "right" as const,
      render: (_: unknown, r: UnitConversion) => (
        <Tooltip title="Eliminar regla">
          <Popconfirm
            title="¿Eliminar esta recela de conversión?"
            onConfirm={() => deleteConversion(r.id)}
            okText="Sí, eliminar"
            cancelText="Cancelar"
            okButtonProps={{ danger: true }}
          >
            <Button type="text" danger icon={<DeleteOutlined />} size="small" />
          </Popconfirm>
        </Tooltip>
      ),
    },
  ];

  return (
    <Card 
      type="inner" 
      title={<Text strong><SwapOutlined /> Reglas de Conversión para {item.name}</Text>}
      extra={
        <Button size="small" type="primary" ghost icon={<PlusOutlined />} onClick={() => setOpen(true)}>
          Nueva Regla
        </Button>
      }
      style={{ margin: "16px", backgroundColor: "#fafafa" }}
    >
      <Table
        dataSource={conversions}
        columns={columns}
        rowKey="id"
        loading={isLoading}
        size="small"
        pagination={false}
        locale={{ 
          emptyText: (
            <div style={{ padding: "20px 0" }}>
              <Text type="secondary">Este producto aún no tiene reglas de conversión configuradas.</Text>
            </div>
          ) 
        }}
      />

      <Modal
        title={
          <Space>
            <SwapOutlined /> Nueva Regla de Conversión
          </Space>
        }
        open={open}
        onCancel={() => { setOpen(false); form.resetFields(); }}
        onOk={() => form.submit()}
        okText="Guardar Regla"
        cancelText="Cancelar"
        confirmLoading={adding}
        destroyOnClose
        width={480}
        centered
      >
        <div style={{ backgroundColor: "#e6f7ff", padding: "12px", borderRadius: "6px", marginBottom: "20px", border: "1px solid #91d5ff" }}>
          <Text>
            La unidad base de almacenaje para <strong>{item.name}</strong> es{" "}
            <Tag color="blue" style={{ margin: "0 4px" }}>{item.baseUnitName} ({item.baseUnitSymbol})</Tag>
          </Text>
        </div>
        <Form form={form} layout="vertical" onFinish={addConversion}>
          <Form.Item
            label={<Text strong>Si el proveedor me vende en esta Unidad...</Text>}
            name="fromUnitId"
            rules={[{ required: true, message: "Por favor selecciona la unidad de entrada" }]}
          >
            <Select options={unitOptions} placeholder="Ej: Caja, Litro, Mapple" size="large" />
          </Form.Item>
          
          <Form.Item
            label={<Text strong>Se convertirá internamente a:</Text>}
            name="toUnitId"
            initialValue={item.baseUnitId}
            rules={[{ required: true, message: "Requerido" }]}
          >
            <Select options={unitOptions} disabled size="large" />
          </Form.Item>
          
          <Form.Item
            label={<Text strong>Una (1) unidad de entrada equivale a:</Text>}
            name="factor"
            rules={[{ required: true, message: "Por favor ingresa el factor numérico" }]}
            extra={<Text type="secondary" style={{ fontSize: "12px" }}>Ejemplo: Si compras por "Caja" y vienen "50 Unidades", el factor es 50.</Text>}
          >
            <InputNumber
              style={{ width: "100%" }}
              min={0.000001}
              precision={6}
              placeholder="Ej: 50"
              size="large"
            />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
};

// --- Sub-Container for Item Row Expansion ---
const ItemDetailsContainer = ({ item }: { item: Item }) => {
  return (
    <div style={{ padding: "0 24px 24px 24px", backgroundColor: "#fbfbfb" }}>
      <Tabs
        defaultActiveKey="1"
        items={[
          {
            key: "1",
            label: <span><SwapOutlined /> Conversiones de Medida</span>,
            children: <ConversionsTable item={item} />,
          },
          {
            key: "2",
            label: <span><WarningOutlined /> Stock Mínimo (Alertas)</span>,
            children: <MinStockTable item={item} />,
          },
        ]}
      />
    </div>
  );
};

// --- Main component ---

export const ItemList = () => {
  const queryClient = useQueryClient();
  const [form] = Form.useForm();
  
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Item | null>(null);
  const [searchText, setSearchText] = useState("");

  const { data: items = [], isLoading } = useQuery<Item[]>({
    queryKey: ["items"],
    queryFn: () => fetchJson(`${apiUrl}/items`),
  });

  const { data: units = [] } = useQuery<Unit[]>({
    queryKey: ["units"],
    queryFn: () => fetchJson(`${apiUrl}/units`),
  });

  const unitOptions = units.map((u) => ({
    label: `${u.name} (${u.symbol})`,
    value: u.id,
  }));

  const { mutate: saveItem, isPending: saving } = useMutation({
    mutationFn: (values: { name: string; baseUnitId: string; active?: boolean }) =>
      editing
        ? fetchJson(`${apiUrl}/items/${editing.id}`, {
            method: "PUT",
            body: JSON.stringify(values),
          })
        : fetchJson(`${apiUrl}/items`, {
            method: "POST",
            body: JSON.stringify(values),
          }),
    onSuccess: () => {
      message.success(editing ? "Producto actualizado exitosamente" : "Producto creado exitosamente");
      queryClient.invalidateQueries({ queryKey: ["items"] });
      handleClose();
    },
    onError: () => message.error("Error al guardar el producto"),
  });

  const { mutate: deleteItem } = useMutation({
    mutationFn: (id: string) =>
      fetchJson(`${apiUrl}/items/${id}`, { method: "DELETE" }),
    onSuccess: () => {
      message.success("Producto desactivado exitosamente (Soft Delete)");
      queryClient.invalidateQueries({ queryKey: ["items"] });
    },
    onError: () => message.error("Error al desactivar el producto"),
  });

  const handleClose = () => {
    setOpen(false);
    setEditing(null);
    form.resetFields();
  };

  const openEdit = (record: Item) => {
    setEditing(record);
    form.setFieldsValue({ 
      name: record.name, 
      baseUnitId: record.baseUnitId,
    });
    setOpen(true);
  };

  const filteredItems = items.filter((item) =>
    item.name.toLowerCase().includes(searchText.toLowerCase())
  );

  const columns = [
    { 
      title: "Nombre del Producto", 
      dataIndex: "name", 
      key: "name",
      render: (text: string) => <Text strong>{text}</Text>
    },
    {
      title: "Unidad Base Almacenaje",
      key: "baseUnit",
      render: (_: unknown, r: Item) => (
        <Tag color="blue" style={{ fontSize: "14px", padding: "4px 8px" }}>
          {r.baseUnitName} ({r.baseUnitSymbol})
        </Tag>
      ),
    },
    {
      title: "Estado",
      dataIndex: "active",
      key: "active",
      render: (active: boolean) => (
        <Tag color={active ? "green" : "red"}>{active ? "ACTIVO" : "INACTIVO"}</Tag>
      ),
    },
    {
      title: "Acciones",
      key: "actions",
      align: "right" as const,
      render: (_: unknown, record: Item) => (
        <Space size="middle">
          <Tooltip title="Editar Producto">
            <Button
              type="text"
              icon={<EditOutlined style={{ color: "#1890ff" }} />}
              onClick={() => openEdit(record)}
            />
          </Tooltip>
          {record.active && (
            <Tooltip title="Desactivar Producto">
              <Popconfirm
                title="¿Desactivar este producto?"
                description="No aparecerá más en nuevos movimientos de inventario."
                onConfirm={() => deleteItem(record.id)}
                okText="Sí, desactivar"
                cancelText="Cancelar"
                okButtonProps={{ danger: true }}
              >
                <Button type="text" danger icon={<DeleteOutlined />} />
              </Popconfirm>
            </Tooltip>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: "24px", maxWidth: "1200px", margin: "0 auto" }}>
      <Row justify="space-between" align="middle" style={{ marginBottom: "24px" }}>
        <Col>
          <Title level={3} style={{ margin: 0, display: "flex", alignItems: "center", gap: "10px" }}>
            <AppstoreOutlined style={{ color: "#1890ff" }} /> Catálogo de Productos
          </Title>
          <Text type="secondary">Gestiona los insumos, su unidad base y las alertas de inventario.</Text>
        </Col>
        <Col>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            size="large"
            onClick={() => setOpen(true)}
            style={{ borderRadius: "6px" }}
          >
            Nuevo Producto
          </Button>
        </Col>
      </Row>

      <Card
        bordered={false}
        style={{ borderRadius: "8px", boxShadow: "0 1px 2px -2px rgba(0,0,0,0.16), 0 3px 6px 0 rgba(0,0,0,0.12), 0 5px 12px 4px rgba(0,0,0,0.09)" }}
      >
        <div style={{ marginBottom: "16px", display: "flex", justifyContent: "flex-end" }}>
          <Input
            placeholder="Buscar por nombre de producto..."
            prefix={<SearchOutlined style={{ color: "rgba(0,0,0,.25)" }} />}
            style={{ width: 300, borderRadius: "6px" }}
            onChange={(e) => setSearchText(e.target.value)}
            allowClear
          />
        </div>

        <Table
          dataSource={filteredItems}
          columns={columns}
          rowKey="id"
          loading={isLoading}
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            pageSizeOptions: ["10", "20", "50"],
            showTotal: (total) => `Total: ${total} productos`,
          }}
          locale={{
            emptyText: searchText ? (
              <div style={{ padding: "40px 0" }}>
                <SearchOutlined style={{ fontSize: "48px", color: "#d9d9d9", marginBottom: "16px" }} />
                <Typography.Title level={5} style={{ color: "#8c8c8c" }}>
                  No se encontraron productos para "{searchText}"
                </Typography.Title>
              </div>
            ) : (
              <div style={{ padding: "40px 0" }}>
                <AppstoreOutlined style={{ fontSize: "48px", color: "#d9d9d9", marginBottom: "16px" }} />
                <Typography.Title level={5} style={{ color: "#8c8c8c" }}>
                  Aún no tienes productos en tu catálogo
                </Typography.Title>
                <Text type="secondary">
                  Comienza agregando tu primer producto e indicando en qué unidad lo almacenarás.
                </Text>
              </div>
            ),
          }}
          expandable={{
            expandedRowRender: (record: Item) => <ItemDetailsContainer item={record} />,
            expandRowByClick: false,
          }}
        />
      </Card>

      <Modal
        title={
          <Space>
            {editing ? <EditOutlined /> : <PlusOutlined />}
            {editing ? "Editar Producto" : "Nuevo Producto en Catálogo"}
          </Space>
        }
        open={open}
        onCancel={handleClose}
        onOk={() => form.submit()}
        okText={editing ? "Guardar Cambios" : "Crear Producto"}
        cancelText="Cancelar"
        confirmLoading={saving}
        destroyOnClose
        width={500}
        centered
        style={{ padding: "24px" }}
      >
        <Form 
          form={form} 
          layout="vertical" 
          onFinish={saveItem}
          style={{ marginTop: "24px" }}
        >
          <Form.Item
            label={<Text strong>Nombre del Producto</Text>}
            name="name"
            rules={[
              { required: true, message: "Por favor, ingresa el nombre" },
              { min: 3, message: "Debe tener al menos 3 caracteres" }
            ]}
          >
            <Input placeholder="Ejemplo: Arroz Grano Largo, Cerveza Paceña Lata" size="large" />
          </Form.Item>
          
          <Form.Item
            label={<Text strong>Unidad Base (Almacenaje interno)</Text>}
            name="baseUnitId"
            rules={[{ required: true, message: "Debes seleccionar una unidad base" }]}
            extra={<Text type="secondary" style={{ fontSize: "12px" }}>¡Importante! Todo el stock de este producto se contará internamente en esta unidad matemática.</Text>}
          >
            <Select 
              options={unitOptions} 
              placeholder="Selecciona la menor unidad posible (Ej: Gramo, Unidad, Mililitro)" 
              size="large" 
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};
