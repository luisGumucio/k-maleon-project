import { useQuery } from "@tanstack/react-query";
import { Table, Tag, Button, Tooltip, Card, List, Typography, Grid } from "antd";
import { FileTextOutlined } from "@ant-design/icons";
import { DateField } from "@refinedev/antd";
import { Movement, MovementType } from "../../types/movement";
import { formatUSD } from "../../utils/money";
import { apiUrl, fetchJson } from "../../providers/data";

const { useBreakpoint } = Grid;

const TYPE_COLOR: Record<MovementType, string> = {
  entrada: "green",
  salida: "red",
};

const TYPE_LABEL: Record<MovementType, string> = {
  entrada: "Entrada",
  salida: "Salida",
};

type Props = {
  operationId: string;
};

export const MovementTable = ({ operationId }: Props) => {
  const { data, isLoading } = useQuery<Movement[]>({
    queryKey: ["movements", operationId],
    queryFn: () => fetchJson(`${apiUrl}/operations/${operationId}/movements`),
  });

  const screens = useBreakpoint();
  const isMobile = !screens.md;
  const movements = (data ?? []).slice().sort(
    (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
  );

  if (isMobile) {
    return (
      <List
        loading={isLoading}
        dataSource={movements}
        rowKey="id"
        renderItem={(m) => (
          <Card
            size="small"
            style={{ marginBottom: 8 }}
            styles={{ body: { padding: "10px 12px" } }}
          >
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
              <Tag color={TYPE_COLOR[m.type]}>{TYPE_LABEL[m.type]}</Tag>
              <Typography.Text
                strong
                style={{ color: m.type === "salida" ? "#ff4d4f" : "#52c41a", fontSize: 15 }}
              >
                {m.type === "salida" ? "-" : "+"}{formatUSD(m.amount)}
              </Typography.Text>
            </div>
            <div style={{ marginTop: 6 }}>
              <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                {m.date}
                {m.paymentType ? ` · ${m.paymentType.toUpperCase()}` : ""}
              </Typography.Text>
              {m.description && (
                <>
                  <br />
                  <Typography.Text style={{ fontSize: 12 }}>{m.description}</Typography.Text>
                </>
              )}
              {m.attachmentUrl && (
                <>
                  <br />
                  <Button
                    type="link"
                    size="small"
                    icon={<FileTextOutlined />}
                    href={m.attachmentUrl}
                    target="_blank"
                    style={{ padding: 0, fontSize: 12 }}
                  >
                    Ver comprobante
                  </Button>
                </>
              )}
            </div>
          </Card>
        )}
      />
    );
  }

  return (
    <Table dataSource={movements} loading={isLoading} rowKey="id" pagination={false}>
      <Table.Column
        dataIndex="date"
        title="Fecha"
        render={(v, record: Movement) => (
          <div>
            <div>{v}</div>
            <Typography.Text type="secondary" style={{ fontSize: 11 }}>
              <DateField value={record.createdAt} format="HH:mm" />
            </Typography.Text>
          </div>
        )}
      />
      <Table.Column
        dataIndex="type"
        title="Tipo"
        render={(v: MovementType) => (
          <Tag color={TYPE_COLOR[v]}>{TYPE_LABEL[v] ?? v}</Tag>
        )}
      />
      <Table.Column dataIndex="paymentType" title="Método de pago" render={(v) => v ?? "—"} />
      <Table.Column
        dataIndex="amount"
        title="Monto"
        render={(v, record: Movement) => (
          <span style={{ color: record.type === "salida" ? "#ff4d4f" : "#52c41a", fontWeight: 500 }}>
            {record.type === "salida" ? "-" : "+"}{formatUSD(v)}
          </span>
        )}
      />
      <Table.Column dataIndex="description" title="Descripción" render={(v) => v ?? "—"} />
      <Table.Column
        dataIndex="attachmentUrl"
        title="Comprobante"
        render={(url) =>
          url ? (
            <Tooltip title="Ver comprobante">
              <Button type="link" icon={<FileTextOutlined />} href={url} target="_blank" size="small">
                Ver PDF
              </Button>
            </Tooltip>
          ) : "—"
        }
      />
    </Table>
  );
};
