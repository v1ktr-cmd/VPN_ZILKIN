package com.khti.fastikvpn.ui.screens.home

import com.khti.fastikvpn.core.model.ConnectionState

// Данные для отрисовки главного экрана
data class HomeUiState(
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val ipAddress: String? = null,
    val isIpLoading: Boolean = true,
    val pingMs: Int? = null,
    val isPingLoading: Boolean = false,
    val isPingVisible: Boolean = false
)
