import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from 'src/app/services/auth.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html'
})
export class RegisterComponent {

  form = this.formBuilder.group({
    firstName: ['', [Validators.required, Validators.maxLength(100)]],
    lastName: ['', [Validators.required, Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  errorMessage = '';
  loading = false;

  constructor(private formBuilder: FormBuilder,
              private authService: AuthService,
              private router: Router) { }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.errorMessage = '';

    const value = this.form.getRawValue();
    this.authService.register({
      firstName: value.firstName ?? '',
      lastName: value.lastName ?? '',
      email: value.email ?? '',
      password: value.password ?? ''
    }).subscribe({
      next: () => this.router.navigateByUrl('/products'),
      error: (error: HttpErrorResponse) => {
        this.errorMessage = error.status === 409
          ? 'An account already exists with this email.'
          : 'Registration failed. Check the form and try again.';
        this.loading = false;
      }
    });
  }
}
