%%

%class AnalizadorLexico
%public
%type Token
%line
%column

%{
    public static class Token {
        public String tipo;
        public String valor;
        public int linea;
        public int columna;
        
        public Token(String tipo, String valor, int linea, int columna) {
            this.tipo = tipo;
            this.valor = valor;
            this.linea = linea;
            this.columna = columna;
        }
        
        @Override
        public String toString() {
            return "Token{tipo='" + tipo + "', valor='" + valor + "'}";
        }
    }
    
    private Token token(String tipo, String valor) {
        return new Token(tipo, valor, yyline + 1, yycolumn + 1);
    }
%}

%%

[ \t\r\n]+                      { /* ignorar espacios y saltos */ }
"//".*                          { /* comentario de una línea */ }

"int"                           { return token("PALABRA_RESERVADA", yytext()); }
"bool"                          { return token("PALABRA_RESERVADA", yytext()); }
"void"                          { return token("PALABRA_RESERVADA", yytext()); }
"main"                          { return token("PALABRA_RESERVADA", yytext()); }
"return"                        { return token("PALABRA_RESERVADA", yytext()); }
"if"                            { return token("PALABRA_RESERVADA", yytext()); }
"then"                          { return token("PALABRA_RESERVADA", yytext()); }
"else"                          { return token("PALABRA_RESERVADA", yytext()); }
"while"                         { return token("PALABRA_RESERVADA", yytext()); }

"true"                          { return token("CONSTANTE_LOGICA", yytext()); }
"false"                         { return token("CONSTANTE_LOGICA", yytext()); }

"=="                            { return token("OPERADOR_IGUALDAD", yytext()); }
"&&"                            { return token("OPERADOR_AND", yytext()); }
"||"                            { return token("OPERADOR_OR", yytext()); }
"="                             { return token("OPERADOR_ASIGNACION", yytext()); }
"+"                             { return token("OPERADOR_SUMA", yytext()); }
"-"                             { return token("OPERADOR_RESTA", yytext()); }
"*"                             { return token("OPERADOR_MULTIPLICACION", yytext()); }
"/"                             { return token("OPERADOR_DIVISION", yytext()); }
"%"                             { return token("OPERADOR_MODULO", yytext()); }
"!"                             { return token("OPERADOR_NOT", yytext()); }
"<"                             { return token("OPERADOR_MENOR", yytext()); }
">"                             { return token("OPERADOR_MAYOR", yytext()); }

"("                             { return token("PARENTESIS_ABRE", yytext()); }
")"                             { return token("PARENTESIS_CIERRA", yytext()); }
"{"                             { return token("LLAVE_ABRE", yytext()); }
"}"                             { return token("LLAVE_CIERRA", yytext()); }
";"                             { return token("PUNTO_COMA", yytext()); }

[0-9]+                          { return token("NUMERO", yytext()); }
[A-Za-z][A-Za-z0-9_]*           { return token("IDENTIFICADOR", yytext()); }

.                               { return token("ERROR", yytext()); }

<<EOF>>                         { return token("EOF", ""); }