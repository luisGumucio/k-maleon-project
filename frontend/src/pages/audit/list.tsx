import { useQuery } from "@tanstack/react-query";
import {
  Table,
  Tag,
  Button,
  Modal,
  Form,
  Input,
  DatePicker,
  Row,
  Col,
  Typography,
  Tooltip,
} from "antd";
import { FileSearchOutlined } from "@ant-design/icons";
import { useState } from "react";
import { apiUrl, fetchJson } from "../../providers/data";
import dayjs from "dayjs";

type AuditEntry = {
  id: string;
  action: string;
  entityName: string;
  entityId: string | null;
  userId: string | null;
  payload: string | null;
  createdAt: string;
};

type Filters = {
  action?: string;
  entity?: string;
  from?: string;
  to?: string;
};

const ACTION_COLOR: Record<string, string> = {
  movement_created: "blue",
  operation_created: "green",
  operation_updated: "orange",
  balance_updated: "purple",
};

export const AuditList = () => {
  const [filters, setFilters] = useState<Filters>({});
  const [payloadModal, setPayloadModal] = useState<string | null>(null);
  const [form] = Form.useForm();

  const { data, isLoading } = useQuery<AuditEntry[]>({
    queryKey: ["audit-log", filters],
    queryFn: () => {
      const params = new URLSearchParams();
      if (filters.action) params.append("action", filters.action);
      if (filters.entity) params.append("entity", filters.entity);
      if (filters.from) params.append("from", filters.from);
      if (filters.to) params.append("to", filters.to);
      const query = params.toString() ? `?${params.toString()}` : "";
      return fetchJson(`${apiUrl}/audit-log${query}`);
    },
  });

  const onFilter = (values: {
    action?: string;
    entity?: string;
    dateRange?: [dayjs.Dayjs, dayjs.Dayjs];
  }) => {
    setFilters({
      action: values.action || undefined,
      entity: values.entity || undefined,
      from: values.dateRange?.[0]?.toISOString(),
      to: values.dateRange?.[1]?.toISOString(),
    });
  };

  const onClear = () => {
    form.resetFields();
    setFilters({});
  };

  const formatPayload = (raw: string | null) => {
    if (!raw) return "—";
    try {
      return JSON.stringify(JSON.parse(raw), null, 2);
    } catch {
      return raw;
    }
  };

  return (
    <div style={{ padding: 24 }}>
      <Typography.Title level={3} style={{ marginBottom: 24 }}>
        Audit Log
      </Typography.Title>

      <Form form={form} onFinish={onFilter} layout="inline" style={{ marginBottom: 16 }}>
        <Row gutter={[8, 8]} style={{ width: "100%" }}>
          <Col>
            <Form.Item name="action" noStyle>
              <Input placeholder="Acción" allowClear style={{ width: 180 }} />
            </Form.Item>
          </Col>
          <Col>
            <Form.Item name="entity" noStyle>
              <Input placeholder="Entidad" allowClear style={{ width: 160 }} />
            </Form.Item>
          </Col>
          <Col>
            <Form.Item name="dateRange" noStyle>
              <DatePicker.RangePicker showTime format="DD/MM/YYYY HH:mm" />
            </Form.Item>
          </Col>
          <Col>
            <Button type="primary" htmlType="submit">
              Filtrar
            </Button>
          </Col>
          <Col>
            <Button onClick={onClear}>Limpiar</Button>
          </Col>
        </Row>
      </Form>

      <Table
        dataSource={data ?? []}
        loading={isLoading}
        rowKey="id"
        pagination={{ pageSize: 20 }}
      >
        <Table.Column
          dataIndex="createdAt"
          title="Fecha"
          render={(v) => dayjs(v).format("DD/MM/YYYY HH:mm:ss")}
          width={160}
        />
        <Table.Column
          dataIndex="action"
          title="Acción"
          render={(v: string) => (
            <Tag color={ACTION_COLOR[v] ?? "default"}>{v}</Tag>
          )}
        />
        <Table.Column dataIndex="entityName" title="Entidad" />
        <Table.Column
          dataIndex="entityId"
          title="ID de entidad"
          render={(v) =>
            v ? (
              <Tooltip title={v}>
                <Typography.Text code style={{ fontSize: 11 }}>
                  {v.slice(0, 8)}…
                </Typography.Text>
              </Tooltip>
            ) : (
              "—"
            )
          }
        />
        <Table.Column
          dataIndex="userId"
          title="Usuario"
          render={(v) => v ?? "—"}
        />
        <Table.Column
          dataIndex="payload"
          title="Payload"
          render={(v) =>
            v ? (
              <Button
                type="link"
                size="small"
                icon={<FileSearchOutlined />}
                onClick={() => setPayloadModal(v)}
              >
                Ver
              </Button>
            ) : (
              "—"
            )
          }
        />
      </Table>

      <Modal
        title="Payload"
        open={payloadModal !== null}
        onCancel={() => setPayloadModal(null)}
        footer={null}
        width={700}
      >
        <pre
          style={{
            background: "#f5f5f5",
            padding: 16,
            borderRadius: 6,
            maxHeight: 500,
            overflow: "auto",
            fontSize: 12,
          }}
        >
          {formatPayload(payloadModal)}
        </pre>
      </Modal>
    </div>
  );
};
