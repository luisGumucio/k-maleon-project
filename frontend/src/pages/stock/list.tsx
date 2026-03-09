import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import {
  Alert,
  Button,
  Grid,
  Spin,
  Table,
  Typography,
  Card,
  Row,
  Col,
  Tag,
  Space,
  Input,
} from "antd";
import {
  PlusOutlined,
  SwapOutlined,
  WarningOutlined,
  AppstoreOutlined,
  BankOutlined,
  ShopOutlined,
  SearchOutlined,
  CheckCircleOutlined,
} from "@ant-design/icons";
import { useNavigate } from "react-router";
import { apiUrl, fetchJson } from "../../providers/data";
import type { ItemStock, StockLocationEntry } from "../../types/inventory";

const { Title, Text } = Typography;
const { useBreakpoint } = Grid;

// --- Sub-Tabla: Desglose por Ubicación ---
const StockLocationTable = ({ item }: { item: ItemStock }) => {
  const columns = [
    {
      title: "Ubicación",
      key: "location",
      render: (_: unknown, loc: StockLocationEntry) => (
        <Space>
          {loc.locationType === "warehouse" ? (
            <BankOutlined style={{ color: "#1890ff" }} />
          ) : (
            <ShopOutlined style={{ color: "#52c41a" }} />
          )}
          <Text strong>{loc.locationName}</Text>
          {loc.lowStock && (
            <Tag color="orange" icon={<WarningOutlined />}>
              Stock Bajo
            </Tag>
          )}
        </Space>
      ),
    },
    {
      title: "Cantidad Mínima",
      key: "min",
      render: (_: unknown, loc: StockLocationEntry) => (
        <Text type="secondary">
          {loc.minQuantity} {item.baseUnitSymbol}
        </Text>
      ),
    },
    {
      title: "Disponible",
      key: "qty",
      align: "right" as const,
      render: (_: unknown, loc: StockLocationEntry) => (
        <Text
          strong
          style={{ color: loc.lowStock ? "#faad14" : "inherit", fontSize: "15px" }}
        >
          {loc.quantity} {item.baseUnitSymbol}
        </Text>
      ),
    },
  ];

  return (
    <Card
      type="inner"
      style={{ margin: "16px", backgroundColor: "#fafafa" }}
      bodyStyle={{ padding: "16px" }}
    >
      <div style={{ marginBottom: "16px", padding: "8px 12px", backgroundColor: "#e6f7ff", borderRadius: "6px", border: "1px solid #91d5ff" }}>
        <Text>
          Total en la empresa:{" "}
          <Text strong style={{ fontSize: "16px" }}>
            {item.totalQuantity} {item.baseUnitSymbol}
          </Text>
        </Text>
      </div>
      <Table
        dataSource={item.locations}
        columns={columns}
        rowKey="locationId"
        pagination={false}
        size="small"
        bordered
      />
    </Card>
  );
};

// --- Vista Principal ---
export const StockList = () => {
  const screens = useBreakpoint();
  const isMobile = !screens.md;
  const navigate = useNavigate();
  const [searchText, setSearchText] = useState("");

  const { data: stockList = [], isLoading, isError } = useQuery<ItemStock[]>({
    queryKey: ["inventory-stock"],
    queryFn: () => fetchJson(`${apiUrl}/inventory/stock`),
  });

  if (isLoading) return <Spin size="large" style={{ display: "block", marginTop: 80 }} />;

  if (isError)
    return (
      <Alert
        type="error"
        message="No se pudo cargar el stock. Verifica que el backend esté activo."
        style={{ margin: 24 }}
      />
    );

  // Filtro de búsqueda
  const filteredStock = stockList.filter((item) =>
    item.itemName.toLowerCase().includes(searchText.toLowerCase())
  );

  const columns = [
    {
      title: "Producto",
      dataIndex: "itemName",
      key: "itemName",
      render: (text: string) => <Text strong>{text}</Text>,
    },
    {
      title: "Estado Global",
      key: "status",
      render: (_: unknown, record: ItemStock) => {
        const hasLowStock = record.locations.some((loc) => loc.lowStock);
        return hasLowStock ? (
          <Tag color="orange" icon={<WarningOutlined />}>
            Requiere Atención
          </Tag>
        ) : (
          <Tag color="green" icon={<CheckCircleOutlined />}>
            Normal
          </Tag>
        );
      },
    },
    {
      title: "Stock Total Empresa",
      key: "total",
      align: "right" as const,
      render: (_: unknown, record: ItemStock) => (
        <Tag color="blue" style={{ fontSize: "14px", padding: "4px 12px", borderRadius: "16px" }}>
          {record.totalQuantity} {record.baseUnitSymbol}
        </Tag>
      ),
    },
    {
      title: "Acciones Rapidas",
      key: "actions",
      align: "right" as const,
      render: (_: unknown, record: ItemStock) => (
        <Button
          type="dashed"
          icon={<SwapOutlined />}
          onClick={() => navigate(`/inventory/transfer?itemId=${record.itemId}`)}
        >
          Transferir
        </Button>
      ),
    },
  ];

  return (
    <div style={{ padding: "24px", maxWidth: "1200px", margin: "0 auto" }}>
      <Row justify="space-between" align="middle" style={{ marginBottom: "24px" }}>
        <Col>
          <Title level={3} style={{ margin: 0, display: "flex", alignItems: "center", gap: "10px" }}>
            <AppstoreOutlined style={{ color: "#1890ff" }} /> Monitor de Stock
          </Title>
          <Text type="secondary">Visualiza el inventario consolidado y desglosado por sucursal.</Text>
        </Col>
        <Col>
          <Space>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              size="large"
              onClick={() => navigate("/inventory/purchase")}
              style={{ borderRadius: "6px" }}
            >
              Comprar Mercadería
            </Button>
          </Space>
        </Col>
      </Row>

      <Card
        bordered={false}
        style={{ borderRadius: "8px", boxShadow: "0 1px 2px -2px rgba(0,0,0,0.16), 0 3px 6px 0 rgba(0,0,0,0.12), 0 5px 12px 4px rgba(0,0,0,0.09)" }}
      >
        <div style={{ marginBottom: "16px", display: "flex", justifyContent: "flex-end" }}>
          <Input
            placeholder="Buscar producto..."
            prefix={<SearchOutlined style={{ color: "rgba(0,0,0,.25)" }} />}
            style={{ width: 300, borderRadius: "6px" }}
            onChange={(e) => setSearchText(e.target.value)}
            allowClear
          />
        </div>

        <Table
          dataSource={filteredStock}
          columns={columns}
          rowKey="itemId"
          size="middle"
          pagination={{ pageSize: 20, showTotal: (total) => `Total ${total} productos` }}
          locale={{ 
            emptyText: searchText ? "No se encontraron resultados" : "No hay stock registrado. Comienza ingresando compras." 
          }}
          expandable={{
            expandedRowRender: (record: ItemStock) => <StockLocationTable item={record} />,
            expandRowByClick: false, // El usuario debe hacer clic en el [+]
            rowExpandable: (record: ItemStock) => record.locations.length > 0,
          }}
        />
      </Card>
    </div>
  );
};
