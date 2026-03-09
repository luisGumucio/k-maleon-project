import { DateField, EditButton, List, useTable } from "@refinedev/antd";
import { useApiUrl, useCustomMutation, useInvalidate } from "@refinedev/core";
import {
  Table,
  Card,
  List as AntList,
  Typography,
  Grid,
  Tag,
  Space,
  Popconfirm,
  Button,
} from "antd";
import { StopOutlined, CheckCircleOutlined } from "@ant-design/icons";

type Supplier = {
  id: string;
  name: string;
  email?: string;
  phone?: string;
  active: boolean;
  created_at: string;
};

const { useBreakpoint } = Grid;

export const SupplierList = () => {
  const { tableProps } = useTable<Supplier>({ syncWithLocation: true });
  const screens = useBreakpoint();
  const isMobile = !screens.md;

  const apiUrl = useApiUrl();
  const { mutate: customMutate } = useCustomMutation();
  const invalidate = useInvalidate();

  const suppliers: Supplier[] = (tableProps.dataSource as Supplier[]) ?? [];

  const toggleActive = (id: string, currentlyActive: boolean) => {
    const action = currentlyActive ? "deactivate" : "activate";
    customMutate(
      {
        url: `${apiUrl}/suppliers/${id}/${action}`,
        method: "patch",
        values: {},
      },
      {
        onSuccess: () => {
          invalidate({ resource: "suppliers", invalidates: ["list"] });
        },
      }
    );
  };

  const renderToggleButton = (record: Supplier) =>
    record.active ? (
      <Popconfirm
        title="¿Desactivar este proveedor?"
        onConfirm={() => toggleActive(record.id, true)}
        okText="Sí"
        cancelText="No"
      >
        <Button size="small" danger icon={<StopOutlined />}>
          Desactivar
        </Button>
      </Popconfirm>
    ) : (
      <Popconfirm
        title="¿Activar este proveedor?"
        onConfirm={() => toggleActive(record.id, false)}
        okText="Sí"
        cancelText="No"
      >
        <Button size="small" icon={<CheckCircleOutlined />}>
          Activar
        </Button>
      </Popconfirm>
    );

  if (isMobile) {
    return (
      <List>
        <AntList
          loading={tableProps.loading as boolean}
          dataSource={suppliers}
          rowKey="id"
          renderItem={(item) => (
            <Card size="small" style={{ marginBottom: 8 }}>
              <Space style={{ width: "100%", justifyContent: "space-between" }}>
                <div>
                  <Typography.Text strong style={{ fontSize: 15 }}>
                    {item.name}
                  </Typography.Text>
                  <Tag
                    color={item.active ? "green" : "default"}
                    style={{ marginLeft: 8 }}
                  >
                    {item.active ? "Activo" : "Inactivo"}
                  </Tag>
                  {item.email && (
                    <>
                      <br />
                      <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                        {item.email}
                      </Typography.Text>
                    </>
                  )}
                  {item.phone && (
                    <>
                      <br />
                      <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                        {item.phone}
                      </Typography.Text>
                    </>
                  )}
                  <br />
                  <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                    Registrado:{" "}
                    <DateField value={item.created_at} format="DD/MM/YYYY" />
                  </Typography.Text>
                </div>
                <Space direction="vertical">
                  <EditButton hideText size="small" recordItemId={item.id} />
                  {renderToggleButton(item)}
                </Space>
              </Space>
            </Card>
          )}
        />
      </List>
    );
  }

  return (
    <List>
      <Table {...tableProps} rowKey="id">
        <Table.Column dataIndex="name" title="Nombre" />
        <Table.Column dataIndex="email" title="Email" />
        <Table.Column dataIndex="phone" title="Teléfono" />
        <Table.Column
          dataIndex="active"
          title="Estado"
          render={(value: boolean) => (
            <Tag color={value ? "green" : "default"}>
              {value ? "Activo" : "Inactivo"}
            </Tag>
          )}
        />
        <Table.Column
          dataIndex="created_at"
          title="Fecha de registro"
          render={(value) => <DateField value={value} format="DD/MM/YYYY" />}
        />
        <Table.Column
          title="Acciones"
          render={(_, record: Supplier) => (
            <Space>
              <EditButton hideText size="small" recordItemId={record.id} />
              {renderToggleButton(record)}
            </Space>
          )}
        />
      </Table>
    </List>
  );
};
