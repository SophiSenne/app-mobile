package com.hanger.app.data.repository

import com.hanger.app.data.model.OpenMeteoResponse
import com.hanger.app.data.model.WeatherSnapshot
import com.hanger.app.data.network.RetrofitClient
import com.hanger.app.data.network.WeatherApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.roundToInt

sealed class WeatherResult {
    data class Success(val weather: WeatherSnapshot) : WeatherResult()
    data class Error(val message: String) : WeatherResult()
}

class WeatherRepository(
    private val api: WeatherApiService = RetrofitClient.weatherApiService
) {
    suspend fun getWeather(
        latitude: Double,
        longitude: Double,
        cityLabel: String
    ): WeatherResult = withContext(Dispatchers.IO) {
        try {
            val response = api.getForecast(latitude = latitude, longitude = longitude)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    WeatherResult.Success(body.toSnapshot(cityLabel))
                } else {
                    WeatherResult.Error("Resposta vazia do serviço de clima.")
                }
            } else {
                WeatherResult.Error("Não foi possível obter a previsão (${response.code()})")
            }
        } catch (e: Exception) {
            WeatherResult.Error(e.message ?: "Falha de conexão com o serviço de clima.")
        }
    }

    private fun OpenMeteoResponse.toSnapshot(cityLabel: String): WeatherSnapshot {
        val current = current
        val hourly = hourly

        val currentTemp = current?.temperature?.roundToInt() ?: 0
        val (condition, emoji) = weatherCodeToLabel(current?.weatherCode ?: 0)

        // Índices de hoje (manhã ~9h, tarde ~15h, noite ~21h) e de amanhã (meio-dia)
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)

        fun closestIndex(targetHour: Int, date: LocalDate): Int? {
            val times = hourly?.time ?: return null
            var bestIndex: Int? = null
            var bestDiff = Long.MAX_VALUE
            for (i in times.indices) {
                val dt = runCatching { LocalDateTime.parse(times[i]) }.getOrNull() ?: continue
                if (dt.toLocalDate() != date) continue
                val diff = kotlin.math.abs(dt.hour - targetHour).toLong()
                if (diff < bestDiff) {
                    bestDiff = diff
                    bestIndex = i
                }
            }
            return bestIndex
        }

        val morningIdx = closestIndex(9, today)
        val afternoonIdx = closestIndex(15, today)
        val nightIdx = closestIndex(21, today)
        val tomorrowIdx = closestIndex(12, tomorrow)

        fun tempAt(index: Int?): Int =
            index?.let { hourly?.temperature?.getOrNull(it)?.roundToInt() } ?: currentTemp

        fun emojiAt(index: Int?): String =
            index?.let { hourly?.weatherCode?.getOrNull(it) }?.let { weatherCodeToLabel(it).second } ?: emoji

        val suggestion = buildSuggestion(currentTemp, current?.weatherCode ?: 0)

        return WeatherSnapshot(
            cityLabel = cityLabel,
            currentTempC = currentTemp,
            condition = condition,
            conditionEmoji = emoji,
            humidityPercent = current?.humidity ?: 0,
            windKmh = current?.windSpeed?.roundToInt() ?: 0,
            suggestion = suggestion,
            morningTempC = tempAt(morningIdx),
            afternoonTempC = tempAt(afternoonIdx),
            nightTempC = tempAt(nightIdx),
            tomorrowTempC = tempAt(tomorrowIdx),
            tomorrowEmoji = emojiAt(tomorrowIdx)
        )
    }

    /** Mapeia o WMO weather code (usado pela Open-Meteo) para um rótulo em
     * português + emoji, similar ao usado no protótipo. */
    private fun weatherCodeToLabel(code: Int): Pair<String, String> = when (code) {
        0 -> "Ensolarado" to "☀️"
        1, 2 -> "Parcialmente nublado" to "🌤️"
        3 -> "Nublado" to "☁️"
        45, 48 -> "Neblina" to "🌫️"
        51, 53, 55, 56, 57 -> "Garoa" to "🌦️"
        61, 63, 65, 66, 67 -> "Chuva" to "🌧️"
        71, 73, 75, 77 -> "Neve" to "❄️"
        80, 81, 82 -> "Pancadas de chuva" to "🌧️"
        85, 86 -> "Pancadas de neve" to "🌨️"
        95, 96, 99 -> "Tempestade" to "⛈️"
        else -> "Tempo estável" to "🌙"
    }

    private fun buildSuggestion(tempC: Int, weatherCode: Int): String = when {
        weatherCode in listOf(61, 63, 65, 66, 67, 80, 81, 82, 95, 96, 99) ->
            "🌧️ Chuva à vista! Hanger sugere levar uma jaqueta impermeável ou guarda-chuva, e calçados que não escorreguem."
        tempC >= 28 ->
            "🌡️ Quente! Hanger sugere roupas leves em tecidos naturais — linho, algodão ou viscose. Evite peças escuras que absorvem calor. Sandálias abertas são a pedida certa."
        tempC >= 20 ->
            "🌤️ Clima agradável! Uma camada leve, como uma camisa ou blusa fina, já dá conta do recado o dia todo."
        tempC >= 12 ->
            "🧥 Fresco. Hanger sugere uma jaqueta ou casaco leve, e talvez uma segunda camada para o período da noite."
        else ->
            "❄️ Frio! Hanger sugere casacos mais pesados, cachecol e botas — capriche nas camadas para se manter aquecida."
    }
}