package com.hanger.app.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanger.app.data.model.WeatherSnapshot
import com.hanger.app.data.repository.WeatherRepository
import com.hanger.app.data.repository.WeatherResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WeatherUiState(
    val snapshot: WeatherSnapshot? = null,
    val isLoading: Boolean = false
)

class WeatherViewModel(
    private val repository: WeatherRepository = WeatherRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState(isLoading = true))
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        // Carrega clima de São Paulo por padrão
        loadWeather(latitude = -23.5505, longitude = -46.6333, cityLabel = "São Paulo")
    }

    fun loadWeather(
        latitude: Double,
        longitude: Double,
        cityLabel: String
    ) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState(isLoading = true)
            val result = repository.getWeather(latitude, longitude, cityLabel)
            _uiState.value = when (result) {
                is WeatherResult.Success -> WeatherUiState(snapshot = result.weather, isLoading = false)
                is WeatherResult.Error   -> WeatherUiState(isLoading = false)
            }
        }
    }
}
