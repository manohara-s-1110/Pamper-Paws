import { Calendar, Clock, Users, PawPrint } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/card';
import { Badge } from '../../components/ui/badge';
import { getAppointmentsByVetId, getCustomerById, getPetById } from '../../lib/data';

export function VetDashboard() {
  const currentVetId = 'v1'; // Mock current vet
  const allAppointments = getAppointmentsByVetId(currentVetId);
  const upcomingAppointments = allAppointments.filter(a => a.status === 'upcoming');
  
  // Get today's appointments
  const today = new Date().toISOString().split('T')[0];
  const todaysAppointments = upcomingAppointments
    .filter(a => a.date === today)
    .sort((a, b) => a.timeSlot.localeCompare(b.timeSlot));

  return (
    <div className="space-y-6">
      <div>
        <h1 className="mb-2">Veterinarian Dashboard</h1>
        <p className="text-muted-foreground">Overview of your appointments and schedule</p>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Total Appointments</CardTitle>
            <Calendar className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-primary">{allAppointments.length}</div>
            <p className="text-xs text-muted-foreground">All time</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Upcoming Visits</CardTitle>
            <Clock className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-primary">{upcomingAppointments.length}</div>
            <p className="text-xs text-muted-foreground">Scheduled appointments</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Today's Appointments</CardTitle>
            <Users className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-primary">{todaysAppointments.length}</div>
            <p className="text-xs text-muted-foreground">Appointments today</p>
          </CardContent>
        </Card>
      </div>

      {/* Today's Appointments */}
      <div>
        <h2 className="mb-4">Today's Appointments</h2>
        <div className="space-y-3">
          {todaysAppointments.map((appointment) => {
            const customer = getCustomerById(appointment.customerId);
            const pet = getPetById(appointment.petId);
            
            return (
              <Card key={appointment.id} className="hover:shadow-md transition-shadow">
                <CardContent className="p-6">
                  <div className="flex items-start justify-between">
                    <div className="flex gap-4">
                      <div className="flex flex-col items-center justify-center bg-primary/10 rounded-xl px-4 py-3 min-w-[80px]">
                        <Clock className="h-5 w-5 text-primary mb-1" />
                        <p className="text-sm font-semibold">{appointment.timeSlot}</p>
                      </div>
                      <div className="space-y-2">
                        <div className="flex items-center gap-2">
                          <h3 className="font-semibold">
                            {customer?.firstName} {customer?.lastName}
                          </h3>
                          <Badge variant="secondary">{appointment.status}</Badge>
                        </div>
                        <div className="flex items-center gap-2 text-sm text-muted-foreground">
                          <PawPrint className="h-4 w-4" />
                          <span>{pet?.name} - {pet?.breed}</span>
                        </div>
                        <p className="text-sm">{appointment.reason}</p>
                        <div className="flex gap-2 text-xs text-muted-foreground">
                          <span>📞 {customer?.phone}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            );
          })}
          
          {todaysAppointments.length === 0 && (
            <Card>
              <CardContent className="flex flex-col items-center justify-center py-12">
                <Calendar className="h-12 w-12 text-muted-foreground mb-4" />
                <p className="text-muted-foreground">No appointments scheduled for today</p>
              </CardContent>
            </Card>
          )}
        </div>
      </div>

      {/* Upcoming Appointments */}
      <div>
        <h2 className="mb-4">Upcoming Appointments</h2>
        <div className="space-y-3">
          {upcomingAppointments
            .filter(a => a.date !== today)
            .slice(0, 5)
            .map((appointment) => {
              const customer = getCustomerById(appointment.customerId);
              const pet = getPetById(appointment.petId);
              
              return (
                <Card key={appointment.id} className="hover:shadow-md transition-shadow">
                  <CardContent className="p-6">
                    <div className="flex items-start justify-between">
                      <div className="flex gap-4">
                        <div className="flex flex-col items-center justify-center bg-secondary rounded-xl px-4 py-3 min-w-[80px]">
                          <p className="text-xs text-muted-foreground">
                            {new Date(appointment.date).toLocaleDateString('en-US', { month: 'short' })}
                          </p>
                          <p className="text-2xl font-semibold text-secondary-foreground">
                            {new Date(appointment.date).getDate()}
                          </p>
                        </div>
                        <div className="space-y-2">
                          <div className="flex items-center gap-2">
                            <h3 className="font-semibold">
                              {customer?.firstName} {customer?.lastName}
                            </h3>
                            <Badge variant="outline">{appointment.timeSlot}</Badge>
                          </div>
                          <div className="flex items-center gap-2 text-sm text-muted-foreground">
                            <PawPrint className="h-4 w-4" />
                            <span>{pet?.name} - {pet?.breed}</span>
                          </div>
                          <p className="text-sm">{appointment.reason}</p>
                        </div>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              );
            })}
        </div>
      </div>
    </div>
  );
}
