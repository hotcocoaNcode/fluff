package co.josh.processors.token;

public class Token {
    TokenType tokenType;

    public Object value;

    int line;

    public Token(TokenType tokenType, Object value, int line) {
        this.tokenType = tokenType;
        this.value = value;
        this.line = line;
    }

    public Token(TokenType tokenType, Object value) {
        this.tokenType = tokenType;
        this.value = value;
        this.line = -1;
    }

    public int getLine(){
        return line;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public Object getValue() {
        return value;
    }
}
