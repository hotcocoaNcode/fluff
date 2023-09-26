package co.josh.bytecode.compile.fluff;

import co.josh.JoshLogger;
import co.josh.processors.token.Token;
import co.josh.processors.token.TokenType;
import co.josh.processors.token.Tokenizer;
import co.josh.processors.token.v2.v2Tokenizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class MacroInfo {
    ArrayList<Token> tokens;

    String name;

    ArrayList<String> argTypes = new ArrayList<>();

    ArrayList<String> argNames = new ArrayList<>();

    public MacroInfo(String fileName) {
        File f = new File(fileName);
        try {
            Scanner fileRead = new Scanner(f);
            Tokenizer tokenizer = new v2Tokenizer();
            StringBuilder str = new StringBuilder();
            while (fileRead.hasNextLine()){
                str.append(fileRead.nextLine()).append("\n");
            }
            tokens = tokenizer.tokenize(str.toString(), fileName);
            if (tokens.get(0).getTokenType() != TokenType.macro_def) JoshLogger.error("Macro file must start with macro def!");
            tokens.remove(0);
            if (tokens.get(0).getTokenType() != TokenType.name) JoshLogger.error("Macro must be followed by name!");
            name = tokens.remove(0).getValue().toString();
            while (tokens.get(0).getTokenType() != TokenType.semi){
                if (tokens.get(0).getTokenType() == TokenType.int_var){
                    argTypes.add("short");
                    tokens.remove(0);
                    argNames.add(tokens.remove(0).getValue().toString());
                } else if (tokens.get(0).getTokenType() == TokenType.byte_var) {
                    argTypes.add("byte");
                    tokens.remove(0);
                    argNames.add(tokens.remove(0).getValue().toString());
                } else {
                    JoshLogger.error("Unexpected token \"" + tokens.get(0).getTokenType() + "\" in macro def! (" + fileName + ")");
                }
            }
            tokens.remove(0);
        } catch (IOException e) {
            JoshLogger.error("While trying to read \"" + fileName + "\" an exception occurred\n" + e.getMessage());
        }
    }
}
