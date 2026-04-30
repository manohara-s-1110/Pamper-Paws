import { Outlet } from 'react-router';
import { LayoutDashboard, PawPrint, Calendar, User } from 'lucide-react';
import { AppNavbar } from '../components/AppNavbar';
import { AppSidebar } from '../components/AppSidebar';
import { MobileNav } from '../components/MobileNav';
import { getCustomerById } from '../lib/data';

export function CustomerLayout() {
  const currentCustomerId = 'c1'; // Mock current user
  const customer = getCustomerById(currentCustomerId);

  const sidebarLinks = [
    {
      to: '/customer',
      label: 'Dashboard',
      icon: <LayoutDashboard className="h-5 w-5" />,
    },
    {
      to: '/customer/pets',
      label: 'My Pets',
      icon: <PawPrint className="h-5 w-5" />,
    },
    {
      to: '/customer/book-appointment',
      label: 'Book Appointment',
      icon: <Calendar className="h-5 w-5" />,
    },
    {
      to: '/customer/profile',
      label: 'Profile',
      icon: <User className="h-5 w-5" />,
    },
  ];

  return (
    <div className="min-h-screen">
      <AppNavbar
        userName={`${customer?.firstName} ${customer?.lastName}`}
        userRole="customer"
      />
      <div className="flex">
        <AppSidebar links={sidebarLinks} />
        <main className="flex-1 p-4 md:p-8 pb-20 md:pb-8">
          <Outlet />
        </main>
      </div>
      <MobileNav links={sidebarLinks} />
    </div>
  );
}