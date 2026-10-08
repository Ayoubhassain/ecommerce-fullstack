export interface AdminProduct {
  id?: number;
  sku: string;
  name: string;
  description: string;
  unitPrice: number;
  imageUrl: string;
  active: boolean;
  unitsInStock: number;
  categoryId: number;
  categoryName?: string;
}
