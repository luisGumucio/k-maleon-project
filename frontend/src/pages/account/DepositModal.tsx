import { useState } from "react";
import { Modal, Form, InputNumber, Input, Button, message } from "antd";
import { PlusOutlined } from "@ant-design/icons";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import dayjs from "dayjs";
import { apiUrl, fetchJson } from "../../providers/data";
import { dollarsToCents } from "../../utils/money";

type FormValues = {
  amount: number;
  description?: string;
};

export const DepositModal = () => {
  const [open, setOpen] = useState(false);
  const [form] = Form.useForm<FormValues>();
  const queryClient = useQueryClient();

  const { mutate, isPending } = useMutation({
    mutationFn: (values: FormValues) =>
      fetchJson(`${apiUrl}/account/deposit`, {
        method: "POST",
        body: JSON.stringify({
          amount: dollarsToCents(values.amount),
          date: dayjs().format("YYYY-MM-DD"),
          description: values.description ?? null,
        }),
      }),
    onSuccess: () => {
      message.success("Saldo agregado correctamente");
      queryClient.invalidateQueries({ queryKey: ["account-summary"] });
      queryClient.invalidateQueries({ queryKey: ["account-deposits"] });
      setOpen(false);
      form.resetFields();
    },
    onError: () => {
      message.error("Error al agregar saldo");
    },
  });

  return (
    <>
      <Button type="primary" icon={<PlusOutlined />} onClick={() => setOpen(true)}>
        Agregar saldo
      </Button>

      <Modal
        title="Agregar saldo a la cuenta"
        open={open}
        onCancel={() => setOpen(false)}
        onOk={() => form.submit()}
        okText="Agregar"
        cancelText="Cancelar"
        confirmLoading={isPending}
        width={420}
        destroyOnClose
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={(values) => mutate(values)}
        >
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

          <Form.Item label="Descripción" name="description">
            <Input placeholder="Opcional" />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
};
