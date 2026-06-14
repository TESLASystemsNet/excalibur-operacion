import axios from 'axios'
import type {
  CashierConsole,
  CustomerRegistration,
  CashierMovement,
  CashierSession,
  CashierShift,
  EgmMeterSnapshot,
  LoginResponse,
  OperationSchedule,
  OperationalAssignment,
  OperationalAuditEvent,
  OperationalCard,
  OperationalCloseSummary,
  OperationalDay,
  Role,
  TreasuryCardMovement,
  TreasuryConsole,
  TreasuryLedgerEntry,
  TreasuryMovement,
  TreasurySession,
  UserDetail,
  UserForm,
  UserSummary,
  Workstation
} from '../types/security'

const API_BASE_URL = import.meta.env.VITE_EXCALIBUR_API_URL ?? 'http://192.168.1.94:8083'
const TOKEN_KEY = 'excalibur.accessToken'

export const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export function saveToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function hasToken() {
  return Boolean(localStorage.getItem(TOKEN_KEY))
}

export async function login(username: string, password: string) {
  const { data } = await api.post<LoginResponse>('/api/auth/login', { username, password })
  saveToken(data.accessToken)
  return data
}

export async function logout() {
  try {
    await api.post('/api/auth/logout')
  } finally {
    clearToken()
  }
}

export async function me() {
  const { data } = await api.get<LoginResponse>('/api/auth/me')
  return data
}

export async function changeOwnPassword(currentPassword: string, newPassword: string) {
  await api.patch('/api/auth/me/password', { currentPassword, newPassword })
}

export async function getUsers(search = '') {
  const { data } = await api.get<UserSummary[]>('/api/users', {
    params: search ? { search } : undefined
  })
  return data
}

export async function getUser(id: number) {
  const { data } = await api.get<UserDetail>(`/api/users/${id}`)
  return data
}

export async function createUser(form: UserForm) {
  const { data } = await api.post<UserDetail>('/api/users', normalizeCreatePayload(form))
  return data
}

export async function updateUser(id: number, form: UserForm) {
  const { data } = await api.put<UserDetail>(`/api/users/${id}`, normalizeUpdatePayload(form))
  return data
}

export async function updateUserPassword(id: number, password: string) {
  await api.patch(`/api/users/${id}/password`, { password })
}

export async function deleteUser(id: number) {
  await api.delete(`/api/users/${id}`)
}

export async function getRoles() {
  const { data } = await api.get<Role[]>('/api/roles')
  return data
}

export async function getOperationSchedules() {
  const { data } = await api.get<OperationSchedule[]>('/api/operational-config/operation-schedules')
  return data
}

export async function createOperationSchedule(form: Omit<OperationSchedule, 'id'>) {
  const { data } = await api.post<OperationSchedule>('/api/operational-config/operation-schedules', form)
  return data
}

export async function updateOperationSchedule(id: number, form: Omit<OperationSchedule, 'id'>) {
  await api.put(`/api/operational-config/operation-schedules/${id}`, form)
}

export async function deleteOperationSchedule(id: number) {
  await api.delete(`/api/operational-config/operation-schedules/${id}`)
}

export async function getCashierShifts() {
  const { data } = await api.get<CashierShift[]>('/api/operational-config/cashier-shifts')
  return data
}

export async function createCashierShift(form: Omit<CashierShift, 'id'>) {
  const { data } = await api.post<CashierShift>('/api/operational-config/cashier-shifts', form)
  return data
}

export async function updateCashierShift(id: number, form: Omit<CashierShift, 'id'>) {
  await api.put(`/api/operational-config/cashier-shifts/${id}`, form)
}

export async function deleteCashierShift(id: number) {
  await api.delete(`/api/operational-config/cashier-shifts/${id}`)
}

export async function getWorkstations() {
  const { data } = await api.get<Workstation[]>('/api/operational-config/workstations')
  return data
}

type WorkstationPayload = Omit<Workstation, 'id' | 'cajaNombre'>

export async function createWorkstation(form: WorkstationPayload) {
  const { data } = await api.post<Workstation>('/api/operational-config/workstations', form)
  return data
}

export async function updateWorkstation(id: number, form: WorkstationPayload) {
  await api.put(`/api/operational-config/workstations/${id}`, form)
}

export async function deleteWorkstation(id: number) {
  await api.delete(`/api/operational-config/workstations/${id}`)
}

export async function getOperationalAssignments() {
  const { data } = await api.get<OperationalAssignment[]>('/api/operational-config/assignments')
  return data
}

export async function getCurrentOperationalDay() {
  const response = await api.get<OperationalDay | ''>('/api/operational-config/operational-day/current', {
    validateStatus: (status) => status === 200 || status === 204
  })
  return response.status === 204 ? null : (response.data as OperationalDay)
}

export async function openOperationalDay(form: { fechaJornada: string; observaciones?: string | null }) {
  const { data } = await api.post<OperationalDay>('/api/operational-config/operational-day/open', form)
  return data
}

export async function getOperationalClose() {
  const response = await api.get<OperationalCloseSummary | ''>('/api/operational-config/operational-day/close', {
    validateStatus: (status) => status === 200 || status === 204
  })
  return response.status === 204 ? null : (response.data as OperationalCloseSummary)
}

export async function getCurrentEgmSnapshots() {
  const { data } = await api.get<EgmMeterSnapshot[]>('/api/operational-config/operational-day/current/egm-snapshots')
  return data
}

export async function closeOperationalDay(form: { observaciones?: string | null; forzarConDiferencias?: boolean }) {
  const { data } = await api.post<OperationalCloseSummary>('/api/operational-config/operational-day/close', form)
  return data
}

export async function testEgmSweep() {
  const { data } = await api.post<EgmMeterSnapshot[]>('/api/operational-config/operational-day/egm-sweep/test')
  return data
}

export async function getTreasuryConsole() {
  const { data } = await api.get<TreasuryConsole>('/api/operational-config/treasury/console')
  return data
}

export async function getTreasuryMovements() {
  const { data } = await api.get<TreasuryMovement[]>('/api/operational-config/treasury/movements')
  return data
}

export async function getTreasuryLedger() {
  const { data } = await api.get<TreasuryLedgerEntry[]>('/api/operational-config/treasury/ledger')
  return data
}

export async function openTreasury(form: { estacionId: number; saldoInicial: number; observaciones?: string | null }) {
  const { data } = await api.post<TreasurySession>('/api/operational-config/treasury/open', form)
  return data
}

export async function createTreasuryMovement(form: {
  tipo: 'ENTRADA' | 'SALIDA'
  concepto: string
  monto: number
  estacionCajaId?: number | null
  turnoId?: number | null
  referencia?: string | null
  observaciones?: string | null
}) {
  const { data } = await api.post<TreasuryMovement>('/api/operational-config/treasury/movements', form)
  return data
}

export async function sendTreasuryBoxFund(form: {
  concepto: string
  monto: number
  estacionCajaId: number
  turnoId?: number | null
  referencia?: string | null
  observaciones?: string | null
}) {
  const { data } = await api.post<TreasuryMovement>('/api/operational-config/treasury/box-fund', form)
  return data
}

export async function receiveTreasuryCashReturn(form: {
  concepto: string
  monto: number
  estacionCajaId: number
  turnoId?: number | null
  referencia?: string | null
  observaciones?: string | null
}) {
  const { data } = await api.post<TreasuryMovement>('/api/operational-config/treasury/cash-return', form)
  return data
}

export async function deliverTreasuryCardRange(form: {
  estacionCajaId: number
  turnoId?: number | null
  numeroInicial: string
  numeroFinal: string
  referencia?: string | null
  observaciones?: string | null
}) {
  const { data } = await api.post<TreasuryCardMovement>('/api/operational-config/treasury/card-delivery', form)
  return data
}

export async function receiveTreasuryCardReturn(form: {
  estacionCajaId: number
  turnoId?: number | null
  numeroInicial: string
  numeroFinal: string
  referencia?: string | null
  observaciones?: string | null
}) {
  const { data } = await api.post<TreasuryCardMovement>('/api/operational-config/treasury/card-return', form)
  return data
}

export async function precloseTreasury(observaciones?: string | null) {
  const { data } = await api.post<TreasurySession>('/api/operational-config/treasury/preclose', { observaciones })
  return data
}

export async function closeTreasury(observaciones?: string | null) {
  const { data } = await api.post<TreasurySession>('/api/operational-config/treasury/close', { observaciones })
  return data
}

export async function getCashierConsole() {
  const { data } = await api.get<CashierConsole>('/api/operational-config/cashier/console')
  return data
}

export async function openCashier(form: {
  estacionId: number
  turnoId: number
  montoApertura: number
  tarjetasApertura: number
  observaciones?: string | null
}) {
  const { data } = await api.post<CashierSession>('/api/operational-config/cashier/open', form)
  return data
}

export async function createCashierSale(form: {
  numeroTarjeta: string
  monto: number
  referencia?: string | null
  observaciones?: string | null
}) {
  const { data } = await api.post<CashierMovement>('/api/operational-config/cashier/sale', form)
  return data
}

export async function createCashierPayout(form: {
  numeroTarjeta: string
  monto: number
  referencia?: string | null
  observaciones?: string | null
}) {
  const { data } = await api.post<CashierMovement>('/api/operational-config/cashier/payout', form)
  return data
}

export async function precloseCashier(form: {
  montoDeclarado?: number | null
  tarjetasDevueltas?: number | null
  observaciones?: string | null
}) {
  const { data } = await api.post<CashierSession>('/api/operational-config/cashier/preclose', form)
  return data
}

export async function closeCashier(form: {
  montoDeclarado: number
  tarjetasDevueltas: number
  observaciones?: string | null
}) {
  const { data } = await api.post<CashierSession>('/api/operational-config/cashier/close', form)
  return data
}

export async function registerCashierCustomer(form: {
  nombre: string
  apellidoPaterno?: string | null
  apellidoMaterno?: string | null
  telefono?: string | null
  email?: string | null
  fechaNacimiento?: string | null
  documentoIdentidad?: string | null
  numeroTarjeta: string
  nip?: string | null
  observaciones?: string | null
}) {
  const { data } = await api.post<CustomerRegistration>('/api/operational-config/cashier/customers', form)
  return data
}

export async function createOperationalAssignment(form: {
  usuarioId: number
  estacionId: number
  turnoId: number
  fechaOperacion: string
  rolOperativo: 'SUPERVISOR' | 'TESORERO' | 'CAJERO'
  activa: boolean
}) {
  const { data } = await api.post<OperationalAssignment>('/api/operational-config/assignments', form)
  return data
}

export async function updateOperationalAssignment(
  id: number,
  form: {
    usuarioId: number
    estacionId: number
    turnoId: number
    fechaOperacion: string
    rolOperativo: 'SUPERVISOR' | 'TESORERO' | 'CAJERO'
    activa: boolean
  }
) {
  await api.put(`/api/operational-config/assignments/${id}`, form)
}

export async function deleteOperationalAssignment(id: number) {
  await api.delete(`/api/operational-config/assignments/${id}`)
}

export async function getOperationalCards(search = '') {
  const { data } = await api.get<OperationalCard[]>('/api/operational-config/cards', {
    params: search ? { search } : undefined
  })
  return data
}

export async function createOperationalCardRange(form: {
  numeroInicial: string
  numeroFinal: string
  tipo: 'CLIENTE' | 'GENERICA'
  fechaVencimiento: string | null
  maximoTarjetas: number
}) {
  const { data } = await api.post<OperationalCard[]>('/api/operational-config/cards/range', form)
  return data
}

export async function createOperationalCard(form: Omit<OperationalCard, 'id'>) {
  const { data } = await api.post<OperationalCard>('/api/operational-config/cards', form)
  return data
}

export async function updateOperationalCard(id: number, form: Omit<OperationalCard, 'id'>) {
  await api.put(`/api/operational-config/cards/${id}`, form)
}

export async function deleteOperationalCard(id: number) {
  await api.delete(`/api/operational-config/cards/${id}`)
}

export async function getOperationalAudit() {
  const { data } = await api.get<OperationalAuditEvent[]>('/api/operational-config/audit')
  return data
}

function emptyToNull(value: string) {
  return value.trim() === '' ? null : value.trim()
}

function normalizeCreatePayload(form: UserForm) {
  return {
    username: form.username.trim(),
    email: emptyToNull(form.email),
    telefono: emptyToNull(form.telefono),
    nombre: form.nombre.trim(),
    apellidoPaterno: emptyToNull(form.apellidoPaterno),
    apellidoMaterno: emptyToNull(form.apellidoMaterno),
    password: form.password,
    activo: form.activo,
    requiereCambioPassword: form.requiereCambioPassword,
    roles: form.roles
  }
}

function normalizeUpdatePayload(form: UserForm) {
  return {
    email: emptyToNull(form.email),
    telefono: emptyToNull(form.telefono),
    nombre: emptyToNull(form.nombre),
    apellidoPaterno: emptyToNull(form.apellidoPaterno),
    apellidoMaterno: emptyToNull(form.apellidoMaterno),
    activo: form.activo,
    bloqueado: form.bloqueado,
    motivoBloqueo: emptyToNull(form.motivoBloqueo),
    requiereCambioPassword: form.requiereCambioPassword,
    roles: form.roles
  }
}
