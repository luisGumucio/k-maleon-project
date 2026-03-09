import { Form, Input, DatePicker } from "antd";
import { PaymentType } from "../../types/movement";

type Props = {
  paymentType: PaymentType | null;
};

export const MetadataFields = ({ paymentType }: Props) => {
  if (!paymentType) return null;

  if (paymentType === "swift") {
    return (
      <>
        <Form.Item label="Message ID" name={["metadata", "message_id"]}>
          <Input />
        </Form.Item>
        <Form.Item label="UETR" name={["metadata", "uetr"]}>
          <Input />
        </Form.Item>
        <Form.Item label="Fecha de liquidación" name={["metadata", "settlement_date"]}>
          <DatePicker style={{ width: "100%" }} format="DD/MM/YYYY" />
        </Form.Item>
        <Form.Item label="Banco deudor" name={["metadata", "debtor_bank"]}>
          <Input />
        </Form.Item>
        <Form.Item label="BIC deudor" name={["metadata", "debtor_bic"]}>
          <Input />
        </Form.Item>
        <Form.Item label="Cuenta deudor" name={["metadata", "debtor_account"]}>
          <Input />
        </Form.Item>
        <Form.Item label="Banco acreedor" name={["metadata", "creditor_bank"]}>
          <Input />
        </Form.Item>
        <Form.Item label="BIC acreedor" name={["metadata", "creditor_bic"]}>
          <Input />
        </Form.Item>
        <Form.Item label="Nombre acreedor" name={["metadata", "creditor_name"]}>
          <Input />
        </Form.Item>
        <Form.Item label="Cuenta acreedor" name={["metadata", "creditor_account"]}>
          <Input />
        </Form.Item>
        <Form.Item label="Remesa" name={["metadata", "remittance"]}>
          <Input />
        </Form.Item>
        <Form.Item label="Cargo (charge bearer)" name={["metadata", "charge_bearer"]}>
          <Input placeholder="Ej: DEBT" />
        </Form.Item>
      </>
    );
  }

  if (paymentType === "transfer") {
    return (
      <>
        <Form.Item label="Banco" name={["metadata", "bank_name"]}>
          <Input />
        </Form.Item>
        <Form.Item label="Número de cuenta" name={["metadata", "account_number"]}>
          <Input />
        </Form.Item>
        <Form.Item label="Referencia" name={["metadata", "reference"]}>
          <Input />
        </Form.Item>
        <Form.Item label="Beneficiario" name={["metadata", "beneficiary"]}>
          <Input />
        </Form.Item>
      </>
    );
  }

  if (paymentType === "dhl") {
    return (
      <>
        <Form.Item label="Número de tracking" name={["metadata", "tracking_number"]}>
          <Input />
        </Form.Item>
        <Form.Item label="Origen" name={["metadata", "origin"]}>
          <Input />
        </Form.Item>
        <Form.Item label="Destino" name={["metadata", "destination"]}>
          <Input />
        </Form.Item>
        <Form.Item label="Tipo de servicio" name={["metadata", "service_type"]}>
          <Input placeholder="Ej: EXPRESS" />
        </Form.Item>
      </>
    );
  }

  if (paymentType === "cash") {
    return (
      <>
        <Form.Item label="Recibido por" name={["metadata", "received_by"]}>
          <Input />
        </Form.Item>
        <Form.Item label="Ubicación" name={["metadata", "location"]}>
          <Input />
        </Form.Item>
      </>
    );
  }

  if (paymentType === "other") {
    return (
      <Form.Item label="Descripción del tipo" name={["metadata", "custom_type"]}>
        <Input.TextArea rows={3} placeholder="Describe el método de pago" />
      </Form.Item>
    );
  }

  return null;
};
