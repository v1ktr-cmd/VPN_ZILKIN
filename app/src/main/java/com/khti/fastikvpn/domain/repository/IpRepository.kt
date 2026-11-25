package com.khti.fastikvpn.domain.repository

// Источник внешнего IP
interface IpRepository {
    suspend fun getPublicIp(): String?
}
