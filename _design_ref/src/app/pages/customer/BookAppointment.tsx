import { useState } from 'react';
import { Calendar as CalendarIcon, Clock, MapPin, Award, Briefcase, PawPrint } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/card';
import { Button } from '../../components/ui/button';
import { Label } from '../../components/ui/label';
import { Calendar } from '../../components/ui/calendar';
import { Popover, PopoverContent, PopoverTrigger } from '../../components/ui/popover';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../../components/ui/select';
import { Textarea } from '../../components/ui/textarea';
import { vets, getPetsByCustomerId, timeSlots, appointments } from '../../lib/data';
import { cn } from '../../components/ui/utils';
import { format } from 'date-fns';
import { toast } from 'sonner';

export function BookAppointment() {
  const currentCustomerId = 'c1'; // Mock current user
  const myPets = getPetsByCustomerId(currentCustomerId);
  
  const [selectedDate, setSelectedDate] = useState<Date>();
  const [selectedVet, setSelectedVet] = useState<string>('');
  const [selectedPet, setSelectedPet] = useState<string>('');
  const [selectedTimeSlot, setSelectedTimeSlot] = useState<string>('');
  const [reason, setReason] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedDate || !selectedVet || !selectedPet || !selectedTimeSlot || !reason) {
      toast.error('Please fill in all fields');
      return;
    }

    const vet = vets.find(v => v.id === selectedVet);
    const pet = myPets.find(p => p.id === selectedPet);

    toast.success(
      `Appointment booked with ${vet?.firstName} ${vet?.lastName} for ${pet?.name} on ${format(selectedDate, 'PPP')} at ${selectedTimeSlot}`
    );

    // Reset form
    setSelectedDate(undefined);
    setSelectedVet('');
    setSelectedPet('');
    setSelectedTimeSlot('');
    setReason('');
  };

  // Check if a time slot is already booked
  const isTimeSlotBooked = (timeSlot: string) => {
    if (!selectedDate || !selectedVet) return false;
    
    const dateStr = format(selectedDate, 'yyyy-MM-dd');
    return appointments.some(
      apt => apt.vetId === selectedVet && apt.date === dateStr && apt.timeSlot === timeSlot
    );
  };

  const selectedVetData = vets.find(v => v.id === selectedVet);

  return (
    <div className="space-y-6 max-w-4xl">
      <div>
        <h1 className="mb-2">Book an Appointment</h1>
        <p className="text-muted-foreground">Schedule a visit for your pet with one of our veterinarians</p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Select Pet */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <PawPrint className="h-5 w-5 text-primary" />
              Select Pet
            </CardTitle>
            <CardDescription>Choose which pet needs to see the vet</CardDescription>
          </CardHeader>
          <CardContent>
            <Select value={selectedPet} onValueChange={setSelectedPet}>
              <SelectTrigger>
                <SelectValue placeholder="Select a pet" />
              </SelectTrigger>
              <SelectContent>
                {myPets.map((pet) => (
                  <SelectItem key={pet.id} value={pet.id}>
                    {pet.name} ({pet.breed})
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </CardContent>
        </Card>

        {/* Select Veterinarian */}
        <Card>
          <CardHeader>
            <CardTitle>Select Veterinarian</CardTitle>
            <CardDescription>Choose your preferred veterinarian</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid gap-4">
              {vets.map((vet) => (
                <Card
                  key={vet.id}
                  className={cn(
                    'cursor-pointer transition-all hover:shadow-md',
                    selectedVet === vet.id && 'ring-2 ring-primary'
                  )}
                  onClick={() => setSelectedVet(vet.id)}
                >
                  <CardContent className="p-4">
                    <div className="flex items-start justify-between">
                      <div className="space-y-2">
                        <h4 className="font-semibold">{vet.firstName} {vet.lastName}</h4>
                        <div className="space-y-1 text-sm text-muted-foreground">
                          <div className="flex items-center gap-2">
                            <Award className="h-4 w-4" />
                            <span>{vet.specialization}</span>
                          </div>
                          <div className="flex items-center gap-2">
                            <Briefcase className="h-4 w-4" />
                            <span>{vet.experience} years experience</span>
                          </div>
                          <div className="flex items-center gap-2">
                            <MapPin className="h-4 w-4" />
                            <span>{vet.clinicAddress}</span>
                          </div>
                        </div>
                      </div>
                      {selectedVet === vet.id && (
                        <div className="flex h-6 w-6 items-center justify-center rounded-full bg-primary">
                          <div className="h-2 w-2 rounded-full bg-primary-foreground" />
                        </div>
                      )}
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* Select Date */}
        <Card>
          <CardHeader>
            <CardTitle>Select Date</CardTitle>
            <CardDescription>Choose a date for your appointment</CardDescription>
          </CardHeader>
          <CardContent>
            <Popover>
              <PopoverTrigger asChild>
                <Button
                  variant="outline"
                  className={cn(
                    'w-full justify-start text-left font-normal',
                    !selectedDate && 'text-muted-foreground'
                  )}
                >
                  <CalendarIcon className="mr-2 h-4 w-4" />
                  {selectedDate ? format(selectedDate, 'PPP') : 'Pick a date'}
                </Button>
              </PopoverTrigger>
              <PopoverContent className="w-auto p-0" align="start">
                <Calendar
                  mode="single"
                  selected={selectedDate}
                  onSelect={setSelectedDate}
                  disabled={(date) => {
                    const isPast = date < new Date(new Date().setHours(0, 0, 0, 0));
                    const dayName = date.toLocaleDateString('en-US', { weekday: 'long' });
                    const isVetAvailable = selectedVetData?.availableDays.includes(dayName) ?? true;
                    return isPast || !isVetAvailable;
                  }}
                  initialFocus
                />
              </PopoverContent>
            </Popover>
          </CardContent>
        </Card>

        {/* Select Time Slot */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Clock className="h-5 w-5 text-primary" />
              Select Time Slot
            </CardTitle>
            <CardDescription>Choose an available time for your appointment</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-3 sm:grid-cols-4 gap-3">
              {timeSlots.map((slot) => {
                const isBooked = isTimeSlotBooked(slot);
                const isDisabled = isBooked || !selectedDate || !selectedVet;
                
                return (
                  <Button
                    key={slot}
                    type="button"
                    variant={selectedTimeSlot === slot ? 'default' : 'outline'}
                    disabled={isDisabled}
                    onClick={() => setSelectedTimeSlot(slot)}
                    className={cn(
                      'h-auto py-3',
                      isBooked && 'opacity-50 cursor-not-allowed'
                    )}
                  >
                    {slot}
                  </Button>
                );
              })}
            </div>
            {selectedDate && selectedVet && (
              <p className="text-xs text-muted-foreground mt-4">
                * Greyed out slots are already booked
              </p>
            )}
          </CardContent>
        </Card>

        {/* Reason for Visit */}
        <Card>
          <CardHeader>
            <CardTitle>Reason for Visit</CardTitle>
            <CardDescription>Please describe the reason for this appointment</CardDescription>
          </CardHeader>
          <CardContent>
            <Textarea
              placeholder="E.g., Regular checkup, vaccination, skin issue, etc."
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              className="min-h-[100px]"
              required
            />
          </CardContent>
        </Card>

        <Button type="submit" size="lg" className="w-full">
          <CalendarIcon className="mr-2 h-5 w-5" />
          Book Appointment
        </Button>
      </form>
    </div>
  );
}
