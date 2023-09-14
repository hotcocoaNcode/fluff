package co.josh.processors.expression;

import co.josh.processors.expression.liveEval.InequalityEvaluator;
import co.josh.processors.expression.liveEval.RPN;
import co.josh.processors.expression.shuntingyard.BooleanShuntingYard;
import co.josh.processors.expression.shuntingyard.NumericalShuntingYard;
import co.josh.processors.token.Token;
import co.josh.processors.token.TokenType;

import java.util.ArrayList;

public class Expression {
    public static Object evalExpression(ArrayList<Token> tokens){
        boolean concat = false;
        boolean isBool = false;
        boolean floats = false;
        if (tokens.size() == 3 && (tokens.get(1).getTokenType() == TokenType.inequality_equals
                || (tokens.get(1).getTokenType()  == TokenType.inequality_greater)
                || (tokens.get(1).getTokenType()  == TokenType.inequality_lesser))){
            if ((tokens.get(1).getTokenType()  == TokenType.inequality_greater)
                    || (tokens.get(1).getTokenType()  == TokenType.inequality_lesser)){
                nonNumberCheckInequality(tokens);
                return InequalityEvaluator.evalInequality(tokens);
            } else {
                return tokens.get(0).getValue().hashCode() == tokens.get(2).getValue().hashCode();
            }
        }
        for (Token t : tokens){
            if (t.getTokenType() == TokenType.xor_bool_op
                    || t.getTokenType() == TokenType.not_bool_op
                    || t.getTokenType() == TokenType.and_bool_op
                    || t.getTokenType() == TokenType.or_bool_op) {
                isBool = true;
            }
            if (t.getTokenType() == TokenType.string_val){
                concat = true;
                if (isBool){
                    System.out.println("Expression error: concatenation in boolean operation!");
                    System.exit(1);
                }
            }
            if (t.getTokenType() == TokenType.float_val){
                floats = true;
                if (isBool){
                    System.out.println("Expression error: math in boolean operation!");
                    System.exit(1);
                }
            }
            if ((t.getTokenType() == TokenType.subtract
                    || t.getTokenType() == TokenType.multiply
                    || t.getTokenType() == TokenType.divide)){
                if (isBool){
                    System.out.println("Expression error: math in boolean operation!");
                    System.exit(1);
                }
                if (concat){
                    System.out.println("Expression error: Cannot concatenate string with math operators!");
                    System.exit(1);
                }
            }
            if (t.getTokenType() == TokenType.add){
                if (isBool){
                    System.out.println("Expression error: concatenation or math in boolean!");
                    System.exit(1);
                }
            }
        }
        if (isBool){
            return RPN.evalRPN_boolean(BooleanShuntingYard.infixToRpn(tokens));
        }
        if (concat){
            return concat(tokens);
        } else {
            if (floats) return RPN.evalRPN_floats(NumericalShuntingYard.infixToRpn(tokens));
            else return RPN.evalRPN_ints(NumericalShuntingYard.infixToRpn(tokens));
        }
    }

    public static int expressionType(ArrayList<Token> tokens){
        boolean concat = false;
        boolean isBool = false;
        boolean floats = false;
        if (tokens.size() == 3 && (tokens.get(1).getTokenType() == TokenType.inequality_equals
                || (tokens.get(1).getTokenType()  == TokenType.inequality_greater)
                || (tokens.get(1).getTokenType()  == TokenType.inequality_lesser))){
            if ((tokens.get(1).getTokenType()  == TokenType.inequality_greater)
                    || (tokens.get(1).getTokenType()  == TokenType.inequality_lesser)){
                nonNumberCheckInequality(tokens);
                return 4;
            } else {
                return 4;
            }
        }
        for (Token t : tokens){
            if (t.getTokenType() == TokenType.xor_bool_op
                    || t.getTokenType() == TokenType.not_bool_op
                    || t.getTokenType() == TokenType.and_bool_op
                    || t.getTokenType() == TokenType.or_bool_op) {
                isBool = true;
            }
            if (t.getTokenType() == TokenType.string_val){
                concat = true;
                if (isBool){
                    System.out.println("Expression error: concatenation in boolean operation!");
                    System.exit(1);
                }
            }
            if (t.getTokenType() == TokenType.float_val){
                floats = true;
                if (isBool){
                    System.out.println("Expression error: math in boolean operation!");
                    System.exit(1);
                }
            }
            if ((t.getTokenType() == TokenType.subtract
                    || t.getTokenType() == TokenType.multiply
                    || t.getTokenType() == TokenType.divide)){
                if (isBool){
                    System.out.println("Expression error: math in boolean operation!");
                    System.exit(1);
                }
                if (concat){
                    System.out.println("Expression error: Cannot concatenate string with math operators!");
                    System.exit(1);
                }
            }
            if (t.getTokenType() == TokenType.add){
                if (isBool){
                    System.out.println("Expression error: concatenation or math in boolean!");
                    System.exit(1);
                }
            }
        }
        if (isBool){
            return 3;
        }
        if (concat){
            return 2;
        } else {
            if (floats) return 1;
            else return 0;
        }
    }

    private static void nonNumberCheckInequality(ArrayList<Token> tokens) {
        if (!(tokens.get(0).getValue() instanceof Integer ||
                tokens.get(0).getValue() instanceof Float  || tokens.get(0).getValue() instanceof Byte)
                || !(tokens.get(2).getValue() instanceof Integer ||
                tokens.get(2).getValue() instanceof Float || tokens.get(2).getValue() instanceof Byte)){
            System.out.println("Expression error: string cannot be used in greater/lesser inequalities!");
            System.exit(1);
        }
    }

    public static String concat(ArrayList<Token> tokens){
        String buf = "";
        for (Token t : tokens){
            if (t.getTokenType() != TokenType.add) buf = buf + t.getValue().toString();
        }
        return buf;
    }

    public static Token t_concat(ArrayList<Token> tokens){
        String buf = "";
        for (Token t : tokens){
            if (t.getTokenType() != TokenType.add) buf = buf + t.getValue().toString();
        }
        return new Token(TokenType.string_val, buf);
    }
}
