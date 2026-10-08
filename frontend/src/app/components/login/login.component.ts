import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html'
})
export class LoginComponent {

  form = this.formBuilder.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  errorMessage = '';
  loading = false;

  constructor(private formBuilder: FormBuilder,
              private authService: AuthService,
              private router: Router,
              private route: ActivatedRoute) { }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.errorMessage = '';

    const { email, password } = this.form.getRawValue();
    this.authService.login(email ?? '', password ?? '').subscribe({
      next: response => {
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
        const home = response.user.role === 'ADMIN' ? '/admin/products' : '/products';
        this.router.navigateByUrl(returnUrl || home);
      },
      error: () => {
        this.errorMessage = 'Invalid email or password.';
        this.loading = false;
      }
    });
  }
}
