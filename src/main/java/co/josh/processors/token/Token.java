package co.josh.processors.token;

public class Token {
    TokenType tokenType;

    public TokenType getRefType() {
        if (!hasRefType) return null;
        return refType;
    }

    public void setRefType(TokenType refType) {
        this.refType = refType;
        hasRefType = refType != null;
    }

    TokenType refType;
    boolean hasRefType;
    Object value;

    public Token(TokenType tokenType, Object value) {
        this.tokenType = tokenType;
        this.value = value;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
