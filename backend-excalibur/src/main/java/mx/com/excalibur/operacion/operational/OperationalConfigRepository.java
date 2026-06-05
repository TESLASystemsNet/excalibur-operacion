package mx.com.excalibur.operacion.operational;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
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
                SELECT id_estacion, nombre, tipo, sala, ubicacion, activa, fecha_creacion, fecha_actualizacion
                FROM app.estaciones_operativas
                ORDER BY tipo, nombre
                """)
                .query((rs, rowNum) -> workstation(rs))
                .list();
    }

    public Long createWorkstation(WorkstationRequest request, Long userId) {
        return jdbcClient.sql("""
                INSERT INTO app.estaciones_operativas (nombre, tipo, sala, ubicacion, activa, creado_por)
                VALUES (:nombre, :tipo, :sala, :ubicacion, :activa, :userId)
                RETURNING id_estacion
                """)
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
                SET nombre = :nombre,
                    tipo = :tipo,
                    sala = :sala,
                    ubicacion = :ubicacion,
                    activa = :activa,
                    actualizado_por = :userId,
                    fecha_actualizacion = CURRENT_TIMESTAMP
                WHERE id_estacion = :id
                """)
                .param("id", id)
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

    public List<OperationalCard> findCards(String search) {
        String normalized = blankToNull(search);
        return jdbcClient.sql("""
                SELECT id_tarjeta, numero_tarjeta, tipo, fecha_vencimiento, estado,
                       fecha_creacion, fecha_actualizacion
                FROM app.tarjetas_operativas
                WHERE (CAST(:search AS VARCHAR) IS NULL OR numero_tarjeta LIKE :pattern)
                ORDER BY id_tarjeta DESC
                LIMIT 300
                """)
                .param("search", normalized)
                .param("pattern", normalized == null ? null : "%" + normalized + "%")
                .query((rs, rowNum) -> card(rs))
                .list();
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
                offsetDateTime(rs, "fecha_creacion"),
                offsetDateTime(rs, "fecha_actualizacion")
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

    private static OffsetDateTime offsetDateTime(ResultSet rs, String column) throws SQLException {
        return rs.getObject(column, OffsetDateTime.class);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
