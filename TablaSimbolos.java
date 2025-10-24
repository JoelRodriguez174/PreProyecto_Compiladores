import java.util.HashMap;
import java.util.Map;

/**
 * Clase que implementa una Tabla de Símbolos para el intérprete.
 * Se encarga de almacenar las variables declaradas en el programa,
 * junto con su valor y la línea donde fueron declaradas.
 * Permite:
 *  - Declarar variables
 *  - Asignarles valores
 *  - Obtener sus valores
 *  - Detectar errores si se usan variables no declaradas
 *  - Detectar redeclaraciones de variables
 */
public class TablaSimbolos {

    private static class Entrada {
        int valor;   
        int linea;  
        Entrada(int valor, int linea) {
            this.valor = valor;
            this.linea = linea;
        }
    }

    // Estructura principal: un mapa de nombreVariable -> Entrada
    private Map<String, Entrada> tabla = new HashMap<>();

    // Declara una nueva variable en la tabla de símbolos y si ya estaba declarada, lanza un error con información de la primera declaración.
     
    public void declarar(String nombre, int linea) {
        if (tabla.containsKey(nombre)) {
            throw new RuntimeException(
                "Error: variable '" + nombre + "' ya declarada (primera declaración en línea " 
                + tabla.get(nombre).linea + ")"
            );
        }
        // Se agrega la variable con valor inicial 0
        tabla.put(nombre, new Entrada(0, linea));
    }

    // Asigna un valor a una variable ya declarada, si la variable no existe en la tabla, lanza un error indicando la línea del fallo.
    
    public void asignar(String nombre, int valor, int linea) {
        Entrada e = tabla.get(nombre);
        if (e == null) {
            throw new RuntimeException(
                "Error en línea " + (linea + 1) + ": variable '" + nombre + "' no declarada"
            );
        }
        e.valor = valor;
    }

    // Obtiene el valor de una variable, si la variable no existe, lanza un error indicando la línea de la referencia.
    
    public int obtener(String nombre, int linea) {
        Entrada  e = tabla.get(nombre);
        if (e == null) {
            throw new RuntimeException(
                "Error en línea " + (linea + 1) + ": variable '" + nombre + "' no declarada"
            );
        }
        return e.valor;
    }

    // Devuelve una representación en texto de la tabla de símbolos, mostrando cada variable con su valor actual.
     
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{ ");
        for (var entry : tabla.entrySet()) {
            sb.append(entry.getKey())
              .append("=")
              .append(entry.getValue().valor)
              .append(" ");
        }
        sb.append("}");
        return sb.toString();
    }
}
