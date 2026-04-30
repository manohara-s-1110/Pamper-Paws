import { Calendar, Clock, PawPrint, Plus } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/card';
import { Button } from '../../components/ui/button';
import { Badge } from '../../components/ui/badge';
import { getPetsByCustomerId, getAppointmentsByCustomerId, getVetById, getPetById } from '../../lib/data';
import { Link } from 'react-router';

export function CustomerDashboard() {
  const currentCustomerId = 'c1'; // Mock current user
  const myPets = getPetsByCustomerId(currentCustomerId);
  const myAppointments = getAppointmentsByCustomerId(currentCustomerId)
    .filter(a => a.status === 'upcoming')
    .sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());

  return (
    <div className="space-y-6">
      <div>
        <h1 className="mb-2">Dashboard</h1>
        <p className="text-muted-foreground">Welcome back! Here's an overview of your pets and appointments.</p>
      </div>

      {/* My Pets Section */}
      <div>
        <div className="flex items-center justify-between mb-4">
          <h2>My Pets</h2>
          <Link to="/customer/pets">
            <Button>
              <Plus className="h-4 w-4 mr-2" />
              Add Pet
            </Button>
          </Link>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {myPets.map((pet) => (
            <Card key={pet.id} className="overflow-hidden hover:shadow-lg transition-shadow">
              <div className="aspect-square relative overflow-hidden bg-muted">
                <img
                  src={pet.image}
                  alt={pet.name}
                  className="object-cover w-full h-full"
                />
              </div>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <PawPrint className="h-5 w-5 text-primary" />
                  {pet.name}
                </CardTitle>
                <CardDescription>
                  {pet.breed} • {pet.age} {pet.age === 1 ? 'year' : 'years'} old
                </CardDescription>
              </CardHeader>
            </Card>
          ))}
          {myPets.length === 0 && (
            <Card className="col-span-full">
              <CardContent className="flex flex-col items-center justify-center py-12">
                <PawPrint className="h-12 w-12 text-muted-foreground mb-4" />
                <p className="text-muted-foreground mb-4">You haven't added any pets yet</p>
                <Link to="/customer/pets">
                  <Button>
                    <Plus className="h-4 w-4 mr-2" />
                    Add Your First Pet
                  </Button>
                </Link>
              </CardContent>
            </Card>
          )}
        </div>
      </div>

      {/* Upcoming Appointments Section */}
      <div>
        <div className="flex items-center justify-between mb-4">
          <h2>Upcoming Appointments</h2>
          <Link to="/customer/book-appointment">
            <Button variant="outline">
              <Calendar className="h-4 w-4 mr-2" />
              Book Appointment
            </Button>
          </Link>
        </div>
        <div className="space-y-3">
          {myAppointments.map((appointment) => {
            const vet = getVetById(appointment.vetId);
            const pet = getPetById(appointment.petId);
            return (
              <Card key={appointment.id} className="hover:shadow-md transition-shadow">
                <CardContent className="p-6">
                  <div className="flex items-start justify-between">
                    <div className="flex gap-4">
                      <div className="flex flex-col items-center justify-center bg-primary/10 rounded-xl px-4 py-3 min-w-[80px]">
                        <p className="text-xs text-muted-foreground">
                          {new Date(appointment.date).toLocaleDateString('en-US', { month: 'short' })}
                        </p>
                        <p className="text-2xl font-semibold text-primary">
                          {new Date(appointment.date).getDate()}
                        </p>
                      </div>
                      <div className="space-y-2">
                        <div className="flex items-center gap-2">
                          <h3 className="font-semibold">{vet?.firstName} {vet?.lastName}</h3>
                          <Badge variant="secondary">{appointment.status}</Badge>
                        </div>
                        <div className="flex items-center gap-4 text-sm text-muted-foreground">
                          <div className="flex items-center gap-1">
                            <Clock className="h-4 w-4" />
                            <span>{appointment.timeSlot}</span>
                          </div>
                          <div className="flex items-center gap-1">
                            <PawPrint className="h-4 w-4" />
                            <span>{pet?.name}</span>
                          </div>
                        </div>
                        <p className="text-sm">{appointment.reason}</p>
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            );
          })}
          {myAppointments.length === 0 && (
            <Card>
              <CardContent className="flex flex-col items-center justify-center py-12">
                <Calendar className="h-12 w-12 text-muted-foreground mb-4" />
                <p className="text-muted-foreground mb-4">No upcoming appointments</p>
                <Link to="/customer/book-appointment">
                  <Button>
                    <Calendar className="h-4 w-4 mr-2" />
                    Book an Appointment
                  </Button>
                </Link>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
}
