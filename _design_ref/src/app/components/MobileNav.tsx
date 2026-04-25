import { Link, useLocation } from 'react-router';
import { cn } from './ui/utils';

interface SidebarLink {
  to: string;
  label: string;
  icon: React.ReactNode;
}

interface MobileNavProps {
  links: SidebarLink[];
}

export function MobileNav({ links }: MobileNavProps) {
  const location = useLocation();

  return (
    <nav className="md:hidden fixed bottom-0 left-0 right-0 z-50 border-t bg-card">
      <div className="flex items-center justify-around px-2 py-3">
        {links.map((link) => {
          const isActive = location.pathname === link.to;
          return (
            <Link
              key={link.to}
              to={link.to}
              className={cn(
                'flex flex-col items-center gap-1 px-3 py-2 rounded-xl transition-all duration-200 min-w-[60px]',
                isActive
                  ? 'text-primary'
                  : 'text-muted-foreground'
              )}
            >
              {link.icon}
              <span className="text-xs font-medium">{link.label}</span>
            </Link>
          );
        })}
      </div>
    </nav>
  );
}
