import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  Card,
  Statistic,
  Button,
  Modal,
  InputNumber,
  Form,
  Typography,
  Alert,
  Spin,
  message,
  Grid,
  Row,
  Col,
} from "antd";
import { DollarOutlined, SettingOutlined } from "@ant-design/icons";
import { useState } from "react";
import { apiUrl, fetchJson } from "../../providers/data";
import { formatUSD, dollarsToCents } from "../../utils/money";
import { DepositModal } from "./DepositModal";
import { DepositHistory } from "./DepositHistory";

const { useBreakpoint } = Grid;

type AccountSummary = {
  id: string;
  balance: number;
  totalDeposits: number;
  totalEntradas: number;
  totalSalidas: number;
  updatedAt: string;
};

export const AccountBalance = () => {
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm();
  const queryClient = useQueryClient();
  const screens = useBreakpoint();
  const isMobile = !screens.md;

  const { data, isLoading, isError } = useQuery<AccountSummary>({
    queryKey: ["account-summary"],
    queryFn: () => fetchJson(`${apiUrl}/account/summary`),
  });

  const { mutate: setInitialBalance, isPending } = useMutation({
    mutationFn: (amountInCents: number) =>
      fetchJson(`${apiUrl}/account/initial-balance`, {
        method: "POST",
        body: JSON.stringify(amountInCents),
      }),
    onSuccess: () => {
      message.success("Saldo inicial configurado correctamente");
      queryClient.invalidateQueries({ queryKey: ["account-summary"] });
      setModalOpen(false);
      form.resetFields();
    },
    onError: () => {
      message.error("Error al configurar el saldo inicial");
    },
  });

  const handleSubmit = ({ amount }: { amount: number }) => {
    setInitialBalance(dollarsToCents(amount));
  };

  if (isLoading) return <Spin size="large" style={{ display: "block", marginTop: 80 }} />;

  if (isError) {
    return (
      <Alert
        type="error"
        message="No se pudo cargar el saldo. Verifica que el backend esté activo."
        style={{ margin: 24 }}
      />
    );
  }

  return (
    <div style={{ padding: isMobile ? 16 : 24 }}>
      <Row justify="space-between" align="middle" style={{ marginBottom: 24 }} wrap>
        <Col>
          <Typography.Title level={isMobile ? 4 : 3} style={{ margin: 0 }}>
            Cuenta general
          </Typography.Title>
        </Col>
        <Col>
          <Row gutter={8} wrap={false}>
            <Col>
              <DepositModal />
            </Col>
            <Col>
              <Button
                icon={<SettingOutlined />}
                onClick={() => setModalOpen(true)}
                size={isMobile ? "small" : "middle"}
              >
                {isMobile ? "" : "Saldo inicial"}
              </Button>
            </Col>
          </Row>
        </Col>
      </Row>

      <Card size={isMobile ? "small" : "default"} style={{ marginBottom: 24 }}>
        <Row gutter={[16, 16]}>
          <Col xs={24} sm={12}>
            <Statistic
              title="Saldo actual"
              value={formatUSD(data?.balance)}
              prefix={<DollarOutlined />}
              valueStyle={{
                fontSize: isMobile ? 22 : 32,
                color: (data?.balance ?? 0) >= 0 ? "#52c41a" : "#ff4d4f",
              }}
            />
          </Col>
          <Col xs={24} sm={12}>
            <Statistic
              title="Total depositado"
              value={formatUSD(data?.totalDeposits)}
              valueStyle={{ fontSize: isMobile ? 18 : 26, color: "#1677ff" }}
            />
          </Col>
        </Row>
        {data?.updatedAt && (
          <Typography.Text type="secondary" style={{ marginTop: 12, display: "block", fontSize: 12 }}>
            Última actualización: {new Date(data.updatedAt).toLocaleString("es-CL")}
          </Typography.Text>
        )}
      </Card>

      <DepositHistory />

      <Modal
        title="Configurar saldo inicial"
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={() => form.submit()}
        okText="Guardar"
        cancelText="Cancelar"
        confirmLoading={isPending}
        width={isMobile ? "100%" : 420}
        destroyOnClose
      >
        <Alert
          type="warning"
          showIcon
          message="Esta acción solo debe realizarse una vez al iniciar el sistema."
          style={{ marginBottom: 16 }}
        />
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item
            label="Saldo inicial (USD)"
            name="amount"
            rules={[{ required: true, message: "Ingresa el monto" }]}
          >
            <InputNumber
              style={{ width: "100%" }}
              min={0}
              precision={2}
              prefix="$"
              placeholder="0.00"
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};
