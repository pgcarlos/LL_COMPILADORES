package mx.ipn.escom.compiladores;

import javax.management.StringValueExp;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.Stack;

public class Parser {

    private final List<Token> tokens;

    private final Token identificador = new Token(TipoToken.IDENTIFICADOR, "");
    private final Token select = new Token(TipoToken.SELECT, "select");
    private final Token from = new Token(TipoToken.FROM, "from");
    private final Token distinct = new Token(TipoToken.DISTINCT, "distinct");
    private final Token coma = new Token(TipoToken.COMA, ",");
    private final Token punto = new Token(TipoToken.PUNTO, ".");
    private final Token asterisco = new Token(TipoToken.ASTERISCO, "*");

    private final Token finCadena = new Token(TipoToken.EOF, "$");

    private int i = 0; // para entrada
    private int tope = 0; // para pila
    private boolean hayErrores = false; // no hay errores

    private Token aux; // Contiene tipo_token y lexema

    public Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    Stack <String> pila = new Stack<>();
    public void parse() {
        i = 0; // contador que recorre la lista de tokens
        tope = 0; // apuntador que apunta al tope de la pila
        aux = tokens.get(i); // aux es el el que contenga al token en la posición i
        pila.push("$");
        pila.push("Q"); // Agregamos Q a la pila

        if (aux.equals(finCadena)) // si se ha recorrido toda la entrada
            System.out.println("Ninguna entrada ingresada. Intente de nuevo");
        else
        {
            Q(); // Llamamos a la función

            if(i <= tokens.size()-1) // se recorrio toda la entrada
            {
                if (pila.get(tope).equals(finCadena.lexema)) // El tope de la pila y hasta donde se leyó la cadena son $ (la entrada fue aceptada)
                    System.out.println("Consulta válida");
                else
                    System.out.println("Consulta no válida");
            }
        }
    }
    void Q() {
        if (hayErrores) return;

        if (pila.get(tope + 1).equals("Q")) {
            pila.pop(); // Se saca a Q de la pila
            pila.push("T");
            tope++;
            pila.push("FROM");
            tope++;
            pila.push("D");
            tope++;
            pila.push("SELECT");
            tope++;

            String lex = "";
            lex = String.valueOf(aux.tipo); // Convertimos el tipo de token a string para poder hacer la comparación con el tope de la pila

            if (lex.equals(pila.get(tope))) // se verifica que el primer caracter sea 'select'
            {
                pila.pop(); //se saca a 'select' de la pila
                tope--; // el tope disminuye uno
                coincidir(select);
                D(); // Q → select  D
            }
            else
            {
               hayErrores = true;
               System.out.println("Error en la posición " + aux.posicion + "("+ aux.lexema +")" + "Se esperaba un select");
            }
        }
    }
    void D()
    {
        if (hayErrores) return;

        if (aux.equals(distinct)) // D → distinct P
        {
            pila.pop(); // Sacamos a D de la pila
            pila.push("P");
            pila.push("DISTINCT");
            tope++;

            String lex = "";
            lex = String.valueOf(aux.tipo); // Convertimos el tipo de token a string para poder hacer la comparación con el tope de la pila

            if (lex.equals(pila.get(tope)))
            {
                coincidir(distinct);
                pila.pop(); // Se saca distinct de la pila
                tope--;
                P();
                if (hayErrores) return;
            }
        }
        else if (aux.equals(asterisco)) // D → P
        {
            pila.pop(); // Sacamos a D de la pila
            pila.push("P"); // Metemos a P en la pila
            P();
            if (hayErrores) return;
        }
        else if (aux.equals(identificador)) // D → P → A → A2 A1, A2 → id A3
        {
            pila.pop(); // Sacamos a D de la pila
            pila.push("P"); // Metemos a P en la pila
            P();
            if (hayErrores) return;
        }
        else
        {
            hayErrores = true;
            System.out.println("Error en la posición: " + aux.posicion + "("+ aux.lexema +")" + ". Se esperaba un distinct, * o un identificador ");
            if (hayErrores) return;
        }

        // Se verifica que después de D haya un from
        String lex = "";
        lex = String.valueOf(aux.tipo); // Convertimos el tipo de token a string para poder hacer la comparación con el tope de la pila

        if (lex.equals(pila.get(tope)))// Q → select D from T
        {
            pila.pop(); // Sacamos from de la pila
            coincidir(from);
            tope--;
            // T → T2 T1 → id T3
            T();
        }
        else
        {
            hayErrores = true;
            System.out.println("Error en la posición " + aux.posicion + "("+ aux.lexema +")" + ". Se esperaba un from");
        }

    }
    void P()
    {
        if (hayErrores) return;

        if (aux.equals(asterisco)) // P → *
        {
            pila.pop(); // Sacamos a P de la pila
            pila.push("ASTERISCO");

            String lex = "";
            lex = String.valueOf(aux.tipo); // Convertimos el tipo de token a string para poder hacer la comparación con el tope de la pila

            if (lex.equals(pila.get(tope)))
            {
                coincidir(asterisco);
                pila.pop(); // Se saca el * de la pila
                tope--;
            }
        }
        else if (aux.equals(identificador)) // P → A
        {
            pila.pop(); // Sacamos a P de la pila
            pila.push("A"); // Metemos A en la pila
            A();
        }
        else
        {
            hayErrores = true;
            System.out.println("Error en la posicion " + aux.posicion + "(" + aux.lexema + ")" + ". Se esperaba un * o un identificador");
        }
    }
    void A()
    {
        pila.pop(); // Sacamos a A de la pila
        // A → A2 A1
        pila.push("A1");
        pila.push("A2");
        tope++;

        A2();
        if (hayErrores) return; // Si hay errores sintácticos en A2, A1 ya no se ejecuta y deja de leer la cadena
        A1();

    }
    void A1()
    {
        // A1 → , A | Ɛ
        if (aux.equals(coma) && tokens.get(i+1).equals(identificador)) // verificamos que si hay una coma, el token siguiente sea de identificador
        {
            pila.pop(); // Sacamos a A1 de la pila
            pila.push("A");
            pila.push("COMA");
            tope++;

            String lex = "";
            lex = String.valueOf(aux.tipo);
            if (lex.equals(pila.get(tope)))
            {
                coincidir(coma);
                pila.pop(); // Sacamos a la COMA de la pila
                tope--;
                A();
            }

        } else if (aux.equals(from)) {
            // Para A1 → Ɛ
            // Si A1 es Ɛ, pero el token que se lee es FROM (S(A1)), se saca a A1 de la pila
            pila.pop(); // Sacamos a A1 de la pila
            tope--;
        }
    }
    void A2()
    {
        if(hayErrores) return;

        pila.pop(); // Sacamos a A2 de la pila
        pila.push("A3");
        pila.push("IDENTIFICADOR");
        tope++;

        String lex = "";
        lex = String.valueOf(aux.tipo); // Convertimos en string el tipo de token para compararlo
        if (lex.equals(pila.get(tope))) // A2 → id A3
        {
            coincidir(identificador);
            pila.pop(); // Sacamos a IDENTIFICADOR de la pila
            tope--;
            A3();
        }
        else
        {
            hayErrores = true;
            System.out.println("Error en la posición " + aux.posicion + "(" + aux.lexema + ")" + ". Se esperaba un identificador");
        }
    }
    void A3()
    {
        if (hayErrores) return;
        if (aux.equals(punto)) // A3 → . id
        {
            pila.pop(); // Sacamos a A3 de la pila
            pila.push("IDENTIFICADOR");
            pila.push("PUNTO");
            tope++;

            String lex = "";
            lex = String.valueOf(aux.tipo);
            if (lex.equals(pila.get(tope)))
            {
                coincidir(punto);
                pila.pop(); // Sacamos a IDENTIFICADOR de la pila
                tope--;

                lex = String.valueOf(aux.tipo);
                if (lex.equals(pila.get(tope))) // Verificamos que después de un PUNTO haya un IDENTIFICADOR
                {
                    coincidir(identificador);
                    pila.pop(); // Sacamos a IDENTIFICADOR  de la pila
                    tope--;
                }
                else
                {
                    hayErrores = true;
                    System.out.println("Error en la posición: " + aux.posicion + "(" + aux.lexema + ")" + ". Se esperaba un identificador");
                }
            }
        }
        else if (aux.equals(from) || aux.equals(coma))
        {
            // Para A3 → Ɛ
            // Sabemos A → A2 A1, A2 → id A3
            // Esto es igual a A → id A3 A1
            // Si A3 es Ɛ, pero el token que se lee es COMA(por A1), se saca a A3 de la pila para que el tope pueda ser A1
            // Si A1 y A3 son Ɛ, pero el digito que se lee es FROM, se saca a A3 de la pila y posteriormente a A1(en su función)
            pila.pop();// Sacamos a A3 de la pila
            tope--;
        }
    }
    void T()
    {
        pila.pop(); // Sacamos a T de la pila
        // A → A2 A1
        pila.push("T1");
        pila.push("T2");
        tope++;
        T2();
        if (hayErrores) return; // Si hay errores sintácticos en T2, T1 ya no se ejecuta y deja de leer la cadena
        T1();
        if (hayErrores) return; // Si hay errores sintácticos en T1, se deja de leer la entrada

        if (aux.equals(finCadena)) // si se ha recorrido toda la entrada hasta aquí, es válida
            tope = 0;
    }
    void T1()
    {
        if (hayErrores) return;

        if (aux.equals(coma))
        {
            pila.pop(); // Sacamos a T1 de la pila
            pila.push("T");
            pila.push("COMA");
            tope++;

            String lex = "";
            lex = String.valueOf(aux.tipo); // Convertimos el tipo de token a string para poder hacer la comparación con el tope de la pila

            if (lex.equals(pila.get(tope)))
            {
                coincidir(coma);
                tope--;
                pila.pop(); // Sacamos a COMA de la pila
                T();
            }
        }
        else if (aux.equals(finCadena))
        {
            // Para T1 → Ɛ
            pila.pop(); // Sacamos a T1 de la pila
            tope--;
        }
        else if ((tokens.get(i-1).equals(identificador) && tokens.get(i-2).equals(identificador)) || (tokens.get(i-1).equals(identificador)&&tokens.get(i+1).equals(identificador)))
        {
            hayErrores = true;
            System.out.println("Error en la posición " + aux.posicion + "("+ aux.lexema +")" + ". Se esperaba una coma");
        }
    }
    void T2()
    {
        if(hayErrores) return;

        pila.pop(); // Sacamos a T2 de la pila
        pila.push("T3");
        pila.push("IDENTIFICADOR");
        tope++;

        String lex = "";
        lex = String.valueOf(aux.tipo); // Convertimos el tipo de token a string para poder hacer la comparación con el tope de la pila

        if (lex.equals(pila.get(tope)))
        {
            coincidir(identificador);
            tope--;
            pila.pop(); // Sacamos a IDENTIFICADOR de la pila
                T3();
        }
        else
        {
            hayErrores = true;
            System.out.println("Error en la posición " + aux.posicion + "("+ aux.lexema +")" + ". Se esperaba un identificador");
        }
    }
    void T3()
    {
        if (aux.equals(identificador))
        {
            pila.pop(); // Sacamos a T3 de la pila
            pila.push("IDENTIFICADOR");

            String lex = "";
            lex = String.valueOf(aux.tipo); // Convertimos el tipo de token a string para poder hacer la comparación con el tope de la pila

            if (lex.equals(pila.get(tope)))
            {
                coincidir(identificador);
                tope--;
                pila.pop(); // Sacamos a IDENTIFICADOR de la pila
            }
        } else if (aux.equals(coma)) {
            // Para A3 → Ɛ
            pila.pop(); // Sacamos a T3 de la pila
            tope--;
        }
    }
    void coincidir(Token t)
    {
        if(hayErrores) return;

        if(aux.tipo == t.tipo){
            i++;
            aux = tokens.get(i);
        }
        else{
            hayErrores = true;
            System.out.println("Error en la posición " + aux.posicion + "(" + aux.lexema + ")" + ". Se esperaba un  " + t.tipo);

        }
    }

}
