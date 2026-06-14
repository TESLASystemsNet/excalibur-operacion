<template>
  <main class="app-shell">
    <section v-if="!session" class="login-screen">
      <div class="brand-panel">
        <div class="brand-mark">PV</div>
        <p class="eyebrow">PLAY VALLEY CASINO</p>
        <h1>Control de acceso</h1>
        <p class="brand-copy">
          Autenticacion segura para usuarios, roles y permisos del sistema operativo.
        </p>
      </div>

      <form class="login-card" @submit.prevent="handleLogin">
        <div>
          <p class="section-kicker">Inicio de sesion</p>
          <h2>Ingresa tus credenciales</h2>
        </div>

        <label>
          Usuario
          <input v-model.trim="loginForm.username" autocomplete="username" required />
        </label>

        <label>
          Password
          <input v-model="loginForm.password" type="password" autocomplete="current-password" required />
        </label>

        <p v-if="errorMessage" class="error-line">{{ errorMessage }}</p>

        <button class="primary-action" :disabled="loading" type="submit">
          <LogIn :size="18" />
          {{ loading ? 'Validando...' : 'Entrar' }}
        </button>
      </form>
    </section>

    <section v-else class="workspace">
      <aside class="sidebar">
        <div class="product-block">
          <div class="brand-mark small">PV</div>
          <div>
            <strong>Play Valley</strong>
            <span>CASINO</span>
          </div>
        </div>

        <nav v-if="isAdminDashboard">
          <button class="nav-item active">
            <Users :size="18" />
            Usuarios
          </button>
          <button class="nav-item">
            <ShieldCheck :size="18" />
            Seguridad
          </button>
        </nav>

        <nav v-else class="supervisor-nav">
          <button
            class="hamburger-action"
            type="button"
            :aria-expanded="supervisorMenuOpen"
            aria-controls="supervisor-menu"
            @click="supervisorMenuOpen = !supervisorMenuOpen"
          >
            <Menu :size="18" />
            {{ operatorMenuLabel }}
          </button>

          <div v-if="supervisorMenuOpen" id="supervisor-menu" class="supervisor-menu">
            <div class="menu-root">
              <ClipboardList :size="16" />
              {{ operatorHomeLabel }}
            </div>
            <div v-for="section in operatorMenu" :key="section.title" class="menu-group">
              <button
                class="menu-group-title"
                :class="{ open: isSupervisorSectionOpen(section.title) }"
                type="button"
                :aria-expanded="isSupervisorSectionOpen(section.title)"
                @click="toggleSupervisorSection(section.title)"
              >
                <span>{{ section.title }}</span>
                <ChevronDown :size="15" />
              </button>
              <div v-if="isSupervisorSectionOpen(section.title)" class="menu-children">
                <button
                  v-for="item in section.items"
                  :key="item"
                  class="menu-leaf"
                  :class="{ active: item === activeSupervisorOption }"
                  type="button"
                  @click="selectOperatorMenuItem(item)"
                >
                  {{ item }}
                </button>
              </div>
            </div>
          </div>
        </nav>

        <div class="session-card">
          <span>Sesion</span>
          <strong>{{ session.username }}</strong>
          <small>{{ session.roles.join(', ') }}</small>
          <button class="ghost-action" @click="handleLogout">
            <LogOut :size="16" />
            Salir
          </button>
        </div>
      </aside>

      <section v-if="isAdminDashboard" class="content">
        <header class="topbar">
          <div>
            <p class="section-kicker">Administracion</p>
            <h1>Usuarios del sistema</h1>
          </div>
          <button class="primary-action compact" @click="newUser">
            <UserPlus :size="18" />
            Nuevo usuario
          </button>
        </header>

        <section class="metrics-grid">
          <article class="metric-card">
            <span>Total</span>
            <strong>{{ users.length }}</strong>
          </article>
          <article class="metric-card">
            <span>Activos</span>
            <strong>{{ activeUsers }}</strong>
          </article>
          <article class="metric-card">
            <span>Bloqueados</span>
            <strong>{{ blockedUsers }}</strong>
          </article>
          <article class="metric-card">
            <span>Roles</span>
            <strong>{{ roles.length }}</strong>
          </article>
        </section>

        <section class="toolbar">
          <div class="search-box">
            <Search :size="18" />
            <input v-model="search" placeholder="Buscar por usuario, nombre o correo" @keyup.enter="loadUsers" />
          </div>
          <button class="secondary-action" @click="loadUsers">
            <RefreshCw :size="16" />
            Actualizar
          </button>
        </section>

        <section class="main-grid">
          <div class="table-panel">
            <table>
              <thead>
                <tr>
                  <th>Usuario</th>
                  <th>Nombre</th>
                  <th>Roles</th>
                  <th>Estado</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="user in users"
                  :key="user.id"
                  :class="{ selected: editorMode === 'edit' && selectedUser?.id === user.id }"
                >
                  <td>
                    <strong>{{ user.username }}</strong>
                    <span>{{ user.email || 'Sin correo' }}</span>
                  </td>
                  <td>{{ fullName(user) }}</td>
                  <td>
                    <div class="pill-row">
                      <span v-for="role in user.roles" :key="role" class="role-pill">{{ role }}</span>
                    </div>
                  </td>
                  <td>
                    <span class="status-pill" :class="{ danger: user.bloqueado || !user.activo }">
                      {{ userStatus(user) }}
                    </span>
                  </td>
                  <td>
                    <button class="icon-action" title="Editar" @click="selectUser(user.id)">
                      <Pencil :size="16" />
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <div v-if="editorMode !== 'none'" class="modal-backdrop" role="presentation">
          <form class="editor-panel modal-panel" role="dialog" aria-modal="true" autocomplete="off" @submit.prevent="saveUser">
            <div class="panel-heading">
              <div>
                <p class="section-kicker">{{ editorMode === 'edit' ? 'Edicion' : 'Alta' }}</p>
                <h2>{{ editorMode === 'edit' ? selectedUser?.username : 'Nuevo usuario' }}</h2>
              </div>
              <div class="panel-actions">
                <button
                  v-if="editorMode === 'edit'"
                  class="icon-action danger"
                  type="button"
                  title="Eliminar"
                  @click="removeSelected"
                >
                  <Trash2 :size="16" />
                </button>
                <button class="icon-action" type="button" title="Cerrar formulario" @click="closeEditor">
                  <X :size="16" />
                </button>
              </div>
            </div>

            <div class="form-grid">
              <label>
                Usuario
                <input v-model.trim="form.username" :disabled="editorMode === 'edit'" required />
              </label>
              <label>
                Nombre
                <input v-model.trim="form.nombre" required />
              </label>
              <label>
                Apellido paterno
                <input v-model.trim="form.apellidoPaterno" />
              </label>
              <label>
                Apellido materno
                <input v-model.trim="form.apellidoMaterno" />
              </label>
              <label>
                Correo
                <input v-model.trim="form.email" type="email" />
              </label>
              <label>
                Telefono
                <input v-model.trim="form.telefono" />
              </label>
            </div>

            <label v-if="editorMode === 'create'">
              Password temporal
              <input v-model="form.password" type="password" minlength="8" required />
            </label>

            <div class="switch-row">
              <label class="switch-label">
                <input v-model="form.activo" type="checkbox" />
                Activo
              </label>
              <label class="switch-label">
                <input v-model="form.requiereCambioPassword" type="checkbox" />
                Cambiar password
              </label>
              <label v-if="editorMode === 'edit'" class="switch-label">
                <input v-model="form.bloqueado" type="checkbox" />
                Bloqueado
              </label>
            </div>

            <label v-if="editorMode === 'edit' && form.bloqueado">
              Motivo de bloqueo
              <input v-model.trim="form.motivoBloqueo" />
            </label>

            <fieldset class="roles-box">
              <legend>Roles</legend>
              <label v-for="role in roles" :key="role.id" class="role-check">
                <input v-model="form.roles" :value="role.nombre" type="checkbox" />
                <span>{{ role.nombre }}</span>
              </label>
            </fieldset>

            <div v-if="editorMode === 'edit'" class="password-box">
              <label>
                Nuevo password
                <input v-model="passwordReset" type="password" minlength="8" placeholder="Opcional" />
              </label>
              <button class="secondary-action" type="button" :disabled="!passwordReset" @click="resetPassword">
                <KeyRound :size="16" />
                Actualizar password
              </button>
            </div>

            <p v-if="successMessage" class="success-line">{{ successMessage }}</p>
            <p v-if="errorMessage" class="error-line">{{ errorMessage }}</p>

            <div class="modal-footer">
              <button class="secondary-action" type="button" :disabled="loading" @click="closeEditor">
                <X :size="16" />
                Cancelar
              </button>
              <button class="primary-action" :disabled="loading" type="submit">
                <Save :size="18" />
                {{ loading ? 'Guardando...' : 'Aceptar' }}
              </button>
            </div>
          </form>
        </div>
      </section>

      <section v-else class="content">
        <header class="topbar">
          <div>
            <p class="section-kicker">{{ operatorRoleLabel }}</p>
            <h1>{{ activeSupervisorOption }}</h1>
            <p v-if="successMessage" class="success-line">{{ successMessage }}</p>
            <p v-if="errorMessage" class="error-line">{{ errorMessage }}</p>
          </div>
          <button
            v-if="activeSupervisorOption === 'Inicio de Operaciones'"
            class="primary-action compact"
            type="button"
            :disabled="loading || Boolean(currentOperationalDay)"
            @click="openDay"
          >
            <PlayCircle :size="18" />
            {{ currentOperationalDay ? 'Jornada abierta' : 'Realizar apertura de jornada' }}
          </button>
          <button
            v-if="isCashierDashboard"
            class="secondary-action compact"
            type="button"
            :disabled="loading"
            @click="loadOperationalConfig"
          >
            <RefreshCw :size="16" />
            Actualizar
          </button>
        </header>

        <section v-if="isCashierDashboard" class="cashier-dashboard">
          <article class="config-panel cashier-status-panel">
            <div>
              <p class="section-kicker">Apertura de Caja</p>
              <h2>Validacion de turno</h2>
            </div>
            <div class="treasury-summary">
              <div>
                <span>Jornada</span>
                <strong>{{ currentOperationalDay ? formatDate(currentOperationalDay.fechaJornada) : 'Sin jornada' }}</strong>
              </div>
              <div>
                <span>Cajero</span>
                <strong>{{ session.username }}</strong>
              </div>
              <div>
                <span>Caja</span>
                <strong>{{ currentCashierAssignment?.estacionNombre ?? 'Sin asignacion' }}</strong>
              </div>
              <div>
                <span>Turno</span>
                <strong>{{ currentCashierAssignment?.turnoNombre ?? 'Pendiente' }}</strong>
              </div>
            </div>
            <div class="cashier-opening-grid">
              <div>
                <span>Fondo recibido</span>
                <strong>{{ formatMoney(cashierFundsReceived) }}</strong>
                <small>{{ cashierFundMovements.length }} movimiento(s) desde tesoreria</small>
              </div>
              <div>
                <span>Estado caja</span>
                <strong>{{ cashierSession?.estado ?? 'Sin abrir' }}</strong>
                <small>{{ cashierSession ? `Apertura ${formatDateTime(cashierSession.fechaApertura)}` : 'Pendiente de apertura' }}</small>
              </div>
              <div>
                <span>Tarjetas / clientes</span>
                <strong>{{ operationalCards.length }}</strong>
                <small>Inventario global disponible en BD</small>
              </div>
            </div>
            <div class="cashier-checklist">
              <span :class="{ done: Boolean(currentCashierAssignment) }">Cajero asignado a caja y turno</span>
              <span :class="{ done: cashierFundsReceived > 0 }">Fondo inicial enviado por tesoreria</span>
              <span :class="{ done: Boolean(cashierSession) }">Caja aperturada</span>
            </div>
            <div class="treasury-actions">
              <button
                class="primary-action compact"
                type="button"
                :disabled="loading || Boolean(cashierSession) || !currentCashierAssignment || cashierFundsReceived <= 0"
                @click="openCashierFromDashboard"
              >
                <PlayCircle :size="18" />
                {{ cashierSession ? 'Caja aperturada' : 'Aperturar Caja' }}
              </button>
            </div>
          </article>

          <section class="metrics-grid supervisor-metrics">
            <article class="metric-card">
              <span>Disponible en caja</span>
              <strong>{{ formatMoney(cashierFundsReceived - cashierCashReturned) }}</strong>
            </article>
            <article class="metric-card">
              <span>Devuelto a tesoreria</span>
              <strong>{{ formatMoney(cashierCashReturned) }}</strong>
            </article>
            <article class="metric-card">
              <span>Tarjetas BD</span>
              <strong>{{ operationalCards.length }}</strong>
            </article>
            <article class="metric-card">
              <span>Movimientos</span>
              <strong>{{ cashierFundMovements.length + cashierSessionMovements.length }}</strong>
            </article>
          </section>

          <section class="cashier-action-grid">
            <article
              v-for="action in cashierActions"
              :key="action.title"
              class="operation-card action-card"
              :class="{ selected: action.menu === activeSupervisorOption }"
              role="button"
              tabindex="0"
              @click="selectCashierAction(action.menu)"
              @keydown.enter.prevent="selectCashierAction(action.menu)"
              @keydown.space.prevent="selectCashierAction(action.menu)"
            >
              <div class="operation-icon">
                <CreditCard v-if="action.kind === 'card'" :size="22" />
                <Search v-else-if="action.kind === 'search'" :size="22" />
                <PlayCircle v-else :size="22" />
              </div>
              <div>
                <h2>{{ action.title }}</h2>
                <p>{{ action.description }}</p>
              </div>
              <button class="secondary-action" type="button" @click.stop="selectCashierAction(action.menu)">
                {{ action.menu === activeSupervisorOption ? 'Abierto' : action.status }}
              </button>
            </article>
          </section>

          <article v-if="activeSupervisorOption === 'Alta de Cliente'" class="config-panel">
            <div>
              <p class="section-kicker">Asignaciones</p>
              <h2>Clientes registrados en caja</h2>
            </div>
            <div class="table-panel config-table-panel">
              <table>
                <thead>
                  <tr>
                    <th>Fecha</th>
                    <th>Cliente</th>
                    <th>Tarjeta</th>
                    <th>Estado</th>
                    <th>Cajero</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="customer in cashierCustomers" :key="customer.asignacionId">
                    <td>{{ formatDateTime(customer.fechaAsignacion) }}</td>
                    <td>
                      <strong>{{ customerFullName(customer) }}</strong>
                      <span>{{ customer.documentoIdentidad || customer.telefono || customer.email || '--' }}</span>
                    </td>
                    <td>{{ customer.numeroTarjeta }}</td>
                    <td><span class="status-pill">{{ customer.estadoTarjeta }}</span></td>
                    <td>{{ customer.asignadoUsername }}</td>
                  </tr>
                  <tr v-if="cashierCustomers.length === 0">
                    <td colspan="5">Sin clientes registrados en esta caja.</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </article>

          <article class="config-panel">
            <div>
              <p class="section-kicker">Bitacora de Caja</p>
              <h2>Movimientos recibidos y devueltos</h2>
            </div>
            <div class="table-panel config-table-panel">
              <table>
                <thead>
                  <tr>
                    <th>Fecha</th>
                    <th>Operacion</th>
                    <th>Detalle</th>
                    <th>Importe</th>
                    <th>Referencia</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="movement in cashierBoxLogMovements" :key="movement.key">
                    <td>{{ formatDateTime(movement.fechaMovimiento) }}</td>
                    <td><span class="role-pill">{{ movement.tipo }}</span></td>
                    <td>
                      <strong>{{ movement.detalle }}</strong>
                      <span>{{ movement.subdetalle }}</span>
                    </td>
                    <td>{{ formatMoney(movement.monto) }}</td>
                    <td>{{ movement.referencia || movement.observaciones || '--' }}</td>
                  </tr>
                  <tr v-if="cashierBoxLogMovements.length === 0">
                    <td colspan="5">Sin movimientos registrados para esta caja.</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </article>
        </section>

        <section v-if="!isCashierDashboard && isOperationalConfigOption" class="config-workspace">
          <article
            v-if="activeSupervisorOption === 'Configuracion Operativa' || activeSupervisorOption === 'Horario de Operacion'"
            class="config-panel"
          >
            <div>
              <p class="section-kicker">Jornada</p>
              <h2>Horario de Operacion</h2>
            </div>
            <button class="primary-action compact section-new-action" type="button" @click="newOperationSchedule">
              <UserPlus :size="18" />
              Nuevo horario
            </button>
            <div class="table-panel config-table-panel">
              <table>
                <thead>
                  <tr>
                    <th>Nombre</th>
                    <th>Horario</th>
                    <th>Estado</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in operationSchedules" :key="item.id">
                    <td>
                      <strong>{{ item.nombre }}</strong>
                    </td>
                    <td>{{ item.horaInicio }} - {{ item.horaFin }}</td>
                    <td>
                      <span class="status-pill" :class="{ danger: !item.activo }">
                        {{ item.activo ? 'Activo' : 'Inactivo' }}
                      </span>
                    </td>
                    <td>
                      <div class="row-actions">
                        <button class="icon-action" type="button" title="Editar" @click="editOperationSchedule(item)">
                          <Pencil :size="16" />
                        </button>
                        <button class="icon-action danger" type="button" title="Eliminar" @click="removeOperationSchedule(item.id)">
                          <Trash2 :size="16" />
                        </button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </article>

          <article
            v-if="activeSupervisorOption === 'Configuracion Operativa' || activeSupervisorOption === 'Turnos de Caja'"
            class="config-panel"
          >
            <div>
              <p class="section-kicker">Cajas</p>
              <h2>Turnos de Caja</h2>
            </div>
            <button class="primary-action compact section-new-action" type="button" @click="newCashierShift">
              <UserPlus :size="18" />
              Nuevo turno
            </button>
            <div class="table-panel config-table-panel">
              <table>
                <thead>
                  <tr>
                    <th>Nombre</th>
                    <th>Horario</th>
                    <th>Estado</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in cashierShifts" :key="item.id">
                    <td>
                      <strong>{{ item.nombre }}</strong>
                    </td>
                    <td>{{ item.horaInicio }} - {{ item.horaFin }}</td>
                    <td>
                      <span class="status-pill" :class="{ danger: !item.activo }">
                        {{ item.activo ? 'Activo' : 'Inactivo' }}
                      </span>
                    </td>
                    <td>
                      <div class="row-actions">
                        <button class="icon-action" type="button" title="Editar" @click="editCashierShift(item)">
                          <Pencil :size="16" />
                        </button>
                        <button class="icon-action danger" type="button" title="Eliminar" @click="removeCashierShift(item.id)">
                          <Trash2 :size="16" />
                        </button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </article>

          <article
            v-if="activeSupervisorOption === 'Configuracion Operativa' || activeSupervisorOption === 'Cajas y Tesorerias'"
            class="config-panel"
          >
            <div>
              <p class="section-kicker">Estaciones</p>
              <h2>Cajas y Tesorerias</h2>
            </div>
            <button class="primary-action compact section-new-action" type="button" @click="newWorkstation">
              <UserPlus :size="18" />
              Nueva estacion
            </button>
            <div class="table-panel config-table-panel">
              <table>
                <thead>
                  <tr>
                    <th>Nombre</th>
                    <th>Tipo</th>
                    <th>Sala</th>
                    <th>Ubicacion</th>
                    <th>Estado</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in workstations" :key="item.id">
                    <td>
                      <strong>{{ item.nombre }}</strong>
                    </td>
                    <td>
                      <span class="role-pill">{{ item.tipo }}</span>
                    </td>
                    <td>{{ item.sala || 'Sin sala' }}</td>
                    <td>{{ item.ubicacion || 'Sin ubicacion' }}</td>
                    <td>
                      <span class="status-pill" :class="{ danger: !item.activa }">
                        {{ item.activa ? 'Activa' : 'Inactiva' }}
                      </span>
                    </td>
                    <td>
                      <div class="row-actions">
                        <button class="icon-action" type="button" title="Editar" @click="editWorkstation(item)">
                          <Pencil :size="16" />
                        </button>
                        <button class="icon-action danger" type="button" title="Eliminar" @click="removeWorkstation(item.id)">
                          <Trash2 :size="16" />
                        </button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </article>

          <article
            v-if="activeSupervisorOption === 'Configuracion Operativa' || activeSupervisorOption === 'Asignacion de Usuarios'"
            class="config-panel"
          >
            <div>
              <p class="section-kicker">Responsables</p>
              <h2>Asignacion de Usuarios</h2>
            </div>
            <button class="primary-action compact section-new-action" type="button" @click="newOperationalAssignment">
              <UserPlus :size="18" />
              Nueva asignacion
            </button>
            <div class="table-panel config-table-panel">
              <table>
                <thead>
                  <tr>
                    <th>Usuario</th>
                    <th>Estacion</th>
                    <th>Turno</th>
                    <th>Fecha</th>
                    <th>Rol</th>
                    <th>Estado</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in operationalAssignments" :key="item.id">
                    <td>
                      <strong>{{ item.username }}</strong>
                      <span>{{ item.nombreUsuario }}</span>
                    </td>
                    <td>
                      <strong>{{ item.estacionNombre }}</strong>
                      <span>{{ item.estacionTipo }}</span>
                    </td>
                    <td>{{ item.turnoNombre }}</td>
                    <td>{{ formatDate(item.fechaOperacion) }}</td>
                    <td>
                      <span class="role-pill">{{ item.rolOperativo }}</span>
                    </td>
                    <td>
                      <span class="status-pill" :class="{ danger: !item.activa }">
                        {{ item.activa ? 'Activa' : 'Inactiva' }}
                      </span>
                    </td>
                    <td>
                      <div class="row-actions">
                        <button class="icon-action" type="button" title="Editar" @click="editOperationalAssignment(item)">
                          <Pencil :size="16" />
                        </button>
                        <button class="icon-action danger" type="button" title="Eliminar" @click="removeOperationalAssignment(item.id)">
                          <Trash2 :size="16" />
                        </button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </article>

          <article
            v-if="activeSupervisorOption === 'Configuracion Operativa' || activeSupervisorOption === 'Definir Tarjetas'"
            class="config-panel"
          >
            <div>
              <p class="section-kicker">Tarjetas</p>
              <h2>Definir Tarjetas</h2>
            </div>
            <div class="section-action-row">
              <button class="secondary-action compact" type="button" @click="newOperationalCardRange">
                <CreditCard :size="18" />
                Alta por rango
              </button>
              <button class="primary-action compact" type="button" @click="newOperationalCard">
                <UserPlus :size="18" />
                Nueva tarjeta
              </button>
            </div>
            <div class="treasury-summary card-inventory-summary">
              <div>
                <span>Total tarjetas</span>
                <strong>{{ operationalCards.length }}</strong>
              </div>
              <div>
                <span>Disponibles</span>
                <strong>{{ availableCards }}</strong>
              </div>
              <div>
                <span>Asignadas</span>
                <strong>{{ assignedCards }}</strong>
              </div>
              <div>
                <span>Bloqueadas / inactivas</span>
                <strong>{{ unavailableCards }}</strong>
              </div>
            </div>
            <section class="toolbar card-toolbar">
              <div class="search-box">
                <Search :size="18" />
                <input v-model="cardSearch" placeholder="Buscar por numero de tarjeta" @keyup.enter="loadOperationalConfig" />
              </div>
              <button class="secondary-action" type="button" :disabled="loading" @click="loadOperationalConfig">
                <RefreshCw :size="16" />
                Buscar
              </button>
            </section>
            <div class="table-panel config-table-panel">
              <table>
                <thead>
                  <tr>
                    <th>Tarjeta</th>
                    <th>Cliente</th>
                    <th>Tipo</th>
                    <th>Vencimiento</th>
                    <th>Estado</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in operationalCards" :key="item.id">
                    <td>
                      <strong>{{ item.numeroTarjeta }}</strong>
                    </td>
                    <td>{{ cardCustomerName(item) }}</td>
                    <td>
                      <span class="role-pill">{{ item.tipo }}</span>
                    </td>
                    <td>{{ formatNullableDate(item.fechaVencimiento, 'Sin vencimiento') }}</td>
                    <td>
                      <span class="status-pill" :class="{ danger: item.estado !== 'DISPONIBLE' }">
                        {{ item.estado }}
                      </span>
                    </td>
                    <td>
                      <div class="row-actions">
                        <button class="icon-action" type="button" title="Editar" @click="editOperationalCard(item)">
                          <Pencil :size="16" />
                        </button>
                        <button class="icon-action danger" type="button" title="Eliminar" @click="removeOperationalCard(item.id)">
                          <Trash2 :size="16" />
                        </button>
                      </div>
                    </td>
                  </tr>
                  <tr v-if="operationalCards.length === 0">
                    <td colspan="6">Sin tarjetas registradas.</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </article>

          <article v-if="activeSupervisorOption === 'Configuracion Operativa'" class="config-panel">
            <div>
              <p class="section-kicker">Auditoria</p>
              <h2>Bitacora Operativa</h2>
            </div>
            <div class="compact-list">
              <div v-for="event in operationalAudit" :key="event.id">
                <strong>{{ event.accion }} · {{ event.entidad }}</strong>
                <span>{{ event.username || 'Sistema' }} · {{ formatDateTime(event.fechaEvento) }}</span>
              </div>
            </div>
          </article>
        </section>

        <section v-if="!isCashierDashboard && !isOperationalConfigOption && !isTreasuryOption && !isOperationalCloseOption" class="operation-hero">
          <div>
            <p class="section-kicker">PLAY VALLEY CASINO</p>
            <h2>Control operativo de clientes y tarjetas</h2>
            <p>
              Desde esta pantalla el supervisor podra iniciar la operacion del turno, validar clientes y
              autorizar la asignacion o reposicion de tarjetas de juego.
            </p>
          </div>
          <div class="operation-status">
            <span>Estado</span>
            <strong>{{ currentOperationalDay ? 'Jornada abierta' : 'Pendiente de apertura' }}</strong>
            <small v-if="currentOperationalDay">
              {{ formatDate(currentOperationalDay.fechaJornada) }} · {{ currentOperationalDay.aperturaUsername }}
            </small>
          </div>
        </section>

        <section v-if="!isCashierDashboard && !isOperationalConfigOption && !isTreasuryOption && !isOperationalCloseOption" class="metrics-grid supervisor-metrics">
          <article class="metric-card">
            <span>Jornada</span>
            <strong>{{ currentOperationalDay ? formatDate(currentOperationalDay.fechaJornada) : 'Sin abrir' }}</strong>
          </article>
          <article class="metric-card">
            <span>Clientes</span>
            <strong>0</strong>
          </article>
          <article class="metric-card">
            <span>Tarjetas activas</span>
            <strong>0</strong>
          </article>
          <article class="metric-card">
            <span>Autorizaciones</span>
            <strong>0</strong>
          </article>
        </section>

        <section v-if="!isCashierDashboard && isTreasuryOption" class="treasury-console">
          <article class="config-panel">
            <div>
              <p class="section-kicker">Consola de Tesoreria</p>
              <h2>Jornada, asignacion y saldo</h2>
            </div>
            <button class="secondary-action section-new-action" type="button" :disabled="loading" @click="loadOperationalConfig">
              <RefreshCw :size="16" />
              Actualizar
            </button>
            <div class="treasury-summary">
              <div>
                <span>Jornada abierta</span>
                <strong>{{ treasuryConsole?.jornada ? formatDate(treasuryConsole.jornada.fechaJornada) : 'Sin jornada' }}</strong>
              </div>
              <div>
                <span>Tesoreria</span>
                <strong>{{ treasurySession ? treasurySession.estacionNombre : 'Sin abrir' }}</strong>
              </div>
              <div>
                <span>Estado</span>
                <strong>{{ treasurySession?.estado ?? 'Pendiente' }}</strong>
              </div>
              <div>
                <span>Saldo actual</span>
                <strong>{{ treasurySession ? formatMoney(treasurySession.saldoActual) : formatMoney(0) }}</strong>
              </div>
            </div>
            <div class="treasury-actions">
              <button class="primary-action compact" type="button" :disabled="loading || Boolean(treasurySession)" @click="openTreasuryEditor('open')">
                <PlayCircle :size="18" />
                Abrir Tesoreria
              </button>
              <button class="secondary-action" type="button" :disabled="loading || !treasurySession || treasurySession.estado !== 'ABIERTA'" @click="openTreasuryEditor('fund')">
                Enviar fondo inicial
              </button>
              <button class="secondary-action" type="button" :disabled="loading || !treasurySession || treasurySession.estado !== 'ABIERTA'" @click="openTreasuryEditor('cardDelivery')">
                Entregar tarjetas
              </button>
              <button class="secondary-action" type="button" :disabled="loading || !treasurySession || treasurySession.estado !== 'ABIERTA'" @click="openTreasuryEditor('cashReturn')">
                Recibir efectivo
              </button>
              <button class="secondary-action" type="button" :disabled="loading || !treasurySession || treasurySession.estado !== 'ABIERTA'" @click="openTreasuryEditor('cardReturn')">
                Recibir tarjetas
              </button>
              <button class="secondary-action" type="button" :disabled="loading || !treasurySession || treasurySession.estado !== 'ABIERTA'" @click="openTreasuryEditor('preclose')">
                Precierre
              </button>
              <button class="secondary-action" type="button" :disabled="loading || !treasurySession || treasurySession.estado === 'CERRADA'" @click="openTreasuryEditor('close')">
                Cierre
              </button>
            </div>
          </article>

          <article class="config-panel">
            <div>
              <p class="section-kicker">Asignacion</p>
              <h2>Tesoreria y turno asignados</h2>
            </div>
            <div class="compact-list treasury-list">
              <div v-for="assignment in treasuryAssignments" :key="assignment.id">
                <strong>{{ assignment.estacionNombre }}</strong>
                <span>{{ assignment.turnoNombre }} · {{ assignment.username }}</span>
                <span>{{ formatDate(assignment.fechaOperacion) }}</span>
              </div>
              <div v-if="treasuryAssignments.length === 0">
                <strong>Sin asignacion TESORERO</strong>
                <span>Configura asignacion de usuarios para la jornada.</span>
                <span></span>
              </div>
            </div>
          </article>

          <article v-if="activeSupervisorOption !== 'Movimientos de Tesoreria' && activeSupervisorOption !== 'Bitacora de Tesoreria'" class="config-panel">
            <div>
              <p class="section-kicker">Movimientos</p>
              <h2>Bitacora del turno</h2>
            </div>
            <div class="table-panel config-table-panel">
              <table>
                <thead>
                  <tr>
                    <th>Fecha</th>
                    <th>Tipo</th>
                    <th>Concepto</th>
                    <th>Caja</th>
                    <th>Monto</th>
                    <th>Usuario</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="movement in treasuryMovements" :key="movement.id">
                    <td>{{ formatDateTime(movement.fechaMovimiento) }}</td>
                    <td><span class="role-pill">{{ movement.tipo }}</span></td>
                    <td>
                      <strong>{{ movement.concepto }}</strong>
                      <span>{{ movement.referencia || movement.observaciones || '' }}</span>
                    </td>
                    <td>{{ movement.estacionCajaNombre || 'Tesoreria' }}</td>
                    <td>{{ formatMoney(movement.monto) }}</td>
                    <td>{{ movement.registradoUsername }}</td>
                  </tr>
                  <tr v-for="movement in treasuryCardMovements" :key="`cards-${movement.id}`">
                    <td>{{ formatDateTime(movement.fechaMovimiento) }}</td>
                    <td><span class="role-pill">{{ movement.tipo }}</span></td>
                    <td>
                      <strong>{{ movement.numeroInicial }} - {{ movement.numeroFinal }}</strong>
                      <span>{{ movement.cantidad }} tarjetas</span>
                    </td>
                    <td>{{ movement.estacionCajaNombre }}</td>
                    <td>--</td>
                    <td>{{ movement.registradoUsername }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </article>

          <article v-if="activeSupervisorOption === 'Movimientos de Tesoreria'" class="config-panel">
            <div>
              <p class="section-kicker">Movimientos de Tesoreria</p>
              <h2>Entradas, salidas y fondos</h2>
            </div>
            <div class="table-panel config-table-panel">
              <table>
                <thead>
                  <tr>
                    <th>Fecha</th>
                    <th>Tipo</th>
                    <th>Concepto</th>
                    <th>Caja/Tesoreria</th>
                    <th>Turno</th>
                    <th>Monto</th>
                    <th>Usuario</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="movement in treasuryMovementLog" :key="`treasury-movement-${movement.id}`">
                    <td>{{ formatDateTime(movement.fechaMovimiento) }}</td>
                    <td><span class="role-pill">{{ movement.tipo }}</span></td>
                    <td>
                      <strong>{{ movement.concepto }}</strong>
                      <span>{{ movement.referencia || movement.observaciones || '' }}</span>
                    </td>
                    <td>{{ movement.estacionCajaNombre || 'Tesoreria' }}</td>
                    <td>{{ movement.turnoNombre || '--' }}</td>
                    <td>{{ formatMoney(movement.monto) }}</td>
                    <td>{{ movement.registradoUsername }}</td>
                  </tr>
                  <tr v-if="treasuryMovementLog.length === 0">
                    <td colspan="7">Sin movimientos de tesoreria registrados.</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </article>

          <article v-if="activeSupervisorOption === 'Bitacora de Tesoreria'" class="config-panel">
            <div>
              <p class="section-kicker">Bitacora de Tesoreria</p>
              <h2>Eventos auditables</h2>
            </div>
            <div class="table-panel config-table-panel">
              <table>
                <thead>
                  <tr>
                    <th>Fecha</th>
                    <th>Categoria</th>
                    <th>Tipo</th>
                    <th>Detalle</th>
                    <th>Estacion</th>
                    <th>Importe/Cantidad</th>
                    <th>Usuario</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="entry in treasuryLedger" :key="entry.id">
                    <td>{{ formatDateTime(entry.fechaEvento) }}</td>
                    <td><span class="role-pill">{{ entry.categoria }}</span></td>
                    <td>{{ entry.tipo }}</td>
                    <td>
                      <strong>{{ entry.detalle }}</strong>
                      <span>{{ entry.referencia || entry.observaciones || '' }}</span>
                    </td>
                    <td>{{ entry.estacion || 'Tesoreria' }}<span v-if="entry.turno"> · {{ entry.turno }}</span></td>
                    <td>{{ entry.monto !== null ? formatMoney(entry.monto) : entry.cantidad !== null ? `${entry.cantidad} tarjetas` : '--' }}</td>
                    <td>{{ entry.username || '--' }}</td>
                  </tr>
                  <tr v-if="treasuryLedger.length === 0">
                    <td colspan="7">Sin eventos de tesoreria registrados.</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </article>
        </section>

        <section v-if="!isCashierDashboard && isOperationalCloseOption" class="treasury-console">
          <article class="config-panel">
            <div>
              <p class="section-kicker">Cierre de Operaciones</p>
              <h2>Barrido EGM y conciliacion contra caja</h2>
            </div>
            <button class="secondary-action section-new-action" type="button" :disabled="loading" @click="loadOperationalConfig">
              <RefreshCw :size="16" />
              Actualizar
            </button>
            <div class="treasury-summary">
              <div>
                <span>Jornada</span>
                <strong>{{ operationalClose?.jornada ? formatDate(operationalClose.jornada.fechaJornada) : currentOperationalDay ? formatDate(currentOperationalDay.fechaJornada) : 'Sin jornada' }}</strong>
              </div>
              <div>
                <span>Estado cierre</span>
                <strong>{{ operationalClose?.estado ?? 'Pendiente' }}</strong>
              </div>
              <div>
                <span>Avance</span>
                <strong>{{ operationalClose?.porcentaje ?? 0 }}%</strong>
              </div>
              <div>
                <span>EGMs</span>
                <strong>{{ operationalClose?.egmsOk ?? 0 }}/{{ operationalClose?.totalEgms ?? 0 }} OK</strong>
              </div>
              <div>
                <span>Diferencias</span>
                <strong>{{ operationalClose?.egmsDiferencia ?? 0 }}</strong>
              </div>
              <div>
                <span>Incompletas</span>
                <strong>{{ operationalClose?.egmsIncompletas ?? 0 }}</strong>
              </div>
              <div>
                <span>Total caja</span>
                <strong>{{ formatMoney(operationalClose?.totalCajaReportado ?? 0) }}</strong>
              </div>
              <div>
                <span>Diferencia total</span>
                <strong>{{ formatMoney(operationalClose?.diferenciaTotal ?? 0) }}</strong>
              </div>
            </div>
            <p v-if="!currentOperationalDay" class="error-line">
              No hay jornada abierta. Realiza la apertura antes de ejecutar barrido o cierre.
            </p>
            <div v-if="currentOperationalDay" class="form-grid">
              <label>
                Forzar con diferencias
                <select v-model="operationalCloseForm.forzarConDiferencias">
                  <option :value="false">No</option>
                  <option :value="true">Si</option>
                </select>
              </label>
              <label class="form-span-2">
                Observaciones
                <input v-model.trim="operationalCloseForm.observaciones" autocomplete="off" placeholder="Motivo operativo del cierre" />
              </label>
            </div>
            <div v-if="currentOperationalDay" class="treasury-actions">
              <button class="primary-action compact" type="button" :disabled="loading" @click="runOperationalClose">
                <Save :size="18" />
                {{ loading ? 'Procesando...' : 'Ejecutar cierre' }}
              </button>
              <button class="secondary-action" type="button" :disabled="loading" @click="runEgmSweepTest">
                <RefreshCw :size="16" />
                Probar barrido real
              </button>
            </div>
          </article>

          <article class="config-panel">
            <div>
              <p class="section-kicker">Conciliacion por EGM</p>
              <h2>Perdidas y ganancias contra caja</h2>
            </div>
            <div class="table-panel config-table-panel">
              <table>
                <thead>
                  <tr>
                    <th>EGM</th>
                    <th>Estado</th>
                    <th>Delta coin in</th>
                    <th>Delta coin out</th>
                    <th>G/P EGM</th>
                    <th>Caja</th>
                    <th>Diferencia</th>
                    <th>Creditos cierre</th>
                    <th>Detalle</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in operationalCloseReconciliations" :key="item.id">
                    <td>
                      <strong>{{ item.egmNombre }}</strong>
                      <span>{{ item.egmAddr }}</span>
                    </td>
                    <td>
                      <span class="status-pill" :class="{ danger: item.estado !== 'CUADRADO' }">{{ item.estado }}</span>
                    </td>
                    <td>{{ formatMoney(item.coinInDelta) }}</td>
                    <td>{{ formatMoney(item.coinOutDelta) }}</td>
                    <td>{{ formatMoney(item.gananciaCalculada) }}</td>
                    <td>{{ formatMoney(item.cajaReportado) }}</td>
                    <td>{{ formatMoney(item.diferenciaVsCaja) }}</td>
                    <td>{{ formatMoney(item.currentCreditsCierre) }}</td>
                    <td>{{ item.detalle || '--' }}</td>
                  </tr>
                  <tr v-if="operationalCloseReconciliations.length === 0">
                    <td colspan="9">Sin conciliacion de cierre registrada.</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </article>

          <article class="config-panel">
            <div>
              <p class="section-kicker">Snapshots SAS</p>
              <h2>Apertura y cierre por maquina</h2>
            </div>
            <div class="table-panel config-table-panel">
              <table>
                <thead>
                  <tr>
                    <th>Fecha</th>
                    <th>EGM</th>
                    <th>Tipo</th>
                    <th>Estado</th>
                    <th>Meter coin in</th>
                    <th>Meter coin out</th>
                    <th>Jackpot</th>
                    <th>Creditos</th>
                    <th>Proveedor</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="snapshot in displayedEgmSnapshots" :key="snapshot.id">
                    <td>{{ formatDateTime(snapshot.fechaSnapshot) }}</td>
                    <td>
                      <strong>{{ snapshot.egmNombre }}</strong>
                      <span>{{ snapshot.egmAddr }}</span>
                    </td>
                    <td><span class="role-pill">{{ snapshot.tipoSnapshot }}</span></td>
                    <td><span class="status-pill" :class="{ danger: snapshot.estado !== 'OK' }">{{ snapshot.estado }}</span></td>
                    <td>{{ formatMoney(snapshot.coinIn) }}</td>
                    <td>{{ formatMoney(snapshot.coinOut) }}</td>
                    <td>{{ formatMoney(snapshot.jackpot) }}</td>
                    <td>{{ formatMoney(snapshot.currentCredits) }}</td>
                    <td>{{ snapshot.proveedor }}</td>
                  </tr>
                  <tr v-if="displayedEgmSnapshots.length === 0">
                    <td colspan="9">Sin snapshots de apertura, cierre o barrido manual.</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </article>
        </section>

        <section v-if="!isCashierDashboard && isOperationalStatementOption" class="treasury-console">
          <article class="config-panel">
            <div>
              <p class="section-kicker">Reportes</p>
              <h2>Estado de cuenta de operacion</h2>
            </div>
            <button class="secondary-action section-new-action" type="button" :disabled="loading" @click="loadOperationalConfig">
              <RefreshCw :size="16" />
              Actualizar
            </button>
            <div class="treasury-summary">
              <div>
                <span>Jornada</span>
                <strong>{{ operationalClose?.jornada ? formatDate(operationalClose.jornada.fechaJornada) : 'Sin cierre' }}</strong>
              </div>
              <div>
                <span>Estado</span>
                <strong>{{ operationalClose?.estado ?? 'Pendiente' }}</strong>
              </div>
              <div>
                <span>Fondo inicial cajas</span>
                <strong>{{ formatMoney(operationStatementInitialCash) }}</strong>
              </div>
              <div>
                <span>Efectivo cierre cajas</span>
                <strong>{{ formatMoney(operationStatementDeclaredCash) }}</strong>
              </div>
              <div>
                <span>Variacion caja</span>
                <strong>{{ formatMoney(operationStatementCashDelta) }}</strong>
              </div>
              <div>
                <span>Caja reportado EGM</span>
                <strong>{{ formatMoney(operationalClose?.totalCajaReportado ?? 0) }}</strong>
              </div>
              <div>
                <span>G/P EGM</span>
                <strong>{{ formatMoney(operationalClose?.totalEgmCalculado ?? 0) }}</strong>
              </div>
              <div>
                <span>Diferencia</span>
                <strong>{{ formatMoney(operationalClose?.diferenciaTotal ?? 0) }}</strong>
              </div>
            </div>
            <p v-if="!operationalClose" class="error-line">
              Sin cierre de operacion registrado para consultar.
            </p>
          </article>

          <article class="config-panel">
            <div>
              <p class="section-kicker">Caja</p>
              <h2>Resumen por caja</h2>
            </div>
            <div class="table-panel config-table-panel">
              <table>
                <thead>
                  <tr>
                    <th>Caja</th>
                    <th>Turno</th>
                    <th>Fondo inicial</th>
                    <th>Saldo sistema</th>
                    <th>Declarado cierre</th>
                    <th>Variacion</th>
                    <th>Estado</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="cashier in operationStatementCashiers" :key="cashier.id">
                    <td>{{ cashier.estacionNombre }}</td>
                    <td>{{ cashier.turnoNombre }}</td>
                    <td>{{ formatMoney(cashier.saldoInicial) }}</td>
                    <td>{{ formatMoney(cashier.saldoActual) }}</td>
                    <td>{{ formatMoney(cashier.montoDeclaradoCierre ?? cashier.saldoActual) }}</td>
                    <td>{{ formatMoney(Number(cashier.montoDeclaradoCierre ?? cashier.saldoActual) - Number(cashier.saldoInicial ?? 0)) }}</td>
                    <td><span class="status-pill">{{ cashier.estado }}</span></td>
                  </tr>
                  <tr v-if="operationStatementCashiers.length === 0">
                    <td colspan="7">Sin cajas cerradas registradas en el cierre.</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </article>

          <article class="config-panel">
            <div>
              <p class="section-kicker">EGM</p>
              <h2>Conciliacion por maquina</h2>
            </div>
            <div class="table-panel config-table-panel">
              <table>
                <thead>
                  <tr>
                    <th>EGM</th>
                    <th>Estado</th>
                    <th>Coin in</th>
                    <th>Coin out</th>
                    <th>G/P EGM</th>
                    <th>Caja</th>
                    <th>Diferencia</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in operationalCloseReconciliations" :key="item.id">
                    <td>
                      <strong>{{ item.egmNombre }}</strong>
                      <span>{{ item.egmAddr }}</span>
                    </td>
                    <td><span class="status-pill" :class="{ danger: item.estado !== 'CUADRADO' }">{{ item.estado }}</span></td>
                    <td>{{ formatMoney(item.coinInDelta) }}</td>
                    <td>{{ formatMoney(item.coinOutDelta) }}</td>
                    <td>{{ formatMoney(item.gananciaCalculada) }}</td>
                    <td>{{ formatMoney(item.cajaReportado) }}</td>
                    <td>{{ formatMoney(item.diferenciaVsCaja) }}</td>
                  </tr>
                  <tr v-if="operationalCloseReconciliations.length === 0">
                    <td colspan="7">Sin conciliacion EGM registrada.</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </article>
        </section>

        <section v-if="!isCashierDashboard && !isOperationalConfigOption && !isTreasuryOption && !isOperationalCloseOption && !isOperationalStatementOption" class="operation-grid">
          <article class="operation-card">
            <div class="operation-icon">
              <UserPlus :size="22" />
            </div>
            <div>
              <h2>Alta de cliente</h2>
              <p>Registro controlado de clientes con validacion de datos antes de emitir tarjeta.</p>
            </div>
            <button class="secondary-action" type="button" disabled>Preparado</button>
          </article>

          <article class="operation-card">
            <div class="operation-icon">
              <CreditCard :size="22" />
            </div>
            <div>
              <h2>Asignacion de tarjeta</h2>
              <p>Entrega inicial, reposicion o bloqueo de tarjeta con trazabilidad por supervisor.</p>
            </div>
            <button class="secondary-action" type="button" disabled>Preparado</button>
          </article>

          <article class="operation-card">
            <div class="operation-icon">
              <Search :size="22" />
            </div>
            <div>
              <h2>Consulta operativa</h2>
              <p>Busqueda por cliente, numero de tarjeta o estado para seguimiento de piso.</p>
            </div>
            <button class="secondary-action" type="button" disabled>Preparado</button>
          </article>
        </section>

        <div v-if="treasuryEditor !== 'none'" class="modal-backdrop" role="presentation">
          <form class="editor-panel modal-panel" role="dialog" aria-modal="true" autocomplete="off" @submit.prevent="saveTreasuryAction">
            <div class="panel-heading">
              <div>
                <p class="section-kicker">Tesoreria</p>
                <h2>{{ treasuryEditor === 'open' ? 'Abrir Tesoreria' : treasuryEditor === 'preclose' ? 'Precierre de Tesoreria' : treasuryEditor === 'close' ? 'Cierre de Tesoreria' : 'Movimiento de Tesoreria' }}</h2>
              </div>
              <button class="icon-action" type="button" title="Cerrar formulario" @click="treasuryEditor = 'none'">
                <X :size="16" />
              </button>
            </div>

            <div v-if="treasuryEditor === 'open'" class="form-grid">
              <label>
                Tesoreria
                <select v-model.number="treasuryOpenForm.estacionId" required>
                  <option :value="0">Tesoreria</option>
                  <option v-for="station in treasuryStations" :key="station.id" :value="station.id">{{ station.nombre }}</option>
                </select>
              </label>
              <label>
                Monto inicial
                <input v-model.number="treasuryOpenForm.saldoInicial" type="number" min="0" step="0.01" required />
              </label>
              <label>
                Observaciones
                <input v-model.trim="treasuryOpenForm.observaciones" />
              </label>
            </div>

            <div v-if="['movement', 'fund', 'cashReturn'].includes(treasuryEditor)" class="form-grid">
              <label v-if="treasuryEditor === 'movement'">
                Tipo
                <select v-model="treasuryMovementForm.tipo">
                  <option value="ENTRADA">Entrada</option>
                  <option value="SALIDA">Salida</option>
                </select>
              </label>
              <label>
                Concepto
                <input v-model.trim="treasuryMovementForm.concepto" :placeholder="treasuryEditor === 'fund' ? 'Fondo inicial a caja' : treasuryEditor === 'cashReturn' ? 'Devolucion de efectivo desde caja' : ''" />
              </label>
              <label>
                Monto
                <input v-model.number="treasuryMovementForm.monto" type="number" min="0.01" step="0.01" required />
              </label>
              <p v-if="treasuryEditor === 'cashReturn'" class="success-line form-grid-message">
                Esperado por cierre: {{ formatMoney(treasuryExpectedCashReturn) }}
              </p>
              <label v-if="treasuryEditor !== 'movement'">
                Caja
                <select v-model.number="treasuryMovementForm.estacionCajaId" required @change="applyExpectedCashReturnAmount">
                  <option :value="0">Caja</option>
                  <option v-for="station in cashierStations" :key="station.id" :value="station.id">{{ station.nombre }}</option>
                </select>
              </label>
              <label v-if="treasuryEditor !== 'movement'">
                Turno
                <select v-model.number="treasuryMovementForm.turnoId" @change="applyExpectedCashReturnAmount">
                  <option :value="0">Turno</option>
                  <option v-for="shift in cashierShifts" :key="shift.id" :value="shift.id">{{ shift.nombre }}</option>
                </select>
              </label>
              <label>
                Referencia
                <input v-model.trim="treasuryMovementForm.referencia" />
              </label>
              <label>
                Observaciones
                <input v-model.trim="treasuryMovementForm.observaciones" />
              </label>
            </div>

            <div v-if="['cardDelivery', 'cardReturn'].includes(treasuryEditor)" class="form-grid">
              <label>
                Caja
                <select v-model.number="treasuryCardForm.estacionCajaId" required>
                  <option :value="0">Caja</option>
                  <option v-for="station in cashierStations" :key="station.id" :value="station.id">{{ station.nombre }}</option>
                </select>
              </label>
              <label>
                Turno
                <select v-model.number="treasuryCardForm.turnoId">
                  <option :value="0">Turno</option>
                  <option v-for="shift in cashierShifts" :key="shift.id" :value="shift.id">{{ shift.nombre }}</option>
                </select>
              </label>
              <label>
                Numero inicial
                <input v-model.trim="treasuryCardForm.numeroInicial" required />
              </label>
              <label>
                Numero final
                <input v-model.trim="treasuryCardForm.numeroFinal" required />
              </label>
              <label>
                Referencia
                <input v-model.trim="treasuryCardForm.referencia" />
              </label>
              <label>
                Observaciones
                <input v-model.trim="treasuryCardForm.observaciones" />
              </label>
            </div>

            <div v-if="['preclose', 'close'].includes(treasuryEditor)" class="form-grid">
              <label>
                Observaciones
                <input v-model.trim="treasuryCloseForm.observaciones" />
              </label>
            </div>

            <p v-if="successMessage" class="success-line">{{ successMessage }}</p>
            <p v-if="errorMessage" class="error-line">{{ errorMessage }}</p>
            <p v-if="!cashierSession" class="error-line">
              Primero apertura la caja para registrar ventas.
            </p>

            <div class="modal-footer">
              <button class="secondary-action" type="button" :disabled="loading" @click="treasuryEditor = 'none'">
                <X :size="16" />
                Cancelar
              </button>
              <button class="primary-action" :disabled="loading" type="submit">
                <Save :size="18" />
                {{ loading ? 'Guardando...' : 'Aceptar' }}
              </button>
            </div>
          </form>
        </div>

        <div v-if="configEditor !== 'none'" class="modal-backdrop" role="presentation">
          <form class="editor-panel modal-panel" role="dialog" aria-modal="true" autocomplete="off" @submit.prevent="saveConfigEdit">
            <div class="panel-heading">
              <div>
                <p class="section-kicker">{{ selectedConfigId ? 'Edicion' : 'Alta' }}</p>
                <h2>{{ configEditorTitle }}</h2>
              </div>
              <button class="icon-action" type="button" title="Cerrar formulario" @click="closeConfigEditor">
                <X :size="16" />
              </button>
            </div>

            <div v-if="configEditor === 'schedule'" class="form-grid">
              <label>
                Nombre
                <input v-model.trim="operationScheduleForm.nombre" required />
              </label>
              <label>
                Hora inicio
                <input v-model="operationScheduleForm.horaInicio" type="time" required />
              </label>
              <label>
                Hora fin
                <input v-model="operationScheduleForm.horaFin" type="time" required />
              </label>
              <label class="switch-label">
                <input v-model="operationScheduleForm.activo" type="checkbox" />
                Activo
              </label>
            </div>

            <div v-if="configEditor === 'shift'" class="form-grid">
              <label>
                Nombre
                <input v-model.trim="cashierShiftForm.nombre" required />
              </label>
              <label>
                Hora inicio
                <input v-model="cashierShiftForm.horaInicio" type="time" required />
              </label>
              <label>
                Hora fin
                <input v-model="cashierShiftForm.horaFin" type="time" required />
              </label>
              <label class="switch-label">
                <input v-model="cashierShiftForm.activo" type="checkbox" />
                Activo
              </label>
            </div>

            <div v-if="configEditor === 'workstation'" class="form-grid">
              <label>
                Nombre
                <input v-model.trim="workstationForm.nombre" required />
              </label>
              <label>
                Tipo
                <select v-model="workstationForm.tipo">
                  <option value="CAJA">Caja</option>
                  <option value="TESORERIA">Tesoreria</option>
                </select>
              </label>
              <label>
                Sala
                <input v-model.trim="workstationForm.sala" />
              </label>
              <label>
                Ubicacion
                <input v-model.trim="workstationForm.ubicacion" />
              </label>
              <label class="switch-label">
                <input v-model="workstationForm.activa" type="checkbox" />
                Activa
              </label>
            </div>

            <div v-if="configEditor === 'assignment'" class="form-grid">
              <label>
                Usuario
                <select v-model.number="assignmentForm.usuarioId" required>
                  <option :value="0">Usuario</option>
                  <option v-for="user in users" :key="user.id" :value="user.id">{{ user.username }}</option>
                </select>
              </label>
              <label>
                Caja/Tesoreria
                <select v-model.number="assignmentForm.estacionId" required>
                  <option :value="0">Caja/Tesoreria</option>
                  <option v-for="station in workstations" :key="station.id" :value="station.id">
                    {{ station.nombre }} - {{ station.tipo }}
                  </option>
                </select>
              </label>
              <label>
                Turno
                <select v-model.number="assignmentForm.turnoId" required>
                  <option :value="0">Turno</option>
                  <option v-for="shift in cashierShifts" :key="shift.id" :value="shift.id">{{ shift.nombre }}</option>
                </select>
              </label>
              <label>
                Fecha operacion
                <input
                  v-model.trim="assignmentForm.fechaOperacion"
                  inputmode="numeric"
                  maxlength="10"
                  pattern="\d{2}/\d{2}/\d{4}"
                  placeholder="dd/mm/aaaa"
                  required
                  @blur="assignmentForm.fechaOperacion = normalizeDisplayDate(assignmentForm.fechaOperacion)"
                />
              </label>
              <label>
                Rol operativo
                <select v-model="assignmentForm.rolOperativo">
                  <option value="SUPERVISOR">Supervisor</option>
                  <option value="TESORERO">Tesorero</option>
                  <option value="CAJERO">Cajero</option>
                </select>
              </label>
              <label class="switch-label">
                <input v-model="assignmentForm.activa" type="checkbox" />
                Activa
              </label>
            </div>

            <div v-if="configEditor === 'card'" class="form-grid">
              <label>
                Numero de tarjeta
                <input v-model.trim="cardForm.numeroTarjeta" required />
              </label>
              <label>
                Tipo
                <select v-model="cardForm.tipo">
                  <option value="CLIENTE">Cliente</option>
                  <option value="GENERICA">Generica</option>
                </select>
              </label>
              <label>
                Fecha vencimiento
                <input
                  v-model="cardForm.fechaVencimiento"
                  type="date"
                />
              </label>
              <label>
                Estado
                <select v-model="cardForm.estado">
                  <option value="DISPONIBLE">Disponible</option>
                  <option value="ASIGNADA">Asignada</option>
                  <option value="BLOQUEADA">Bloqueada</option>
                  <option value="VENCIDA">Vencida</option>
                  <option value="INACTIVA">Inactiva</option>
                </select>
              </label>
            </div>

            <div v-if="configEditor === 'cardRange'" class="form-grid">
              <label>
                Numero inicial
                <input v-model.trim="cardRangeForm.numeroInicial" inputmode="numeric" required />
              </label>
              <label>
                Numero final
                <input v-model.trim="cardRangeForm.numeroFinal" inputmode="numeric" required />
              </label>
              <label>
                Tipo
                <select v-model="cardRangeForm.tipo">
                  <option value="CLIENTE">Cliente</option>
                  <option value="GENERICA">Generica</option>
                </select>
              </label>
              <label>
                Fecha vencimiento
                <input
                  v-model="cardRangeForm.fechaVencimiento"
                  type="date"
                />
              </label>
              <label>
                Maximo permitido
                <input v-model.number="cardRangeForm.maximoTarjetas" type="number" min="1" max="10000" required />
              </label>
            </div>

            <p v-if="successMessage" class="success-line">{{ successMessage }}</p>
            <p v-if="errorMessage" class="error-line">{{ errorMessage }}</p>

            <div class="modal-footer">
              <button class="secondary-action" type="button" :disabled="loading" @click="closeConfigEditor">
                <X :size="16" />
                Cancelar
              </button>
            <button class="primary-action" :disabled="loading" type="submit">
                <Save :size="18" />
                {{ loading ? 'Guardando...' : 'Aceptar' }}
              </button>
            </div>
          </form>
        </div>

        <div v-if="cashierCustomerModalOpen" class="modal-backdrop" role="presentation">
          <form
            class="editor-panel modal-panel cashier-customer-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="cashier-customer-modal-title"
            autocomplete="off"
            @submit.prevent="saveCashierCustomer"
          >
            <div class="panel-heading">
              <div>
                <p class="section-kicker">Cliente / Tarjeta</p>
                <h2 id="cashier-customer-modal-title">Registrar cliente y asignar tarjeta</h2>
              </div>
              <button class="icon-action" type="button" title="Cerrar formulario" @click="closeCashierCustomerModal">
                <X :size="16" />
              </button>
            </div>

            <div class="cashier-customer-form">
              <label>
                Nombre
                <input v-model.trim="cashierCustomerForm.nombre" autocomplete="off" required />
              </label>
              <label>
                Apellido paterno
                <input v-model.trim="cashierCustomerForm.apellidoPaterno" autocomplete="off" />
              </label>
              <label>
                Apellido materno
                <input v-model.trim="cashierCustomerForm.apellidoMaterno" autocomplete="off" />
              </label>
              <label>
                Telefono
                <input v-model.trim="cashierCustomerForm.telefono" autocomplete="off" inputmode="tel" />
              </label>
              <label>
                Email
                <input v-model.trim="cashierCustomerForm.email" autocomplete="off" type="email" />
              </label>
              <label>
                Fecha nacimiento
                <input v-model="cashierCustomerForm.fechaNacimiento" autocomplete="off" type="date" />
              </label>
              <label>
                Documento identidad
                <input v-model.trim="cashierCustomerForm.documentoIdentidad" autocomplete="off" />
              </label>
              <label>
                Tarjeta disponible
                <input v-model.trim="cashierCustomerForm.numeroTarjeta" autocomplete="off" list="available-client-cards" required />
                <datalist id="available-client-cards">
                  <option v-for="card in availableClientCards" :key="card.id" :value="card.numeroTarjeta" />
                </datalist>
              </label>
              <label>
                NIP
                <input v-model.trim="cashierCustomerForm.nip" autocomplete="off" inputmode="numeric" maxlength="4" pattern="\d{4}" />
              </label>
              <label class="form-span-2">
                Observaciones
                <input v-model.trim="cashierCustomerForm.observaciones" autocomplete="off" />
              </label>
            </div>

            <p v-if="successMessage" class="success-line">{{ successMessage }}</p>
            <p v-if="errorMessage" class="error-line">{{ errorMessage }}</p>

            <div class="modal-footer">
              <button class="secondary-action" type="button" :disabled="loading" @click="closeCashierCustomerModal">
                <X :size="16" />
                Cancelar
              </button>
              <button class="primary-action" type="submit" :disabled="loading || !cashierSession">
                <Save :size="18" />
                {{ loading ? 'Registrando...' : 'Registrar cliente' }}
              </button>
            </div>
          </form>
        </div>

        <div v-if="cashierSaleModalOpen" class="modal-backdrop" role="presentation">
          <form
            class="editor-panel modal-panel cashier-sale-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="cashier-sale-modal-title"
            autocomplete="off"
            @submit.prevent="saveCashierSale"
          >
            <div class="panel-heading">
              <div>
                <p class="section-kicker">Venta</p>
                <h2 id="cashier-sale-modal-title">Registrar venta a tarjeta</h2>
              </div>
              <button class="icon-action" type="button" title="Cerrar formulario" @click="closeCashierSaleModal">
                <X :size="16" />
              </button>
            </div>

            <div class="form-grid">
              <label>
                Tarjeta
                <input v-model.trim="cashierSaleForm.numeroTarjeta" autocomplete="off" list="cashier-sale-client-cards" required />
                <datalist id="cashier-sale-client-cards">
                  <option v-for="card in saleClientCards" :key="card.id" :value="card.numeroTarjeta">
                    {{ card.estado }}
                  </option>
                </datalist>
              </label>
              <label>
                Monto venta
                <input
                  v-model.number="cashierSaleForm.monto"
                  type="number"
                  min="0.01"
                  step="0.01"
                  autocomplete="off"
                  :disabled="cashierSaleCardCaptured"
                  required
                />
              </label>
              <p v-if="cashierSaleCardCaptured" class="error-line form-grid-message">
                Tarjeta actualmente en uso
              </p>
              <label>
                Referencia
                <input v-model.trim="cashierSaleForm.referencia" autocomplete="off" placeholder="Ticket / folio" />
              </label>
              <label>
                Observaciones
                <input v-model.trim="cashierSaleForm.observaciones" autocomplete="off" />
              </label>
            </div>

            <section v-if="cashierSaleCardNumber && cashierSaleSelectedCard" class="query-summary-grid">
              <article>
                <span>Tarjeta</span>
                <strong>{{ cashierSaleSelectedCard.numeroTarjeta }}</strong>
                <small>{{ cardCustomerName(cashierSaleSelectedCard, cashierSaleSelectedCustomer) }}</small>
              </article>
              <article>
                <span>Origen</span>
                <strong>{{ cashierSaleSelectedCustomer ? customerFullName(cashierSaleSelectedCustomer) : 'Inventario operativo' }}</strong>
                <small>{{ cashierSaleSelectedCustomer?.telefono || cashierSaleSelectedCustomer?.email || cashierSaleSelectedCustomer?.documentoIdentidad || 'Universo total en BD' }}</small>
              </article>
              <article>
                <span>Saldo estimado</span>
                <strong>{{ formatMoney(cashierSaleCardBalance) }}</strong>
                <small>Ventas menos pagos registrados en caja</small>
              </article>
              <article>
                <span>Movimientos</span>
                <strong>{{ cashierSaleCardMovements.length }}</strong>
                <small>{{ cashierSaleSelectedCard.fechaVencimiento ? `Vence ${formatDate(cashierSaleSelectedCard.fechaVencimiento)}` : 'Sin vencimiento' }}</small>
              </article>
            </section>

            <div v-if="cashierSaleCardNumber && cashierSaleSelectedCard" class="table-panel config-table-panel query-movements-table">
              <table>
                <thead>
                  <tr>
                    <th>Fecha</th>
                    <th>Operacion</th>
                    <th>Monto</th>
                    <th>Referencia</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="movement in cashierSaleCardMovements" :key="`sale-card-${movement.id}`">
                    <td>{{ formatDateTime(movement.fechaMovimiento) }}</td>
                    <td>
                      <strong>{{ movement.tipo }}</strong>
                      <span>{{ movement.motivo || movement.observaciones || '--' }}</span>
                    </td>
                    <td>{{ formatMoney(movement.monto) }}</td>
                    <td>{{ movement.referencia || '--' }}</td>
                  </tr>
                  <tr v-if="cashierSaleCardMovements.length === 0">
                    <td colspan="4">Sin movimientos para esta tarjeta.</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <p v-if="successMessage" class="success-line">{{ successMessage }}</p>
            <p v-if="errorMessage" class="error-line">{{ errorMessage }}</p>

            <div class="modal-footer">
              <button class="secondary-action" type="button" :disabled="loading" @click="closeCashierSaleModal">
                <X :size="16" />
                Cancelar
              </button>
              <button class="primary-action" type="submit" :disabled="loading || !cashierSession || cashierSaleCardCaptured">
                <Save :size="18" />
                {{ loading ? 'Registrando...' : 'Registrar venta' }}
              </button>
            </div>
          </form>
        </div>

        <div v-if="cashierPayoutModalOpen" class="modal-backdrop" role="presentation">
          <form
            class="editor-panel modal-panel cashier-sale-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="cashier-payout-modal-title"
            autocomplete="off"
            @submit.prevent="saveCashierPayout"
          >
            <div class="panel-heading">
              <div>
                <p class="section-kicker">Pago</p>
                <h2 id="cashier-payout-modal-title">Pagar efectivo a cliente</h2>
              </div>
              <button class="icon-action" type="button" title="Cerrar formulario" @click="closeCashierPayoutModal">
                <X :size="16" />
              </button>
            </div>

            <div class="form-grid">
              <label>
                Tarjeta
                <input v-model.trim="cashierPayoutForm.numeroTarjeta" autocomplete="off" list="cashier-payout-client-cards" required />
                <datalist id="cashier-payout-client-cards">
                  <option v-for="card in saleClientCards" :key="card.id" :value="card.numeroTarjeta">
                    {{ card.estado }}
                  </option>
                </datalist>
              </label>
              <label>
                Monto solicitado
                <input
                  v-model.number="cashierPayoutForm.monto"
                  type="number"
                  min="0.01"
                  step="0.01"
                  autocomplete="off"
                  :max="cashierPayoutCardBalance || undefined"
                  :disabled="cashierPayoutCardCaptured"
                  required
                />
              </label>
              <p v-if="cashierPayoutCardCaptured" class="error-line form-grid-message">
                Tarjeta actualmente en uso
              </p>
              <label>
                Referencia
                <input v-model.trim="cashierPayoutForm.referencia" autocomplete="off" placeholder="Recibo / folio" />
              </label>
              <label>
                Observaciones
                <input v-model.trim="cashierPayoutForm.observaciones" autocomplete="off" />
              </label>
            </div>

            <section v-if="cashierPayoutCardNumber && cashierPayoutSelectedCard" class="query-summary-grid">
              <article>
                <span>Tarjeta</span>
                <strong>{{ cashierPayoutSelectedCard.numeroTarjeta }}</strong>
                <small>{{ cardCustomerName(cashierPayoutSelectedCard, cashierPayoutSelectedCustomer) }}</small>
              </article>
              <article>
                <span>Saldo disponible</span>
                <strong>{{ formatMoney(cashierPayoutCardBalance) }}</strong>
                <small>Disponible para pago parcial o total</small>
              </article>
              <article>
                <span>A entregar</span>
                <strong>{{ formatMoney(cashierPayoutRequestedAmount) }}</strong>
                <small>Efectivo solicitado por cliente</small>
              </article>
              <article>
                <span>Saldo a favor</span>
                <strong>{{ formatMoney(cashierPayoutRemainingBalance) }}</strong>
                <small>Permanece en la tarjeta</small>
              </article>
            </section>

            <div v-if="cashierPayoutCardNumber && cashierPayoutSelectedCard" class="table-panel config-table-panel query-movements-table">
              <table>
                <thead>
                  <tr>
                    <th>Fecha</th>
                    <th>Operacion</th>
                    <th>Monto</th>
                    <th>Referencia</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="movement in cashierPayoutCardMovements" :key="`payout-card-${movement.id}`">
                    <td>{{ formatDateTime(movement.fechaMovimiento) }}</td>
                    <td>
                      <strong>{{ movement.tipo }}</strong>
                      <span>{{ movement.motivo || movement.observaciones || '--' }}</span>
                    </td>
                    <td>{{ formatMoney(movement.monto) }}</td>
                    <td>{{ movement.referencia || '--' }}</td>
                  </tr>
                  <tr v-if="cashierPayoutCardMovements.length === 0">
                    <td colspan="4">Sin movimientos para esta tarjeta.</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <p v-if="successMessage" class="success-line">{{ successMessage }}</p>
            <p v-if="errorMessage" class="error-line">{{ errorMessage }}</p>

            <div class="modal-footer">
              <button class="secondary-action" type="button" :disabled="loading" @click="closeCashierPayoutModal">
                <X :size="16" />
                Cancelar
              </button>
              <button
                class="primary-action"
                type="submit"
                :disabled="loading || !cashierSession || cashierPayoutCardCaptured || cashierPayoutRequestedAmount <= 0 || cashierPayoutRequestedAmount > cashierPayoutCardBalance"
              >
                <Save :size="18" />
                {{ loading ? 'Registrando...' : 'Realizar pago' }}
              </button>
            </div>
          </form>
        </div>

        <div v-if="cashierCardQueryModalOpen" class="modal-backdrop" role="presentation">
          <section
            class="editor-panel modal-panel cashier-card-query-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="cashier-card-query-modal-title"
          >
            <div class="panel-heading">
              <div>
                <p class="section-kicker">Consulta</p>
                <h2 id="cashier-card-query-modal-title">Consulta de tarjeta</h2>
              </div>
              <button class="icon-action" type="button" title="Cerrar consulta" @click="closeCashierCardQueryModal">
                <X :size="16" />
              </button>
            </div>

            <div class="form-grid">
              <label>
                Numero de tarjeta
                <input v-model.trim="cashierCardQueryForm.numeroTarjeta" autocomplete="off" list="query-client-cards" autofocus />
                <datalist id="query-client-cards">
                  <option v-for="card in saleClientCards" :key="card.id" :value="card.numeroTarjeta">
                    {{ card.estado }}
                  </option>
                </datalist>
              </label>
              <div class="query-status-box">
                <span>Resultado</span>
                <strong>{{ queriedCardNumber ? queriedOperationalCard ? 'Tarjeta localizada' : 'Sin coincidencia' : 'Captura tarjeta' }}</strong>
              </div>
            </div>

            <section v-if="queriedCardNumber && queriedOperationalCard" class="query-summary-grid">
              <article>
                <span>Tarjeta</span>
                <strong>{{ queriedOperationalCard.numeroTarjeta }}</strong>
                <small>{{ cardCustomerName(queriedOperationalCard, queriedCustomer) }}</small>
              </article>
              <article>
                <span>Origen</span>
                <strong>{{ queriedCustomer ? customerFullName(queriedCustomer) : 'Inventario operativo' }}</strong>
                <small>{{ queriedCustomer?.telefono || queriedCustomer?.email || queriedCustomer?.documentoIdentidad || 'Universo total en BD' }}</small>
              </article>
              <article>
                <span>Saldo estimado</span>
                <strong>{{ formatMoney(queriedCardBalance) }}</strong>
                <small>Ventas menos pagos registrados en caja</small>
              </article>
              <article>
                <span>Movimientos</span>
                <strong>{{ queriedCardMovements.length }}</strong>
                <small>{{ queriedOperationalCard.fechaVencimiento ? `Vence ${formatDate(queriedOperationalCard.fechaVencimiento)}` : 'Sin vencimiento' }}</small>
              </article>
            </section>

            <div v-if="queriedCardNumber && queriedOperationalCard" class="table-panel config-table-panel query-movements-table">
              <table>
                <thead>
                  <tr>
                    <th>Fecha</th>
                    <th>Operacion</th>
                    <th>Monto</th>
                    <th>Referencia</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="movement in queriedCardMovements" :key="`query-card-${movement.id}`">
                    <td>{{ formatDateTime(movement.fechaMovimiento) }}</td>
                    <td>
                      <strong>{{ movement.tipo }}</strong>
                      <span>{{ movement.motivo || movement.observaciones || '--' }}</span>
                    </td>
                    <td>{{ formatMoney(movement.monto) }}</td>
                    <td>{{ movement.referencia || '--' }}</td>
                  </tr>
                  <tr v-if="queriedCardMovements.length === 0">
                    <td colspan="4">Sin movimientos para esta tarjeta.</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <p v-if="queriedCardNumber && !queriedOperationalCard" class="error-line">
              No se encontro la tarjeta en el inventario operativo.
            </p>

            <div class="modal-footer">
              <button class="secondary-action" type="button" @click="closeCashierCardQueryModal">
                <X :size="16" />
                Cerrar
              </button>
            </div>
          </section>
        </div>

        <div v-if="cashierCloseModalOpen" class="modal-backdrop" role="presentation">
          <form
            class="editor-panel modal-panel cashier-close-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="cashier-close-modal-title"
            autocomplete="off"
            @submit.prevent="saveCashierCloseAction"
          >
            <div class="panel-heading">
              <div>
                <p class="section-kicker">Cierre</p>
                <h2 id="cashier-close-modal-title">
                  {{ cashierCloseMode === 'preclose' ? 'Precierre de caja' : 'Cierre de caja' }}
                </h2>
              </div>
              <button class="icon-action" type="button" title="Cerrar formulario" @click="closeCashierCloseModal">
                <X :size="16" />
              </button>
            </div>

            <section class="query-summary-grid">
              <article>
                <span>Estado caja</span>
                <strong>{{ cashierSession?.estado ?? 'Sin caja' }}</strong>
                <small>{{ cashierSession?.estacionNombre ?? currentCashierAssignment?.estacionNombre ?? '--' }}</small>
              </article>
              <article>
                <span>Saldo sistema</span>
                <strong>{{ formatMoney(cashierSession?.saldoActual ?? 0) }}</strong>
                <small>{{ cashierSessionMovements.length }} movimiento(s)</small>
              </article>
              <article>
                <span>Ventas</span>
                <strong>{{ formatMoney(cashierCloseSalesTotal) }}</strong>
                <small>Ingresos registrados</small>
              </article>
              <article>
                <span>X pagar</span>
                <strong>{{ formatMoney(cashierClosePendingPayoutTotal) }}</strong>
                <small>Cashout EGM pendiente de entregar</small>
              </article>
              <article>
                <span>Pagos</span>
                <strong>{{ formatMoney(cashierCloseCashPayoutTotal) }}</strong>
                <small>Efectivo entregado a clientes</small>
              </article>
              <article>
                <span>Tarjetas activas</span>
                <strong>{{ cashierCustomers.length }}</strong>
                <small>{{ cashierCapturedCardsCount }} capturada(s) por EGM</small>
              </article>
              <article>
                <span>Diferencia</span>
                <strong>{{ formatMoney(cashierCloseDifference) }}</strong>
                <small>Declarado menos sistema</small>
              </article>
            </section>

            <div class="form-grid">
              <label>
                Monto declarado
                <input
                  v-model.number="cashierCloseForm.montoDeclarado"
                  type="number"
                  min="0"
                  step="0.01"
                  autocomplete="off"
                  :disabled="cashierCloseMode === 'preclose'"
                  required
                />
              </label>
              <label>
                Tarjetas devueltas
                <input
                  v-model.number="cashierCloseForm.tarjetasDevueltas"
                  type="number"
                  min="0"
                  step="1"
                  autocomplete="off"
                  :disabled="cashierCloseMode === 'preclose'"
                  required
                />
              </label>
              <label class="form-span-2">
                Observaciones
                <input v-model.trim="cashierCloseForm.observaciones" autocomplete="off" />
              </label>
            </div>

            <p v-if="cashierCapturedCardsCount > 0" class="error-line">
              No se puede cerrar caja: hay tarjetas capturadas por EGM.
            </p>
            <p v-if="successMessage" class="success-line">{{ successMessage }}</p>
            <p v-if="errorMessage" class="error-line">{{ errorMessage }}</p>

            <div class="modal-footer">
              <button class="secondary-action" type="button" :disabled="loading" @click="closeCashierCloseModal">
                <X :size="16" />
                Cancelar
              </button>
              <button
                class="primary-action"
                type="submit"
                :disabled="loading || !cashierSession || cashierCapturedCardsCount > 0"
              >
                <Save :size="18" />
                {{ loading ? 'Procesando...' : cashierCloseMode === 'preclose' ? 'Realizar precierre' : 'Cerrar caja' }}
              </button>
            </div>
          </form>
        </div>
      </section>
    </section>
  </main>
</template>

<script setup lang="ts">
import {
  ChevronDown,
  ClipboardList,
  CreditCard,
  KeyRound,
  LogIn,
  LogOut,
  Menu,
  Pencil,
  PlayCircle,
  RefreshCw,
  Save,
  Search,
  ShieldCheck,
  Trash2,
  UserPlus,
  Users,
  X
} from 'lucide-vue-next'
import { computed, onMounted, reactive, ref } from 'vue'
import {
  changeOwnPassword,
  closeCashier,
  closeOperationalDay,
  closeTreasury,
  getCashierConsole,
  createCashierPayout,
  createCashierSale,
  createCashierShift,
  createOperationSchedule,
  createOperationalAssignment,
  createOperationalCard,
  createOperationalCardRange,
  createTreasuryMovement,
  createWorkstation,
  createUser,
  deleteCashierShift,
  deleteOperationSchedule,
  deleteOperationalAssignment,
  deleteOperationalCard,
  deleteWorkstation,
  deleteUser,
  getCashierShifts,
  getCurrentEgmSnapshots,
  getCurrentOperationalDay,
  getOperationSchedules,
  getOperationalAssignments,
  getOperationalAudit,
  getOperationalCards,
  getOperationalClose,
  getRoles,
  getTreasuryConsole,
  getTreasuryLedger,
  getTreasuryMovements,
  getUser,
  getUsers,
  getWorkstations,
  hasToken,
  login,
  logout,
  me,
  openOperationalDay,
  openCashier,
  openTreasury,
  precloseCashier,
  precloseTreasury,
  receiveTreasuryCardReturn,
  receiveTreasuryCashReturn,
  registerCashierCustomer,
  sendTreasuryBoxFund,
  testEgmSweep,
  deliverTreasuryCardRange,
  updateCashierShift,
  updateOperationSchedule,
  updateOperationalAssignment,
  updateOperationalCard,
  updateWorkstation,
  updateUser,
  updateUserPassword
} from './services/api'
import type {
  CashierConsole,
  CashierMovement,
  CashierSession,
  CashierShift,
  CustomerRegistration,
  LoginResponse,
  OperationSchedule,
  OperationalAssignment,
  OperationalAuditEvent,
  OperationalCard,
  OperationalCloseSummary,
  OperationalDay,
  EgmMeterSnapshot,
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
} from './types/security'

const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const session = ref<LoginResponse | null>(null)
const users = ref<UserSummary[]>([])
const roles = ref<Role[]>([])
const selectedUser = ref<UserDetail | null>(null)
const editorMode = ref<'none' | 'create' | 'edit'>('none')
const search = ref('')
const cardSearch = ref('')
const passwordReset = ref('')
const supervisorMenuOpen = ref(true)
const openSupervisorSections = ref<string[]>(['Configuracion Operativa', 'Operacion de Jornada'])
const activeSupervisorOption = ref('Configuracion Operativa')
const operationSchedules = ref<OperationSchedule[]>([])
const cashierShifts = ref<CashierShift[]>([])
const workstations = ref<Workstation[]>([])
const operationalAssignments = ref<OperationalAssignment[]>([])
const operationalCards = ref<OperationalCard[]>([])
const operationalAudit = ref<OperationalAuditEvent[]>([])
const currentOperationalDay = ref<OperationalDay | null>(null)
const operationalClose = ref<OperationalCloseSummary | null>(null)
const currentEgmSnapshots = ref<EgmMeterSnapshot[]>([])
const manualEgmSweepSnapshots = ref<EgmMeterSnapshot[]>([])
const treasuryConsole = ref<TreasuryConsole | null>(null)
const treasurySession = ref<TreasurySession | null>(null)
const treasuryMovements = ref<TreasuryMovement[]>([])
const treasuryMovementLog = ref<TreasuryMovement[]>([])
const treasuryCardMovements = ref<TreasuryCardMovement[]>([])
const treasuryLedger = ref<TreasuryLedgerEntry[]>([])
const cashierConsole = ref<CashierConsole | null>(null)
const cashierSession = ref<CashierSession | null>(null)
const cashierSessionMovements = ref<CashierMovement[]>([])
const cardMovements = ref<CashierMovement[]>([])
const cashierCustomers = ref<CustomerRegistration[]>([])
const configEditor = ref<'none' | 'schedule' | 'shift' | 'workstation' | 'assignment' | 'card' | 'cardRange'>('none')
const treasuryEditor = ref<'none' | 'open' | 'movement' | 'fund' | 'cashReturn' | 'cardDelivery' | 'cardReturn' | 'preclose' | 'close'>('none')
const cashierCustomerModalOpen = ref(false)
const cashierSaleModalOpen = ref(false)
const cashierPayoutModalOpen = ref(false)
const cashierCardQueryModalOpen = ref(false)
const cashierCloseModalOpen = ref(false)
const cashierCloseMode = ref<'preclose' | 'close'>('close')
const selectedConfigId = ref<number | null>(null)

const supervisorMenu = [
  {
    title: 'Configuracion Operativa',
    items: [
      'Configuracion Operativa',
      'Horario de Operacion',
      'Turnos de Caja',
      'Cajas y Tesorerias',
      'Asignacion de Usuarios',
      'Definir Tarjetas'
    ]
  },
  {
    title: 'Operacion de Jornada',
    items: [
      'Inicio de Operaciones',
      'Apertura de Tesoreria',
      'Entrada y Salida de Tesoreria',
      'Apertura de Caja',
      'Precierre de Tesoreria',
      'Cierre de Tesoreria',
      'Cierre de Operaciones',
      'Reversion de Caja'
    ]
  },
  {
    title: 'Consultas',
    items: ['Consulta de Tarjeta', 'Consulta de Maquinas', 'Cierres de Operacion']
  },
  {
    title: 'Reportes',
    items: ['Estado de cuenta de operacion', 'Reporte Operaciones de Caja', 'Reporte Estado de Cajas']
  }
]

const treasuryMenu = [
  {
    title: 'Operacion de Tesoreria',
    items: [
      'Apertura de Tesoreria',
      'Entrada y Salida de Tesoreria',
      'Precierre de Tesoreria',
      'Cierre de Tesoreria'
    ]
  },
  {
    title: 'Consultas',
    items: ['Movimientos de Tesoreria', 'Bitacora de Tesoreria']
  }
]

const cashierMenu = [
  {
    title: 'Operacion de Caja',
    items: ['Apertura de Caja', 'Alta de Cliente', 'Venta', 'Pago', 'Pago Manual', 'Transaccion Especial']
  },
  {
    title: 'Beneficios',
    items: ['Cortesias', 'Promocionales']
  },
  {
    title: 'Consultas',
    items: ['Consulta de Tarjeta', 'Reimpresion de Comprobantes']
  },
  {
    title: 'Cierre',
    items: ['Precierre de Caja', 'Cierre de Caja']
  }
]

const loginForm = reactive({
  username: 'Excalibur',
  password: ''
})

const form = reactive<UserForm>({
  username: '',
  email: '',
  telefono: '',
  nombre: '',
  apellidoPaterno: '',
  apellidoMaterno: '',
  password: '',
  activo: true,
  bloqueado: false,
  motivoBloqueo: '',
  requiereCambioPassword: true,
  roles: ['ADMIN']
})

const operationScheduleForm = reactive({
  nombre: '',
  horaInicio: '09:00',
  horaFin: '23:59',
  activo: true
})

const cashierShiftForm = reactive({
  nombre: '',
  horaInicio: '09:00',
  horaFin: '17:00',
  activo: true
})

const workstationForm = reactive({
  nombre: '',
  tipo: 'CAJA' as 'CAJA' | 'TESORERIA',
  cajaId: null as number | null,
  sala: 'Play Valley',
  ubicacion: '',
  activa: true
})

const assignmentForm = reactive({
  usuarioId: 0,
  estacionId: 0,
  turnoId: 0,
  fechaOperacion: todayDisplayDate(),
  rolOperativo: 'SUPERVISOR' as 'SUPERVISOR' | 'TESORERO' | 'CAJERO',
  activa: true
})

const cardRangeForm = reactive({
  numeroInicial: '',
  numeroFinal: '',
  tipo: 'CLIENTE' as 'CLIENTE' | 'GENERICA',
  fechaVencimiento: '',
  maximoTarjetas: 500
})

const cardForm = reactive({
  numeroTarjeta: '',
  tipo: 'CLIENTE' as 'CLIENTE' | 'GENERICA',
  fechaVencimiento: '',
  estado: 'DISPONIBLE'
})

const treasuryOpenForm = reactive({
  estacionId: 0,
  saldoInicial: 0,
  observaciones: ''
})

const treasuryMovementForm = reactive({
  tipo: 'ENTRADA' as 'ENTRADA' | 'SALIDA',
  concepto: '',
  monto: 0,
  estacionCajaId: 0,
  turnoId: 0,
  referencia: '',
  observaciones: ''
})

const treasuryCardForm = reactive({
  estacionCajaId: 0,
  turnoId: 0,
  numeroInicial: '',
  numeroFinal: '',
  referencia: '',
  observaciones: ''
})

const treasuryCloseForm = reactive({
  observaciones: ''
})

const cashierCustomerForm = reactive({
  nombre: '',
  apellidoPaterno: '',
  apellidoMaterno: '',
  telefono: '',
  email: '',
  fechaNacimiento: '',
  documentoIdentidad: '',
  numeroTarjeta: '',
  nip: '',
  observaciones: ''
})

const cashierSaleForm = reactive({
  numeroTarjeta: '',
  monto: 0,
  referencia: '',
  observaciones: ''
})

const cashierPayoutForm = reactive({
  numeroTarjeta: '',
  monto: 0,
  referencia: '',
  observaciones: ''
})

const cashierCardQueryForm = reactive({
  numeroTarjeta: ''
})

const cashierCloseForm = reactive({
  montoDeclarado: 0,
  tarjetasDevueltas: 0,
  observaciones: ''
})

const operationalCloseForm = reactive({
  observaciones: '',
  forzarConDiferencias: false
})

const activeUsers = computed(() => users.value.filter((user) => user.activo && !user.eliminado).length)
const blockedUsers = computed(() => users.value.filter((user) => user.bloqueado).length)
const availableCards = computed(() => operationalCards.value.filter((card) => card.estado === 'DISPONIBLE').length)
const assignedCards = computed(() => operationalCards.value.filter((card) => card.estado === 'ASIGNADA').length)
const unavailableCards = computed(() =>
  operationalCards.value.filter((card) => ['BLOQUEADA', 'VENCIDA', 'INACTIVA'].includes(card.estado)).length
)
const hasOperationalRole = computed(() => hasAnyRole(['SUPERVISOR', 'TESORERO', 'CAJERO']))
const isAdminDashboard = computed(() => hasAnyRole(['ADMIN']) && !hasOperationalRole.value)
const isTreasuryDashboard = computed(() =>
  hasAnyRole(['TESORERO']) && !hasAnyRole(['SUPERVISOR'])
)
const isCashierDashboard = computed(() =>
  !isTreasuryDashboard.value && hasAnyRole(['CAJERO']) && !hasAnyRole(['SUPERVISOR'])
)
const operatorMenu = computed(() => {
  if (isTreasuryDashboard.value) return treasuryMenu
  if (isCashierDashboard.value) return cashierMenu
  return supervisorMenu
})
const operatorMenuLabel = computed(() => {
  if (isTreasuryDashboard.value) return 'Menu tesoreria'
  if (isCashierDashboard.value) return 'Menu cajero'
  return 'Menu supervisor'
})
const operatorHomeLabel = computed(() => {
  if (isTreasuryDashboard.value) return 'Consola'
  if (isCashierDashboard.value) return 'Caja'
  return 'Inicio'
})
const operatorRoleLabel = computed(() => {
  if (isTreasuryDashboard.value) return 'Tesorero'
  if (isCashierDashboard.value) return 'Cajero'
  return 'Supervisor'
})
const isOperationalConfigOption = computed(() =>
  [
    'Configuracion Operativa',
    'Horario de Operacion',
    'Turnos de Caja',
    'Cajas y Tesorerias',
    'Asignacion de Usuarios',
    'Definir Tarjetas'
  ].includes(activeSupervisorOption.value)
)
const isTreasuryOption = computed(() =>
  [
    'Apertura de Tesoreria',
    'Entrada y Salida de Tesoreria',
    'Precierre de Tesoreria',
    'Cierre de Tesoreria',
    'Movimientos de Tesoreria',
    'Bitacora de Tesoreria'
  ].includes(activeSupervisorOption.value)
)
const isOperationalCloseOption = computed(() =>
  ['Cierre de Operaciones', 'Cierres de Operacion'].includes(activeSupervisorOption.value)
)
const isOperationalStatementOption = computed(() => activeSupervisorOption.value === 'Estado de cuenta de operacion')
const operationalCloseReconciliations = computed(() => operationalClose.value?.conciliaciones ?? [])
const operationalCloseSnapshots = computed(() => operationalClose.value?.snapshots ?? [])
const operationStatementCashiers = computed(() => operationalClose.value?.cajasCerradas ?? [])
const operationStatementInitialCash = computed(() =>
  operationStatementCashiers.value.reduce((total, cashier) => total + Number(cashier.saldoInicial || 0), 0)
)
const operationStatementDeclaredCash = computed(() =>
  operationStatementCashiers.value.reduce(
    (total, cashier) => total + Number(cashier.montoDeclaradoCierre ?? cashier.saldoActual ?? 0),
    0
  )
)
const operationStatementCashDelta = computed(() => operationStatementDeclaredCash.value - operationStatementInitialCash.value)
const displayedEgmSnapshots = computed(() =>
  currentOperationalDay.value && currentEgmSnapshots.value.length > 0
    ? currentEgmSnapshots.value
    : operationalCloseSnapshots.value.length > 0
      ? operationalCloseSnapshots.value
      : manualEgmSweepSnapshots.value
)
const treasuryStations = computed(() => workstations.value.filter((station) => station.tipo === 'TESORERIA' && station.activa))
const cashierStations = computed(() => workstations.value.filter((station) => station.tipo === 'CAJA' && station.activa))
const treasuryAssignments = computed(() =>
  treasuryConsole.value?.asignaciones.filter((assignment) => assignment.rolOperativo === 'TESORERO') ?? []
)
const cashierAssignments = computed(() =>
  treasuryConsole.value?.asignaciones.filter((assignment) => assignment.rolOperativo === 'CAJERO') ?? []
)
const treasuryClosedCashiers = computed(() => treasuryConsole.value?.cajasCerradas ?? [])
const treasurySelectedClosedCashier = computed(() =>
  treasuryClosedCashiers.value.find((cashier) =>
    cashier.estacionId === treasuryMovementForm.estacionCajaId
      && (!treasuryMovementForm.turnoId || cashier.turnoId === treasuryMovementForm.turnoId)
  ) ?? null
)
const treasurySelectedCashReturned = computed(() =>
  treasuryMovements.value
    .filter((movement) =>
      movement.tipo === 'DEVOLUCION_CAJA'
        && movement.estacionCajaId === treasuryMovementForm.estacionCajaId
        && (!treasuryMovementForm.turnoId || movement.turnoId === treasuryMovementForm.turnoId)
    )
    .reduce((total, movement) => total + Number(movement.monto || 0), 0)
)
const treasuryExpectedCashReturn = computed(() => {
  const closed = treasurySelectedClosedCashier.value
  if (!closed) return 0
  const declared = Number(closed.montoDeclaradoCierre ?? closed.saldoActual ?? 0)
  return Math.max(0, declared - treasurySelectedCashReturned.value)
})
const currentCashierAssignment = computed(() =>
  cashierConsole.value?.asignacion
    ?? cashierAssignments.value.find((assignment) => assignment.usuarioId === session.value?.userId)
    ?? cashierAssignments.value[0]
    ?? null
)
const currentCashierStationId = computed(() => currentCashierAssignment.value?.estacionId ?? null)
const cashierTreasuryMovements = computed(() => cashierConsole.value?.fondosTesoreria ?? [])
const cashierFundMovements = computed(() => cashierMovements.value.filter((movement) => movement.tipo === 'FONDO_CAJA'))
const cashierMovements = computed(() => cashierTreasuryMovements.value)
const cashierCashReturnMovements = computed(() => cashierTreasuryMovements.value.filter((movement) => movement.tipo === 'DEVOLUCION_CAJA'))
const cashierFundsReceived = computed(() => sumMoney(cashierFundMovements.value))
const cashierCashReturned = computed(() => sumMoney(cashierCashReturnMovements.value))
const cashierBoxLogMovements = computed(() => {
  const treasuryEntries = cashierFundMovements.value.map((movement) => ({
    key: `cashier-money-${movement.id}`,
    fechaMovimiento: movement.fechaMovimiento,
    tipo: movement.tipo,
    detalle: movement.concepto,
    subdetalle: movement.turnoNombre || currentCashierAssignment.value?.turnoNombre || '',
    monto: movement.monto,
    referencia: movement.referencia,
    observaciones: movement.observaciones
  }))
  const cashierEntries = cashierSessionMovements.value.map((movement) => ({
    key: `cashier-session-${movement.id}`,
    fechaMovimiento: movement.fechaMovimiento,
    tipo: cashierMovementDisplayType(movement),
    detalle: movement.numeroTarjeta || movement.motivo || 'Movimiento de caja',
    subdetalle: movement.maquina || movement.observaciones || '',
    monto: movement.monto,
    referencia: movement.referencia,
    observaciones: movement.observaciones
  }))
  return [...treasuryEntries, ...cashierEntries].sort(
    (left, right) => new Date(right.fechaMovimiento).getTime() - new Date(left.fechaMovimiento).getTime()
  )
})
const cashierCloseSalesTotal = computed(() =>
  cashierSessionMovements.value
    .filter((movement) => movement.tipo === 'VENTA')
    .reduce((total, movement) => total + Number(movement.monto || 0), 0)
)
const cashierClosePendingPayoutTotal = computed(() =>
  Math.max(0, cashierSessionMovements.value
    .filter((movement) => movement.tipo === 'PAGO' && movement.motivo === 'Cobro RFID desde EGM')
    .reduce((total, movement) => total + Number(movement.monto || 0), 0)
    - cashierCloseCashPayoutTotal.value
  )
)
const cashierCloseCashPayoutTotal = computed(() =>
  cashierSessionMovements.value
    .filter((movement) => movement.tipo === 'PAGO' && movement.motivo === 'Pago efectivo a cliente')
    .reduce((total, movement) => total + Number(movement.monto || 0), 0)
)
const cashierCapturedCardsCount = computed(() =>
  cashierCustomers.value.filter((customer) => customer.estadoTarjeta === 'CAPTURADA_EGM').length
)
const cashierCloseDifference = computed(() =>
  Number(cashierCloseForm.montoDeclarado || 0) - Number(cashierSession.value?.saldoActual || 0)
)
const availableClientCards = computed(() =>
  operationalCards.value.filter((card) => card.tipo === 'CLIENTE' && card.estado === 'DISPONIBLE')
)
const saleClientCards = computed(() =>
  operationalCards.value.filter((card) => card.tipo === 'CLIENTE' && ['DISPONIBLE', 'ASIGNADA', 'CAPTURADA_EGM'].includes(card.estado))
)
const cashierSaleCardNumber = computed(() => cashierSaleForm.numeroTarjeta.trim())
const cashierSaleSelectedCustomer = computed(() =>
  cashierCustomers.value.find((customer) => customer.numeroTarjeta === cashierSaleCardNumber.value) ?? null
)
const cashierSaleSelectedCard = computed(() =>
  operationalCards.value.find((card) => card.numeroTarjeta === cashierSaleCardNumber.value) ?? null
)
const cashierSaleCardCaptured = computed(() =>
  Boolean(
    cashierSaleSelectedCard.value?.capturadaEgm ||
      cashierSaleSelectedCard.value?.estado === 'CAPTURADA_EGM' ||
      cashierSaleSelectedCustomer.value?.estadoTarjeta === 'CAPTURADA_EGM'
  )
)
const cashierSaleCardMovements = computed(() =>
  cardMovements.value
    .filter((movement) => movement.numeroTarjeta === cashierSaleCardNumber.value)
    .slice()
    .sort((left, right) => new Date(right.fechaMovimiento).getTime() - new Date(left.fechaMovimiento).getTime())
)
const cashierSaleCardBalance = computed(() =>
  cashierSaleCardMovements.value.reduce((total, movement) => total + cashierMovementBalanceImpact(movement), 0)
)
const cashierPayoutCardNumber = computed(() => cashierPayoutForm.numeroTarjeta.trim())
const cashierPayoutSelectedCustomer = computed(() =>
  cashierCustomers.value.find((customer) => customer.numeroTarjeta === cashierPayoutCardNumber.value) ?? null
)
const cashierPayoutSelectedCard = computed(() =>
  operationalCards.value.find((card) => card.numeroTarjeta === cashierPayoutCardNumber.value) ?? null
)
const cashierPayoutCardCaptured = computed(() =>
  Boolean(
    cashierPayoutSelectedCard.value?.capturadaEgm ||
      cashierPayoutSelectedCard.value?.estado === 'CAPTURADA_EGM' ||
      cashierPayoutSelectedCustomer.value?.estadoTarjeta === 'CAPTURADA_EGM'
  )
)
const cashierPayoutCardMovements = computed(() =>
  cardMovements.value
    .filter((movement) => movement.numeroTarjeta === cashierPayoutCardNumber.value)
    .slice()
    .sort((left, right) => new Date(right.fechaMovimiento).getTime() - new Date(left.fechaMovimiento).getTime())
)
const cashierPayoutCardBalance = computed(() =>
  cashierPayoutCardMovements.value.reduce((total, movement) => total + cashierMovementBalanceImpact(movement), 0)
)
const cashierPayoutRequestedAmount = computed(() =>
  Math.max(0, Number(cashierPayoutForm.monto || 0))
)
const cashierPayoutRemainingBalance = computed(() =>
  Math.max(0, cashierPayoutCardBalance.value - cashierPayoutRequestedAmount.value)
)
const queriedCardNumber = computed(() => cashierCardQueryForm.numeroTarjeta.trim())
const queriedOperationalCard = computed(() =>
  operationalCards.value.find((card) => card.numeroTarjeta === queriedCardNumber.value) ?? null
)
const queriedCustomer = computed(() =>
  cashierCustomers.value.find((customer) => customer.numeroTarjeta === queriedCardNumber.value) ?? null
)
const queriedCardMovements = computed(() =>
  cardMovements.value
    .filter((movement) => movement.numeroTarjeta === queriedCardNumber.value)
    .slice()
    .sort((left, right) => new Date(right.fechaMovimiento).getTime() - new Date(left.fechaMovimiento).getTime())
)
const queriedCardBalance = computed(() =>
  queriedCardMovements.value.reduce((total, movement) => total + cashierMovementBalanceImpact(movement), 0)
)
const cashierActions = computed(() => [
  {
    title: 'Apertura de Caja',
    menu: 'Apertura de Caja',
    kind: 'money',
    status: cashierSession.value ? 'Abierta' : cashierFundsReceived.value > 0 ? 'Listo' : 'Pendiente',
    description: 'Validar cajero, caja, turno y monto fisico recibido desde tesoreria.'
  },
  {
    title: 'Alta de Cliente',
    menu: 'Alta de Cliente',
    kind: 'card',
    status: cashierSession.value ? `${availableClientCards.value.length} disp.` : 'Abrir caja',
    description: 'Registra cliente nuevo y asigna una tarjeta disponible del inventario global.'
  },
  {
    title: 'Venta',
    menu: 'Venta',
    kind: 'card',
    status: 'Preparado',
    description: 'Captura de tarjeta e importe para convertir efectivo a creditos de juego.'
  },
  {
    title: 'Pago',
    menu: 'Pago',
    kind: 'money',
    status: 'Preparado',
    description: 'Consulta saldo de tarjeta y calcula efectivo a pagar al cliente.'
  },
  {
    title: 'Pago Manual',
    menu: 'Pago Manual',
    kind: 'money',
    status: 'Preparado',
    description: 'Registro de pago manual por jackpot, falla o evento liberado desde maquina.'
  },
  {
    title: 'Cortesias y Promocionales',
    menu: 'Cortesias',
    kind: 'card',
    status: 'Preparado',
    description: 'Entrega de creditos redimibles o no redimibles con motivo y comprobante.'
  },
  {
    title: 'Consulta de Tarjeta',
    menu: 'Consulta de Tarjeta',
    kind: 'search',
    status: 'Preparado',
    description: 'Busqueda por tarjeta para saldo, movimientos actuales e historicos.'
  },
  {
    title: 'Precierre de Caja',
    menu: 'Precierre de Caja',
    kind: 'money',
    status: 'Preparado',
    description: 'Validacion previa del cajero, caja y turno antes del cierre definitivo.'
  },
  {
    title: 'Cierre de Caja',
    menu: 'Cierre de Caja',
    kind: 'money',
    status: 'Preparado',
    description: 'Conciliacion final de monto operado, movimientos y comprobantes.'
  }
])
const configEditorTitle = computed(() => {
  const titles = {
    none: '',
    schedule: 'Horario de Operacion',
    shift: 'Turno de Caja',
    workstation: 'Caja/Tesoreria',
    assignment: 'Asignacion de Usuario',
    card: 'Tarjeta',
    cardRange: 'Alta de Tarjetas por Rango'
  }
  return titles[configEditor.value]
})

onMounted(async () => {
  if (hasToken()) {
    try {
      session.value = await me()
      await bootstrap()
    } catch {
      session.value = null
    }
  }
})

async function handleLogin() {
  await withLoading(async () => {
    session.value = await login(loginForm.username, loginForm.password)
    await bootstrap()
  })
}

async function bootstrap() {
  configureDashboardForRole()
  if (isAdminDashboard.value) {
    await Promise.all([loadRoles(), loadUsers()])
  } else {
    await loadOperationalConfig()
  }
  closeEditor()
}

function configureDashboardForRole() {
  if (isTreasuryDashboard.value) {
    openSupervisorSections.value = ['Operacion de Tesoreria']
    if (!isTreasuryOption.value) {
      activeSupervisorOption.value = 'Apertura de Tesoreria'
    }
    return
  }
  if (isCashierDashboard.value) {
    openSupervisorSections.value = ['Operacion de Caja']
    if (!operatorMenu.value.some((section) => section.items.includes(activeSupervisorOption.value))) {
      activeSupervisorOption.value = 'Apertura de Caja'
    }
    return
  }
  if (!isAdminDashboard.value && !operatorMenu.value.some((section) => section.items.includes(activeSupervisorOption.value))) {
    activeSupervisorOption.value = 'Configuracion Operativa'
  }
}

async function loadUsers() {
  users.value = await getUsers(search.value)
}

async function loadRoles() {
  roles.value = await getRoles()
}

async function loadOperationalConfig() {
  const [
    schedules,
    shifts,
    stations,
    assignments,
    cards,
    audit,
    operationalUsers,
    openDay,
    egmSnapshots,
    closeSummary,
    treasury,
    treasuryMovementList,
    treasuryLedgerList,
    cashier
  ] = await Promise.all([
    getOperationSchedules(),
    getCashierShifts(),
    getWorkstations(),
    getOperationalAssignments(),
    getOperationalCards(cardSearch.value),
    getOperationalAudit(),
    getUsers(),
    getCurrentOperationalDay(),
    getCurrentEgmSnapshots(),
    getOperationalClose(),
    getTreasuryConsole(),
    getTreasuryMovements(),
    getTreasuryLedger(),
    getCashierConsole()
  ])
  operationSchedules.value = schedules
  cashierShifts.value = shifts
  workstations.value = stations
  operationalAssignments.value = assignments
  operationalCards.value = cards
  operationalAudit.value = audit
  users.value = operationalUsers
  currentOperationalDay.value = openDay
  currentEgmSnapshots.value = egmSnapshots
  operationalClose.value = openDay && closeSummary?.jornada.id !== openDay.id ? null : closeSummary
  treasuryConsole.value = treasury
  treasurySession.value = treasury.tesoreria
  treasuryMovements.value = treasury.movimientos
  treasuryMovementLog.value = treasuryMovementList
  treasuryCardMovements.value = treasury.movimientosTarjetas
  treasuryLedger.value = treasuryLedgerList
  cashierConsole.value = cashier
  cashierSession.value = cashier.caja
  cashierSessionMovements.value = cashier.movimientos
  cardMovements.value = cashier.movimientosTarjeta
  cashierCustomers.value = cashier.clientes
}

async function openCashierFromDashboard() {
  if (!currentCashierAssignment.value) {
    errorMessage.value = 'No hay asignacion CAJERO activa para esta jornada'
    return
  }
  if (cashierFundsReceived.value <= 0) {
    errorMessage.value = 'No hay fondo inicial enviado por tesoreria'
    return
  }
  await withLoading(async () => {
    cashierSession.value = await openCashier({
      estacionId: currentCashierAssignment.value!.estacionId,
      turnoId: currentCashierAssignment.value!.turnoId,
      montoApertura: cashierFundsReceived.value - cashierCashReturned.value,
      tarjetasApertura: 0,
      observaciones: 'Apertura de caja desde dashboard CAJERO'
    })
    await loadOperationalConfig()
    successMessage.value = 'Caja aperturada correctamente'
  })
}

function selectCashierAction(menu: string) {
  activeSupervisorOption.value = menu
  if (menu === 'Alta de Cliente') {
    openCashierCustomerModal()
  } else if (menu === 'Venta') {
    openCashierSaleModal()
  } else if (menu === 'Pago') {
    openCashierPayoutModal()
  } else if (menu === 'Consulta de Tarjeta') {
    openCashierCardQueryModal()
  } else if (menu === 'Precierre de Caja') {
    openCashierCloseModal('preclose')
  } else if (menu === 'Cierre de Caja') {
    openCashierCloseModal('close')
  }
}

function selectOperatorMenuItem(item: string) {
  if (isCashierDashboard.value && ['Alta de Cliente', 'Venta', 'Pago', 'Consulta de Tarjeta', 'Precierre de Caja', 'Cierre de Caja'].includes(item)) {
    selectCashierAction(item)
    return
  }
  activeSupervisorOption.value = item
}

function openCashierCustomerModal() {
  activeSupervisorOption.value = 'Alta de Cliente'
  resetCashierCustomerForm()
  errorMessage.value = ''
  successMessage.value = ''
  cashierCustomerModalOpen.value = true
}

function closeCashierCustomerModal() {
  cashierCustomerModalOpen.value = false
  resetCashierCustomerForm()
  errorMessage.value = ''
}

function openCashierSaleModal() {
  activeSupervisorOption.value = 'Venta'
  resetCashierSaleForm()
  errorMessage.value = ''
  successMessage.value = ''
  cashierSaleModalOpen.value = true
}

function closeCashierSaleModal() {
  cashierSaleModalOpen.value = false
  resetCashierSaleForm()
  errorMessage.value = ''
}

async function openCashierPayoutModal() {
  activeSupervisorOption.value = 'Pago'
  resetCashierPayoutForm()
  errorMessage.value = ''
  successMessage.value = ''
  cashierPayoutModalOpen.value = true
  await withLoading(async () => {
    await loadOperationalConfig()
  })
}

function closeCashierPayoutModal() {
  cashierPayoutModalOpen.value = false
  resetCashierPayoutForm()
  errorMessage.value = ''
}

async function openCashierCardQueryModal() {
  activeSupervisorOption.value = 'Consulta de Tarjeta'
  resetCashierCardQueryForm()
  errorMessage.value = ''
  successMessage.value = ''
  cashierCardQueryModalOpen.value = true
  await withLoading(async () => {
    await loadOperationalConfig()
  })
}

function closeCashierCardQueryModal() {
  cashierCardQueryModalOpen.value = false
  resetCashierCardQueryForm()
  errorMessage.value = ''
}

function openCashierCloseModal(mode: 'preclose' | 'close') {
  activeSupervisorOption.value = mode === 'preclose' ? 'Precierre de Caja' : 'Cierre de Caja'
  cashierCloseMode.value = mode
  resetCashierCloseForm()
  errorMessage.value = ''
  successMessage.value = ''
  cashierCloseModalOpen.value = true
}

function closeCashierCloseModal() {
  cashierCloseModalOpen.value = false
  resetCashierCloseForm()
  errorMessage.value = ''
}

async function saveCashierSale() {
  if (!cashierSession.value) {
    errorMessage.value = 'Primero apertura la caja'
    return
  }
  if (!cashierSaleForm.numeroTarjeta.trim()) {
    errorMessage.value = 'Selecciona una tarjeta'
    return
  }
  if (!cashierSaleSelectedCard.value) {
    errorMessage.value = 'La tarjeta no existe en el inventario operativo'
    return
  }
  if (cashierSaleSelectedCard.value.tipo !== 'CLIENTE') {
    errorMessage.value = 'Solo se pueden registrar ventas a tarjetas tipo CLIENTE'
    return
  }
  if (cashierSaleCardCaptured.value) {
    errorMessage.value = 'Tarjeta actualmente en uso'
    return
  }
  if (cashierSaleForm.monto <= 0) {
    errorMessage.value = 'El monto de venta debe ser mayor a cero'
    return
  }
  await withLoading(async () => {
    await createCashierSale({
      numeroTarjeta: cashierSaleForm.numeroTarjeta,
      monto: cashierSaleForm.monto,
      referencia: emptyFormValue(cashierSaleForm.referencia),
      observaciones: emptyFormValue(cashierSaleForm.observaciones)
    })
    resetCashierSaleForm()
    await loadOperationalConfig()
    cashierSaleModalOpen.value = false
    successMessage.value = 'Venta registrada correctamente'
  })
}

async function saveCashierPayout() {
  if (!cashierSession.value) {
    errorMessage.value = 'Primero apertura la caja'
    return
  }
  if (!cashierPayoutForm.numeroTarjeta.trim()) {
    errorMessage.value = 'Selecciona una tarjeta'
    return
  }
  if (!cashierPayoutSelectedCard.value) {
    errorMessage.value = 'La tarjeta no existe en el inventario operativo'
    return
  }
  if (cashierPayoutSelectedCard.value.tipo !== 'CLIENTE') {
    errorMessage.value = 'Solo se pueden pagar tarjetas tipo CLIENTE'
    return
  }
  if (cashierPayoutCardCaptured.value) {
    errorMessage.value = 'Tarjeta actualmente en uso'
    return
  }
  if (cashierPayoutRequestedAmount.value <= 0) {
    errorMessage.value = 'El monto solicitado debe ser mayor a cero'
    return
  }
  if (cashierPayoutRequestedAmount.value > cashierPayoutCardBalance.value) {
    errorMessage.value = 'El monto solicitado excede el saldo disponible'
    return
  }
  if (cashierPayoutRequestedAmount.value > Number(cashierSession.value.saldoActual || 0)) {
    errorMessage.value = 'Saldo insuficiente en caja'
    return
  }
  await withLoading(async () => {
    await createCashierPayout({
      numeroTarjeta: cashierPayoutForm.numeroTarjeta,
      monto: cashierPayoutRequestedAmount.value,
      referencia: emptyFormValue(cashierPayoutForm.referencia),
      observaciones: emptyFormValue(cashierPayoutForm.observaciones)
    })
    resetCashierPayoutForm()
    await loadOperationalConfig()
    cashierPayoutModalOpen.value = false
    successMessage.value = 'Pago registrado correctamente'
  })
}

async function saveCashierCustomer() {
  if (!cashierSession.value) {
    errorMessage.value = 'Primero apertura la caja'
    return
  }
  if (cashierCustomerForm.nip && !/^\d{4}$/.test(cashierCustomerForm.nip)) {
    errorMessage.value = 'El NIP debe ser de 4 digitos'
    return
  }
  await withLoading(async () => {
    await registerCashierCustomer({
      nombre: cashierCustomerForm.nombre,
      apellidoPaterno: emptyFormValue(cashierCustomerForm.apellidoPaterno),
      apellidoMaterno: emptyFormValue(cashierCustomerForm.apellidoMaterno),
      telefono: emptyFormValue(cashierCustomerForm.telefono),
      email: emptyFormValue(cashierCustomerForm.email),
      fechaNacimiento: cashierCustomerForm.fechaNacimiento || null,
      documentoIdentidad: emptyFormValue(cashierCustomerForm.documentoIdentidad),
      numeroTarjeta: cashierCustomerForm.numeroTarjeta,
      nip: emptyFormValue(cashierCustomerForm.nip),
      observaciones: emptyFormValue(cashierCustomerForm.observaciones)
    })
    resetCashierCustomerForm()
    await loadOperationalConfig()
    cashierCustomerModalOpen.value = false
    successMessage.value = 'Cliente registrado y tarjeta asignada'
  })
}

function resetCashierCustomerForm() {
  Object.assign(cashierCustomerForm, {
    nombre: '',
    apellidoPaterno: '',
    apellidoMaterno: '',
    telefono: '',
    email: '',
    fechaNacimiento: '',
    documentoIdentidad: '',
    numeroTarjeta: '',
    nip: '',
    observaciones: ''
  })
}

function resetCashierSaleForm() {
  Object.assign(cashierSaleForm, {
    numeroTarjeta: '',
    monto: 0,
    referencia: '',
    observaciones: ''
  })
}

function resetCashierPayoutForm() {
  Object.assign(cashierPayoutForm, {
    numeroTarjeta: '',
    monto: 0,
    referencia: '',
    observaciones: ''
  })
}

function resetCashierCardQueryForm() {
  Object.assign(cashierCardQueryForm, {
    numeroTarjeta: ''
  })
}

function resetCashierCloseForm() {
  Object.assign(cashierCloseForm, {
    montoDeclarado: Number(cashierSession.value?.saldoActual ?? 0),
    tarjetasDevueltas: 0,
    observaciones: ''
  })
}

async function saveCashierCloseAction() {
  if (!cashierSession.value) {
    errorMessage.value = 'Primero apertura la caja'
    return
  }
  if (cashierCapturedCardsCount.value > 0) {
    errorMessage.value = 'No se puede cerrar caja: hay tarjetas capturadas por EGM'
    return
  }
  if (cashierCloseForm.montoDeclarado < 0 || cashierCloseForm.tarjetasDevueltas < 0) {
    errorMessage.value = 'El monto y las tarjetas devueltas no pueden ser negativos'
    return
  }
  await withLoading(async () => {
    if (cashierCloseMode.value === 'preclose') {
      await precloseCashier({
        montoDeclarado: cashierCloseForm.montoDeclarado,
        tarjetasDevueltas: cashierCloseForm.tarjetasDevueltas,
        observaciones: emptyFormValue(cashierCloseForm.observaciones)
      })
      successMessage.value = 'Precierre de caja realizado'
    } else {
      await closeCashier({
        montoDeclarado: cashierCloseForm.montoDeclarado,
        tarjetasDevueltas: cashierCloseForm.tarjetasDevueltas,
        observaciones: emptyFormValue(cashierCloseForm.observaciones)
      })
      successMessage.value = 'Cierre de caja realizado'
    }
    cashierCloseModalOpen.value = false
    resetCashierCloseForm()
    await loadOperationalConfig()
  })
}

async function openDay() {
  const fechaJornada = toApiDate(todayDisplayDate())
  if (!fechaJornada) {
    errorMessage.value = 'La fecha de jornada debe tener formato dd/mm/aaaa'
    return
  }
  await withLoading(async () => {
    currentOperationalDay.value = await openOperationalDay({
      fechaJornada,
      observaciones: 'Apertura desde Inicio de Operaciones'
    })
    await loadOperationalConfig()
    successMessage.value = 'Apertura de jornada realizada correctamente'
  })
}

async function runOperationalClose() {
  if (!currentOperationalDay.value) {
    errorMessage.value = 'No hay jornada abierta para cerrar'
    return
  }
  await withLoading(async () => {
    operationalClose.value = await closeOperationalDay({
      observaciones: emptyFormValue(operationalCloseForm.observaciones),
      forzarConDiferencias: operationalCloseForm.forzarConDiferencias
    })
    operationalCloseForm.observaciones = ''
    operationalCloseForm.forzarConDiferencias = false
    await loadOperationalConfig()
    successMessage.value = 'Cierre de operaciones ejecutado con barrido y conciliacion EGM'
  })
}

async function runEgmSweepTest() {
  if (!currentOperationalDay.value) {
    errorMessage.value = 'No hay jornada abierta para probar barrido EGM'
    return
  }
  await withLoading(async () => {
    manualEgmSweepSnapshots.value = await testEgmSweep()
    await loadOperationalConfig()
    successMessage.value = `Barrido real EGM ejecutado: ${manualEgmSweepSnapshots.value.length} snapshot(s) registrado(s)`
  })
}

function openTreasuryEditor(editor: typeof treasuryEditor.value) {
  treasuryEditor.value = editor
  resetTreasuryForms()
  if (editor === 'cashReturn') {
    const pendingCashier = firstTreasuryPendingClosedCashier()
    if (pendingCashier) {
      treasuryMovementForm.estacionCajaId = pendingCashier.estacionId
      treasuryMovementForm.turnoId = pendingCashier.turnoId
    }
    applyExpectedCashReturnAmount()
  }
}

async function saveTreasuryAction() {
  await withLoading(async () => {
    if (treasuryEditor.value === 'open') {
      if (!treasuryOpenForm.estacionId) {
        errorMessage.value = 'Selecciona tesoreria'
        return
      }
      await openTreasury({
        estacionId: treasuryOpenForm.estacionId,
        saldoInicial: treasuryOpenForm.saldoInicial,
        observaciones: emptyFormValue(treasuryOpenForm.observaciones)
      })
      successMessage.value = 'Tesoreria abierta'
    }
    if (treasuryEditor.value === 'movement') {
      await createTreasuryMovement({
        ...treasuryMovementForm,
        estacionCajaId: null,
        turnoId: null,
        referencia: emptyFormValue(treasuryMovementForm.referencia),
        observaciones: emptyFormValue(treasuryMovementForm.observaciones)
      })
      successMessage.value = 'Movimiento registrado'
    }
    if (treasuryEditor.value === 'fund') {
      await sendTreasuryBoxFund(treasuryMoneyPayload('Fondo inicial a caja'))
      successMessage.value = 'Fondo enviado a caja'
    }
    if (treasuryEditor.value === 'cashReturn') {
      await receiveTreasuryCashReturn(treasuryMoneyPayload('Devolucion de efectivo desde caja'))
      successMessage.value = 'Devolucion de efectivo recibida'
    }
    if (treasuryEditor.value === 'cardDelivery') {
      await deliverTreasuryCardRange(treasuryCardPayload())
      successMessage.value = 'Rango de tarjetas entregado'
    }
    if (treasuryEditor.value === 'cardReturn') {
      await receiveTreasuryCardReturn(treasuryCardPayload())
      successMessage.value = 'Tarjetas no usadas recibidas'
    }
    if (treasuryEditor.value === 'preclose') {
      await precloseTreasury(emptyFormValue(treasuryCloseForm.observaciones))
      successMessage.value = 'Precierre de tesoreria realizado'
    }
    if (treasuryEditor.value === 'close') {
      await closeTreasury(emptyFormValue(treasuryCloseForm.observaciones))
      successMessage.value = 'Cierre de tesoreria realizado'
    }
    treasuryEditor.value = 'none'
    await loadOperationalConfig()
  })
}

function treasuryMoneyPayload(defaultConcept: string) {
  if (!treasuryMovementForm.estacionCajaId) {
    throw new Error('Selecciona caja')
  }
  return {
    concepto: treasuryMovementForm.concepto.trim() || defaultConcept,
    monto: treasuryMovementForm.monto,
    estacionCajaId: treasuryMovementForm.estacionCajaId,
    turnoId: treasuryMovementForm.turnoId || null,
    referencia: emptyFormValue(treasuryMovementForm.referencia),
    observaciones: emptyFormValue(treasuryMovementForm.observaciones)
  }
}

function treasuryCardPayload() {
  if (!treasuryCardForm.estacionCajaId) {
    throw new Error('Selecciona caja')
  }
  return {
    estacionCajaId: treasuryCardForm.estacionCajaId,
    turnoId: treasuryCardForm.turnoId || null,
    numeroInicial: treasuryCardForm.numeroInicial,
    numeroFinal: treasuryCardForm.numeroFinal,
    referencia: emptyFormValue(treasuryCardForm.referencia),
    observaciones: emptyFormValue(treasuryCardForm.observaciones)
  }
}

function resetTreasuryForms() {
  Object.assign(treasuryOpenForm, {
    estacionId: treasuryStations.value[0]?.id ?? 0,
    saldoInicial: 0,
    observaciones: ''
  })
  Object.assign(treasuryMovementForm, {
    tipo: 'ENTRADA',
    concepto: '',
    monto: 0,
    estacionCajaId: cashierStations.value[0]?.id ?? 0,
    turnoId: cashierShifts.value[0]?.id ?? 0,
    referencia: '',
    observaciones: ''
  })
  Object.assign(treasuryCardForm, {
    estacionCajaId: cashierStations.value[0]?.id ?? 0,
    turnoId: cashierShifts.value[0]?.id ?? 0,
    numeroInicial: '',
    numeroFinal: '',
    referencia: '',
    observaciones: ''
  })
  Object.assign(treasuryCloseForm, { observaciones: '' })
}

function applyExpectedCashReturnAmount() {
  if (treasuryEditor.value !== 'cashReturn') return
  treasuryMovementForm.monto = treasuryExpectedCashReturn.value
  const closed = treasurySelectedClosedCashier.value
  treasuryMovementForm.concepto = 'Devolucion de efectivo desde caja'
  treasuryMovementForm.referencia = closed ? `CIERRE-CAJA-${closed.id}` : ''
}

function firstTreasuryPendingClosedCashier() {
  return treasuryClosedCashiers.value.find((cashier) => {
    const returned = treasuryMovements.value
      .filter((movement) =>
        movement.tipo === 'DEVOLUCION_CAJA'
          && movement.estacionCajaId === cashier.estacionId
          && movement.turnoId === cashier.turnoId
      )
      .reduce((total, movement) => total + Number(movement.monto || 0), 0)
    const declared = Number(cashier.montoDeclaradoCierre ?? cashier.saldoActual ?? 0)
    return declared - returned > 0
  }) ?? null
}

async function saveOperationSchedule() {
  await withLoading(async () => {
    await createOperationSchedule(operationScheduleForm)
    Object.assign(operationScheduleForm, { nombre: '', horaInicio: '09:00', horaFin: '23:59', activo: true })
    await loadOperationalConfig()
    successMessage.value = 'Horario guardado'
  })
}

async function saveCashierShift() {
  await withLoading(async () => {
    await createCashierShift(cashierShiftForm)
    Object.assign(cashierShiftForm, { nombre: '', horaInicio: '09:00', horaFin: '17:00', activo: true })
    await loadOperationalConfig()
    successMessage.value = 'Turno guardado'
  })
}

async function saveWorkstation() {
  await withLoading(async () => {
    await createWorkstation(workstationForm)
    Object.assign(workstationForm, {
      nombre: '',
      tipo: 'CAJA',
      sala: 'Play Valley',
      ubicacion: '',
      activa: true
    })
    await loadOperationalConfig()
    successMessage.value = 'Caja/Tesoreria guardada'
  })
}

async function saveOperationalAssignment() {
  if (!assignmentForm.usuarioId || !assignmentForm.estacionId || !assignmentForm.turnoId) {
    errorMessage.value = 'Selecciona usuario, estacion y turno'
    return
  }
  const fechaOperacion = toApiDate(assignmentForm.fechaOperacion)
  if (!fechaOperacion) {
    errorMessage.value = 'La fecha de operacion debe tener formato dd/mm/aaaa'
    return
  }
  await withLoading(async () => {
    await createOperationalAssignment({
      ...assignmentForm,
      fechaOperacion
    })
    Object.assign(assignmentForm, {
      usuarioId: 0,
      estacionId: 0,
      turnoId: 0,
      fechaOperacion: todayDisplayDate(),
      rolOperativo: 'SUPERVISOR',
      activa: true
    })
    await loadOperationalConfig()
    successMessage.value = 'Asignacion guardada'
  })
}

async function saveCardRange() {
  const fechaVencimiento = toNullableApiDate(cardRangeForm.fechaVencimiento)
  if (cardRangeForm.fechaVencimiento && !fechaVencimiento) {
    errorMessage.value = 'La fecha de vencimiento debe tener formato dd/mm/aaaa'
    return
  }
  await withLoading(async () => {
    await createOperationalCardRange({
      ...cardRangeForm,
      fechaVencimiento
    })
    Object.assign(cardRangeForm, {
      numeroInicial: '',
      numeroFinal: '',
      tipo: 'CLIENTE',
      fechaVencimiento: '',
      maximoTarjetas: 500
    })
    await loadOperationalConfig()
    successMessage.value = 'Tarjetas guardadas'
  })
}

function newOperationSchedule() {
  resetOperationalForms()
  selectedConfigId.value = null
  configEditor.value = 'schedule'
}

function newCashierShift() {
  resetOperationalForms()
  selectedConfigId.value = null
  configEditor.value = 'shift'
}

function newWorkstation() {
  resetOperationalForms()
  selectedConfigId.value = null
  configEditor.value = 'workstation'
}

function newOperationalAssignment() {
  resetOperationalForms()
  selectedConfigId.value = null
  configEditor.value = 'assignment'
}

function newOperationalCard() {
  resetOperationalForms()
  selectedConfigId.value = null
  configEditor.value = 'card'
}

function newOperationalCardRange() {
  resetOperationalForms()
  selectedConfigId.value = null
  configEditor.value = 'cardRange'
}

function editOperationSchedule(item: OperationSchedule) {
  selectedConfigId.value = item.id
  configEditor.value = 'schedule'
  Object.assign(operationScheduleForm, {
    nombre: item.nombre,
    horaInicio: item.horaInicio.slice(0, 5),
    horaFin: item.horaFin.slice(0, 5),
    activo: item.activo
  })
}

function editCashierShift(item: CashierShift) {
  selectedConfigId.value = item.id
  configEditor.value = 'shift'
  Object.assign(cashierShiftForm, {
    nombre: item.nombre,
    horaInicio: item.horaInicio.slice(0, 5),
    horaFin: item.horaFin.slice(0, 5),
    activo: item.activo
  })
}

function editWorkstation(item: Workstation) {
  selectedConfigId.value = item.id
  configEditor.value = 'workstation'
  Object.assign(workstationForm, {
    nombre: item.nombre,
    tipo: item.tipo,
    cajaId: item.cajaId,
    sala: item.sala ?? '',
    ubicacion: item.ubicacion ?? '',
    activa: item.activa
  })
}

function editOperationalAssignment(item: OperationalAssignment) {
  selectedConfigId.value = item.id
  configEditor.value = 'assignment'
  Object.assign(assignmentForm, {
    usuarioId: item.usuarioId,
    estacionId: item.estacionId,
    turnoId: item.turnoId,
    fechaOperacion: formatDate(item.fechaOperacion),
    rolOperativo: item.rolOperativo,
    activa: item.activa
  })
}

function editOperationalCard(item: OperationalCard) {
  selectedConfigId.value = item.id
  configEditor.value = 'card'
  Object.assign(cardForm, {
    numeroTarjeta: item.numeroTarjeta,
    tipo: item.tipo,
    fechaVencimiento: item.fechaVencimiento ?? '',
    estado: item.estado
  })
}

function closeConfigEditor() {
  configEditor.value = 'none'
  selectedConfigId.value = null
  resetOperationalForms()
}

function resetOperationalForms() {
  Object.assign(operationScheduleForm, { nombre: '', horaInicio: '09:00', horaFin: '23:59', activo: true })
  Object.assign(cashierShiftForm, { nombre: '', horaInicio: '09:00', horaFin: '17:00', activo: true })
  Object.assign(workstationForm, {
    nombre: '',
    tipo: 'CAJA',
    cajaId: null,
    sala: 'Play Valley',
    ubicacion: '',
    activa: true
  })
  Object.assign(assignmentForm, {
    usuarioId: 0,
    estacionId: 0,
    turnoId: 0,
    fechaOperacion: todayDisplayDate(),
    rolOperativo: 'SUPERVISOR',
    activa: true
  })
  Object.assign(cardForm, {
    numeroTarjeta: '',
    tipo: 'CLIENTE',
    fechaVencimiento: '',
    estado: 'DISPONIBLE'
  })
  Object.assign(cardRangeForm, {
    numeroInicial: '',
    numeroFinal: '',
    tipo: 'CLIENTE',
    fechaVencimiento: '',
    maximoTarjetas: 500
  })
}

async function saveConfigEdit() {
  await withLoading(async () => {
    if (configEditor.value === 'schedule') {
      if (selectedConfigId.value) {
        await updateOperationSchedule(selectedConfigId.value, operationScheduleForm)
        successMessage.value = 'Horario actualizado'
      } else {
        await createOperationSchedule(operationScheduleForm)
        successMessage.value = 'Horario creado'
      }
    }
    if (configEditor.value === 'shift') {
      if (selectedConfigId.value) {
        await updateCashierShift(selectedConfigId.value, cashierShiftForm)
        successMessage.value = 'Turno actualizado'
      } else {
        await createCashierShift(cashierShiftForm)
        successMessage.value = 'Turno creado'
      }
    }
    if (configEditor.value === 'workstation') {
      if (selectedConfigId.value) {
        await updateWorkstation(selectedConfigId.value, workstationForm)
        successMessage.value = 'Caja/Tesoreria actualizada'
      } else {
        await createWorkstation(workstationForm)
        successMessage.value = 'Caja/Tesoreria creada'
      }
    }
    if (configEditor.value === 'assignment') {
      if (!assignmentForm.usuarioId || !assignmentForm.estacionId || !assignmentForm.turnoId) {
        errorMessage.value = 'Selecciona usuario, estacion y turno'
        return
      }
      const fechaOperacion = toApiDate(assignmentForm.fechaOperacion)
      if (!fechaOperacion) {
        errorMessage.value = 'La fecha de operacion debe tener formato dd/mm/aaaa'
        return
      }
      const payload = {
        ...assignmentForm,
        fechaOperacion
      }
      if (selectedConfigId.value) {
        await updateOperationalAssignment(selectedConfigId.value, payload)
        successMessage.value = 'Asignacion actualizada'
      } else {
        await createOperationalAssignment(payload)
        successMessage.value = 'Asignacion creada'
      }
    }
    if (configEditor.value === 'card') {
      const fechaVencimiento = toNullableApiDate(cardForm.fechaVencimiento)
      if (cardForm.fechaVencimiento && !fechaVencimiento) {
        errorMessage.value = 'La fecha de vencimiento debe tener formato dd/mm/aaaa'
        return
      }
      const payload = {
        ...cardForm,
        fechaVencimiento
      }
      if (selectedConfigId.value) {
        await updateOperationalCard(selectedConfigId.value, payload)
        successMessage.value = 'Tarjeta actualizada'
      } else {
        await createOperationalCard(payload)
        successMessage.value = 'Tarjeta creada'
      }
    }
    if (configEditor.value === 'cardRange') {
      const fechaVencimiento = toNullableApiDate(cardRangeForm.fechaVencimiento)
      if (cardRangeForm.fechaVencimiento && !fechaVencimiento) {
        errorMessage.value = 'La fecha de vencimiento debe tener formato dd/mm/aaaa'
        return
      }
      await createOperationalCardRange({
        ...cardRangeForm,
        fechaVencimiento
      })
      successMessage.value = 'Rango de tarjetas creado'
    }
    await loadOperationalConfig()
    closeConfigEditor()
  })
}

async function removeOperationSchedule(id: number) {
  await removeConfigRecord(() => deleteOperationSchedule(id), 'Horario eliminado')
}

async function removeCashierShift(id: number) {
  await removeConfigRecord(() => deleteCashierShift(id), 'Turno eliminado')
}

async function removeWorkstation(id: number) {
  await removeConfigRecord(() => deleteWorkstation(id), 'Caja/Tesoreria eliminada')
}

async function removeOperationalAssignment(id: number) {
  await removeConfigRecord(() => deleteOperationalAssignment(id), 'Asignacion eliminada')
}

async function removeOperationalCard(id: number) {
  await removeConfigRecord(() => deleteOperationalCard(id), 'Tarjeta eliminada')
}

async function removeConfigRecord(action: () => Promise<void>, message: string) {
  await withLoading(async () => {
    await action()
    await loadOperationalConfig()
    successMessage.value = message
  })
}

async function selectUser(id: number) {
  selectedUser.value = await getUser(id)
  editorMode.value = 'edit'
  passwordReset.value = ''
  Object.assign(form, {
    username: selectedUser.value.username,
    email: selectedUser.value.email ?? '',
    telefono: selectedUser.value.telefono ?? '',
    nombre: selectedUser.value.nombre,
    apellidoPaterno: selectedUser.value.apellidoPaterno ?? '',
    apellidoMaterno: selectedUser.value.apellidoMaterno ?? '',
    password: '',
    activo: selectedUser.value.activo,
    bloqueado: selectedUser.value.bloqueado,
    motivoBloqueo: selectedUser.value.motivoBloqueo ?? '',
    requiereCambioPassword: selectedUser.value.requiereCambioPassword,
    roles: [...selectedUser.value.roles]
  })
}

function newUser() {
  selectedUser.value = null
  editorMode.value = 'create'
  passwordReset.value = ''
  Object.assign(form, {
    username: '',
    email: '',
    telefono: '',
    nombre: '',
    apellidoPaterno: '',
    apellidoMaterno: '',
    password: '',
    activo: true,
    bloqueado: false,
    motivoBloqueo: '',
    requiereCambioPassword: true,
    roles: ['ADMIN']
  })
}

function closeEditor() {
  selectedUser.value = null
  editorMode.value = 'none'
  passwordReset.value = ''
  closeConfigEditor()
}

async function saveUser() {
  await withLoading(async () => {
    if (editorMode.value === 'edit' && selectedUser.value) {
      await updateUser(selectedUser.value.id, form)
      successMessage.value = 'Usuario actualizado'
      await loadUsers()
      closeEditor()
      return
    }
    await createUser(form)
    successMessage.value = 'Usuario creado'
    await loadUsers()
    closeEditor()
  })
}

async function resetPassword() {
  if (editorMode.value !== 'edit' || !selectedUser.value || !passwordReset.value) return
  await withLoading(async () => {
    await updateUserPassword(selectedUser.value!.id, passwordReset.value)
    passwordReset.value = ''
    successMessage.value = 'Password actualizado'
  })
}

async function removeSelected() {
  if (editorMode.value !== 'edit' || !selectedUser.value) return
  await withLoading(async () => {
    await deleteUser(selectedUser.value!.id)
    successMessage.value = 'Usuario eliminado'
    await loadUsers()
    closeEditor()
  })
}

async function handleLogout() {
  await logout()
  session.value = null
  users.value = []
  roles.value = []
  cashierCustomers.value = []
  closeEditor()
}

async function withLoading(action: () => Promise<void>) {
  loading.value = true
  errorMessage.value = ''
  successMessage.value = ''
  try {
    await action()
  } catch (error) {
    errorMessage.value = resolveError(error)
  } finally {
    loading.value = false
  }
}

function resolveError(error: unknown) {
  if (typeof error === 'object' && error && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response
    return response?.data?.message ?? 'Operacion no completada'
  }
  if (error instanceof Error) {
    return error.message
  }
  return 'Operacion no completada'
}

function emptyFormValue(value: string) {
  const trimmed = value.trim()
  return trimmed ? trimmed : null
}

function fullName(user: UserSummary) {
  return [user.nombre, user.apellidoPaterno, user.apellidoMaterno].filter(Boolean).join(' ')
}

function customerFullName(customer: CustomerRegistration) {
  return [customer.nombre, customer.apellidoPaterno, customer.apellidoMaterno].filter(Boolean).join(' ')
}

function cardCustomerName(card: OperationalCard, customer?: CustomerRegistration | null) {
  const name = card.clienteNombre || (customer ? customerFullName(customer) : '')
  return (name || 'Sin cliente').toUpperCase()
}

function userStatus(user: UserSummary) {
  if (user.eliminado) return 'Eliminado'
  if (user.bloqueado) return 'Bloqueado'
  if (!user.activo) return 'Inactivo'
  return 'Activo'
}

function sumMoney(movements: TreasuryMovement[]) {
  return movements.reduce((total, movement) => total + movement.monto, 0)
}

function sumCashierMovementsByType(movements: CashierMovement[], types: string[]) {
  return movements
    .filter((movement) => types.includes(movement.tipo))
    .reduce((total, movement) => total + movement.monto, 0)
}

function cashierMovementBalanceImpact(movement: CashierMovement) {
  if (movement.impactoSaldo !== null && movement.impactoSaldo !== undefined) {
    return movement.impactoSaldo
  }
  if (['VENTA', 'CORTESIA', 'PROMOCIONAL', 'TRANSACCION_ESPECIAL'].includes(movement.tipo)) {
    return movement.monto
  }
  if (['PAGO', 'PAGO_MANUAL', 'DEVOLUCION'].includes(movement.tipo)) {
    return -movement.monto
  }
  return 0
}

function cashierMovementDisplayType(movement: CashierMovement) {
  if (movement.tipo === 'PAGO' && movement.motivo === 'Cobro RFID desde EGM') {
    return 'EGM PAGO'
  }
  return movement.tipo
}

function sumCards(movements: TreasuryCardMovement[]) {
  return movements.reduce((total, movement) => total + movement.cantidad, 0)
}

function todayDisplayDate() {
  const today = new Date()
  return [
    String(today.getDate()).padStart(2, '0'),
    String(today.getMonth() + 1).padStart(2, '0'),
    today.getFullYear()
  ].join('/')
}

function formatDate(value: string) {
  const parts = parseApiDate(value)
  if (!parts) return value
  return [parts.day, parts.month, parts.year].join('/')
}

function formatNullableDate(value: string | null, fallback = '') {
  return value ? formatDate(value) : fallback
}

function toNullableApiDate(value: string) {
  return value.trim() ? toApiDate(value) : null
}

function normalizeDisplayDate(value: string) {
  const trimmed = value.trim()
  if (!trimmed) return ''
  const match = /^(\d{1,2})\/(\d{1,2})\/(\d{4})$/.exec(trimmed)
  if (!match) return trimmed
  const [, day, month, year] = match
  const normalizedDay = day.padStart(2, '0')
  const normalizedMonth = month.padStart(2, '0')
  return isValidDateParts(normalizedDay, normalizedMonth, year)
    ? `${normalizedDay}/${normalizedMonth}/${year}`
    : trimmed
}

function toApiDate(value: string) {
  const trimmed = value.trim()
  const displayMatch = /^(\d{1,2})\/(\d{1,2})\/(\d{4})$/.exec(trimmed)
  if (displayMatch) {
    const [, day, month, year] = displayMatch
    const normalizedDay = day.padStart(2, '0')
    const normalizedMonth = month.padStart(2, '0')
    return isValidDateParts(normalizedDay, normalizedMonth, year)
      ? `${year}-${normalizedMonth}-${normalizedDay}`
      : null
  }
  const apiParts = parseApiDate(trimmed)
  return apiParts && isValidDateParts(apiParts.day, apiParts.month, apiParts.year)
    ? `${apiParts.year}-${apiParts.month}-${apiParts.day}`
    : null
}

function parseApiDate(value: string) {
  const match = /^(\d{4})-(\d{2})-(\d{2})/.exec(value)
  if (!match) return null
  const [, year, month, day] = match
  return { year, month, day }
}

function isValidDateParts(day: string, month: string, year: string) {
  const dayNumber = Number(day)
  const monthNumber = Number(month)
  const yearNumber = Number(year)
  const date = new Date(yearNumber, monthNumber - 1, dayNumber)
  return (
    date.getFullYear() === yearNumber &&
    date.getMonth() === monthNumber - 1 &&
    date.getDate() === dayNumber
  )
}

function formatDateTime(value: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const day = String(date.getDate()).padStart(2, '0')
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const year = date.getFullYear()
  const time = new Intl.DateTimeFormat('es-MX', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  }).format(date)
  return `${day}/${month}/${year} ${time}`
}

function formatMoney(value: number) {
  return new Intl.NumberFormat('es-MX', {
    style: 'currency',
    currency: 'MXN'
  }).format(value)
}

function hasAnyRole(expectedRoles: string[]) {
  return expectedRoles.some((role) => session.value?.roles.includes(role))
}

function isSupervisorSectionOpen(title: string) {
  return openSupervisorSections.value.includes(title)
}

function toggleSupervisorSection(title: string) {
  if (isSupervisorSectionOpen(title)) {
    openSupervisorSections.value = openSupervisorSections.value.filter((section) => section !== title)
    return
  }
  openSupervisorSections.value = [...openSupervisorSections.value, title]
}
</script>
