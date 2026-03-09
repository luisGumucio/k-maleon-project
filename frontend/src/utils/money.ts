export function formatUSD(cents: number | null | undefined): string {
  if (cents == null) return "$0.00";
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
  }).format(cents / 100);
}

export function dollarsToCents(dollars: number | string): number {
  return Math.round(Number(dollars) * 100);
}

export function centsToDollars(cents: number): number {
  return cents / 100;
}
