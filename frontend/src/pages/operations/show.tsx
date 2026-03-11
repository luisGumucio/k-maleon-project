import { Show } from "@refinedev/antd";
import { useShow, useNavigation, useInvalidate } from "@refinedev/core";
import { Descriptions, Tag, Card, Statistic, Row, Col, Button, Grid } from "antd";
import { EditOutlined } from "@ant-design/icons";
import { Operation, OperationStatus } from "../../types/operation";
import { formatUSD } from "../../utils/money";
import { MovementTable } from "../movements/MovementTable";
import { MovementCreateModal } from "../movements/MovementCreateModal";

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

export const OperationShow = () => {
  const { query } = useShow<Operation>();
  const { edit } = useNavigation();
  const invalidate = useInvalidate();
  const screens = useBreakpoint();
  const isMobile = !screens.md;
  const operation = query.data?.data;

  const handleMovementSuccess = () => {
    invalidate({ resource: "operations", invalidates: ["detail"], id: operation?.id });
  };

  return (
    <Show
      headerButtons={
        operation
          ? [
              <Button
                key="edit"
                icon={<EditOutlined />}
                onClick={() => edit("operations", operation.id)}
              >
                Editar
              </Button>,
            ]
          : []
      }
    >
      {operation && (
        <>
          <Descriptions
            title={`Operación ${operation.container}`}
            bordered
            column={isMobile ? 1 : 2}
            style={{ marginBottom: 24 }}
            size={isMobile ? "small" : "default"}
          >
            <Descriptions.Item label="Proveedor">{operation.supplierName}</Descriptions.Item>
            <Descriptions.Item label="Contenedor">{operation.container}</Descriptions.Item>
            <Descriptions.Item label="Origen">{operation.origin ?? "—"}</Descriptions.Item>
            <Descriptions.Item label="Estado">
              <Tag color={STATUS_COLOR[operation.status as OperationStatus]}>
                {STATUS_LABEL[operation.status as OperationStatus] ?? operation.status}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="Fecha inicio">{operation.startDate}</Descriptions.Item>
            <Descriptions.Item label="Fecha fin">{operation.endDate ?? "—"}</Descriptions.Item>
            {operation.description && (
              <Descriptions.Item label="Descripción" span={isMobile ? 1 : 2}>
                {operation.description}
              </Descriptions.Item>
            )}
            {operation.notes && (
              <Descriptions.Item label="Notas" span={isMobile ? 1 : 2}>
                {operation.notes}
              </Descriptions.Item>
            )}
          </Descriptions>

          <Card title="Resumen financiero" style={{ marginBottom: 24 }} size={isMobile ? "small" : "default"}>
            <Row gutter={[16, 16]}>
              <Col xs={24} sm={8}>
                <Statistic
                  title="Monto acordado"
                  value={formatUSD(operation.totalAmount)}
                  valueStyle={{ color: "#1677ff", fontSize: isMobile ? 20 : 28 }}
                />
              </Col>
              <Col xs={12} sm={8}>
                <Statistic
                  title="Total pagado"
                  value={formatUSD(operation.paidAmount)}
                  valueStyle={{ color: "#52c41a", fontSize: isMobile ? 20 : 28 }}
                />
              </Col>
              <Col xs={12} sm={8}>
                <Statistic
                  title="Pendiente"
                  value={formatUSD(operation.pendingAmount)}
                  valueStyle={{
                    color: operation.pendingAmount < 0 ? "#ff4d4f" : operation.pendingAmount > 0 ? "#faad14" : "#52c41a",
                    fontSize: isMobile ? 20 : 28,
                  }}
                />
              </Col>
            </Row>
          </Card>

          <Card
            title="Movimientos"
            size={isMobile ? "small" : "default"}
            extra={
              <MovementCreateModal
                operationId={operation.id}
                onSuccess={handleMovementSuccess}
              />
            }
          >
            <MovementTable operationId={operation.id} />
          </Card>
        </>
      )}
    </Show>
  );
};
