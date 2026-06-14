package mx.com.excalibur.operacion.operational;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class OperationalConfigRepository {

    private final JdbcClient jdbcClient;

    public OperationalConfigRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<OperationSchedule> findOperationSchedules() {
        return jdbcClient.sql("""
                SELECT id_horario, nombre, hora_inicio, hora_fin, activo, fecha_creacion, fecha_actualizacion
                FROM app.horarios_operacion
                ORDER BY id_horario
                """)
                .query((rs, rowNum) -> operationSchedule(rs))
                .list();
    }

    public Long createOperationSchedule(OperationScheduleRequest request, Long userId) {
        return jdbcClient.sql("""
                INSERT INTO app.horarios_operacion (nombre, hora_inicio, hora_fin, activo, creado_por)
                VALUES (:nombre, :horaInicio, :horaFin, :activo, :userId)
                RETURNING id_horario
                """)
                .param("nombre", request.nombre().trim())
                .param("horaInicio", request.horaInicio())
                .param("horaFin", request.horaFin())
                .param("activo", request.activo() == null || request.activo())
                .param("userId", userId)
                .query(Long.class)
                .single();
    }

    public void updateOperationSchedule(Long id, OperationScheduleRequest request, Long userId) {
        jdbcClient.sql("""
                UPDATE app.horarios_operacion
                SET nombre = :nombre,
                    hora_inicio = :horaInicio,
                    hora_fin = :horaFin,
                    activo = :activo,
                    actualizado_por = :userId,
                    fecha_actualizacion = CURRENT_TIMESTAMP
                WHERE id_horario = :id
                """)
                .param("id", id)
                .param("nombre", request.nombre().trim())
                .param("horaInicio", request.horaInicio())
                .param("horaFin", request.horaFin())
                .param("activo", request.activo() == null || request.activo())
                .param("userId", userId)
                .update();
    }

    public void deleteOperationSchedule(Long id) {
        jdbcClient.sql("DELETE FROM app.horarios_operacion WHERE id_horario = :id")
                .param("id", id)
                .update();
    }

    public List<CashierShift> findCashierShifts() {
        return jdbcClient.sql("""
                SELECT id_turno, nombre, hora_inicio, hora_fin, activo, fecha_creacion, fecha_actualizacion
                FROM app.turnos_caja
                ORDER BY id_turno
                """)
                .query((rs, rowNum) -> cashierShift(rs))
                .list();
    }

    public Long createCashierShift(CashierShiftRequest request, Long userId) {
        return jdbcClient.sql("""
                INSERT INTO app.turnos_caja (nombre, hora_inicio, hora_fin, activo, creado_por)
                VALUES (:nombre, :horaInicio, :horaFin, :activo, :userId)
                RETURNING id_turno
                """)
                .param("nombre", request.nombre().trim())
                .param("horaInicio", request.horaInicio())
                .param("horaFin", request.horaFin())
                .param("activo", request.activo() == null || request.activo())
                .param("userId", userId)
                .query(Long.class)
                .single();
    }

    public void updateCashierShift(Long id, CashierShiftRequest request, Long userId) {
        jdbcClient.sql("""
                UPDATE app.turnos_caja
                SET nombre = :nombre,
                    hora_inicio = :horaInicio,
                    hora_fin = :horaFin,
                    activo = :activo,
                    actualizado_por = :userId,
                    fecha_actualizacion = CURRENT_TIMESTAMP
                WHERE id_turno = :id
                """)
                .param("id", id)
                .param("nombre", request.nombre().trim())
                .param("horaInicio", request.horaInicio())
                .param("horaFin", request.horaFin())
                .param("activo", request.activo() == null || request.activo())
                .param("userId", userId)
                .update();
    }

    public void deleteCashierShift(Long id) {
        jdbcClient.sql("DELETE FROM app.turnos_caja WHERE id_turno = :id")
                .param("id", id)
                .update();
    }

    public List<Workstation> findWorkstations() {
        return jdbcClient.sql("""
                SELECT e.id_estacion, e.id_caja, c.nombre AS caja_nombre, e.nombre, e.tipo,
                       e.sala, e.ubicacion, e.activa, e.fecha_creacion, e.fecha_actualizacion
                FROM app.estaciones_operativas e
                LEFT JOIN app.cajas_operativas c ON c.id_caja = e.id_caja
                ORDER BY e.tipo, e.nombre
                """)
                .query((rs, rowNum) -> workstation(rs))
                .list();
    }

    public Long createWorkstation(WorkstationRequest request, Long userId) {
        return jdbcClient.sql("""
                INSERT INTO app.estaciones_operativas (id_caja, nombre, tipo, sala, ubicacion, activa, creado_por)
                VALUES (:cajaId, :nombre, :tipo, :sala, :ubicacion, :activa, :userId)
                RETURNING id_estacion
                """)
                .param("cajaId", cashierBoxIdForWorkstation(request))
                .param("nombre", request.nombre().trim())
                .param("tipo", request.tipo().trim().toUpperCase())
                .param("sala", blankToNull(request.sala()))
                .param("ubicacion", blankToNull(request.ubicacion()))
                .param("activa", request.activa() == null || request.activa())
                .param("userId", userId)
                .query(Long.class)
                .single();
    }

    public void updateWorkstation(Long id, WorkstationRequest request, Long userId) {
        jdbcClient.sql("""
                UPDATE app.estaciones_operativas
                SET id_caja = :cajaId,
                    nombre = :nombre,
                    tipo = :tipo,
                    sala = :sala,
                    ubicacion = :ubicacion,
                    activa = :activa,
                    actualizado_por = :userId,
                    fecha_actualizacion = CURRENT_TIMESTAMP
                WHERE id_estacion = :id
                """)
                .param("id", id)
                .param("cajaId", cashierBoxIdForWorkstation(request))
                .param("nombre", request.nombre().trim())
                .param("tipo", request.tipo().trim().toUpperCase())
                .param("sala", blankToNull(request.sala()))
                .param("ubicacion", blankToNull(request.ubicacion()))
                .param("activa", request.activa() == null || request.activa())
                .param("userId", userId)
                .update();
    }

    public void deleteWorkstation(Long id) {
        jdbcClient.sql("DELETE FROM app.estaciones_operativas WHERE id_estacion = :id")
                .param("id", id)
                .update();
    }

    public List<OperationalAssignment> findAssignments(LocalDate fechaOperacion) {
        String query = """
                SELECT a.id_asignacion, a.id_usuario, u.username,
                       concat_ws(' ', u.nombre, u.apellido_paterno, u.apellido_materno) AS nombre_usuario,
                       a.id_estacion, e.nombre AS estacion_nombre, e.tipo AS estacion_tipo,
                       a.id_turno, t.nombre AS turno_nombre, a.fecha_operacion, a.rol_operativo,
                       a.activa, a.fecha_creacion, a.fecha_actualizacion
                FROM app.asignaciones_operativas a
                JOIN app.usuarios u ON u.id_usuario = a.id_usuario
                JOIN app.estaciones_operativas e ON e.id_estacion = a.id_estacion
                JOIN app.turnos_caja t ON t.id_turno = a.id_turno
                WHERE (CAST(:fechaOperacion AS DATE) IS NULL OR a.fecha_operacion = :fechaOperacion)
                ORDER BY a.fecha_operacion DESC, e.tipo, e.nombre, a.rol_operativo
                """;
        return jdbcClient.sql(query)
                .param("fechaOperacion", fechaOperacion)
                .query((rs, rowNum) -> assignment(rs))
                .list();
    }

    public Long createAssignment(OperationalAssignmentRequest request, Long userId) {
        return jdbcClient.sql("""
                INSERT INTO app.asignaciones_operativas (
                    id_usuario, id_estacion, id_turno, fecha_operacion, rol_operativo, activa, creado_por
                )
                VALUES (:usuarioId, :estacionId, :turnoId, :fechaOperacion, :rolOperativo, :activa, :userId)
                RETURNING id_asignacion
                """)
                .param("usuarioId", request.usuarioId())
                .param("estacionId", request.estacionId())
                .param("turnoId", request.turnoId())
                .param("fechaOperacion", request.fechaOperacion())
                .param("rolOperativo", request.rolOperativo().trim().toUpperCase())
                .param("activa", request.activa() == null || request.activa())
                .param("userId", userId)
                .query(Long.class)
                .single();
    }

    public void updateAssignment(Long id, OperationalAssignmentRequest request, Long userId) {
        jdbcClient.sql("""
                UPDATE app.asignaciones_operativas
                SET id_usuario = :usuarioId,
                    id_estacion = :estacionId,
                    id_turno = :turnoId,
                    fecha_operacion = :fechaOperacion,
                    rol_operativo = :rolOperativo,
                    activa = :activa,
                    actualizado_por = :userId,
                    fecha_actualizacion = CURRENT_TIMESTAMP
                WHERE id_asignacion = :id
                """)
                .param("id", id)
                .param("usuarioId", request.usuarioId())
                .param("estacionId", request.estacionId())
                .param("turnoId", request.turnoId())
                .param("fechaOperacion", request.fechaOperacion())
                .param("rolOperativo", request.rolOperativo().trim().toUpperCase())
                .param("activa", request.activa() == null || request.activa())
                .param("userId", userId)
                .update();
    }

    public void deleteAssignment(Long id) {
        jdbcClient.sql("DELETE FROM app.asignaciones_operativas WHERE id_asignacion = :id")
                .param("id", id)
                .update();
    }

    public Optional<OperationalDay> findOpenOperationalDay() {
        return jdbcClient.sql("""
                SELECT j.id_jornada, j.fecha_jornada, j.estado, j.fecha_apertura, j.fecha_cierre,
                       j.apertura_por, au.username AS apertura_username,
                       concat_ws(' ', au.nombre, au.apellido_paterno, au.apellido_materno) AS apertura_nombre,
                       j.cierre_por, cu.username AS cierre_username, j.observaciones
                FROM app.jornadas_operativas j
                JOIN app.usuarios au ON au.id_usuario = j.apertura_por
                LEFT JOIN app.usuarios cu ON cu.id_usuario = j.cierre_por
                WHERE j.estado = 'ABIERTA'
                ORDER BY j.fecha_apertura DESC
                LIMIT 1
                """)
                .query((rs, rowNum) -> operationalDay(rs))
                .optional();
    }

    public Optional<OperationalDay> findOperationalDayByDate(LocalDate fechaJornada) {
        return jdbcClient.sql("""
                SELECT j.id_jornada, j.fecha_jornada, j.estado, j.fecha_apertura, j.fecha_cierre,
                       j.apertura_por, au.username AS apertura_username,
                       concat_ws(' ', au.nombre, au.apellido_paterno, au.apellido_materno) AS apertura_nombre,
                       j.cierre_por, cu.username AS cierre_username, j.observaciones
                FROM app.jornadas_operativas j
                JOIN app.usuarios au ON au.id_usuario = j.apertura_por
                LEFT JOIN app.usuarios cu ON cu.id_usuario = j.cierre_por
                WHERE j.fecha_jornada = :fechaJornada
                """)
                .param("fechaJornada", fechaJornada)
                .query((rs, rowNum) -> operationalDay(rs))
                .optional();
    }

    public Long openOperationalDay(LocalDate fechaJornada, String observaciones, Long userId) {
        return jdbcClient.sql("""
                INSERT INTO app.jornadas_operativas (fecha_jornada, estado, apertura_por, observaciones)
                VALUES (:fechaJornada, 'ABIERTA', :userId, :observaciones)
                RETURNING id_jornada
                """)
                .param("fechaJornada", fechaJornada)
                .param("userId", userId)
                .param("observaciones", blankToNull(observaciones))
                .query(Long.class)
                .single();
    }

    public Optional<TreasurySession> findCurrentTreasurySession() {
        return jdbcClient.sql("""
                SELECT tj.id_tesoreria_jornada, tj.id_jornada, j.fecha_jornada,
                       tj.id_estacion, e.nombre AS estacion_nombre, tj.estado,
                       tj.saldo_inicial, tj.saldo_actual, tj.fecha_apertura,
                       au.username AS apertura_username, tj.fecha_precierre,
                       pu.username AS precierre_username, tj.fecha_cierre,
                       cu.username AS cierre_username, tj.observaciones
                FROM app.tesorerias_jornada tj
                JOIN app.jornadas_operativas j ON j.id_jornada = tj.id_jornada
                JOIN app.estaciones_operativas e ON e.id_estacion = tj.id_estacion
                JOIN app.usuarios au ON au.id_usuario = tj.apertura_por
                LEFT JOIN app.usuarios pu ON pu.id_usuario = tj.precierre_por
                LEFT JOIN app.usuarios cu ON cu.id_usuario = tj.cierre_por
                WHERE tj.estado IN ('ABIERTA', 'PRECIERRE')
                ORDER BY tj.fecha_apertura DESC
                LIMIT 1
                """)
                .query((rs, rowNum) -> treasurySession(rs))
                .optional();
    }

    public Optional<TreasurySession> findTreasurySessionById(Long id) {
        return jdbcClient.sql("""
                SELECT tj.id_tesoreria_jornada, tj.id_jornada, j.fecha_jornada,
                       tj.id_estacion, e.nombre AS estacion_nombre, tj.estado,
                       tj.saldo_inicial, tj.saldo_actual, tj.fecha_apertura,
                       au.username AS apertura_username, tj.fecha_precierre,
                       pu.username AS precierre_username, tj.fecha_cierre,
                       cu.username AS cierre_username, tj.observaciones
                FROM app.tesorerias_jornada tj
                JOIN app.jornadas_operativas j ON j.id_jornada = tj.id_jornada
                JOIN app.estaciones_operativas e ON e.id_estacion = tj.id_estacion
                JOIN app.usuarios au ON au.id_usuario = tj.apertura_por
                LEFT JOIN app.usuarios pu ON pu.id_usuario = tj.precierre_por
                LEFT JOIN app.usuarios cu ON cu.id_usuario = tj.cierre_por
                WHERE tj.id_tesoreria_jornada = :id
                """)
                .param("id", id)
                .query((rs, rowNum) -> treasurySession(rs))
                .optional();
    }

    public boolean existsActiveWorkstation(Long id, String tipo) {
        Integer total = jdbcClient.sql("""
                SELECT count(*) FROM app.estaciones_operativas
                WHERE id_estacion = :id AND tipo = :tipo AND activa = TRUE
                """)
                .param("id", id)
                .param("tipo", tipo)
                .query(Integer.class)
                .single();
        return total != null && total > 0;
    }

    public Optional<Long> findCashierBoxIdForStation(Long stationId) {
        return jdbcClient.sql("""
                SELECT COALESCE(e.id_caja, cp.id_caja) AS id_caja
                FROM app.estaciones_operativas e
                LEFT JOIN LATERAL (
                    SELECT id_caja
                    FROM app.cajas_operativas
                    WHERE principal = TRUE AND activa = TRUE
                    ORDER BY id_caja
                    LIMIT 1
                ) cp ON TRUE
                WHERE e.id_estacion = :stationId
                  AND e.tipo = 'CAJA'
                  AND e.activa = TRUE
                """)
                .param("stationId", stationId)
                .query(Long.class)
                .optional();
    }

    public boolean existsActiveAssignment(Long userId, Long stationId, LocalDate fechaOperacion, String rolOperativo) {
        Integer total = jdbcClient.sql("""
                SELECT count(*)
                FROM app.asignaciones_operativas
                WHERE id_usuario = :userId
                  AND id_estacion = :stationId
                  AND fecha_operacion = :fechaOperacion
                  AND rol_operativo = :rolOperativo
                  AND activa = TRUE
                """)
                .param("userId", userId)
                .param("stationId", stationId)
                .param("fechaOperacion", fechaOperacion)
                .param("rolOperativo", rolOperativo)
                .query(Integer.class)
                .single();
        return total != null && total > 0;
    }

    public Optional<OperationalAssignment> findActiveAssignment(
            Long userId,
            Long stationId,
            Long shiftId,
            LocalDate fechaOperacion,
            String rolOperativo
    ) {
        return jdbcClient.sql("""
                SELECT a.id_asignacion, a.id_usuario, u.username,
                       concat_ws(' ', u.nombre, u.apellido_paterno, u.apellido_materno) AS nombre_usuario,
                       a.id_estacion, e.nombre AS estacion_nombre, e.tipo AS estacion_tipo,
                       a.id_turno, t.nombre AS turno_nombre, a.fecha_operacion, a.rol_operativo,
                       a.activa, a.fecha_creacion, a.fecha_actualizacion
                FROM app.asignaciones_operativas a
                JOIN app.usuarios u ON u.id_usuario = a.id_usuario
                JOIN app.estaciones_operativas e ON e.id_estacion = a.id_estacion
                JOIN app.turnos_caja t ON t.id_turno = a.id_turno
                WHERE a.id_usuario = :userId
                  AND a.id_estacion = :stationId
                  AND a.id_turno = :shiftId
                  AND a.fecha_operacion = :fechaOperacion
                  AND a.rol_operativo = :rolOperativo
                  AND a.activa = TRUE
                """)
                .param("userId", userId)
                .param("stationId", stationId)
                .param("shiftId", shiftId)
                .param("fechaOperacion", fechaOperacion)
                .param("rolOperativo", rolOperativo)
                .query((rs, rowNum) -> assignment(rs))
                .optional();
    }

    public Long openTreasurySession(Long jornadaId, TreasuryOpenRequest request, Long userId) {
        return jdbcClient.sql("""
                INSERT INTO app.tesorerias_jornada (
                    id_jornada, id_estacion, estado, saldo_inicial, saldo_actual, apertura_por, observaciones
                )
                VALUES (:jornadaId, :estacionId, 'ABIERTA', :saldoInicial, :saldoInicial, :userId, :observaciones)
                RETURNING id_tesoreria_jornada
                """)
                .param("jornadaId", jornadaId)
                .param("estacionId", request.estacionId())
                .param("saldoInicial", request.saldoInicial())
                .param("userId", userId)
                .param("observaciones", blankToNull(request.observaciones()))
                .query(Long.class)
                .single();
    }

    public Long createTreasuryMovement(Long treasurySessionId, String tipo, TreasuryMovementRequest request, Long userId) {
        return jdbcClient.sql("""
                INSERT INTO app.movimientos_tesoreria (
                    id_tesoreria_jornada, id_estacion_caja, id_turno, tipo, concepto, monto,
                    referencia, observaciones, registrado_por
                )
                VALUES (
                    :treasurySessionId, :estacionCajaId, :turnoId, :tipo, :concepto, :monto,
                    :referencia, :observaciones, :userId
                )
                RETURNING id_movimiento_tesoreria
                """)
                .param("treasurySessionId", treasurySessionId)
                .param("estacionCajaId", request.estacionCajaId())
                .param("turnoId", request.turnoId())
                .param("tipo", tipo)
                .param("concepto", request.concepto().trim())
                .param("monto", request.monto())
                .param("referencia", blankToNull(request.referencia()))
                .param("observaciones", blankToNull(request.observaciones()))
                .param("userId", userId)
                .query(Long.class)
                .single();
    }

    public void applyTreasuryBalance(Long treasurySessionId, BigDecimal amountDelta) {
        jdbcClient.sql("""
                UPDATE app.tesorerias_jornada
                SET saldo_actual = saldo_actual + :amountDelta,
                    fecha_actualizacion = CURRENT_TIMESTAMP
                WHERE id_tesoreria_jornada = :treasurySessionId
                """)
                .param("treasurySessionId", treasurySessionId)
                .param("amountDelta", amountDelta)
                .update();
    }

    public Long createTreasuryCardMovement(
            Long treasurySessionId,
            String tipo,
            TreasuryCardMovementRequest request,
            Integer cantidad,
            Long userId
    ) {
        return jdbcClient.sql("""
                INSERT INTO app.movimientos_tarjetas_tesoreria (
                    id_tesoreria_jornada, id_estacion_caja, id_turno, tipo, numero_inicial,
                    numero_final, cantidad, referencia, observaciones, registrado_por
                )
                VALUES (
                    :treasurySessionId, :estacionCajaId, :turnoId, :tipo, :numeroInicial,
                    :numeroFinal, :cantidad, :referencia, :observaciones, :userId
                )
                RETURNING id_movimiento_tarjetas
                """)
                .param("treasurySessionId", treasurySessionId)
                .param("estacionCajaId", request.estacionCajaId())
                .param("turnoId", request.turnoId())
                .param("tipo", tipo)
                .param("numeroInicial", request.numeroInicial().trim())
                .param("numeroFinal", request.numeroFinal().trim())
                .param("cantidad", cantidad)
                .param("referencia", blankToNull(request.referencia()))
                .param("observaciones", blankToNull(request.observaciones()))
                .param("userId", userId)
                .query(Long.class)
                .single();
    }

    public void precloseTreasurySession(Long id, String observaciones, Long userId) {
        jdbcClient.sql("""
                UPDATE app.tesorerias_jornada
                SET estado = 'PRECIERRE',
                    fecha_precierre = CURRENT_TIMESTAMP,
                    precierre_por = :userId,
                    observaciones = COALESCE(:observaciones, observaciones),
                    fecha_actualizacion = CURRENT_TIMESTAMP
                WHERE id_tesoreria_jornada = :id
                """)
                .param("id", id)
                .param("observaciones", blankToNull(observaciones))
                .param("userId", userId)
                .update();
    }

    public void closeTreasurySession(Long id, String observaciones, Long userId) {
        jdbcClient.sql("""
                UPDATE app.tesorerias_jornada
                SET estado = 'CERRADA',
                    fecha_cierre = CURRENT_TIMESTAMP,
                    cierre_por = :userId,
                    observaciones = COALESCE(:observaciones, observaciones),
                    fecha_actualizacion = CURRENT_TIMESTAMP
                WHERE id_tesoreria_jornada = :id
                """)
                .param("id", id)
                .param("observaciones", blankToNull(observaciones))
                .param("userId", userId)
                .update();
    }

    public List<TreasuryMovement> findTreasuryMovements(Long treasurySessionId) {
        return jdbcClient.sql("""
                SELECT m.id_movimiento_tesoreria, m.id_tesoreria_jornada, m.id_estacion_caja,
                       e.nombre AS estacion_caja_nombre, m.id_turno, t.nombre AS turno_nombre,
                       m.tipo, m.concepto, m.monto, m.referencia, m.observaciones,
                       m.registrado_por, u.username AS registrado_username, m.fecha_movimiento
                FROM app.movimientos_tesoreria m
                LEFT JOIN app.estaciones_operativas e ON e.id_estacion = m.id_estacion_caja
                LEFT JOIN app.turnos_caja t ON t.id_turno = m.id_turno
                JOIN app.usuarios u ON u.id_usuario = m.registrado_por
                WHERE (CAST(:treasurySessionId AS BIGINT) IS NULL OR m.id_tesoreria_jornada = :treasurySessionId)
                ORDER BY m.fecha_movimiento DESC
                LIMIT 300
                """)
                .param("treasurySessionId", treasurySessionId)
                .query((rs, rowNum) -> treasuryMovement(rs))
                .list();
    }

    public List<TreasuryCardMovement> findTreasuryCardMovements(Long treasurySessionId) {
        return jdbcClient.sql("""
                SELECT m.id_movimiento_tarjetas, m.id_tesoreria_jornada, m.id_estacion_caja,
                       e.nombre AS estacion_caja_nombre, m.id_turno, t.nombre AS turno_nombre,
                       m.tipo, m.numero_inicial, m.numero_final, m.cantidad, m.referencia,
                       m.observaciones, m.registrado_por, u.username AS registrado_username, m.fecha_movimiento
                FROM app.movimientos_tarjetas_tesoreria m
                JOIN app.estaciones_operativas e ON e.id_estacion = m.id_estacion_caja
                LEFT JOIN app.turnos_caja t ON t.id_turno = m.id_turno
                JOIN app.usuarios u ON u.id_usuario = m.registrado_por
                WHERE (CAST(:treasurySessionId AS BIGINT) IS NULL OR m.id_tesoreria_jornada = :treasurySessionId)
                ORDER BY m.fecha_movimiento DESC
                LIMIT 300
                """)
                .param("treasurySessionId", treasurySessionId)
                .query((rs, rowNum) -> treasuryCardMovement(rs))
                .list();
    }

    public List<TreasuryLedgerEntry> findTreasuryLedger() {
        return jdbcClient.sql("""
                SELECT *
                FROM (
                    SELECT
                        'MOV-' || m.id_movimiento_tesoreria AS id,
                        'EFECTIVO' AS categoria,
                        m.tipo,
                        m.concepto AS detalle,
                        COALESCE(e.nombre, te.nombre, 'Tesoreria') AS estacion,
                        t.nombre AS turno,
                        m.monto,
                        CAST(NULL AS INTEGER) AS cantidad,
                        m.referencia,
                        m.observaciones,
                        u.username,
                        m.fecha_movimiento AS fecha_evento
                    FROM app.movimientos_tesoreria m
                    JOIN app.tesorerias_jornada tj ON tj.id_tesoreria_jornada = m.id_tesoreria_jornada
                    JOIN app.estaciones_operativas te ON te.id_estacion = tj.id_estacion
                    LEFT JOIN app.estaciones_operativas e ON e.id_estacion = m.id_estacion_caja
                    LEFT JOIN app.turnos_caja t ON t.id_turno = m.id_turno
                    JOIN app.usuarios u ON u.id_usuario = m.registrado_por
                    UNION ALL
                    SELECT
                        'TAR-' || m.id_movimiento_tarjetas AS id,
                        'TARJETAS' AS categoria,
                        m.tipo,
                        m.numero_inicial || ' - ' || m.numero_final AS detalle,
                        e.nombre AS estacion,
                        t.nombre AS turno,
                        CAST(NULL AS NUMERIC(14,2)) AS monto,
                        m.cantidad,
                        m.referencia,
                        m.observaciones,
                        u.username,
                        m.fecha_movimiento AS fecha_evento
                    FROM app.movimientos_tarjetas_tesoreria m
                    JOIN app.estaciones_operativas e ON e.id_estacion = m.id_estacion_caja
                    LEFT JOIN app.turnos_caja t ON t.id_turno = m.id_turno
                    JOIN app.usuarios u ON u.id_usuario = m.registrado_por
                    UNION ALL
                    SELECT
                        'BIT-' || b.id_bitacora AS id,
                        'BITACORA' AS categoria,
                        b.accion AS tipo,
                        COALESCE(b.detalle, b.entidad) AS detalle,
                        NULL AS estacion,
                        NULL AS turno,
                        CAST(NULL AS NUMERIC(14,2)) AS monto,
                        CAST(NULL AS INTEGER) AS cantidad,
                        b.entidad || COALESCE(' #' || b.entidad_id, '') AS referencia,
                        b.ip_origen AS observaciones,
                        u.username,
                        b.fecha_evento
                    FROM app.bitacora_operativa b
                    LEFT JOIN app.usuarios u ON u.id_usuario = b.id_usuario
                    WHERE b.entidad IN (
                        'TESORERIA_JORNADA',
                        'MOVIMIENTO_TESORERIA',
                        'MOVIMIENTO_TARJETAS_TESORERIA'
                    )
                ) ledger
                ORDER BY fecha_evento DESC
                LIMIT 500
                """)
                .query((rs, rowNum) -> treasuryLedgerEntry(rs))
                .list();
    }

    public List<TreasuryMovement> findTreasuryMovementsForCashier(Long stationId, Long shiftId, Long jornadaId) {
        return jdbcClient.sql("""
                SELECT m.id_movimiento_tesoreria, m.id_tesoreria_jornada, m.id_estacion_caja,
                       e.nombre AS estacion_caja_nombre, m.id_turno, t.nombre AS turno_nombre,
                       m.tipo, m.concepto, m.monto, m.referencia, m.observaciones,
                       m.registrado_por, u.username AS registrado_username, m.fecha_movimiento
                FROM app.movimientos_tesoreria m
                JOIN app.tesorerias_jornada tj ON tj.id_tesoreria_jornada = m.id_tesoreria_jornada
                LEFT JOIN app.estaciones_operativas e ON e.id_estacion = m.id_estacion_caja
                LEFT JOIN app.turnos_caja t ON t.id_turno = m.id_turno
                JOIN app.usuarios u ON u.id_usuario = m.registrado_por
                WHERE m.id_estacion_caja = :stationId
                  AND (CAST(:shiftId AS BIGINT) IS NULL OR m.id_turno = :shiftId)
                  AND tj.id_jornada = :jornadaId
                ORDER BY m.fecha_movimiento DESC
                LIMIT 300
                """)
                .param("stationId", stationId)
                .param("shiftId", shiftId)
                .param("jornadaId", jornadaId)
                .query((rs, rowNum) -> treasuryMovement(rs))
                .list();
    }

    public List<TreasuryCardMovement> findTreasuryCardMovementsForCashier(Long stationId, Long shiftId, Long jornadaId) {
        return jdbcClient.sql("""
                SELECT m.id_movimiento_tarjetas, m.id_tesoreria_jornada, m.id_estacion_caja,
                       e.nombre AS estacion_caja_nombre, m.id_turno, t.nombre AS turno_nombre,
                       m.tipo, m.numero_inicial, m.numero_final, m.cantidad, m.referencia,
                       m.observaciones, m.registrado_por, u.username AS registrado_username, m.fecha_movimiento
                FROM app.movimientos_tarjetas_tesoreria m
                JOIN app.tesorerias_jornada tj ON tj.id_tesoreria_jornada = m.id_tesoreria_jornada
                JOIN app.estaciones_operativas e ON e.id_estacion = m.id_estacion_caja
                LEFT JOIN app.turnos_caja t ON t.id_turno = m.id_turno
                JOIN app.usuarios u ON u.id_usuario = m.registrado_por
                WHERE m.id_estacion_caja = :stationId
                  AND (CAST(:shiftId AS BIGINT) IS NULL OR m.id_turno = :shiftId)
                  AND tj.id_jornada = :jornadaId
                ORDER BY m.fecha_movimiento DESC
                LIMIT 300
                """)
                .param("stationId", stationId)
                .param("shiftId", shiftId)
                .param("jornadaId", jornadaId)
                .query((rs, rowNum) -> treasuryCardMovement(rs))
                .list();
    }

    public Optional<CashierSession> findCurrentCashierSession(Long userId) {
        return jdbcClient.sql("""
                SELECT cj.id_caja_jornada, cj.id_jornada, j.fecha_jornada,
                       cj.id_caja, c.nombre AS caja_nombre,
                       a.id_estacion, ae.nombre AS estacion_nombre, cj.id_turno,
                       t.nombre AS turno_nombre, cj.estado, cj.saldo_inicial,
                       cj.saldo_actual, cj.tarjetas_iniciales, cj.tarjetas_actuales,
                       cj.fecha_apertura, au.username AS apertura_username,
                       cj.fecha_precierre, pu.username AS precierre_username,
                       cj.fecha_cierre, cu.username AS cierre_username,
                       cj.monto_declarado_cierre, cj.tarjetas_devueltas_cierre,
                       cj.observaciones
                FROM app.cajas_jornada cj
                JOIN app.jornadas_operativas j ON j.id_jornada = cj.id_jornada
                JOIN app.cajas_operativas c ON c.id_caja = cj.id_caja
                JOIN app.turnos_caja t ON t.id_turno = cj.id_turno
                JOIN app.asignaciones_operativas a ON a.id_turno = cj.id_turno
                    AND a.fecha_operacion = j.fecha_jornada
                    AND a.rol_operativo = 'CAJERO'
                    AND a.activa = TRUE
                    AND a.id_usuario = :userId
                JOIN app.estaciones_operativas ae ON ae.id_estacion = a.id_estacion
                    AND ae.tipo = 'CAJA'
                    AND ae.activa = TRUE
                    AND COALESCE(ae.id_caja, cj.id_caja) = cj.id_caja
                JOIN app.usuarios au ON au.id_usuario = cj.apertura_por
                LEFT JOIN app.usuarios pu ON pu.id_usuario = cj.precierre_por
                LEFT JOIN app.usuarios cu ON cu.id_usuario = cj.cierre_por
                WHERE cj.estado IN ('ABIERTA', 'PRECIERRE')
                ORDER BY cj.fecha_apertura DESC
                LIMIT 1
                """)
                .param("userId", userId)
                .query((rs, rowNum) -> cashierSession(rs))
                .optional();
    }

    public Optional<CashierSession> findCashierSessionById(Long id) {
        return jdbcClient.sql("""
                SELECT cj.id_caja_jornada, cj.id_jornada, j.fecha_jornada,
                       cj.id_caja, c.nombre AS caja_nombre,
                       cj.id_estacion, e.nombre AS estacion_nombre, cj.id_turno,
                       t.nombre AS turno_nombre, cj.estado, cj.saldo_inicial,
                       cj.saldo_actual, cj.tarjetas_iniciales, cj.tarjetas_actuales,
                       cj.fecha_apertura, au.username AS apertura_username,
                       cj.fecha_precierre, pu.username AS precierre_username,
                       cj.fecha_cierre, cu.username AS cierre_username,
                       cj.monto_declarado_cierre, cj.tarjetas_devueltas_cierre,
                       cj.observaciones
                FROM app.cajas_jornada cj
                JOIN app.jornadas_operativas j ON j.id_jornada = cj.id_jornada
                JOIN app.cajas_operativas c ON c.id_caja = cj.id_caja
                JOIN app.estaciones_operativas e ON e.id_estacion = cj.id_estacion
                JOIN app.turnos_caja t ON t.id_turno = cj.id_turno
                JOIN app.usuarios au ON au.id_usuario = cj.apertura_por
                LEFT JOIN app.usuarios pu ON pu.id_usuario = cj.precierre_por
                LEFT JOIN app.usuarios cu ON cu.id_usuario = cj.cierre_por
                WHERE cj.id_caja_jornada = :id
                """)
                .param("id", id)
                .query((rs, rowNum) -> cashierSession(rs))
                .optional();
    }

    public List<CashierSession> findClosedCashierSessions(Long jornadaId) {
        return jdbcClient.sql("""
                SELECT cj.id_caja_jornada, cj.id_jornada, j.fecha_jornada,
                       cj.id_caja, c.nombre AS caja_nombre,
                       cj.id_estacion, e.nombre AS estacion_nombre, cj.id_turno,
                       t.nombre AS turno_nombre, cj.estado, cj.saldo_inicial,
                       cj.saldo_actual, cj.tarjetas_iniciales, cj.tarjetas_actuales,
                       cj.fecha_apertura, au.username AS apertura_username,
                       cj.fecha_precierre, pu.username AS precierre_username,
                       cj.fecha_cierre, cu.username AS cierre_username,
                       cj.monto_declarado_cierre, cj.tarjetas_devueltas_cierre,
                       cj.observaciones
                FROM app.cajas_jornada cj
                JOIN app.jornadas_operativas j ON j.id_jornada = cj.id_jornada
                JOIN app.cajas_operativas c ON c.id_caja = cj.id_caja
                JOIN app.estaciones_operativas e ON e.id_estacion = cj.id_estacion
                JOIN app.turnos_caja t ON t.id_turno = cj.id_turno
                JOIN app.usuarios au ON au.id_usuario = cj.apertura_por
                LEFT JOIN app.usuarios pu ON pu.id_usuario = cj.precierre_por
                LEFT JOIN app.usuarios cu ON cu.id_usuario = cj.cierre_por
                WHERE cj.id_jornada = :jornadaId
                  AND cj.estado = 'CERRADA'
                ORDER BY cj.fecha_cierre DESC
                """)
                .param("jornadaId", jornadaId)
                .query((rs, rowNum) -> cashierSession(rs))
                .list();
    }

    public Optional<CashierSession> findOpenCashierSession(Long jornadaId, Long cajaId, Long turnoId) {
        return jdbcClient.sql("""
                SELECT cj.id_caja_jornada, cj.id_jornada, j.fecha_jornada,
                       cj.id_caja, c.nombre AS caja_nombre,
                       cj.id_estacion, e.nombre AS estacion_nombre, cj.id_turno,
                       t.nombre AS turno_nombre, cj.estado, cj.saldo_inicial,
                       cj.saldo_actual, cj.tarjetas_iniciales, cj.tarjetas_actuales,
                       cj.fecha_apertura, au.username AS apertura_username,
                       cj.fecha_precierre, pu.username AS precierre_username,
                       cj.fecha_cierre, cu.username AS cierre_username,
                       cj.monto_declarado_cierre, cj.tarjetas_devueltas_cierre,
                       cj.observaciones
                FROM app.cajas_jornada cj
                JOIN app.jornadas_operativas j ON j.id_jornada = cj.id_jornada
                JOIN app.cajas_operativas c ON c.id_caja = cj.id_caja
                JOIN app.estaciones_operativas e ON e.id_estacion = cj.id_estacion
                JOIN app.turnos_caja t ON t.id_turno = cj.id_turno
                JOIN app.usuarios au ON au.id_usuario = cj.apertura_por
                LEFT JOIN app.usuarios pu ON pu.id_usuario = cj.precierre_por
                LEFT JOIN app.usuarios cu ON cu.id_usuario = cj.cierre_por
                WHERE cj.id_jornada = :jornadaId
                  AND cj.id_caja = :cajaId
                  AND cj.id_turno = :turnoId
                  AND cj.estado IN ('ABIERTA', 'PRECIERRE')
                ORDER BY cj.fecha_apertura DESC
                LIMIT 1
                """)
                .param("jornadaId", jornadaId)
                .param("cajaId", cajaId)
                .param("turnoId", turnoId)
                .query((rs, rowNum) -> cashierSession(rs))
                .optional();
    }

    public Long openCashierSession(Long jornadaId, Long cajaId, CashierOpenRequest request, Long userId) {
        return jdbcClient.sql("""
                INSERT INTO app.cajas_jornada (
                    id_jornada, id_caja, id_estacion, id_turno, estado, saldo_inicial, saldo_actual,
                    tarjetas_iniciales, tarjetas_actuales, apertura_por, observaciones
                )
                VALUES (
                    :jornadaId, :cajaId, :estacionId, :turnoId, 'ABIERTA', :montoApertura, :montoApertura,
                    :tarjetasApertura, :tarjetasApertura, :userId, :observaciones
                )
                RETURNING id_caja_jornada
                """)
                .param("jornadaId", jornadaId)
                .param("cajaId", cajaId)
                .param("estacionId", request.estacionId())
                .param("turnoId", request.turnoId())
                .param("montoApertura", request.montoApertura())
                .param("tarjetasApertura", request.tarjetasApertura())
                .param("userId", userId)
                .param("observaciones", blankToNull(request.observaciones()))
                .query(Long.class)
                .single();
    }

    public Long createCashierMovement(Long cashierSessionId, Long stationId, String tipo, CashierMovementRequest request, Long userId) {
        return jdbcClient.sql("""
                INSERT INTO app.movimientos_caja (
                    id_caja_jornada, id_estacion, tipo, numero_tarjeta, monto, maquina, motivo,
                    referencia, observaciones, registrado_por
                )
                VALUES (
                    :cashierSessionId, :stationId, :tipo, :numeroTarjeta, :monto, :maquina, :motivo,
                    :referencia, :observaciones, :userId
                )
                RETURNING id_movimiento_caja
                """)
                .param("cashierSessionId", cashierSessionId)
                .param("stationId", stationId)
                .param("tipo", tipo)
                .param("numeroTarjeta", blankToNull(request.numeroTarjeta()))
                .param("monto", request.monto())
                .param("maquina", blankToNull(request.maquina()))
                .param("motivo", blankToNull(request.motivo()))
                .param("referencia", blankToNull(request.referencia()))
                .param("observaciones", blankToNull(request.observaciones()))
                .param("userId", userId)
                .query(Long.class)
                .single();
    }

    public Long createCashierSale(Long cashierSessionId, Long stationId, CashierMovementRequest request, Long userId) {
        Long movementId = jdbcClient.sql("""
                SELECT app.registrar_venta_caja(
                    :cashierSessionId,
                    :numeroTarjeta,
                    :monto,
                    :referencia,
                    :observaciones,
                    :userId
                )
                """)
                .param("cashierSessionId", cashierSessionId)
                .param("numeroTarjeta", blankToNull(request.numeroTarjeta()))
                .param("monto", request.monto())
                .param("referencia", blankToNull(request.referencia()))
                .param("observaciones", blankToNull(request.observaciones()))
                .param("userId", userId)
                .query(Long.class)
                .single();
        assignCashierMovementStation(movementId, stationId);
        return movementId;
    }

    public void assignCashierMovementStation(Long movementId, Long stationId) {
        jdbcClient.sql("""
                UPDATE app.movimientos_caja
                SET id_estacion = :stationId
                WHERE id_movimiento_caja = :movementId
                """)
                .param("movementId", movementId)
                .param("stationId", stationId)
                .update();
    }

    public void applyCashierBalance(Long cashierSessionId, BigDecimal amountDelta) {
        jdbcClient.sql("""
                UPDATE app.cajas_jornada
                SET saldo_actual = saldo_actual + :amountDelta,
                    fecha_actualizacion = CURRENT_TIMESTAMP
                WHERE id_caja_jornada = :cashierSessionId
                """)
                .param("cashierSessionId", cashierSessionId)
                .param("amountDelta", amountDelta)
                .update();
    }

    public void precloseCashierSession(Long id, String observaciones, Long userId) {
        jdbcClient.sql("""
                UPDATE app.cajas_jornada
                SET estado = 'PRECIERRE',
                    fecha_precierre = CURRENT_TIMESTAMP,
                    precierre_por = :userId,
                    observaciones = COALESCE(:observaciones, observaciones),
                    fecha_actualizacion = CURRENT_TIMESTAMP
                WHERE id_caja_jornada = :id
                """)
                .param("id", id)
                .param("observaciones", blankToNull(observaciones))
                .param("userId", userId)
                .update();
    }

    public void closeCashierSession(Long id, CashierCloseRequest request, Long userId) {
        jdbcClient.sql("""
                SELECT app.cerrar_caja_jornada(
                    :id,
                    :montoDeclarado,
                    :tarjetasDevueltas,
                    :observaciones,
                    :userId
                )
                """)
                .param("id", id)
                .param("montoDeclarado", request.montoDeclarado())
                .param("tarjetasDevueltas", request.tarjetasDevueltas())
                .param("observaciones", blankToNull(request.observaciones()))
                .param("userId", userId)
                .query(Long.class)
                .single();
    }

    public int countActiveEgmCapturesForCashier(Long cashierSessionId) {
        return jdbcClient.sql("""
                SELECT COUNT(*)
                FROM app.capturas_rfid_egm
                WHERE id_caja_jornada = :cashierSessionId
                  AND estado IN ('AUTORIZADA', 'CONFIRMADA')
                """)
                .param("cashierSessionId", cashierSessionId)
                .query(Integer.class)
                .single();
    }

    public List<CashierMovement> findCashierMovements(Long cashierSessionId) {
        return jdbcClient.sql("""
                SELECT m.id_movimiento_caja, m.id_caja_jornada, m.id_estacion, e.nombre AS estacion_nombre,
                       m.tipo, m.numero_tarjeta,
                       m.monto, m.impacto_saldo, m.maquina, m.motivo, m.referencia, m.observaciones,
                       m.registrado_por, u.username AS registrado_username, m.fecha_movimiento
                FROM app.movimientos_caja m
                LEFT JOIN app.estaciones_operativas e ON e.id_estacion = m.id_estacion
                JOIN app.usuarios u ON u.id_usuario = m.registrado_por
                WHERE (CAST(:cashierSessionId AS BIGINT) IS NULL OR m.id_caja_jornada = :cashierSessionId)
                ORDER BY m.fecha_movimiento DESC
                LIMIT 300
                """)
                .param("cashierSessionId", cashierSessionId)
                .query((rs, rowNum) -> cashierMovement(rs))
                .list();
    }

    public List<CustomerRegistration> findCashierCustomerRegistrations(Long cashierSessionId) {
        return jdbcClient.sql("""
                SELECT c.id_cliente, c.nombre, c.apellido_paterno, c.apellido_materno,
                       c.telefono, c.email, c.fecha_nacimiento, c.documento_identidad,
                       c.estado AS estado_cliente, t.id_tarjeta, t.numero_tarjeta,
                       CASE WHEN t.capturada_egm THEN 'CAPTURADA_EGM' ELSE t.estado END AS estado_tarjeta,
                       ct.id_cliente_tarjeta, ct.id_caja_jornada,
                       e.nombre AS caja_nombre, cj.id_turno, tc.nombre AS turno_nombre,
                       ct.asignado_por, u.username AS asignado_username, ct.fecha_asignacion,
                       ct.observaciones
                FROM app.cliente_tarjetas ct
                JOIN app.clientes_operativos c ON c.id_cliente = ct.id_cliente
                JOIN app.tarjetas_operativas t ON t.id_tarjeta = ct.id_tarjeta
                LEFT JOIN app.cajas_jornada cj ON cj.id_caja_jornada = ct.id_caja_jornada
                LEFT JOIN app.estaciones_operativas e ON e.id_estacion = cj.id_estacion
                LEFT JOIN app.turnos_caja tc ON tc.id_turno = cj.id_turno
                JOIN app.usuarios u ON u.id_usuario = ct.asignado_por
                WHERE (CAST(:cashierSessionId AS BIGINT) IS NULL OR ct.id_caja_jornada = :cashierSessionId)
                ORDER BY ct.fecha_asignacion DESC
                LIMIT 300
                """)
                .param("cashierSessionId", cashierSessionId)
                .query((rs, rowNum) -> customerRegistration(rs))
                .list();
    }

    public Optional<CustomerRegistration> findCustomerByDocument(String documentoIdentidad) {
        String normalized = blankToNull(documentoIdentidad);
        if (normalized == null) {
            return Optional.empty();
        }
        return jdbcClient.sql("""
                SELECT c.id_cliente, c.nombre, c.apellido_paterno, c.apellido_materno,
                       c.telefono, c.email, c.fecha_nacimiento, c.documento_identidad,
                       c.estado AS estado_cliente, t.id_tarjeta, t.numero_tarjeta,
                       CASE WHEN t.capturada_egm THEN 'CAPTURADA_EGM' ELSE t.estado END AS estado_tarjeta,
                       ct.id_cliente_tarjeta, ct.id_caja_jornada,
                       e.nombre AS caja_nombre, cj.id_turno, tc.nombre AS turno_nombre,
                       ct.asignado_por, u.username AS asignado_username, ct.fecha_asignacion,
                       ct.observaciones
                FROM app.clientes_operativos c
                LEFT JOIN app.cliente_tarjetas ct ON ct.id_cliente = c.id_cliente AND ct.estado = 'ACTIVA'
                LEFT JOIN app.tarjetas_operativas t ON t.id_tarjeta = ct.id_tarjeta
                LEFT JOIN app.cajas_jornada cj ON cj.id_caja_jornada = ct.id_caja_jornada
                LEFT JOIN app.estaciones_operativas e ON e.id_estacion = cj.id_estacion
                LEFT JOIN app.turnos_caja tc ON tc.id_turno = cj.id_turno
                LEFT JOIN app.usuarios u ON u.id_usuario = ct.asignado_por
                WHERE upper(c.documento_identidad) = upper(:documentoIdentidad)
                ORDER BY ct.fecha_asignacion DESC NULLS LAST
                LIMIT 1
                """)
                .param("documentoIdentidad", normalized)
                .query((rs, rowNum) -> customerRegistration(rs))
                .optional();
    }

    public List<OperationalCard> findCards(String search) {
        String normalized = blankToNull(search);
        return jdbcClient.sql("""
                SELECT t.id_tarjeta, t.numero_tarjeta, t.tipo, t.fecha_vencimiento,
                       CASE WHEN t.capturada_egm THEN 'CAPTURADA_EGM' ELSE t.estado END AS estado,
                       CONCAT_WS(' ', c.nombre, c.apellido_paterno, c.apellido_materno) AS cliente_nombre,
                       t.capturada_egm, t.captura_egm_txid, t.captura_egm_fecha,
                       t.fecha_creacion, t.fecha_actualizacion
                FROM app.tarjetas_operativas t
                LEFT JOIN app.cliente_tarjetas ct ON ct.id_tarjeta = t.id_tarjeta AND ct.estado = 'ACTIVA'
                LEFT JOIN app.clientes_operativos c ON c.id_cliente = ct.id_cliente
                WHERE (CAST(:search AS VARCHAR) IS NULL OR t.numero_tarjeta LIKE :pattern)
                ORDER BY t.id_tarjeta DESC
                LIMIT 300
                """)
                .param("search", normalized)
                .param("pattern", normalized == null ? null : "%" + normalized + "%")
                .query((rs, rowNum) -> card(rs))
                .list();
    }

    public Optional<OperationalCard> findCardByNumber(String numeroTarjeta) {
        return jdbcClient.sql("""
                SELECT t.id_tarjeta, t.numero_tarjeta, t.tipo, t.fecha_vencimiento,
                       CASE WHEN t.capturada_egm THEN 'CAPTURADA_EGM' ELSE t.estado END AS estado,
                       CONCAT_WS(' ', c.nombre, c.apellido_paterno, c.apellido_materno) AS cliente_nombre,
                       t.capturada_egm, t.captura_egm_txid, t.captura_egm_fecha,
                       t.fecha_creacion, t.fecha_actualizacion
                FROM app.tarjetas_operativas t
                LEFT JOIN app.cliente_tarjetas ct ON ct.id_tarjeta = t.id_tarjeta AND ct.estado = 'ACTIVA'
                LEFT JOIN app.clientes_operativos c ON c.id_cliente = ct.id_cliente
                WHERE t.numero_tarjeta = :numeroTarjeta
                """)
                .param("numeroTarjeta", numeroTarjeta.trim())
                .query((rs, rowNum) -> card(rs))
                .optional();
    }

    public BigDecimal findCardBalance(String numeroTarjeta) {
        return jdbcClient.sql("""
                SELECT COALESCE(SUM(
                    COALESCE(impacto_saldo,
                        CASE
                            WHEN tipo IN ('VENTA', 'CORTESIA', 'PROMOCIONAL', 'TRANSACCION_ESPECIAL') THEN monto
                            WHEN tipo IN ('PAGO', 'PAGO_MANUAL', 'DEVOLUCION') THEN -monto
                            ELSE 0
                        END
                    )
                ), 0)
                FROM app.movimientos_caja
                WHERE numero_tarjeta = :numeroTarjeta
                """)
                .param("numeroTarjeta", numeroTarjeta.trim())
                .query(BigDecimal.class)
                .single();
    }

    public boolean existsActiveEgmCapture(String numeroTarjeta) {
        return Boolean.TRUE.equals(jdbcClient.sql("""
                SELECT EXISTS (
                    SELECT 1
                    FROM app.tarjetas_operativas t
                    WHERE t.numero_tarjeta = :numeroTarjeta
                      AND (
                          t.capturada_egm = TRUE
                          OR EXISTS (
                              SELECT 1
                              FROM app.capturas_rfid_egm c
                              WHERE c.id_tarjeta = t.id_tarjeta
                                AND c.estado IN ('AUTORIZADA', 'CONFIRMADA')
                          )
                      )
                )
                """)
                .param("numeroTarjeta", numeroTarjeta.trim())
                .query(Boolean.class)
                .single());
    }

    public Long createCustomer(CustomerRegistrationRequest request, Long userId) {
        return jdbcClient.sql("""
                INSERT INTO app.clientes_operativos (
                    nombre, apellido_paterno, apellido_materno, telefono, email,
                    fecha_nacimiento, documento_identidad, observaciones, creado_por
                )
                VALUES (
                    :nombre, :apellidoPaterno, :apellidoMaterno, :telefono, :email,
                    :fechaNacimiento, :documentoIdentidad, :observaciones, :userId
                )
                RETURNING id_cliente
                """)
                .param("nombre", request.nombre().trim())
                .param("apellidoPaterno", blankToNull(request.apellidoPaterno()))
                .param("apellidoMaterno", blankToNull(request.apellidoMaterno()))
                .param("telefono", blankToNull(request.telefono()))
                .param("email", blankToNull(request.email()))
                .param("fechaNacimiento", request.fechaNacimiento())
                .param("documentoIdentidad", blankToNull(request.documentoIdentidad()))
                .param("observaciones", blankToNull(request.observaciones()))
                .param("userId", userId)
                .query(Long.class)
                .single();
    }

    public boolean markCardAssigned(Long cardId, Long userId) {
        int updated = jdbcClient.sql("""
                UPDATE app.tarjetas_operativas
                SET estado = 'ASIGNADA',
                    actualizado_por = :userId,
                    fecha_actualizacion = CURRENT_TIMESTAMP
                WHERE id_tarjeta = :cardId
                  AND estado = 'DISPONIBLE'
                """)
                .param("cardId", cardId)
                .param("userId", userId)
                .update();
        return updated == 1;
    }

    public Long assignCardToCustomer(
            Long customerId,
            Long cardId,
            Long cashierSessionId,
            String nipHash,
            String observaciones,
            Long userId
    ) {
        return jdbcClient.sql("""
                INSERT INTO app.cliente_tarjetas (
                    id_cliente, id_tarjeta, id_caja_jornada, tipo_asignacion, estado,
                    nip_hash, observaciones, asignado_por
                )
                VALUES (
                    :customerId, :cardId, :cashierSessionId, 'ALTA_CLIENTE', 'ACTIVA',
                    :nipHash, :observaciones, :userId
                )
                RETURNING id_cliente_tarjeta
                """)
                .param("customerId", customerId)
                .param("cardId", cardId)
                .param("cashierSessionId", cashierSessionId)
                .param("nipHash", blankToNull(nipHash))
                .param("observaciones", blankToNull(observaciones))
                .param("userId", userId)
                .query(Long.class)
                .single();
    }

    public Long createCard(OperationalCardRequest request, Long userId) {
        return jdbcClient.sql("""
                INSERT INTO app.tarjetas_operativas (
                    numero_tarjeta, tipo, fecha_vencimiento, estado, creado_por
                )
                VALUES (:numeroTarjeta, :tipo, :fechaVencimiento, :estado, :userId)
                RETURNING id_tarjeta
                """)
                .param("numeroTarjeta", request.numeroTarjeta().trim())
                .param("tipo", request.tipo().trim().toUpperCase())
                .param("fechaVencimiento", request.fechaVencimiento())
                .param("estado", request.estado() == null || request.estado().isBlank()
                        ? "DISPONIBLE"
                        : request.estado().trim().toUpperCase())
                .param("userId", userId)
                .query(Long.class)
                .single();
    }

    public void updateCard(Long id, OperationalCardRequest request, Long userId) {
        jdbcClient.sql("""
                UPDATE app.tarjetas_operativas
                SET numero_tarjeta = :numeroTarjeta,
                    tipo = :tipo,
                    fecha_vencimiento = :fechaVencimiento,
                    estado = :estado,
                    actualizado_por = :userId,
                    fecha_actualizacion = CURRENT_TIMESTAMP
                WHERE id_tarjeta = :id
                """)
                .param("id", id)
                .param("numeroTarjeta", request.numeroTarjeta().trim())
                .param("tipo", request.tipo().trim().toUpperCase())
                .param("fechaVencimiento", request.fechaVencimiento())
                .param("estado", request.estado() == null || request.estado().isBlank()
                        ? "DISPONIBLE"
                        : request.estado().trim().toUpperCase())
                .param("userId", userId)
                .update();
    }

    public void deleteCard(Long id) {
        jdbcClient.sql("DELETE FROM app.tarjetas_operativas WHERE id_tarjeta = :id")
                .param("id", id)
                .update();
    }

    public void audit(String accion, String entidad, Long entidadId, String detalle, Long userId, String ipOrigen) {
        jdbcClient.sql("""
                INSERT INTO app.bitacora_operativa (accion, entidad, entidad_id, detalle, id_usuario, ip_origen)
                VALUES (:accion, :entidad, :entidadId, :detalle, :userId, :ipOrigen)
                """)
                .param("accion", accion)
                .param("entidad", entidad)
                .param("entidadId", entidadId)
                .param("detalle", detalle)
                .param("userId", userId)
                .param("ipOrigen", blankToNull(ipOrigen))
                .update();
    }

    public List<OperationalAuditEvent> findAuditEvents(String entidad, Long entidadId) {
        return jdbcClient.sql("""
                SELECT b.id_bitacora, b.accion, b.entidad, b.entidad_id, b.detalle, b.id_usuario,
                       u.username, b.fecha_evento, b.ip_origen
                FROM app.bitacora_operativa b
                LEFT JOIN app.usuarios u ON u.id_usuario = b.id_usuario
                WHERE (CAST(:entidad AS VARCHAR) IS NULL OR b.entidad = :entidad)
                  AND (CAST(:entidadId AS BIGINT) IS NULL OR b.entidad_id = :entidadId)
                ORDER BY b.fecha_evento DESC
                LIMIT 300
                """)
                .param("entidad", blankToNull(entidad))
                .param("entidadId", entidadId)
                .query((rs, rowNum) -> auditEvent(rs))
                .list();
    }

    public List<EgmMachine> findActiveEgms() {
        return jdbcClient.sql("""
                SELECT id_egm, egm_addr, nombre, sala, activo, raspberry_base_url, denominacion, timeout_segundos
                FROM app.egms_operativas
                WHERE activo = TRUE
                ORDER BY egm_addr
                """)
                .query((rs, rowNum) -> egmMachine(rs))
                .list();
    }

    public Long createSasMeterSnapshot(Long jornadaId, EgmMachine egm, String snapshotType,
                                       EgmMetersResponse response, Long userId) {
        Map<String, Long> meters = response.meters() == null ? Map.of() : response.meters();
        return jdbcClient.sql("""
                INSERT INTO app.egm_meter_snapshots (
                    id_jornada, id_egm, tipo_snapshot, estado, proveedor,
                    coin_in, coin_out, jackpot, handpay_cancelled, cancelled,
                    games_played, games_won, games_lost, bills_accepted, current_credits,
                    raw_response, mensaje, creado_por
                )
                VALUES (
                    :jornadaId, :egmId, :snapshotType, 'OK', 'SAS_RASPBERRY',
                    :coinIn, :coinOut, :jackpot, :handpayCancelled, :cancelled,
                    :gamesPlayed, :gamesWon, :gamesLost, :billsAccepted, :currentCredits,
                    :rawResponse, :mensaje, :userId
                )
                ON CONFLICT (id_jornada, id_egm, tipo_snapshot) WHERE tipo_snapshot IN ('APERTURA', 'CIERRE')
                DO UPDATE SET
                    estado = EXCLUDED.estado,
                    proveedor = EXCLUDED.proveedor,
                    coin_in = EXCLUDED.coin_in,
                    coin_out = EXCLUDED.coin_out,
                    jackpot = EXCLUDED.jackpot,
                    handpay_cancelled = EXCLUDED.handpay_cancelled,
                    cancelled = EXCLUDED.cancelled,
                    games_played = EXCLUDED.games_played,
                    games_won = EXCLUDED.games_won,
                    games_lost = EXCLUDED.games_lost,
                    bills_accepted = EXCLUDED.bills_accepted,
                    current_credits = EXCLUDED.current_credits,
                    raw_response = EXCLUDED.raw_response,
                    mensaje = EXCLUDED.mensaje,
                    fecha_snapshot = CURRENT_TIMESTAMP,
                    creado_por = EXCLUDED.creado_por
                RETURNING id_snapshot
                """)
                .param("jornadaId", jornadaId)
                .param("egmId", egm.id())
                .param("snapshotType", snapshotType)
                .param("coinIn", meter(meters, "coinIn"))
                .param("coinOut", meter(meters, "coinOut"))
                .param("jackpot", meter(meters, "jackpot"))
                .param("handpayCancelled", meter(meters, "handpayCancelled"))
                .param("cancelled", meter(meters, "cancelled"))
                .param("gamesPlayed", meter(meters, "gamesPlayed"))
                .param("gamesWon", meter(meters, "gamesWon"))
                .param("gamesLost", meter(meters, "gamesLost"))
                .param("billsAccepted", firstMeter(meters, "billsAccepted", "totalDrop"))
                .param("currentCredits", meter(meters, "currentCredits"))
                .param("rawResponse", rawMetersResponse(response))
                .param("mensaje", "Snapshot real SAS $1C capturado desde Raspberry/backend EGM")
                .param("userId", userId)
                .query(Long.class)
                .single();
    }

    public Long createSnapshotFailure(Long jornadaId, EgmMachine egm, String snapshotType, String message, Long userId) {
        return jdbcClient.sql("""
                INSERT INTO app.egm_meter_snapshots (
                    id_jornada, id_egm, tipo_snapshot, estado, proveedor, raw_response, mensaje, creado_por
                )
                VALUES (
                    :jornadaId, :egmId, :snapshotType, 'SIN_RESPUESTA', 'SAS_RASPBERRY',
                    :rawResponse, :message, :userId
                )
                ON CONFLICT (id_jornada, id_egm, tipo_snapshot) WHERE tipo_snapshot IN ('APERTURA', 'CIERRE')
                DO UPDATE SET
                    estado = EXCLUDED.estado,
                    proveedor = EXCLUDED.proveedor,
                    raw_response = EXCLUDED.raw_response,
                    mensaje = EXCLUDED.mensaje,
                    fecha_snapshot = CURRENT_TIMESTAMP,
                    creado_por = EXCLUDED.creado_por
                RETURNING id_snapshot
                """)
                .param("jornadaId", jornadaId)
                .param("egmId", egm.id())
                .param("snapshotType", snapshotType)
                .param("rawResponse", "{\"error\":\"" + safeJson(message) + "\"}")
                .param("message", message == null ? "Sin respuesta SAS/Raspberry" : message)
                .param("userId", userId)
                .query(Long.class)
                .single();
    }

    public void createOpeningSnapshots(Long jornadaId, Long userId) {
        jdbcClient.sql("""
                INSERT INTO app.egm_meter_snapshots (
                    id_jornada, id_egm, tipo_snapshot, estado, proveedor, mensaje, raw_response, creado_por
                )
                SELECT :jornadaId, e.id_egm, 'APERTURA', 'SIMULADO', 'INTERNO_SIMULADO',
                       'Snapshot de apertura preparado para conciliacion de cierre',
                       '{"source":"internal","type":"opening-baseline"}',
                       :userId
                FROM app.egms_operativas e
                WHERE e.activo = TRUE
                ON CONFLICT DO NOTHING
                """)
                .param("jornadaId", jornadaId)
                .param("userId", userId)
                .update();
    }

    public Long createOperationalClose(Long jornadaId, String observaciones, Long userId) {
        return jdbcClient.sql("""
                INSERT INTO app.cierres_operacion (
                    id_jornada, estado, porcentaje, observaciones, iniciado_por
                )
                VALUES (:jornadaId, 'EN_PROCESO', 5, :observaciones, :userId)
                ON CONFLICT (id_jornada) DO UPDATE SET
                    estado = 'EN_PROCESO',
                    porcentaje = 5,
                    observaciones = COALESCE(EXCLUDED.observaciones, app.cierres_operacion.observaciones),
                    iniciado_por = EXCLUDED.iniciado_por,
                    fecha_inicio = CURRENT_TIMESTAMP,
                    fecha_fin = NULL
                RETURNING id_cierre_operacion
                """)
                .param("jornadaId", jornadaId)
                .param("observaciones", blankToNull(observaciones))
                .param("userId", userId)
                .query(Long.class)
                .single();
    }

    public int countOpenCashierSessions(Long jornadaId) {
        Integer total = jdbcClient.sql("""
                SELECT count(*)
                FROM app.cajas_jornada
                WHERE id_jornada = :jornadaId
                  AND estado IN ('ABIERTA', 'PRECIERRE')
                """)
                .param("jornadaId", jornadaId)
                .query(Integer.class)
                .single();
        return total == null ? 0 : total;
    }

    public int countOpenTreasurySessions(Long jornadaId) {
        Integer total = jdbcClient.sql("""
                SELECT count(*)
                FROM app.tesorerias_jornada
                WHERE id_jornada = :jornadaId
                  AND estado IN ('ABIERTA', 'PRECIERRE')
                """)
                .param("jornadaId", jornadaId)
                .query(Integer.class)
                .single();
        return total == null ? 0 : total;
    }

    public int countActiveEgmCapturesForDay(Long jornadaId) {
        Integer total = jdbcClient.sql("""
                SELECT count(*)
                FROM app.capturas_rfid_egm c
                JOIN app.cajas_jornada cj ON cj.id_caja_jornada = c.id_caja_jornada
                WHERE cj.id_jornada = :jornadaId
                  AND c.estado IN ('AUTORIZADA', 'CONFIRMADA')
                """)
                .param("jornadaId", jornadaId)
                .query(Integer.class)
                .single();
        return total == null ? 0 : total;
    }

    public int countInvalidOpeningSnapshots(Long jornadaId) {
        Integer total = jdbcClient.sql("""
                SELECT count(*)
                FROM app.egms_operativas e
                LEFT JOIN app.egm_meter_snapshots s
                  ON s.id_jornada = :jornadaId
                 AND s.id_egm = e.id_egm
                 AND s.tipo_snapshot = 'APERTURA'
                WHERE e.activo = TRUE
                  AND (
                    s.id_snapshot IS NULL
                    OR s.estado <> 'OK'
                    OR s.proveedor <> 'SAS_RASPBERRY'
                  )
                """)
                .param("jornadaId", jornadaId)
                .query(Integer.class)
                .single();
        return total == null ? 0 : total;
    }

    public Long createClosingSnapshot(Long jornadaId, EgmMachine egm, Long userId) {
        return jdbcClient.sql("""
                WITH caja AS (
                    SELECT
                        COALESCE(SUM(m.monto) FILTER (WHERE m.tipo IN ('VENTA', 'CORTESIA', 'PROMOCIONAL', 'TRANSACCION_ESPECIAL')), 0) AS coin_in,
                        COALESCE(SUM(m.monto) FILTER (WHERE m.tipo IN ('PAGO', 'PAGO_MANUAL')), 0) AS coin_out,
                        COALESCE(SUM(m.monto) FILTER (WHERE m.tipo = 'PAGO_MANUAL'), 0) AS jackpot,
                        COALESCE(SUM(m.monto) FILTER (WHERE m.tipo = 'DEVOLUCION'), 0) AS cancelled,
                        COALESCE(SUM(m.monto) FILTER (WHERE m.tipo = 'VENTA'), 0) AS bills_accepted,
                        COALESCE(COUNT(m.id_movimiento_caja), 0) AS games_played,
                        COALESCE(COUNT(m.id_movimiento_caja) FILTER (WHERE m.tipo IN ('PAGO', 'PAGO_MANUAL')), 0) AS games_won,
                        COALESCE(COUNT(m.id_movimiento_caja) FILTER (WHERE m.tipo IN ('VENTA', 'TRANSACCION_ESPECIAL')), 0) AS games_lost
                    FROM app.movimientos_caja m
                    JOIN app.cajas_jornada cj ON cj.id_caja_jornada = m.id_caja_jornada
                    WHERE cj.id_jornada = :jornadaId
                      AND (
                        upper(COALESCE(m.maquina, '')) = upper(:egmAddr)
                        OR upper(COALESCE(m.maquina, '')) = upper(:egmNombre)
                      )
                ),
                creditos AS (
                    SELECT COALESCE(SUM(c.saldo_autorizado), 0) AS current_credits
                    FROM app.capturas_rfid_egm c
                    JOIN app.cajas_jornada cj ON cj.id_caja_jornada = c.id_caja_jornada
                    WHERE cj.id_jornada = :jornadaId
                      AND upper(COALESCE(c.egm_addr, '')) = upper(:egmAddr)
                      AND c.estado IN ('AUTORIZADA', 'CONFIRMADA')
                )
                INSERT INTO app.egm_meter_snapshots (
                    id_jornada, id_egm, tipo_snapshot, estado, proveedor,
                    coin_in, coin_out, jackpot, cancelled, games_played, games_won, games_lost,
                    bills_accepted, current_credits, raw_response, mensaje, creado_por
                )
                SELECT
                    :jornadaId,
                    :egmId,
                    'CIERRE',
                    'SIMULADO',
                    'INTERNO_SIMULADO',
                    caja.coin_in,
                    caja.coin_out,
                    caja.jackpot,
                    caja.cancelled,
                    caja.games_played,
                    caja.games_won,
                    caja.games_lost,
                    caja.bills_accepted,
                    creditos.current_credits,
                    json_build_object(
                        'source', 'internal',
                        'egmAddr', :egmAddr,
                        'coinIn', caja.coin_in,
                        'coinOut', caja.coin_out,
                        'currentCredits', creditos.current_credits
                    )::text,
                    'Snapshot de cierre generado con datos internos; reemplazable por meters SAS',
                    :userId
                FROM caja, creditos
                ON CONFLICT (id_jornada, id_egm, tipo_snapshot) WHERE tipo_snapshot IN ('APERTURA', 'CIERRE')
                DO UPDATE SET
                    estado = EXCLUDED.estado,
                    proveedor = EXCLUDED.proveedor,
                    coin_in = EXCLUDED.coin_in,
                    coin_out = EXCLUDED.coin_out,
                    jackpot = EXCLUDED.jackpot,
                    cancelled = EXCLUDED.cancelled,
                    games_played = EXCLUDED.games_played,
                    games_won = EXCLUDED.games_won,
                    games_lost = EXCLUDED.games_lost,
                    bills_accepted = EXCLUDED.bills_accepted,
                    current_credits = EXCLUDED.current_credits,
                    raw_response = EXCLUDED.raw_response,
                    mensaje = EXCLUDED.mensaje,
                    fecha_snapshot = CURRENT_TIMESTAMP,
                    creado_por = EXCLUDED.creado_por
                RETURNING id_snapshot
                """)
                .param("jornadaId", jornadaId)
                .param("egmId", egm.id())
                .param("egmAddr", egm.egmAddr())
                .param("egmNombre", egm.nombre())
                .param("userId", userId)
                .query(Long.class)
                .single();
    }

    public void reconcileEgm(Long jornadaId, EgmMachine egm) {
        jdbcClient.sql("""
                WITH apertura AS (
                    SELECT *
                    FROM app.egm_meter_snapshots
                    WHERE id_jornada = :jornadaId
                      AND id_egm = :egmId
                      AND tipo_snapshot = 'APERTURA'
                    LIMIT 1
                ),
                cierre AS (
                    SELECT *
                    FROM app.egm_meter_snapshots
                    WHERE id_jornada = :jornadaId
                      AND id_egm = :egmId
                      AND tipo_snapshot = 'CIERRE'
                    LIMIT 1
                ),
                caja AS (
                    SELECT
                        COALESCE(SUM(m.monto) FILTER (WHERE m.tipo IN ('VENTA', 'CORTESIA', 'PROMOCIONAL', 'TRANSACCION_ESPECIAL')), 0) AS host_loads,
                        COALESCE(SUM(m.monto) FILTER (WHERE m.tipo IN ('PAGO', 'PAGO_MANUAL')), 0) AS host_cashouts,
                        COALESCE(SUM(m.monto) FILTER (WHERE m.tipo IN ('VENTA', 'TRANSACCION_ESPECIAL')), 0)
                            - COALESCE(SUM(m.monto) FILTER (WHERE m.tipo IN ('PAGO', 'PAGO_MANUAL')), 0) AS caja_reportado
                    FROM app.movimientos_caja m
                    JOIN app.cajas_jornada cj ON cj.id_caja_jornada = m.id_caja_jornada
                    WHERE cj.id_jornada = :jornadaId
                      AND (
                        upper(COALESCE(m.maquina, '')) = upper(:egmAddr)
                        OR upper(COALESCE(m.maquina, '')) = upper(:egmNombre)
                      )
                ),
                calculo AS (
                    SELECT
                        cierre.id_snapshot AS id_snapshot_cierre,
                        apertura.id_snapshot AS id_snapshot_apertura,
                        apertura.estado AS estado_apertura,
                        cierre.estado AS estado_cierre,
                        apertura.proveedor AS proveedor_apertura,
                        cierre.proveedor AS proveedor_cierre,
                        cierre.coin_in - apertura.coin_in AS coin_in_delta,
                        cierre.coin_out - apertura.coin_out AS coin_out_delta,
                        cierre.jackpot - apertura.jackpot AS jackpot_delta,
                        cierre.cancelled - apertura.cancelled AS cancelled_delta,
                        cierre.bills_accepted - apertura.bills_accepted AS bills_accepted_delta,
                        cierre.current_credits AS current_credits_cierre,
                        caja.host_loads,
                        caja.host_cashouts,
                        (cierre.coin_in - apertura.coin_in)
                            - (cierre.coin_out - apertura.coin_out)
                            - (cierre.jackpot - apertura.jackpot)
                            - (cierre.cancelled - apertura.cancelled) AS ganancia_calculada,
                        GREATEST(
                            ((cierre.coin_out - apertura.coin_out)
                                + (cierre.jackpot - apertura.jackpot)
                                + (cierre.cancelled - apertura.cancelled))
                            - (cierre.coin_in - apertura.coin_in),
                            0
                        ) AS perdida_calculada,
                        caja.caja_reportado
                    FROM apertura, cierre, caja
                )
                INSERT INTO app.egm_daily_reconciliations (
                    id_jornada, id_egm, id_snapshot_apertura, id_snapshot_cierre,
                    coin_in_delta, coin_out_delta, jackpot_delta, cancelled_delta,
                    bills_accepted_delta, current_credits_cierre, host_loads, host_cashouts,
                    ganancia_calculada, perdida_calculada, caja_reportado, diferencia_vs_caja,
                    estado, detalle
                )
                SELECT
                    :jornadaId,
                    :egmId,
                    id_snapshot_apertura,
                    id_snapshot_cierre,
                    coin_in_delta,
                    coin_out_delta,
                    jackpot_delta,
                    cancelled_delta,
                    bills_accepted_delta,
                    current_credits_cierre,
                    host_loads,
                    host_cashouts,
                    ganancia_calculada,
                    perdida_calculada,
                    caja_reportado,
                    ganancia_calculada - caja_reportado,
                    CASE
                        WHEN estado_apertura <> 'OK'
                          OR estado_cierre <> 'OK'
                          OR proveedor_apertura <> 'SAS_RASPBERRY'
                          OR proveedor_cierre <> 'SAS_RASPBERRY' THEN 'INCOMPLETO'
                        WHEN current_credits_cierre > 0 THEN 'REQUIERE_REVISION'
                        WHEN ABS(ganancia_calculada - caja_reportado) > 0.01 THEN 'DIFERENCIA'
                        ELSE 'CUADRADO'
                    END,
                    CASE
                        WHEN estado_apertura <> 'OK'
                          OR estado_cierre <> 'OK'
                          OR proveedor_apertura <> 'SAS_RASPBERRY'
                          OR proveedor_cierre <> 'SAS_RASPBERRY'
                            THEN 'Snapshot de apertura/cierre no real o sin respuesta SAS'
                        WHEN current_credits_cierre > 0 THEN 'EGM con creditos/capturas pendientes al cierre'
                        WHEN ABS(ganancia_calculada - caja_reportado) > 0.01 THEN 'Diferencia entre meters y caja/backend'
                        ELSE 'Conciliacion cuadrada'
                    END
                FROM calculo
                ON CONFLICT (id_jornada, id_egm) DO UPDATE SET
                    id_snapshot_apertura = EXCLUDED.id_snapshot_apertura,
                    id_snapshot_cierre = EXCLUDED.id_snapshot_cierre,
                    coin_in_delta = EXCLUDED.coin_in_delta,
                    coin_out_delta = EXCLUDED.coin_out_delta,
                    jackpot_delta = EXCLUDED.jackpot_delta,
                    cancelled_delta = EXCLUDED.cancelled_delta,
                    bills_accepted_delta = EXCLUDED.bills_accepted_delta,
                    current_credits_cierre = EXCLUDED.current_credits_cierre,
                    host_loads = EXCLUDED.host_loads,
                    host_cashouts = EXCLUDED.host_cashouts,
                    ganancia_calculada = EXCLUDED.ganancia_calculada,
                    perdida_calculada = EXCLUDED.perdida_calculada,
                    caja_reportado = EXCLUDED.caja_reportado,
                    diferencia_vs_caja = EXCLUDED.diferencia_vs_caja,
                    estado = EXCLUDED.estado,
                    detalle = EXCLUDED.detalle,
                    fecha_calculo = CURRENT_TIMESTAMP
                """)
                .param("jornadaId", jornadaId)
                .param("egmId", egm.id())
                .param("egmAddr", egm.egmAddr())
                .param("egmNombre", egm.nombre())
                .update();
    }

    public void completeOperationalClose(Long jornadaId, Long closeId, boolean force, Long userId) {
        jdbcClient.sql("""
                WITH resumen AS (
                    SELECT
                        COUNT(*)::INTEGER AS total_egms,
                        COUNT(*) FILTER (WHERE estado = 'CUADRADO')::INTEGER AS egms_ok,
                        COUNT(*) FILTER (WHERE estado = 'DIFERENCIA')::INTEGER AS egms_diferencia,
                        COUNT(*) FILTER (WHERE estado IN ('INCOMPLETO', 'REQUIERE_REVISION'))::INTEGER AS egms_incompletas,
                        COALESCE(SUM(caja_reportado), 0) AS total_caja_reportado,
                        COALESCE(SUM(ganancia_calculada), 0) AS total_egm_calculado,
                        COALESCE(SUM(diferencia_vs_caja), 0) AS diferencia_total
                    FROM app.egm_daily_reconciliations
                    WHERE id_jornada = :jornadaId
                )
                UPDATE app.cierres_operacion co
                SET total_egms = resumen.total_egms,
                    egms_ok = resumen.egms_ok,
                    egms_diferencia = resumen.egms_diferencia,
                    egms_incompletas = resumen.egms_incompletas,
                    total_caja_reportado = resumen.total_caja_reportado,
                    total_egm_calculado = resumen.total_egm_calculado,
                    diferencia_total = resumen.diferencia_total,
                    estado = CASE
                        WHEN resumen.egms_incompletas > 0 THEN 'INCOMPLETO'
                        WHEN resumen.egms_diferencia > 0 AND :force = TRUE THEN 'CON_DIFERENCIAS'
                        WHEN resumen.egms_diferencia > 0 THEN 'CON_DIFERENCIAS'
                        ELSE 'COMPLETO'
                    END,
                    porcentaje = 100,
                    fecha_fin = CURRENT_TIMESTAMP
                FROM resumen
                WHERE co.id_cierre_operacion = :closeId
                """)
                .param("jornadaId", jornadaId)
                .param("closeId", closeId)
                .param("force", force)
                .update();

        jdbcClient.sql("""
                UPDATE app.jornadas_operativas
                SET estado = 'CERRADA',
                    fecha_cierre = CURRENT_TIMESTAMP,
                    cierre_por = :userId,
                    fecha_actualizacion = CURRENT_TIMESTAMP
                WHERE id_jornada = :jornadaId
                """)
                .param("jornadaId", jornadaId)
                .param("userId", userId)
                .update();
    }

    public Optional<OperationalCloseSummary> findLatestOperationalCloseSummary() {
        return jdbcClient.sql("""
                SELECT co.id_cierre_operacion, co.estado AS cierre_estado, co.porcentaje,
                       co.total_egms, co.egms_ok, co.egms_diferencia, co.egms_incompletas,
                       co.total_caja_reportado, co.total_egm_calculado, co.diferencia_total,
                       co.observaciones, iu.username AS iniciado_username, co.fecha_inicio, co.fecha_fin,
                       j.id_jornada, j.fecha_jornada, j.estado, j.fecha_apertura, j.fecha_cierre,
                       j.apertura_por, au.username AS apertura_username,
                       concat_ws(' ', au.nombre, au.apellido_paterno, au.apellido_materno) AS apertura_nombre,
                       j.cierre_por, cu.username AS cierre_username, j.observaciones AS jornada_observaciones
                FROM app.cierres_operacion co
                JOIN app.jornadas_operativas j ON j.id_jornada = co.id_jornada
                JOIN app.usuarios au ON au.id_usuario = j.apertura_por
                LEFT JOIN app.usuarios cu ON cu.id_usuario = j.cierre_por
                LEFT JOIN app.usuarios iu ON iu.id_usuario = co.iniciado_por
                ORDER BY co.fecha_inicio DESC
                LIMIT 1
                """)
                .query((rs, rowNum) -> operationalCloseSummary(rs))
                .optional()
                .map(summary -> new OperationalCloseSummary(
                        summary.id(),
                        summary.jornada(),
                        summary.estado(),
                        summary.porcentaje(),
                        summary.totalEgms(),
                        summary.egmsOk(),
                        summary.egmsDiferencia(),
                        summary.egmsIncompletas(),
                        summary.totalCajaReportado(),
                        summary.totalEgmCalculado(),
                        summary.diferenciaTotal(),
                        summary.observaciones(),
                        summary.iniciadoUsername(),
                        summary.fechaInicio(),
                        summary.fechaFin(),
                        findClosedCashierSessions(summary.jornada().id()),
                        findEgmSnapshots(summary.jornada().id()),
                        findEgmReconciliations(summary.jornada().id())
                ));
    }

    public List<EgmMeterSnapshot> findEgmSnapshots(Long jornadaId) {
        return jdbcClient.sql("""
                SELECT s.id_snapshot, s.id_jornada, s.id_egm, e.egm_addr, e.nombre AS egm_nombre,
                       s.tipo_snapshot, s.estado, s.proveedor, s.coin_in, s.coin_out, s.jackpot,
                       s.handpay_cancelled, s.cancelled, s.games_played, s.games_won, s.games_lost,
                       s.bills_accepted, s.current_credits, s.raw_response, s.mensaje, s.fecha_snapshot
                FROM app.egm_meter_snapshots s
                JOIN app.egms_operativas e ON e.id_egm = s.id_egm
                WHERE s.id_jornada = :jornadaId
                ORDER BY e.egm_addr, s.tipo_snapshot
                """)
                .param("jornadaId", jornadaId)
                .query((rs, rowNum) -> egmMeterSnapshot(rs))
                .list();
    }

    public List<EgmDailyReconciliation> findEgmReconciliations(Long jornadaId) {
        return jdbcClient.sql("""
                SELECT r.id_reconciliacion, r.id_jornada, r.id_egm, e.egm_addr, e.nombre AS egm_nombre,
                       r.coin_in_delta, r.coin_out_delta, r.jackpot_delta, r.cancelled_delta,
                       r.bills_accepted_delta, r.current_credits_cierre, r.host_loads, r.host_cashouts,
                       r.ganancia_calculada, r.perdida_calculada, r.caja_reportado, r.diferencia_vs_caja,
                       r.estado, r.detalle, r.fecha_calculo
                FROM app.egm_daily_reconciliations r
                JOIN app.egms_operativas e ON e.id_egm = r.id_egm
                WHERE r.id_jornada = :jornadaId
                ORDER BY e.egm_addr
                """)
                .param("jornadaId", jornadaId)
                .query((rs, rowNum) -> egmDailyReconciliation(rs))
                .list();
    }

    private static OperationSchedule operationSchedule(ResultSet rs) throws SQLException {
        return new OperationSchedule(
                rs.getLong("id_horario"),
                rs.getString("nombre"),
                rs.getObject("hora_inicio", LocalTime.class),
                rs.getObject("hora_fin", LocalTime.class),
                rs.getBoolean("activo"),
                offsetDateTime(rs, "fecha_creacion"),
                offsetDateTime(rs, "fecha_actualizacion")
        );
    }

    private static CashierShift cashierShift(ResultSet rs) throws SQLException {
        return new CashierShift(
                rs.getLong("id_turno"),
                rs.getString("nombre"),
                rs.getObject("hora_inicio", LocalTime.class),
                rs.getObject("hora_fin", LocalTime.class),
                rs.getBoolean("activo"),
                offsetDateTime(rs, "fecha_creacion"),
                offsetDateTime(rs, "fecha_actualizacion")
        );
    }

    private static Workstation workstation(ResultSet rs) throws SQLException {
        return new Workstation(
                rs.getLong("id_estacion"),
                rs.getObject("id_caja", Long.class),
                rs.getString("caja_nombre"),
                rs.getString("nombre"),
                rs.getString("tipo"),
                rs.getString("sala"),
                rs.getString("ubicacion"),
                rs.getBoolean("activa"),
                offsetDateTime(rs, "fecha_creacion"),
                offsetDateTime(rs, "fecha_actualizacion")
        );
    }

    private static OperationalAssignment assignment(ResultSet rs) throws SQLException {
        return new OperationalAssignment(
                rs.getLong("id_asignacion"),
                rs.getLong("id_usuario"),
                rs.getString("username"),
                rs.getString("nombre_usuario"),
                rs.getLong("id_estacion"),
                rs.getString("estacion_nombre"),
                rs.getString("estacion_tipo"),
                rs.getLong("id_turno"),
                rs.getString("turno_nombre"),
                rs.getObject("fecha_operacion", LocalDate.class),
                rs.getString("rol_operativo"),
                rs.getBoolean("activa"),
                offsetDateTime(rs, "fecha_creacion"),
                offsetDateTime(rs, "fecha_actualizacion")
        );
    }

    private static OperationalCard card(ResultSet rs) throws SQLException {
        return new OperationalCard(
                rs.getLong("id_tarjeta"),
                rs.getString("numero_tarjeta"),
                rs.getString("tipo"),
                rs.getObject("fecha_vencimiento", LocalDate.class),
                rs.getString("estado"),
                rs.getString("cliente_nombre"),
                rs.getBoolean("capturada_egm"),
                rs.getString("captura_egm_txid"),
                offsetDateTime(rs, "captura_egm_fecha"),
                offsetDateTime(rs, "fecha_creacion"),
                offsetDateTime(rs, "fecha_actualizacion")
        );
    }

    private static OperationalDay operationalDay(ResultSet rs) throws SQLException {
        return new OperationalDay(
                rs.getLong("id_jornada"),
                rs.getObject("fecha_jornada", LocalDate.class),
                rs.getString("estado"),
                offsetDateTime(rs, "fecha_apertura"),
                offsetDateTime(rs, "fecha_cierre"),
                rs.getLong("apertura_por"),
                rs.getString("apertura_username"),
                rs.getString("apertura_nombre"),
                rs.getObject("cierre_por", Long.class),
                rs.getString("cierre_username"),
                rs.getString("observaciones")
        );
    }

    private static TreasurySession treasurySession(ResultSet rs) throws SQLException {
        return new TreasurySession(
                rs.getLong("id_tesoreria_jornada"),
                rs.getLong("id_jornada"),
                rs.getObject("fecha_jornada", LocalDate.class),
                rs.getLong("id_estacion"),
                rs.getString("estacion_nombre"),
                rs.getString("estado"),
                rs.getBigDecimal("saldo_inicial"),
                rs.getBigDecimal("saldo_actual"),
                offsetDateTime(rs, "fecha_apertura"),
                rs.getString("apertura_username"),
                offsetDateTime(rs, "fecha_precierre"),
                rs.getString("precierre_username"),
                offsetDateTime(rs, "fecha_cierre"),
                rs.getString("cierre_username"),
                rs.getString("observaciones")
        );
    }

    private static TreasuryMovement treasuryMovement(ResultSet rs) throws SQLException {
        return new TreasuryMovement(
                rs.getLong("id_movimiento_tesoreria"),
                rs.getLong("id_tesoreria_jornada"),
                rs.getObject("id_estacion_caja", Long.class),
                rs.getString("estacion_caja_nombre"),
                rs.getObject("id_turno", Long.class),
                rs.getString("turno_nombre"),
                rs.getString("tipo"),
                rs.getString("concepto"),
                rs.getBigDecimal("monto"),
                rs.getString("referencia"),
                rs.getString("observaciones"),
                rs.getLong("registrado_por"),
                rs.getString("registrado_username"),
                offsetDateTime(rs, "fecha_movimiento")
        );
    }

    private static TreasuryCardMovement treasuryCardMovement(ResultSet rs) throws SQLException {
        return new TreasuryCardMovement(
                rs.getLong("id_movimiento_tarjetas"),
                rs.getLong("id_tesoreria_jornada"),
                rs.getLong("id_estacion_caja"),
                rs.getString("estacion_caja_nombre"),
                rs.getObject("id_turno", Long.class),
                rs.getString("turno_nombre"),
                rs.getString("tipo"),
                rs.getString("numero_inicial"),
                rs.getString("numero_final"),
                rs.getInt("cantidad"),
                rs.getString("referencia"),
                rs.getString("observaciones"),
                rs.getLong("registrado_por"),
                rs.getString("registrado_username"),
                offsetDateTime(rs, "fecha_movimiento")
        );
    }

    private static TreasuryLedgerEntry treasuryLedgerEntry(ResultSet rs) throws SQLException {
        return new TreasuryLedgerEntry(
                rs.getString("id"),
                rs.getString("categoria"),
                rs.getString("tipo"),
                rs.getString("detalle"),
                rs.getString("estacion"),
                rs.getString("turno"),
                rs.getBigDecimal("monto"),
                rs.getObject("cantidad", Integer.class),
                rs.getString("referencia"),
                rs.getString("observaciones"),
                rs.getString("username"),
                offsetDateTime(rs, "fecha_evento")
        );
    }

    private static CashierSession cashierSession(ResultSet rs) throws SQLException {
        return new CashierSession(
                rs.getLong("id_caja_jornada"),
                rs.getLong("id_jornada"),
                rs.getObject("fecha_jornada", LocalDate.class),
                rs.getLong("id_caja"),
                rs.getString("caja_nombre"),
                rs.getLong("id_estacion"),
                rs.getString("estacion_nombre"),
                rs.getLong("id_turno"),
                rs.getString("turno_nombre"),
                rs.getString("estado"),
                rs.getBigDecimal("saldo_inicial"),
                rs.getBigDecimal("saldo_actual"),
                rs.getInt("tarjetas_iniciales"),
                rs.getInt("tarjetas_actuales"),
                offsetDateTime(rs, "fecha_apertura"),
                rs.getString("apertura_username"),
                offsetDateTime(rs, "fecha_precierre"),
                rs.getString("precierre_username"),
                offsetDateTime(rs, "fecha_cierre"),
                rs.getString("cierre_username"),
                rs.getBigDecimal("monto_declarado_cierre"),
                rs.getObject("tarjetas_devueltas_cierre", Integer.class),
                rs.getString("observaciones")
        );
    }

    private static CashierMovement cashierMovement(ResultSet rs) throws SQLException {
        return new CashierMovement(
                rs.getLong("id_movimiento_caja"),
                rs.getLong("id_caja_jornada"),
                rs.getObject("id_estacion", Long.class),
                rs.getString("estacion_nombre"),
                rs.getString("tipo"),
                rs.getString("numero_tarjeta"),
                rs.getBigDecimal("monto"),
                rs.getBigDecimal("impacto_saldo"),
                rs.getString("maquina"),
                rs.getString("motivo"),
                rs.getString("referencia"),
                rs.getString("observaciones"),
                rs.getLong("registrado_por"),
                rs.getString("registrado_username"),
                offsetDateTime(rs, "fecha_movimiento")
        );
    }

    private static CustomerRegistration customerRegistration(ResultSet rs) throws SQLException {
        return new CustomerRegistration(
                rs.getLong("id_cliente"),
                rs.getString("nombre"),
                rs.getString("apellido_paterno"),
                rs.getString("apellido_materno"),
                rs.getString("telefono"),
                rs.getString("email"),
                rs.getObject("fecha_nacimiento", LocalDate.class),
                rs.getString("documento_identidad"),
                rs.getString("estado_cliente"),
                rs.getObject("id_tarjeta", Long.class),
                rs.getString("numero_tarjeta"),
                rs.getString("estado_tarjeta"),
                rs.getObject("id_cliente_tarjeta", Long.class),
                rs.getObject("id_caja_jornada", Long.class),
                rs.getString("caja_nombre"),
                rs.getObject("id_turno", Long.class),
                rs.getString("turno_nombre"),
                rs.getObject("asignado_por", Long.class),
                rs.getString("asignado_username"),
                offsetDateTime(rs, "fecha_asignacion"),
                rs.getString("observaciones")
        );
    }

    private static OperationalAuditEvent auditEvent(ResultSet rs) throws SQLException {
        return new OperationalAuditEvent(
                rs.getLong("id_bitacora"),
                rs.getString("accion"),
                rs.getString("entidad"),
                rs.getObject("entidad_id", Long.class),
                rs.getString("detalle"),
                rs.getObject("id_usuario", Long.class),
                rs.getString("username"),
                offsetDateTime(rs, "fecha_evento"),
                rs.getString("ip_origen")
        );
    }

    private static EgmMachine egmMachine(ResultSet rs) throws SQLException {
        return new EgmMachine(
                rs.getLong("id_egm"),
                rs.getString("egm_addr"),
                rs.getString("nombre"),
                rs.getString("sala"),
                rs.getBoolean("activo"),
                rs.getString("raspberry_base_url"),
                rs.getBigDecimal("denominacion"),
                rs.getObject("timeout_segundos", Integer.class)
        );
    }

    private static EgmMeterSnapshot egmMeterSnapshot(ResultSet rs) throws SQLException {
        return new EgmMeterSnapshot(
                rs.getLong("id_snapshot"),
                rs.getLong("id_jornada"),
                rs.getLong("id_egm"),
                rs.getString("egm_addr"),
                rs.getString("egm_nombre"),
                rs.getString("tipo_snapshot"),
                rs.getString("estado"),
                rs.getString("proveedor"),
                rs.getBigDecimal("coin_in"),
                rs.getBigDecimal("coin_out"),
                rs.getBigDecimal("jackpot"),
                rs.getBigDecimal("handpay_cancelled"),
                rs.getBigDecimal("cancelled"),
                rs.getBigDecimal("games_played"),
                rs.getBigDecimal("games_won"),
                rs.getBigDecimal("games_lost"),
                rs.getBigDecimal("bills_accepted"),
                rs.getBigDecimal("current_credits"),
                rs.getString("raw_response"),
                rs.getString("mensaje"),
                offsetDateTime(rs, "fecha_snapshot")
        );
    }

    private static EgmDailyReconciliation egmDailyReconciliation(ResultSet rs) throws SQLException {
        return new EgmDailyReconciliation(
                rs.getLong("id_reconciliacion"),
                rs.getLong("id_jornada"),
                rs.getLong("id_egm"),
                rs.getString("egm_addr"),
                rs.getString("egm_nombre"),
                rs.getBigDecimal("coin_in_delta"),
                rs.getBigDecimal("coin_out_delta"),
                rs.getBigDecimal("jackpot_delta"),
                rs.getBigDecimal("cancelled_delta"),
                rs.getBigDecimal("bills_accepted_delta"),
                rs.getBigDecimal("current_credits_cierre"),
                rs.getBigDecimal("host_loads"),
                rs.getBigDecimal("host_cashouts"),
                rs.getBigDecimal("ganancia_calculada"),
                rs.getBigDecimal("perdida_calculada"),
                rs.getBigDecimal("caja_reportado"),
                rs.getBigDecimal("diferencia_vs_caja"),
                rs.getString("estado"),
                rs.getString("detalle"),
                offsetDateTime(rs, "fecha_calculo")
        );
    }

    private static OperationalCloseSummary operationalCloseSummary(ResultSet rs) throws SQLException {
        OperationalDay jornada = new OperationalDay(
                rs.getLong("id_jornada"),
                rs.getObject("fecha_jornada", LocalDate.class),
                rs.getString("estado"),
                offsetDateTime(rs, "fecha_apertura"),
                offsetDateTime(rs, "fecha_cierre"),
                rs.getLong("apertura_por"),
                rs.getString("apertura_username"),
                rs.getString("apertura_nombre"),
                rs.getObject("cierre_por", Long.class),
                rs.getString("cierre_username"),
                rs.getString("jornada_observaciones")
        );
        return new OperationalCloseSummary(
                rs.getLong("id_cierre_operacion"),
                jornada,
                rs.getString("cierre_estado"),
                rs.getInt("porcentaje"),
                rs.getInt("total_egms"),
                rs.getInt("egms_ok"),
                rs.getInt("egms_diferencia"),
                rs.getInt("egms_incompletas"),
                rs.getBigDecimal("total_caja_reportado"),
                rs.getBigDecimal("total_egm_calculado"),
                rs.getBigDecimal("diferencia_total"),
                rs.getString("observaciones"),
                rs.getString("iniciado_username"),
                offsetDateTime(rs, "fecha_inicio"),
                offsetDateTime(rs, "fecha_fin"),
                List.of(),
                List.of(),
                List.of()
        );
    }

    private static OffsetDateTime offsetDateTime(ResultSet rs, String column) throws SQLException {
        return rs.getObject(column, OffsetDateTime.class);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Long cashierBoxIdForWorkstation(WorkstationRequest request) {
        if (!"CAJA".equals(request.tipo().trim().toUpperCase())) {
            return null;
        }
        if (request.cajaId() != null) {
            return request.cajaId();
        }
        return jdbcClient.sql("""
                SELECT id_caja
                FROM app.cajas_operativas
                WHERE principal = TRUE AND activa = TRUE
                ORDER BY id_caja
                LIMIT 1
                """)
                .query(Long.class)
                .single();
    }

    private static BigDecimal meter(Map<String, Long> meters, String key) {
        Long value = meters.get(key);
        return value == null ? BigDecimal.ZERO : BigDecimal.valueOf(value);
    }

    private static BigDecimal firstMeter(Map<String, Long> meters, String firstKey, String secondKey) {
        Long first = meters.get(firstKey);
        if (first != null) {
            return BigDecimal.valueOf(first);
        }
        Long second = meters.get(secondKey);
        return second == null ? BigDecimal.ZERO : BigDecimal.valueOf(second);
    }

    private static String rawMetersResponse(EgmMetersResponse response) {
        return """
                {"operationId":"%s","egmAddress":"%s","rawFrameHex":"%s","meters":%s}
                """.formatted(
                safeJson(response.operationId()),
                safeJson(response.egmAddress()),
                safeJson(response.rawFrameHex()),
                response.meters() == null ? "{}" : response.meters().toString()
        ).trim();
    }

    private static String safeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
