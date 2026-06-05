export interface LoginResponse {
  tokenType: string
  accessToken: string
  userId: number
  username: string
  requiereCambioPassword: boolean
  roles: string[]
  permisos: string[]
}

export interface Role {
  id: number
  nombre: string
  descripcion: string | null
  activo: boolean
}

export interface UserSummary {
  id: number
  username: string
  email: string | null
  telefono: string | null
  nombre: string
  apellidoPaterno: string | null
  apellidoMaterno: string | null
  activo: boolean
  bloqueado: boolean
  eliminado: boolean
  requiereCambioPassword: boolean
  ultimoLogin: string | null
  roles: string[]
}

export interface UserDetail extends UserSummary {
  motivoBloqueo: string | null
  intentosFallidos: number
  ultimoLogout: string | null
  mfaHabilitado: boolean
  mfaTipo: string | null
  version: number
  permisos: string[]
}

export interface UserForm {
  username: string
  email: string
  telefono: string
  nombre: string
  apellidoPaterno: string
  apellidoMaterno: string
  password: string
  activo: boolean
  bloqueado: boolean
  motivoBloqueo: string
  requiereCambioPassword: boolean
  roles: string[]
}

export interface OperationSchedule {
  id: number
  nombre: string
  horaInicio: string
  horaFin: string
  activo: boolean
}

export interface CashierShift {
  id: number
  nombre: string
  horaInicio: string
  horaFin: string
  activo: boolean
}

export interface Workstation {
  id: number
  nombre: string
  tipo: 'CAJA' | 'TESORERIA'
  sala: string | null
  ubicacion: string | null
  activa: boolean
}

export interface OperationalAssignment {
  id: number
  usuarioId: number
  username: string
  nombreUsuario: string
  estacionId: number
  estacionNombre: string
  estacionTipo: string
  turnoId: number
  turnoNombre: string
  fechaOperacion: string
  rolOperativo: 'SUPERVISOR' | 'TESORERO' | 'CAJERO'
  activa: boolean
}

export interface OperationalCard {
  id: number
  numeroTarjeta: string
  tipo: 'CLIENTE' | 'GENERICA'
  fechaVencimiento: string | null
  estado: string
}

export interface OperationalAuditEvent {
  id: number
  accion: string
  entidad: string
  entidadId: number | null
  detalle: string | null
  usuarioId: number | null
  username: string | null
  fechaEvento: string
  ipOrigen: string | null
}
