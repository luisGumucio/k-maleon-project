import { DateField, DeleteButton, EditButton, List, ShowButton, useSelect, useTable } from "@refinedev/antd";
import { Table, Select, Input, DatePicker, Form, Button, Row, Col, Card, List as AntList, Typography, Space, Grid } from "antd";
import { Shipment } from "../../types/shipment";

const { useBreakpoint } = Grid;

export const ShipmentList = () => {
  const [form] = Form.useForm();
  const screens = useBreakpoint();
  const isMobile = !screens.md;

  const { tableProps, setFilters } = useTable<Shipment>({
    syncWithLocation: true,
  });

  const { selectProps: supplierSelectProps } = useSelect({
    resource: "suppliers",
    optionLabel: "name",
    optionValue: "id",
  });

  const onFilter = (values: {
    supplierId?: string;
    containerNumber?: string;
    dateRange?: [unknown, unknown];
  }) => {
    const filters = [];
    if (values.supplierId)
      filters.push({ field: "supplierId", operator: "eq" as const, value: values.supplierId });
    if (values.containerNumber)
      filters.push({ field: "containerNumber", operator: "eq" as const, value: values.containerNumber });
    if (values.dateRange?.[0]) {
      const [from, to] = values.dateRange as [{ format: (f: string) => string }, { format: (f: string) => string }];
      filters.push({ field: "from", operator: "eq" as const, value: from.format("YYYY-MM-DD") });
      if (to) filters.push({ field: "to", operator: "eq" as const, value: to.format("YYYY-MM-DD") });
    }
    setFilters(filters, "replace");
  };

  const onClear = () => {
    form.resetFields();
    setFilters([], "replace");
  };

  const shipments: Shipment[] = (tableProps.dataSource as Shipment[]) ?? [];

  const filterBar = (
    <Form form={form} onFinish={onFilter} style={{ marginBottom: 16 }}>
      {isMobile ? (
        <Row gutter={[8, 8]}>
          <Col xs={24}>
            <Form.Item name="supplierId" noStyle>
              <Select {...supplierSelectProps} placeholder="Proveedor" allowClear style={{ width: "100%" }} />
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
            <Select {...supplierSelectProps} placeholder="Proveedor" allowClear style={{ width: 200 }} />
          </Form.Item>
          <Form.Item name="containerNumber" noStyle>
            <Input placeholder="N° Contenedor" allowClear style={{ width: 180 }} />
          </Form.Item>
          <Form.Item name="dateRange" noStyle>
            <DatePicker.RangePicker format="DD/MM/YYYY" placeholder={["Partida desde", "Partida hasta"]} />
          </Form.Item>
          <Button type="primary" htmlType="submit">Filtrar</Button>
          <Button onClick={onClear}>Limpiar</Button>
        </div>
      )}
    </Form>
  );

  if (isMobile) {
    return (
      <List>
        {filterBar}
        <AntList
          loading={tableProps.loading as boolean}
          dataSource={shipments}
          rowKey="id"
          renderItem={(s) => (
            <Card size="small" style={{ marginBottom: 10 }}>
              <Row justify="space-between" align="top">
                <Col>
                  <Typography.Text strong style={{ fontSize: 15 }}>
                    #{s.number} — {s.containerNumber ?? "Sin contenedor"}
                  </Typography.Text>
                  <br />
                  <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                    {s.supplierName}
                  </Typography.Text>
                </Col>
                <Col>
                  <Space>
                    <ShowButton size="small" hideText recordItemId={s.id} />
                    <EditButton size="small" hideText recordItemId={s.id} />
                    <DeleteButton size="small" hideText recordItemId={s.id} />
                  </Space>
                </Col>
              </Row>
              <Row style={{ marginTop: 8 }} gutter={8}>
                {s.departureDate && (
                  <Col span={12}>
                    <Typography.Text type="secondary" style={{ fontSize: 11 }}>Partida</Typography.Text>
                    <br />
                    <DateField value={s.departureDate} format="DD/MM/YYYY" style={{ fontSize: 13 }} />
                  </Col>
                )}
                {s.arrivalDate && (
                  <Col span={12}>
                    <Typography.Text type="secondary" style={{ fontSize: 11 }}>Llegada</Typography.Text>
                    <br />
                    <DateField value={s.arrivalDate} format="DD/MM/YYYY" style={{ fontSize: 13 }} />
                  </Col>
                )}
                {s.quantity != null && (
                  <Col span={12}>
                    <Typography.Text type="secondary" style={{ fontSize: 11 }}>Cantidad</Typography.Text>
                    <br />
                    <Typography.Text style={{ fontSize: 13 }}>{s.quantity}</Typography.Text>
                  </Col>
                )}
              </Row>
            </Card>
          )}
        />
      </List>
    );
  }

  return (
    <List>
      {filterBar}
      <Table {...tableProps} rowKey="id">
        <Table.Column dataIndex="number" title="N°" width={60} />
        <Table.Column dataIndex="supplierName" title="Proveedor" />
        <Table.Column dataIndex="containerNumber" title="N° Contenedor" render={(v) => v ?? "—"} />
        <Table.Column
          dataIndex="departureDate"
          title="Fecha Partida"
          render={(v) => v ? <DateField value={v} format="DD/MM/YYYY" /> : "—"}
        />
        <Table.Column
          dataIndex="arrivalDate"
          title="Fecha Llegada"
          render={(v) => v ? <DateField value={v} format="DD/MM/YYYY" /> : "—"}
        />
        <Table.Column dataIndex="quantity" title="Cantidad" render={(v) => v ?? "—"} />
        <Table.Column
          title="Acciones"
          render={(_, record: Shipment) => (
            <Space>
              <ShowButton size="small" hideText recordItemId={record.id} />
              <EditButton size="small" hideText recordItemId={record.id} />
              <DeleteButton size="small" hideText recordItemId={record.id} />
            </Space>
          )}
        />
      </Table>
    </List>
  );
};
