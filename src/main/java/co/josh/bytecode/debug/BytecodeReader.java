package co.josh.bytecode.debug;

import co.josh.JoshLogger;
import co.josh.bytecode.Instruction;

import java.util.HashMap;

//Probably will implement this at some point, but just storing reverse of compiler methods for now.
public class BytecodeReader {
    public static void read(byte[] file, HashMap<Instruction, Byte> bytecodeMap){
        System.out.println("Raw bytes: ");
        for (int i = 0; i < file.length; i++){
            if (i != file.length-1) System.out.printf("0x%x, ", file[i]);
            else System.out.printf("0x%x", file[i]);
        }
        System.out.print("\n");
        HashMap<Byte, Instruction> reversed = new HashMap<>();
        for (Instruction i : bytecodeMap.keySet()){
            reversed.put(bytecodeMap.get(i), i);
        }
        System.out.println("Attempting translation...");
        for (int i = 0; i < file.length; i++){
            Instruction inst = null;
            if (reversed.get(file[i]) != null) inst = reversed.get(file[i]);
            else JoshLogger.error("Instruction was null. Most likely a reading error has occured.");
            switch (inst){
                case exit -> {
                    System.out.print("exit ");
                    i++;
                    System.out.print(file[i] + "\n");
                }

                case invokeInterpreterMode -> {
                    System.out.print("invokeInterpreterMode ");
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
                    System.out.print("pointer_copy_to addr ");
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
                    System.out.print("set_pointer_at addr ");
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

                case popToOutput -> {
                    System.out.println("char_out_pop");
                }

                case pushInputChar -> {
                    System.out.println("char_in_push");
                }

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
                    System.out.print("stack_push_16_addr ");
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

                case add -> {
                    System.out.println("add_stack");
                }

                case subtract -> {
                    System.out.println("sub_stack");
                }

                case divide -> {
                    System.out.println("divide_stack");
                }

                case multiply -> {
                    System.out.println("multiply_stack");
                }

                case modulo -> {
                    System.out.println("modulo_stack");
                }

                case lshift -> {
                    System.out.println("left_shift_stack");
                }

                case rshift -> {
                    System.out.println("right_shift_stack");
                }

                case not -> {
                    System.out.println("not_stack");
                }

                case or -> {
                    System.out.println("or_stack");
                }

                case and -> {
                    System.out.println("and_stack");
                }

                case xor -> {
                    System.out.println("xor_stack");
                }

                case greater -> {
                    System.out.println("greater_stack");
                }

                case equals -> {
                    System.out.println("equals_stack");
                }

                case nequals -> {
                    System.out.println("not_equals_stack");
                }

                case lesser -> {
                    System.out.println("lesser_stack");
                }

                case setByteConstAddress -> {
                    System.out.print("set_mem_byte addr ");
                    i++;
                    int lo = file[i];
                    i++;
                    int hi = file[i];
                    System.out.print((short)(((hi & 0xFF) << 8) | (lo & 0xFF)));
                    System.out.print(" const ");
                    i++;
                    int a = file[i];
                    System.out.println(a);
                }

                case setWordConstAddress -> {
                    System.out.print("set_mem_16 addr ");
                    i++;
                    int lo = file[i];
                    i++;
                    int hi = file[i];
                    System.out.print((short)(((hi & 0xFF) << 8) | (lo & 0xFF)));
                    System.out.print(" const ");
                    i++;
                    lo = file[i];
                    i++;
                    hi = file[i];
                    System.out.println((short)(((hi & 0xFF) << 8) | (lo & 0xFF)));
                }

                case conditionalJumpRelative -> {
                    System.out.print("conditionalJumpRelative to byte ");
                    i++;
                    int a = file[i];
                    System.out.println((i+a) + JoshLogger.ANSI_WHITE + " (gap " + a + ", instruction " + reversed.get(file[i+a]) + ")" + JoshLogger.ANSI_RESET);
                }

                case conditionalJumpExact -> {
                    System.out.print("conditionalJumpExact to byte ");
                    i++;
                    int lo = file[i];
                    i++;
                    int m1 = file[i];
                    i++;
                    int m2 = file[i];
                    i++;
                    int hi = file[i];
                    int combined = (((hi & 0xFF) << 24) | ((m2 & 0xFF) << 16) | ((m1 & 0xFF) << 8) | (lo & 0xFF));
                    System.out.print(combined);
                    System.out.println(JoshLogger.ANSI_WHITE + " (gap " + -(i - combined) + ", instruction " + reversed.get(file[combined]) + ")" +JoshLogger.ANSI_RESET);
                }

                default -> {
                    System.out.printf("%x", file[i]);
                    System.out.println(" (decimal: " + file[i] + ")");
                }
            }
        }
    }

    public static float toFloat( int hbits )
    {
        int mant = hbits & 0x03ff;            // 10 bits mantissa
        int exp =  hbits & 0x7c00;            // 5 bits exponent
        if( exp == 0x7c00 )                   // NaN/Inf
            exp = 0x3fc00;                    // -> NaN/Inf
        else if( exp != 0 )                   // normalized value
        {
            exp += 0x1c000;                   // exp - 15 + 127
            if( mant == 0 && exp > 0x1c400 )  // smooth transition
                return Float.intBitsToFloat( ( hbits & 0x8000 ) << 16
                        | exp << 13 | 0x3ff );
        }
        else if( mant != 0 )                  // && exp==0 -> subnormal
        {
            exp = 0x1c400;                    // make it normal
            do {
                mant <<= 1;                   // mantissa * 2
                exp -= 0x400;                 // decrease exp by 1
            } while( ( mant & 0x400 ) == 0 ); // while not normal
            mant &= 0x3ff;                    // discard subnormal bit
        }                                     // else +/-0 -> +/-0
        return Float.intBitsToFloat(          // combine all parts
                ( hbits & 0x8000 ) << 16          // sign  << ( 31 - 15 )
                        | ( exp | mant ) << 13 );         // value << ( 23 - 10 )
    }
}
