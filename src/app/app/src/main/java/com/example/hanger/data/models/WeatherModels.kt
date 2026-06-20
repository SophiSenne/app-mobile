package com.hanger.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Resposta da Open-Meteo (https://api.open-meteo.com/v1/forecast).
 * Pedimos `current` (condições agora) e `hourly` (para montar manhã/tarde/noite
 * de hoje + amanhã de forma simples, sem precisar de outro endpoint).
 */
data class OpenMeteoResponse(
    @SerializedName("current") val current: OpenMeteoCurrent?,
    @SerializedName("hourly") val hourly: OpenMeteoHourly?,
    @SerializedName("hourly_units") val hourlyUnits: OpenMeteoHourlyUnits?
)

data class OpenMeteoCurrent(
    @SerializedName("time") val time: String,
    @SerializedName("temperature_2m") val temperature: Double,
    @SerializedName("relative_humidity_2m") val humidity: Int,
    @SerializedName("wind_speed_10m") val windSpeed: Double,
    @SerializedName("weather_code") val weatherCode: Int
)

data class OpenMeteoHourly(
    @SerializedName("time") val time: List<String>,
    @SerializedName("temperature_2m") val temperature: List<Double>,
    @SerializedName("weather_code") val weatherCode: List<Int>
)

data class OpenMeteoHourlyUnits(
    @SerializedName("temperature_2m") val temperatureUnit: String?
)

/**
 * Modelo simplificado e já traduzido para uso na UI (modal de temperatura),
 * derivado de [OpenMeteoResponse] em [WeatherRepository].
 */
data class WeatherSnapshot(
    val cityLabel: String,
    val currentTempC: Int,
    val condition: String,
    val conditionEmoji: String,
    val humidityPercent: Int,
    val windKmh: Int,
    val suggestion: String,
    val morningTempC: Int,
    val afternoonTempC: Int,
    val nightTempC: Int,
    val tomorrowTempC: Int,
    val tomorrowEmoji: String
)