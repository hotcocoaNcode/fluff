package co.josh.processors.expression.shuntingyard;

import co.josh.processors.token.Token;
import co.josh.processors.token.TokenType;

import java.util.ArrayList;
import java.util.Stack;

public class BooleanShuntingYard {
    // Method is used to get the precedence of operators
    private static boolean isNotOperator(Token t)
    {
        return t.getTokenType() == TokenType.boolean_val || t.getTokenType() == TokenType.name;
    }

    // Operator having higher precedence
    // value will be returned
    static int getPrecedence(Token t)
    {

        if (t.getTokenType() == TokenType.or_bool_op)
            return 1;
        else if (t.getTokenType() == TokenType.xor_bool_op)
            return 2;
        else if (t.getTokenType() == TokenType.and_bool_op)
            return 3;
        else if (t.getTokenType() == TokenType.not_bool_op)
            return 4;
        else
            return -1;
    }

    // Operator has Left --> Right associativity
    static boolean hasLeftAssociativity(Token t) {
        return t.getTokenType() == TokenType.not_bool_op
                || t.getTokenType() == TokenType.and_bool_op
                || t.getTokenType() == TokenType.xor_bool_op
                || t.getTokenType() == TokenType.or_bool_op;
    }

    // Method converts  given infixto postfix expression
    // to illustrate shunting yard algorithm
    public static ArrayList<Token> infixToRpn(ArrayList<Token> expression)
    {
        // Initialising an empty String
        // (for output) and an empty stack
        Stack<Token> stack = new Stack<>();

        // Initially empty string taken
        ArrayList<Token> output = new ArrayList<>();

        // Iterating over tokens using inbuilt
        // .length() function
        for (Token t : expression) {
            // Finding character at 'i'th index
            // If the scanned Token is an
            // operand, add it to output
            if (isNotOperator(t))
                output.add(t);

                // If the scanned Token is an '('
                // push it to the stack
            else if (t.getTokenType() == TokenType.opening_parentheses)
                stack.push(t);

                // If the scanned Token is an ')' pop and append
                // it to output from the stack until an '(' is
                // encountered
            else if (t.getTokenType() == TokenType.closing_parentheses) {
                while (!stack.isEmpty()
                        && stack.peek().getTokenType() != TokenType.opening_parentheses)
                    output.add(stack.pop());

                stack.pop();
            }

            // If an operator is encountered then taken the
            // further action based on the precedence of the
            // operator

            else {
                while (
                        !stack.isEmpty()
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
        // the stack and append them to output
        while (!stack.isEmpty()) {
            if (stack.peek().getTokenType() == TokenType.opening_parentheses){
                System.out.println("Expression is invalid!");
                System.exit(1);
            }
            output.add(stack.pop());
        }
        return output;
    }
}
