package co.josh.processors.expression.liveEval;

import co.josh.processors.token.Token;
import co.josh.processors.token.TokenType;

import java.util.List;

public class InequalityEvaluator {
    //This is going to be some stupid ass if else shit. I don't really care though
    public static boolean evalInequality(List<Token> tokens){
        TokenType type = tokens.get(1).getTokenType();
        float val1 = 0;
        if (tokens.get(0).getValue() instanceof Integer){
            //Janky ass workaround.

            // Essentially the first cast isn't a cast, it just tells the
            // JVM that we're going from an int to a float
            // I think I used this in evalRPN_float
            val1 = (float)(int)tokens.get(0).getValue();
        } else if (tokens.get(0).getValue() instanceof Float){
            val1 = (float)tokens.get(0).getValue();
        }

        float val2 = 0;
        if (tokens.get(2).getValue() instanceof Integer){
            val2 = (float)(int)tokens.get(2).getValue();
        } else if (tokens.get(2).getValue() instanceof Float){
            val2 = (float)tokens.get(2).getValue();
        }
        switch (type) {
            case inequality_equals -> {
                return val1 == val2;
            }
            case inequality_greater -> {
                return val1 > val2;
            }
            case inequality_lesser -> {
                return val1 < val2;
            }
            default -> {
                return false;
            }
        }
    }
}
