import { useState } from "react";
import { Edit } from "@refinedev/antd";
import { useInvalidate } from "@refinedev/core";
import { Card, Button, Upload, Typography, message } from "antd";
import { UploadOutlined, FileOutlined } from "@ant-design/icons";
import { useShipmentForm, ShipmentFormFields } from "./form";
import { apiUrl, fetchJson } from "../../providers/data";

export const ShipmentEdit = () => {
  const { formProps, saveButtonProps, initialValues, shipment } = useShipmentForm("edit");
  const [uploading, setUploading] = useState(false);
  const [currentDocumentUrl, setCurrentDocumentUrl] = useState<string | null>(null);
  const invalidate = useInvalidate();

  // Use the current upload state if set, otherwise fall back to the shipment value
  const documentUrl = currentDocumentUrl ?? shipment?.documentUrl ?? null;

  const handleUpload = async (file: File) => {
    setUploading(true);
    try {
      const formData = new FormData();
      formData.append("file", file);
      const res = await fetch(`${apiUrl}/attachments/upload`, {
        method: "POST",
        body: formData,
      });
      if (!res.ok) throw new Error("Upload failed");
      const data = await res.json();
      const newUrl: string = data.url;

      await fetchJson(`${apiUrl}/shipments/${shipment!.id}`, {
        method: "PUT",
        body: JSON.stringify({
          supplierId: shipment!.supplierId,
          departureDate: shipment!.departureDate,
          containerNumber: shipment!.containerNumber,
          quantity: shipment!.quantity,
          productDetails: shipment!.productDetails,
          arrivalDate: shipment!.arrivalDate,
          documentUrl: newUrl,
        }),
      });

      setCurrentDocumentUrl(newUrl);
      invalidate({ resource: "shipments", invalidates: ["detail"], id: shipment!.id });
      message.success("Documento subido correctamente");
    } catch {
      message.error("Error al subir el documento");
    } finally {
      setUploading(false);
    }
    return false;
  };

  return (
    <Edit saveButtonProps={saveButtonProps}>
      <ShipmentFormFields formProps={formProps} initialValues={initialValues} />

      <Card title="Documento" size="small" style={{ marginTop: 24 }}>
        {documentUrl ? (
          <div style={{ display: "flex", alignItems: "center", gap: 12, flexWrap: "wrap" }}>
            <a href={documentUrl} target="_blank" rel="noopener noreferrer">
              <Button icon={<FileOutlined />}>Ver documento</Button>
            </a>
            <Upload
              beforeUpload={handleUpload}
              showUploadList={false}
              accept=".pdf,.jpg,.jpeg,.png"
              disabled={uploading}
            >
              <Button icon={<UploadOutlined />} loading={uploading}>
                {uploading ? "Subiendo..." : "Reemplazar"}
              </Button>
            </Upload>
          </div>
        ) : (
          <div>
            <Typography.Text type="secondary" style={{ display: "block", marginBottom: 8 }}>
              Sin documento adjunto
            </Typography.Text>
            <Upload
              beforeUpload={handleUpload}
              showUploadList={false}
              accept=".pdf,.jpg,.jpeg,.png"
              disabled={uploading}
            >
              <Button icon={<UploadOutlined />} loading={uploading}>
                {uploading ? "Subiendo..." : "Subir documento"}
              </Button>
            </Upload>
          </div>
        )}
      </Card>
    </Edit>
  );
};
