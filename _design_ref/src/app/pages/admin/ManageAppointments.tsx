import { useState } from 'react';
import { Calendar, Search, Filter, Clock, PawPrint } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/card';
import { Input } from '../../components/ui/input';
import { Badge } from '../../components/ui/badge';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../../components/ui/select';
import { appointments, getCustomerById, getVetById, getPetById } from '../../lib/data';

export function ManageAppointments() {
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [vetFilter, setVetFilter] = useState('all');

  const filteredAppointments = appointments.filter(appointment => {
    const customer = getCustomerById(appointment.customerId);
    const vet = getVetById(appointment.vetId);
    const pet = getPetById(appointment.petId);

    const matchesSearch = searchTerm === '' || 
      `${customer?.firstName} ${customer?.lastName} ${vet?.firstName} ${vet?.lastName} ${pet?.name}`
        .toLowerCase()
        .includes(searchTerm.toLowerCase());

    const matchesStatus = statusFilter === 'all' || appointment.status === statusFilter;
    const matchesVet = vetFilter === 'all' || appointment.vetId === vetFilter;

    return matchesSearch && matchesStatus && matchesVet;
  });

  const sortedAppointments = [...filteredAppointments].sort(
    (a, b) => new Date(b.date).getTime() - new Date(a.date).getTime()
  );

  return (
    <div className="space-y-6">
      <div>
        <h1 className="mb-2">Manage Appointments</h1>
        <p className="text-muted-foreground">View and filter all appointments in the system</p>
      </div>

      {/* Filters */}
      <Card>
        <CardContent className="pt-6">
          <div className="space-y-4">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Search by customer, vet, or pet name..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10"
              />
            </div>
            
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <label className="text-sm font-medium">Status</label>
                <Select value={statusFilter} onValueChange={setStatusFilter}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">All Statuses</SelectItem>
                    <SelectItem value="upcoming">Upcoming</SelectItem>
                    <SelectItem value="completed">Completed</SelectItem>
                    <SelectItem value="cancelled">Cancelled</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium">Veterinarian</label>
                <Select value={vetFilter} onValueChange={setVetFilter}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">All Vets</SelectItem>
                    <SelectItem value="v1">Dr. Emily Rodriguez</SelectItem>
                    <SelectItem value="v2">Dr. James Wilson</SelectItem>
                    <SelectItem value="v3">Dr. Aisha Patel</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Results Summary */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Appointment List</CardTitle>
              <CardDescription>
                Showing {sortedAppointments.length} of {appointments.length} appointments
              </CardDescription>
            </div>
            <Badge variant="outline" className="text-sm">
              <Filter className="h-3 w-3 mr-1" />
              {sortedAppointments.length} Results
            </Badge>
          </div>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            {sortedAppointments.map((appointment) => {
              const customer = getCustomerById(appointment.customerId);
              const vet = getVetById(appointment.vetId);
              const pet = getPetById(appointment.petId);
              const appointmentDate = new Date(appointment.date);

              return (
                <Card key={appointment.id} className="hover:shadow-md transition-shadow">
                  <CardContent className="p-6">
                    <div className="flex items-start justify-between">
                      <div className="flex gap-4 flex-1">
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
                            <Badge
                              variant={
                                appointment.status === 'upcoming'
                                  ? 'default'
                                  : appointment.status === 'completed'
                                  ? 'secondary'
                                  : 'destructive'
                              }
                            >
                              {appointment.status}
                            </Badge>
                            <span className="text-sm text-muted-foreground">#{appointment.id}</span>
                          </div>

                          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-sm">
                            <div>
                              <p className="text-muted-foreground">Customer:</p>
                              <p className="font-medium">
                                {customer?.firstName} {customer?.lastName}
                              </p>
                            </div>
                            <div>
                              <p className="text-muted-foreground">Veterinarian:</p>
                              <p className="font-medium">
                                {vet?.firstName} {vet?.lastName}
                              </p>
                            </div>
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

                          <div className="pt-1">
                            <p className="text-sm font-medium">Reason:</p>
                            <p className="text-sm text-muted-foreground">{appointment.reason}</p>
                          </div>
                        </div>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              );
            })}

            {sortedAppointments.length === 0 && (
              <div className="text-center py-12">
                <Calendar className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <p className="text-muted-foreground">No appointments found</p>
                <p className="text-sm text-muted-foreground mt-2">
                  Try adjusting your filters
                </p>
              </div>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
