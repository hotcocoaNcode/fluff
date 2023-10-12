package co.josh.processors.expression.eval;

import co.josh.JoshLogger;
import co.josh.bytecode.compile.fluff.FluffCompiler;
import co.josh.bytecode.Instruction;
import co.josh.bytecode.compile.fluff.MemorySpace;
import co.josh.processors.token.Token;

import java.util.ArrayList;
import java.util.HashMap;

public class RPN {

    public static ArrayList<Byte> compRPN_ints(ArrayList<Token> tokens,
                                               HashMap<Instruction, Byte> instructions,
                                               MemorySpace memorySpace) {
        ArrayList<Byte> bytes = new ArrayList<>();
        short stackSize = 0;
        ExpOpt.optimize(tokens);
        for (Token token : tokens) {
            if (stackSize > 255) {
                JoshLogger.error("RPN Stack Overflow! Please split the expression into multiple parts.");
            } else if (stackSize < 0) {
                JoshLogger.error("RPN Stack at negative size! Something has gone seriously wrong.");
            }
            switch (token.getTokenType()) {
                case add -> {
                    bytes.add(instructions.get(Instruction.add));
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
                    Byte[] splitted = FluffCompiler.splitShort(Short.parseShort(token.getValue().toString()));
                    bytes.add(splitted[0]);
                    bytes.add(splitted[1]);
                }
                case name -> {
                    stackSize++;
                    String name = token.getValue().toString();
                    if (memorySpace.variableSizes.containsKey(name)) {
                        if (memorySpace.variableTypes.get(name).equals("int16")) {
                            bytes.add(instructions.get(Instruction.push16bit));
                        } else if (memorySpace.variableTypes.get(name).equals("int8")) {
                            bytes.add(instructions.get(Instruction.pushByteAs16));
                        } else {
                            JoshLogger.error("Non integer type (" + memorySpace.variableTypes.get(name) + ") in expression.");
                        }
                        Byte[] splitted = FluffCompiler.splitShort(memorySpace.memoryMap.get(name));
                        bytes.add(splitted[0]);
                        bytes.add(splitted[1]);
                    } else {
                        JoshLogger.syntaxError("Variable \"" + name + "\" does not exist!", token.getLine());
                    }
                }
            }
        }
        if (stackSize > 1) {
            JoshLogger.error("Operation stack has more than one value left.");
        }
        return bytes;
    }
}
