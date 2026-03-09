import type { RefineThemedLayoutHeaderProps } from "@refinedev/antd";
import { useGetIdentity, useLogout } from "@refinedev/core";
import {
  Layout as AntdLayout,
  Avatar,
  Button,
  Popconfirm,
  Space,
  Switch,
  Tag,
  theme,
  Typography,
} from "antd";
import { LogoutOutlined } from "@ant-design/icons";
import React, { useContext } from "react";
import { ColorModeContext } from "../../contexts/color-mode";

const { Text } = Typography;
const { useToken } = theme;

type IUser = {
  name: string;
  role: string;
};

const ROLE_LABELS: Record<string, string> = {
  super_admin: "Super Admin",
  admin: "Admin",
  inventory_admin: "Inv. Admin",
  almacenero: "Almacenero",
  encargado_sucursal: "Encargado",
};

export const Header: React.FC<RefineThemedLayoutHeaderProps> = ({
  sticky = true,
}) => {
  const { token } = useToken();
  const { data: user } = useGetIdentity<IUser>();
  const { mode, setMode } = useContext(ColorModeContext);
  const { mutate: logout } = useLogout();

  const headerStyles: React.CSSProperties = {
    backgroundColor: token.colorBgElevated,
    display: "flex",
    justifyContent: "flex-end",
    alignItems: "center",
    padding: "0px 24px",
    height: "64px",
  };

  if (sticky) {
    headerStyles.position = "sticky";
    headerStyles.top = 0;
    headerStyles.zIndex = 1;
  }

  return (
    <AntdLayout.Header style={headerStyles}>
      <Space>
        <Switch
          checkedChildren="🌛"
          unCheckedChildren="🔆"
          onChange={() => setMode(mode === "light" ? "dark" : "light")}
          defaultChecked={mode === "dark"}
        />
        <Space style={{ marginLeft: "8px" }} size="middle">
          {user?.name && <Text strong>{user.name}</Text>}
          {user?.role && (
            <Tag color="blue">{ROLE_LABELS[user.role] ?? user.role}</Tag>
          )}
          <Avatar style={{ backgroundColor: token.colorPrimary }}>
            {user?.name?.[0]?.toUpperCase() ?? "U"}
          </Avatar>
          <Popconfirm
            title="¿Cerrar sesión?"
            onConfirm={() => logout()}
            okText="Sí"
            cancelText="No"
          >
            <Button icon={<LogoutOutlined />} type="text" danger />
          </Popconfirm>
        </Space>
      </Space>
    </AntdLayout.Header>
  );
};
