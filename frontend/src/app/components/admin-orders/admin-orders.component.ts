import { Component, OnInit } from '@angular/core';
import { OrderSummary } from 'src/app/common/order-summary';
import { AdminService } from 'src/app/services/admin.service';

@Component({
  selector: 'app-admin-orders',
  templateUrl: './admin-orders.component.html'
})
export class AdminOrdersComponent implements OnInit {

  readonly statuses = ['NEW', 'SHIPPED', 'DELIVERED', 'CANCELLED'];

  orders: OrderSummary[] = [];
  pageNumber = 1;
  pageSize = 10;
  totalElements = 0;
  message = '';

  constructor(private adminService: AdminService) { }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.adminService.getOrders(this.pageNumber - 1, this.pageSize).subscribe(page => {
      this.orders = page.content;
      this.totalElements = page.totalElements;
    });
  }

  changeStatus(order: OrderSummary, status: string): void {
    this.adminService.updateOrderStatus(order.id, status).subscribe({
      next: updated => {
        order.status = updated.status;
        this.message = `Order ${order.orderTrackingNumber.substring(0, 8)}… is now ${updated.status}.`;
      },
      error: () => this.message = 'The status could not be updated.'
    });
  }
}
