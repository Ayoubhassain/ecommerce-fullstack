import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../config/api';
import { AdminProduct } from '../common/admin-product';
import { OrderSummary } from '../common/order-summary';
import { PageResponse } from '../common/page-response';

@Injectable({
  providedIn: 'root'
})
export class AdminService {

  private baseUrl = `${API_URL}/admin`;

  constructor(private http: HttpClient) { }

  getProducts(page: number, size: number, search: string): Observable<PageResponse<AdminProduct>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (search.trim()) {
      params = params.set('search', search.trim());
    }
    return this.http.get<PageResponse<AdminProduct>>(`${this.baseUrl}/products`, { params });
  }

  getProduct(id: number): Observable<AdminProduct> {
    return this.http.get<AdminProduct>(`${this.baseUrl}/products/${id}`);
  }

  createProduct(product: AdminProduct): Observable<AdminProduct> {
    return this.http.post<AdminProduct>(`${this.baseUrl}/products`, product);
  }

  updateProduct(id: number, product: AdminProduct): Observable<AdminProduct> {
    return this.http.put<AdminProduct>(`${this.baseUrl}/products/${id}`, product);
  }

  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/products/${id}`);
  }

  getOrders(page: number, size: number): Observable<PageResponse<OrderSummary>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<OrderSummary>>(`${this.baseUrl}/orders`, { params });
  }

  updateOrderStatus(id: number, status: string): Observable<OrderSummary> {
    return this.http.put<OrderSummary>(`${this.baseUrl}/orders/${id}/status`, { status });
  }
}
