package co.josh.bytecode.compile.fluff;

import co.josh.JoshLogger;
import co.josh.processors.token.Token;
import co.josh.processors.token.TokenType;

import java.util.ArrayList;

public class MacroInfo {
    ArrayList<Token> tokens;

    String name;

    ArrayList<String> argTypes = new ArrayList<>();

    ArrayList<String> argNames = new ArrayList<>();

    public MacroInfo(ArrayList<Token> tokens) {
        this.tokens = tokens;
        tokens.remove(0);
        if (tokens.get(0).getTokenType() != TokenType.name) JoshLogger.error("Macro must be followed by name!");
        name = tokens.remove(0).getValue().toString();
        while (tokens.get(0).getTokenType() != TokenType.scope_up){
            if (tokens.get(0).getTokenType() == TokenType.int16_variable){
                argTypes.add("int16");
                tokens.remove(0);
                argNames.add(tokens.remove(0).getValue().toString());
            } else if (tokens.get(0).getTokenType() == TokenType.int8_variable) {
                argTypes.add("int8");
                tokens.remove(0);
                argNames.add(tokens.remove(0).getValue().toString());
            } else {
                JoshLogger.syntaxError("Unexpected token \"" + tokens.get(0).getTokenType() + "\" in macro def! (" + name + ")", tokens.get(0).getLine());
            }
        }
        if (tokens.get(tokens.size()-1).getTokenType() != TokenType.scope_down) JoshLogger.syntaxError("Macro scope not closed!", tokens.get(tokens.size()-1).getLine());
    }

    ArrayList<Token> use(){
        ArrayList<Token> ret = new ArrayList<>();
        for (Token t : tokens){
            ret.add(new Token(t.getTokenType(), t.getValue(), t.getLine()));
        }
        return ret;
    }
}
