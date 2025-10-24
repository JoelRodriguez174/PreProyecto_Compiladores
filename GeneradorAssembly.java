import java.util.*;

public class GeneradorAssembly {
    private int tempCount = 0;
    private StringBuilder codigo = new StringBuilder();
    private int offset = 0;
    private Map<String, Integer> offsets = new HashMap<>();
    private int totalVariables = 0; // contador de variables declaradas

    private String nuevoTemporal() {
        tempCount++;
        return "T" + tempCount;
    }

    public String generar(Nodo raiz) {
        codigo.setLength(0);
        tempCount = 0;
        offset = 0;
        offsets.clear();
        totalVariables = 0;

        // Prólogo del main
        codigo.append("main:\n");
        codigo.append("    pushq %rbp\n");
        codigo.append("    movq %rsp, %rbp\n");

        // Recorremos el árbol
        recorrer(raiz);

        // Reservamos espacio en stack solo una vez (8 bytes por variable)
        if (totalVariables > 0) {
            codigo.insert(codigo.indexOf("movq %rsp, %rbp\n") + "movq %rsp, %rbp\n".length(),
                String.format("    subq $%d, %%rsp    # reservar espacio para %d variables\n",
                    totalVariables * 8, totalVariables));
        }

        // Epílogo
        codigo.append("    leave\n");
        codigo.append("    ret\n");

        return codigo.toString();
    }

    private String recorrer(Nodo nodo) {
        if (nodo == null) return "";

        String tipo = nodo.nombre;

        switch (tipo) {
            case "Programa":
            case "Main":
            case "Bloque":
            case "Declaraciones":
            case "Sentencias":
                for (Nodo hijo : nodo.hijos)
                    recorrer(hijo);
                return "";

            case "Declaracion": {
                // Identificar nombre de la variable
                if (!nodo.hijos.isEmpty()) {
                    Nodo varNode = nodo.hijos.get(0);
                    String var = varNode.valor != null ? varNode.valor : varNode.nombre;
                    totalVariables++;
                }
                return "";
            }

            case "Asignacion": {
                String var = recorrer(nodo.hijos.get(0));
                String valor = recorrer(nodo.hijos.get(1));

                if (esNumero(valor)) {
                    codigo.append(String.format("    movq $%s, %s   # %s = %s\n",
                            valor, getDireccion(var), var, valor));
                } else {
                    codigo.append(String.format("    movq %s, %%rax\n", getDireccion(valor)));
                    codigo.append(String.format("    movq %%rax, %s   # %s = %s\n",
                            getDireccion(var), var, valor));
                }
                return var;
            }

            case "Return": {
                String valor = recorrer(nodo.hijos.get(0));

                if (esNumero(valor)) {
                    codigo.append(String.format("    movq $%s, %%rax   # return %s\n", valor, valor));
                } else {
                    codigo.append(String.format("    movq %s, %%rax   # return %s\n", getDireccion(valor), valor));
                }
                return valor;
            }

            case "Identificador":
                return nodo.valor;

            case "Numero":
                return nodo.valor;

            case "Suma": {
                String izq = recorrer(nodo.hijos.get(0));
                String der = recorrer(nodo.hijos.get(1));

                codigo.append(String.format("    movq %s, %%rax\n", getDireccion(izq)));
                codigo.append(String.format("    addq %s, %%rax\n", getDireccion(der)));

                String t = nuevoTemporal();
                
                codigo.append(String.format("    movq %%rax, %s   # guardar %s\n", getDireccion(t), t));
                return t;
            }

            case "Multiplicacion": {
                String izq = recorrer(nodo.hijos.get(0));
                String der = recorrer(nodo.hijos.get(1));

                codigo.append(String.format("    movq %s, %%rax\n", getDireccion(izq)));
                codigo.append(String.format("    imulq %s, %%rax\n", getDireccion(der)));

                String t = nuevoTemporal();
            
                codigo.append(String.format("    movq %%rax, %s   # guardar %s\n", getDireccion(t), t));
                return t;
            }

            default:
                for (Nodo hijo : nodo.hijos)
                    recorrer(hijo);
                return "";
        }
    }

    private boolean esNumero(String s) {
        return s.matches("\\d+");
    }

    private String getDireccion(String nombre) {
        Integer off = offsets.get(nombre);
        if (off == null) {
            off = offset -= 8;
            offsets.put(nombre, off);
        }
        return off + "(%rbp)";
    }
}
