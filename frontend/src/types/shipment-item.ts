export type ShipmentItem = {
  id: string;
  shipmentId: string;
  containerNumber: string;
  description: string;
  quantity: number | null;
  unitPrice: number | null;
  amount: number;
  createdAt: string;
  updatedAt: string;
};

export type ShipmentDetail = {
  id: string;
  number: number;
  containerNumber: string;
  supplierName: string;
  departureDate: string | null;
  arrivalDate: string | null;
  items: ShipmentItem[];
  totalAmount: number;
};
