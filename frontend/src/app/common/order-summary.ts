export interface OrderSummary {
  id: number;
  orderTrackingNumber: string;
  customerName: string;
  customerEmail: string;
  totalQuantity: number;
  totalPrice: number;
  status: string;
  dateCreated: string;
}
