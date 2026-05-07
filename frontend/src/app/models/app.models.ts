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
  consultationFee?: number;
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
  petName?: string;
  status?: 'PENDING' | 'CONFIRMED' | 'COMPLETED' | 'MISSED' | 'CANCELLED';
  paymentMethod?: 'ONLINE' | 'CASH';
  paymentStatus?: 'PENDING' | 'SUCCESS' | 'FAILED';
  consultationFee?: number;
  payment?: PaymentInitiateResponse;
}

export interface RegisterAuthPayload {
  username: string;
  password: string;
  role: UserRole;
  name: string;
  email: string;
  phone: string;
  address: string;
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
  consultationFee: number;
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
  consultationFee: number;
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
  paymentMethod: 'ONLINE' | 'CASH';
}

export interface PaymentInitiateResponse {
  paymentId: number;
  appointmentId: number;
  userId: number;
  amount: number;
  paymentMethod: 'ONLINE' | 'CASH';
  paymentStatus: 'PENDING' | 'SUCCESS' | 'FAILED';
  razorpayOrderId?: string;
  razorpayKeyId?: string;
  currency?: string;
}

export interface PaymentInitiatePayload {
  appointmentId: number;
  userId: number;
  amount: number;
  paymentMethod: 'ONLINE' | 'CASH';
}

export interface PaymentVerifyPayload {
  appointmentId: number;
  razorpayOrderId: string;
  razorpayPaymentId: string;
  razorpaySignature: string;
}

export interface Payment {
  id: number;
  appointmentId: number;
  userId: number;
  amount: number;
  paymentMethod: 'ONLINE' | 'CASH';
  paymentStatus: 'PENDING' | 'SUCCESS' | 'FAILED';
  transactionId?: string;
  razorpayOrderId?: string;
  createdAt: string;
}

export interface VetLeave {
  id: number;
  vetId: number;
  date: string;
}

export interface ChangePasswordPayload {
  currentPassword: string;
  newPassword: string;
}
