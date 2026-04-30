import { Outlet } from 'react-router';
import { LayoutDashboard, Users, Calendar } from 'lucide-react';
import { AppNavbar } from '../components/AppNavbar';
import { AppSidebar } from '../components/AppSidebar';
import { MobileNav } from '../components/MobileNav';

export function AdminLayout() {
  const sidebarLinks = [
    {
      to: '/admin',
      label: 'Dashboard',
      icon: <LayoutDashboard className="h-5 w-5" />,
    },
    {
      to: '/admin/users',
      label: 'Manage Users',
      icon: <Users className="h-5 w-5" />,
    },
    {
      to: '/admin/appointments',
      label: 'Manage Appointments',
      icon: <Calendar className="h-5 w-5" />,
    },
  ];

  return (
    <div className="min-h-screen">
      <AppNavbar userName="Admin User" userRole="admin" />
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