export type Shipment = {
  id: string;
  number: number;
  supplierId: string;
  supplierName: string;
  departureDate: string | null;
  containerNumber: string | null;
  quantity: number | null;
  productDetails: string | null;
  arrivalDate: string | null;
  documentUrl: string | null;
  createdAt: string;
  updatedAt: string;
};
