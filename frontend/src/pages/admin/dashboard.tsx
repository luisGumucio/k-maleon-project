import { useQuery } from "@tanstack/react-query";
import { Card, Col, Row, Statistic, Typography, Spin, Alert, Grid } from "antd";
import { DollarOutlined, FileTextOutlined } from "@ant-design/icons";
import { apiUrl, fetchJson } from "../../providers/data";
import { formatUSD } from "../../utils/money";

const { useBreakpoint } = Grid;

type AccountBalance = {
  id: string;
  balance: number;
  updatedAt: string;
};

type OperationSummary = {
  total: number;
  active: number;
};

export const AdminDashboard = () => {
  const screens = useBreakpoint();
  const isMobile = !screens.md;

  const { data: account, isLoading: loadingAccount } = useQuery<AccountBalance>({
    queryKey: ["account-balance"],
    queryFn: () => fetchJson(`${apiUrl}/account/balance`),
  });

  const { data: operations, isLoading: loadingOps } = useQuery<OperationSummary>({
    queryKey: ["operations-summary"],
    queryFn: async () => {
      const res = await fetchJson(
        `${apiUrl}/operations?page=0&size=1&status=active`
      );
      return { total: res.total ?? 0, active: res.total ?? 0 };
    },
  });

  const isLoading = loadingAccount || loadingOps;

  if (isLoading)
    return <Spin size="large" style={{ display: "block", marginTop: 80 }} />;

  return (
    <div style={{ padding: isMobile ? 16 : 24 }}>
      <Typography.Title level={isMobile ? 4 : 3} style={{ marginBottom: 24 }}>
        Dashboard
      </Typography.Title>

      <Row gutter={[16, 16]}>
        <Col xs={24} md={12}>
          <Card size={isMobile ? "small" : "default"}>
            <Statistic
              title="Saldo de cuenta"
              value={formatUSD(account?.balance)}
              prefix={<DollarOutlined />}
              valueStyle={{
                color: (account?.balance ?? 0) >= 0 ? "#52c41a" : "#ff4d4f",
                fontSize: isMobile ? 24 : 32,
              }}
            />
            {account?.updatedAt && (
              <Typography.Text
                type="secondary"
                style={{ fontSize: 12, marginTop: 4, display: "block" }}
              >
                Actualizado:{" "}
                {new Date(account.updatedAt).toLocaleString("es-CL")}
              </Typography.Text>
            )}
          </Card>
        </Col>

        <Col xs={24} md={12}>
          <Card size={isMobile ? "small" : "default"}>
            <Statistic
              title="Operaciones activas"
              value={operations?.active ?? 0}
              prefix={<FileTextOutlined />}
              valueStyle={{ fontSize: isMobile ? 24 : 32 }}
            />
          </Card>
        </Col>
      </Row>
    </div>
  );
};
