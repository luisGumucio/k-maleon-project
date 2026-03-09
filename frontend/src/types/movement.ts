export type MovementType = "entrada" | "salida";

export type PaymentType = "swift" | "transfer" | "dhl" | "cash" | "other";

export type Movement = {
  id: string;
  operationId: string;
  type: MovementType;
  paymentType: PaymentType | null;
  amount: number;
  currency: string;
  date: string;
  description: string | null;
  metadata: string | null;
  attachmentUrl: string | null;
  createdAt: string;
};
