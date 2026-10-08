package com.example.miaplicacioniluminacion.models

// Importa la anotación DocumentId desde la librería de Firebase Firestore
// Esta anotación le indica a Firestore que este campo debe llenarse automáticamente 
// con el ID único del documento cuando se lea desde la base de datos.
import com.google.firebase.firestore.DocumentId

// Declara una "data class" llamada Lectura. 
// Las data classes en Kotlin están diseñadas específicamente para almacenar datos 
// y automáticamente generan métodos útiles como toString(), equals(), y hashCode().
data class Lectura(
    // Define la propiedad 'id' de tipo String. 
    // La anotación @DocumentId es exclusiva de Firestore para capturar el ID del documento.
    // El valor por defecto "" (cadena vacía) permite crear el objeto sin necesidad de especificar un ID inicial,
    // lo cual es muy útil cuando vamos a crear un nuevo registro en Firestore (Firestore asignará el ID luego).
    @DocumentId
    var id: String = "",

    // Define la propiedad 'descripcion' de tipo String.
    // Almacenará texto descriptivo sobre la lectura.
    // Se inicializa con "" (cadena vacía) por defecto para evitar errores de valores nulos al crear el objeto.
    var descripcion: String = "",

    // Define la propiedad 'valor' de tipo Double (números con decimales).
    // Representa el valor numérico de la lectura capturada.
    // Se le asigna 0.0 por defecto para que siempre tenga un valor numérico inicial válido.
    var valor: Double = 0.0
)
