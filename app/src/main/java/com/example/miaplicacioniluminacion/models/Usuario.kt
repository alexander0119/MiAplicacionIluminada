package com.example.miaplicacioniluminacion.models

// FIREBASE: MODELO DE DATOS PARA GUARDAR INFORMACIÓN DE USUARIOS EN FIRESTORE
data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val fechaRegistro: Long = System.currentTimeMillis()
)
