package co.josh.processors.expression.shuntingyard;

import co.josh.JoshLogger;
import co.josh.processors.token.Token;
import co.josh.processors.token.TokenType;

import java.util.ArrayList;
import java.util.Stack;

public class CompilerShuntingYard {
    // Method is used to get the precedence of operators
    private static boolean isNotOperator(Token t)
    {
        return t.getTokenType() == TokenType.int_literal || t.getTokenType() == TokenType.name;
    }

    // Operator having higher precedence
    // value will be returned
    static int getPrecedence(Token t)
    {
        if (t.getTokenType() == TokenType.inequality_greater
                || t.getTokenType() == TokenType.inequality_equals
                || t.getTokenType() == TokenType.inequality_lesser
                || t.getTokenType() == TokenType.inequality_not_equals
                || t.getTokenType() == TokenType.not_bool_op
                || t.getTokenType() == TokenType.or_bool_op
                || t.getTokenType() == TokenType.and_bool_op
                || t.getTokenType() == TokenType.xor_bool_op)
            return 1;
        else if (t.getTokenType() == TokenType.bit_shift_right
                || t.getTokenType() == TokenType.bit_shift_left)
            return 2;
        else if (t.getTokenType() == TokenType.add
                || t.getTokenType() == TokenType.subtract)
            return 3;
        else if (t.getTokenType() == TokenType.multiply
                || t.getTokenType() == TokenType.divide
                || t.getTokenType() == TokenType.modulo)
            return 4;
        else
            return -1;
    }

    // Operator has Left --> Right associativity
    static boolean hasLeftAssociativity(Token t) {
        return     t.getTokenType() == TokenType.add
                || t.getTokenType() == TokenType.subtract
                || t.getTokenType() == TokenType.divide
                || t.getTokenType() == TokenType.multiply
                || t.getTokenType() == TokenType.bit_shift_left
                || t.getTokenType() == TokenType.bit_shift_right
                || t.getTokenType() == TokenType.modulo
                || t.getTokenType() == TokenType.inequality_equals
                || t.getTokenType() == TokenType.inequality_lesser
                || t.getTokenType() == TokenType.inequality_greater
                || t.getTokenType() == TokenType.inequality_not_equals
                || t.getTokenType() == TokenType.and_bool_op
                || t.getTokenType() == TokenType.or_bool_op
                || t.getTokenType() == TokenType.xor_bool_op
                || t.getTokenType() == TokenType.not_bool_op;
    }
    public static ArrayList<Token> infixToRpn(ArrayList<Token> expression)
    {
        // Token stack
        Stack<Token> stack = new Stack<>();

        // Initially empty string taken
        ArrayList<Token> output = new ArrayList<>();

        for (Token t : expression) {
            //Add non-ops to output
            if (isNotOperator(t))
                output.add(t);
            // If token is opening paren push it
            else if (t.getTokenType() == TokenType.opening_parentheses)
                stack.push(t);
            // If token is closing paren then pop from stack to output until the stack top is an opening paren
            else if (t.getTokenType() == TokenType.closing_parentheses) {
                while (!stack.isEmpty()
                        && stack.peek().getTokenType() != TokenType.opening_parentheses)
                    output.add(stack.pop());

                stack.pop();
            }
            //This means we hit an operator
            else {
                // While the stack isn't empty, and the precedence of the operator
                // is larger than the top of the stack (and it has left associativity)
                // pop from stack to output and then push the operator onto the stack
                while (!stack.isEmpty()
                                && getPrecedence(t)
                                <= getPrecedence(stack.peek())
                                && hasLeftAssociativity(t)) {
                    // peek() inbuilt stack function to
                    // fetch the top element(token)

                    output.add(stack.pop());
                }
                stack.push(t);
            }
        }

        // pop all the remaining operators from
        // the stack to the output
        while (!stack.isEmpty()) {
            if (stack.peek().getTokenType() == TokenType.opening_parentheses){
                JoshLogger.syntaxError("Expression is not valid!", expression.get(0).getLine());
            }
            output.add(stack.pop());
        }
        return output;
    }
}
