import { Routes } from '@angular/router';

import { ADMIN_NAV, CUSTOMER_NAV, VET_NAV } from './navigation';
import { PortalLayoutComponent } from './layouts/portal-layout/portal-layout';
import { roleAccessGuard } from './guards/role-access-guard';
import { PublicContactComponent } from './pages/public/contact/contact';
import { CustomerAppointmentsComponent } from './pages/customer/appointments/customer-appointments';
import { CustomerDashboardComponent } from './pages/customer/dashboard/customer-dashboard';
import { CustomerPetsComponent } from './pages/customer/pets/customer-pets';
import { CustomerProfileComponent } from './pages/customer/profile/customer-profile';
import { PublicHelpComponent } from './pages/public/help/help';
import { PublicHomeComponent } from './pages/public/home/home';
import { PublicLoginComponent } from './pages/public/login/login';
import { PublicRegisterComponent } from './pages/public/register/register';
import { AdminAppointmentsComponent } from './pages/admin/appointments/admin-appointments';
import { AdminDashboardComponent } from './pages/admin/dashboard/admin-dashboard';
import { AdminUsersComponent } from './pages/admin/users/admin-users';
import { VetAppointmentsComponent } from './pages/vet/appointments/vet-appointments';
import { VetAvailabilityComponent } from './pages/vet/availability/vet-availability';
import { VetDashboardComponent } from './pages/vet/dashboard/vet-dashboard';
import { VetProfileComponent } from './pages/vet/profile/vet-profile';

export const routes: Routes = [
  { path: '', component: PublicHomeComponent },
  { path: 'login', component: PublicLoginComponent },
  { path: 'register', component: PublicRegisterComponent },
  { path: 'contact', component: PublicContactComponent },
  { path: 'help', component: PublicHelpComponent },
  {
    path: 'customer',
    component: PortalLayoutComponent,
    canActivate: [roleAccessGuard],
    data: {
      roles: ['CUSTOMER'],
      nav: CUSTOMER_NAV,
      portalTitle: 'Customer Portal',
    },
    children: [
      { path: '', component: CustomerDashboardComponent },
      { path: 'pets', component: CustomerPetsComponent },
      { path: 'appointments', component: CustomerAppointmentsComponent },
      { path: 'profile', component: CustomerProfileComponent },
    ],
  },
  {
    path: 'vet',
    component: PortalLayoutComponent,
    canActivate: [roleAccessGuard],
    data: {
      roles: ['VET'],
      nav: VET_NAV,
      portalTitle: 'Veterinarian Portal',
    },
    children: [
      { path: '', component: VetDashboardComponent },
      { path: 'appointments', component: VetAppointmentsComponent },
      { path: 'availability', component: VetAvailabilityComponent },
      { path: 'profile', component: VetProfileComponent },
    ],
  },
  {
    path: 'admin',
    component: PortalLayoutComponent,
    canActivate: [roleAccessGuard],
    data: {
      roles: ['ADMIN'],
      nav: ADMIN_NAV,
      portalTitle: 'Admin Portal',
    },
    children: [
      { path: '', component: AdminDashboardComponent },
      { path: 'users', component: AdminUsersComponent },
      { path: 'appointments', component: AdminAppointmentsComponent },
    ],
  },
  { path: '**', redirectTo: '' },
];
