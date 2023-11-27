package mx.ipn.escom.compiladores;

public enum TipoToken {
    IDENTIFICADOR,

    // Palabras reservadas
    SELECT, FROM, DISTINCT,

    // Caracteres
    COMA, PUNTO, ASTERISCO,

    // ERROR
    ERROR,
    // Final de cadena
    EOF
}
