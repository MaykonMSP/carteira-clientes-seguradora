import type {
  Credentials,
  Customer,
  CustomerPayload,
  Insurer,
  InsurerPayload,
  Page,
  Policy,
  PolicyFilters,
  PolicyPayload,
  PolicyStatus,
  PolicyType
} from "../types";

const API_BASE = (import.meta.env.VITE_API_BASE_URL || "/api").replace(/\/$/, "");

function authHeader(credentials: Credentials) {
  return `Basic ${btoa(`${credentials.username}:${credentials.password}`)}`;
}

function withQuery(path: string, params: Record<string, string | number | undefined | null>) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      query.set(key, String(value));
    }
  });
  const suffix = query.toString();
  return suffix ? `${path}?${suffix}` : path;
}

async function parseError(response: Response) {
  if (response.status === 401) {
    return "Credenciais invalidas ou sessao expirada.";
  }
  if (response.status === 403) {
    return "Seu perfil nao tem permissao para executar esta acao.";
  }

  const fallback = `Erro ${response.status}`;
  try {
    const body = await response.json();
    const message = body.message || body.error || fallback;
    if (Array.isArray(body.details) && body.details.length) {
      return `${message}: ${body.details.join("; ")}`;
    }
    return message;
  } catch {
    return fallback;
  }
}

export async function request<T>(
  credentials: Credentials,
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const headers = new Headers(options.headers);
  headers.set("Authorization", authHeader(credentials));
  if (options.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  let response: Response;
  try {
    response = await fetch(`${API_BASE}${path}`, {
      ...options,
      headers
    });
  } catch {
    throw new Error("Nao foi possivel conectar a API. Verifique se o back-end esta rodando.");
  }

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const text = await response.text();
  return text ? (JSON.parse(text) as T) : (undefined as T);
}

export const api = {
  login: (credentials: Credentials) =>
    request<Page<Customer>>(credentials, "/customers?size=1"),

  customers: {
    list: (credentials: Credentials, search = "") =>
      request<Page<Customer>>(credentials, withQuery("/customers", { search, size: 200, sort: "fullName,asc" })),
    create: (credentials: Credentials, payload: CustomerPayload) =>
      request<Customer>(credentials, "/customers", { method: "POST", body: JSON.stringify(payload) }),
    update: (credentials: Credentials, id: string, payload: CustomerPayload) =>
      request<Customer>(credentials, `/customers/${id}`, { method: "PUT", body: JSON.stringify(payload) }),
    remove: (credentials: Credentials, id: string) =>
      request<void>(credentials, `/customers/${id}`, { method: "DELETE" })
  },

  insurers: {
    list: (credentials: Credentials, name = "") =>
      request<Page<Insurer>>(credentials, withQuery("/insurers", { name, size: 200, sort: "name,asc" })),
    create: (credentials: Credentials, payload: InsurerPayload) =>
      request<Insurer>(credentials, "/insurers", { method: "POST", body: JSON.stringify(payload) }),
    update: (credentials: Credentials, id: string, payload: InsurerPayload) =>
      request<Insurer>(credentials, `/insurers/${id}`, { method: "PUT", body: JSON.stringify(payload) }),
    remove: (credentials: Credentials, id: string) =>
      request<void>(credentials, `/insurers/${id}`, { method: "DELETE" })
  },

  policies: {
    list: (credentials: Credentials, filters: Partial<PolicyFilters> = {}) =>
      request<Page<Policy>>(
        credentials,
        withQuery("/policies", {
          search: filters.search,
          status: filters.status,
          type: filters.type,
          customerId: filters.customerId,
          insurerId: filters.insurerId,
          endDateFrom: filters.endDateFrom,
          endDateTo: filters.endDateTo,
          size: 250,
          sort: "endDate,asc"
        })
      ),
    byStatus: (credentials: Credentials, status: PolicyStatus) =>
      request<Page<Policy>>(credentials, withQuery("/policies", { status, size: 1 })),
    byType: (credentials: Credentials, type: PolicyType) =>
      request<Page<Policy>>(credentials, withQuery("/policies", { type, size: 1 })),
    expiring: (credentials: Credentials, days = 30) =>
      request<Policy[]>(credentials, withQuery("/policies/expiring", { days })),
    get: (credentials: Credentials, id: string) => request<Policy>(credentials, `/policies/${id}`),
    create: (credentials: Credentials, payload: PolicyPayload) =>
      request<Policy>(credentials, "/policies", { method: "POST", body: JSON.stringify(payload) }),
    update: (credentials: Credentials, id: string, payload: PolicyPayload) =>
      request<Policy>(credentials, `/policies/${id}`, { method: "PUT", body: JSON.stringify(payload) }),
    remove: (credentials: Credentials, id: string) =>
      request<void>(credentials, `/policies/${id}`, { method: "DELETE" })
  }
};
