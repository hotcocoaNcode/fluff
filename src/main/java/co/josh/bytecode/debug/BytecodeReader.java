package co.josh.bytecode.debug;

import co.josh.JoshLogger;
import co.josh.bytecode.Instruction;

import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;

public class BytecodeReader {
    public static void read(byte[] file, HashMap<Instruction, Byte> bytecodeMap){
        JoshLogger.importantPurple("Raw bytes (" + JoshLogger.ANSI_CYAN + "blue" + JoshLogger.ANSI_PURPLE + " is likely header): ");
        for (int i = 0; i < file.length; i++){
            if (i < file[3]){
                System.out.print(JoshLogger.ANSI_CYAN);
            } else {
                System.out.print(JoshLogger.ANSI_RESET);
            }
            if (i != file.length-1) System.out.printf("0x%x, ", ((int)file[i])&0xFF);
            else System.out.printf("0x%x", ((int)file[i])&0xFF);
            if ((i+1) % 15 == 0) System.out.print('\n'); //newline every 15 bytes
        }
        System.out.print("\n");
        HashMap<Byte, Instruction> reversed = new HashMap<>();
        for (Instruction i : bytecodeMap.keySet()){
            reversed.put(bytecodeMap.get(i), i);
        }
        int b1 = file[0] & 0xff;
        int b2 = file[1] & 0xff;
        int b3 = file[2] & 0xff;

        boolean isValidFluffBytecode =
                b1 == 0xf1 && b2 == 0x00 && b3 == 0xf1;
        JoshLogger.importantNormal("HAS VALID BYTECODE HEADER: " + (isValidFluffBytecode ? JoshLogger.ANSI_GREEN + "YES" : JoshLogger.ANSI_RED + "NO"));
        if (!isValidFluffBytecode) System.exit(0);
        JoshLogger.importantNormal("Header length: " + JoshLogger.ANSI_CYAN + file[3] + " bytes"
                + JoshLogger.ANSI_RESET +  "\nCompiler Version: "  + JoshLogger.ANSI_BLUE + file[4] + "." + file[5] + "." + file[6]
                + JoshLogger.ANSI_RESET +  "\nBytecode Version: "  + JoshLogger.ANSI_PURPLE +  file[7]);
        JoshLogger.importantNormal( JoshLogger.ANSI_WHITE + "Other ASCII data: ");
        for (int i = 5; i < file[3]; i++){
            if (file[i] >= 32) System.out.print((char)file[i]);
        }
        System.out.print( JoshLogger.ANSI_RESET + '\n');
        Scanner input = new Scanner(System.in);
        JoshLogger.importantNormal("Attempt to decode instructions? Enter 'y' for yes");
        if (input.nextLine().charAt(0) != 'y') System.exit(0);
        int[] isJumpByteFlag = new int[file.length];
        //Process linked jumps
        processLinkedJumps: for (int i = file[3]; i < file.length; i++) {
            Instruction inst;
            if (reversed.get(file[i]) != null) inst = reversed.get(file[i]);
            else break;
            switch (inst) {
                case exit, pushConstByteAs16 -> i++;

                case invokeInterpreterMode -> {
                    i++;
                    while (file[i] != 0){
                        i++;
                    }
                    break processLinkedJumps;
                }

                case copyFromGetPointer, copyFromAtPointer -> i+=6;


                case constantCopyToAtPointer -> i+=5;

                case pushConst16bit, popByte, pop16bit, pushByteAs16, push16bit -> i+=2;

                case setByteConstAddress -> i+=3;

                case setWordConstAddress -> i+=4;

                case conditionalJumpRelative -> {
                    i++;
                    int a = file[i];
                    isJumpByteFlag[i+a] = i-1;
                }

                case conditionalNotJumpExact, conditionalJumpExact -> {
                    i++;
                    int lo = file[i];
                    i++;
                    int m1 = file[i];
                    i++;
                    int m2 = file[i];
                    i++;
                    int hi = file[i];
                    int combined = (((hi & 0xFF) << 24) | ((m2 & 0xFF) << 16) | ((m1 & 0xFF) << 8) | (lo & 0xFF));
                    isJumpByteFlag[combined] = i-4;
                }
            }
        }
        JoshLogger.importantNormal("Decoded instructions: ");
        for (int i = file[3]; i < file.length; i++){
            Instruction inst = reversed.get(file[i]);
            //Non null check
            if (Objects.isNull(inst)) JoshLogger.error("Instruction was null. Most likely a reading error has occurred.");
            //Print number
            if (isJumpByteFlag[i] == 0) System.out.print(JoshLogger.ANSI_BLUE + i + JoshLogger.ANSI_RESET + ": ");
            //Is jump flagged?
            else System.out.print(JoshLogger.ANSI_CYAN + i + JoshLogger.ANSI_WHITE +  " (linked jump at " + isJumpByteFlag[i] + ")"  + JoshLogger.ANSI_RESET  +  ": ");
            switch (inst){
                case exit -> {
                    System.out.print(JoshLogger.ANSI_RED + "exit " + JoshLogger.ANSI_RESET);
                    i++;
                    System.out.print(file[i] + "\n");
                }

                case invokeInterpreterMode -> {
                    System.out.print(JoshLogger.ANSI_RED + "invokeInterpreterMode "  + JoshLogger.ANSI_RESET);
                    i++;
                    StringBuilder mode = new StringBuilder();
                    while (file[i] != 0){
                        mode.append((char) file[i]);
                        i++;
                    }
                    System.out.println(mode);
                    System.out.println("Cannot continue reader in non-vanilla bytecode.");
                    System.exit(0);
                }

                case copyFromGetPointer -> {
                    i++;
                    int lo = file[i];
                    i++;
                    int hi = file[i];
                    int pointerAddress = (short) (((hi & 0xFF) << 8) | (lo & 0xFF));
                    i++;
                    lo = file[i];
                    i++;
                    hi = file[i];
                    int address = (short) (((hi & 0xFF) << 8) | (lo & 0xFF));
                    i++;
                    lo = file[i];
                    i++;
                    hi = file[i];
                    int offset = (short) (((hi & 0xFF) << 8) | (lo & 0xFF));
                    System.out.print(JoshLogger.ANSI_CYAN + "get_from_pointer "
                            + JoshLogger.ANSI_RESET + "byte at addr at " + pointerAddress + " plus " + offset +
                            " to byte at " + address + "\n");
                }

                case constantCopyToAtPointer -> {
                    i++;
                    int lo = file[i];
                    i++;
                    int hi = file[i];
                    int pointerAddress = (short) (((hi & 0xFF) << 8) | (lo & 0xFF));
                    i++;
                    byte constant = file[i];
                    i++;
                    lo = file[i];
                    i++;
                    hi = file[i];
                    int offset = (short) (((hi & 0xFF) << 8) | (lo & 0xFF));
                    System.out.print(JoshLogger.ANSI_CYAN + "set_at_pointer "
                            + JoshLogger.ANSI_RESET + "addr at " + pointerAddress + " plus " + offset +
                            " to byte " + constant + "\n");
                }

                case copyFromAtPointer -> {
                    i++;
                    int lo = file[i];
                    i++;
                    int hi = file[i];
                    int pointerAddress = (short)(((hi & 0xFF) << 8) | (lo & 0xFF));
                    i++;
                    lo = file[i];
                    i++;
                    hi = file[i];
                    int address = (short) (((hi & 0xFF) << 8) | (lo & 0xFF));
                    i++;
                    lo = file[i];
                    i++;
                    hi = file[i];
                    int offset = (short) (((hi & 0xFF) << 8) | (lo & 0xFF));
                    System.out.print(JoshLogger.ANSI_CYAN + "set_at_pointer "
                            + JoshLogger.ANSI_RESET + "addr at " + pointerAddress + " plus " + offset +
                            " to byte at " + address + "\n");
                }

                case pushConst16bit -> {
                    System.out.print("stack_push_16 ");
                    i++;
                    int lo = file[i];
                    i++;
                    int hi = file[i];
                    System.out.print((short)(((hi & 0xFF) << 8) | (lo & 0xFF)) + "\n");
                }

                case popToOutput -> System.out.println(JoshLogger.ANSI_RED + "char_out_pop");

                case pushInputChar -> System.out.println(JoshLogger.ANSI_RED + "char_in_push");

                case popByte -> {
                    System.out.print("stack_pop_b_addr ");
                    i++;
                    int lo = file[i];
                    i++;
                    int hi = file[i];
                    System.out.print((short)(((hi & 0xFF) << 8) | (lo & 0xFF)) + "\n");
                }

                case pop16bit -> {
                    System.out.print("stack_pop_16_addr ");
                    i++;
                    int lo = file[i];
                    i++;
                    int hi = file[i];
                    System.out.print((short)(((hi & 0xFF) << 8) | (lo & 0xFF)) + "\n");
                }

                case pushByteAs16 -> {
                    System.out.print("stack_push_b_addr ");
                    i++;
                    int lo = file[i];
                    i++;
                    int hi = file[i];
                    System.out.print((short)(((hi & 0xFF) << 8) | (lo & 0xFF)) + "\n");
                }

                case push16bit -> {
                    System.out.print("stack_push_16 addr ");
                    i++;
                    int lo = file[i];
                    i++;
                    int hi = file[i];
                    System.out.print((short)(((hi & 0xFF) << 8) | (lo & 0xFF)) + "\n");
                }

                case pushConstByteAs16 -> {
                    System.out.print("stack_push_b ");
                    i++;
                    int a = file[i];
                    System.out.print(a + "\n");
                }

                case add -> System.out.println(JoshLogger.ANSI_YELLOW + "add_stack" + JoshLogger.ANSI_RESET);

                case subtract -> System.out.println(JoshLogger.ANSI_YELLOW + "subtract_stack" + JoshLogger.ANSI_RESET);

                case divide -> System.out.println(JoshLogger.ANSI_YELLOW + "divide_stack" + JoshLogger.ANSI_RESET);

                case multiply -> System.out.println(JoshLogger.ANSI_YELLOW + "multiply_stack" + JoshLogger.ANSI_RESET);

                case modulo -> System.out.println(JoshLogger.ANSI_YELLOW + "modulo_stack" + JoshLogger.ANSI_RESET);

                case lshift -> System.out.println(JoshLogger.ANSI_YELLOW + "left_shift_stack" + JoshLogger.ANSI_RESET);

                case rshift -> System.out.println(JoshLogger.ANSI_YELLOW + "right_shift_stack" + JoshLogger.ANSI_RESET);

                case not -> System.out.println(JoshLogger.ANSI_YELLOW + "not_boolean_stack" + JoshLogger.ANSI_RESET);

                case or -> System.out.println(JoshLogger.ANSI_YELLOW + "or_boolean_stack" + JoshLogger.ANSI_RESET);

                case and -> System.out.println(JoshLogger.ANSI_YELLOW + "and_boolean_stack" + JoshLogger.ANSI_RESET);

                case xor -> System.out.println(JoshLogger.ANSI_YELLOW + "xor_stack" + JoshLogger.ANSI_RESET);

                case greater -> System.out.println(JoshLogger.ANSI_YELLOW + "greater_inequality_stack" + JoshLogger.ANSI_RESET);

                case equals -> System.out.println(JoshLogger.ANSI_YELLOW + "equals_inequality_stack" + JoshLogger.ANSI_RESET);

                case nequals -> System.out.println(JoshLogger.ANSI_YELLOW + "not_equals_inequality_stack" + JoshLogger.ANSI_RESET);

                case lesser -> System.out.println(JoshLogger.ANSI_YELLOW + "lesser_inequality_stack" + JoshLogger.ANSI_RESET);

                case greater_equals -> System.out.println(JoshLogger.ANSI_YELLOW + "greater_equals_inequality_stack" + JoshLogger.ANSI_RESET);

                case lesser_equals -> System.out.println(JoshLogger.ANSI_YELLOW + "lesser_equals_inequality_stack" + JoshLogger.ANSI_RESET);

                case setByteConstAddress -> {
                    System.out.print(JoshLogger.ANSI_PURPLE + "set_mem_byte" + JoshLogger.ANSI_RESET + " addr ");
                    i = printMemoryJumps(file, i);
                    int a = file[i];
                    System.out.println(a);
                }

                case setWordConstAddress -> {
                    System.out.print(JoshLogger.ANSI_PURPLE + "set_mem_16" + JoshLogger.ANSI_RESET + " addr ");
                    i = printMemoryJumps(file, i);
                    int lo;
                    int hi;
                    lo = file[i];
                    i++;
                    hi = file[i];
                    System.out.println((short)(((hi & 0xFF) << 8) | (lo & 0xFF)));
                }

                case conditionalJumpRelative -> {
                    i++;
                    int a = file[i];
                    System.out.print(JoshLogger.ANSI_GREEN + "conditionalJumpRelative " + JoshLogger.ANSI_RESET + "to byte " + JoshLogger.ANSI_CYAN + (i+a));
                    System.out.println(JoshLogger.ANSI_WHITE + " gap " + a + " (" + ((a < 0) ? "likely a loop" : "likely an if statement") + "), instruction " + reversed.get(file[i+a]) +JoshLogger.ANSI_RESET);
                }

                case conditionalNotJumpExact -> {
                    System.out.print(JoshLogger.ANSI_GREEN + "conditionalNotJumpExact " + JoshLogger.ANSI_RESET + "to byte ");
                    i++;
                    int lo = file[i];
                    i++;
                    int m1 = file[i];
                    i++;
                    int m2 = file[i];
                    i++;
                    int hi = file[i];
                    int combined = (((hi & 0xFF) << 24) | ((m2 & 0xFF) << 16) | ((m1 & 0xFF) << 8) | (lo & 0xFF));
                    int gap = -(i - combined);
                    System.out.print(combined);
                    System.out.println(JoshLogger.ANSI_WHITE + " gap " + gap + " (" + ((gap < 0) ? "likely a loop" : "likely an if statement") + "), instruction " + reversed.get(file[combined]) +JoshLogger.ANSI_RESET);
                }

                case conditionalJumpExact -> {
                    System.out.print(JoshLogger.ANSI_GREEN + "conditionalJumpExact " + JoshLogger.ANSI_RESET + "to byte "  + JoshLogger.ANSI_CYAN);
                    i++;
                    int lo = file[i];
                    i++;
                    int m1 = file[i];
                    i++;
                    int m2 = file[i];
                    i++;
                    int hi = file[i];
                    int combined = (((hi & 0xFF) << 24) | ((m2 & 0xFF) << 16) | ((m1 & 0xFF) << 8) | (lo & 0xFF));
                    int gap = -(i - combined);
                    System.out.print(combined);
                    System.out.println(JoshLogger.ANSI_WHITE + " gap " + gap + " (" + ((gap < 0) ? "likely a loop" : "likely an if statement") + "), instruction " + reversed.get(file[combined]) +JoshLogger.ANSI_RESET);
                }

                default -> {
                    System.out.printf("%x", file[i]);
                    System.out.println(" (decimal: " + file[i] + ")");
                }
            }
        }
    }

    private static int printMemoryJumps(byte[] file, int i) {
        i++;
        int lo = file[i];
        i++;
        int hi = file[i];
        System.out.print((short)(((hi & 0xFF) << 8) | (lo & 0xFF)));
        System.out.print(" const ");
        i++;
        return i;
    }
}
