export type Role = "ADMIN" | "RECEPTIONIST" | "MECHANIC" | "MANAGER"

export interface User {
  id: string
  name: string
  email: string
  role: Role
}

export interface LoginCredentials {
  email: string
  password: string
}

export interface LoginResponse {
  token: string
  refreshToken: string
  userId: string
  name: string
  email: string
  role: Role
}

/* ---------- Clientes ---------- */
export interface Client {
  id: string
  name: string
  cpf: string | null
  phone: string | null
  email: string | null
  address: string | null
  createdAt: string
  isActive: boolean
  vehicleCount: number
}

// null limpa o campo no servidor; omitir o campo o mantém como está.
export interface ClientRequest {
  name: string
  cpf: string | null
  phone: string | null
  email: string | null
  address: string | null
}

/* ---------- Veículos ---------- */
export interface Vehicle {
  id: string
  plate: string
  brand: string
  model: string
  year: number
  mileage: number | null
  fuelType: string | null
  clientId: string
  clientName: string
  createdAt: string
  isActive: boolean
}

export interface VehicleRequest {
  plate: string
  brand: string
  model: string
  year: number
  mileage: number | null
  fuelType: string | null
  clientId: string
}

/* ---------- Mecânicos ---------- */
export interface Mechanic {
  id: string
  name: string
  specialty: string | null
  phone: string | null
  isAvailable: boolean
  createdAt: string
  isActive: boolean
}

export interface MechanicRequest {
  name: string
  specialty: string | null
  phone: string | null
}

/* ---------- Estoque ---------- */
export interface Part {
  id: string
  name: string
  code: string
  manufacturer: string | null
  purchasePrice: number
  salePrice: number
  stockQuantity: number
  minimumStock: number
  isLowStock: boolean
  createdAt: string
  isActive: boolean
}

export interface PartRequest {
  name: string
  code: string
  manufacturer: string | null
  purchasePrice: number
  salePrice: number
  stockQuantity: number
  minimumStock: number
}

/* ---------- Ordens de serviço ---------- */
export type ServiceOrderStatus =
  | "CRIADA"
  | "EM_DIAGNOSTICO"
  | "AGUARDANDO_APROVACAO"
  | "APROVADA"
  | "EM_EXECUCAO"
  | "FINALIZADA"
  | "CANCELADA"

export interface ServiceOrderItem {
  id: string
  partId: string
  partName: string
  partCode: string
  quantity: number
  unitPrice: number
  subtotal: number
  createdAt: string
}

export interface ServiceOrder {
  id: string
  orderNumber: string
  clientId: string
  clientName: string
  vehicleId: string
  vehicleInfo: string
  mechanicId: string | null
  mechanicName: string | null
  reportedProblem: string | null
  diagnosis: string | null
  status: ServiceOrderStatus
  laborAmount: number
  partsAmount: number
  totalAmount: number
  items: ServiceOrderItem[]
  startedAt: string | null
  completedAt: string | null
  createdAt: string
}

export interface ServiceOrderRequest {
  clientId: string
  vehicleId: string
  mechanicId: string | null
  reportedProblem: string | null
}

export interface ServiceOrderItemRequest {
  partId: string
  quantity: number
}

export interface StatusUpdateRequest {
  status: ServiceOrderStatus
  diagnosis?: string | null
}

/* ---------- Orçamentos ---------- */
export type BudgetStatus = "PENDENTE" | "APROVADO" | "RECUSADO" | "EXPIRADO"

export interface Budget {
  id: string
  budgetNumber: string
  clientId: string
  clientName: string
  vehicleId: string
  vehicleInfo: string
  serviceOrderId: string | null
  description: string | null
  totalAmount: number
  status: BudgetStatus
  validUntil: string | null
  createdAt: string
}

export interface BudgetRequest {
  clientId: string
  vehicleId: string
  serviceOrderId?: string | null
  description: string | null
  totalAmount: number
  validUntil: string | null
}

/* ---------- Dashboard ---------- */
export interface DashboardStats {
  totalClients: number
  totalVehicles: number
  totalServiceOrders: number
  openServiceOrders: number
  inProgressServiceOrders: number
  waitingApprovalServiceOrders: number
  monthlyRevenue: number
  lowStockItems: number
  monthlyRevenueChart: Array<{ month: string; amount: number }>
  topMechanics: Array<{ mechanicId: string; mechanicName: string; completedOrders: number }>
  mostUsedParts: Array<{ partId: string; partName: string; usageCount: number }>
}
