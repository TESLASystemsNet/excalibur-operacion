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
            Menu supervisor
          </button>

          <div v-if="supervisorMenuOpen" id="supervisor-menu" class="supervisor-menu">
            <div class="menu-root">
              <ClipboardList :size="16" />
              Inicio
            </div>
            <div v-for="section in supervisorMenu" :key="section.title" class="menu-group">
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
                  @click="activeSupervisorOption = item"
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
          <form class="editor-panel modal-panel" role="dialog" aria-modal="true" @submit.prevent="saveUser">
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
            <p class="section-kicker">Supervisor</p>
            <h1>{{ activeSupervisorOption }}</h1>
            <p v-if="successMessage" class="success-line">{{ successMessage }}</p>
            <p v-if="errorMessage" class="error-line">{{ errorMessage }}</p>
          </div>
          <button v-if="activeSupervisorOption === 'Inicio de Operaciones'" class="primary-action compact" type="button">
            <PlayCircle :size="18" />
            Abrir turno
          </button>
        </header>

        <section v-if="isOperationalConfigOption" class="config-workspace">
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
            <button class="primary-action compact section-new-action" type="button" @click="newOperationalCard">
              <UserPlus :size="18" />
              Nueva tarjeta
            </button>
            <div class="table-panel config-table-panel">
              <table>
                <thead>
                  <tr>
                    <th>Tarjeta</th>
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

        <section v-else class="operation-hero">
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
            <strong>Pendiente de apertura</strong>
          </div>
        </section>

        <section v-if="!isOperationalConfigOption" class="metrics-grid supervisor-metrics">
          <article class="metric-card">
            <span>Turno</span>
            <strong>Sin abrir</strong>
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

        <section v-if="!isOperationalConfigOption" class="operation-grid">
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

        <div v-if="configEditor !== 'none'" class="modal-backdrop" role="presentation">
          <form class="editor-panel modal-panel" role="dialog" aria-modal="true" @submit.prevent="saveConfigEdit">
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
                  v-model.trim="cardForm.fechaVencimiento"
                  inputmode="numeric"
                  maxlength="10"
                  pattern="\d{2}/\d{2}/\d{4}"
                  placeholder="dd/mm/aaaa"
                  @blur="cardForm.fechaVencimiento = normalizeDisplayDate(cardForm.fechaVencimiento)"
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
  createCashierShift,
  createOperationSchedule,
  createOperationalAssignment,
  createOperationalCard,
  createOperationalCardRange,
  createWorkstation,
  createUser,
  deleteCashierShift,
  deleteOperationSchedule,
  deleteOperationalAssignment,
  deleteOperationalCard,
  deleteWorkstation,
  deleteUser,
  getCashierShifts,
  getOperationSchedules,
  getOperationalAssignments,
  getOperationalAudit,
  getOperationalCards,
  getRoles,
  getUser,
  getUsers,
  getWorkstations,
  hasToken,
  login,
  logout,
  me,
  updateCashierShift,
  updateOperationSchedule,
  updateOperationalAssignment,
  updateOperationalCard,
  updateWorkstation,
  updateUser,
  updateUserPassword
} from './services/api'
import type {
  CashierShift,
  LoginResponse,
  OperationSchedule,
  OperationalAssignment,
  OperationalAuditEvent,
  OperationalCard,
  Role,
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
const configEditor = ref<'none' | 'schedule' | 'shift' | 'workstation' | 'assignment' | 'card'>('none')
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
    items: ['Reporte Operaciones de Caja', 'Reporte Estado de Cajas']
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

const activeUsers = computed(() => users.value.filter((user) => user.activo && !user.eliminado).length)
const blockedUsers = computed(() => users.value.filter((user) => user.bloqueado).length)
const isAdminDashboard = computed(() => hasAnyRole(['ADMIN']))
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
const configEditorTitle = computed(() => {
  const titles = {
    none: '',
    schedule: 'Horario de Operacion',
    shift: 'Turno de Caja',
    workstation: 'Caja/Tesoreria',
    assignment: 'Asignacion de Usuario',
    card: 'Tarjeta'
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
  if (isAdminDashboard.value) {
    await Promise.all([loadRoles(), loadUsers()])
  } else {
    await loadOperationalConfig()
  }
  closeEditor()
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
    operationalUsers
  ] = await Promise.all([
    getOperationSchedules(),
    getCashierShifts(),
    getWorkstations(),
    getOperationalAssignments(),
    getOperationalCards(),
    getOperationalAudit(),
    getUsers()
  ])
  operationSchedules.value = schedules
  cashierShifts.value = shifts
  workstations.value = stations
  operationalAssignments.value = assignments
  operationalCards.value = cards
  operationalAudit.value = audit
  users.value = operationalUsers
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
    fechaVencimiento: formatNullableDate(item.fechaVencimiento),
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
  return 'Operacion no completada'
}

function fullName(user: UserSummary) {
  return [user.nombre, user.apellidoPaterno, user.apellidoMaterno].filter(Boolean).join(' ')
}

function userStatus(user: UserSummary) {
  if (user.eliminado) return 'Eliminado'
  if (user.bloqueado) return 'Bloqueado'
  if (!user.activo) return 'Inactivo'
  return 'Activo'
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
