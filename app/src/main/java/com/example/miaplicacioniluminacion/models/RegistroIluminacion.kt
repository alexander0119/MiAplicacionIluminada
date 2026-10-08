package com.example.miaplicacioniluminacion.models

// Importa la anotación DocumentId desde la librería de Firebase Firestore.
// Esta anotación le indica a Firestore que este campo debe llenarse automáticamente 
// con el ID único asignado al documento en la base de datos al momento de leerlo.
import com.google.firebase.firestore.DocumentId

// Declara una "data class" llamada RegistroIluminacion.
// En Kotlin, al definir valores por defecto para todas las propiedades en el constructor primario,
// el compilador genera automáticamente un constructor vacío (sin argumentos), 
// requisito indispensable para que Firebase Firestore pueda deserializar documentos a objetos.
data class RegistroIluminacion(
    // Identificador único del documento en Firestore.
    // Con @DocumentId, Firestore mapea el ID del documento directamente aquí.
    // Usamos 'var' para permitir mutabilidad y 'String?' con valor por defecto null para indicar
    // que antes de guardarse en Firestore aún no posee ID.
    @DocumentId
    var id: String? = null,

    // Ubicación física o área donde se realiza la medición (por ejemplo: "Sala", "Patio").
    // Se inicializa con cadena vacía "" por defecto y como 'var' para permitir modificar su valor.
    var ubicacion: String = "",

    // Nivel de luz ambiental registrado, con precisión decimal (Double).
    // Se inicializa en 0.0 por defecto y como 'var'.
    var nivelLuzAmbiental: Double = 0.0,

    // Intensidad de la luz configurada o medida expresada como número entero (Int).
    // Se inicializa en 0 por defecto y como 'var'.
    var intensidadLuz: Int = 0,

    // Fecha y hora del registro almacenada en formato texto (por ejemplo: "06/10/2026 18:30").
    // Usar String simplifica la visualización directa en pantalla y la edición sin conversiones complejas.
    var fechaRegistro: String = ""
)
