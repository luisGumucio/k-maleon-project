import { Card, Col, Row, Statistic, Typography, Grid } from "antd";
import { InboxOutlined, ShopOutlined } from "@ant-design/icons";

const { useBreakpoint } = Grid;

export const InventoryDashboard = () => {
  const screens = useBreakpoint();
  const isMobile = !screens.md;

  return (
    <div style={{ padding: isMobile ? 16 : 24 }}>
      <Typography.Title level={isMobile ? 4 : 3} style={{ marginBottom: 24 }}>
        Dashboard de Inventario
      </Typography.Title>

      <Row gutter={[16, 16]}>
        <Col xs={24} md={12}>
          <Card size={isMobile ? "small" : "default"}>
            <Statistic
              title="Stock total (unidades)"
              value={0}
              prefix={<InboxOutlined />}
              valueStyle={{ fontSize: isMobile ? 24 : 32 }}
            />
          </Card>
        </Col>

        <Col xs={24} md={12}>
          <Card size={isMobile ? "small" : "default"}>
            <Statistic
              title="Sucursales activas"
              value={0}
              prefix={<ShopOutlined />}
              valueStyle={{ fontSize: isMobile ? 24 : 32 }}
            />
          </Card>
        </Col>
      </Row>
    </div>
  );
};
