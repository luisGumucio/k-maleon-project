import { useQuery } from "@tanstack/react-query";
import { Table, Card, List, Typography, Empty, Grid } from "antd";
import { DateField } from "@refinedev/antd";
import { apiUrl, fetchJson } from "../../providers/data";
import { formatUSD } from "../../utils/money";

type AccountDeposit = {
  id: string;
  accountId: string;
  amount: number;
  description: string | null;
  date: string;
  createdAt: string;
};

const { useBreakpoint } = Grid;

export const DepositHistory = () => {
  const { data, isLoading } = useQuery<AccountDeposit[]>({
    queryKey: ["account-deposits"],
    queryFn: () => fetchJson(`${apiUrl}/account/deposits`),
  });

  const screens = useBreakpoint();
  const isMobile = !screens.md;
  const deposits = data ?? [];

  if (!isLoading && deposits.length === 0) {
    return (
      <Card title="Historial de depósitos" size={isMobile ? "small" : "default"}>
        <Empty description="Sin depósitos registrados" />
      </Card>
    );
  }

  if (isMobile) {
    return (
      <Card title="Historial de depósitos" size="small">
        <List
          loading={isLoading}
          dataSource={deposits}
          rowKey="id"
          renderItem={(d) => (
            <Card size="small" style={{ marginBottom: 8 }} styles={{ body: { padding: "10px 12px" } }}>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <Typography.Text strong style={{ color: "#52c41a", fontSize: 15 }}>
                  +{formatUSD(d.amount)}
                </Typography.Text>
                <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                  {d.date}
                </Typography.Text>
              </div>
              {d.description && (
                <Typography.Text style={{ fontSize: 12, display: "block", marginTop: 4 }}>
                  {d.description}
                </Typography.Text>
              )}
            </Card>
          )}
        />
      </Card>
    );
  }

  return (
    <Card title="Historial de depósitos" size="default">
      <Table dataSource={deposits} loading={isLoading} rowKey="id" pagination={false}>
        <Table.Column
          dataIndex="date"
          title="Fecha"
          render={(v, record: AccountDeposit) => (
            <div>
              <div>{v}</div>
              <Typography.Text type="secondary" style={{ fontSize: 11 }}>
                <DateField value={record.createdAt} format="HH:mm" />
              </Typography.Text>
            </div>
          )}
        />
        <Table.Column
          dataIndex="amount"
          title="Monto"
          render={(v) => (
            <span style={{ color: "#52c41a", fontWeight: 500 }}>+{formatUSD(v)}</span>
          )}
        />
        <Table.Column
          dataIndex="description"
          title="Descripción"
          render={(v) => v ?? "—"}
        />
      </Table>
    </Card>
  );
};
