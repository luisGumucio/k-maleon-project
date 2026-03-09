import { List, useTable, useSelect, DateField } from "@refinedev/antd";
import { Table, Tag, Select, Form, DatePicker, Button, Row, Col, Card, List as AntList, Typography, Space, Grid } from "antd";
import { useNavigation } from "@refinedev/core";
import { Operation, OperationStatus } from "../../types/operation";
import { formatUSD } from "../../utils/money";

const { useBreakpoint } = Grid;

const STATUS_COLOR: Record<OperationStatus, string> = {
  active: "blue",
  completed: "green",
  cancelled: "red",
};

const STATUS_LABEL: Record<OperationStatus, string> = {
  active: "Activa",
  completed: "Completada",
  cancelled: "Cancelada",
};

export const OperationList = () => {
  const { show } = useNavigation();
  const [form] = Form.useForm();
  const screens = useBreakpoint();
  const isMobile = !screens.md;

  const { tableProps, setFilters } = useTable<Operation>({
    syncWithLocation: true,
  });

  const { selectProps: supplierSelectProps } = useSelect({
    resource: "suppliers",
    optionLabel: "name",
    optionValue: "id",
  });

  const onFilter = (values: {
    status?: string;
    supplierId?: string;
    dateRange?: [unknown, unknown];
  }) => {
    const filters = [];
    if (values.status) filters.push({ field: "status", operator: "eq" as const, value: values.status });
    if (values.supplierId) filters.push({ field: "supplierId", operator: "eq" as const, value: values.supplierId });
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

  const operations: Operation[] = (tableProps.dataSource as Operation[]) ?? [];

  const filters = (
    <Form form={form} onFinish={onFilter} style={{ marginBottom: 16 }}>
      <Row gutter={[8, 8]}>
        <Col xs={24} sm="auto">
          <Form.Item name="status" noStyle>
            <Select placeholder="Estado" allowClear style={{ width: "100%" }}>
              <Select.Option value="active">Activa</Select.Option>
              <Select.Option value="completed">Completada</Select.Option>
              <Select.Option value="cancelled">Cancelada</Select.Option>
            </Select>
          </Form.Item>
        </Col>
        <Col xs={24} sm="auto">
          <Form.Item name="supplierId" noStyle>
            <Select
              {...supplierSelectProps}
              placeholder="Proveedor"
              allowClear
              style={{ width: "100%" }}
            />
          </Form.Item>
        </Col>
        {!isMobile && (
          <Col sm="auto">
            <Form.Item name="dateRange" noStyle>
              <DatePicker.RangePicker format="DD/MM/YYYY" />
            </Form.Item>
          </Col>
        )}
        <Col xs={12} sm="auto">
          <Button type="primary" htmlType="submit" block={isMobile}>Filtrar</Button>
        </Col>
        <Col xs={12} sm="auto">
          <Button onClick={onClear} block={isMobile}>Limpiar</Button>
        </Col>
      </Row>
    </Form>
  );

  if (isMobile) {
    return (
      <List>
        {filters}
        <AntList
          loading={tableProps.loading as boolean}
          dataSource={operations}
          rowKey="id"
          renderItem={(op) => (
            <Card
              size="small"
              style={{ marginBottom: 10, cursor: "pointer" }}
              onClick={() => show("operations", op.id)}
            >
              <Row justify="space-between" align="top">
                <Col>
                  <Typography.Text strong style={{ fontSize: 15 }}>
                    {op.container}
                  </Typography.Text>
                  <br />
                  <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                    {op.supplierName}
                  </Typography.Text>
                </Col>
                <Col>
                  <Tag color={STATUS_COLOR[op.status]}>{STATUS_LABEL[op.status] ?? op.status}</Tag>
                </Col>
              </Row>
              <Row style={{ marginTop: 10 }} gutter={8}>
                <Col span={8}>
                  <Typography.Text type="secondary" style={{ fontSize: 11 }}>Acordado</Typography.Text>
                  <br />
                  <Typography.Text style={{ fontSize: 13 }}>{formatUSD(op.totalAmount)}</Typography.Text>
                </Col>
                <Col span={8}>
                  <Typography.Text type="secondary" style={{ fontSize: 11 }}>Pagado</Typography.Text>
                  <br />
                  <Typography.Text style={{ fontSize: 13, color: "#52c41a" }}>{formatUSD(op.paidAmount)}</Typography.Text>
                </Col>
                <Col span={8}>
                  <Typography.Text type="secondary" style={{ fontSize: 11 }}>Pendiente</Typography.Text>
                  <br />
                  <Typography.Text style={{ fontSize: 13, color: op.pendingAmount > 0 ? "#faad14" : "#52c41a" }}>
                    {formatUSD(op.pendingAmount)}
                  </Typography.Text>
                </Col>
              </Row>
              <Typography.Text type="secondary" style={{ fontSize: 11, marginTop: 6, display: "block" }}>
                Inicio: <DateField value={op.startDate} format="DD/MM/YYYY" />
              </Typography.Text>
            </Card>
          )}
        />
      </List>
    );
  }

  return (
    <List>
      {filters}
      <Table
        {...tableProps}
        rowKey="id"
        onRow={(record) => ({ onClick: () => show("operations", record.id) })}
        rowClassName="cursor-pointer"
      >
        <Table.Column dataIndex="container" title="Contenedor" />
        <Table.Column dataIndex="supplierName" title="Proveedor" />
        <Table.Column dataIndex="totalAmount" title="Monto acordado" render={(v) => formatUSD(v)} />
        <Table.Column dataIndex="paidAmount" title="Pagado" render={(v) => formatUSD(v)} />
        <Table.Column dataIndex="pendingAmount" title="Pendiente" render={(v) => formatUSD(v)} />
        <Table.Column
          dataIndex="status"
          title="Estado"
          render={(v: OperationStatus) => (
            <Tag color={STATUS_COLOR[v]}>{STATUS_LABEL[v] ?? v}</Tag>
          )}
        />
        <Table.Column
          dataIndex="startDate"
          title="Fecha inicio"
          render={(v) => <DateField value={v} format="DD/MM/YYYY" />}
        />
      </Table>
    </List>
  );
};
