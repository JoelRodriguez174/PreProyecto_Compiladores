/**
 * Recorre el AST y lo interpreta (ejecuta).
 * Usa una Tabla de Símbolos para guardar las variables declaradas,
 * sus valores y validar el uso correcto de las mismas.
 */
public class Evaluador {
    private TablaSimbolos ts = new TablaSimbolos();
    private int blockDepth = 0;
    private boolean returned = false;
    private int returnValue = 0;

    public int evaluar(Nodo ast) {
        if (ast == null) return 0;
        if (returned) return returnValue;

        switch (ast.nombre) {
            case "Programa":
                return evaluarHijos(ast);

            case "Bloque":
                blockDepth++;
                int res = evaluarHijos(ast);
                blockDepth--;
                return res;

            case "Declaraciones":
            case "Sentencias":
                return evaluarHijos(ast);

            case "Declaracion":
                if (blockDepth != 1) {
                    throw new RuntimeException(
                            "Error semántico (línea " + ast.linea + "): declaración fuera del bloque global no permitida.");
                }

                String var = ast.hijos.get(1).valor;
                ts.declarar(var, ast.linea);

                if (ast.hijos.size() > 2) {
                    int valorInit = evaluar(ast.hijos.get(2));
                    ts.asignar(var, valorInit, ast.linea);
                }
                return 0;

            case "Asignacion":
                String nombreVar = ast.hijos.get(0).valor;
                int valor = evaluar(ast.hijos.get(1));
                ts.asignar(nombreVar, valor, ast.linea);
                return valor;

            case "Return":
                returnValue = ast.hijos.isEmpty() ? 0 : evaluar(ast.hijos.get(0));
                returned = true;
                return returnValue;

            case "Numero":
                return Integer.parseInt(ast.valor);

            case "True":
            case "true":
            case "BooleanoTrue":
                return 1;

            case "False":
            case "false":
            case "BooleanoFalse":
                return 0;

            case "Identificador":
                return ts.obtener(ast.valor, ast.linea);

            case "Suma":
                return evaluar(ast.hijos.get(0)) + evaluar(ast.hijos.get(1));

            case "Resta":
                return evaluar(ast.hijos.get(0)) - evaluar(ast.hijos.get(1));

            case "Multiplicacion":
                return evaluar(ast.hijos.get(0)) * evaluar(ast.hijos.get(1));

            case "Division":
                int divisor = evaluar(ast.hijos.get(1));
                if (divisor == 0)
                    throw new RuntimeException("Error: división por cero (línea " + ast.linea + ")");
                return evaluar(ast.hijos.get(0)) / divisor;

            case "Modulo":
                int mod = evaluar(ast.hijos.get(1));
                if (mod == 0)
                    throw new RuntimeException("Error: módulo por cero (línea " + ast.linea + ")");
                return evaluar(ast.hijos.get(0)) % mod;

            case "Menor":
                return evaluar(ast.hijos.get(0)) < evaluar(ast.hijos.get(1)) ? 1 : 0;

            case "Mayor":
                return evaluar(ast.hijos.get(0)) > evaluar(ast.hijos.get(1)) ? 1 : 0;

            case "Igual":
                return evaluar(ast.hijos.get(0)) == evaluar(ast.hijos.get(1)) ? 1 : 0;

            case "And":
            case "&&":
            case "Conjuncion":
                return (evaluar(ast.hijos.get(0)) != 0 && evaluar(ast.hijos.get(1)) != 0) ? 1 : 0;

            case "Or":
            case "||":
            case "Disyuncion":
                return (evaluar(ast.hijos.get(0)) != 0 || evaluar(ast.hijos.get(1)) != 0) ? 1 : 0;

            case "Not":
            case "!":
            case "Negacion":
                return (evaluar(ast.hijos.get(0)) == 0) ? 1 : 0;

            case "MenosUnario":
            case "Uminus":
                return -evaluar(ast.hijos.get(0));

            case "If":
                int cond = evaluar(ast.hijos.get(0));
                if (cond != 0)
                    evaluar(ast.hijos.get(1));
                else if (ast.hijos.size() > 2)
                    evaluar(ast.hijos.get(2));
                return 0;

            case "While":
                while (evaluar(ast.hijos.get(0)) != 0 && !returned) {
                    evaluar(ast.hijos.get(1));
                }
                return 0;

            default:
                return evaluarHijos(ast);
        }
    }

    private int evaluarHijos(Nodo n) {
        int result = 0;
        for (Nodo h : n.hijos) {
            if (returned) break;
            result = evaluar(h);
        }
        return result;
    }

    public void mostrarTabla() {
        System.out.println("Tabla de símbolos: " + ts);
    }
}
