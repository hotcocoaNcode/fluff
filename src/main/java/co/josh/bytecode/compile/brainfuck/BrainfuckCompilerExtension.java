package co.josh.bytecode.compile.brainfuck;


import co.josh.JoshLogger;
import co.josh.bytecode.Instruction;
import co.josh.bytecode.compile.cex.CompilerExtension;
import co.josh.processors.token.Token;
import co.josh.processors.token.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class BrainfuckCompilerExtension implements CompilerExtension {

    @Override
    public String getStatement(){
        return "bfi";
    }
    @Override
    public ArrayList<Byte> compile(ArrayList<Token> tokens, HashMap<Instruction, Byte> iset) {
        ArrayList<Byte> bytes = new ArrayList<>();
        //Invoke Brainfuck Interpreter Mode
        bytes.add(iset.get(Instruction.invokeInterpreterMode));
        bytes.add((byte)'b');
        bytes.add((byte)'f');
        bytes.add((byte)'i');
        bytes.add((byte) 0);
        if (tokens.get(0).getTokenType() == TokenType.string_val && tokens.get(0).getValue() != null){
            String brainfuck = tokens.get(0).getValue().toString();
            for (int i = 0; i < brainfuck.length(); i++){
                switch (brainfuck.charAt(i)){
                    case '>' -> {
                        bytes.add((byte) 0);
                    }

                    case '<' -> {
                        bytes.add((byte) 1);
                    }

                    case '[' -> {
                        bytes.add((byte) 2);
                    }

                    case ']' -> {
                        bytes.add((byte) 3);
                    }

                    case '+' -> {
                        bytes.add((byte) 4);
                    }

                    case '-' -> {
                        bytes.add((byte) 5);
                    }

                    case '.' -> {
                        bytes.add((byte) 6);
                    }

                    case ',' -> {
                        bytes.add((byte) 7);
                    }

                    case '~' -> {
                        bytes.add((byte) 8);
                    }
                }
            }
        } else JoshLogger.syntaxError("Brainfuck command must take in a string val!");

        bytes.add((byte) 8);

        return bytes;
    }

    @Override
    public int interpret(byte[] bytecode, byte[] ram, int i) {
        int pointer = 0;
        byte[] _ram = ram.clone();
        Stack<Integer> loops = new Stack<>();
        for (; i < bytecode.length; i++){
            if (pointer < 0){
                JoshLogger.error("Brainfuck pointer lower than 0!");
            }
            switch (bytecode[i]) {
                case (0) -> {
                    pointer++;
                }
                case (1) -> {
                    pointer--;
                }
                case (2) -> {
                    loops.push(i);
                }
                case (3) -> {
                    if (_ram[pointer] != 0){
                        i = loops.peek();
                    } else {
                        loops.pop();
                    }
                }
                case (4) -> {
                    _ram[pointer]++;
                }
                case (5) -> {
                    _ram[pointer]--;
                }
                case (6) -> {
                    System.out.print((char)_ram[pointer]);
                }
                case (7) -> {
                    //TODO
                }
                case (8) -> {
                    return i;
                }
            }
        }
        return i;
    }
}
