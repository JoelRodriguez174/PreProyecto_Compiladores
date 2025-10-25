import java_cup.runtime.*;
%%
%class AnalizadorLexicoCUP
%public
%line
%column
%cup
%{
    private Symbol symbol(int type) {
        return new Symbol(type, yyline, yycolumn);
    }
    
    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline, yycolumn, value);
    }
%}

%%

[ \t\r\n]+                      { /* ignorar espacios y saltos */ }
"//".*                           { /* comentario de una línea */ }

/* Palabras reservadas */
"int"                            { return symbol(sym.INT, yytext()); }
"bool"                           { return symbol(sym.BOOL, yytext()); }
"void"                           { return symbol(sym.VOID, yytext()); }
"main"                           { return symbol(sym.MAIN, yytext()); }
"return"                         { return symbol(sym.RETURN, yytext()); }
"if"                             { return symbol(sym.IF, yytext()); }
"then"                           { return symbol(sym.THEN, yytext()); }
"else"                           { return symbol(sym.ELSE, yytext()); }
"while"                          { return symbol(sym.WHILE, yytext()); }

/* Constantes lógicas */
"true"                           { return symbol(sym.TRUE, yytext()); }
"false"                          { return symbol(sym.FALSE, yytext()); }

/* Operadores */
"=="                             { return symbol(sym.OPERADOR_IGUALDAD, yytext()); }
"&&"                             { return symbol(sym.OPERADOR_AND, yytext()); }
"||"                             { return symbol(sym.OPERADOR_OR, yytext()); }
"="                              { return symbol(sym.OPERADOR_ASIGNACION, yytext()); }
"+"                              { return symbol(sym.OPERADOR_SUMA, yytext()); }
"-"                              { return symbol(sym.OPERADOR_RESTA, yytext()); }
"*"                              { return symbol(sym.OPERADOR_MULTIPLICACION, yytext()); }
"/"                              { return symbol(sym.OPERADOR_DIVISION, yytext()); }
"%"                              { return symbol(sym.OPERADOR_MODULO, yytext()); }
"!"                              { return symbol(sym.OPERADOR_NOT, yytext()); }
"<"                              { return symbol(sym.OPERADOR_MENOR, yytext()); }
">"                              { return symbol(sym.OPERADOR_MAYOR, yytext()); }

/* Delimitadores */
"("                              { return symbol(sym.PARENTESIS_ABRE, yytext()); }
")"                              { return symbol(sym.PARENTESIS_CIERRA, yytext()); }
"{"                              { return symbol(sym.LLAVE_ABRE, yytext()); }
"}"                              { return symbol(sym.LLAVE_CIERRA, yytext()); }
";"                              { return symbol(sym.PUNTO_COMA, yytext()); }

/* Literales y identificadores */
[0-9]+                            { return symbol(sym.NUMERO, yytext()); }
[A-Za-z][A-Za-z0-9_]*             { return symbol(sym.IDENTIFICADOR, yytext()); }

/* Carácter ilegal */
.                                 { System.err.println("Carácter ilegal: " + yytext()); }

/* Fin de archivo */
<<EOF>>                            { return symbol(sym.EOF); }