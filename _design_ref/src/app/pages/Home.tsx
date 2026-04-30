import { Link } from 'react-router';
import { PawPrint, Users, Stethoscope, Shield } from 'lucide-react';
import { Button } from '../components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';

export function Home() {
  return (
    <div className="min-h-screen bg-gradient-to-b from-background to-muted/20">
      {/* Header */}
      <header className="border-b bg-card">
        <div className="container mx-auto px-6 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-primary">
                <PawPrint className="h-6 w-6 text-primary-foreground" />
              </div>
              <span className="text-xl font-semibold">Pamper Paws</span>
            </div>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="container mx-auto px-6 py-20">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <h1 className="text-4xl sm:text-5xl lg:text-6xl font-bold mb-6">
            Complete Pet Care Management
          </h1>
          <p className="text-xl text-muted-foreground mb-8">
            A modern platform connecting pet owners with veterinarians for seamless pet care
          </p>
          <div className="flex flex-wrap gap-4 justify-center">
            <Link to="/customer">
              <Button size="lg" className="text-lg px-8">
                <Users className="mr-2 h-5 w-5" />
                Customer Portal
              </Button>
            </Link>
            <Link to="/vet">
              <Button size="lg" variant="outline" className="text-lg px-8">
                <Stethoscope className="mr-2 h-5 w-5" />
                Vet Portal
              </Button>
            </Link>
            <Link to="/admin">
              <Button size="lg" variant="outline" className="text-lg px-8">
                <Shield className="mr-2 h-5 w-5" />
                Admin Portal
              </Button>
            </Link>
          </div>
        </div>

        {/* Role Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-6xl mx-auto">
          <Card className="hover:shadow-lg transition-shadow">
            <CardHeader>
              <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-primary/10 mb-4">
                <Users className="h-6 w-6 text-primary" />
              </div>
              <CardTitle>For Pet Owners</CardTitle>
              <CardDescription>
                Manage your pets, book appointments, and keep track of their health
              </CardDescription>
            </CardHeader>
            <CardContent>
              <ul className="space-y-2 text-sm text-muted-foreground">
                <li>✓ Manage pet profiles</li>
                <li>✓ Book appointments online</li>
                <li>✓ View appointment history</li>
                <li>✓ Track pet health records</li>
              </ul>
              <Link to="/customer" className="mt-4 block">
                <Button variant="outline" className="w-full">
                  Access Customer Portal
                </Button>
              </Link>
            </CardContent>
          </Card>

          <Card className="hover:shadow-lg transition-shadow">
            <CardHeader>
              <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-primary/10 mb-4">
                <Stethoscope className="h-6 w-6 text-primary" />
              </div>
              <CardTitle>For Veterinarians</CardTitle>
              <CardDescription>
                Manage your schedule, appointments, and provide quality care
              </CardDescription>
            </CardHeader>
            <CardContent>
              <ul className="space-y-2 text-sm text-muted-foreground">
                <li>✓ View daily appointments</li>
                <li>✓ Manage availability</li>
                <li>✓ Track patient history</li>
                <li>✓ Professional dashboard</li>
              </ul>
              <Link to="/vet" className="mt-4 block">
                <Button variant="outline" className="w-full">
                  Access Vet Portal
                </Button>
              </Link>
            </CardContent>
          </Card>

          <Card className="hover:shadow-lg transition-shadow">
            <CardHeader>
              <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-primary/10 mb-4">
                <Shield className="h-6 w-6 text-primary" />
              </div>
              <CardTitle>For Administrators</CardTitle>
              <CardDescription>
                Oversee operations, manage users, and monitor system performance
              </CardDescription>
            </CardHeader>
            <CardContent>
              <ul className="space-y-2 text-sm text-muted-foreground">
                <li>✓ Manage users & vets</li>
                <li>✓ Monitor appointments</li>
                <li>✓ View analytics</li>
                <li>✓ System administration</li>
              </ul>
              <Link to="/admin" className="mt-4 block">
                <Button variant="outline" className="w-full">
                  Access Admin Portal
                </Button>
              </Link>
            </CardContent>
          </Card>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t mt-20">
        <div className="container mx-auto px-6 py-8">
          <div className="text-center text-muted-foreground">
            <p>© 2026 Pamper Paws. All rights reserved.</p>
            <p className="text-sm mt-2">Modern pet care management system</p>
          </div>
        </div>
      </footer>
    </div>
  );
}
