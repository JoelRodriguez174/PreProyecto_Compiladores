import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java_cup.runtime.Symbol;

public class Main {
    public static void main(String[] argv) {
        if (argv.length < 1) {
            System.err.println("Uso: java Main <archivo_fuente>");
            return;
        }

        try {
            // 1) Crear el parser usando el analizador léxico
            Parser p = new Parser(new AnalizadorLexicoCUP(new FileReader(argv[0])));

            // 2) Ejecutar el parser y recuperar el AST
            Symbol s = p.parse();
            Nodo ast = (Nodo) s.value;

            // 3) Mostrar el AST en consola
            System.out.println("=== AST generado ===");
            System.out.println(ast);

            // 4) Generar archivo DOT para Graphviz
            try (FileWriter fw = new FileWriter("ast.dot")) {
                fw.write(ast.toDot());
                System.out.println("\nArchivo 'ast.dot' generado correctamente.");
                System.out.println("Para convertirlo en imagen ejecuta:");
                System.out.println("dot -Tpng ast.dot -o ast.png");
            } catch (IOException e) {
                System.err.println("Error al escribir el archivo DOT: " + e.getMessage());
            }

            // 5) Interpretar el programa a partir del AST
            System.out.println("\n=== EJECUCIÓN DEL PROGRAMA ===");
            Evaluador eval = new Evaluador();
            int resultado = eval.evaluar(ast);
            eval.mostrarTabla();
            System.out.println("Valor de retorno: " + resultado);

            GeneradorAssembly generador = new GeneradorAssembly();
String asm = generador.generar(ast); // ast es tu nodo raíz
System.out.println("=== Pseudo-Assembly ===");
System.out.println(asm);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
