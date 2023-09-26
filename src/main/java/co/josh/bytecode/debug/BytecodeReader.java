package co.josh.bytecode.debug;

import co.josh.JoshLogger;
import co.josh.bytecode.Instruction;

import java.util.HashMap;

//Probably will implement this at some point, but just storing reverse of compiler methods for now.
public class BytecodeReader {
    public static void read(byte[] file, HashMap<Instruction, Byte> bytecodeMap){
        JoshLogger.importantPurple("Raw bytes: ");
        for (int i = 0; i < file.length; i++){
            if (i != file.length-1) System.out.printf("0x%x, ", file[i]);
            else System.out.printf("0x%x", file[i]);
        }
        System.out.print("\n");
        HashMap<Byte, Instruction> reversed = new HashMap<>();
        for (Instruction i : bytecodeMap.keySet()){
            reversed.put(bytecodeMap.get(i), i);
        }
        JoshLogger.importantPurple("Attempting translation...");
        for (int i = 0; i < file.length; i++){
            Instruction inst = null;
            if (reversed.get(file[i]) != null) inst = reversed.get(file[i]);
            else JoshLogger.error("Instruction was null. Most likely a reading error has occurred.");
            System.out.print(JoshLogger.ANSI_BLUE + i + JoshLogger.ANSI_RESET + ": ");
            switch (inst){
                case exit -> {
                    System.out.print(JoshLogger.ANSI_RED + "exit " + JoshLogger.ANSI_RESET);
                    i++;
                    System.out.print(file[i] + "\n");
                }

                case invokeInterpreterMode -> {
                    System.out.print(JoshLogger.ANSI_RED + "invokeInterpreterMode "  + JoshLogger.ANSI_RESET);
                    i++;
                    String mode = "";
                    while (file[i] != 0){
                        mode = mode + (char)file[i];
                        i++;
                    }
                    System.out.println(mode);
                    System.out.println("Cannot continue reader in non-vanilla bytecode.");
                    System.exit(0);
                }

                case copyFromAtPointer -> {
                    System.out.print(JoshLogger.ANSI_CYAN + "pointer_copy_to " + JoshLogger.ANSI_RESET + "addr ");
                    i++;
                    int lo = file[i];
                    i++;
                    int hi = file[i];
                    System.out.print((short)(((hi & 0xFF) << 8) | (lo & 0xFF)));
                    System.out.print(" from ");
                    i++;
                    lo = file[i];
                    i++;
                    hi = file[i];
                    System.out.println((short)(((hi & 0xFF) << 8) | (lo & 0xFF)));
                }

                case copyFromGetPointer -> {
                    System.out.print(JoshLogger.ANSI_CYAN + "set_at_pointer " + JoshLogger.ANSI_RESET + "addr ");
                    i++;
                    int lo = file[i];
                    i++;
                    int hi = file[i];
                    System.out.print((short)(((hi & 0xFF) << 8) | (lo & 0xFF)));
                    System.out.print(" to byte at ");
                    i++;
                    lo = file[i];
                    i++;
                    hi = file[i];
                    System.out.println((short)(((hi & 0xFF) << 8) | (lo & 0xFF)));
                }

                case pushConst16bit -> {
                    System.out.print("stack_push_16 ");
                    i++;
                    int lo = file[i];
                    i++;
                    int hi = file[i];
                    System.out.print((short)(((hi & 0xFF) << 8) | (lo & 0xFF)) + "\n");
                }

                case popToOutput -> System.out.println("char_out_pop");

                case pushInputChar -> System.out.println("char_in_push");

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

                case setByteConstAddress -> {
                    System.out.print("set_mem_byte addr ");
                    i = printMemoryJumps(file, i);
                    int a = file[i];
                    System.out.println(a);
                }

                case setWordConstAddress -> {
                    System.out.print("set_mem_16 addr ");
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
                    System.out.print(JoshLogger.ANSI_GREEN + "conditionalJumpRelative " + JoshLogger.ANSI_RESET + "to byte " + (i+a));
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
                    System.out.print(JoshLogger.ANSI_GREEN + "conditionalJumpExact " + JoshLogger.ANSI_RESET + "to byte ");
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
