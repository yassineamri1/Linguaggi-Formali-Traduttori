import java.io.*;

public class Valutatore {
    private Lexer lex;
    private BufferedReader pbr;
    private Token look;

    public Valutatore(Lexer l, BufferedReader br) {
        lex = l;
        pbr = br;
        move();
    }

    void move() {
        look = lex.lexical_scan(pbr);
        System.err.println("token = " + look);
    }

    void error(String s) {

        throw new Error("near line " + lex.line + ": " + s);
    }

    void match(int t) {
        if (look.tag == t) {
            if (look.tag != Tag.EOF) move();
        } else error("syntax error");
    }

    public void start() {
        int expr_val;
        if (look.tag == '(' || look.tag ==  Tag.NUM) {
            expr_val = expr();
            match(Tag.EOF);
            System.out.println(expr_val);
        }
        else {
            error("errore in start");
        }
    }

    private int expr() {
        int term_val;
        int expr_val = 0;

        if (look.tag == '(' || look.tag ==  Tag.NUM) {
            term_val = term();
            expr_val =  exprp(term_val);
        }
        else {
            error("errore in expr");
        }

        return expr_val;
    }

    private int exprp(int exprp_i) {
        int term_val;
        int exprp_val = 0;

        if (look.tag == '+') {
            match('+');
            term_val = term();
            exprp_val = exprp(exprp_i + term_val);
        }
        else if (look.tag == '-') {
            match('-');
            term_val = term();
            exprp_val = exprp(exprp_i - term_val);
        }
        else if (look.tag ==')' || look.tag == Tag.EOF) {
            exprp_val = exprp_i;
        }
        else {
            error("errore in exprp");
        }

        return exprp_val;
    }

    private int term() {
        int fact_val;
        int term_val = 0;

        if (look.tag ==  '(' || look.tag ==  Tag.NUM) {
            fact_val = fact();
            term_val = termp(fact_val);
        }
        else {
            error("errore in term");
        }

        return term_val;
    }

    private int termp(int termp_i) {
        int fact_val;
        int termp_val = 0;

        if (look.tag ==  '*') {
            match('*');
            fact_val = fact();
            termp_val = termp(termp_i * fact_val);
        }
        else if (look.tag == '/') {
            match('/');
            fact_val = fact();
            termp_val = termp(termp_i / fact_val);
        }
        else if (look.tag == Tag.EOF || look.tag ==  '+'|| look.tag ==  '-' || look.tag == ')') {
            termp_val = termp_i;
        } else {
            error("errore in termp");
        }

        return termp_val;
    }

    private int fact() {
        int expr_val;
        int fact_val = 0;
        String s = "";
        double n = 0;

        if (look.tag == '(') {
            match('(');
            expr_val = expr();
            match(')');
            fact_val = expr_val;
        }
        else if (look.tag == Tag.NUM) {
            s = ((NumberTok)look).lexeme;
            n = Integer.parseInt(s);
            fact_val = (int) n;
            match(Tag.NUM);
        }
        else {
            error("errore in fact");
        }

        return fact_val;
    }

    public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "prova.txt";
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Valutatore valutatore = new Valutatore(lex, br);
            valutatore.start();
            br.close();
        } catch (IOException e) {e.printStackTrace();}
    }
}

