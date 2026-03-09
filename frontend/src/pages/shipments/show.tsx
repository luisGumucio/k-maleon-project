import { Show, EditButton } from "@refinedev/antd";
import { useShow } from "@refinedev/core";
import { Descriptions, Button, Grid } from "antd";
import { FileOutlined } from "@ant-design/icons";
import { Shipment } from "../../types/shipment";

const { useBreakpoint } = Grid;

export const ShipmentShow = () => {
  const { query } = useShow<Shipment>();
  const screens = useBreakpoint();
  const isMobile = !screens.md;
  const shipment = query.data?.data;

  return (
    <Show
      headerButtons={({ editButtonProps }) => (
        <EditButton {...editButtonProps} />
      )}
    >
      {shipment && (
        <Descriptions
          title={`Envío #${shipment.number}`}
          bordered
          column={isMobile ? 1 : 2}
          size={isMobile ? "small" : "default"}
        >
          <Descriptions.Item label="Proveedor">{shipment.supplierName}</Descriptions.Item>
          <Descriptions.Item label="N° Contenedor">{shipment.containerNumber ?? "—"}</Descriptions.Item>
          <Descriptions.Item label="Fecha de partida">{shipment.departureDate ?? "—"}</Descriptions.Item>
          <Descriptions.Item label="Fecha de llegada">{shipment.arrivalDate ?? "—"}</Descriptions.Item>
          <Descriptions.Item label="Cantidad">{shipment.quantity ?? "—"}</Descriptions.Item>
          <Descriptions.Item label="Documento">
            {shipment.documentUrl ? (
              <a href={shipment.documentUrl} target="_blank" rel="noopener noreferrer">
                <Button size="small" icon={<FileOutlined />}>Ver documento</Button>
              </a>
            ) : (
              "—"
            )}
          </Descriptions.Item>
          {shipment.productDetails && (
            <Descriptions.Item label="Detalles del producto" span={isMobile ? 1 : 2}>
              {shipment.productDetails}
            </Descriptions.Item>
          )}
        </Descriptions>
      )}
    </Show>
  );
};
