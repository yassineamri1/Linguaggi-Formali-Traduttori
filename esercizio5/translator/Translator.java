import java.io.*;
import java.util.*;


public class Translator {
    private Lexer lex;
    private BufferedReader pbr;
    private Token look;

    SymbolTable st = new SymbolTable();
    CodeGenerator code = new CodeGenerator();
    int count=0;
    public Translator(Lexer l, BufferedReader br) {
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

    private int getAddr(String s) {
        int id_addr = st.lookupAddress(s);
        if (id_addr == -1) {
            id_addr = count;
            st.insert(s, count++);
        }

        return id_addr;
    }

    public void prog() {
        if (look.tag == Tag.PRINT || look.tag == Tag.READ || look.tag == Tag.COND || look.tag == Tag.WHILE || look.tag == '{' || look.tag == '=') {
            int lnext_prog = code.newLabel();
            statlist(lnext_prog);
            code.emitLabel(lnext_prog);

            match(Tag.EOF);

            try {
                code.toJasmin();
            }
            catch(java.io.IOException e) {
                System.out.println("IO error\n");
            }
        }
        else {
            error("errore in prog");
        }
    }

    private void statlist(int sl_next) {
        if (look.tag == Tag.PRINT || look.tag == Tag.READ || look.tag == Tag.COND || look.tag == Tag.WHILE || look.tag == '{' || look.tag =='=') {
            int s_next = code.newLabel();
            stat(s_next);
            code.emitLabel(s_next);
            statlistp(sl_next);
        }
        else {
            error("errore in statlist");
        }
    }

    private void statlistp(int sl_next) {
        if (look.tag == ';') {
            match(';');
            int s_next = code.newLabel();
            stat(s_next);
            code.emitLabel(s_next);
            statlistp(sl_next);
        }
        else if (look.tag == Tag.EOF || look.tag == '}') {

        }
        else {
            error("errore in statlistp");
        }
    }

    private void stat(int lnext) {
        if (look.tag == '=') {
            match('=');
            int id_addr = getAddr(((Word)look).lexeme);
            match(Tag.ID);
            expr();
            code.emit(OpCode.istore, id_addr);
        }
        else if (look.tag == Tag.PRINT) {
            match(Tag.PRINT);
            match('(');
            exprlist(1);
            match(')');
        }
        else if (look.tag == Tag.READ) {
            match(Tag.READ);
            match('(');
            if (look.tag == Tag.ID) {
                int id_addr = getAddr(((Word)look).lexeme);
                match(Tag.ID);
                match(')');
                code.emit(OpCode.invokestatic, 0);
                code.emit(OpCode.istore, id_addr);
            }
            else {
                error("errore in stat: read");
            }
        }
        else if (look.tag == Tag.COND) {
            match(Tag.COND);
            whenlist(lnext);
            match(Tag.ELSE);
            stat(lnext);
        }
        else if (look.tag == Tag.WHILE) {
            match(Tag.WHILE);
            match('(');
            int ltrue = code.newLabel();
            int loop_head = code.newLabel();
            code.emitLabel(loop_head);
            bexpr(ltrue, lnext);
            match(')');
            code.emitLabel(ltrue);
            stat(loop_head);
            code.emit(OpCode.GOto, loop_head);
        }
        else if (look.tag == '{') {
            match('{');
            statlist(lnext);
            match('}');
        }
        else {
            error("errore in stat");
        }
    }

    private void whenlist(int next_whenlist) {
        if (look.tag == Tag.WHEN) {
            int w_next = code.newLabel();
            whenitem(w_next, next_whenlist);
            code.emitLabel(w_next);
            whenlistp(next_whenlist);
        }
        else {
            error("errore in whenlist");
        }
    }

    private void whenlistp(int next_whenlist) {
        if (look.tag == Tag.WHEN) {
            int w_next = code.newLabel();
            whenitem(w_next, next_whenlist);
            code.emitLabel(w_next);
            whenlistp(next_whenlist);
        }
        else if (look.tag == Tag.ELSE) {
            
        }
        else {
            error("errore in whenlistp");
        }
    }

    private void whenitem(int next_when, int next_whenlist) {
        if (look.tag == Tag.WHEN) {
            match(Tag.WHEN);
            match('(');
            int ltrue = code.newLabel();
            bexpr(ltrue, next_when);
            match(')');
            match(Tag.DO);
            code.emitLabel(ltrue);
            //match(Tag.DO);
            stat(next_whenlist);
            code.emit(OpCode.GOto, next_whenlist);
        }
        else {
            error("errore in whenitem");
        }
    }

    private void bexpr(int ltrue, int lfalse) {
        if (look.tag == Tag.RELOP) {

            OpCode op = null;
            if (look == Word.lt)
                op = OpCode.if_icmplt;
            else if (look == Word.le)
                op = OpCode.if_icmple;
            else if (look == Word.gt)
                op = OpCode.if_icmpgt;
            else if (look == Word.ge)
                op = OpCode.if_icmpge;
            else if (look == Word.eq)
                op = OpCode.if_icmpeq;
            else if (look == Word.ne)
                op = OpCode.if_icmpne;
            else
                error("errore in bexpr: relop");

            match(Tag.RELOP);
            expr();
            expr();

            code.emit(op, ltrue);
            code.emit(OpCode.GOto, lfalse);
        }
        else {
            error("errore in bexpr");
        }
    }

    private void expr() {
        if (look.tag == '+') {
            match('+');
            match('(');
            exprlist(2);
            match(')');
            
        } else if (look.tag == '-') {
            match('-');
            expr();
            expr();
            code.emit(OpCode.isub);
        } else if (look.tag == '*') {
            match('*');
            match('(');
            exprlist(3);
            match(')');
           
        } else if (look.tag == '/') {
            match('/');
            expr();
            expr();
            code.emit(OpCode.idiv);
        } else if (look.tag == Tag.ID) {
            int id_addr = st.lookupAddress(((Word)look).lexeme);
            if (id_addr == -1)
                error("errore in expr: identificatore non dichiarato");

            code.emit(OpCode.iload, id_addr);
            match(Tag.ID);
        } else if (look.tag == Tag.NUM) {
            String s = ((NumberTok)look).lexeme;
            double n = Integer.parseInt(s);
            int num = (int) n;
            code.emit(OpCode.ldc, num);
            match(Tag.NUM);

        } else {
            error("errore in expr");
        }
    }

    private void exprlist(int op) {
        if(look.tag == '+' || look.tag == '-' || look.tag == '/' || look.tag == '*' || look.tag == Tag.ID || look.tag == Tag.NUM) {
            expr();
			if(op==1){
                code.emit(OpCode.invokestatic,1);
            }
            exprlistp(op);
        } else {
            error("errore in exprlist");
        }

    }

    private void exprlistp(int op) {
        if(look.tag == '+' || look.tag == '-' || look.tag == '/' || look.tag == '*' || look.tag == Tag.ID || look.tag == Tag.NUM) {
            expr();
			switch(op){
				case 1:
					code.emit(OpCode.invokestatic,1);
					break;
				case 2:
					code.emit(OpCode.iadd);
					break;
				case 3:
					code.emit(OpCode.imul);
					break;
				default:
					break;
			}
            exprlistp(op);
        } else if (look.tag ==')') {
            
        } else
            error("errore in exprlistp");

    }

    public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "prova.txt";


        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Translator translator = new Translator(lex, br);
            translator.prog();
            br.close();
        } catch (IOException e) {e.printStackTrace();}
    }

}
