package co.josh.processors.expression.eval;

import co.josh.processors.token.Token;
import co.josh.processors.token.TokenType;

import java.util.ArrayList;

public class ExpOpt {

    public static void optimize(ArrayList<Token> tokens) {
        for (short i = 0; i < tokens.size(); i++){
            switch (tokens.get(i).getTokenType()){
                case add -> {
                    ExpOptPull eop = new ExpOptPull(tokens, i);
                    tokens.add(0, new Token(TokenType.int_literal, (short)(eop.b + eop.a), eop.line)); //Push precalculated value
                    i = -1; //Reset expression processing to avoid skipping TODO can this be removed?
                }

                case subtract -> {
                    ExpOptPull eop = new ExpOptPull(tokens, i);
                    tokens.add(0, new Token(TokenType.int_literal, (short)(eop.b - eop.a), eop.line));
                    i = -1;
                }

                case multiply -> {
                    ExpOptPull eop = new ExpOptPull(tokens, i);
                    tokens.add(0, new Token(TokenType.int_literal, (short)(eop.b * eop.a), eop.line));
                    i = -1;
                }

                case divide -> {
                    ExpOptPull eop = new ExpOptPull(tokens, i);
                    tokens.add(0, new Token(TokenType.int_literal, (short)(eop.b / eop.a), eop.line));
                    i = -1;
                }

                case modulo -> {
                    ExpOptPull eop = new ExpOptPull(tokens, i);
                    tokens.add(0, new Token(TokenType.int_literal, (short)(eop.b % eop.a), eop.line));
                    i = -1;
                }

                case bit_shift_left -> {
                    ExpOptPull eop = new ExpOptPull(tokens, i);
                    tokens.add(0, new Token(TokenType.int_literal, (short)(eop.b << eop.a), eop.line));
                    i = -1;
                }

                case bit_shift_right -> {
                    ExpOptPull eop = new ExpOptPull(tokens, i);
                    tokens.add(0, new Token(TokenType.int_literal, (short)(eop.b >> eop.a), eop.line));
                    i = -1;
                }

                case or_bool_op -> {
                    ExpOptPull eop = new ExpOptPull(tokens, i);
                    tokens.add(0, new Token(TokenType.int_literal, (short) ((eop.b==1 || eop.a==1 ? 1 : 0)), eop.line));
                    i = -1;
                }

                case and_bool_op -> {
                    ExpOptPull eop = new ExpOptPull(tokens, i);
                    tokens.add(0, new Token(TokenType.int_literal, (short) ((eop.b==1 && eop.a==1 ? 1 : 0)), eop.line));
                    i = -1;
                }

                case xor_bool_op -> {
                    ExpOptPull eop = new ExpOptPull(tokens, i);
                    tokens.add(0, new Token(TokenType.int_literal, (short) ((eop.b==1 ^ eop.a==1 ? 1 : 0)), eop.line));
                    i = -1;
                }

                case inequality_equals -> {
                    ExpOptPull eop = new ExpOptPull(tokens, i);
                    tokens.add(0, new Token(TokenType.int_literal, (short) ((eop.b == eop.a ? 1 : 0)), eop.line));
                    i = -1;
                }

                case inequality_greater -> {
                    ExpOptPull eop = new ExpOptPull(tokens, i);
                    tokens.add(0, new Token(TokenType.int_literal, (short) ((eop.b > eop.a ? 1 : 0)), eop.line));
                    i = -1;
                }

                case inequality_lesser -> {
                    ExpOptPull eop = new ExpOptPull(tokens, i);
                    tokens.add(0, new Token(TokenType.int_literal, (short) ((eop.b < eop.a ? 1 : 0)), eop.line));
                    i = -1;
                }

                case inequality_greater_equals -> {
                    ExpOptPull eop = new ExpOptPull(tokens, i);
                    tokens.add(0, new Token(TokenType.int_literal, (short) ((eop.b >= eop.a ? 1 : 0)), eop.line));
                    i = -1;
                }

                case inequality_not_equals -> {
                    ExpOptPull eop = new ExpOptPull(tokens, i);
                    tokens.add(0, new Token(TokenType.int_literal, (short) ((eop.b != eop.a ? 1 : 0)), eop.line));
                    i = -1;
                }

                case inequality_lesser_equals -> {
                    ExpOptPull eop = new ExpOptPull(tokens, i);
                    tokens.add(0, new Token(TokenType.int_literal, (short) ((eop.b <= eop.a ? 1 : 0)), eop.line));
                    i = -1;
                }

                case not_bool_op -> {
                    short finalI = i;
                    int line = tokens.remove(i).getLine();
                    short a = Short.parseShort(tokens.remove(tokens.indexOf(tokens.stream().filter(token -> token.getTokenType() == TokenType.int_literal).filter(token -> tokens.indexOf(token) < finalI).reduce((first, second) -> second).get())).getValue().toString());
                    tokens.add(0, new Token(TokenType.int_literal, (short) ((a==1 ? 0 : 1)), line));
                    i = -1;
                }

                case name -> {
                    return;
                }
            }
        }
    }
}
