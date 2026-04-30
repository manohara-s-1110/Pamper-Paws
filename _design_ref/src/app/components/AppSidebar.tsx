import { Link, useLocation } from 'react-router';
import { cn } from './ui/utils';

interface SidebarLink {
  to: string;
  label: string;
  icon: React.ReactNode;
}

interface AppSidebarProps {
  links: SidebarLink[];
}

export function AppSidebar({ links }: AppSidebarProps) {
  const location = useLocation();

  return (
    <aside className="hidden md:block w-64 border-r bg-card min-h-[calc(100vh-4rem)] sticky top-16">
      <nav className="p-4 space-y-2">
        {links.map((link) => {
          const isActive = location.pathname === link.to;
          return (
            <Link
              key={link.to}
              to={link.to}
              className={cn(
                'flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-200',
                isActive
                  ? 'bg-primary text-primary-foreground shadow-sm'
                  : 'text-muted-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground'
              )}
            >
              {link.icon}
              <span className="font-medium">{link.label}</span>
            </Link>
          );
        })}
      </nav>
    </aside>
  );
}