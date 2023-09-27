package co.josh.processors.expression.eval;

import co.josh.JoshLogger;
import co.josh.bytecode.compile.fluff.FluffCompiler;
import co.josh.bytecode.Instruction;
import co.josh.bytecode.compile.fluff.MemorySpace;
import co.josh.processors.token.Token;
import co.josh.processors.token.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class RPN {

    public static ArrayList<Byte> compRPN_ints(ArrayList<Token> tokens,
                                               HashMap<Instruction, Byte> instructions,
                                               MemorySpace memorySpace) {
        ArrayList<Byte> bytes = new ArrayList<>();
        for (Token i : tokens){
            if(i.getTokenType() == TokenType.add){
                bytes.add(instructions.get(Instruction.add));
                //int t = a.pop()+a.pop();
                //a.push(t);
            }
            else if(i.getTokenType() == TokenType.subtract){
                bytes.add(instructions.get(Instruction.subtract));
                //int t = (a.pop()-a.pop())*(-1);
                //a.push(t);
            }
            else if(i.getTokenType() == TokenType.multiply){
                bytes.add(instructions.get(Instruction.multiply));
                //int t = a.pop()*a.pop();
                //a.push(t);
            }
            else if(i.getTokenType() == TokenType.divide){
                bytes.add(instructions.get(Instruction.divide));
                //int x = a.pop();
                //int y = a.pop();
                //a.push(y/x);
            } else if(i.getTokenType() == TokenType.modulo){
                bytes.add(instructions.get(Instruction.modulo));
            }
            else if(i.getTokenType() == TokenType.bit_shift_left){
                bytes.add(instructions.get(Instruction.lshift));
            }
            else if(i.getTokenType() == TokenType.bit_shift_right){
                bytes.add(instructions.get(Instruction.rshift));
            }
            else if(i.getTokenType() == TokenType.inequality_greater){
                bytes.add(instructions.get(Instruction.greater));
            }
            else if(i.getTokenType() == TokenType.inequality_lesser){
                bytes.add(instructions.get(Instruction.lesser));
            }
            else if(i.getTokenType() == TokenType.inequality_equals){
                bytes.add(instructions.get(Instruction.equals));
            }
            else if(i.getTokenType() == TokenType.inequality_not_equals){
                bytes.add(instructions.get(Instruction.nequals));
            }
            else if(i.getTokenType() == TokenType.inequality_lesser_equals){
                bytes.add(instructions.get(Instruction.lesser_equals));
            }
            else if(i.getTokenType() == TokenType.inequality_greater_equals){
                bytes.add(instructions.get(Instruction.greater_equals));
            }
            else if(i.getTokenType() == TokenType.not_bool_op){
                bytes.add(instructions.get(Instruction.not));
            }
            else if(i.getTokenType() == TokenType.or_bool_op){
                bytes.add(instructions.get(Instruction.or));
            }
            else if(i.getTokenType() == TokenType.and_bool_op){
                bytes.add(instructions.get(Instruction.and));
            }
            else if(i.getTokenType() == TokenType.xor_bool_op){
                bytes.add(instructions.get(Instruction.xor));
            }
            else if (i.getTokenType() == TokenType.int_literal){
                bytes.add(instructions.get(Instruction.pushConst16bit));
                Byte[] splitted = FluffCompiler.splitShort(Short.parseShort(i.getValue().toString()));
                bytes.add(splitted[0]);
                bytes.add(splitted[1]);
            } else if (i.getTokenType() == TokenType.name){
                String name = i.getValue().toString();
                if (memorySpace.variableSizes.containsKey(name)){
                    if (memorySpace.variableTypes.get(name).equals("int16")){
                        bytes.add(instructions.get(Instruction.push16bit));
                    } else if (memorySpace.variableTypes.get(name).equals("int8")){
                        bytes.add(instructions.get(Instruction.pushByteAs16));
                    } else {
                        JoshLogger.importantPurple("Achievement Get: How did we get here?");
                        JoshLogger.importantGreen("The compiler is on crack right now. Please report this.");
                        JoshLogger.error("Somehow there's a " + memorySpace.variableTypes.get(name) + " in integer math...? No bueno.");
                    }
                    Byte[] splitted = FluffCompiler.splitShort(memorySpace.memoryMap.get(name));
                    bytes.add(splitted[0]);
                    bytes.add(splitted[1]);
                } else {
                    JoshLogger.syntaxError("Variable \"" + name + "\" does not exist!", i.getLine());
                }
            }
        }
        return bytes;
    }

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
            else if (i.getTokenType() == TokenType.int_literal){
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
            } else if (i.getTokenType() == TokenType.int_literal){
                float t = (float)(int)i.getValue(); //I hate java sometimes dude
                a.push(t);
            }
        }
        return a.pop();
    }
}
