package co.josh.processors.expression.liveEval;

import co.josh.processors.token.Token;
import co.josh.processors.token.TokenType;

import java.util.ArrayList;
import java.util.Stack;

public class RPN {
    public static int evalRPN_ints(ArrayList<Token> tokens) {
        Stack<Integer> a = new Stack<>();
        for(Token i : tokens){
            if(i.getTokenType() == TokenType.add){
                int t = a.pop()+a.pop();
                a.push(t);
            }
            else if(i.getTokenType() == TokenType.subtract){
                int t = (a.pop()-a.pop())*(-1);
                a.push(t);
            }
            else if(i.getTokenType() == TokenType.multiply){
                int t = a.pop()*a.pop();
                a.push(t);
            }
            else if(i.getTokenType() == TokenType.divide){
                int x = a.pop();
                int y = a.pop();
                a.push(y/x);
            }
            else if (i.getTokenType() == TokenType.int_val){
                int t =  (int)i.getValue();
                a.push(t);
            }
        }
        return a.pop();
    }

    public static boolean evalRPN_boolean(ArrayList<Token> tokens) {
        Stack<Boolean> a = new Stack<>();
        for(Token i : tokens){
            if(i.getTokenType() == TokenType.not_bool_op){
                boolean t = !a.pop();
                a.push(t);
            }
            else if(i.getTokenType() == TokenType.and_bool_op){
                boolean t = a.pop() && a.pop();
                a.push(t);
            }
            else if(i.getTokenType() == TokenType.or_bool_op){
                boolean t = a.pop() || a.pop();
                a.push(t);
            }
            else if(i.getTokenType() == TokenType.xor_bool_op){
                boolean t = a.pop() ^ a.pop();
                a.push(t);
            }
            else if (i.getTokenType() == TokenType.boolean_val){
                boolean t =  (boolean)i.getValue();
                a.push(t);
            }
        }
        return a.pop();
    }

    public static float evalRPN_floats(ArrayList<Token> tokens) {
        Stack<Float> a = new Stack<>();
        for(Token i : tokens){
            if(i.getTokenType() == TokenType.add){
                float t = a.pop()+a.pop();
                a.push(t);
            }
            else if(i.getTokenType() == TokenType.subtract){
                float t = (a.pop()-a.pop())*(-1);
                a.push(t);
            }
            else if(i.getTokenType() == TokenType.multiply){
                float t = a.pop()*a.pop();
                a.push(t);
            }
            else if(i.getTokenType() == TokenType.divide){
                float x = a.pop();
                float y = a.pop();
                a.push(y/x);
            }
            else if (i.getTokenType() == TokenType.float_val){
                float t = (float)i.getValue();
                a.push(t);
            } else if (i.getTokenType() == TokenType.int_val){
                float t = (float)(int)i.getValue(); //I hate java sometimes dude
                a.push(t);
            }
        }
        return a.pop();
    }
}
