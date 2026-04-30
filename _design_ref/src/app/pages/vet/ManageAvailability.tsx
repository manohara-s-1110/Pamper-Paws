import { useState } from 'react';
import { Calendar, Clock } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/card';
import { Button } from '../../components/ui/button';
import { Label } from '../../components/ui/label';
import { Checkbox } from '../../components/ui/checkbox';
import { Input } from '../../components/ui/input';
import { getVetById } from '../../lib/data';
import { toast } from 'sonner';

const daysOfWeek = [
  'Monday',
  'Tuesday',
  'Wednesday',
  'Thursday',
  'Friday',
  'Saturday',
  'Sunday',
];

export function ManageAvailability() {
  const currentVetId = 'v1'; // Mock current vet
  const vet = getVetById(currentVetId);

  const [selectedDays, setSelectedDays] = useState<string[]>(vet?.availableDays || []);
  const [startTime, setStartTime] = useState(vet?.availableTimeStart || '09:00');
  const [endTime, setEndTime] = useState(vet?.availableTimeEnd || '17:00');

  const handleDayToggle = (day: string) => {
    setSelectedDays((prev) =>
      prev.includes(day) ? prev.filter((d) => d !== day) : [...prev, day]
    );
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (selectedDays.length === 0) {
      toast.error('Please select at least one day');
      return;
    }
    toast.success('Availability updated successfully!');
  };

  const quickSelect = (days: string[]) => {
    setSelectedDays(days);
  };

  return (
    <div className="space-y-6 max-w-3xl">
      <div>
        <h1 className="mb-2">Manage Availability</h1>
        <p className="text-muted-foreground">Set your working days and hours</p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Available Days */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Calendar className="h-5 w-5 text-primary" />
              Available Days
            </CardTitle>
            <CardDescription>Select the days you're available for appointments</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {/* Quick Select Buttons */}
            <div className="flex flex-wrap gap-2">
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={() => quickSelect(['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'])}
              >
                Mon-Fri
              </Button>
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={() => quickSelect(['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'])}
              >
                Mon-Sat
              </Button>
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={() => quickSelect(daysOfWeek)}
              >
                Every Day
              </Button>
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={() => quickSelect([])}
              >
                Clear All
              </Button>
            </div>

            {/* Days Checkboxes */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {daysOfWeek.map((day) => (
                <div key={day} className="flex items-center space-x-2">
                  <Checkbox
                    id={day}
                    checked={selectedDays.includes(day)}
                    onCheckedChange={() => handleDayToggle(day)}
                  />
                  <Label
                    htmlFor={day}
                    className="text-sm font-normal cursor-pointer"
                  >
                    {day}
                  </Label>
                </div>
              ))}
            </div>

            {/* Selected Days Summary */}
            {selectedDays.length > 0 && (
              <div className="mt-4 p-4 bg-muted rounded-lg">
                <p className="text-sm font-medium mb-2">Selected Days:</p>
                <p className="text-sm text-muted-foreground">
                  {selectedDays.join(', ')}
                </p>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Available Time */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Clock className="h-5 w-5 text-primary" />
              Available Time
            </CardTitle>
            <CardDescription>Set your working hours</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="startTime">Start Time</Label>
                <Input
                  id="startTime"
                  type="time"
                  value={startTime}
                  onChange={(e) => setStartTime(e.target.value)}
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="endTime">End Time</Label>
                <Input
                  id="endTime"
                  type="time"
                  value={endTime}
                  onChange={(e) => setEndTime(e.target.value)}
                  required
                />
              </div>
            </div>

            {startTime && endTime && (
              <div className="mt-4 p-4 bg-muted rounded-lg">
                <p className="text-sm font-medium mb-1">Working Hours:</p>
                <p className="text-sm text-muted-foreground">
                  {startTime} - {endTime}
                </p>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Current Schedule Summary */}
        <Card className="bg-primary/5 border-primary/20">
          <CardHeader>
            <CardTitle>Your Current Schedule</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-2">
              <div className="flex items-start gap-2">
                <Calendar className="h-5 w-5 text-primary mt-0.5" />
                <div>
                  <p className="font-medium">Available Days:</p>
                  <p className="text-sm text-muted-foreground">
                    {selectedDays.length > 0 ? selectedDays.join(', ') : 'None selected'}
                  </p>
                </div>
              </div>
              <div className="flex items-start gap-2">
                <Clock className="h-5 w-5 text-primary mt-0.5" />
                <div>
                  <p className="font-medium">Working Hours:</p>
                  <p className="text-sm text-muted-foreground">
                    {startTime} - {endTime}
                  </p>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        <Button type="submit" size="lg" className="w-full">
          Save Availability
        </Button>
      </form>
    </div>
  );
}
