package co.josh.compile;

import co.josh.JoshLogger;
import co.josh.processors.token.Token;
import co.josh.processors.token.TokenType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;

public class BytecodeCompiler {

    HashMap<Instruction, Byte> bytecodeMap = new HashMap<Instruction, Byte>();

    public BytecodeCompiler(int compilerVersion){
        if (compilerVersion == 0){
            //Exit
            bytecodeMap.put(Instruction.exit, (byte) 0); //exit <byte>
            //Variable management
            bytecodeMap.put(Instruction.setVariableFConst, (byte) 1); //set variable <short>
            bytecodeMap.put(Instruction.setVariableFVariable, (byte) 2); //set variable <short>
            bytecodeMap.put(Instruction.newVariableFConst, (byte) 3); //new variable <short>
            bytecodeMap.put(Instruction.newVariableFVariable, (byte) 4); //new variable <short>
            //Integer math
            bytecodeMap.put(Instruction.add, (byte) 5); //add r1 + r2 -> r1
            bytecodeMap.put(Instruction.sub, (byte) 6); //subtract r1 - r2 -> r1
            bytecodeMap.put(Instruction.mult, (byte) 7); //multiply r1*r2 -> r1
            bytecodeMap.put(Instruction.div, (byte) 8); //divide r1/r2 -> r1
            //Jumps
            bytecodeMap.put(Instruction.jumpmarker, (byte) 9); //Compiler generated markers for jumping
            bytecodeMap.put(Instruction.jump, (byte) 10); //Jump to marker (Compiler generated)
            //Definitions
            //bytecodeMap.put(Instruction.newtype, (byte) 11); //New type with custom byte width
            //bytecodeMap.put(Instruction.newmacro, (byte) 12); //New macro
            //bytecodeMap.put(Instruction.macroexec, (byte) 13); //Execute existing macro
            //Registers

        } else {
            JoshLogger.error("Bytecode revision " + compilerVersion + " does not exist!\n" +
                                        "Are you sure you have given the compiler the correct arguments?");
        }
    }

    ArrayList<Byte> bytes = new ArrayList<>();
    HashMap<String, Short> memoryMap = new HashMap<>();
    HashMap<String, Short> macros = new HashMap<>();
    HashMap<String, TokenType> variableTypes = new HashMap<>();
    HashMap<String, Integer> variableScopes = new HashMap<>();
    HashMap<String, Integer> variableSizes = new HashMap<>();

    public byte[] compile(ArrayList<Token> tokens){
        int scopeCounter = 0;
        short macroCount = 0;
        for (int i = 0; i < tokens.size(); i++){
            switch (tokens.get(i).getTokenType()) {
                case exit -> {
                    bytes.add(bytecodeMap.get(Instruction.exit));
                    i++;
                    if (tokens.get(i).getTokenType() == TokenType.int_val){
                        String sizeStr = tokens.get(i).getValue().toString();
                        byte byteval = Byte.parseByte(sizeStr);
                        bytes.add(byteval);
                    } else {
                        JoshLogger.syntaxError("Exit code must be constant byte!");
                    }
                }

                case macro_def -> {
                    bytes.add(bytecodeMap.get(Instruction.newmacro)); //TODO currently nop
                    i++;
                    if (tokens.get(i).getTokenType() == TokenType.name){
                        macros.put(tokens.get(i).getValue().toString(), macroCount);
                        macroCount++;
                    } else {
                        JoshLogger.syntaxError("Macro must have non-keyword name!");
                    }
                }

                case name -> {
                    String name = tokens.get(i).getValue().toString();
                    i++;
                    if (tokens.get(i).getTokenType() == TokenType.quick_assign){ //Singular (no expression)
                        i++;
                        Byte[] indexOfVariable = splitShort(memoryMap.get(name));
                        if (tokens.get(i).getTokenType() == TokenType.name){
                            TokenType thisType = variableTypes.get(tokens.get(i).getValue().toString());
                            bytes.add(bytecodeMap.get(Instruction.setVariableFVariable));
                            bytes.add(indexOfVariable[0]);
                            bytes.add(indexOfVariable[1]);
                            i++;
                            if ((tokens.get(i).getTokenType() == TokenType.name)
                                    && (variableTypes.get(tokens.get(i).getValue().toString()) == thisType)){
                                Byte[] indexOfVar2 = splitShort(memoryMap.get(tokens.get(i).getValue().toString()));
                                bytes.add(indexOfVar2[0]);
                                bytes.add(indexOfVar2[1]);
                            }
                        } else {
                            bytes.add(bytecodeMap.get(Instruction.setVariableFConst));
                            bytes.add(indexOfVariable[0]);
                            bytes.add(indexOfVariable[1]);
                            i++;
                            if (tokens.get(i).getTokenType() == TokenType.int_val) {
                                if (variableTypes.get(name) == TokenType.int_val) {
                                    String sizeStr = tokens.get(i).getValue().toString();
                                    Byte[] value = splitShort(Short.parseShort(sizeStr));
                                    bytes.add(value[0]);
                                    bytes.add(value[1]);
                                } else if (variableTypes.get(name) == TokenType.byte_val) {
                                    String sizeStr = tokens.get(i).getValue().toString();
                                    try {
                                        byte byteval = Byte.parseByte(sizeStr);
                                        bytes.add(byteval);
                                        bytes.add((byte) 0);
                                    } catch (Exception e) {
                                        JoshLogger.syntaxError("Byte values must be between -127 and 127!");
                                    }
                                }
                            } else if (tokens.get(i).getTokenType() == TokenType.float_val) {
                                if (variableTypes.get(name) == TokenType.float_val) {
                                    String sizeStr = tokens.get(i).getValue().toString();
                                    Byte[] value = splitShort(fromFloat(Float.parseFloat(sizeStr)));
                                    bytes.add(value[0]);
                                    bytes.add(value[1]);
                                } else {
                                    JoshLogger.syntaxError("Attempting to assign float to " + variableTypes.get(name));
                                }
                            } else if (tokens.get(i).getTokenType() == TokenType.boolean_val) {
                                if (variableTypes.get(name) == TokenType.boolean_val) {
                                    boolean booleanval = (boolean) tokens.get(i).getValue();
                                    bytes.add(booleanval ? (byte) 1 : (byte) 0);
                                    bytes.add((byte) 0);
                                } else {
                                    JoshLogger.syntaxError("Attempting to assign boolean to " + variableTypes.get(name));
                                }
                            }
                        }
                    } else if (tokens.get(i).getTokenType() == TokenType.bracket_open) { //Array
                        i++;
                        if (tokens.get(i).getTokenType() == TokenType.int_val) {
                            Byte[] index = splitShort((short) (memoryMap.get(name) + (int) tokens.get(i).getValue()));
                            bytes.add(index[0]);
                            bytes.add(index[1]);
                        } else {
                            JoshLogger.syntaxError("Array index must be integer value!");
                        }
                    }
                    else if (macros.containsKey(name)) {
                        bytes.add(bytecodeMap.get(Instruction.macroexec)); //TODO currently nop
                    } else {
                        JoshLogger.syntaxError("Error parsing name \"" + name + "\"!");
                    }
                }

                case int_var -> {
                    bytes.add(bytecodeMap.get(Instruction.newVariableFConst));
                    i++;
                    if (tokens.get(i).getTokenType() == TokenType.name && !variableTypes.containsKey(tokens.get(i).getValue().toString())){
                        String name = tokens.get(i).getValue().toString();
                        variableTypes.put(name, TokenType.int_val);
                        i++;
                        if (tokens.get(i).getTokenType() == TokenType.quick_assign){
                            i++;
                            if (tokens.get(i).getTokenType() == TokenType.int_val) {
                                String sizeStr = tokens.get(i).getValue().toString();
                                Byte[] value = splitShort(Short.parseShort(sizeStr));
                                bytes.add(value[0]);
                                bytes.add(value[1]);
                                Short address = mapAlloc(1);
                                memoryMap.put(name, address);
                                variableSizes.put(name, 1);
                                value = splitShort(address);
                                bytes.add(value[0]);
                                bytes.add(value[1]);
                            } else {
                                JoshLogger.syntaxError("Cannot assign non-int to int!");
                            }
                        }
                        else if (tokens.get(i).getTokenType() == TokenType.equals) {
                            i++;
                            ArrayList<Token> expression = new ArrayList<>();
                            while (tokens.get(i).getTokenType() != TokenType.semi){
                                expression.add(tokens.get(i));
                                i++;
                            }
                            //TODO finish expression madness with register commands

                        } else {
                            JoshLogger.syntaxError("Variable declaration must always assign a value!");
                        }
                    } else {
                        JoshLogger.syntaxError("Integer declaration must be followed by an unused name!");
                    }
                }
            }
        }
        byte[] returnVal = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++){
            returnVal[i] = bytes.get(i);
        }
        return returnVal;
    }

    Short mapAlloc(int size){
        short ret = 0;
        for (String s : memoryMap.keySet()){
            Short c = memoryMap.get(s);
            Integer size1 = variableSizes.get(s);
            for (int i = 0; i < size; i++){
                if ((ret >= c) && (ret <= c + size1)) {
                    ret = (short) (c + size1);
                    break;
                }
            }

        }
        return ret;
    }

    Byte[] splitShort(Short s){
        Byte[] ret = new Byte[2];
        ret[0] = (byte)(s & 0xff);
        ret[1] = (byte)((s >> 8) & 0xff);
        return ret;
    }

    public static Short fromFloat( float fval )
    {
        //I'm going to be honest with you, I stole this shit *SO* hard.
        //Whoever spent a long time making this on stack overflow, thank you.
        int fbits = Float.floatToIntBits( fval );
        int sign = fbits >>> 16 & 0x8000;          // sign only
        int val = ( fbits & 0x7fffffff ) + 0x1000; // rounded value

        if( val >= 0x47800000 )               // might be or become NaN/Inf
        {                                     // avoid Inf due to rounding
            if( ( fbits & 0x7fffffff ) >= 0x47800000 )
            {                                 // is or must become NaN/Inf
                if( val < 0x7f800000 )        // was value but too large
                    return (short)(sign | 0x7c00);     // make it +/-Inf
                return (short)(sign | 0x7c00 |        // remains +/-Inf or NaN
                        ( fbits & 0x007fffff ) >>> 13); // keep NaN (and Inf) bits
            }
            return (short)(sign | 0x7bff);             // unrounded not quite Inf
        }
        if( val >= 0x38800000 )               // remains normalized value
            return (short)(sign | val - 0x38000000 >>> 13); // exp - 127 + 15
        if( val < 0x33000000 )                // too small for subnormal
            return (short)sign;                      // becomes +/-0
        val = ( fbits & 0x7fffffff ) >>> 23;  // tmp exp for subnormal calc
        return (short)(sign | ( ( fbits & 0x7fffff | 0x800000 ) // add subnormal bit
                + ( 0x800000 >>> val - 102 )     // round depending on cut off
                >>> 126 - val ));   // div by 2^(1-(exp-127+15)) and >> 13 | exp=0
    }
}
