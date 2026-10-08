import { Component, OnInit } from '@angular/core';
import { AdminProduct } from 'src/app/common/admin-product';
import { AdminService } from 'src/app/services/admin.service';

@Component({
  selector: 'app-admin-products',
  templateUrl: './admin-products.component.html'
})
export class AdminProductsComponent implements OnInit {

  products: AdminProduct[] = [];
  search = '';
  pageNumber = 1;
  pageSize = 10;
  totalElements = 0;
  message = '';

  constructor(private adminService: AdminService) { }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.adminService.getProducts(this.pageNumber - 1, this.pageSize, this.search)
      .subscribe(page => {
        this.products = page.content;
        this.totalElements = page.totalElements;
      });
  }

  onSearch(): void {
    this.pageNumber = 1;
    this.load();
  }

  delete(product: AdminProduct): void {
    if (!product.id || !confirm(`Delete "${product.name}"?`)) {
      return;
    }
    this.adminService.deleteProduct(product.id).subscribe({
      next: () => {
        this.message = `"${product.name}" was deleted.`;
        this.load();
      },
      error: () => this.message = `"${product.name}" could not be deleted (it may be used in an order).`
    });
  }
}
