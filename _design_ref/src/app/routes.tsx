import { createBrowserRouter } from 'react-router';
import { Home } from './pages/Home';

// Customer pages
import { CustomerLayout } from './layouts/CustomerLayout';
import { CustomerDashboard } from './pages/customer/Dashboard';
import { PetManagement } from './pages/customer/PetManagement';
import { BookAppointment } from './pages/customer/BookAppointment';
import { CustomerProfile } from './pages/customer/Profile';

// Vet pages
import { VetLayout } from './layouts/VetLayout';
import { VetDashboard } from './pages/vet/Dashboard';
import { VetAppointments } from './pages/vet/Appointments';
import { ManageAvailability } from './pages/vet/ManageAvailability';
import { VetProfile } from './pages/vet/Profile';

// Admin pages
import { AdminLayout } from './layouts/AdminLayout';
import { AdminDashboard } from './pages/admin/Dashboard';
import { ManageUsers } from './pages/admin/ManageUsers';
import { ManageAppointments } from './pages/admin/ManageAppointments';

export const router = createBrowserRouter([
  {
    path: '/',
    Component: Home,
  },
  {
    path: '/customer',
    Component: CustomerLayout,
    children: [
      {
        index: true,
        Component: CustomerDashboard,
      },
      {
        path: 'pets',
        Component: PetManagement,
      },
      {
        path: 'book-appointment',
        Component: BookAppointment,
      },
      {
        path: 'profile',
        Component: CustomerProfile,
      },
    ],
  },
  {
    path: '/vet',
    Component: VetLayout,
    children: [
      {
        index: true,
        Component: VetDashboard,
      },
      {
        path: 'appointments',
        Component: VetAppointments,
      },
      {
        path: 'availability',
        Component: ManageAvailability,
      },
      {
        path: 'profile',
        Component: VetProfile,
      },
    ],
  },
  {
    path: '/admin',
    Component: AdminLayout,
    children: [
      {
        index: true,
        Component: AdminDashboard,
      },
      {
        path: 'users',
        Component: ManageUsers,
      },
      {
        path: 'appointments',
        Component: ManageAppointments,
      },
    ],
  },
]);
