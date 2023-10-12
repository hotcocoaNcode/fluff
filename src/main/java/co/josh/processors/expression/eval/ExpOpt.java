package co.josh.processors.expression.eval;

import co.josh.processors.token.Token;
import co.josh.processors.token.TokenType;

import java.util.ArrayList;

public class ExpOpt {
    public static void optimize(ArrayList<Token> tokens) {
        for (short i = 0; i < tokens.size(); i++){
            switch (tokens.get(i).getTokenType()){
                case add -> {
                    //Simplify expression
                    int line = tokens.get(0).getLine();
                    short a = (short) tokens.remove(0).getValue();
                    short b = (short) tokens.remove(0).getValue();
                    tokens.remove(0); //Pop operator
                    tokens.add(0, new Token(TokenType.int_literal, (short)(a+b), line)); //Push precalculated value
                    i = -1; //Reset expression processing to avoid skipping TODO find a better way to do this
                }

                case subtract -> {
                    int line = tokens.get(0).getLine();
                    short a = (short) tokens.remove(0).getValue();
                    short b = (short) tokens.remove(0).getValue();
                    tokens.remove(0);
                    tokens.add(0, new Token(TokenType.int_literal, (short)(a-b), line));
                    i = -1;
                }

                case multiply -> {
                    int line = tokens.get(0).getLine();
                    short a = (short) tokens.remove(0).getValue();
                    short b = (short) tokens.remove(0).getValue();
                    tokens.remove(0);
                    tokens.add(0, new Token(TokenType.int_literal, (short)(a*b), line));
                    i = -1;
                }

                case divide -> {
                    int line = tokens.get(0).getLine();
                    short a = (short) tokens.remove(0).getValue();
                    short b = (short) tokens.remove(0).getValue();
                    tokens.remove(0);
                    tokens.add(0, new Token(TokenType.int_literal, (short)(a/b), line));
                    i = -1;
                }

                case modulo -> {
                    int line = tokens.get(0).getLine();
                    short a = (short) tokens.remove(0).getValue();
                    short b = (short) tokens.remove(0).getValue();
                    tokens.remove(0);
                    tokens.add(0, new Token(TokenType.int_literal, (short)(a%b), line));
                    i = -1;
                }

                case name -> {
                    return;
                }
            }
        }
    }
}
