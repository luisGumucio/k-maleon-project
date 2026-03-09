import { DateField, List, useTable } from "@refinedev/antd";
import { Table, Card, List as AntList, Typography, Grid } from "antd";

type Supplier = {
  id: string;
  name: string;
  created_at: string;
};

const { useBreakpoint } = Grid;

export const SupplierList = () => {
  const { tableProps } = useTable<Supplier>({ syncWithLocation: true });
  const screens = useBreakpoint();
  const isMobile = !screens.md;

  const suppliers: Supplier[] = (tableProps.dataSource as Supplier[]) ?? [];

  if (isMobile) {
    return (
      <List>
        <AntList
          loading={tableProps.loading as boolean}
          dataSource={suppliers}
          rowKey="id"
          renderItem={(item) => (
            <Card size="small" style={{ marginBottom: 8 }}>
              <Typography.Text strong style={{ fontSize: 15 }}>
                {item.name}
              </Typography.Text>
              <br />
              <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                Registrado:{" "}
                <DateField value={item.created_at} format="DD/MM/YYYY" />
              </Typography.Text>
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
        <Table.Column
          dataIndex="created_at"
          title="Fecha de registro"
          render={(value) => <DateField value={value} format="DD/MM/YYYY" />}
        />
      </Table>
    </List>
  );
};
