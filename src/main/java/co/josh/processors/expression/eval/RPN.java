package co.josh.processors.expression.eval;

import co.josh.JoshLogger;
import co.josh.bytecode.compile.fluff.FluffCompiler;
import co.josh.bytecode.Instruction;
import co.josh.bytecode.compile.fluff.MemorySpace;
import co.josh.processors.token.Token;
import co.josh.processors.token.TokenType;

import java.util.ArrayList;
import java.util.HashMap;

public class RPN {

    public static ArrayList<Byte> compRPN_ints(ArrayList<Token> tokens,
                                               HashMap<Instruction, Byte> instructions,
                                               MemorySpace memorySpace) {
        ArrayList<Byte> bytes = new ArrayList<>();
        short stackSize = 0;
        boolean encounteredVariable = false;
        for (int i = 0; i < tokens.size(); i++){
            if (stackSize > 255){
                JoshLogger.error("RPN Stack Overflow!");
            } else if (stackSize < 0) {
                JoshLogger.error("RPN Stack at negative size!");
            }
            switch (tokens.get(i).getTokenType()) {
                case add -> {
                    if (encounteredVariable){
                        bytes.add(instructions.get(Instruction.add));
                    } else {
                        int line = tokens.get(0).getLine();
                        int a = (short) tokens.remove(0).getValue();
                        int b = (short) tokens.remove(0).getValue();
                        tokens.remove(0); //Pop operator
                        for (int iter = 0; iter < 6; iter++){
                            bytes.remove(bytes.size()-1); //Remove last 6 bytes of output (stack_push_16 low,high)
                        }
                        tokens.add(0, new Token(TokenType.int_literal, a+b, line));
                        i = -1; // Reset i to 0 on next iteration. i++ after continue so this ensures i will be 0
                        stackSize = 0;
                        continue;
                    }
                    stackSize--;
                }
                case subtract -> {
                    bytes.add(instructions.get(Instruction.subtract));
                    stackSize--;
                }
                case multiply -> {
                    bytes.add(instructions.get(Instruction.multiply));
                    stackSize--;
                }
                case divide -> {
                    bytes.add(instructions.get(Instruction.divide));
                    stackSize--;
                }
                case modulo -> {
                    bytes.add(instructions.get(Instruction.modulo));
                    stackSize--;
                }
                case bit_shift_left -> {
                    bytes.add(instructions.get(Instruction.lshift));
                    stackSize--;
                }
                case bit_shift_right -> {
                    bytes.add(instructions.get(Instruction.rshift));
                    stackSize--;
                }
                case inequality_greater -> {
                    bytes.add(instructions.get(Instruction.greater));
                    stackSize--;
                }
                case inequality_lesser -> {
                    bytes.add(instructions.get(Instruction.lesser));
                    stackSize--;
                }
                case inequality_equals -> {
                    bytes.add(instructions.get(Instruction.equals));
                    stackSize--;
                }
                case inequality_not_equals -> {
                    bytes.add(instructions.get(Instruction.nequals));
                    stackSize--;
                }
                case inequality_lesser_equals -> {
                    bytes.add(instructions.get(Instruction.lesser_equals));
                    stackSize--;
                }
                case inequality_greater_equals -> {
                    bytes.add(instructions.get(Instruction.greater_equals));
                    stackSize--;
                }

                case not_bool_op -> bytes.add(instructions.get(Instruction.not));

                case or_bool_op -> {
                    bytes.add(instructions.get(Instruction.or));
                    stackSize--;
                }
                case and_bool_op -> {
                    bytes.add(instructions.get(Instruction.and));
                    stackSize--;
                }
                case xor_bool_op -> {
                    bytes.add(instructions.get(Instruction.xor));
                    stackSize--;
                }
                case int_literal -> {
                    stackSize++;
                    bytes.add(instructions.get(Instruction.pushConst16bit));
                    Byte[] splitted = FluffCompiler.splitShort(Short.parseShort(tokens.get(i).getValue().toString()));
                    bytes.add(splitted[0]);
                    bytes.add(splitted[1]);
                }
                case name -> {
                    encounteredVariable = true;
                    stackSize++;
                    String name = tokens.get(i).getValue().toString();
                    if (memorySpace.variableSizes.containsKey(name)) {
                        if (memorySpace.variableTypes.get(name).equals("int16")) {
                            bytes.add(instructions.get(Instruction.push16bit));
                        } else if (memorySpace.variableTypes.get(name).equals("int8")) {
                            bytes.add(instructions.get(Instruction.pushByteAs16));
                        } else {
                            JoshLogger.importantPurple("Achievement Get: How Did We Get Here?");
                            JoshLogger.importantGreen("The compiler is on crack right now. Please report this.");
                            JoshLogger.error("Somehow there's a non integer type...? (" + memorySpace.variableTypes.get(name) + ")");
                        }
                        Byte[] splitted = FluffCompiler.splitShort(memorySpace.memoryMap.get(name));
                        bytes.add(splitted[0]);
                        bytes.add(splitted[1]);
                    } else {
                        JoshLogger.syntaxError("Variable \"" + name + "\" does not exist!", tokens.get(i).getLine());
                    }
                }
            }
        }
        return bytes;
    }
}
