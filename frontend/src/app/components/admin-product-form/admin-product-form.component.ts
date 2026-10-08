import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ProductCategory } from 'src/app/common/product-category';
import { AdminProduct } from 'src/app/common/admin-product';
import { AdminService } from 'src/app/services/admin.service';
import { ProductService } from 'src/app/services/product.service';

/** Create a product (/admin/products/new) or edit one (/admin/products/:id). */
@Component({
  selector: 'app-admin-product-form',
  templateUrl: './admin-product-form.component.html'
})
export class AdminProductFormComponent implements OnInit {

  productId: number | null = null;
  categories: ProductCategory[] = [];
  errorMessage = '';
  saving = false;

  form = this.formBuilder.group({
    sku: ['', [Validators.required, Validators.maxLength(255)]],
    name: ['', [Validators.required, Validators.maxLength(255)]],
    description: ['', [Validators.maxLength(255)]],
    unitPrice: [0, [Validators.required, Validators.min(0.01)]],
    imageUrl: ['', [Validators.maxLength(255)]],
    unitsInStock: [0, [Validators.required, Validators.min(0)]],
    active: [true],
    categoryId: [null as number | null, [Validators.required]]
  });

  constructor(private formBuilder: FormBuilder,
              private adminService: AdminService,
              private productService: ProductService,
              private route: ActivatedRoute,
              private router: Router) { }

  ngOnInit(): void {
    this.productService.getProductCategories().subscribe(categories => this.categories = categories);

    const id = this.route.snapshot.paramMap.get('id');
    if (id && id !== 'new') {
      this.productId = +id;
      this.adminService.getProduct(this.productId).subscribe(product => this.form.patchValue(product));
    }
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving = true;
    this.errorMessage = '';

    const value = this.form.getRawValue();
    const product: AdminProduct = {
      sku: value.sku ?? '',
      name: value.name ?? '',
      description: value.description ?? '',
      unitPrice: Number(value.unitPrice),
      imageUrl: value.imageUrl ?? '',
      unitsInStock: Number(value.unitsInStock),
      active: !!value.active,
      categoryId: Number(value.categoryId)
    };

    const request = this.productId
      ? this.adminService.updateProduct(this.productId, product)
      : this.adminService.createProduct(product);

    request.subscribe({
      next: () => this.router.navigateByUrl('/admin/products'),
      error: (error: HttpErrorResponse) => {
        this.errorMessage = error.error?.message ?? 'The product could not be saved.';
        this.saving = false;
      }
    });
  }

  invalid(field: string): boolean {
    const control = this.form.get(field);
    return !!control && control.touched && control.invalid;
  }
}
