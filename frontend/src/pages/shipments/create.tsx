import { useState } from "react";
import { Create } from "@refinedev/antd";
import { Button, Upload, Typography, message } from "antd";
import { UploadOutlined, DeleteOutlined } from "@ant-design/icons";
import { useShipmentForm, ShipmentFormFields } from "./form";
import { apiUrl } from "../../providers/data";

export const ShipmentCreate = () => {
  const [uploading, setUploading] = useState(false);
  const [uploadedUrl, setUploadedUrl] = useState<string | null>(null);
  const [uploadedFileName, setUploadedFileName] = useState<string | null>(null);

  const { formProps, saveButtonProps } = useShipmentForm("create", uploadedUrl);

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
      setUploadedUrl(data.url);
      setUploadedFileName(file.name);
      message.success("Documento subido correctamente");
    } catch {
      message.error("Error al subir el documento");
    } finally {
      setUploading(false);
    }
    return false;
  };

  const handleRemove = () => {
    setUploadedUrl(null);
    setUploadedFileName(null);
  };

  return (
    <Create saveButtonProps={saveButtonProps}>
      <ShipmentFormFields formProps={formProps} />

      <div style={{ marginTop: 8 }}>
        <Typography.Text strong>Documento (opcional)</Typography.Text>
        <div style={{ marginTop: 8 }}>
          {uploadedUrl ? (
            <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <a href={uploadedUrl} target="_blank" rel="noopener noreferrer">
                {uploadedFileName ?? "Ver documento"}
              </a>
              <Button size="small" danger icon={<DeleteOutlined />} onClick={handleRemove} />
            </div>
          ) : (
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
          )}
        </div>
      </div>
    </Create>
  );
};
