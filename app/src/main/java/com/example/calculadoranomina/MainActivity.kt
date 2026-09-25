package com.example.calculadoranomina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { PantallaNomina() } }
    }
}

private fun moneda(valor: Double): String = NumberFormat.getCurrencyInstance(
    Locale.Builder().setLanguage("es").setRegion("CO").build()
).apply { maximumFractionDigits = 0; minimumFractionDigits = 0 }.format(valor)

@Composable
fun CampoNumerico(
    etiqueta: String,
    valor: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions,
    isError: Boolean,
    modifier: Modifier = Modifier,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = valor, onValueChange = onValueChange, label = { Text(etiqueta) },
        modifier = modifier.fillMaxWidth(), singleLine = true, isError = isError,
        keyboardOptions = keyboardOptions, keyboardActions = keyboardActions
    )
}

@Composable
fun FilaInterruptor(etiqueta: String, activado: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(etiqueta, modifier = Modifier.weight(1f))
        Switch(checked = activado, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun FilaResultado(etiqueta: String, valor: Double, destacado: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(etiqueta, modifier = Modifier.weight(1f), fontWeight = if (destacado) FontWeight.Bold else FontWeight.Normal)
        Spacer(Modifier.width(8.dp))
        Text(moneda(valor), fontWeight = if (destacado) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun PantallaNomina() {
    var salario by rememberSaveable { mutableStateOf("") }
    var diurnas by rememberSaveable { mutableStateOf("") }
    var nocturnas by rememberSaveable { mutableStateOf("") }
    var dominical by rememberSaveable { mutableStateOf(false) }
    var transporteEmpresa by rememberSaveable { mutableStateOf(false) }
    var calculado by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf("") }
    var campoError by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val errorSalario = stringResource(R.string.error_salario)
    val errorMinimo = stringResource(R.string.error_minimo)
    val errorHoras = stringResource(R.string.error_horas)
    val errorLimite = stringResource(R.string.error_limite)

    fun validarYCalcular(): Boolean {
        val s = salario.trim().replace(',', '.').toDoubleOrNull()
        val d = if (diurnas.isBlank()) 0.0 else diurnas.trim().replace(',', '.').toDoubleOrNull()
        val n = if (nocturnas.isBlank()) 0.0 else nocturnas.trim().replace(',', '.').toDoubleOrNull()
        val fallo = when {
            s == null || !s.isFinite() -> "salario" to errorSalario
            s < SMMLV -> "salario" to errorMinimo
            d == null || !d.isFinite() || d < 0 -> "diurnas" to errorHoras
            n == null || !n.isFinite() || n < 0 -> "nocturnas" to errorHoras
            d + n > 48 -> "horas" to errorLimite
            else -> null
        }
        campoError = fallo?.first.orEmpty()
        error = fallo?.second.orEmpty()
        return fallo == null
    }

    val resultado = if (calculado) {
        calcularNomina(salario.trim().replace(',', '.').toDouble(),
            diurnas.ifBlank { "0" }.trim().replace(',', '.').toDouble(),
            nocturnas.ifBlank { "0" }.trim().replace(',', '.').toDouble(), dominical, transporteEmpresa)
    } else null

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.descripcion), style = MaterialTheme.typography.bodyMedium)
        CampoNumerico(stringResource(R.string.salario), salario, { salario = it; calculado = false; error = "" },
            KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            campoError == "salario", keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }))
        CampoNumerico(stringResource(R.string.diurnas), diurnas, { diurnas = it; calculado = false; error = "" },
            KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
            campoError == "diurnas" || campoError == "horas", keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }))
        CampoNumerico(stringResource(R.string.nocturnas), nocturnas, { nocturnas = it; calculado = false; error = "" },
            KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
            campoError == "nocturnas" || campoError == "horas", keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }))
        FilaInterruptor(stringResource(R.string.dominical), dominical) { dominical = it }
        FilaInterruptor(stringResource(R.string.transporte_empresa), transporteEmpresa) { transporteEmpresa = it }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { calculado = validarYCalcular(); focusManager.clearFocus() }, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.calcular))
            }
            OutlinedButton(onClick = {
                salario = ""; diurnas = ""; nocturnas = ""; dominical = false
                transporteEmpresa = false; calculado = false; error = ""; campoError = ""
                focusManager.clearFocus()
            }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.limpiar)) }
        }
        if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
        if (resultado != null) {
            HorizontalDivider()
            val imagen = when (resultado.rango) {
                RangoSalarial.RANGO_1 -> R.drawable.rango_1
                RangoSalarial.RANGO_2 -> R.drawable.rango_2
                RangoSalarial.RANGO_3 -> R.drawable.rango_3
            }
            val descripcion = when (resultado.rango) {
                RangoSalarial.RANGO_1 -> stringResource(R.string.rango_1)
                RangoSalarial.RANGO_2 -> stringResource(R.string.rango_2)
                RangoSalarial.RANGO_3 -> stringResource(R.string.rango_3)
            }
            Image(painterResource(imagen), contentDescription = descripcion,
                modifier = Modifier.size(72.dp).align(Alignment.CenterHorizontally))
            Text(descripcion, modifier = Modifier.align(Alignment.CenterHorizontally))
            Text(stringResource(R.string.devengado), style = MaterialTheme.typography.titleLarge)
            FilaResultado(stringResource(R.string.valor_hora), resultado.valorHora)
            FilaResultado(stringResource(R.string.salario_basico), resultado.salarioBasico)
            FilaResultado(stringResource(R.string.horas_extra), resultado.totalHorasExtra)
            FilaResultado(stringResource(R.string.auxilio), resultado.auxilioTransporte)
            FilaResultado(stringResource(R.string.total_devengado), resultado.totalDevengado, true)
            HorizontalDivider()
            Text(stringResource(R.string.deducciones), style = MaterialTheme.typography.titleLarge)
            FilaResultado(stringResource(R.string.salud), resultado.aporteSalud)
            FilaResultado(stringResource(R.string.pension), resultado.aportePension)
            FilaResultado(stringResource(R.string.solidaridad), resultado.fondoSolidaridad)
            FilaResultado(stringResource(R.string.total_deducciones), resultado.totalDeducciones, true)
            HorizontalDivider()
            Text(stringResource(R.string.salario_neto), style = MaterialTheme.typography.titleLarge)
            Text(moneda(resultado.salarioNeto), fontSize = 30.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VistaPrevia() { MaterialTheme { PantallaNomina() } }
