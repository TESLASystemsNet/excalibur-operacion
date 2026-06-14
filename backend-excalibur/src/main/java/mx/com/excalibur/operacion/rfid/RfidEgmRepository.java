package mx.com.excalibur.operacion.rfid;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class RfidEgmRepository {

    private final JdbcClient jdbcClient;

    public RfidEgmRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<RfidRuntimeCard> findRuntimeCard(String uid) {
        return jdbcClient.sql("""
                WITH saldo AS (
                    SELECT numero_tarjeta,
                           COALESCE(SUM(
                               COALESCE(impacto_saldo,
                                   CASE
                                       WHEN tipo IN ('VENTA', 'CORTESIA', 'PROMOCIONAL', 'TRANSACCION_ESPECIAL') THEN monto
                                       WHEN tipo IN ('PAGO', 'PAGO_MANUAL', 'DEVOLUCION') THEN -monto
                                       ELSE 0
                                   END
                               )
                           ), 0) AS saldo
                    FROM app.movimientos_caja
                    WHERE numero_tarjeta = :uid
                    GROUP BY numero_tarjeta
                )
                SELECT t.id_tarjeta, t.numero_tarjeta, t.estado AS tarjeta_estado,
                       ct.id_cliente_tarjeta, cj.id_caja_jornada, cj.estado AS caja_estado,
                       cj.apertura_por AS registrado_por,
                       CONCAT_WS(' ', c.nombre, c.apellido_paterno, c.apellido_materno) AS cliente_nombre,
                       COALESCE(s.saldo, 0) AS saldo
                FROM app.tarjetas_operativas t
                JOIN app.cliente_tarjetas ct ON ct.id_tarjeta = t.id_tarjeta
                JOIN app.clientes_operativos c ON c.id_cliente = ct.id_cliente
                LEFT JOIN LATERAL (
                    SELECT cj.id_caja_jornada, cj.estado, cj.apertura_por
                    FROM app.cajas_jornada cj
                    JOIN app.jornadas_operativas j ON j.id_jornada = cj.id_jornada
                    WHERE cj.estado = 'ABIERTA'
                      AND j.estado = 'ABIERTA'
                    ORDER BY cj.fecha_apertura DESC
                    LIMIT 1
                ) cj ON TRUE
                LEFT JOIN saldo s ON s.numero_tarjeta = t.numero_tarjeta
                WHERE t.numero_tarjeta = :uid
                  AND t.tipo = 'CLIENTE'
                  AND ct.estado = 'ACTIVA'
                ORDER BY ct.fecha_asignacion DESC
                LIMIT 1
                """)
                .param("uid", uid.trim())
                .query((rs, rowNum) -> runtimeCard(rs))
                .optional();
    }

    public Optional<RfidCapture> findActiveCapture(String uid) {
        return jdbcClient.sql("""
                SELECT id_captura, txid, uid, id_tarjeta, id_cliente_tarjeta, id_caja_jornada, egm_addr,
                       saldo_autorizado, creditos, estado
                FROM app.capturas_rfid_egm
                WHERE uid = :uid
                  AND estado IN ('AUTORIZADA', 'CONFIRMADA')
                ORDER BY fecha_creacion DESC
                LIMIT 1
                """)
                .param("uid", uid.trim())
                .query((rs, rowNum) -> capture(rs))
                .optional();
    }

    public Long createCapture(String txid, String uid, String egmAddr, RfidRuntimeCard card, int credits) {
        Long captureId = jdbcClient.sql("""
                INSERT INTO app.capturas_rfid_egm (
                    txid, uid, id_tarjeta, id_cliente_tarjeta, id_caja_jornada,
                    egm_addr, saldo_autorizado, creditos, estado
                )
                VALUES (
                    :txid, :uid, :tarjetaId, :clienteTarjetaId, :cajaJornadaId,
                    :egmAddr, :saldoAutorizado, :creditos, 'AUTORIZADA'
                )
                RETURNING id_captura
                """)
                .param("txid", txid)
                .param("uid", uid.trim())
                .param("tarjetaId", card.tarjetaId())
                .param("clienteTarjetaId", card.clienteTarjetaId())
                .param("cajaJornadaId", card.cajaJornadaId())
                .param("egmAddr", blankToNull(egmAddr))
                .param("saldoAutorizado", card.saldo())
                .param("creditos", credits)
                .query(Long.class)
                .single();
        markCardCaptured(card.tarjetaId(), txid);
        return captureId;
    }

    public void finishCaptureLoad(String txid, String status, String result, String message) {
        jdbcClient.sql("""
                UPDATE app.capturas_rfid_egm
                SET estado = :status,
                    resultado_egm = :result,
                    mensaje = :message,
                    fecha_confirmacion = CURRENT_TIMESTAMP,
                    fecha_cierre = CASE WHEN :status = 'CANCELADA' THEN CURRENT_TIMESTAMP ELSE fecha_cierre END
                WHERE txid = :txid
                  AND estado IN ('AUTORIZADA', 'CONFIRMADA')
                """)
                .param("txid", txid.trim())
                .param("status", status)
                .param("result", blankToNull(result))
                .param("message", blankToNull(message))
                .update();
        if ("CANCELADA".equals(status)) {
            releaseCardByTxid(txid);
        }
    }

    public void closeCapture(String txid, String message) {
        jdbcClient.sql("""
                UPDATE app.capturas_rfid_egm
                SET estado = 'CERRADA',
                    mensaje = COALESCE(:message, mensaje),
                    fecha_cierre = CURRENT_TIMESTAMP
                WHERE txid = :txid
                  AND estado IN ('AUTORIZADA', 'CONFIRMADA')
                """)
                .param("txid", txid.trim())
                .param("message", blankToNull(message))
                .update();
        releaseCardByTxid(txid);
    }

    public Long createSettlementMovement(
            RfidCapture capture,
            String type,
            BigDecimal amount,
            BigDecimal balanceImpact,
            Integer credits,
            String reason,
            String message,
            Long userId
    ) {
        return jdbcClient.sql("""
                INSERT INTO app.movimientos_caja (
                    id_caja_jornada, tipo, numero_tarjeta, monto, maquina, motivo,
                    referencia, observaciones, registrado_por, impacto_saldo
                )
                VALUES (
                    :cashierSessionId, :type, :uid, :amount, :maquina, :reason,
                    :reference, :observations, :userId, :balanceImpact
                )
                RETURNING id_movimiento_caja
                """)
                .param("cashierSessionId", capture.cajaJornadaId())
                .param("type", type)
                .param("uid", capture.uid())
                .param("amount", amount)
                .param("maquina", blankToNull(capture.egmAddr()))
                .param("balanceImpact", balanceImpact)
                .param("reason", reason)
                .param("reference", capture.txid())
                .param("observations", "creditos=" + (credits == null ? 0 : credits) + " " + (message == null ? "" : message))
                .param("userId", userId)
                .query(Long.class)
                .single();
    }

    public void applyCashierDelta(Long cashierSessionId, BigDecimal delta) {
        jdbcClient.sql("""
                UPDATE app.cajas_jornada
                SET saldo_actual = saldo_actual + :delta,
                    fecha_actualizacion = CURRENT_TIMESTAMP
                WHERE id_caja_jornada = :cashierSessionId
                """)
                .param("cashierSessionId", cashierSessionId)
                .param("delta", delta)
                .update();
    }

    public void markCardCaptured(Long cardId, String txid) {
        jdbcClient.sql("""
                UPDATE app.tarjetas_operativas
                SET capturada_egm = TRUE,
                    captura_egm_txid = :txid,
                    captura_egm_fecha = CURRENT_TIMESTAMP,
                    fecha_actualizacion = CURRENT_TIMESTAMP
                WHERE id_tarjeta = :cardId
                """)
                .param("cardId", cardId)
                .param("txid", txid.trim())
                .update();
    }

    public void releaseCardByTxid(String txid) {
        jdbcClient.sql("""
                UPDATE app.tarjetas_operativas
                SET capturada_egm = FALSE,
                    captura_egm_txid = NULL,
                    captura_egm_fecha = NULL,
                    fecha_actualizacion = CURRENT_TIMESTAMP
                WHERE captura_egm_txid = :txid
                """)
                .param("txid", txid.trim())
                .update();
    }

    public void audit(String action, String entity, Long entityId, String detail, Long userId) {
        jdbcClient.sql("""
                INSERT INTO app.bitacora_operativa (accion, entidad, entidad_id, detalle, id_usuario, ip_origen)
                VALUES (:action, :entity, :entityId, :detail, :userId, 'RASPBERRY_RFID')
                """)
                .param("action", action)
                .param("entity", entity)
                .param("entityId", entityId)
                .param("detail", detail)
                .param("userId", userId)
                .update();
    }

    private static RfidRuntimeCard runtimeCard(ResultSet rs) throws SQLException {
        return new RfidRuntimeCard(
                rs.getLong("id_tarjeta"),
                rs.getString("numero_tarjeta"),
                rs.getString("tarjeta_estado"),
                rs.getLong("id_cliente_tarjeta"),
                rs.getObject("id_caja_jornada", Long.class),
                rs.getString("caja_estado"),
                rs.getObject("registrado_por", Long.class),
                rs.getString("cliente_nombre"),
                rs.getBigDecimal("saldo")
        );
    }

    private static RfidCapture capture(ResultSet rs) throws SQLException {
        return new RfidCapture(
                rs.getLong("id_captura"),
                rs.getString("txid"),
                rs.getString("uid"),
                rs.getLong("id_tarjeta"),
                rs.getLong("id_cliente_tarjeta"),
                rs.getLong("id_caja_jornada"),
                rs.getString("egm_addr"),
                rs.getBigDecimal("saldo_autorizado"),
                rs.getInt("creditos"),
                rs.getString("estado")
        );
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
