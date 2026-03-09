import { useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Button,
  Card,
  Descriptions,
  Empty,
  Grid,
  List as AntList,
  Popconfirm,
  Space,
  Statistic,
  Table,
  Typography,
  message,
} from "antd";
import { ArrowLeftOutlined, DeleteOutlined, EditOutlined, PlusOutlined } from "@ant-design/icons";
import { useState } from "react";
import { useNavigate, useParams } from "react-router";
import { ShipmentDetail, ShipmentItem } from "../../types/shipment-item";
import { apiUrl, fetchJson } from "../../providers/data";
import { ItemFormModal } from "./ItemFormModal";

const { useBreakpoint } = Grid;

function formatAmount(v: number | null | undefined): string {
  if (v == null) return "—";
  return new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" }).format(v);
}

export const ShipmentItemShow = () => {
  const { id } = useParams<{ id: string }>();
  const screens = useBreakpoint();
  const isMobile = !screens.md;
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const [modalOpen, setModalOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<ShipmentItem | undefined>();

  const { data: detail, isLoading } = useQuery<ShipmentDetail>({
    queryKey: ["shipment-detail", id],
    queryFn: () => fetchJson(`${apiUrl}/shipment-items?shipmentId=${id}`),
    enabled: !!id,
  });

  const refresh = () => {
    queryClient.invalidateQueries({ queryKey: ["shipment-detail", id] });
    queryClient.invalidateQueries({ queryKey: ["shipment-details-summary"] });
  };

  const handleDelete = async (itemId: string) => {
    try {
      await fetchJson(`${apiUrl}/shipment-items/${itemId}`, { method: "DELETE" });
      message.success("Ítem eliminado");
      refresh();
    } catch {
      message.error("Error al eliminar el ítem");
    }
  };

  const openCreate = () => {
    setEditingItem(undefined);
    setModalOpen(true);
  };

  const openEdit = (item: ShipmentItem) => {
    setEditingItem(item);
    setModalOpen(true);
  };

  const handleModalSuccess = () => {
    setModalOpen(false);
    refresh();
  };

  const items = detail?.items ?? [];

  return (
    <div style={{ padding: isMobile ? 12 : 24 }}>
      {/* Header */}
      <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate("/shipment-items")} />
        <Typography.Title level={4} style={{ margin: 0 }}>
          {detail ? `#${detail.number} — ${detail.containerNumber}` : "Cargando..."}
        </Typography.Title>
      </div>

      {/* Resumen */}
      {detail && (
        <Card style={{ marginBottom: 16 }}>
          <Descriptions column={isMobile ? 1 : 4} size="small" style={{ marginBottom: 12 }}>
            <Descriptions.Item label="Contenedor">{detail.containerNumber}</Descriptions.Item>
            <Descriptions.Item label="Proveedor">{detail.supplierName}</Descriptions.Item>
            <Descriptions.Item label="Partida">{detail.departureDate ?? "—"}</Descriptions.Item>
            <Descriptions.Item label="Llegada">{detail.arrivalDate ?? "—"}</Descriptions.Item>
          </Descriptions>
          <Statistic
            title="Total"
            value={detail.totalAmount}
            precision={2}
            prefix="$"
            valueStyle={{ color: "#1677ff", fontWeight: 700 }}
          />
        </Card>
      )}

      {/* Botón agregar */}
      <div style={{ display: "flex", justifyContent: "flex-end", marginBottom: 12 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate} loading={isLoading}>
          Agregar ítem
        </Button>
      </div>

      {/* Ítems vacíos */}
      {!isLoading && items.length === 0 && (
        <Empty description="Sin ítems registrados. Agrega el primero." />
      )}

      {/* Mobile: cards */}
      {isMobile && items.length > 0 && (
        <AntList
          loading={isLoading}
          dataSource={items}
          rowKey="id"
          renderItem={(item) => (
            <Card size="small" style={{ marginBottom: 8 }} styles={{ body: { padding: "10px 12px" } }}>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
                <div>
                  <Typography.Text strong>{item.description}</Typography.Text>
                  <br />
                  <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                    {item.quantity != null ? `Cant: ${item.quantity}` : ""}
                    {item.quantity != null && item.unitPrice != null ? " · " : ""}
                    {item.unitPrice != null ? `P.U: ${formatAmount(item.unitPrice)}` : ""}
                  </Typography.Text>
                </div>
                <div style={{ textAlign: "right" }}>
                  <Typography.Text strong style={{ fontSize: 15 }}>
                    {formatAmount(item.amount)}
                  </Typography.Text>
                  <br />
                  <Space size={4} style={{ marginTop: 4 }}>
                    <Button size="small" icon={<EditOutlined />} onClick={() => openEdit(item)} />
                    <Popconfirm
                      title="¿Eliminar este ítem?"
                      onConfirm={() => handleDelete(item.id)}
                      okText="Sí"
                      cancelText="No"
                    >
                      <Button size="small" danger icon={<DeleteOutlined />} />
                    </Popconfirm>
                  </Space>
                </div>
              </div>
            </Card>
          )}
        />
      )}

      {/* Desktop: tabla */}
      {!isMobile && (
        <Table
          dataSource={items}
          loading={isLoading}
          rowKey="id"
          pagination={false}
          summary={() =>
            detail && items.length > 0 ? (
              <Table.Summary.Row>
                <Table.Summary.Cell index={0} colSpan={3}>
                  <Typography.Text strong>Total</Typography.Text>
                </Table.Summary.Cell>
                <Table.Summary.Cell index={3}>
                  <Typography.Text strong style={{ color: "#1677ff" }}>
                    {formatAmount(detail.totalAmount)}
                  </Typography.Text>
                </Table.Summary.Cell>
                <Table.Summary.Cell index={4} />
              </Table.Summary.Row>
            ) : null
          }
        >
          <Table.Column dataIndex="description" title="Descripción" />
          <Table.Column dataIndex="quantity" title="Cantidad" render={(v) => v ?? "—"} />
          <Table.Column dataIndex="unitPrice" title="Precio Unit." render={(v) => formatAmount(v)} />
          <Table.Column dataIndex="amount" title="Importe" render={(v) => formatAmount(v)} />
          <Table.Column
            title="Acciones"
            render={(_, item: ShipmentItem) => (
              <Space>
                <Button size="small" icon={<EditOutlined />} onClick={() => openEdit(item)}>
                  Editar
                </Button>
                <Popconfirm
                  title="¿Eliminar este ítem?"
                  onConfirm={() => handleDelete(item.id)}
                  okText="Sí"
                  cancelText="No"
                >
                  <Button size="small" danger icon={<DeleteOutlined />}>
                    Eliminar
                  </Button>
                </Popconfirm>
              </Space>
            )}
          />
        </Table>
      )}

      {id && (
        <ItemFormModal
          open={modalOpen}
          onClose={() => setModalOpen(false)}
          onSuccess={handleModalSuccess}
          shipmentId={id}
          item={editingItem}
        />
      )}
    </div>
  );
};
