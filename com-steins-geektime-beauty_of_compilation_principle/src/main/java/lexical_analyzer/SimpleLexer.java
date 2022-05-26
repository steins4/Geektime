package lexical_analyzer;



import java.io.CharArrayReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SimpleLexer {
    private StringBuilder tokenText = null;
    private List<Token> tokens = null;
    private SimpleToken token;
    public static void main(String[] args) throws IOException {
        SimpleLexer lexer = new SimpleLexer();

        String code = "int age = 123";
        System.out.println("parse: "+code);
        SimpleTokenReader reader = lexer.tokenize(code);
        dump(reader);

        code = "inta age = 123 ";
        System.out.println("\nparse: "+code);
        reader = lexer.tokenize(code);
        dump(reader);

        code = "in age = 123 ";
        System.out.println("\nparse: "+code);
        reader = lexer.tokenize(code);
        dump(reader);

        code = "age >= 123";
        System.out.println("\nparse: "+code);
        reader = lexer.tokenize(code);
        dump(reader);

        code = "age > 123";
        System.out.println("\nparse: "+code);
        reader = lexer.tokenize(code);
        dump(reader);
    }

    // 判断ch的类型
    private boolean isAlpha(char ch) {
        return (ch >= 'a' && ch <= 'z') ||
                (ch >= 'A' && ch <= 'Z');
    }
    private boolean isDigit(char ch) {
        return '0' <= ch && ch <= '9';
    }
    private boolean isBlank(char ch) {
        return ch == ' ';
    }

    private DfaState initToken(char ch) {
        if (tokenText.length() > 0) {
            token.tokenText = tokenText.toString();
            tokens.add(token);
            tokenText = new StringBuilder();
            token = new SimpleToken();
        }
        DfaState state = DfaState.Initial;

        if (isAlpha(ch)) {              //第一个字符是字母
            if (ch == 'i') {
                state = DfaState.Id_int1;
            } else {
                state = DfaState.Id; //进入Id状态
            }
            token.type = TokenType.Identifier;
            tokenText.append(ch);
        } else if (isDigit(ch)) {       //第一个字符是数字
            state = DfaState.IntLiteral;
            token.type = TokenType.IntLiteral;
            tokenText.append(ch);
        } else if (ch == '>') {         //第一个字符是>
            state = DfaState.GT;
            token.type = TokenType.GT;
            tokenText.append(ch);
        } else if (ch == '+') {
            state = DfaState.Plus;
            token.type = TokenType.Plus;
            tokenText.append(ch);
        } else if (ch == '-') {
            state = DfaState.Minus;
            token.type = TokenType.Minus;
            tokenText.append(ch);
        } else if (ch == '*') {
            state = DfaState.Star;
            token.type = TokenType.Star;
            tokenText.append(ch);
        } else if (ch == '/') {
            state = DfaState.Slash;
            token.type = TokenType.Slash;
            tokenText.append(ch);
        } else if (ch == ';') {
            state = DfaState.SemiColon;
            token.type = TokenType.SemiColon;
            tokenText.append(ch);
        } else if (ch == '(') {
            state = DfaState.LeftParen;
            token.type = TokenType.LeftParen;
            tokenText.append(ch);
        } else if (ch == ')') {
            state = DfaState.RightParen;
            token.type = TokenType.RightParen;
            tokenText.append(ch);
        } else if (ch == '=') {
            state = DfaState.Assignment;
            token.type = TokenType.Assignment;
            tokenText.append(ch);
        } else {
            state = DfaState.Initial; // skip all unknown patterns
        }
        return state;
    }

    public SimpleTokenReader tokenize(String code) throws IOException {
        CharArrayReader reader = new CharArrayReader(code.toCharArray());
        int idx = 0;
        char ch = 0;
        tokens = new ArrayList<Token>();
        tokenText = new StringBuilder();
        token = new SimpleToken();
        DfaState state = DfaState.Initial;
        try{
            while ((idx = reader.read()) != -1) {
                ch = (char) idx;
                switch (state) {
                    case Initial:
                        state = initToken(ch);          //重新确定后续状态
                        break;
                    case Id:
                        if (isAlpha(ch) || isDigit(ch)) {
                            tokenText.append(ch);       //保持标识符状态
                        } else {
                            state = initToken(ch);      //退出标识符状态，并保存Token
                        }
                        break;
                    case GT:
                        if (ch == '=') {
                            token.type = TokenType.GE;  //转换成GE
                            state = DfaState.GE;
                            tokenText.append(ch);
                        } else {
                            state = initToken(ch);      //退出GT状态，并保存Token
                        }
                        break;
                    case GE:
                    case Assignment:
                    case Plus:
                    case Minus:
                    case Star:
                    case Slash:
                    case SemiColon:
                    case LeftParen:
                    case RightParen:
                        state = initToken(ch);          //退出当前状态，并保存Token
                        break;
                    case IntLiteral:
                        if (isDigit(ch)) {
                            tokenText.append(ch);       //继续保持在数字字面量状态
                        } else {
                            state = initToken(ch);      //退出当前状态，并保存Token
                        }
                        break;
                    case Id_int1:
                        if (ch == 'n') {
                            state = DfaState.Id_int2;
                            tokenText.append(ch);
                        }
                        else if (isDigit(ch) || isAlpha(ch)){
                            state = DfaState.Id;    //切换回Id状态
                            tokenText.append(ch);
                        }
                        else {
                            state = initToken(ch);
                        }
                        break;
                    case Id_int2:
                        if (ch == 't') {
                            state = DfaState.Id_int3;
                            tokenText.append(ch);
                        }
                        else if (isDigit(ch) || isAlpha(ch)){
                            state = DfaState.Id;    //切换回id状态
                            tokenText.append(ch);
                        }
                        else {
                            state = initToken(ch);
                        }
                        break;
                    case Id_int3:
                        if (isBlank(ch)) {
                            token.type = TokenType.Int;
                            state = initToken(ch);
                        }
                        else{
                            state = DfaState.Id;    //切换回Id状态
                            tokenText.append(ch);
                        }
                        break;
                    default:

                }

            }
            // 把最后一个token送进去
            if (tokenText.length() > 0) {
                initToken(ch);
            }


        } catch (IOException io) {
            io.printStackTrace();
        }
        SimpleTokenReader tokenReader = new SimpleTokenReader(tokens);
        return tokenReader;
    }

    public static void dump(SimpleTokenReader reader) {
        Token token;
        while ((token = reader.read()) != null) {
            System.out.print(token.getType() + ":" + token.getTokenText());
            System.out.print("  ");
        }
    }
    // 有限机状态量
    private enum DfaState {
        Initial,

        If, Id_if1, Id_if2, Else, Id_else1, Id_else2, Id_else3, Id_else4, Int, Id_int1, Id_int2, Id_int3, Id, GT, GE,

        Assignment,

        Plus, Minus, Star, Slash,

        SemiColon,
        LeftParen,
        RightParen,

        IntLiteral
    }

    // token读取器
    public class SimpleTokenReader implements TokenReader{
        private List<Token> tokens;
        private int pos = 0;

        public SimpleTokenReader(List<Token> tokens) {
            this.tokens = tokens;
        }

        public Token read() {
            if (pos < tokens.size()) {
                return tokens.get(pos++);
            } else {
                return null;
            }
        }

        public Token peek() {
            if (pos < tokens.size()) {
                return tokens.get(pos);
            } else {
                return null;
            }
        }

        public void unread() {
            if (pos > 0) {
                pos--;
            }
        }

        public int getPosition() {
            return pos;
        }

        public void setPosition(int pos) {
            if (pos >= 0 && pos < tokens.size()) {
                this.pos = pos;
            }
        }
    }
    // token
    private class SimpleToken implements Token{
        public TokenType type;
        public String tokenText;
        public TokenType getType() {
            return type;
        }

        public String getTokenText() {
            return tokenText;
        }
    }


}
