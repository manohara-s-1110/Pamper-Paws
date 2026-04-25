export type UserRole = 'CUSTOMER' | 'VET' | 'ADMIN';

export interface AuthSession {
  token: string;
  username: string;
  role: UserRole;
}

export interface NavItem {
  label: string;
  path: string;
  exact?: boolean;
}

export interface Customer {
  id: number;
  username: string;
  name: string;
  email: string;
  phone: string;
  address: string;
}

export interface Pet {
  id: number;
  customerId?: number;
  name: string;
  type: string;
  age: number;
  imageUrl?: string;
}

export interface Vet {
  id: number;
  username: string;
  name: string;
  specialization: string;
  experience: number;
  phone: string;
  email: string;
  clinicAddress: string;
  availableDays: string;
  availableTime: string;
}

export interface Visit {
  id: number;
  customerId: number;
  vetId: number;
  petId?: number;
  visitDate: string;
  timeSlot: string;
  reason: string;
  notes?: string;
}

export interface RegisterAuthPayload {
  username: string;
  password: string;
  role: UserRole;
  name: string;
}

export interface VetRegisterPayload {
  username: string;
  password: string;
  name: string;
  email: string;
  phone: string;
  specialization: string;
  experience: number;
  clinicAddress: string;
  availableDays: string;
  availableTime: string;
}

export interface CustomerPayload {
  username: string;
  name: string;
  email: string;
  phone: string;
  address: string;
}

export interface VetPayload {
  username: string;
  name: string;
  specialization: string;
  experience: number;
  phone: string;
  email: string;
  clinicAddress: string;
  availableDays: string;
  availableTime: string;
}

export interface PetPayload {
  name: string;
  type: string;
  age: number;
  imageUrl?: string;
}

export interface VisitPayload {
  customerId: number;
  vetId: number;
  petId?: number;
  visitDate: string;
  timeSlot: string;
  reason: string;
}

export interface ChangePasswordPayload {
  currentPassword: string;
  newPassword: string;
}
