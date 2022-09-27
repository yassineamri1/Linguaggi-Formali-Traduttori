import java.io.*;

public class Parser {

    private Lexer lex;
    private BufferedReader pbr;
    private Token look;

    public Parser(Lexer l, BufferedReader br) {
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
            if (look.tag != Tag.EOF) {
                move();
            }
        } else {
            error("syntax error");
        }
    }

    public void start() {
        if (look.tag == '(' || look.tag == Tag.NUM) {
            expr();
            match(Tag.EOF);
        } else {
            System.out.println("Errore in start.");
        }

    }

    private void expr() {
        if (look.tag == Tag.NUM || look.tag == '(') {
            term();
            exprp();

        } else {
            error("Errore in Expr.");
        }

    }

    private void exprp() {
        if (look.tag == '+' || look.tag == '-' || look.tag == Tag.EOF || look.tag == ')') {
            switch (look.tag) {
                case '+':
                    match('+');
                    term();
                    exprp();
                    break;
                case '-':
                    match('-');
                    term();
                    exprp();
                    break;
					
				case ')':
                    break;
                case Tag.EOF:
                    break;

            }
        } else {
            System.out.println("Errore in exprp");
        }
    }

    private void term() {
        if (look.tag == Tag.NUM || look.tag == '(') {
            fact();
            termp();

        } else {
            error("Errore in Term.");
        }

    }

    private void termp() {
        if (look.tag == '*' || look.tag == '/' || look.tag == '+' || look.tag == '-' || look.tag == Tag.EOF || look.tag == ')') {
            switch (look.tag) {
                case '*':
                    move();
                    fact();
                    termp();
                    break;
                case '/':
                    move();
                    fact();
                    termp();
                    break;
					
				case Tag.EOF:

                case '+':

                case '-':

                case ')':
            }
        } else {
            System.out.println("Errore in termp");
        }
    }

    private void fact() {
        if (look.tag == Tag.NUM) {
            match(Tag.NUM);
        } else if (look.tag == '('){
            match('(');
            expr();
            match(')');
        } else
            error("errore in fact");
    }

    public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "prova.txt";
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Parser parser = new Parser(lex, br);
            parser.start();
            System.out.println("Input OK");
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
