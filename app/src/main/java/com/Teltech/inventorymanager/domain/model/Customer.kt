package com.Teltech.inventorymanager.domain.model

data class Customer(
    val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val email: String = ""
)