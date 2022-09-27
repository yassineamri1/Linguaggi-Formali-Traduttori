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

    public void prog() {
        if (look.tag == Tag.PRINT || look.tag == Tag.READ || look.tag == Tag.COND || look.tag == Tag.WHILE || look.tag == '{' || look.tag == '=') {
            statlist();
            match(Tag.EOF);
        }
        else {
            error("errore in prog");
        }
    }



    private void statlist() {
        if (look.tag == Tag.PRINT || look.tag == Tag.READ || look.tag == Tag.COND || look.tag == Tag.WHILE || look.tag == '{' || look.tag == '=') {
            stat();
            statlistp();
        }
        else {
            error("errore in statlist");
        }
    }

    private void statlistp() {
        if (look.tag == ';') {
            match(';');
            stat();
            statlistp();
        }
        else if (look.tag == Tag.EOF || look.tag == '}') {
            
        }
        else {
            error("errore in statlistp");
        }
    }

    private void stat() {
        if (look.tag == '=') {
            match('=');
            match(Tag.ID);
            expr();
        }
        else if (look.tag == Tag.PRINT) {
            match(Tag.PRINT);
            match('(');
            exprlist();
            match(')');
        }
        else if (look.tag == Tag.READ) {
            match(Tag.READ);
            match('(');
            match(Tag.ID);
            match(')');
        }
        else if (look.tag == Tag.COND) {
            match(Tag.COND);
            whenlist();
            match(Tag.ELSE);
            stat();
        }
        else if (look.tag == Tag.WHILE) {
            match(Tag.WHILE);
            match('(');
            bexpr();
            match(')');
            stat();
        }
        else if (look.tag == '{') {
            match('{');
            statlist();
            match('}');
        }
        else {
            error("errore in stat");
        }
    }

    private void whenlist() {
        if (look.tag == Tag.WHEN) {
            whenitem();
            whenlistp();
        }
        else {
            error("errore in whenlist");
        }
    }

    private void whenlistp() {
        if (look.tag == Tag.WHEN) {
            whenitem();
            whenlistp();
        }
        else if (look.tag == Tag.ELSE) {
            
        }
        else {
            error("errore in whenlistp");
        }
    }

    private void whenitem() {
        if (look.tag == Tag.WHEN) {
            match(Tag.WHEN);
            match('(');
            bexpr();
            match(')');
            match(Tag.DO);
            stat();
        }
        else {
            error("errore in whenitem");
        }
    }


    private void bexpr() {
        if (look.tag == Tag.RELOP) {
            match(Tag.RELOP);
            expr();
            expr();
        }
        else {
            error("errore in bexpr");
        }
    }


    private void expr() {
        if (look.tag == '+') {
            match('+');
            match('(');
            exprlist();
            match(')');
        }
        else if (look.tag == '-') {
            match('-');
            expr();
            expr();
        } else if (look.tag == '*') {
            match('*');
            match('(');
            exprlist();
            match(')');
        }
        else if (look.tag == '/') {
            match('/');
            expr();
            expr();
        } else if (look.tag == Tag.ID) {
            match(Tag.ID);

        } else if (look.tag == Tag.NUM) {
            match(Tag.NUM);
        } else {
            error("errore in expr");
        }
    }

    private void exprlist() {
        if(look.tag == '+' || look.tag == '-' || look.tag == '/' || look.tag == '*' || look.tag == Tag.ID || look.tag == Tag.NUM) {
            expr();
            exprlistp();
        } else {
            error("errore in exprlist");
        }

    }

    private void exprlistp() {
        if(look.tag == '+' || look.tag == '-' || look.tag == '/' || look.tag == '*' || look.tag == Tag.ID || look.tag == Tag.NUM) {
            expr();
            exprlistp();
        } else if (look.tag == ')') {
            
        } else
            error("errore in exprlistp");

    }
    public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "prova.txt";
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Parser parser = new Parser(lex, br);
            parser.prog();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
