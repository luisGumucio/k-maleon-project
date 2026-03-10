import { useState } from "react";
import {
  Modal,
  Form,
  Select,
  InputNumber,
  DatePicker,
  Input,
  Alert,
  Button,
  message,
  Grid,
  Upload,
} from "antd";
import { PlusOutlined, UploadOutlined, DeleteOutlined } from "@ant-design/icons";
import { useQueryClient } from "@tanstack/react-query";
import dayjs from "dayjs";
import { PaymentType, MovementType } from "../../types/movement";
import { MetadataFields } from "./MetadataFields";
import { dollarsToCents } from "../../utils/money";
import { apiUrl, fetchJson, fetchWithAuth } from "../../providers/data";

const { useBreakpoint } = Grid;

type Props = {
  operationId: string;
  onSuccess: () => void;
};

type FormValues = {
  type: MovementType;
  paymentType: PaymentType | null;
  amount: number;
  date: dayjs.Dayjs;
  description?: string;
  metadata?: Record<string, unknown>;
};

export const MovementCreateModal = ({ operationId, onSuccess }: Props) => {
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [uploadedUrl, setUploadedUrl] = useState<string | null>(null);
  const [uploadedFileName, setUploadedFileName] = useState<string | null>(null);
  const [selectedPaymentType, setSelectedPaymentType] = useState<PaymentType | null>(null);
  const [movementType, setMovementType] = useState<MovementType>("salida");
  const [form] = Form.useForm<FormValues>();
  const queryClient = useQueryClient();
  const screens = useBreakpoint();
  const isMobile = !screens.md;

  const handleOpen = () => {
    form.resetFields();
    setSelectedPaymentType(null);
    setMovementType("salida");
    setUploadedUrl(null);
    setUploadedFileName(null);
    setOpen(true);
  };

  const handleCancel = () => setOpen(false);

  const handleUpload = async (file: File) => {
    setUploading(true);
    try {
      const formData = new FormData();
      formData.append("file", file);
      const data = await fetchWithAuth(`${apiUrl}/attachments/upload`, {
        method: "POST",
        body: formData
      });
      setUploadedUrl(data.url);
      setUploadedFileName(file.name);
      message.success("Archivo subido correctamente");
    } catch {
      message.error("Error al subir el archivo");
    } finally {
      setUploading(false);
    }
    return false; // prevent antd default upload behavior
  };

  const handleRemoveAttachment = () => {
    setUploadedUrl(null);
    setUploadedFileName(null);
  };

  const handleFinish = async (values: FormValues) => {
    setLoading(true);
    try {
      const metadataRaw = values.metadata ?? {};
      const processedMetadata: Record<string, unknown> = {};
      for (const [k, v] of Object.entries(metadataRaw)) {
        if (v == null || v === "") continue;
        processedMetadata[k] = dayjs.isDayjs(v) ? v.format("YYYY-MM-DD") : v;
      }

      const payload = {
        operationId,
        type: values.type,
        paymentType: values.paymentType ?? null,
        amount: dollarsToCents(values.amount),
        currency: "USD",
        date: values.date.format("YYYY-MM-DD"),
        description: values.description ?? null,
        attachmentUrl: uploadedUrl ?? null,
        metadata: Object.keys(processedMetadata).length > 0
          ? JSON.stringify(processedMetadata)
          : null,
      };

      await fetchJson(`${apiUrl}/movements`, {
        method: "POST",
        body: JSON.stringify(payload),
      });

      message.success("Movimiento registrado correctamente");
      queryClient.invalidateQueries({ queryKey: ["movements", operationId] });
      queryClient.invalidateQueries({ queryKey: ["operations"] });
      setOpen(false);
      onSuccess();
    } catch (err) {
      message.error("Error al registrar el movimiento");
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Button type="primary" icon={<PlusOutlined />} onClick={handleOpen}>
        {isMobile ? "" : "Nuevo movimiento"}
      </Button>

      <Modal
        title="Registrar movimiento"
        open={open}
        onCancel={handleCancel}
        onOk={() => form.submit()}
        okText="Registrar"
        cancelText="Cancelar"
        confirmLoading={loading}
        width={isMobile ? "100%" : 600}
        style={isMobile ? { top: 0, margin: 0, maxWidth: "100vw", padding: 0 } : undefined}
        styles={isMobile ? { body: { maxHeight: "80vh", overflowY: "auto" } } : undefined}
        destroyOnClose
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleFinish}
          initialValues={{ type: "salida", currency: "USD" }}
        >
          <Form.Item label="Tipo" name="type" rules={[{ required: true }]}>
            <Select onChange={(v) => setMovementType(v)}>
              <Select.Option value="salida">Salida</Select.Option>
              <Select.Option value="entrada">Entrada</Select.Option>
            </Select>
          </Form.Item>

          {movementType === "salida" && (
            <Alert
              type="warning"
              showIcon
              message="Esto reducirá el saldo de la cuenta y sumará al monto pagado de la operación."
              style={{ marginBottom: 16 }}
            />
          )}

          <Form.Item label="Método de pago" name="paymentType">
            <Select
              allowClear
              placeholder="Selecciona un método"
              onChange={(v) => setSelectedPaymentType(v ?? null)}
            >
              <Select.Option value="swift">SWIFT</Select.Option>
              <Select.Option value="transfer">Transferencia</Select.Option>
              <Select.Option value="dhl">DHL</Select.Option>
              <Select.Option value="cash">Efectivo</Select.Option>
              <Select.Option value="other">Otro</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item
            label="Monto (USD)"
            name="amount"
            rules={[{ required: true, message: "El monto es requerido" }]}
          >
            <InputNumber
              style={{ width: "100%" }}
              min={0.01}
              precision={2}
              prefix="$"
              placeholder="0.00"
            />
          </Form.Item>

          <Form.Item
            label="Fecha"
            name="date"
            rules={[{ required: true, message: "La fecha es requerida" }]}
            initialValue={dayjs()}
          >
            <DatePicker style={{ width: "100%" }} format="DD/MM/YYYY" />
          </Form.Item>

          <Form.Item label="Descripción" name="description">
            <Input.TextArea rows={2} />
          </Form.Item>

          <Form.Item label="Comprobante (PDF / imagen)">
            {uploadedUrl ? (
              <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                <a href={uploadedUrl} target="_blank" rel="noopener noreferrer">
                  {uploadedFileName ?? "Ver archivo"}
                </a>
                <Button
                  size="small"
                  danger
                  icon={<DeleteOutlined />}
                  onClick={handleRemoveAttachment}
                />
              </div>
            ) : (
              <Upload
                beforeUpload={handleUpload}
                customRequest={() => {}}
                showUploadList={false}
                accept=".pdf,.jpg,.jpeg,.png"
                disabled={uploading}
              >
                <Button icon={<UploadOutlined />} loading={uploading}>
                  {uploading ? "Subiendo..." : "Subir archivo"}
                </Button>
              </Upload>
            )}
          </Form.Item>

          <MetadataFields paymentType={selectedPaymentType} />
        </Form>
      </Modal>
    </>
  );
};
