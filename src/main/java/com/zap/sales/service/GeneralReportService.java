package com.zap.sales.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Parameter;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import com.zap.acquisition.vo.StatusAcquisitionEnum;
import com.zap.sales.vo.venta.StatusVentaEnum;
import com.zap.security.entity.authenticate.AuthUserEntity;
import com.zap.security.entity.authenticate.ProviderConfigEntity;
import com.zap.security.vo.enumerates.RoleEnum;

@Stateless
public class GeneralReportService implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String[] CLAVES_MESES = {
            "ene", "feb", "mar", "abr", "may", "jun",
            "jul", "ago", "sep", "oct", "nov", "dic" };

    private static final List<StatusVentaEnum> ESTADOS_VENTAS_CAIDAS = Collections
            .unmodifiableList(Arrays.asList(StatusVentaEnum.CANCELADO, StatusVentaEnum.ERROR));

    private static final String TIPO_VENTAS_CAIDAS = "Ventas Caídas";
    private static final String TIPO_PORCENTAJE_VENTAS_CAIDAS = "% Ventas Caídas";

    private static final TimeZone ZONA_HORARIA_ESPANIA = TimeZone.getTimeZone("Europe/Madrid");

    @PersistenceContext(unitName = "zapLaboralgrouPool")
    private EntityManager entityManager;

    public List<Map<String, Object>> load(int year, String uuidProvider, String reference) {
        return load(year, uuidProvider, reference, null);
    }

    public List<Map<String, Object>> load(int year, String uuidProvider, String reference, String providerName) {
        Map<String, Integer> insertados = crearMapaMesesVacio();
        Map<String, Integer> ventasBrutas = crearMapaMesesVacio();
        Map<String, Integer> ventasNetas = crearMapaMesesVacio();
        Map<String, Integer> cerrados = crearMapaMesesVacio();
        Map<String, Integer> abiertos = crearMapaMesesVacio();
        Map<String, Integer> insertadosConInvalidacion = crearMapaMesesVacio();
        Map<String, Integer> ventasCaidas = crearMapaMesesVacio();

        String consultaInsertados;
        String consultaVentasBrutas;
        String consultaVentasNetas;
        String consultaCerrados;
        String consultaVentasCaidas;

        if ("fxLastCall".equals(reference)) {
            consultaInsertados = "AcquisitionEntity.countInsertedByMonthLastCall";
            consultaVentasBrutas = "VentaEntity.countByAcqLastCallMonth";
            consultaVentasNetas = "VentaEntity.countByAcqLastCallMonthStatus";
            consultaCerrados = "AcquisitionEntity.countClosedByMonthLastCall";
            consultaVentasCaidas = "VentaEntity.countByAcqLastCallMonthStatusIn";
        } else {
            consultaInsertados = "AcquisitionEntity.countInsertedByMonth";
            consultaVentasBrutas = "VentaEntity.countByAcqInsertionMonth";
            consultaVentasNetas = "VentaEntity.countByAcqInsertionMonthStatus";
            consultaCerrados = "AcquisitionEntity.countClosedByMonth";
            consultaVentasCaidas = "VentaEntity.countByAcqInsertionMonthStatusIn";
        }

        asignarConteosPorMes(ejecutarConsulta(consultaInsertados,
                crearParametros("year", year, "uuidProvider", uuidProvider)), insertados);

        asignarConteosPorMes(ejecutarConsulta(consultaVentasBrutas,
                crearParametros("year", year, "uuidProvider", uuidProvider)), ventasBrutas);

        asignarConteosPorMes(ejecutarConsulta(consultaVentasNetas,
                crearParametros("year", year, "uuidProvider", uuidProvider, "status", StatusVentaEnum.EN_EJECUCION)),
                ventasNetas);

        asignarConteosPorMes(ejecutarConsulta(consultaVentasCaidas,
                crearParametros("year", year, "uuidProvider", uuidProvider, "statuses", ESTADOS_VENTAS_CAIDAS)),
                ventasCaidas);

        asignarConteosPorMes(ejecutarConsulta(consultaCerrados,
                crearParametros("year", year, "uuidProvider", uuidProvider, "status", StatusAcquisitionEnum.CERRADO)),
                cerrados);

        asignarConteosPorMes(ejecutarConsulta(consultaCerrados,
                crearParametros("year", year, "uuidProvider", uuidProvider, "status", StatusAcquisitionEnum.ABIERTO)),
                abiertos);

        Map<String, BigDecimal> invalidacionPorMes = loadInvalidacionByMonth(year, uuidProvider);
        for (String claveMes : CLAVES_MESES) {
            int totalInsertados = insertados.get(claveMes);
            BigDecimal invalidacion = invalidacionPorMes.getOrDefault(claveMes, BigDecimal.ZERO);
            insertadosConInvalidacion.put(claveMes,
                    calcularInsertadosConInvalidacion(totalInsertados, invalidacion));
        }

        List<Map<String, Object>> resultado = new ArrayList<>();
        int consecutivo = 1;

        ajustarCerradosSegunInsertados(cerrados, "fxLastCall".equals(reference) ? insertados : insertadosConInvalidacion);

        Map<String, Object> filaInsertados = crearFilaBasica(consecutivo++,
                "fxLastCall".equals(reference) ? "Última llamada" : "Total Insertados",
                insertados, Arrays.asList(CLAVES_MESES));
        resultado.add(filaInsertados);

        if (!"fxLastCall".equals(reference)) {
            Map<String, Object> filaInsertadosInvalidacion = crearFilaBasica(consecutivo++,
                    "Total Inse. - invalidacion", insertadosConInvalidacion, Arrays.asList(CLAVES_MESES));
            resultado.add(filaInsertadosInvalidacion);
        }

        Map<String, Object> filaCerrados = crearFilaBasica(consecutivo++, "Cerrados", cerrados,
                Arrays.asList(CLAVES_MESES));
        resultado.add(filaCerrados);

        Map<String, Object> filaAbiertos = crearFilaBasica(consecutivo++, "Abiertos", abiertos,
                Arrays.asList(CLAVES_MESES));
        resultado.add(filaAbiertos);

        Map<String, Object> filaVentasBrutas = crearFilaBasica(consecutivo++, "Ventas Brutas", ventasBrutas,
                Arrays.asList(CLAVES_MESES));
        resultado.add(filaVentasBrutas);

        Map<String, Object> filaRatioBrutoInsertados = crearFilaPorcentaje(consecutivo++,
                "Conversión Bruta Insertados", ventasBrutas, insertados, Arrays.asList(CLAVES_MESES));
        resultado.add(filaRatioBrutoInsertados);

        Map<String, Object> filaRatioBrutoCerrados = crearFilaPorcentaje(consecutivo++,
                "Conversión Bruta Cerrados", ventasBrutas, cerrados, Arrays.asList(CLAVES_MESES));
        resultado.add(filaRatioBrutoCerrados);

        boolean esUltimaLlamada = "fxLastCall".equals(reference);
        Map<String, Integer> baseCpaBruta = esUltimaLlamada ? cerrados : insertadosConInvalidacion;
        Map<String, BigDecimal> costoLeadsPorMes = uuidProvider != null ? loadCplByMonth(year, uuidProvider) : null;
        Map<String, BigDecimal> costosTotalesPorMes = calcularCostosTotales(baseCpaBruta, costoLeadsPorMes,
                Arrays.asList(CLAVES_MESES));

        Map<String, Object> filaCpaBruta = crearFilaCpa(consecutivo++, "CPA Bruta", ventasBrutas, costosTotalesPorMes,
                Arrays.asList(CLAVES_MESES));
        resultado.add(filaCpaBruta);

        Map<String, Object> filaVentasNetas = crearFilaBasica(consecutivo++, "Ventas Netas", ventasNetas,
                Arrays.asList(CLAVES_MESES));
        resultado.add(filaVentasNetas);

        Map<String, Object> filaRatioNetoInsertados = crearFilaPorcentaje(consecutivo++,
                "Conversión Neta insertados", ventasNetas, insertados, Arrays.asList(CLAVES_MESES));
        resultado.add(filaRatioNetoInsertados);

        Map<String, Object> filaRatioNetoCerrados = crearFilaPorcentaje(consecutivo++,
                "Conversión Neta Cerrado", ventasNetas, cerrados, Arrays.asList(CLAVES_MESES));
        resultado.add(filaRatioNetoCerrados);

        Map<String, Object> filaCpaNeta = crearFilaCpa(consecutivo++, "CPA Neta", ventasNetas, costosTotalesPorMes,
                Arrays.asList(CLAVES_MESES));
        resultado.add(filaCpaNeta);

        Map<String, Object> filaVentasCaidas = crearFilaBasica(consecutivo++, TIPO_VENTAS_CAIDAS, ventasCaidas,
                Arrays.asList(CLAVES_MESES));
        resultado.add(filaVentasCaidas);

        Map<String, Object> filaPorcentajeVentasCaidas = crearFilaPorcentaje(consecutivo++,
                TIPO_PORCENTAJE_VENTAS_CAIDAS, ventasCaidas, ventasBrutas, Arrays.asList(CLAVES_MESES));
        resultado.add(filaPorcentajeVentasCaidas);

        reemplazarRatiosConConversion(filaRatioBrutoInsertados, year, reference, providerName);
        Map<String, Integer> insertadosParaTotales = esUltimaLlamada ? insertados : insertadosConInvalidacion;
        agregarColumnaTotales(resultado, insertados, insertadosParaTotales, cerrados, ventasBrutas, ventasNetas,
                ventasCaidas, costosTotalesPorMes);
        return resultado;
    }

    public List<Map<String, Object>> load(int year, List<String> uuidProviders, String reference) {
    	 return load(year, uuidProviders, reference, (Map<String, BigDecimal>) null,
                 (Map<String, BigDecimal>) null);    }

    public List<Map<String, Object>> load(int year, List<String> uuidProviders, String reference,
            Map<String, BigDecimal> invalidacionCompartida) {
        return load(year, uuidProviders, reference, invalidacionCompartida, null);
    }

    public List<Map<String, Object>> load(int year, List<String> uuidProviders, String reference, String providerName,
            String uuidProveedorPrincipal) {
        Map<String, BigDecimal> invalidacionCompartida = uuidProveedorPrincipal != null
                ? loadInvalidacionByMonth(year, uuidProveedorPrincipal)
                : null;
        Map<String, BigDecimal> costoCompartido = uuidProveedorPrincipal != null
                ? loadCplByMonth(year, uuidProveedorPrincipal)
                : null;
        return load(year, uuidProviders, reference, invalidacionCompartida, costoCompartido);
    }

    private List<Map<String, Object>> load(int year, List<String> uuidProviders, String reference,
            Map<String, BigDecimal> invalidacionCompartida, Map<String, BigDecimal> costoCompartido) {
        if (uuidProviders == null || uuidProviders.isEmpty()) {
            return load(year, (String) null, reference);
        }

        Map<String, Integer> insertados = crearMapaMesesVacio();
        Map<String, Integer> ventasBrutas = crearMapaMesesVacio();
        Map<String, Integer> ventasNetas = crearMapaMesesVacio();
        Map<String, Integer> cerrados = crearMapaMesesVacio();
        Map<String, Integer> abiertos = crearMapaMesesVacio();
        Map<String, Integer> insertadosConInvalidacion = crearMapaMesesVacio();
        Map<String, Integer> ventasCaidas = crearMapaMesesVacio();

        String consultaInsertados;
        String consultaVentasBrutas;
        String consultaVentasNetas;
        String consultaCerrados;
        String consultaVentasCaidas;

        if ("fxLastCall".equals(reference)) {
            consultaInsertados = "AcquisitionEntity.countInsertedByMonthLastCall";
            consultaVentasBrutas = "VentaEntity.countByAcqLastCallMonth";
            consultaVentasNetas = "VentaEntity.countByAcqLastCallMonthStatus";
            consultaCerrados = "AcquisitionEntity.countClosedByMonthLastCall";
            consultaVentasCaidas = "VentaEntity.countByAcqLastCallMonthStatusIn";
        } else {
            consultaInsertados = "AcquisitionEntity.countInsertedByMonth";
            consultaVentasBrutas = "VentaEntity.countByAcqInsertionMonth";
            consultaVentasNetas = "VentaEntity.countByAcqInsertionMonthStatus";
            consultaCerrados = "AcquisitionEntity.countClosedByMonth";
            consultaVentasCaidas = "VentaEntity.countByAcqInsertionMonthStatusIn";
        }

        Map<String, BigDecimal> costosTotalesPorMes = crearMapaMesesBigDecimalVacio();
        boolean esUltimaLlamada = "fxLastCall".equals(reference);

        for (String uuidProvider : uuidProviders) {
            List<Object[]> registrosInsertados = ejecutarConsulta(consultaInsertados,
                    crearParametros("year", year, "uuidProvider", uuidProvider));
            Map<String, BigDecimal> invalidacionPorMes = invalidacionCompartida != null ? invalidacionCompartida
                    : loadInvalidacionByMonth(year, uuidProvider);
            Map<String, BigDecimal> costoPorMes = costoCompartido != null ? costoCompartido
                    : loadCplByMonth(year, uuidProvider);
            for (Object[] registro : registrosInsertados) {
                int indiceMes = ((Number) registro[0]).intValue();
                int cantidad = ((Number) registro[1]).intValue();
                String claveMes = CLAVES_MESES[indiceMes - 1];
                insertados.put(claveMes, insertados.get(claveMes) + cantidad);
                BigDecimal invalidacion = invalidacionPorMes.get(claveMes);
                int ajustado = calcularInsertadosConInvalidacion(cantidad, invalidacion);
                insertadosConInvalidacion.put(claveMes, insertadosConInvalidacion.get(claveMes) + ajustado);
                if (!esUltimaLlamada) {
                    BigDecimal costoLead = costoPorMes.getOrDefault(claveMes, BigDecimal.ZERO);
                    acumularMonto(costosTotalesPorMes, claveMes,
                            costoLead.multiply(BigDecimal.valueOf(ajustado)));
                }
            }

            acumularConteosPorMes(ejecutarConsulta(consultaVentasBrutas,
                    crearParametros("year", year, "uuidProvider", uuidProvider)), ventasBrutas);

            acumularConteosPorMes(ejecutarConsulta(consultaVentasNetas,
                    crearParametros("year", year, "uuidProvider", uuidProvider, "status", StatusVentaEnum.EN_EJECUCION)),
                    ventasNetas);

            acumularConteosPorMes(ejecutarConsulta(consultaVentasCaidas,
                    crearParametros("year", year, "uuidProvider", uuidProvider, "statuses", ESTADOS_VENTAS_CAIDAS)),
                    ventasCaidas);

            List<Object[]> registrosCerrados = ejecutarConsulta(consultaCerrados,
                    crearParametros("year", year, "uuidProvider", uuidProvider, "status", StatusAcquisitionEnum.CERRADO));
            for (Object[] registro : registrosCerrados) {
                int indiceMes = ((Number) registro[0]).intValue();
                int cantidad = ((Number) registro[1]).intValue();
                String claveMes = CLAVES_MESES[indiceMes - 1];
                cerrados.put(claveMes, cerrados.get(claveMes) + cantidad);
                if (esUltimaLlamada) {
                    BigDecimal costoLead = costoPorMes.getOrDefault(claveMes, BigDecimal.ZERO);
                    acumularMonto(costosTotalesPorMes, claveMes,
                            costoLead.multiply(BigDecimal.valueOf(cantidad)));
                }
            }

            acumularConteosPorMes(ejecutarConsulta(consultaCerrados,
                    crearParametros("year", year, "uuidProvider", uuidProvider, "status", StatusAcquisitionEnum.ABIERTO)),
                    abiertos);
        }

        List<Map<String, Object>> resultado = new ArrayList<>();
        int consecutivo = 1;

        ajustarCerradosSegunInsertados(cerrados, esUltimaLlamada ? insertados : insertadosConInvalidacion);

        Map<String, Object> filaInsertados = crearFilaBasica(consecutivo++,
                "fxLastCall".equals(reference) ? "Última llamada" : "Total Insertados",
                insertados, Arrays.asList(CLAVES_MESES));
        resultado.add(filaInsertados);

        if (!"fxLastCall".equals(reference)) {
            Map<String, Object> filaInsertadosInvalidacion = crearFilaBasica(consecutivo++,
                    "Total Inse. - invalidacion", insertadosConInvalidacion, Arrays.asList(CLAVES_MESES));
            resultado.add(filaInsertadosInvalidacion);
        }

        Map<String, Object> filaCerrados = crearFilaBasica(consecutivo++, "Cerrados", cerrados,
                Arrays.asList(CLAVES_MESES));
        resultado.add(filaCerrados);

        Map<String, Object> filaAbiertos = crearFilaBasica(consecutivo++, "Abiertos", abiertos,
                Arrays.asList(CLAVES_MESES));
        resultado.add(filaAbiertos);

        Map<String, Object> filaVentasBrutas = crearFilaBasica(consecutivo++, "Ventas Brutas", ventasBrutas,
                Arrays.asList(CLAVES_MESES));
        resultado.add(filaVentasBrutas);

        Map<String, Object> filaRatioBrutoInsertados = crearFilaPorcentaje(consecutivo++,
                "Conversión Bruta Insertados", ventasBrutas, insertados, Arrays.asList(CLAVES_MESES));
        resultado.add(filaRatioBrutoInsertados);

        Map<String, Object> filaRatioBrutoCerrados = crearFilaPorcentaje(consecutivo++,
                "Conversión Bruta Cerrados", ventasBrutas, cerrados, Arrays.asList(CLAVES_MESES));
        resultado.add(filaRatioBrutoCerrados);

        Map<String, Object> filaCpaBruta = crearFilaCpa(consecutivo++, "CPA Bruta", ventasBrutas, costosTotalesPorMes,
                Arrays.asList(CLAVES_MESES));
        resultado.add(filaCpaBruta);

        Map<String, Object> filaVentasNetas = crearFilaBasica(consecutivo++, "Ventas Netas", ventasNetas,
                Arrays.asList(CLAVES_MESES));
        resultado.add(filaVentasNetas);

        Map<String, Object> filaRatioNetoInsertados = crearFilaPorcentaje(consecutivo++,
                "Conversión Neta insertados", ventasNetas, insertados, Arrays.asList(CLAVES_MESES));
        resultado.add(filaRatioNetoInsertados);

        Map<String, Object> filaRatioNetoCerrados = crearFilaPorcentaje(consecutivo++,
                "Conversión Neta Cerrado", ventasNetas, cerrados, Arrays.asList(CLAVES_MESES));
        resultado.add(filaRatioNetoCerrados);

        Map<String, Object> filaCpaNeta = crearFilaCpa(consecutivo++, "CPA Neta", ventasNetas, costosTotalesPorMes,
                Arrays.asList(CLAVES_MESES));
        resultado.add(filaCpaNeta);

        Map<String, Object> filaVentasCaidas = crearFilaBasica(consecutivo++, TIPO_VENTAS_CAIDAS, ventasCaidas,
                Arrays.asList(CLAVES_MESES));
        resultado.add(filaVentasCaidas);

        Map<String, Object> filaPorcentajeVentasCaidas = crearFilaPorcentaje(consecutivo++,
                TIPO_PORCENTAJE_VENTAS_CAIDAS, ventasCaidas, ventasBrutas, Arrays.asList(CLAVES_MESES));
        resultado.add(filaPorcentajeVentasCaidas);

        Map<String, Integer> insertadosParaTotales = "fxLastCall".equals(reference) ? insertados : insertadosConInvalidacion;
        agregarColumnaTotales(resultado, insertados, insertadosParaTotales, cerrados, ventasBrutas, ventasNetas,
                ventasCaidas, costosTotalesPorMes);
        return resultado;
    }

    public List<Map<String, Object>> loadDaily(Calendar start, Calendar end, String uuidProvider, String reference) {
        Map<String, Integer> insertados = crearMapaDiasVacio(start, end);
        Map<String, Integer> insertadosConInvalidacion = crearMapaDiasVacio(start, end);
        Map<String, Integer> ventasBrutas = crearMapaDiasVacio(start, end);
        Map<String, Integer> ventasNetas = crearMapaDiasVacio(start, end);
        Map<String, Integer> cerrados = crearMapaDiasVacio(start, end);
        Map<String, Integer> abiertos = crearMapaDiasVacio(start, end);
        Map<String, Integer> ventasCaidas = crearMapaDiasVacio(start, end);
        boolean esUltimaLlamada = "fxLastCall".equals(reference);

        String consultaInsertados;
        String consultaVentasBrutas;
        String consultaVentasNetas;
        String consultaCerrados;
        String consultaVentasCaidas;

        if ("fxLastCall".equals(reference)) {
            consultaInsertados = "AcquisitionEntity.countInsertedByDayLastCall";
            consultaVentasBrutas = "VentaEntity.countByAcqLastCallDay";
            consultaVentasNetas = "VentaEntity.countByAcqLastCallDayStatus";
            consultaCerrados = "AcquisitionEntity.countClosedByDayLastCall";
            consultaVentasCaidas = "VentaEntity.countByAcqLastCallDayStatusIn";
        } else {
            consultaInsertados = "AcquisitionEntity.countInsertedByDay";
            consultaVentasBrutas = "VentaEntity.countByAcqInsertionDay";
            consultaVentasNetas = "VentaEntity.countByAcqInsertionDayStatus";
            consultaCerrados = "AcquisitionEntity.countClosedByDay";
            consultaVentasCaidas = "VentaEntity.countByAcqInsertionDayStatusIn";
        }

        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd-MM-yyyy");
        formatoFecha.setTimeZone(ZONA_HORARIA_ESPANIA);

        acumularConteosPorFecha(ejecutarConsulta(consultaInsertados,
                crearParametros("start", start, "end", end, "uuidProvider", uuidProvider)), insertados, formatoFecha);

        Map<String, BigDecimal> invalidacionPorDia = loadInvalidacionByDay(start, end, uuidProvider);
        for (String clave : insertados.keySet()) {
            int totalInsertados = insertados.get(clave);
            BigDecimal invalidacion = invalidacionPorDia.getOrDefault(clave, BigDecimal.ZERO);
            insertadosConInvalidacion.put(clave, calcularInsertadosConInvalidacion(totalInsertados, invalidacion));
        }

        acumularConteosPorFecha(ejecutarConsulta(consultaVentasBrutas,
                crearParametros("start", start, "end", end, "uuidProvider", uuidProvider)), ventasBrutas,
                formatoFecha);

        acumularConteosPorFecha(ejecutarConsulta(consultaVentasNetas,
                crearParametros("start", start, "end", end, "uuidProvider", uuidProvider, "status",
                        StatusVentaEnum.EN_EJECUCION)), ventasNetas, formatoFecha);

        acumularConteosPorFecha(ejecutarConsulta(consultaVentasCaidas,
                crearParametros("start", start, "end", end, "uuidProvider", uuidProvider, "statuses",
                        ESTADOS_VENTAS_CAIDAS)), ventasCaidas, formatoFecha);

        acumularConteosPorFecha(ejecutarConsulta(consultaCerrados,
                crearParametros("start", start, "end", end, "uuidProvider", uuidProvider, "status",
                        StatusAcquisitionEnum.CERRADO)), cerrados, formatoFecha);

        acumularConteosPorFecha(ejecutarConsulta(consultaCerrados,
                crearParametros("start", start, "end", end, "uuidProvider", uuidProvider, "status",
                        StatusAcquisitionEnum.ABIERTO)), abiertos, formatoFecha);

        List<Map<String, Object>> resultado = new ArrayList<>();
        int consecutivo = 1;
        Iterable<String> claves = insertados.keySet();

        ajustarCerradosSegunInsertados(cerrados, esUltimaLlamada ? insertados : insertadosConInvalidacion);

        Map<String, Object> filaInsertados = crearFilaBasica(consecutivo++,
                "fxLastCall".equals(reference) ? "Última llamada" : "Total Insertados", insertados, claves);
        resultado.add(filaInsertados);

        if (!esUltimaLlamada) {
            Map<String, Object> filaInsertadosInvalidacion = crearFilaBasica(consecutivo++,
                    "Total Inse. - invalidacion", insertadosConInvalidacion, claves);
            resultado.add(filaInsertadosInvalidacion);
        }

        Map<String, Object> filaCerrados = crearFilaBasica(consecutivo++, "Cerrados", cerrados, claves);
        resultado.add(filaCerrados);

        Map<String, Object> filaAbiertos = crearFilaBasica(consecutivo++, "Abiertos", abiertos, claves);
        resultado.add(filaAbiertos);

        Map<String, Object> filaVentasBrutas = crearFilaBasica(consecutivo++, "Ventas Brutas", ventasBrutas, claves);
        resultado.add(filaVentasBrutas);

        Map<String, Object> filaRatioBrutoInsertados = crearFilaPorcentaje(consecutivo++,
                "Conversión Bruta Insertados", ventasBrutas, insertados, claves);
        resultado.add(filaRatioBrutoInsertados);

        Map<String, Object> filaRatioBrutoCerrados = crearFilaPorcentaje(consecutivo++,
                "Conversión Bruta Cerrados", ventasBrutas, cerrados, claves);
        resultado.add(filaRatioBrutoCerrados);

        Map<String, BigDecimal> costoLeadPorDia = uuidProvider != null ? loadCostoLeadByDay(start, end, uuidProvider)
                : null;
        Map<String, BigDecimal> costosTotalesPorDia = calcularCostosTotales(esUltimaLlamada ? cerrados : insertadosConInvalidacion,
                costoLeadPorDia, claves);

        Map<String, Object> filaCpaBruta = crearFilaCpa(consecutivo++, "CPA Bruta", ventasBrutas, costosTotalesPorDia,
                claves);
        resultado.add(filaCpaBruta);

        Map<String, Object> filaVentasNetas = crearFilaBasica(consecutivo++, "Ventas Netas", ventasNetas, claves);
        resultado.add(filaVentasNetas);

        Map<String, Object> filaRatioNetoInsertados = crearFilaPorcentaje(consecutivo++,
                "Conversión Neta insertados", ventasNetas, insertados, claves);
        resultado.add(filaRatioNetoInsertados);

        Map<String, Object> filaRatioNetoCerrados = crearFilaPorcentaje(consecutivo++,
                "Conversión Neta Cerrado", ventasNetas, cerrados, claves);
        resultado.add(filaRatioNetoCerrados);

        Map<String, Object> filaCpaNeta = crearFilaCpa(consecutivo++, "CPA Neta", ventasNetas, costosTotalesPorDia,
                claves);
        resultado.add(filaCpaNeta);

        Map<String, Object> filaVentasCaidas = crearFilaBasica(consecutivo++, TIPO_VENTAS_CAIDAS, ventasCaidas, claves);
        resultado.add(filaVentasCaidas);

        Map<String, Object> filaPorcentajeVentasCaidas = crearFilaPorcentaje(consecutivo++,
                TIPO_PORCENTAJE_VENTAS_CAIDAS, ventasCaidas, ventasBrutas, claves);
        resultado.add(filaPorcentajeVentasCaidas);

        agregarColumnaTotales(resultado, insertados, esUltimaLlamada ? null : insertadosConInvalidacion, cerrados,
                ventasBrutas, ventasNetas, ventasCaidas, costosTotalesPorDia);
        return resultado;
    }

    public List<Map<String, Object>> loadDaily(Calendar start, Calendar end, List<String> uuidProviders, String reference) {
        return loadDaily(start, end, uuidProviders, reference, (Map<String, BigDecimal>) null,
                (Map<String, BigDecimal>) null);
    }

    public List<Map<String, Object>> loadDaily(Calendar start, Calendar end, List<String> uuidProviders, String reference,
            String uuidProveedorPrincipal) {
        Map<String, BigDecimal> invalidacionCompartida = uuidProveedorPrincipal != null
                ? loadInvalidacionByDay(start, end, uuidProveedorPrincipal)
                : null;
        Map<String, BigDecimal> costoCompartido = uuidProveedorPrincipal != null
                ? loadCostoLeadByDay(start, end, uuidProveedorPrincipal)
                : null;
        return loadDaily(start, end, uuidProviders, reference, invalidacionCompartida, costoCompartido);
    }

    private List<Map<String, Object>> loadDaily(Calendar start, Calendar end, List<String> uuidProviders, String reference,
            Map<String, BigDecimal> invalidacionCompartida, Map<String, BigDecimal> costoCompartido) {
        if (uuidProviders == null || uuidProviders.isEmpty()) {
            return loadDaily(start, end, (String) null, reference);
        }

        Map<String, Integer> insertados = crearMapaDiasVacio(start, end);
        Map<String, Integer> insertadosConInvalidacion = crearMapaDiasVacio(start, end);
        Map<String, Integer> ventasBrutas = crearMapaDiasVacio(start, end);
        Map<String, Integer> ventasNetas = crearMapaDiasVacio(start, end);
        Map<String, Integer> cerrados = crearMapaDiasVacio(start, end);
        Map<String, Integer> abiertos = crearMapaDiasVacio(start, end);
        Map<String, BigDecimal> costosTotalesPorDia = crearMapaDiasBigDecimalVacio(start, end);
        Map<String, Integer> ventasCaidas = crearMapaDiasVacio(start, end);
        boolean esUltimaLlamada = "fxLastCall".equals(reference);

        String consultaInsertados;
        String consultaVentasBrutas;
        String consultaVentasNetas;
        String consultaCerrados;
        String consultaVentasCaidas;

        if ("fxLastCall".equals(reference)) {
            consultaInsertados = "AcquisitionEntity.countInsertedByDayLastCall";
            consultaVentasBrutas = "VentaEntity.countByAcqLastCallDay";
            consultaVentasNetas = "VentaEntity.countByAcqLastCallDayStatus";
            consultaCerrados = "AcquisitionEntity.countClosedByDayLastCall";
            consultaVentasCaidas = "VentaEntity.countByAcqLastCallDayStatusIn";
        } else {
            consultaInsertados = "AcquisitionEntity.countInsertedByDay";
            consultaVentasBrutas = "VentaEntity.countByAcqInsertionDay";
            consultaVentasNetas = "VentaEntity.countByAcqInsertionDayStatus";
            consultaCerrados = "AcquisitionEntity.countClosedByDay";
            consultaVentasCaidas = "VentaEntity.countByAcqInsertionDayStatusIn";
        }

        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd-MM-yyyy");
        formatoFecha.setTimeZone(ZONA_HORARIA_ESPANIA);

        for (String uuidProvider : uuidProviders) {
            Map<String, BigDecimal> costoPorDia = costoCompartido != null ? costoCompartido
                    : loadCostoLeadByDay(start, end, uuidProvider);
            Map<String, BigDecimal> invalidacionPorDia = invalidacionCompartida != null ? invalidacionCompartida
                    : loadInvalidacionByDay(start, end, uuidProvider);

            List<Object[]> registrosInsertados = ejecutarConsulta(consultaInsertados,
                    crearParametros("start", start, "end", end, "uuidProvider", uuidProvider));
            for (Object[] registro : registrosInsertados) {
                java.util.Date fecha = (java.util.Date) registro[0];
                int cantidad = ((Number) registro[1]).intValue();
                String clave = formatoFecha.format(fecha);
                insertados.put(clave, insertados.getOrDefault(clave, 0) + cantidad);
                BigDecimal invalidacion = invalidacionPorDia.getOrDefault(clave, BigDecimal.ZERO);
                int ajustado = calcularInsertadosConInvalidacion(cantidad, invalidacion);
                insertadosConInvalidacion.put(clave, insertadosConInvalidacion.getOrDefault(clave, 0) + ajustado);
                if (!esUltimaLlamada) {
                    BigDecimal costoLead = costoPorDia.getOrDefault(clave, BigDecimal.ZERO);
                    acumularMonto(costosTotalesPorDia, clave,
                            costoLead.multiply(BigDecimal.valueOf(ajustado)));
                }
            }

            acumularConteosPorFecha(ejecutarConsulta(consultaVentasBrutas,
                    crearParametros("start", start, "end", end, "uuidProvider", uuidProvider)), ventasBrutas,
                    formatoFecha);

            acumularConteosPorFecha(ejecutarConsulta(consultaVentasNetas,
                    crearParametros("start", start, "end", end, "uuidProvider", uuidProvider, "status",
                            StatusVentaEnum.EN_EJECUCION)), ventasNetas, formatoFecha);

            acumularConteosPorFecha(ejecutarConsulta(consultaVentasCaidas,
                    crearParametros("start", start, "end", end, "uuidProvider", uuidProvider, "statuses",
                            ESTADOS_VENTAS_CAIDAS)), ventasCaidas, formatoFecha);

            List<Object[]> registrosCerrados = ejecutarConsulta(consultaCerrados,
                    crearParametros("start", start, "end", end, "uuidProvider", uuidProvider, "status",
                            StatusAcquisitionEnum.CERRADO));
            for (Object[] registro : registrosCerrados) {
                java.util.Date fecha = (java.util.Date) registro[0];
                int cantidad = ((Number) registro[1]).intValue();
                String clave = formatoFecha.format(fecha);
                cerrados.put(clave, cerrados.getOrDefault(clave, 0) + cantidad);
                if (esUltimaLlamada) {
                    BigDecimal costoLead = costoPorDia.getOrDefault(clave, BigDecimal.ZERO);
                    acumularMonto(costosTotalesPorDia, clave,
                            costoLead.multiply(BigDecimal.valueOf(cantidad)));
                }
            }

            acumularConteosPorFecha(ejecutarConsulta(consultaCerrados,
                    crearParametros("start", start, "end", end, "uuidProvider", uuidProvider, "status",
                            StatusAcquisitionEnum.ABIERTO)), abiertos, formatoFecha);
        }

        List<Map<String, Object>> resultado = new ArrayList<>();
        int consecutivo = 1;
        Iterable<String> claves = insertados.keySet();

        ajustarCerradosSegunInsertados(cerrados, esUltimaLlamada ? insertados : insertadosConInvalidacion);

        Map<String, Object> filaInsertados = crearFilaBasica(consecutivo++,
                "fxLastCall".equals(reference) ? "Última llamada" : "Total Insertados", insertados, claves);
        resultado.add(filaInsertados);

        if (!esUltimaLlamada) {
            Map<String, Object> filaInsertadosInvalidacion = crearFilaBasica(consecutivo++,
                    "Total Inse. - invalidacion", insertadosConInvalidacion, claves);
            resultado.add(filaInsertadosInvalidacion);
        }

        Map<String, Object> filaCerrados = crearFilaBasica(consecutivo++, "Cerrados", cerrados, claves);
        resultado.add(filaCerrados);

        Map<String, Object> filaAbiertos = crearFilaBasica(consecutivo++, "Abiertos", abiertos, claves);
        resultado.add(filaAbiertos);

        Map<String, Object> filaVentasBrutas = crearFilaBasica(consecutivo++, "Ventas Brutas", ventasBrutas, claves);
        resultado.add(filaVentasBrutas);

        Map<String, Object> filaRatioBrutoInsertados = crearFilaPorcentaje(consecutivo++,
                "Conversión Bruta Insertados", ventasBrutas, insertados, claves);
        resultado.add(filaRatioBrutoInsertados);

        Map<String, Object> filaRatioBrutoCerrados = crearFilaPorcentaje(consecutivo++,
                "Conversión Bruta Cerrados", ventasBrutas, cerrados, claves);
        resultado.add(filaRatioBrutoCerrados);

        Map<String, Object> filaCpaBruta = crearFilaCpa(consecutivo++, "CPA Bruta", ventasBrutas, costosTotalesPorDia,
                claves);
        resultado.add(filaCpaBruta);

        Map<String, Object> filaVentasNetas = crearFilaBasica(consecutivo++, "Ventas Netas", ventasNetas, claves);
        resultado.add(filaVentasNetas);

        Map<String, Object> filaRatioNetoInsertados = crearFilaPorcentaje(consecutivo++,
                "Conversión Neta insertados", ventasNetas, insertados, claves);
        resultado.add(filaRatioNetoInsertados);

        Map<String, Object> filaRatioNetoCerrados = crearFilaPorcentaje(consecutivo++,
                "Conversión Neta Cerrado", ventasNetas, cerrados, claves);
        resultado.add(filaRatioNetoCerrados);

        Map<String, Object> filaCpaNeta = crearFilaCpa(consecutivo++, "CPA Neta", ventasNetas, costosTotalesPorDia,
                claves);
        resultado.add(filaCpaNeta);

        Map<String, Object> filaVentasCaidas = crearFilaBasica(consecutivo++, TIPO_VENTAS_CAIDAS, ventasCaidas, claves);
        resultado.add(filaVentasCaidas);

        Map<String, Object> filaPorcentajeVentasCaidas = crearFilaPorcentaje(consecutivo++,
                TIPO_PORCENTAJE_VENTAS_CAIDAS, ventasCaidas, ventasBrutas, claves);
        resultado.add(filaPorcentajeVentasCaidas);

        agregarColumnaTotales(resultado, insertados, esUltimaLlamada ? null : insertadosConInvalidacion, cerrados,
                ventasBrutas, ventasNetas, ventasCaidas, costosTotalesPorDia);
        return resultado;
    }

    public Map<String, BigDecimal> loadCplByMonth(int year, String uuidProvider) {
        Map<String, BigDecimal> cplPorMes = new LinkedHashMap<>();
        for (String claveMes : CLAVES_MESES) {
            cplPorMes.put(claveMes, BigDecimal.ZERO);
        }

        if (uuidProvider == null || uuidProvider.trim().isEmpty()) {
            return cplPorMes;
        }

        Query consulta = entityManager.createNamedQuery("ProviderConfigEntity.loadByUserUuid");
        consulta.setParameter("uuid", uuidProvider);
        @SuppressWarnings("unchecked")
        List<ProviderConfigEntity> configuraciones = consulta.getResultList();

        if (configuraciones == null || configuraciones.isEmpty()) {
            return cplPorMes;
        }

        configuraciones.sort((configA, configB) -> Long.compare(obtenerMillisInicio(configA.getInvalidacionDesde()),
                obtenerMillisInicio(configB.getInvalidacionDesde())));

        for (int indiceMes = 0; indiceMes < CLAVES_MESES.length; indiceMes++) {
            Calendar referenciaMes = construirReferenciaMes(year, indiceMes);
            BigDecimal valorEncontrado = null;
            for (ProviderConfigEntity configuracion : configuraciones) {
                if (configuracion.getCostoLead() == null) {
                    continue;
                }
                if (estaDentroDelRango(referenciaMes, configuracion.getInvalidacionDesde(),
                        configuracion.getInvalidacionHasta())) {
                    valorEncontrado = configuracion.getCostoLead();
                }
            }
            if (valorEncontrado != null) {
                cplPorMes.put(CLAVES_MESES[indiceMes], valorEncontrado);
            }
        }

        asignarTotalCpl(cplPorMes);
        return cplPorMes;
    }

    public Map<String, BigDecimal> loadCplByDay(Calendar inicio, Calendar fin, String uuidProvider) {
        Map<String, BigDecimal> cplPorDia = loadCostoLeadByDay(inicio, fin, uuidProvider);
        asignarTotalCpl(cplPorDia);
        return cplPorDia;
    }

    private Map<String, BigDecimal> loadCostoLeadByDay(Calendar inicio, Calendar fin, String uuidProvider) {
        Map<String, BigDecimal> costoPorDia = crearMapaDiasBigDecimalVacio(inicio, fin);
        if (uuidProvider == null || uuidProvider.trim().isEmpty()) {
            return costoPorDia;
        }

        Query consulta = entityManager.createNamedQuery("ProviderConfigEntity.loadByUserUuid");
        consulta.setParameter("uuid", uuidProvider);
        @SuppressWarnings("unchecked")
        List<ProviderConfigEntity> configuraciones = consulta.getResultList();

        if (configuraciones == null || configuraciones.isEmpty()) {
            return costoPorDia;
        }

        configuraciones.sort((configA, configB) -> Long.compare(obtenerMillisInicio(configA.getInvalidacionDesde()),
                obtenerMillisInicio(configB.getInvalidacionDesde())));

        Calendar iterador = (Calendar) inicio.clone();
        SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
        formato.setTimeZone(ZONA_HORARIA_ESPANIA);

        while (!iterador.after(fin)) {
            BigDecimal costoEncontrado = null;
            for (ProviderConfigEntity configuracion : configuraciones) {
                if (configuracion.getCostoLead() == null) {
                    continue;
                }
                if (estaDentroDelRango(iterador, configuracion.getInvalidacionDesde(),
                        configuracion.getInvalidacionHasta())) {
                    costoEncontrado = configuracion.getCostoLead();
                }
            }
            if (costoEncontrado != null) {
                costoPorDia.put(formato.format(iterador.getTime()), costoEncontrado);
            }
            iterador.add(Calendar.DAY_OF_MONTH, 1);
        }

        return costoPorDia;
    }

    private void asignarTotalCpl(Map<String, BigDecimal> valoresCpl) {
        if (valoresCpl == null || valoresCpl.isEmpty()) {
            return;
        }
        BigDecimal suma = BigDecimal.ZERO;
        int contadorPositivos = 0;
        int escala = 0;
        for (Map.Entry<String, BigDecimal> entrada : valoresCpl.entrySet()) {
            if ("total".equals(entrada.getKey())) {
                continue;
            }
            BigDecimal valor = entrada.getValue();
            if (valor != null && valor.compareTo(BigDecimal.ZERO) > 0) {
                suma = suma.add(valor);
                contadorPositivos++;
                escala = Math.max(escala, valor.scale());
            }
        }

        BigDecimal total = BigDecimal.ZERO;
        if (contadorPositivos > 0) {
            escala = Math.max(escala, 2);
            total = suma.divide(BigDecimal.valueOf(contadorPositivos), escala, RoundingMode.HALF_UP);
        }
        valoresCpl.put("total", total);
    }

    private Map<String, BigDecimal> loadInvalidacionByDay(Calendar inicio, Calendar fin, String uuidProvider) {
        Map<String, BigDecimal> invalidacionPorDia = crearMapaDiasBigDecimalVacio(inicio, fin);
        List<ProviderConfigEntity> configuraciones = cargarConfiguracionesInvalidacion(uuidProvider);
        if (configuraciones.isEmpty()) {
            return invalidacionPorDia;
        }

        Calendar iterador = (Calendar) inicio.clone();
        SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
        formato.setTimeZone(ZONA_HORARIA_ESPANIA);

        while (!iterador.after(fin)) {
            BigDecimal invalidacionEncontrada = null;
            for (ProviderConfigEntity configuracion : configuraciones) {
                if (configuracion.getInvalidacion() == null) {
                    continue;
                }
                if (estaDentroDelRango(iterador, configuracion.getInvalidacionDesde(),
                        configuracion.getInvalidacionHasta())) {
                    invalidacionEncontrada = normalizarInvalidacion(configuracion.getInvalidacion());
                }
            }
            if (invalidacionEncontrada != null) {
                invalidacionPorDia.put(formato.format(iterador.getTime()), invalidacionEncontrada);
            }
            iterador.add(Calendar.DAY_OF_MONTH, 1);
        }

        return invalidacionPorDia;
    }

    public Map<String, BigDecimal> loadInvalidacionByMonth(int year, String uuidProvider) {
        Map<String, BigDecimal> invalidacionPorMes = new LinkedHashMap<>();
        for (String claveMes : CLAVES_MESES) {
            invalidacionPorMes.put(claveMes, BigDecimal.ZERO);
        }

        List<ProviderConfigEntity> configuraciones = cargarConfiguracionesInvalidacion(uuidProvider);
        if (configuraciones.isEmpty()) {
            return invalidacionPorMes;
        }

        for (int indiceMes = 0; indiceMes < CLAVES_MESES.length; indiceMes++) {
            Calendar referenciaMes = construirReferenciaMes(year, indiceMes);
            BigDecimal valorEncontrado = null;
            for (ProviderConfigEntity configuracion : configuraciones) {
                if (configuracion.getInvalidacion() == null) {
                    continue;
                }
                if (estaDentroDelRango(referenciaMes, configuracion.getInvalidacionDesde(),
                        configuracion.getInvalidacionHasta())) {
                    valorEncontrado = normalizarInvalidacion(configuracion.getInvalidacion());
                }
            }
            if (valorEncontrado != null) {
                invalidacionPorMes.put(CLAVES_MESES[indiceMes], valorEncontrado);
            }
        }

        return invalidacionPorMes;
    }

    private List<ProviderConfigEntity> cargarConfiguracionesInvalidacion(String uuidProvider) {
        List<ProviderConfigEntity> configuraciones = consultarConfiguracionesProveedor(uuidProvider);
        if (!configuraciones.isEmpty()) {
            ordenarConfiguracionesPorFecha(configuraciones);
            return configuraciones;
        }

        String uuidProveedorPrincipal = buscarUuidProveedorPrincipal(uuidProvider);
        if (uuidProveedorPrincipal != null && !uuidProveedorPrincipal.equals(uuidProvider)) {
            List<ProviderConfigEntity> configuracionesPadre = consultarConfiguracionesProveedor(uuidProveedorPrincipal);
            if (!configuracionesPadre.isEmpty()) {
                ordenarConfiguracionesPorFecha(configuracionesPadre);
                return configuracionesPadre;
            }
        }

        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<ProviderConfigEntity> consultarConfiguracionesProveedor(String uuidProvider) {
        if (uuidProvider == null || uuidProvider.trim().isEmpty()) {
            return Collections.emptyList();
        }
        Query consulta = entityManager.createNamedQuery("ProviderConfigEntity.loadByUserUuid");
        consulta.setParameter("uuid", uuidProvider);
        List<ProviderConfigEntity> configuraciones = (List<ProviderConfigEntity>) consulta.getResultList();
        if (configuraciones == null || configuraciones.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(configuraciones);
    }

    private void ordenarConfiguracionesPorFecha(List<ProviderConfigEntity> configuraciones) {
        configuraciones.sort((configA, configB) -> Long.compare(obtenerMillisInicio(configA.getInvalidacionDesde()),
                obtenerMillisInicio(configB.getInvalidacionDesde())));
    }

    private String buscarUuidProveedorPrincipal(String uuidProvider) {
        AuthUserEntity usuario = cargarUsuarioPorUuid(uuidProvider);
        if (usuario == null) {
            return null;
        }
        if (Boolean.TRUE.equals(usuario.getIsMainProvider())) {
            return usuario.getUuid();
        }
        if (usuario.getUuidSupervisor() != null && !usuario.getUuidSupervisor().trim().isEmpty()) {
            return usuario.getUuidSupervisor();
        }
        if (usuario.getUuidCordinador() != null && !usuario.getUuidCordinador().trim().isEmpty()) {
            return usuario.getUuidCordinador();
        }
        return null;
    }

    private AuthUserEntity cargarUsuarioPorUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return null;
        }
        Query consulta = entityManager.createNamedQuery("AuthUserEntity.loadByUuid");
        consulta.setParameter("uuid", uuid);
        try {
            return (AuthUserEntity) consulta.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    public List<Map<String, Object>> loadConversionByProvider(int year, String reference) {
        String consultaInsertados;
        String consultaVentas;
        if ("fxLastCall".equals(reference)) {
            consultaInsertados = "AcquisitionEntity.countInsertedByProviderMonthLastCall";
            consultaVentas = "VentaEntity.countByProviderAcqLastCallMonth";
        } else {
            consultaInsertados = "AcquisitionEntity.countInsertedByProviderMonth";
            consultaVentas = "VentaEntity.countByProviderAcqInsertionMonth";
        }

        List<String> proveedoresPrincipales = cargarNombresProveedoresPrincipales();

        Map<String, Map<String, Integer>> insertadosPorProveedor = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> ventasPorProveedor = new LinkedHashMap<>();

        for (Object[] registro : ejecutarConsulta(consultaInsertados, crearParametros("year", year))) {
            String proveedor = normalizarProveedor((String) registro[0], proveedoresPrincipales);
            int indiceMes = ((Number) registro[1]).intValue();
            int cantidad = ((Number) registro[2]).intValue();
            Map<String, Integer> mapa = insertadosPorProveedor.computeIfAbsent(proveedor, clave -> crearMapaMesesVacio());
            String claveMes = CLAVES_MESES[indiceMes - 1];
            mapa.put(claveMes, mapa.get(claveMes) + cantidad);
        }

        for (Object[] registro : ejecutarConsulta(consultaVentas, crearParametros("year", year))) {
            String proveedor = normalizarProveedor((String) registro[0], proveedoresPrincipales);
            int indiceMes = ((Number) registro[1]).intValue();
            int cantidad = ((Number) registro[2]).intValue();
            Map<String, Integer> mapa = ventasPorProveedor.computeIfAbsent(proveedor, clave -> crearMapaMesesVacio());
            String claveMes = CLAVES_MESES[indiceMes - 1];
            mapa.put(claveMes, mapa.get(claveMes) + cantidad);
        }

        List<String> proveedores = new ArrayList<>();
        proveedores.addAll(insertadosPorProveedor.keySet());
        for (String proveedor : ventasPorProveedor.keySet()) {
            if (!proveedores.contains(proveedor)) {
                proveedores.add(proveedor);
            }
        }

        Map<String, Integer> totalVentas = crearMapaMesesVacio();
        Map<String, Integer> totalInsertados = crearMapaMesesVacio();
        for (String proveedor : proveedores) {
            Map<String, Integer> ventas = ventasPorProveedor.getOrDefault(proveedor, crearMapaMesesVacio());
            Map<String, Integer> insertadosProveedor = insertadosPorProveedor.getOrDefault(proveedor,
                    crearMapaMesesVacio());
            for (String claveMes : CLAVES_MESES) {
                totalVentas.put(claveMes, totalVentas.get(claveMes) + ventas.get(claveMes));
                totalInsertados.put(claveMes, totalInsertados.get(claveMes) + insertadosProveedor.get(claveMes));
            }
        }

        List<Map<String, Object>> resultado = new ArrayList<>();
        int consecutivo = 1;
        for (String proveedor : proveedores) {
            Map<String, Integer> ventas = ventasPorProveedor.getOrDefault(proveedor, crearMapaMesesVacio());
            Map<String, Integer> insertadosProveedor = insertadosPorProveedor.getOrDefault(proveedor,
                    crearMapaMesesVacio());
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("id", consecutivo++);
            fila.put("proveedor", proveedor.toUpperCase());
            int sumaVentas = 0;
            int sumaInsertados = 0;
            for (String claveMes : CLAVES_MESES) {
                int valorInsertados = insertadosProveedor.get(claveMes);
                int valorVentas = ventas.get(claveMes);
                sumaVentas += valorVentas;
                sumaInsertados += valorInsertados;
                fila.put(claveMes, valorInsertados == 0 ? "0%"
                        : formatearPorcentaje(valorVentas / (double) valorInsertados * 100));
            }
            fila.put("total",
                    sumaInsertados == 0 ? "0%" : formatearPorcentaje(sumaVentas / (double) sumaInsertados * 100));
            resultado.add(fila);
        }

        Map<String, Object> filaTotal = new LinkedHashMap<>();
        filaTotal.put("id", consecutivo);
        filaTotal.put("proveedor", "TOTAL");
        int sumaVentasGlobal = 0;
        int sumaInsertadosGlobal = 0;
        for (String claveMes : CLAVES_MESES) {
            int valorInsertados = totalInsertados.get(claveMes);
            int valorVentas = totalVentas.get(claveMes);
            sumaVentasGlobal += valorVentas;
            sumaInsertadosGlobal += valorInsertados;
            filaTotal.put(claveMes, valorInsertados == 0 ? "0%"
                    : formatearPorcentaje(valorVentas / (double) valorInsertados * 100));
        }
        filaTotal.put("total", sumaInsertadosGlobal == 0 ? "0%"
                : formatearPorcentaje(sumaVentasGlobal / (double) sumaInsertadosGlobal * 100));
        resultado.add(filaTotal);
        return resultado;
    }

    private Map<String, Integer> crearMapaMesesVacio() {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        for (String claveMes : CLAVES_MESES) {
            mapa.put(claveMes, 0);
        }
        return mapa;
    }

    private Map<String, BigDecimal> crearMapaMesesBigDecimalVacio() {
        Map<String, BigDecimal> mapa = new LinkedHashMap<>();
        for (String claveMes : CLAVES_MESES) {
            mapa.put(claveMes, BigDecimal.ZERO);
        }
        return mapa;
    }

    private Map<String, Integer> crearMapaDiasVacio(Calendar inicio, Calendar fin) {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
        formato.setTimeZone(ZONA_HORARIA_ESPANIA);
        Calendar iterador = (Calendar) inicio.clone();
        while (!iterador.after(fin)) {
            mapa.put(formato.format(iterador.getTime()), 0);
            iterador.add(Calendar.DAY_OF_MONTH, 1);
        }
        return mapa;
    }

    private Map<String, BigDecimal> crearMapaDiasBigDecimalVacio(Calendar inicio, Calendar fin) {
        Map<String, BigDecimal> mapa = new LinkedHashMap<>();
        SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
        formato.setTimeZone(ZONA_HORARIA_ESPANIA);
        Calendar iterador = (Calendar) inicio.clone();
        while (!iterador.after(fin)) {
            mapa.put(formato.format(iterador.getTime()), BigDecimal.ZERO);
            iterador.add(Calendar.DAY_OF_MONTH, 1);
        }
        return mapa;
    }

    private Map<String, Object> crearFilaBasica(int id, String tipo, Map<String, Integer> valores,
            Iterable<String> claves) {
        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("id", id);
        fila.put("tipo", tipo);
        for (String clave : claves) {
            fila.put(clave, valores.get(clave));
        }
        return fila;
    }

    private Map<String, Object> crearFilaPorcentaje(int id, String tipo, Map<String, Integer> dividendos,
            Map<String, Integer> divisores, Iterable<String> claves) {
        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("id", id);
        fila.put("tipo", tipo);
        for (String clave : claves) {
            int divisor = divisores.get(clave);
            int dividendo = dividendos.get(clave);
            fila.put(clave, divisor == 0 ? "0%" : formatearPorcentaje(dividendo / (double) divisor * 100));
        }
        return fila;
    }

    private Map<String, Object> crearFilaCpa(int id, String tipo, Map<String, Integer> ventas,
            Map<String, BigDecimal> costosTotales, Iterable<String> claves) {
        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("id", id);
        fila.put("tipo", tipo);
        for (String clave : claves) {
            int ventasValor = ventas.get(clave);
            BigDecimal costo = costosTotales != null ? costosTotales.getOrDefault(clave, BigDecimal.ZERO)
                    : BigDecimal.ZERO;
            fila.put(clave, ventasValor == 0 ? "0€" : formatearEuros(calcularCpa(ventasValor, costo)));
        }
        return fila;
    }

    private Map<String, BigDecimal> calcularCostosTotales(Map<String, Integer> base,
            Map<String, BigDecimal> costoPorClave, Iterable<String> claves) {
        Map<String, BigDecimal> costosTotales = new LinkedHashMap<>();
        if (claves == null) {
            return costosTotales;
        }
        for (String clave : claves) {
            int baseValor = base != null ? base.getOrDefault(clave, 0) : 0;
            BigDecimal costoLead = costoPorClave != null ? costoPorClave.getOrDefault(clave, BigDecimal.ZERO)
                    : BigDecimal.ZERO;
            BigDecimal costoTotal = costoLead.multiply(BigDecimal.valueOf(baseValor));
            costosTotales.put(clave, costoTotal);
        }
        return costosTotales;
    }

    private void acumularMonto(Map<String, BigDecimal> destino, String clave, BigDecimal monto) {
        if (destino == null || clave == null || monto == null) {
            return;
        }
        BigDecimal acumulado = destino.getOrDefault(clave, BigDecimal.ZERO);
        destino.put(clave, acumulado.add(monto));
    }

    private void ajustarCerradosSegunInsertados(Map<String, Integer> cerrados, Map<String, Integer> insertadosReferencia) {
        if (cerrados == null || insertadosReferencia == null) {
            return;
        }
        for (Map.Entry<String, Integer> entrada : cerrados.entrySet()) {
            String clave = entrada.getKey();
            Integer maximoPermitido = insertadosReferencia.get(clave);
            if (maximoPermitido == null) {
                continue;
            }
            int valorCerrados = entrada.getValue();
            if (valorCerrados > maximoPermitido) {
                cerrados.put(clave, maximoPermitido);
            }
        }
    }

    private void agregarColumnaTotales(List<Map<String, Object>> filas, Map<String, Integer> insertados,
            Map<String, Integer> insertadosInvalidacion, Map<String, Integer> cerrados,
            Map<String, Integer> ventasBrutas, Map<String, Integer> ventasNetas,
            Map<String, Integer> ventasCaidas, Map<String, BigDecimal> costosTotalesCpa) {
        if (filas == null || filas.isEmpty()) {
            return;
        }

        Map<String, Object> filaRatioBrutoInsertados = null;
        Map<String, Object> filaRatioBrutoCerrados = null;
        Map<String, Object> filaRatioNetoInsertados = null;
        Map<String, Object> filaRatioNetoCerrados = null;
        Map<String, Object> filaCpaBruta = null;
        Map<String, Object> filaCpaNeta = null;
        Map<String, Object> filaPorcentajeVentasCaidas = null;

        for (Map<String, Object> fila : filas) {
            String tipo = (String) fila.get("tipo");
            if ("Conversión Bruta Insertados".equals(tipo)) {
                filaRatioBrutoInsertados = fila;
            } else if ("Conversión Bruta Cerrados".equals(tipo)) {
                filaRatioBrutoCerrados = fila;
            } else if ("Conversión Neta insertados".equals(tipo)) {
                filaRatioNetoInsertados = fila;
            } else if ("Conversión Neta Cerrado".equals(tipo)) {
                filaRatioNetoCerrados = fila;
            } else if ("CPA Bruta".equals(tipo) || "CPA (cerrados)".equals(tipo)) {
                filaCpaBruta = fila;
            } else if ("CPA Neta".equals(tipo)) {
                filaCpaNeta = fila;
            } else if (TIPO_PORCENTAJE_VENTAS_CAIDAS.equals(tipo)) {
                filaPorcentajeVentasCaidas = fila;
            }
        }

        for (Map<String, Object> fila : filas) {
            String tipo = (String) fila.get("tipo");
            if (tipo != null
                    && (tipo.contains("Conversión") || tipo.startsWith("CPA") || TIPO_PORCENTAJE_VENTAS_CAIDAS.equals(tipo))) {
                continue;
            }
            fila.put("total", sumarColumnasNumericas(fila));
        }

        int totalInsertados = insertadosInvalidacion != null ? sumarValores(insertadosInvalidacion)
                : sumarValores(insertados);
        int totalVentasBrutas = sumarValores(ventasBrutas);
        int totalVentasNetas = sumarValores(ventasNetas);
        int totalCerrados = sumarValores(cerrados);
        int totalVentasCaidas = sumarValores(ventasCaidas);
        BigDecimal costoTotal = sumarCostos(costosTotalesCpa);

        if (filaRatioBrutoInsertados != null) {
            filaRatioBrutoInsertados.put("total", totalInsertados == 0 ? "0%"
                    : formatearPorcentaje(totalVentasBrutas / (double) totalInsertados * 100));
        }

        if (filaRatioBrutoCerrados != null) {
            filaRatioBrutoCerrados.put("total",
                    totalCerrados == 0 ? "0%" : formatearPorcentaje(totalVentasBrutas / (double) totalCerrados * 100));
        }

        if (filaRatioNetoInsertados != null) {
            filaRatioNetoInsertados.put("total", totalInsertados == 0 ? "0%"
                    : formatearPorcentaje(totalVentasNetas / (double) totalInsertados * 100));
        }

        if (filaRatioNetoCerrados != null) {
            filaRatioNetoCerrados.put("total",
                    totalCerrados == 0 ? "0%" : formatearPorcentaje(totalVentasNetas / (double) totalCerrados * 100));
        }

        if (filaPorcentajeVentasCaidas != null) {
            filaPorcentajeVentasCaidas.put("total", totalVentasBrutas == 0 ? "0%"
                    : formatearPorcentaje(totalVentasCaidas / (double) totalVentasBrutas * 100));
        }

        if (filaCpaBruta != null) {
            filaCpaBruta.put("total",
                    totalVentasBrutas == 0 ? "0€" : formatearEuros(calcularCpa(totalVentasBrutas, costoTotal)));
        }

        if (filaCpaNeta != null) {
            BigDecimal costoTotalNeto = costoTotal;
            filaCpaNeta.put("total",
                    totalVentasNetas == 0 ? "0€" : formatearEuros(calcularCpa(totalVentasNetas, costoTotalNeto)));
        }
    }

    private void reemplazarRatiosConConversion(Map<String, Object> filaRatio, int year, String reference,
            String providerName) {
        if (filaRatio == null || providerName == null || providerName.trim().isEmpty()) {
            return;
        }
        String proveedorNormalizado = normalizarProveedor(providerName, cargarNombresProveedoresPrincipales());
        List<Map<String, Object>> conversiones = loadConversionByProvider(year, reference);
        if (conversiones == null) {
            return;
        }
        for (Map<String, Object> conversion : conversiones) {
            Object proveedor = conversion.get("proveedor");
            if (proveedor != null && proveedorNormalizado.equalsIgnoreCase(proveedor.toString())) {
                for (String claveMes : CLAVES_MESES) {
                    filaRatio.put(claveMes, conversion.get(claveMes));
                }
                Object total = conversion.get("total");
                if (total != null) {
                    filaRatio.put("total", total);
                }
                break;
            }
        }
    }

    private int sumarColumnasNumericas(Map<String, Object> fila) {
        int suma = 0;
        for (Map.Entry<String, Object> entrada : fila.entrySet()) {
            String clave = entrada.getKey();
            if ("id".equals(clave) || "tipo".equals(clave) || "proveedor".equals(clave) || "total".equals(clave)) {
                continue;
            }
            Object valor = entrada.getValue();
            if (valor instanceof Number) {
                suma += ((Number) valor).intValue();
            }
        }
        return suma;
    }

    private int sumarValores(Map<String, Integer> valores) {
        if (valores == null || valores.isEmpty()) {
            return 0;
        }
        int suma = 0;
        for (Integer valor : valores.values()) {
            if (valor != null) {
                suma += valor;
            }
        }
        return suma;
    }

    private BigDecimal sumarCostos(Map<String, BigDecimal> costos) {
        BigDecimal total = BigDecimal.ZERO;
        if (costos == null || costos.isEmpty()) {
            return total;
        }
        for (BigDecimal costo : costos.values()) {
            if (costo != null) {
                total = total.add(costo);
            }
        }
        return total;
    }

    private List<Object[]> ejecutarConsulta(String nombreConsulta, Map<String, Object> parametros) {
        Query consulta = entityManager.createNamedQuery(nombreConsulta);
        for (Map.Entry<String, Object> entrada : parametros.entrySet()) {
            String nombreParametro = entrada.getKey();
            Object valor = entrada.getValue();

            if (valor == null) {
                Parameter<?> parametro = consulta.getParameter(nombreParametro);
                consulta.setParameter(parametro, null);
            } else if (valor instanceof Calendar) {
                consulta.setParameter(nombreParametro, (Calendar) valor, TemporalType.TIMESTAMP);
            } else if (valor instanceof java.util.Date) {
                consulta.setParameter(nombreParametro, (java.util.Date) valor, TemporalType.TIMESTAMP);
            } else {
                consulta.setParameter(nombreParametro, valor);
            }
        }
        @SuppressWarnings("unchecked")
        List<Object[]> resultados = consulta.getResultList();
        return resultados;
    }

    private Map<String, Object> crearParametros(Object... parametros) {
        Map<String, Object> mapa = new HashMap<>();
        for (int indice = 0; indice < parametros.length; indice += 2) {
            String clave = (String) parametros[indice];
            Object valor = parametros[indice + 1];
            mapa.put(clave, valor);
        }
        return mapa;
    }

    private void asignarConteosPorMes(List<Object[]> registros, Map<String, Integer> destino) {
        for (Object[] registro : registros) {
            int indiceMes = ((Number) registro[0]).intValue();
            int cantidad = ((Number) registro[1]).intValue();
            destino.put(CLAVES_MESES[indiceMes - 1], cantidad);
        }
    }

    private void acumularConteosPorMes(List<Object[]> registros, Map<String, Integer> destino) {
        for (Object[] registro : registros) {
            int indiceMes = ((Number) registro[0]).intValue();
            int cantidad = ((Number) registro[1]).intValue();
            String claveMes = CLAVES_MESES[indiceMes - 1];
            destino.put(claveMes, destino.get(claveMes) + cantidad);
        }
    }

    private void acumularConteosPorFecha(List<Object[]> registros, Map<String, Integer> destino,
            SimpleDateFormat formatoFecha) {
        for (Object[] registro : registros) {
            java.util.Date fecha = (java.util.Date) registro[0];
            int cantidad = ((Number) registro[1]).intValue();
            String clave = formatoFecha.format(fecha);
            destino.put(clave, destino.getOrDefault(clave, 0) + cantidad);
        }
    }

    private String formatearPorcentaje(double valor) {
        return crearFormatoDecimal().format(valor) + "%";
    }

    private String formatearEuros(long valor) {
        return valor + "€";
    }

    private DecimalFormat crearFormatoDecimal() {
        return new DecimalFormat("#.##");
    }

    private long calcularCpa(int ventas, BigDecimal costoTotal) {
        if (ventas <= 0 || costoTotal == null) {
            return 0L;
        }
        return costoTotal.divide(BigDecimal.valueOf(ventas), 0, RoundingMode.HALF_UP).longValue();
    }

    private int calcularInsertadosConInvalidacion(int insertados, BigDecimal invalidacion) {
        if (insertados <= 0 || invalidacion == null) {
            return Math.max(insertados, 0);
        }

        BigDecimal ratio = normalizarInvalidacion(invalidacion);
        if (ratio.compareTo(BigDecimal.ZERO) <= 0) {
            return Math.max(insertados, 0);
        }

        BigDecimal totalInsertados = BigDecimal.valueOf(insertados);
        BigDecimal restantes = totalInsertados.multiply(BigDecimal.ONE.subtract(ratio));
        if (restantes.compareTo(BigDecimal.ZERO) < 0) {
            restantes = BigDecimal.ZERO;
        }
        return restantes.setScale(0, RoundingMode.HALF_UP).intValue();
    }

    private BigDecimal normalizarInvalidacion(BigDecimal invalidacion) {
        if (invalidacion == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal ratio = invalidacion;
        if (ratio.compareTo(BigDecimal.ZERO) < 0) {
            ratio = BigDecimal.ZERO;
        }
        if (ratio.compareTo(BigDecimal.ONE) > 0) {
            ratio = ratio.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        }
        if (ratio.compareTo(BigDecimal.ONE) > 0) {
            ratio = BigDecimal.ONE;
        }
        return ratio;
    }

    private Calendar construirReferenciaMes(int year, int indiceMes) {
        Calendar calendario = Calendar.getInstance(ZONA_HORARIA_ESPANIA);
        calendario.set(Calendar.YEAR, year);
        calendario.set(Calendar.MONTH, indiceMes);
        calendario.set(Calendar.DAY_OF_MONTH, 1);
        calendario.set(Calendar.HOUR_OF_DAY, 12);
        calendario.set(Calendar.MINUTE, 0);
        calendario.set(Calendar.SECOND, 0);
        calendario.set(Calendar.MILLISECOND, 0);
        return calendario;
    }

    private boolean estaDentroDelRango(Calendar objetivo, Calendar inicio, Calendar fin) {
        long objetivoMillis = objetivo != null ? objetivo.getTimeInMillis() : 0L;
        long inicioMillis = obtenerMillisInicio(inicio);
        long finMillis = obtenerMillisFin(fin);
        return objetivoMillis >= inicioMillis && objetivoMillis <= finMillis;
    }

    private long obtenerMillisInicio(Calendar calendario) {
        if (calendario == null) {
            return Long.MIN_VALUE;
        }
        Calendar copia = Calendar.getInstance(ZONA_HORARIA_ESPANIA);
        copia.setTimeInMillis(calendario.getTimeInMillis());
        copia.set(Calendar.HOUR_OF_DAY, 0);
        copia.set(Calendar.MINUTE, 0);
        copia.set(Calendar.SECOND, 0);
        copia.set(Calendar.MILLISECOND, 0);
        return copia.getTimeInMillis();
    }

    private long obtenerMillisFin(Calendar calendario) {
        if (calendario == null) {
            return Long.MAX_VALUE;
        }
        Calendar copia = Calendar.getInstance(ZONA_HORARIA_ESPANIA);
        copia.setTimeInMillis(calendario.getTimeInMillis());
        copia.set(Calendar.HOUR_OF_DAY, 23);
        copia.set(Calendar.MINUTE, 59);
        copia.set(Calendar.SECOND, 59);
        copia.set(Calendar.MILLISECOND, 999);
        return copia.getTimeInMillis();
    }

    @SuppressWarnings("unchecked")
    private List<String> cargarNombresProveedoresPrincipales() {
        Query consulta = entityManager.createNamedQuery("AuthUserEntity.loadMainProviders");
        consulta.setParameter("role", RoleEnum.PROVIDER);
        List<AuthUserEntity> proveedores = (List<AuthUserEntity>) consulta.getResultList();
        List<String> nombres = new ArrayList<>();
        if (proveedores != null) {
            for (AuthUserEntity proveedor : proveedores) {
                if (proveedor.getUsername() != null) {
                    nombres.add(proveedor.getUsername().toUpperCase());
                }
            }
        }
        return nombres;
    }

    private String normalizarProveedor(String nombre, List<String> proveedoresPrincipales) {
        if (nombre == null) {
            return "";
        }
        String normalizado = nombre.toUpperCase();
        if ("PIN_CAPTA".equals(normalizado) || "ZAP_CAPTA".equals(normalizado)) {
            return "CAPTA";
        }
        if ("PIN_APEX".equals(normalizado) || "ZAP_APEX".equals(normalizado)) {
            return "APEX";
        }
        if (proveedoresPrincipales != null) {
            for (String proveedorPrincipal : proveedoresPrincipales) {
                if (normalizado.contains(proveedorPrincipal)) {
                    return proveedorPrincipal;
                }
            }
        }
        return normalizado;
    }
}
