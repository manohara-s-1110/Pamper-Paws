import { useState } from 'react';
import { Calendar, Clock, PawPrint, Filter } from 'lucide-react';
import { Card, CardContent } from '../../components/ui/card';
import { Badge } from '../../components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../../components/ui/tabs';
import { getAppointmentsByVetId, getCustomerById, getPetById } from '../../lib/data';

export function VetAppointments() {
  const currentVetId = 'v1'; // Mock current vet
  const allAppointments = getAppointmentsByVetId(currentVetId);
  
  const upcomingAppointments = allAppointments
    .filter(a => a.status === 'upcoming')
    .sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());
  
  const completedAppointments = allAppointments
    .filter(a => a.status === 'completed')
    .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());

  const AppointmentCard = ({ appointment }: { appointment: typeof allAppointments[0] }) => {
    const customer = getCustomerById(appointment.customerId);
    const pet = getPetById(appointment.petId);
    const appointmentDate = new Date(appointment.date);
    
    return (
      <Card className="hover:shadow-md transition-shadow">
        <CardContent className="p-6">
          <div className="flex items-start justify-between">
            <div className="flex gap-4">
              <div className="flex flex-col items-center justify-center bg-primary/10 rounded-xl px-4 py-3 min-w-[80px]">
                <p className="text-xs text-muted-foreground">
                  {appointmentDate.toLocaleDateString('en-US', { month: 'short' })}
                </p>
                <p className="text-2xl font-semibold text-primary">
                  {appointmentDate.getDate()}
                </p>
                <p className="text-xs text-muted-foreground">
                  {appointmentDate.getFullYear()}
                </p>
              </div>
              <div className="space-y-2 flex-1">
                <div className="flex items-center gap-2 flex-wrap">
                  <h3 className="font-semibold">
                    {customer?.firstName} {customer?.lastName}
                  </h3>
                  <Badge variant={appointment.status === 'upcoming' ? 'default' : 'secondary'}>
                    {appointment.status}
                  </Badge>
                </div>
                
                <div className="flex flex-wrap items-center gap-4 text-sm text-muted-foreground">
                  <div className="flex items-center gap-1">
                    <Clock className="h-4 w-4" />
                    <span>{appointment.timeSlot}</span>
                  </div>
                  <div className="flex items-center gap-1">
                    <PawPrint className="h-4 w-4" />
                    <span>{pet?.name} ({pet?.breed})</span>
                  </div>
                </div>
                
                <div className="space-y-1">
                  <p className="text-sm font-medium">Reason:</p>
                  <p className="text-sm text-muted-foreground">{appointment.reason}</p>
                </div>
                
                <div className="flex gap-4 text-xs text-muted-foreground pt-2">
                  <span>📞 {customer?.phone}</span>
                  <span>✉️ {customer?.email}</span>
                </div>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    );
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="mb-2">Appointments</h1>
        <p className="text-muted-foreground">View and manage your appointments</p>
      </div>

      <Tabs defaultValue="upcoming" className="w-full">
        <TabsList className="grid w-full max-w-md grid-cols-2">
          <TabsTrigger value="upcoming">
            Upcoming ({upcomingAppointments.length})
          </TabsTrigger>
          <TabsTrigger value="completed">
            Completed ({completedAppointments.length})
          </TabsTrigger>
        </TabsList>

        <TabsContent value="upcoming" className="space-y-4 mt-6">
          {upcomingAppointments.length > 0 ? (
            upcomingAppointments.map((appointment) => (
              <AppointmentCard key={appointment.id} appointment={appointment} />
            ))
          ) : (
            <Card>
              <CardContent className="flex flex-col items-center justify-center py-12">
                <Calendar className="h-12 w-12 text-muted-foreground mb-4" />
                <p className="text-muted-foreground">No upcoming appointments</p>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        <TabsContent value="completed" className="space-y-4 mt-6">
          {completedAppointments.length > 0 ? (
            completedAppointments.map((appointment) => (
              <AppointmentCard key={appointment.id} appointment={appointment} />
            ))
          ) : (
            <Card>
              <CardContent className="flex flex-col items-center justify-center py-12">
                <Calendar className="h-12 w-12 text-muted-foreground mb-4" />
                <p className="text-muted-foreground">No completed appointments</p>
              </CardContent>
            </Card>
          )}
        </TabsContent>
      </Tabs>
    </div>
  );
}
