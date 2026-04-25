import { Visit } from '../models/app.models';

export type AppointmentStatus = 'Completed' | 'Today' | 'Upcoming';
export type AppointmentFilter = 'all' | 'today' | 'upcoming' | 'past';

export function appointmentDateTimeValue(visit: Pick<Visit, 'visitDate' | 'timeSlot'>): number {
  return Date.parse(`${visit.visitDate}T${normalizeTimeSlot(visit.timeSlot)}`);
}

export function getAppointmentStatus(
  visit: Pick<Visit, 'visitDate' | 'timeSlot'>,
  referenceDate = new Date(),
): AppointmentStatus {
  const visitDate = new Date(`${visit.visitDate}T00:00:00`);
  const today = new Date(referenceDate);
  today.setHours(0, 0, 0, 0);

  if (visitDate.getTime() < today.getTime()) {
    return 'Completed';
  }

  if (visitDate.getTime() === today.getTime()) {
    return 'Today';
  }

  return 'Upcoming';
}

export function isVisitInFilter(visit: Pick<Visit, 'visitDate' | 'timeSlot'>, filter: AppointmentFilter) {
  const status = getAppointmentStatus(visit);

  switch (filter) {
    case 'today':
      return status === 'Today';
    case 'upcoming':
      return status === 'Upcoming';
    case 'past':
      return status === 'Completed';
    default:
      return true;
  }
}

function normalizeTimeSlot(timeSlot: string): string {
  const trimmed = timeSlot.trim();
  if (/^\d{2}:\d{2}$/.test(trimmed)) {
    return `${trimmed}:00`;
  }

  const twelveHourMatch = trimmed.match(/^(\d{1,2}):(\d{2})\s*([AP]M)$/i);
  if (!twelveHourMatch) {
    return '00:00:00';
  }

  const [, rawHour, minute, meridiem] = twelveHourMatch;
  let hour = Number(rawHour) % 12;
  if (meridiem.toUpperCase() === 'PM') {
    hour += 12;
  }

  return `${String(hour).padStart(2, '0')}:${minute}:00`;
}
