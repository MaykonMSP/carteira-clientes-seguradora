import {
  Activity,
  Building2,
  CalendarClock,
  CheckCircle2,
  ClipboardList,
  Edit3,
  Eye,
  FileText,
  Home,
  LogOut,
  Plus,
  RefreshCcw,
  Search,
  ShieldCheck,
  Trash2,
  Users,
  XCircle
} from "lucide-react";
import { FormEvent, useEffect, useMemo, useState } from "react";
import { api } from "./services/api";
import type {
  Credentials,
  Customer,
  CustomerPayload,
  CustomerType,
  Insurer,
  InsurerPayload,
  Policy,
  PolicyFilters,
  PolicyPayload,
  PolicyStatus,
  PolicyType
} from "./types";

type View = "dashboard" | "customers" | "insurers" | "policies";

const policyTypes: PolicyType[] = ["AUTO", "RESIDENCIAL", "EMPRESARIAL", "VIDA", "SAUDE", "VIAGEM", "OUTROS"];
const policyStatuses: PolicyStatus[] = ["VIGENTE", "VENCIDA", "CANCELADA"];

const emptyCustomer: CustomerPayload = {
  fullName: "",
  customerType: "PESSOA_FISICA",
  cpf: "",
  cnpj: "",
  email: "",
  phone: "",
  birthDate: ""
};

const emptyInsurer: InsurerPayload = {
  name: "",
  cnpj: ""
};

const emptyPolicy: PolicyPayload = {
  policyNumber: "",
  type: "AUTO",
  status: null,
  startDate: "",
  endDate: "",
  monthlyPremium: null,
  notes: "",
  customerId: "",
  insurerId: ""
};

function saveSession(credentials: Credentials) {
  localStorage.setItem("insurance.credentials", JSON.stringify(credentials));
}

function loadSession(): Credentials | null {
  const raw = localStorage.getItem("insurance.credentials");
  if (!raw) return null;
  try {
    return JSON.parse(raw) as Credentials;
  } catch {
    return null;
  }
}

function formatCurrency(value: number | null) {
  if (value === null || value === undefined) return "-";
  return new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(value);
}

function formatDate(value: string | null) {
  if (!value) return "-";
  return new Intl.DateTimeFormat("pt-BR", { timeZone: "UTC" }).format(new Date(`${value}T00:00:00Z`));
}

function daysUntil(date: string) {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const target = new Date(`${date}T00:00:00`);
  return Math.ceil((target.getTime() - today.getTime()) / 86400000);
}

function isExpiring(policy: Policy) {
  const days = daysUntil(policy.endDate);
  return policy.status === "VIGENTE" && days >= 0 && days <= 30;
}

function statusLabel(policy: Policy) {
  if (isExpiring(policy)) return "Proxima";
  if (policy.status === "VIGENTE") return "Vigente";
  if (policy.status === "VENCIDA") return "Vencida";
  return "Cancelada";
}

function statusClass(policy: Policy) {
  if (isExpiring(policy)) return "status expiring";
  return `status ${policy.status.toLowerCase()}`;
}

function compactDocument(value: string | null) {
  return value || "-";
}

function cleanPayload<T extends object>(payload: T): T {
  return Object.fromEntries(
    Object.entries(payload).map(([key, value]) => [key, value === "" ? null : value])
  ) as T;
}

export default function App() {
  const [credentials, setCredentials] = useState<Credentials | null>(() => loadSession());
  const [view, setView] = useState<View>("dashboard");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [insurers, setInsurers] = useState<Insurer[]>([]);
  const [policies, setPolicies] = useState<Policy[]>([]);
  const [expiringPolicies, setExpiringPolicies] = useState<Policy[]>([]);
  const [totals, setTotals] = useState({
    customers: 0,
    insurers: 0,
    policies: 0,
    active: 0,
    expired: 0
  });

  const [customerSearch, setCustomerSearch] = useState("");
  const [insurerSearch, setInsurerSearch] = useState("");
  const [policyFilters, setPolicyFilters] = useState<PolicyFilters>({
    search: "",
    status: "",
    type: "",
    customerId: "",
    insurerId: "",
    endDateFrom: "",
    endDateTo: ""
  });

  const [customerForm, setCustomerForm] = useState<CustomerPayload>(emptyCustomer);
  const [editingCustomer, setEditingCustomer] = useState<Customer | null>(null);
  const [showCustomerForm, setShowCustomerForm] = useState(false);

  const [insurerForm, setInsurerForm] = useState<InsurerPayload>(emptyInsurer);
  const [editingInsurer, setEditingInsurer] = useState<Insurer | null>(null);
  const [showInsurerForm, setShowInsurerForm] = useState(false);

  const [policyForm, setPolicyForm] = useState<PolicyPayload>(emptyPolicy);
  const [editingPolicy, setEditingPolicy] = useState<Policy | null>(null);
  const [showPolicyForm, setShowPolicyForm] = useState(false);
  const [selectedPolicy, setSelectedPolicy] = useState<Policy | null>(null);

  const canWrite = credentials?.username === "admin";

  async function loadData(nextCredentials = credentials) {
    if (!nextCredentials) return;
    setLoading(true);
    setMessage("");
    try {
      const [customerPage, insurerPage, policyPage, activePage, expiredPage, expiring] = await Promise.all([
        api.customers.list(nextCredentials, customerSearch),
        api.insurers.list(nextCredentials, insurerSearch),
        api.policies.list(nextCredentials, policyFilters),
        api.policies.byStatus(nextCredentials, "VIGENTE"),
        api.policies.byStatus(nextCredentials, "VENCIDA"),
        api.policies.expiring(nextCredentials, 30)
      ]);
      setCustomers(customerPage.content);
      setInsurers(insurerPage.content);
      setPolicies(policyPage.content);
      setExpiringPolicies(expiring);
      setTotals({
        customers: customerPage.totalElements,
        insurers: insurerPage.totalElements,
        policies: policyPage.totalElements,
        active: activePage.totalElements,
        expired: expiredPage.totalElements
      });
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Falha ao carregar dados.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (credentials) {
      void loadData(credentials);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [credentials]);

  async function submitLogin(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const nextCredentials = {
      username: String(form.get("username") || ""),
      password: String(form.get("password") || "")
    };
    setLoading(true);
    setMessage("");
    try {
      await api.login(nextCredentials);
      saveSession(nextCredentials);
      setCredentials(nextCredentials);
      setView("dashboard");
    } catch {
      setMessage("Usuario ou senha invalidos.");
    } finally {
      setLoading(false);
    }
  }

  function logout() {
    localStorage.removeItem("insurance.credentials");
    setCredentials(null);
    setMessage("");
  }

  function editCustomer(customer: Customer) {
    setEditingCustomer(customer);
    setCustomerForm({
      fullName: customer.fullName,
      customerType: customer.customerType,
      cpf: customer.cpf || "",
      cnpj: customer.cnpj || "",
      email: customer.email || "",
      phone: customer.phone || "",
      birthDate: customer.birthDate || ""
    });
    setShowCustomerForm(true);
  }

  async function submitCustomer(event: FormEvent) {
    event.preventDefault();
    if (!credentials) return;
    const payload = cleanPayload({
      ...customerForm,
      cpf: customerForm.customerType === "PESSOA_FISICA" ? customerForm.cpf : null,
      cnpj: customerForm.customerType === "PESSOA_JURIDICA" ? customerForm.cnpj : null,
      birthDate: customerForm.customerType === "PESSOA_FISICA" ? customerForm.birthDate : null
    });
    try {
      if (editingCustomer) {
        await api.customers.update(credentials, editingCustomer.id, payload);
        setMessage("Cliente atualizado.");
      } else {
        await api.customers.create(credentials, payload);
        setMessage("Cliente cadastrado.");
      }
      setShowCustomerForm(false);
      setEditingCustomer(null);
      setCustomerForm(emptyCustomer);
      await loadData();
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Falha ao salvar cliente.");
    }
  }

  async function removeCustomer(customer: Customer) {
    if (!credentials || !window.confirm(`Excluir ${customer.fullName}?`)) return;
    try {
      await api.customers.remove(credentials, customer.id);
      setMessage("Cliente excluido.");
      await loadData();
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Falha ao excluir cliente.");
    }
  }

  function editInsurer(insurer: Insurer) {
    setEditingInsurer(insurer);
    setInsurerForm({ name: insurer.name, cnpj: insurer.cnpj || "" });
    setShowInsurerForm(true);
  }

  async function submitInsurer(event: FormEvent) {
    event.preventDefault();
    if (!credentials) return;
    try {
      if (editingInsurer) {
        await api.insurers.update(credentials, editingInsurer.id, cleanPayload(insurerForm));
        setMessage("Seguradora atualizada.");
      } else {
        await api.insurers.create(credentials, cleanPayload(insurerForm));
        setMessage("Seguradora cadastrada.");
      }
      setShowInsurerForm(false);
      setEditingInsurer(null);
      setInsurerForm(emptyInsurer);
      await loadData();
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Falha ao salvar seguradora.");
    }
  }

  async function removeInsurer(insurer: Insurer) {
    if (!credentials || !window.confirm(`Desativar ${insurer.name}?`)) return;
    try {
      await api.insurers.remove(credentials, insurer.id);
      setMessage("Seguradora desativada.");
      await loadData();
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Falha ao excluir seguradora.");
    }
  }

  function editPolicy(policy: Policy) {
    setEditingPolicy(policy);
    setPolicyForm({
      policyNumber: policy.policyNumber,
      type: policy.type,
      status: policy.status,
      startDate: policy.startDate,
      endDate: policy.endDate,
      monthlyPremium: policy.monthlyPremium,
      notes: policy.notes || "",
      customerId: policy.customerId,
      insurerId: policy.insurerId
    });
    setShowPolicyForm(true);
    setSelectedPolicy(null);
  }

  async function submitPolicy(event: FormEvent) {
    event.preventDefault();
    if (!credentials) return;
    const payload = cleanPayload({
      ...policyForm,
      status: policyForm.status || null,
      monthlyPremium: policyForm.monthlyPremium ? Number(policyForm.monthlyPremium) : null
    });
    try {
      if (editingPolicy) {
        await api.policies.update(credentials, editingPolicy.id, payload);
        setMessage("Apolice atualizada.");
      } else {
        await api.policies.create(credentials, payload);
        setMessage("Apolice cadastrada.");
      }
      setShowPolicyForm(false);
      setEditingPolicy(null);
      setPolicyForm(emptyPolicy);
      await loadData();
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Falha ao salvar apolice.");
    }
  }

  async function removePolicy(policy: Policy) {
    if (!credentials || !window.confirm(`Excluir apolice ${policy.policyNumber}?`)) return;
    try {
      await api.policies.remove(credentials, policy.id);
      setMessage("Apolice excluida.");
      setSelectedPolicy(null);
      await loadData();
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Falha ao excluir apolice.");
    }
  }

  const dashboardCards = useMemo(
    () => [
      { label: "Clientes", value: totals.customers, icon: Users },
      { label: "Seguradoras", value: totals.insurers, icon: Building2 },
      { label: "Apolices", value: totals.policies, icon: FileText },
      { label: "Vigentes", value: totals.active, icon: CheckCircle2 },
      { label: "Vencidas", value: totals.expired, icon: XCircle },
      { label: "A vencer", value: expiringPolicies.length, icon: CalendarClock }
    ],
    [expiringPolicies.length, totals]
  );

  if (!credentials) {
    return (
      <main className="login-screen">
        <section className="login-panel">
          <div className="brand-mark">
            <ShieldCheck size={30} />
          </div>
          <h1>Carteira de Seguros</h1>
          <p>Acesse sua operacao de clientes, seguradoras e apolices.</p>
          <form onSubmit={submitLogin} className="stack-form">
            <label>
              Usuario
              <input name="username" defaultValue="admin" autoComplete="username" required />
            </label>
            <label>
              Senha
              <input name="password" defaultValue="admin123" type="password" autoComplete="current-password" required />
            </label>
            {message && <div className="form-message">{message}</div>}
            <button className="primary-button" disabled={loading}>
              <ShieldCheck size={18} />
              Entrar
            </button>
          </form>
        </section>
      </main>
    );
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <ShieldCheck size={28} />
          <span>Carteira</span>
        </div>
        <nav>
          <NavButton active={view === "dashboard"} icon={Home} label="Dashboard" onClick={() => setView("dashboard")} />
          <NavButton active={view === "customers"} icon={Users} label="Clientes" onClick={() => setView("customers")} />
          <NavButton active={view === "insurers"} icon={Building2} label="Seguradoras" onClick={() => setView("insurers")} />
          <NavButton active={view === "policies"} icon={ClipboardList} label="Apolices" onClick={() => setView("policies")} />
        </nav>
      </aside>

      <div className="main-area">
        <header className="topbar">
          <div>
            <strong>{titleFor(view)}</strong>
            <span>{canWrite ? "Perfil administrador" : "Perfil leitura"}</span>
          </div>
          <div className="topbar-actions">
            <button className="icon-button" title="Atualizar" onClick={() => void loadData()}>
              <RefreshCcw size={18} />
            </button>
            <button className="ghost-button" onClick={logout}>
              <LogOut size={18} />
              Sair
            </button>
          </div>
        </header>

        <main className="content">
          {message && <div className="notice">{message}</div>}
          {loading && <div className="loading-line" />}
          {view === "dashboard" && (
            <Dashboard cards={dashboardCards} policies={policies} expiringPolicies={expiringPolicies} onOpenPolicy={setSelectedPolicy} />
          )}
          {view === "customers" && (
            <CustomersView
              canWrite={canWrite}
              customers={customers}
              search={customerSearch}
              setSearch={setCustomerSearch}
              reload={() => loadData()}
              form={customerForm}
              setForm={setCustomerForm}
              showForm={showCustomerForm}
              setShowForm={setShowCustomerForm}
              editing={editingCustomer}
              setEditing={setEditingCustomer}
              onSubmit={submitCustomer}
              onEdit={editCustomer}
              onRemove={removeCustomer}
            />
          )}
          {view === "insurers" && (
            <InsurersView
              canWrite={canWrite}
              insurers={insurers}
              search={insurerSearch}
              setSearch={setInsurerSearch}
              reload={() => loadData()}
              form={insurerForm}
              setForm={setInsurerForm}
              showForm={showInsurerForm}
              setShowForm={setShowInsurerForm}
              editing={editingInsurer}
              setEditing={setEditingInsurer}
              onSubmit={submitInsurer}
              onEdit={editInsurer}
              onRemove={removeInsurer}
            />
          )}
          {view === "policies" && (
            <PoliciesView
              canWrite={canWrite}
              policies={policies}
              customers={customers}
              insurers={insurers}
              filters={policyFilters}
              setFilters={setPolicyFilters}
              reload={() => loadData()}
              form={policyForm}
              setForm={setPolicyForm}
              showForm={showPolicyForm}
              setShowForm={setShowPolicyForm}
              editing={editingPolicy}
              setEditing={setEditingPolicy}
              onSubmit={submitPolicy}
              onEdit={editPolicy}
              onRemove={removePolicy}
              onSelect={setSelectedPolicy}
            />
          )}
        </main>
      </div>

      {selectedPolicy && (
        <PolicyDetails
          policy={selectedPolicy}
          canWrite={canWrite}
          onClose={() => setSelectedPolicy(null)}
          onEdit={editPolicy}
          onRemove={removePolicy}
        />
      )}
    </div>
  );
}

function titleFor(view: View) {
  return {
    dashboard: "Dashboard",
    customers: "Clientes",
    insurers: "Seguradoras",
    policies: "Apolices"
  }[view];
}

function NavButton({
  active,
  icon: Icon,
  label,
  onClick
}: {
  active: boolean;
  icon: typeof Home;
  label: string;
  onClick: () => void;
}) {
  return (
    <button className={active ? "nav-button active" : "nav-button"} onClick={onClick}>
      <Icon size={18} />
      {label}
    </button>
  );
}

function Dashboard({
  cards,
  policies,
  expiringPolicies,
  onOpenPolicy
}: {
  cards: { label: string; value: number; icon: typeof Users }[];
  policies: Policy[];
  expiringPolicies: Policy[];
  onOpenPolicy: (policy: Policy) => void;
}) {
  const recent = policies.slice(0, 6);
  return (
    <section className="page-grid">
      <div className="metrics-grid">
        {cards.map(({ label, value, icon: Icon }) => (
          <article className="metric-card" key={label}>
            <div>
              <span>{label}</span>
              <strong>{value}</strong>
            </div>
            <Icon size={22} />
          </article>
        ))}
      </div>

      <section className="panel">
        <div className="panel-heading">
          <h2>Apolices proximas do vencimento</h2>
          <span>{expiringPolicies.length} em 30 dias</span>
        </div>
        <PolicyMiniList policies={expiringPolicies} empty="Nenhuma apolice proxima." onOpen={onOpenPolicy} />
      </section>

      <section className="panel">
        <div className="panel-heading">
          <h2>Carteira em foco</h2>
          <span>Ordenada por vencimento</span>
        </div>
        <PolicyMiniList policies={recent} empty="Sem apolices cadastradas." onOpen={onOpenPolicy} />
      </section>
    </section>
  );
}

function PolicyMiniList({ policies, empty, onOpen }: { policies: Policy[]; empty: string; onOpen: (policy: Policy) => void }) {
  if (!policies.length) return <div className="empty-state">{empty}</div>;
  return (
    <div className="mini-list">
      {policies.map((policy) => (
        <button key={policy.id} className="mini-row" onClick={() => onOpen(policy)}>
          <span>
            <strong>{policy.policyNumber}</strong>
            <small>{policy.customerName}</small>
          </span>
          <span className={statusClass(policy)}>{statusLabel(policy)}</span>
        </button>
      ))}
    </div>
  );
}

function SectionHeader({ title, action }: { title: string; action?: React.ReactNode }) {
  return (
    <div className="section-header">
      <h1>{title}</h1>
      {action}
    </div>
  );
}

function SearchBox({ value, onChange, onSearch }: { value: string; onChange: (value: string) => void; onSearch: () => void }) {
  return (
    <div className="search-box">
      <Search size={18} />
      <input value={value} onChange={(event) => onChange(event.target.value)} placeholder="Buscar" />
      <button onClick={onSearch}>Filtrar</button>
    </div>
  );
}

function CustomersView(props: {
  canWrite: boolean;
  customers: Customer[];
  search: string;
  setSearch: (value: string) => void;
  reload: () => Promise<void>;
  form: CustomerPayload;
  setForm: (form: CustomerPayload) => void;
  showForm: boolean;
  setShowForm: (value: boolean) => void;
  editing: Customer | null;
  setEditing: (customer: Customer | null) => void;
  onSubmit: (event: FormEvent) => void;
  onEdit: (customer: Customer) => void;
  onRemove: (customer: Customer) => void;
}) {
  return (
    <section>
      <SectionHeader
        title="Clientes"
        action={
          props.canWrite && (
            <button
              className="primary-button"
              onClick={() => {
                props.setEditing(null);
                props.setForm(emptyCustomer);
                props.setShowForm(true);
              }}
            >
              <Plus size={18} />
              Novo cliente
            </button>
          )
        }
      />
      <SearchBox value={props.search} onChange={props.setSearch} onSearch={props.reload} />
      {props.showForm && (
        <CustomerForm
          form={props.form}
          setForm={props.setForm}
          editing={props.editing}
          onSubmit={props.onSubmit}
          onCancel={() => {
            props.setShowForm(false);
            props.setEditing(null);
          }}
        />
      )}
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Nome</th>
              <th>Tipo</th>
              <th>Documento</th>
              <th>Email</th>
              <th>Telefone</th>
              <th>Acoes</th>
            </tr>
          </thead>
          <tbody>
            {props.customers.map((customer) => (
              <tr key={customer.id}>
                <td>{customer.fullName}</td>
                <td>{customer.customerType === "PESSOA_FISICA" ? "Pessoa fisica" : "Pessoa juridica"}</td>
                <td>{compactDocument(customer.cpf || customer.cnpj)}</td>
                <td>{customer.email || "-"}</td>
                <td>{customer.phone || "-"}</td>
                <td>
                  <RowActions canWrite={props.canWrite} onEdit={() => props.onEdit(customer)} onRemove={() => props.onRemove(customer)} />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}

function CustomerForm({
  form,
  setForm,
  editing,
  onSubmit,
  onCancel
}: {
  form: CustomerPayload;
  setForm: (form: CustomerPayload) => void;
  editing: Customer | null;
  onSubmit: (event: FormEvent) => void;
  onCancel: () => void;
}) {
  return (
    <form className="form-panel" onSubmit={onSubmit}>
      <h2>{editing ? "Editar cliente" : "Novo cliente"}</h2>
      <div className="form-grid">
        <label>
          Nome
          <input value={form.fullName} onChange={(event) => setForm({ ...form, fullName: event.target.value })} required />
        </label>
        <label>
          Tipo
          <select value={form.customerType} onChange={(event) => setForm({ ...form, customerType: event.target.value as CustomerType })}>
            <option value="PESSOA_FISICA">Pessoa fisica</option>
            <option value="PESSOA_JURIDICA">Pessoa juridica</option>
          </select>
        </label>
        {form.customerType === "PESSOA_FISICA" ? (
          <>
            <label>
              CPF
              <input value={form.cpf || ""} onChange={(event) => setForm({ ...form, cpf: event.target.value })} required />
            </label>
            <label>
              Nascimento
              <input type="date" value={form.birthDate || ""} onChange={(event) => setForm({ ...form, birthDate: event.target.value })} />
            </label>
          </>
        ) : (
          <label>
            CNPJ
            <input value={form.cnpj || ""} onChange={(event) => setForm({ ...form, cnpj: event.target.value })} required />
          </label>
        )}
        <label>
          Email
          <input type="email" value={form.email || ""} onChange={(event) => setForm({ ...form, email: event.target.value })} />
        </label>
        <label>
          Telefone
          <input value={form.phone || ""} onChange={(event) => setForm({ ...form, phone: event.target.value })} />
        </label>
      </div>
      <FormActions onCancel={onCancel} />
    </form>
  );
}

function InsurersView(props: {
  canWrite: boolean;
  insurers: Insurer[];
  search: string;
  setSearch: (value: string) => void;
  reload: () => Promise<void>;
  form: InsurerPayload;
  setForm: (form: InsurerPayload) => void;
  showForm: boolean;
  setShowForm: (value: boolean) => void;
  editing: Insurer | null;
  setEditing: (insurer: Insurer | null) => void;
  onSubmit: (event: FormEvent) => void;
  onEdit: (insurer: Insurer) => void;
  onRemove: (insurer: Insurer) => void;
}) {
  return (
    <section>
      <SectionHeader
        title="Seguradoras"
        action={
          props.canWrite && (
            <button
              className="primary-button"
              onClick={() => {
                props.setEditing(null);
                props.setForm(emptyInsurer);
                props.setShowForm(true);
              }}
            >
              <Plus size={18} />
              Nova seguradora
            </button>
          )
        }
      />
      <SearchBox value={props.search} onChange={props.setSearch} onSearch={props.reload} />
      {props.showForm && (
        <form className="form-panel" onSubmit={props.onSubmit}>
          <h2>{props.editing ? "Editar seguradora" : "Nova seguradora"}</h2>
          <div className="form-grid">
            <label>
              Nome
              <input value={props.form.name} onChange={(event) => props.setForm({ ...props.form, name: event.target.value })} required />
            </label>
            <label>
              CNPJ
              <input value={props.form.cnpj || ""} onChange={(event) => props.setForm({ ...props.form, cnpj: event.target.value })} />
            </label>
          </div>
          <FormActions
            onCancel={() => {
              props.setShowForm(false);
              props.setEditing(null);
            }}
          />
        </form>
      )}
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Nome</th>
              <th>CNPJ</th>
              <th>Status</th>
              <th>Acoes</th>
            </tr>
          </thead>
          <tbody>
            {props.insurers.map((insurer) => (
              <tr key={insurer.id}>
                <td>{insurer.name}</td>
                <td>{compactDocument(insurer.cnpj)}</td>
                <td>
                  <span className={insurer.active ? "status vigente" : "status cancelada"}>{insurer.active ? "Ativa" : "Inativa"}</span>
                </td>
                <td>
                  <RowActions canWrite={props.canWrite} onEdit={() => props.onEdit(insurer)} onRemove={() => props.onRemove(insurer)} />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}

function PoliciesView(props: {
  canWrite: boolean;
  policies: Policy[];
  customers: Customer[];
  insurers: Insurer[];
  filters: PolicyFilters;
  setFilters: (filters: PolicyFilters) => void;
  reload: () => Promise<void>;
  form: PolicyPayload;
  setForm: (form: PolicyPayload) => void;
  showForm: boolean;
  setShowForm: (value: boolean) => void;
  editing: Policy | null;
  setEditing: (policy: Policy | null) => void;
  onSubmit: (event: FormEvent) => void;
  onEdit: (policy: Policy) => void;
  onRemove: (policy: Policy) => void;
  onSelect: (policy: Policy) => void;
}) {
  return (
    <section>
      <SectionHeader
        title="Apolices"
        action={
          props.canWrite && (
            <button
              className="primary-button"
              onClick={() => {
                props.setEditing(null);
                props.setForm(emptyPolicy);
                props.setShowForm(true);
              }}
            >
              <Plus size={18} />
              Nova apolice
            </button>
          )
        }
      />
      <PolicyFilterBar filters={props.filters} setFilters={props.setFilters} customers={props.customers} insurers={props.insurers} reload={props.reload} />
      {props.showForm && (
        <PolicyForm
          form={props.form}
          setForm={props.setForm}
          editing={props.editing}
          customers={props.customers}
          insurers={props.insurers}
          onSubmit={props.onSubmit}
          onCancel={() => {
            props.setShowForm(false);
            props.setEditing(null);
          }}
        />
      )}
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Numero</th>
              <th>Cliente</th>
              <th>Seguradora</th>
              <th>Tipo</th>
              <th>Vencimento</th>
              <th>Status</th>
              <th>Acoes</th>
            </tr>
          </thead>
          <tbody>
            {props.policies.map((policy) => (
              <tr key={policy.id}>
                <td>{policy.policyNumber}</td>
                <td>{policy.customerName}</td>
                <td>{policy.insurerName}</td>
                <td>{policy.type}</td>
                <td>{formatDate(policy.endDate)}</td>
                <td>
                  <span className={statusClass(policy)}>{statusLabel(policy)}</span>
                </td>
                <td>
                  <div className="row-actions">
                    <button className="icon-button" title="Detalhes" onClick={() => props.onSelect(policy)}>
                      <Eye size={17} />
                    </button>
                    <RowActions canWrite={props.canWrite} onEdit={() => props.onEdit(policy)} onRemove={() => props.onRemove(policy)} />
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}

function PolicyFilterBar({
  filters,
  setFilters,
  customers,
  insurers,
  reload
}: {
  filters: PolicyFilters;
  setFilters: (filters: PolicyFilters) => void;
  customers: Customer[];
  insurers: Insurer[];
  reload: () => Promise<void>;
}) {
  return (
    <div className="filter-bar">
      <label>
        Buscar
        <input value={filters.search} onChange={(event) => setFilters({ ...filters, search: event.target.value })} placeholder="Numero ou observacao" />
      </label>
      <label>
        Status
        <select value={filters.status} onChange={(event) => setFilters({ ...filters, status: event.target.value as PolicyFilters["status"] })}>
          <option value="">Todos</option>
          {policyStatuses.map((status) => (
            <option key={status} value={status}>
              {status}
            </option>
          ))}
        </select>
      </label>
      <label>
        Tipo
        <select value={filters.type} onChange={(event) => setFilters({ ...filters, type: event.target.value as PolicyFilters["type"] })}>
          <option value="">Todos</option>
          {policyTypes.map((type) => (
            <option key={type} value={type}>
              {type}
            </option>
          ))}
        </select>
      </label>
      <label>
        Cliente
        <select value={filters.customerId} onChange={(event) => setFilters({ ...filters, customerId: event.target.value })}>
          <option value="">Todos</option>
          {customers.map((customer) => (
            <option key={customer.id} value={customer.id}>
              {customer.fullName}
            </option>
          ))}
        </select>
      </label>
      <label>
        Seguradora
        <select value={filters.insurerId} onChange={(event) => setFilters({ ...filters, insurerId: event.target.value })}>
          <option value="">Todas</option>
          {insurers.map((insurer) => (
            <option key={insurer.id} value={insurer.id}>
              {insurer.name}
            </option>
          ))}
        </select>
      </label>
      <label>
        Vence de
        <input type="date" value={filters.endDateFrom} onChange={(event) => setFilters({ ...filters, endDateFrom: event.target.value })} />
      </label>
      <label>
        Ate
        <input type="date" value={filters.endDateTo} onChange={(event) => setFilters({ ...filters, endDateTo: event.target.value })} />
      </label>
      <button className="primary-button" onClick={reload}>
        <Search size={17} />
        Filtrar
      </button>
    </div>
  );
}

function PolicyForm({
  form,
  setForm,
  editing,
  customers,
  insurers,
  onSubmit,
  onCancel
}: {
  form: PolicyPayload;
  setForm: (form: PolicyPayload) => void;
  editing: Policy | null;
  customers: Customer[];
  insurers: Insurer[];
  onSubmit: (event: FormEvent) => void;
  onCancel: () => void;
}) {
  return (
    <form className="form-panel" onSubmit={onSubmit}>
      <h2>{editing ? "Editar apolice" : "Nova apolice"}</h2>
      <div className="form-grid">
        <label>
          Numero
          <input value={form.policyNumber} onChange={(event) => setForm({ ...form, policyNumber: event.target.value })} required />
        </label>
        <label>
          Tipo
          <select value={form.type} onChange={(event) => setForm({ ...form, type: event.target.value as PolicyType })}>
            {policyTypes.map((type) => (
              <option key={type} value={type}>
                {type}
              </option>
            ))}
          </select>
        </label>
        <label>
          Status
          <select value={form.status || ""} onChange={(event) => setForm({ ...form, status: (event.target.value || null) as PolicyStatus | null })}>
            <option value="">Automatico</option>
            {policyStatuses.map((status) => (
              <option key={status} value={status}>
                {status}
              </option>
            ))}
          </select>
        </label>
        <label>
          Inicio
          <input type="date" value={form.startDate} onChange={(event) => setForm({ ...form, startDate: event.target.value })} required />
        </label>
        <label>
          Vencimento
          <input type="date" value={form.endDate} onChange={(event) => setForm({ ...form, endDate: event.target.value })} required />
        </label>
        <label>
          Premio mensal
          <input
            type="number"
            min="0"
            step="0.01"
            value={form.monthlyPremium ?? ""}
            onChange={(event) => setForm({ ...form, monthlyPremium: event.target.value ? Number(event.target.value) : null })}
          />
        </label>
        <label>
          Cliente
          <select value={form.customerId} onChange={(event) => setForm({ ...form, customerId: event.target.value })} required>
            <option value="">Selecione</option>
            {customers.map((customer) => (
              <option key={customer.id} value={customer.id}>
                {customer.fullName}
              </option>
            ))}
          </select>
        </label>
        <label>
          Seguradora
          <select value={form.insurerId} onChange={(event) => setForm({ ...form, insurerId: event.target.value })} required>
            <option value="">Selecione</option>
            {insurers
              .filter((insurer) => insurer.active)
              .map((insurer) => (
                <option key={insurer.id} value={insurer.id}>
                  {insurer.name}
                </option>
              ))}
          </select>
        </label>
        <label className="wide-field">
          Observacoes
          <textarea value={form.notes || ""} onChange={(event) => setForm({ ...form, notes: event.target.value })} rows={3} />
        </label>
      </div>
      <FormActions onCancel={onCancel} />
    </form>
  );
}

function PolicyDetails({
  policy,
  canWrite,
  onClose,
  onEdit,
  onRemove
}: {
  policy: Policy;
  canWrite: boolean;
  onClose: () => void;
  onEdit: (policy: Policy) => void;
  onRemove: (policy: Policy) => void;
}) {
  return (
    <aside className="details-drawer">
      <div className="drawer-header">
        <div>
          <span className={statusClass(policy)}>{statusLabel(policy)}</span>
          <h2>{policy.policyNumber}</h2>
        </div>
        <button className="icon-button" onClick={onClose} title="Fechar">
          <XCircle size={20} />
        </button>
      </div>
      <dl className="detail-list">
        <div>
          <dt>Cliente</dt>
          <dd>{policy.customerName}</dd>
        </div>
        <div>
          <dt>Seguradora</dt>
          <dd>{policy.insurerName}</dd>
        </div>
        <div>
          <dt>Tipo</dt>
          <dd>{policy.type}</dd>
        </div>
        <div>
          <dt>Inicio</dt>
          <dd>{formatDate(policy.startDate)}</dd>
        </div>
        <div>
          <dt>Vencimento</dt>
          <dd>{formatDate(policy.endDate)}</dd>
        </div>
        <div>
          <dt>Premio mensal</dt>
          <dd>{formatCurrency(policy.monthlyPremium)}</dd>
        </div>
        <div className="wide-field">
          <dt>Observacoes</dt>
          <dd>{policy.notes || "-"}</dd>
        </div>
      </dl>
      {canWrite && (
        <div className="drawer-actions">
          <button className="secondary-button" onClick={() => onEdit(policy)}>
            <Edit3 size={17} />
            Editar
          </button>
          <button className="danger-button" onClick={() => onRemove(policy)}>
            <Trash2 size={17} />
            Excluir
          </button>
        </div>
      )}
    </aside>
  );
}

function RowActions({ canWrite, onEdit, onRemove }: { canWrite: boolean; onEdit: () => void; onRemove: () => void }) {
  if (!canWrite) return <span className="muted">Somente leitura</span>;
  return (
    <div className="row-actions">
      <button className="icon-button" title="Editar" onClick={onEdit}>
        <Edit3 size={17} />
      </button>
      <button className="icon-button danger-icon" title="Excluir" onClick={onRemove}>
        <Trash2 size={17} />
      </button>
    </div>
  );
}

function FormActions({ onCancel }: { onCancel: () => void }) {
  return (
    <div className="form-actions">
      <button type="button" className="secondary-button" onClick={onCancel}>
        Cancelar
      </button>
      <button className="primary-button">
        <Activity size={17} />
        Salvar
      </button>
    </div>
  );
}
