import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;



public class Lexer {

    public static int line = 1;
    private char peek = ' ';

    private void readch(BufferedReader br) {
        try {
            peek = (char) br.read();
        } catch (IOException exc) {
            peek = (char) -1;
        }
    }

    private void trim(BufferedReader br) {
        while (peek == ' ' || peek == '\t' || peek == '\n'  || peek == '\r') {
            if (peek == '\n') line++;
            readch(br);
        }
    }

    public Token lexical_scan(BufferedReader br) {
        while (peek == ' ' || peek == '\t' || peek == '\n'  || peek == '\r' || peek == '/') {
            trim(br);

            if (peek == '/') {
                readch(br);
                if (peek == '*') {
                    boolean flag = true;
                    while (flag) {
                        readch(br);
                        if (peek == '*') {
                            readch(br);
                            if (peek == '/')
                                flag = false;
                        }
                    }
                    peek = ' ';
                } else if (peek == '/') {
                    boolean flag = true;
                    while (flag) {
                        readch(br);
                        if (peek == (char) -1) {
                            flag = false;
                        } else if (peek == '\n') {
                            line++;
                            flag = false;
                        }
                    }
                    peek = ' ';
                } else {
                    return Token.div;
                }
            }
        }

        switch (peek) {
            case '!':
                peek = ' ';
                return Token.not;

            case '(':
                peek = ' ';
                return Token.lpt;

            case ')':
                peek = ' ';
                return Token.rpt;

            case '{':
                peek = ' ';
                return Token.lpg;

            case '}':
                peek = ' ';
                return Token.rpg;

            case '+':
                peek = ' ';
                return Token.plus;

            case '-':
                peek = ' ';
                return Token.minus;

            case '*':
                peek = ' ';
                return Token.mult;

            case ';':
                peek = ' ';
                return Token.semicolon;

            case '&':
                readch(br);
                if (peek == '&') {
                    peek = ' ';
                    return Word.and;
                } else {
                    System.err.println("Erroneous character"
                            + " after & : "  + peek );
                    return null;
                }

            case '|':
                readch(br);
                if (peek == '|') {
                    peek = ' ';
                    return Word.or;
                } else {
                    System.err.println("Erroneous character"
                            + " after | : "  + peek );
                    return null;
                }

            case '=':
                readch(br);
                if (peek == '=') {
                    peek = ' ';
                    return Word.eq;
                } else if (peek == ' ') {
                    return Token.assign;
                } else {
                    System.err.println("Erroneous character"
                            + " after = : "  + peek );
                    return null;
                }


            case '<':
                readch(br);
                if(peek == '>') {
                    peek = ' ';
                    return Word.ne;
                } else if(peek == '=') {
                    peek = ' ';
                    return Word.le;
                } else {
                    peek = ' ';
                    return Word.lt;
                }

            case '>':
                readch(br);
                if(peek == '=') {
                    peek = ' ';
                    return Word.ge;
                } else {
                    peek = ' ';
                    return Word.gt;
                }

            case (char)-1:
                return new Token(Tag.EOF);

            default:
                if (Character.isLetter(peek) || peek == '_') {
                    String s = "";
                    if(peek == '_') {
                        while (peek == '_') {
                            s += peek;
                            readch(br);
                        }

                        if (!Character.isLetterOrDigit(peek)) {
                            System.err.println("Line " + line + ": Wront identifier format " +
                                    s + peek + "'.");
                            return null;
                        }
                    }

                    while((Character.isLetterOrDigit(peek) || peek == '_')) {
                        s += peek;
                        readch(br);
                    }

                    switch (s) {
                        case "cond":
                            return Word.cond;

                        case "when":
                            return Word.when;

                        case "then":
                            return Word.then;

                        case "else":
                            return Word.elsetok;

                        case "while":
                            return Word.whiletok;

                        case "do":
                            return Word.dotok;

                        case "read":
                            return Word.read;

                        case "print":
                            return Word.print;

                        default:
                            return new Word(Tag.ID, s);
                    }

                } else if (Character.isDigit(peek)) {
                    String num = "";
                    while(Character.isDigit(peek)) {
                        num += peek;
                        readch(br);
                    }
                    if(Character.isLetter(peek) || peek == '_'){
                        System.err.println("Erroneous number: " + peek);
                        return null;
                    }

                    return new NumberTok(Tag.NUM, String.valueOf(num));
                } else {
                    System.err.println("Erroneous character:" + peek);
                    return null;
                }
        }
    }


    public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "test.txt";
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Token tok;
            do {
                tok = lex.lexical_scan(br);
                System.out.println("Scan: " + tok);
            } while (tok.tag != Tag.EOF);
            br.close();
        } catch (IOException e) {e.printStackTrace();}
    }

}