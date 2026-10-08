import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../config/api';
import { OrderSummary } from '../common/order-summary';
import { PageResponse } from '../common/page-response';

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  constructor(private http: HttpClient) { }

  getMyOrders(page: number, size: number): Observable<PageResponse<OrderSummary>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<OrderSummary>>(`${API_URL}/orders/me`, { params });
  }
}
