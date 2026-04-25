import { Outlet } from 'react-router';
import { LayoutDashboard, Calendar, Clock, User } from 'lucide-react';
import { AppNavbar } from '../components/AppNavbar';
import { AppSidebar } from '../components/AppSidebar';
import { MobileNav } from '../components/MobileNav';
import { getVetById } from '../lib/data';

export function VetLayout() {
  const currentVetId = 'v1'; // Mock current user
  const vet = getVetById(currentVetId);

  const sidebarLinks = [
    {
      to: '/vet',
      label: 'Dashboard',
      icon: <LayoutDashboard className="h-5 w-5" />,
    },
    {
      to: '/vet/appointments',
      label: 'Appointments',
      icon: <Calendar className="h-5 w-5" />,
    },
    {
      to: '/vet/availability',
      label: 'Manage Availability',
      icon: <Clock className="h-5 w-5" />,
    },
    {
      to: '/vet/profile',
      label: 'Profile',
      icon: <User className="h-5 w-5" />,
    },
  ];

  return (
    <div className="min-h-screen">
      <AppNavbar
        userName={`${vet?.firstName} ${vet?.lastName}`}
        userRole="veterinarian"
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