package co.josh.bytecode.local_interpret;

import co.josh.JoshLogger;
import co.josh.bytecode.Instruction;
import co.josh.bytecode.compile.cex.CompilerExtension;
import co.josh.bytecode.compile.fluff.FluffCompiler;

import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

public class BytecodeInterpreter {

    public static void interpretBytecode(byte[] bytecode, FluffCompiler compiler, int ramSize){
        byte[] ram = new byte[ramSize];
        Stack<Short> opstack = new Stack<>();
        HashMap<Byte, Instruction> reversed = new HashMap<>();
        Scanner sc = new Scanner(System.in);
        String scannerLine = "";
        for (Instruction i : compiler.bytecodeMap.keySet()) {
            reversed.put(compiler.bytecodeMap.get(i), i);
        }
        for (int i = 0; i < bytecode.length; i++){
            if (opstack.size() >= 256) {
                JoshLogger.error("Operator stack overflow!");
            }
            Instruction inst = reversed.get(bytecode[i]);
            if (inst == null){
                JoshLogger.error("\nUnknown instruction " + bytecode[i]);
            } else {
                switch (inst) {
                    case exit -> {
                        i++;
                        byte code = bytecode[i];
                        System.out.println("\nExiting with code " + code);
                        System.exit(code);
                    }

                    case invokeInterpreterMode -> {
                        StringBuilder interpreterMode = new StringBuilder();
                        i++;
                        while (bytecode[i] != 0){
                            interpreterMode.append((char) bytecode[i]);
                            i++;
                        }
                        CompilerExtension cex = compiler.cexList.get(interpreterMode.toString());
                        i = cex.interpret(bytecode, ram, i);
                    }

                    case copyFromAtPointer -> {
                        i++;
                        int lo = bytecode[i];
                        i++;
                        int hi = bytecode[i];
                        int pointerAddress = (short) (((hi & 0xFF) << 8) | (lo & 0xFF));
                        i++;
                        lo = bytecode[i];
                        i++;
                        hi = bytecode[i];
                        int addressFrom = (short) (((hi & 0xFF) << 8) | (lo & 0xFF));
                        lo = ram[pointerAddress];
                        hi = ram[pointerAddress+1];
                        int addressTo = (short) (((hi & 0xFF) << 8) | (lo & 0xFF));
                        i++;
                        lo = bytecode[i];
                        i++;
                        hi = bytecode[i];
                        int offset = (short) (((hi & 0xFF) << 8) | (lo & 0xFF));
                        ram[addressTo+offset] = ram[addressFrom+offset]; //Set value at pointer to value at other address
                    }

                    case constantCopyToAtPointer -> {
                        i++;
                        int lo = bytecode[i];
                        i++;
                        int hi = bytecode[i];
                        int pointerAddress = (short) (((hi & 0xFF) << 8) | (lo & 0xFF));
                        i++;
                        byte constant = bytecode[i];
                        lo = ram[pointerAddress];
                        hi = ram[pointerAddress+1];
                        int addressTo = (short) (((hi & 0xFF) << 8) | (lo & 0xFF));
                        i++;
                        lo = bytecode[i];
                        i++;
                        hi = bytecode[i];
                        int offset = (short) (((hi & 0xFF) << 8) | (lo & 0xFF));
                        ram[addressTo+offset] = constant; //Set value at pointer constant byte
                    }


                    case copyFromGetPointer -> {
                        i++;
                        int lo = bytecode[i];
                        i++;
                        int hi = bytecode[i];
                        int pointerAddress = (short) (((hi & 0xFF) << 8) | (lo & 0xFF));
                        i++;
                        lo = bytecode[i];
                        i++;
                        hi = bytecode[i];
                        int addressFrom = (short) (((hi & 0xFF) << 8) | (lo & 0xFF));
                        lo = ram[pointerAddress];
                        hi = ram[pointerAddress+1];
                        int addressTo = (short) (((hi & 0xFF) << 8) | (lo & 0xFF));
                        i++;
                        lo = bytecode[i];
                        i++;
                        hi = bytecode[i];
                        int offset = (short) (((hi & 0xFF) << 8) | (lo & 0xFF));
                        ram[addressFrom+offset] = ram[addressTo+offset]; //Get the value at pointer and set value at other address
                    }

                    case pushConst16bit -> {
                        i++;
                        int lo = bytecode[i];
                        i++;
                        int hi = bytecode[i];
                        opstack.push((short) (((hi & 0xFF) << 8) | (lo & 0xFF)));
                    }

                    case popToOutput -> System.out.print((char)opstack.pop().byteValue());

                    case pushInputChar -> {
                        if (scannerLine.isEmpty()) {
                            scannerLine = sc.nextLine()+'\n'; //cfi parity
                        }
                        opstack.push((short) scannerLine.charAt(0));
                        scannerLine = scannerLine.substring(1);
                    }

                    case popByte -> {
                        i++;
                        int lo = bytecode[i];
                        i++;
                        int hi = bytecode[i];
                        ram[(short) (((hi & 0xFF) << 8) | (lo & 0xFF))] = opstack.pop().byteValue();
                    }

                    case pop16bit -> {
                        i++;
                        int lo = bytecode[i];
                        i++;
                        int hi = bytecode[i];
                        Byte[] split = FluffCompiler.splitShort(opstack.pop());
                        ram[(short) (((hi & 0xFF) << 8) | (lo & 0xFF))] = split[0];
                        ram[(short) (((hi & 0xFF) << 8) | (lo & 0xFF))+1] = split[1];
                    }

                    case pushByteAs16 -> {
                        i++;
                        int lo = bytecode[i];
                        i++;
                        int hi = bytecode[i];
                        opstack.push((short) ram[(short) (((hi & 0xFF) << 8) | (lo & 0xFF))]);
                    }

                    case push16bit -> {
                        //Parse address from file
                        i++;
                        int lo = bytecode[i];
                        i++;
                        int hi = bytecode[i];
                        short address = (short) (((hi & 0xFF) << 8) | (lo & 0xFF));
                        //Parse short from ram
                        lo = ram[address];
                        hi = ram[address+1];
                        opstack.push((short) (((hi & 0xFF) << 8) | (lo & 0xFF)));
                    }

                    case pushConstByteAs16 -> {
                        i++;
                        int a = bytecode[i];
                        opstack.push((short) a);
                    }

                    case add -> {
                        short a = opstack.pop();
                        short b = opstack.pop();
                        opstack.push((short) (b + a));
                    }

                    case subtract -> {
                        short a = opstack.pop();
                        short b = opstack.pop();
                        opstack.push((short) ((b - a)));
                    }

                    case divide -> {
                        short a = opstack.pop();
                        short b = opstack.pop();
                        opstack.push((short) ((b / a)));
                    }

                    case multiply -> {
                        short a = opstack.pop();
                        short b = opstack.pop();
                        opstack.push((short) ((b * a)));
                    }

                    case modulo -> {
                        short a = opstack.pop();
                        short b = opstack.pop();
                        opstack.push((short) ((b % a)));
                    }

                    case lshift -> {
                        short a = opstack.pop();
                        short b = opstack.pop();
                        opstack.push((short) ((b << a)));
                    }

                    case rshift -> {
                        short a = opstack.pop();
                        short b = opstack.pop();
                        opstack.push((short) ((b >> a)));
                    }

                    case not -> {
                        short a = opstack.pop();
                        opstack.push((short) (a == 1 ? 0 : 1));
                    }

                    case or -> {
                        short a = opstack.pop();
                        short b = opstack.pop();
                        opstack.push((short) ((b==1 || a==1 ? 1 : 0)));
                    }

                    case and -> {
                        short a = opstack.pop();
                        short b = opstack.pop();
                        opstack.push((short) ((b==1 && a==1 ? 1 : 0)));
                    }

                    case xor -> {
                        short a = opstack.pop();
                        short b = opstack.pop();
                        opstack.push((short) ((b ^ a)));
                    }

                    case greater -> {
                        short a = opstack.pop();
                        short b = opstack.pop();
                        opstack.push(b > a ? (short)1 : (short)0);
                    }

                    case equals -> {
                        short a = opstack.pop();
                        short b = opstack.pop();
                        opstack.push(b == a ? (short)1 : (short)0);
                    }

                    case nequals -> {
                        short a = opstack.pop();
                        short b = opstack.pop();
                        opstack.push(b != a ? (short)1 : (short)0);
                    }

                    case lesser -> {
                        short a = opstack.pop();
                        short b = opstack.pop();
                        opstack.push(b < a ? (short)1 : (short)0);
                    }

                    case lesser_equals -> {
                        short a = opstack.pop();
                        short b = opstack.pop();
                        opstack.push(b <= a ? (short)1 : (short)0);
                    }

                    case greater_equals -> {
                        short a = opstack.pop();
                        short b = opstack.pop();
                        opstack.push(b >= a ? (short)1 : (short)0);
                    }

                    case setByteConstAddress -> {
                        i++;
                        int lo = bytecode[i];
                        i++;
                        int hi = bytecode[i];
                        short addr = (short) (((hi & 0xFF) << 8) | (lo & 0xFF));
                        i++;
                        byte a = bytecode[i];
                        ram[addr] = a;
                    }

                    case setWordConstAddress -> {
                        i++;
                        byte lo = bytecode[i];
                        i++;
                        byte hi = bytecode[i];
                        short addr = (short) (((hi & 0xFF) << 8) | (lo & 0xFF));
                        i++;
                        lo = bytecode[i];
                        i++;
                        hi = bytecode[i];
                        ram[addr] = lo;
                        ram[addr+1] = hi;
                    }

                    case conditionalJumpRelative -> {
                        i++;
                        if (opstack.pop() == (short)1){
                            int a = bytecode[i];
                            i += a-1;
                        }
                    }

                    case conditionalJumpExact -> {
                        i++;
                        int lo = bytecode[i];
                        i++;
                        int m1 = bytecode[i];
                        i++;
                        int m2 = bytecode[i];
                        i++;
                        int hi = bytecode[i];
                        if (opstack.pop() == (short)1){
                            i = (((hi & 0xFF) << 24) | ((m2 & 0xFF) << 16) | ((m1 & 0xFF) << 8) | (lo & 0xFF))-1;
                        }
                    }

                    case conditionalNotJumpExact -> {
                        i++;
                        int lo = bytecode[i];
                        i++;
                        int m1 = bytecode[i];
                        i++;
                        int m2 = bytecode[i];
                        i++;
                        int hi = bytecode[i];
                        if (opstack.pop() != (short)1){
                            i = (((hi & 0xFF) << 24) | ((m2 & 0xFF) << 16) | ((m1 & 0xFF) << 8) | (lo & 0xFF))-1;
                        }
                    }

                    default -> JoshLogger.error("Unknown instruction " + bytecode[i]);
                }
            }
        }
        JoshLogger.error("Program did not call exit 0, no known success!");
    }
}
