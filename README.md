# Codigo para Ejecutar
* del *.class
* del Parser.java
* del sym.java
* del AnalizadorLexico.java
* del AnalizadorLexicoCUP.java
* java -jar lib/java-cup-11b.jar -interface -parser Parser gramatica.cup
* java -jar lib/jflex-full-1.9.1.jar AnalizadorLexico.jflex
* java -jar lib/jflex-full-1.9.1.jar AnalizadorLexicoCUP.jflex
* javac -cp ".;lib/java-cup-11b-runtime.jar" *.java
* java -cp ".;lib/java-cup-11b-runtime.jar" Main test.txt /test_mul.txt
