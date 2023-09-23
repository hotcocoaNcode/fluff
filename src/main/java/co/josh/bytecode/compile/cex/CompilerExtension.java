package co.josh.bytecode.compile.cex;

import co.josh.bytecode.Instruction;
import co.josh.processors.token.Token;

import java.util.ArrayList;
import java.util.HashMap;

public interface CompilerExtension {

    String getStatement();

    ArrayList<Byte> compile(ArrayList<Token> tokens, HashMap<Instruction, Byte> iset);

    int interpret(byte[] bytecode, byte[] ram, int i);
}
