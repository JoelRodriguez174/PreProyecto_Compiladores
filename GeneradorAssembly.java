import java.util.*;

public class GeneradorAssembly {
    private int tempCount = 0;
    private int labelCount = 0;
    private StringBuilder codigo = new StringBuilder();
    private int offset = 0;
    private Map<String, Integer> offsets = new HashMap<>();
    private int totalVariables = 0;

    private String nuevoTemporal() {
        tempCount++;
        return "T" + tempCount;
    }

    private String nuevaEtiqueta(String base) {
        labelCount++;
        return base + labelCount;
    }

    public String generar(Nodo raiz) {
        codigo.setLength(0);
        tempCount = 0;
        labelCount = 0;
        offset = 0;
        offsets.clear();
        totalVariables = 0;

        // Prólogo del main
        codigo.append("main:\n");
        codigo.append("    pushq %rbp\n");
        codigo.append("    movq %rsp, %rbp\n");

        // Recorremos el árbol
        recorrer(raiz);

        // Reservamos espacio en stack solo una vez
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
                // Contamos variables locales
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
                String valor = nodo.hijos.isEmpty() ? "0" : recorrer(nodo.hijos.get(0));

                if (esNumero(valor)) {
                    codigo.append(String.format("    movq $%s, %%rax   # return %s\n", valor, valor));
                } else {
                    codigo.append(String.format("    movq %s, %%rax   # return %s\n", getDireccion(valor), valor));
                }
                // No agregamos salto a end_main, el epílogo ya hace leave/ret
                return valor;
            }

            case "Identificador":
                return nodo.valor;

            case "Numero":
                return nodo.valor;

            case "Suma":
            case "Resta":
            case "Multiplicacion":
            case "Division":
            case "Modulo": {
                String izq = recorrer(nodo.hijos.get(0));
                String der = recorrer(nodo.hijos.get(1));
                codigo.append(String.format("    movq %s, %%rax\n", getDireccion(izq)));

                switch (tipo) {
                    case "Suma": codigo.append(String.format("    addq %s, %%rax\n", getDireccion(der))); break;
                    case "Resta": codigo.append(String.format("    subq %s, %%rax\n", getDireccion(der))); break;
                    case "Multiplicacion": codigo.append(String.format("    imulq %s, %%rax\n", getDireccion(der))); break;
                    case "Division": codigo.append(String.format("    cqto\n    idivq %s\n", getDireccion(der))); break;
                    case "Modulo": codigo.append(String.format("    cqto\n    idivq %s\n    movq %%rdx, %%rax\n", getDireccion(der))); break;
                }

                String t = nuevoTemporal();
                codigo.append(String.format("    movq %%rax, %s   # guardar %s\n", getDireccion(t), t));
                return t;
            }

            case "If": {
                String cond = recorrer(nodo.hijos.get(0));
                Nodo thenNode = nodo.hijos.get(1);
                Nodo elseNode = nodo.hijos.size() > 2 ? nodo.hijos.get(2) : null;

                String elseLabel = elseNode != null ? nuevaEtiqueta("else") : null;
                String endLabel = nuevaEtiqueta("endif");

                // Evaluar condición
                codigo.append(String.format("    cmpq $0, %s\n", getDireccion(cond)));
                if (elseNode != null) codigo.append(String.format("    je %s\n", elseLabel));
                else codigo.append(String.format("    je %s\n", endLabel));

                // Then
                recorrer(thenNode);
                if (elseNode != null) codigo.append(String.format("    jmp %s\n", endLabel));

                // Else
                if (elseNode != null) {
                    codigo.append(elseLabel + ":\n");
                    recorrer(elseNode);
                }

                codigo.append(endLabel + ":\n");
                return "";
            }

            case "While": {
                String condLabel = nuevaEtiqueta("while_cond");
                String endLabel = nuevaEtiqueta("endwhile");

                codigo.append(condLabel + ":\n");
                String cond = recorrer(nodo.hijos.get(0));
                codigo.append(String.format("    cmpq $0, %s\n", getDireccion(cond)));
                codigo.append(String.format("    je %s\n", endLabel));

                recorrer(nodo.hijos.get(1));
                codigo.append(String.format("    jmp %s\n", condLabel));
                codigo.append(endLabel + ":\n");
                return "";
            }

            case "True":
                return "1";
            case "False":
                return "0";

            case "Not": {
                String val = recorrer(nodo.hijos.get(0));
                String t = nuevoTemporal();
                codigo.append(String.format("    movq $0, %%rax\n"));
                codigo.append(String.format("    cmpq %s, %%rax\n", getDireccion(val)));
                codigo.append(String.format("    sete %%al\n"));
                codigo.append(String.format("    movzbq %%al, %%rax\n"));
                codigo.append(String.format("    movq %%rax, %s\n", getDireccion(t)));
                return t;
            }

            default:
                for (Nodo hijo : nodo.hijos)
                    recorrer(hijo);
                return "";
        }
    }

    private boolean esNumero(String s) {
        return s != null && s.matches("\\d+");
    }

    private String getDireccion(String nombre) {
        if (nombre == null) return "0";
        Integer off = offsets.get(nombre);
        if (off == null) {
            offset -= 8;
            off = offset;
            offsets.put(nombre, off);
        }
        return off + "(%rbp)";
    }
}