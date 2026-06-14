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
  cajaId: number | null
  cajaNombre: string | null
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

export interface OperationalDay {
  id: number
  fechaJornada: string
  estado: 'ABIERTA' | 'CERRANDO' | 'CERRADA'
  fechaApertura: string
  fechaCierre: string | null
  aperturaPor: number
  aperturaUsername: string
  aperturaNombre: string
  cierrePor: number | null
  cierreUsername: string | null
  observaciones: string | null
}

export interface EgmMeterSnapshot {
  id: number
  jornadaId: number
  egmId: number
  egmAddr: string
  egmNombre: string
  tipoSnapshot: 'APERTURA' | 'CIERRE' | 'REINTENTO' | 'MANUAL'
  estado: string
  proveedor: string
  coinIn: number
  coinOut: number
  jackpot: number
  handpayCancelled: number
  cancelled: number
  gamesPlayed: number
  gamesWon: number
  gamesLost: number
  billsAccepted: number
  currentCredits: number
  rawResponse: string | null
  mensaje: string | null
  fechaSnapshot: string
}

export interface EgmDailyReconciliation {
  id: number
  jornadaId: number
  egmId: number
  egmAddr: string
  egmNombre: string
  coinInDelta: number
  coinOutDelta: number
  jackpotDelta: number
  cancelledDelta: number
  billsAcceptedDelta: number
  currentCreditsCierre: number
  hostLoads: number
  hostCashouts: number
  gananciaCalculada: number
  perdidaCalculada: number
  cajaReportado: number
  diferenciaVsCaja: number
  estado: 'CUADRADO' | 'DIFERENCIA' | 'INCOMPLETO' | 'REQUIERE_REVISION'
  detalle: string | null
  fechaCalculo: string
}

export interface OperationalCloseSummary {
  id: number
  jornada: OperationalDay
  estado: 'EN_PROCESO' | 'COMPLETO' | 'CON_DIFERENCIAS' | 'INCOMPLETO' | 'FALLIDO'
  porcentaje: number
  totalEgms: number
  egmsOk: number
  egmsDiferencia: number
  egmsIncompletas: number
  totalCajaReportado: number
  totalEgmCalculado: number
  diferenciaTotal: number
  observaciones: string | null
  iniciadoUsername: string | null
  fechaInicio: string
  fechaFin: string | null
  cajasCerradas: CashierSession[]
  snapshots: EgmMeterSnapshot[]
  conciliaciones: EgmDailyReconciliation[]
}

export interface OperationalCard {
  id: number
  numeroTarjeta: string
  tipo: 'CLIENTE' | 'GENERICA'
  fechaVencimiento: string | null
  estado: string
  clienteNombre?: string | null
  capturadaEgm?: boolean
  capturaEgmTxid?: string | null
  capturaEgmFecha?: string | null
}

export interface TreasurySession {
  id: number
  jornadaId: number
  fechaJornada: string
  estacionId: number
  estacionNombre: string
  estado: 'ABIERTA' | 'PRECIERRE' | 'CERRADA'
  saldoInicial: number
  saldoActual: number
  fechaApertura: string
  aperturaUsername: string
  fechaPrecierre: string | null
  precierreUsername: string | null
  fechaCierre: string | null
  cierreUsername: string | null
  observaciones: string | null
}

export interface TreasuryMovement {
  id: number
  tesoreriaJornadaId: number
  estacionCajaId: number | null
  estacionCajaNombre: string | null
  turnoId: number | null
  turnoNombre: string | null
  tipo: 'ENTRADA' | 'SALIDA' | 'FONDO_CAJA' | 'DEVOLUCION_CAJA'
  concepto: string
  monto: number
  referencia: string | null
  observaciones: string | null
  registradoPor: number
  registradoUsername: string
  fechaMovimiento: string
}

export interface TreasuryCardMovement {
  id: number
  tesoreriaJornadaId: number
  estacionCajaId: number
  estacionCajaNombre: string
  turnoId: number | null
  turnoNombre: string | null
  tipo: 'ENTREGA_RANGO' | 'DEVOLUCION_TARJETAS'
  numeroInicial: string
  numeroFinal: string
  cantidad: number
  referencia: string | null
  observaciones: string | null
  registradoPor: number
  registradoUsername: string
  fechaMovimiento: string
}

export interface TreasuryLedgerEntry {
  id: string
  categoria: 'EFECTIVO' | 'TARJETAS' | 'BITACORA'
  tipo: string
  detalle: string
  estacion: string | null
  turno: string | null
  monto: number | null
  cantidad: number | null
  referencia: string | null
  observaciones: string | null
  username: string | null
  fechaEvento: string
}

export interface TreasuryConsole {
  jornada: OperationalDay | null
  tesoreria: TreasurySession | null
  asignaciones: OperationalAssignment[]
  movimientos: TreasuryMovement[]
  movimientosTarjetas: TreasuryCardMovement[]
  cajasCerradas: CashierSession[]
}

export interface CashierSession {
  id: number
  jornadaId: number
  fechaJornada: string
  cajaId: number
  cajaNombre: string
  estacionId: number
  estacionNombre: string
  turnoId: number
  turnoNombre: string
  estado: 'ABIERTA' | 'PRECIERRE' | 'CERRADA'
  saldoInicial: number
  saldoActual: number
  tarjetasIniciales: number
  tarjetasActuales: number
  fechaApertura: string
  aperturaUsername: string
  fechaPrecierre: string | null
  precierreUsername: string | null
  fechaCierre: string | null
  cierreUsername: string | null
  montoDeclaradoCierre: number | null
  tarjetasDevueltasCierre: number | null
  observaciones: string | null
}

export interface CashierMovement {
  id: number
  cajaJornadaId: number
  estacionId: number | null
  estacionNombre: string | null
  tipo: 'REPOSICION' | 'DEVOLUCION' | 'VENTA' | 'PAGO' | 'PAGO_MANUAL' | 'TRANSACCION_ESPECIAL' | 'CORTESIA' | 'PROMOCIONAL' | 'ALTA_CLIENTE'
  numeroTarjeta: string | null
  monto: number
  impactoSaldo: number | null
  maquina: string | null
  motivo: string | null
  referencia: string | null
  observaciones: string | null
  registradoPor: number
  registradoUsername: string
  fechaMovimiento: string
}

export interface CustomerRegistration {
  clienteId: number
  nombre: string
  apellidoPaterno: string | null
  apellidoMaterno: string | null
  telefono: string | null
  email: string | null
  fechaNacimiento: string | null
  documentoIdentidad: string | null
  estadoCliente: string
  tarjetaId: number
  numeroTarjeta: string
  estadoTarjeta: string
  asignacionId: number
  cajaJornadaId: number
  cajaNombre: string | null
  turnoId: number | null
  turnoNombre: string | null
  asignadoPor: number
  asignadoUsername: string
  fechaAsignacion: string
  observaciones: string | null
}

export interface CashierConsole {
  jornada: OperationalDay | null
  asignacion: OperationalAssignment | null
  caja: CashierSession | null
  movimientos: CashierMovement[]
  movimientosTarjeta: CashierMovement[]
  fondosTesoreria: TreasuryMovement[]
  tarjetasTesoreria: TreasuryCardMovement[]
  clientes: CustomerRegistration[]
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
