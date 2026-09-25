package com.example.calculadoranomina

// Constantes del modelo simplificado exigido por el taller (septiembre de 2026).
const val SMMLV = 1_750_905.0 // Decreto 1469 de 2025, según el taller.
const val AUXILIO_TRANSPORTE = 249_095.0 // Decreto 1470 de 2025, según el taller.
const val HORAS_MENSUALES = 210.0 // Ley 2101 de 2021, según el taller.
const val SALUD = 0.04 // Ley 100 de 1993.
const val PENSION = 0.04 // Ley 100 de 1993.
const val SOLIDARIDAD = 0.01 // Ley 797 de 2003.

enum class RangoSalarial { RANGO_1, RANGO_2, RANGO_3 }

data class ResultadoNomina(
    val valorHora: Double,
    val salarioBasico: Double,
    val totalHorasExtra: Double,
    val auxilioTransporte: Double,
    val totalDevengado: Double,
    val aporteSalud: Double,
    val aportePension: Double,
    val fondoSolidaridad: Double,
    val totalDeducciones: Double,
    val salarioNeto: Double,
    val rango: RangoSalarial
)

fun clasificarRango(salarioBasico: Double): RangoSalarial = when {
    salarioBasico <= 2 * SMMLV -> RangoSalarial.RANGO_1
    salarioBasico < 4 * SMMLV -> RangoSalarial.RANGO_2
    else -> RangoSalarial.RANGO_3
}

fun calcularNomina(
    salarioBasico: Double,
    horasDiurnas: Double,
    horasNocturnas: Double,
    esDominical: Boolean,
    transporteEmpresa: Boolean
): ResultadoNomina {
    require(salarioBasico.isFinite() && salarioBasico >= SMMLV)
    require(horasDiurnas.isFinite() && horasNocturnas.isFinite())
    require(horasDiurnas >= 0 && horasNocturnas >= 0 && horasDiurnas + horasNocturnas <= 48)
    val valorHora = salarioBasico / HORAS_MENSUALES
    val factorDiurno = if (esDominical) 2.15 else 1.25
    val factorNocturno = if (esDominical) 2.65 else 1.75
    val extras = horasDiurnas * valorHora * factorDiurno + horasNocturnas * valorHora * factorNocturno
    val ibc = salarioBasico + extras
    val auxilio = if (salarioBasico <= 2 * SMMLV && !transporteEmpresa) AUXILIO_TRANSPORTE else 0.0
    val salud = ibc * SALUD
    val pension = ibc * PENSION
    val solidaridad = if (ibc >= 4 * SMMLV) ibc * SOLIDARIDAD else 0.0
    val deducciones = salud + pension + solidaridad
    val devengado = ibc + auxilio
    return ResultadoNomina(valorHora, salarioBasico, extras, auxilio, devengado,
        salud, pension, solidaridad, deducciones, devengado - deducciones, clasificarRango(salarioBasico))
}
