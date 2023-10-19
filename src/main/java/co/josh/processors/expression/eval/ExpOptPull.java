package co.josh.processors.expression.eval;

import co.josh.processors.token.Token;
import co.josh.processors.token.TokenType;

import java.util.ArrayList;

public class ExpOptPull {
    public int line;
    public short a;
    public short b;
    public ExpOptPull(ArrayList<Token> tokens, int i) {
        //Simplify expression
        this.line = tokens.remove(i).getLine();
        //This is basically lambda hell. I'm trying to remove something in one line so doing way too many operations
        this.a = Short.parseShort(tokens.remove(tokens.indexOf(tokens.stream().filter(token -> token.getTokenType() == TokenType.int_literal).filter(token -> tokens.indexOf(token) < i).reduce((first, second) -> second).get())).getValue().toString());
        this.b = Short.parseShort(tokens.remove(tokens.indexOf(tokens.stream().filter(token -> token.getTokenType() == TokenType.int_literal).filter(token -> tokens.indexOf(token) < i-1).reduce((first, second) -> second).get())).getValue().toString());
    }
}
