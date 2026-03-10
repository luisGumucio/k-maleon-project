import { Card, Col, Row, Typography, Grid } from "antd";
import {
  DollarOutlined,
  InboxOutlined,
  ShopOutlined,
} from "@ant-design/icons";
import { useRole } from "../../contexts/role";

const { useBreakpoint } = Grid;

export const SuperAdminDashboard = () => {
  const screens = useBreakpoint();
  const isMobile = !screens.md;
  const { setViewingAs } = useRole();

  const cardStyle: React.CSSProperties = {
    cursor: "pointer",
    textAlign: "center",
    borderRadius: 12,
    transition: "box-shadow 0.2s",
  };

  return (
    <div style={{ padding: isMobile ? 16 : 40 }}>
      <Typography.Title
        level={isMobile ? 4 : 2}
        style={{ marginBottom: 8, textAlign: "center" }}
      >
        Panel Central
      </Typography.Title>
      <Typography.Paragraph
        type="secondary"
        style={{ textAlign: "center", marginBottom: 40 }}
      >
        Selecciona el panel que deseas administrar
      </Typography.Paragraph>

      <Row gutter={[24, 24]} justify="center">
        <Col xs={24} sm={12} md={10} lg={8}>
          <Card
            hoverable
            style={cardStyle}
            onClick={() => setViewingAs("admin")}
          >
            <DollarOutlined
              style={{ fontSize: 48, color: "#1677ff", marginBottom: 16 }}
            />
            <Typography.Title level={4} style={{ marginBottom: 4 }}>
              Panel de Transacciones
            </Typography.Title>
            <Typography.Text type="secondary">
              Operaciones, movimientos, cuenta y proveedores
            </Typography.Text>
          </Card>
        </Col>

        <Col xs={24} sm={12} md={10} lg={8}>
          <Card
            hoverable
            style={cardStyle}
            onClick={() => setViewingAs("inventory_admin")}
          >
            <InboxOutlined
              style={{ fontSize: 48, color: "#52c41a", marginBottom: 16 }}
            />
            <Typography.Title level={4} style={{ marginBottom: 4 }}>
              Panel de Inventario
            </Typography.Title>
            <Typography.Text type="secondary">
              Bodegas, sucursales, stock y almaceneros
            </Typography.Text>
          </Card>
        </Col>

        <Col xs={24} sm={12} md={10} lg={8}>
          <Card
            hoverable
            style={cardStyle}
            onClick={() => setViewingAs("almacenero")}
          >
            <ShopOutlined
              style={{ fontSize: 48, color: "#fa8c16", marginBottom: 16 }}
            />
            <Typography.Title level={4} style={{ marginBottom: 4 }}>
              Panel de Almacenero
            </Typography.Title>
            <Typography.Text type="secondary">
              Compras, transferencias y solicitudes de sucursales
            </Typography.Text>
          </Card>
        </Col>
      </Row>
    </div>
  );
};
