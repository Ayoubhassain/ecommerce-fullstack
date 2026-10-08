import { Component, OnInit } from '@angular/core';
import { OrderSummary } from 'src/app/common/order-summary';
import { OrderService } from 'src/app/services/order.service';

@Component({
  selector: 'app-my-orders',
  templateUrl: './my-orders.component.html'
})
export class MyOrdersComponent implements OnInit {

  orders: OrderSummary[] = [];
  pageNumber = 1;   // ngb-pagination is 1-based, the API is 0-based
  pageSize = 10;
  totalElements = 0;
  loading = true;

  constructor(private orderService: OrderService) { }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.orderService.getMyOrders(this.pageNumber - 1, this.pageSize).subscribe(page => {
      this.orders = page.content;
      this.totalElements = page.totalElements;
      this.loading = false;
    });
  }
}
