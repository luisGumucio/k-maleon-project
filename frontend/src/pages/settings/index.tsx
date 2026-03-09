import { Card, Typography } from "antd";
import { SettingOutlined } from "@ant-design/icons";

export const SettingsPage = () => {
  return (
    <div style={{ padding: 24 }}>
      <Card>
        <div style={{ textAlign: "center", padding: "40px 0" }}>
          <SettingOutlined style={{ fontSize: 48, color: "#8c8c8c", marginBottom: 16 }} />
          <Typography.Title level={4} type="secondary">
            Configuraciones — próximamente
          </Typography.Title>
        </div>
      </Card>
    </div>
  );
};
