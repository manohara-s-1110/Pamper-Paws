// Mock data for the pet care management system

export type UserRole = 'customer' | 'vet' | 'admin';

export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  role: UserRole;
  gender?: string;
  address?: string;
  isActive: boolean;
}

export interface Customer extends User {
  role: 'customer';
}

export interface Vet extends User {
  role: 'vet';
  specialization: string;
  experience: number;
  clinicAddress: string;
  availableDays: string[];
  availableTimeStart: string;
  availableTimeEnd: string;
}

export interface Pet {
  id: string;
  name: string;
  breed: string;
  age: number;
  image: string;
  customerId: string;
}

export interface Appointment {
  id: string;
  customerId: string;
  vetId: string;
  petId: string;
  date: string;
  timeSlot: string;
  reason: string;
  status: 'upcoming' | 'completed' | 'cancelled';
}

// Mock customers
export const customers: Customer[] = [
  {
    id: 'c1',
    firstName: 'Sarah',
    lastName: 'Johnson',
    email: 'sarah.j@email.com',
    phone: '+1 234-567-8901',
    role: 'customer',
    gender: 'Female',
    address: '123 Oak Street, San Francisco, CA 94102',
    isActive: true,
  },
  {
    id: 'c2',
    firstName: 'Michael',
    lastName: 'Chen',
    email: 'michael.c@email.com',
    phone: '+1 234-567-8902',
    role: 'customer',
    gender: 'Male',
    address: '456 Pine Avenue, Los Angeles, CA 90001',
    isActive: true,
  },
];

// Mock vets
export const vets: Vet[] = [
  {
    id: 'v1',
    firstName: 'Dr. Emily',
    lastName: 'Rodriguez',
    email: 'emily.r@pamperpaws.com',
    phone: '+1 234-567-9001',
    role: 'vet',
    specialization: 'General Practice',
    experience: 8,
    clinicAddress: 'Pamper Paws Clinic, 789 Main St, San Francisco, CA 94105',
    availableDays: ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'],
    availableTimeStart: '09:00',
    availableTimeEnd: '17:00',
    isActive: true,
  },
  {
    id: 'v2',
    firstName: 'Dr. James',
    lastName: 'Wilson',
    email: 'james.w@pamperpaws.com',
    phone: '+1 234-567-9002',
    role: 'vet',
    specialization: 'Surgery & Orthopedics',
    experience: 12,
    clinicAddress: 'Pamper Paws Clinic, 321 Health Blvd, Los Angeles, CA 90015',
    availableDays: ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'],
    availableTimeStart: '10:00',
    availableTimeEnd: '18:00',
    isActive: true,
  },
  {
    id: 'v3',
    firstName: 'Dr. Aisha',
    lastName: 'Patel',
    email: 'aisha.p@pamperpaws.com',
    phone: '+1 234-567-9003',
    role: 'vet',
    specialization: 'Dermatology',
    experience: 6,
    clinicAddress: 'Pamper Paws Clinic, 555 Care Lane, San Diego, CA 92101',
    availableDays: ['Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'],
    availableTimeStart: '08:00',
    availableTimeEnd: '16:00',
    isActive: true,
  },
];

// Mock admin
export const admin: User = {
  id: 'a1',
  firstName: 'Admin',
  lastName: 'User',
  email: 'admin@pamperpaws.com',
  phone: '+1 234-567-0000',
  role: 'admin',
  isActive: true,
};

// Mock pets
export const pets: Pet[] = [
  {
    id: 'p1',
    name: 'Max',
    breed: 'Golden Retriever',
    age: 3,
    image: 'https://images.unsplash.com/photo-1633722715463-d30f4f325e24?w=400&h=400&fit=crop',
    customerId: 'c1',
  },
  {
    id: 'p2',
    name: 'Luna',
    breed: 'Persian Cat',
    age: 2,
    image: 'https://images.unsplash.com/photo-1574158622682-e40e69881006?w=400&h=400&fit=crop',
    customerId: 'c1',
  },
  {
    id: 'p3',
    name: 'Charlie',
    breed: 'Beagle',
    age: 4,
    image: 'https://images.unsplash.com/photo-1505628346881-b72b27e84530?w=400&h=400&fit=crop',
    customerId: 'c2',
  },
];

// Mock appointments
export const appointments: Appointment[] = [
  {
    id: 'a1',
    customerId: 'c1',
    vetId: 'v1',
    petId: 'p1',
    date: '2026-04-18',
    timeSlot: '10:00 AM',
    reason: 'Regular checkup and vaccination',
    status: 'upcoming',
  },
  {
    id: 'a2',
    customerId: 'c1',
    vetId: 'v2',
    petId: 'p2',
    date: '2026-04-20',
    timeSlot: '02:00 PM',
    reason: 'Skin consultation',
    status: 'upcoming',
  },
  {
    id: 'a3',
    customerId: 'c2',
    vetId: 'v1',
    petId: 'p3',
    date: '2026-04-16',
    timeSlot: '11:00 AM',
    reason: 'Dental cleaning',
    status: 'upcoming',
  },
  {
    id: 'a4',
    customerId: 'c1',
    vetId: 'v1',
    petId: 'p1',
    date: '2026-04-10',
    timeSlot: '03:00 PM',
    reason: 'Follow-up consultation',
    status: 'completed',
  },
];

// Available time slots
export const timeSlots = [
  '09:00 AM',
  '10:00 AM',
  '11:00 AM',
  '12:00 PM',
  '01:00 PM',
  '02:00 PM',
  '03:00 PM',
  '04:00 PM',
  '05:00 PM',
];

// Helper function to get user by id
export function getUserById(id: string): User | undefined {
  const allUsers = [...customers, ...vets, admin];
  return allUsers.find(u => u.id === id);
}

// Helper function to get vet by id
export function getVetById(id: string): Vet | undefined {
  return vets.find(v => v.id === id);
}

// Helper function to get customer by id
export function getCustomerById(id: string): Customer | undefined {
  return customers.find(c => c.id === id);
}

// Helper function to get pet by id
export function getPetById(id: string): Pet | undefined {
  return pets.find(p => p.id === id);
}

// Helper function to get pets by customer
export function getPetsByCustomerId(customerId: string): Pet[] {
  return pets.filter(p => p.customerId === customerId);
}

// Helper function to get appointments by customer
export function getAppointmentsByCustomerId(customerId: string): Appointment[] {
  return appointments.filter(a => a.customerId === customerId);
}

// Helper function to get appointments by vet
export function getAppointmentsByVetId(vetId: string): Appointment[] {
  return appointments.filter(a => a.vetId === vetId);
}
