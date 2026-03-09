export type Unit = {
  id: string;
  name: string;
  symbol: string;
};

export type Item = {
  id: string;
  name: string;
  baseUnitId: string;
  baseUnitName: string;
  baseUnitSymbol: string;
  active: boolean;
  createdAt: string;
};

export type UnitConversion = {
  id: string;
  itemId: string;
  fromUnitId: string;
  fromUnitName: string;
  fromUnitSymbol: string;
  toUnitId: string;
  toUnitName: string;
  toUnitSymbol: string;
  factor: number;
};

export type Location = {
  id: string;
  name: string;
  type: "warehouse" | "branch";
  active: boolean;
};

export type StockLocationEntry = {
  locationId: string;
  locationName: string;
  locationType: string;
  quantity: number;
  minQuantity: number;
  lowStock: boolean;
};

export type ItemStock = {
  itemId: string;
  itemName: string;
  baseUnitSymbol: string;
  locations: StockLocationEntry[];
  totalQuantity: number;
};

export type InventoryMovement = {
  id: string;
  itemId: string;
  itemName: string;
  unitId: string;
  unitSymbol: string;
  quantity: number;
  quantityBase: number;
  baseUnitSymbol: string;
  movementType: "purchase" | "transfer" | "adjustment" | "consumption";
  locationFromId: string | null;
  locationFromName: string | null;
  locationToId: string | null;
  locationToName: string | null;
  notes: string | null;
  createdAt: string;
};

export type TransferRequestItem = {
  id: string;
  itemId: string;
  itemName: string;
  unitId: string;
  unitSymbol: string;
  quantity: number;
  quantityBase: number;
  baseUnitSymbol: string;
  locationId: string;
  locationName: string;
  status: "pending" | "completed" | "rejected";
  notes: string | null;
  createdAt: string;
  updatedAt: string;
};
