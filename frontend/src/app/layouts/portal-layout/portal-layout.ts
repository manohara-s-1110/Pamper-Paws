import { Component, computed, inject, signal } from '@angular/core';
import { NgFor, NgIf, TitleCasePipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { NavItem } from '../../models/app.models';
import { SessionAuthService } from '../../services/session-auth';

@Component({
  selector: 'app-portal-layout',
  standalone: true,
  imports: [NgFor, NgIf, RouterLink, RouterLinkActive, RouterOutlet, TitleCasePipe],
  templateUrl: './portal-layout.html',
  styleUrl: './portal-layout.css',
})
export class PortalLayoutComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  readonly auth = inject(SessionAuthService);

  readonly mobileMenuOpen = signal(false);
  readonly navItems = this.route.snapshot.data['nav'] as NavItem[];
  readonly portalTitle = this.route.snapshot.data['portalTitle'] as string;
  readonly session = this.auth.session;
  readonly initials = computed(() => {
    const username = this.session()?.username ?? 'PP';
    return username.slice(0, 2).toUpperCase();
  });

  toggleMobileMenu() {
    this.mobileMenuOpen.update((value) => !value);
  }

  closeMobileMenu() {
    this.mobileMenuOpen.set(false);
  }

  logout() {
    this.auth.logout();
    this.closeMobileMenu();
    void this.router.navigate(['/']);
  }
}
