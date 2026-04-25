import { NavItem } from './models/app.models';

export const CUSTOMER_NAV: NavItem[] = [
  { label: 'Dashboard', path: '/customer', exact: true },
  { label: 'My Pets', path: '/customer/pets' },
  { label: 'Appointments', path: '/customer/appointments' },
  { label: 'Profile', path: '/customer/profile' },
];

export const VET_NAV: NavItem[] = [
  { label: 'Dashboard', path: '/vet', exact: true },
  { label: 'Appointments', path: '/vet/appointments' },
  { label: 'Availability', path: '/vet/availability' },
  { label: 'Profile', path: '/vet/profile' },
];

export const ADMIN_NAV: NavItem[] = [
  { label: 'Dashboard', path: '/admin', exact: true },
  { label: 'Manage Users', path: '/admin/users' },
  { label: 'Appointments', path: '/admin/appointments' },
];
