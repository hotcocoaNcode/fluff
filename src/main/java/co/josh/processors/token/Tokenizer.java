package co.josh.processors.token;

import java.util.ArrayList;

public interface Tokenizer {
    ArrayList<Token> tokenize(String s);
}
