export type OperationStatus = "active" | "completed" | "cancelled";

export type Operation = {
  id: string;
  supplierId: string;
  supplierName: string;
  container: string;
  description: string | null;
  totalAmount: number;
  paidAmount: number;
  pendingAmount: number;
  origin: string | null;
  startDate: string;
  endDate: string | null;
  status: OperationStatus;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
};
