export type CustomerType = "PESSOA_FISICA" | "PESSOA_JURIDICA";
export type PolicyType = "AUTO" | "RESIDENCIAL" | "VIDA" | "SAUDE" | "EMPRESARIAL" | "VIAGEM" | "OUTROS";
export type PolicyStatus = "VIGENTE" | "VENCIDA" | "CANCELADA";

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface Credentials {
  username: string;
  password: string;
}

export interface Customer {
  id: string;
  fullName: string;
  customerType: CustomerType;
  cpf: string | null;
  cnpj: string | null;
  email: string | null;
  phone: string | null;
  birthDate: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CustomerPayload {
  fullName: string;
  customerType: CustomerType;
  cpf?: string | null;
  cnpj?: string | null;
  email?: string | null;
  phone?: string | null;
  birthDate?: string | null;
}

export interface Insurer {
  id: string;
  name: string;
  cnpj: string | null;
  active: boolean;
}

export interface InsurerPayload {
  name: string;
  cnpj?: string | null;
}

export interface Policy {
  id: string;
  policyNumber: string;
  type: PolicyType;
  status: PolicyStatus;
  startDate: string;
  endDate: string;
  monthlyPremium: number | null;
  notes: string | null;
  customerId: string;
  customerName: string;
  insurerId: string;
  insurerName: string;
  createdAt: string;
  updatedAt: string;
}

export interface PolicyPayload {
  policyNumber: string;
  type: PolicyType;
  status?: PolicyStatus | null;
  startDate: string;
  endDate: string;
  monthlyPremium?: number | null;
  notes?: string | null;
  customerId: string;
  insurerId: string;
}

export interface PolicyFilters {
  search: string;
  status: "" | PolicyStatus;
  type: "" | PolicyType;
  customerId: string;
  insurerId: string;
  endDateFrom: string;
  endDateTo: string;
}
