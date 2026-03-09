import { useQuery } from "@tanstack/react-query";
import {
  Button,
  Card,
  Col,
  DatePicker,
  Form,
  Grid,
  Input,
  List as AntList,
  Row,
  Select,
  Space,
  Table,
  Typography,
} from "antd";
import { EyeOutlined } from "@ant-design/icons";
import { List } from "@refinedev/antd";
import { useNavigate } from "react-router";
import { useState } from "react";
import { Shipment } from "../../types/shipment";
import { ShipmentDetail } from "../../types/shipment-item";
import { apiUrl, fetchJson } from "../../providers/data";

const { useBreakpoint } = Grid;

type Filters = {
  supplierId?: string;
  containerNumber?: string;
  from?: string;
  to?: string;
};

function buildQuery(filters: Filters): string {
  const params = new URLSearchParams();
  if (filters.supplierId) params.append("supplierId", filters.supplierId);
  if (filters.containerNumber) params.append("containerNumber", filters.containerNumber);
  if (filters.from) params.append("from", filters.from);
  if (filters.to) params.append("to", filters.to);
  const q = params.toString();
  return q ? `?${q}` : "";
}

function useShipmentDetails(shipments: Shipment[]) {
  return useQuery<Record<string, ShipmentDetail>>({
    queryKey: ["shipment-details-summary", shipments.map((s) => s.id)],
    queryFn: async () => {
      const results = await Promise.all(
        shipments.map((s) =>
          fetchJson(`${apiUrl}/shipment-items?shipmentId=${s.id}`).then(
            (d: ShipmentDetail) => [s.id, d] as [string, ShipmentDetail]
          )
        )
      );
      return Object.fromEntries(results);
    },
    enabled: shipments.length > 0,
  });
}

function formatAmount(v: number | null | undefined): string {
  if (v == null) return "—";
  return new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" }).format(v);
}

export const ShipmentItemList = () => {
  const screens = useBreakpoint();
  const isMobile = !screens.md;
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [filters, setFilters] = useState<Filters>({});

  const { data: suppliers = [] } = useQuery<{ id: string; name: string }[]>({
    queryKey: ["suppliers-select"],
    queryFn: () => fetchJson(`${apiUrl}/suppliers`),
  });

  const { data: shipments = [], isLoading } = useQuery<Shipment[]>({
    queryKey: ["shipments-all", filters],
    queryFn: () => fetchJson(`${apiUrl}/shipments${buildQuery(filters)}`),
  });

  const { data: detailsMap = {} } = useShipmentDetails(shipments);

  const onFilter = (values: {
    supplierId?: string;
    containerNumber?: string;
    dateRange?: [{ format: (f: string) => string } | null, { format: (f: string) => string } | null];
  }) => {
    setFilters({
      supplierId: values.supplierId,
      containerNumber: values.containerNumber || undefined,
      from: values.dateRange?.[0]?.format("YYYY-MM-DD"),
      to: values.dateRange?.[1]?.format("YYYY-MM-DD"),
    });
  };

  const onClear = () => {
    form.resetFields();
    setFilters({});
  };

  const filterBar = (
    <Form form={form} onFinish={onFilter} style={{ marginBottom: 16 }}>
      {isMobile ? (
        <Row gutter={[8, 8]}>
          <Col xs={24}>
            <Form.Item name="supplierId" noStyle>
              <Select
                placeholder="Proveedor"
                allowClear
                style={{ width: "100%" }}
                options={suppliers.map((s) => ({ value: s.id, label: s.name }))}
              />
            </Form.Item>
          </Col>
          <Col xs={24}>
            <Form.Item name="containerNumber" noStyle>
              <Input placeholder="N° Contenedor" allowClear style={{ width: "100%" }} />
            </Form.Item>
          </Col>
          <Col xs={12}>
            <Button type="primary" htmlType="submit" block>Filtrar</Button>
          </Col>
          <Col xs={12}>
            <Button onClick={onClear} block>Limpiar</Button>
          </Col>
        </Row>
      ) : (
        <div style={{ display: "flex", alignItems: "center", gap: 8, flexWrap: "wrap" }}>
          <Form.Item name="supplierId" noStyle>
            <Select
              placeholder="Proveedor"
              allowClear
              style={{ width: 200 }}
              options={suppliers.map((s) => ({ value: s.id, label: s.name }))}
            />
          </Form.Item>
          <Form.Item name="containerNumber" noStyle>
            <Input placeholder="N° Contenedor" allowClear style={{ width: 180 }} />
          </Form.Item>
          <Form.Item name="dateRange" noStyle>
            <DatePicker.RangePicker
              format="DD/MM/YYYY"
              placeholder={["Partida desde", "Partida hasta"]}
            />
          </Form.Item>
          <Button type="primary" htmlType="submit">Filtrar</Button>
          <Button onClick={onClear}>Limpiar</Button>
        </div>
      )}
    </Form>
  );

  if (isMobile) {
    return (
      <List title="Contenidos">
        {filterBar}
        <AntList
          loading={isLoading}
          dataSource={shipments}
          rowKey="id"
          renderItem={(s) => {
            const detail = detailsMap[s.id];
            return (
              <Card size="small" style={{ marginBottom: 10 }}>
                <Row justify="space-between" align="middle">
                  <Col>
                    <Typography.Text strong style={{ fontSize: 15 }}>
                      #{s.number} — {s.containerNumber ?? "Sin contenedor"}
                    </Typography.Text>
                    <br />
                    <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                      {s.supplierName}
                    </Typography.Text>
                    {detail && (
                      <>
                        <br />
                        <Typography.Text style={{ fontSize: 12 }}>
                          {detail.items.length} producto{detail.items.length !== 1 ? "s" : ""} ·{" "}
                          <strong>{formatAmount(detail.totalAmount)}</strong>
                        </Typography.Text>
                      </>
                    )}
                  </Col>
                  <Col>
                    <Button
                      type="primary"
                      size="small"
                      icon={<EyeOutlined />}
                      onClick={() => navigate(`/shipment-items/show/${s.id}`)}
                    >
                      Ver
                    </Button>
                  </Col>
                </Row>
              </Card>
            );
          }}
        />
      </List>
    );
  }

  return (
    <List title="Contenidos">
      {filterBar}
      <Table dataSource={shipments} loading={isLoading} rowKey="id">
        <Table.Column dataIndex="number" title="N°" width={60} />
        <Table.Column dataIndex="supplierName" title="Proveedor" />
        <Table.Column
          dataIndex="containerNumber"
          title="N° Contenedor"
          render={(v) => v ?? "—"}
        />
        <Table.Column
          dataIndex="departureDate"
          title="Fecha Partida"
          render={(v) => v ?? "—"}
        />
        <Table.Column
          key="itemCount"
          title="Productos"
          render={(_, s: Shipment) => {
            const detail = detailsMap[s.id];
            return detail != null ? detail.items.length : "—";
          }}
        />
        <Table.Column
          key="totalAmount"
          title="Total"
          render={(_, s: Shipment) => {
            const detail = detailsMap[s.id];
            return detail != null ? formatAmount(detail.totalAmount) : "—";
          }}
        />
        <Table.Column
          title="Acciones"
          render={(_, s: Shipment) => (
            <Space>
              <Button
                type="primary"
                size="small"
                icon={<EyeOutlined />}
                onClick={() => navigate(`/shipment-items/show/${s.id}`)}
              >
                Ver contenidos
              </Button>
            </Space>
          )}
        />
      </Table>
    </List>
  );
};
