UPDATE app.movimientos_caja m
   SET maquina = c.egm_addr
  FROM app.capturas_rfid_egm c
 WHERE m.referencia = c.txid
   AND m.tipo = 'PAGO'
   AND m.motivo = 'Cobro RFID desde EGM'
   AND (m.maquina IS NULL OR btrim(m.maquina) = '')
   AND c.egm_addr IS NOT NULL;

