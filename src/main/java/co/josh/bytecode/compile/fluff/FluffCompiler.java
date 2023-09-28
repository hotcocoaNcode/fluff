package co.josh.bytecode.compile.fluff;

import co.josh.JoshLogger;
import co.josh.bytecode.Instruction;
import co.josh.bytecode.compile.cex.CompilerExtension;
import co.josh.processors.expression.eval.RPN;
import co.josh.processors.expression.shuntingyard.CompilerShuntingYard;
import co.josh.processors.token.Token;
import co.josh.processors.token.TokenType;
import co.josh.processors.token.Tokenizer;
import co.josh.processors.token.v2.v2Tokenizer;

import java.io.File;
import java.util.*;

public class FluffCompiler {

    public ArrayList<Byte> header = new ArrayList<>();

    public HashMap<Instruction, Byte> bytecodeMap = new HashMap<>();

    public HashMap<String, CompilerExtension> cexList = new HashMap();

    public FluffCompiler(int compilerVersion, ArrayList<CompilerExtension> cexList){
        String enabledCExesPrompt = "Enabled CExes: ";
        int i = 0;
        for (CompilerExtension c : cexList){
            this.cexList.put(c.getStatement(), c);
            enabledCExesPrompt = enabledCExesPrompt + c.getStatement();
            if (i != cexList.size()-1){
                enabledCExesPrompt = enabledCExesPrompt + ", ";
            }
            i++;
        }
        if (enabledCExesPrompt.equals("Enabled CExes: ")) enabledCExesPrompt = "No enabled CExes";
        JoshLogger.log(enabledCExesPrompt);
        if (compilerVersion == 0){
            //Exit
            bytecodeMap.put(Instruction.exit, (byte) 0); //exit <byte>
            //Variable management
            bytecodeMap.put(Instruction.setByteConstAddress, (byte) 1); //set memory byte from const <short>
            bytecodeMap.put(Instruction.copyFromAtPointer, (byte) 2); //set memory byte from word pointer <short>
            bytecodeMap.put(Instruction.setWordConstAddress, (byte) 3); //set memory word <short>
            bytecodeMap.put(Instruction.copyFromGetPointer, (byte) 4); //honestly i forgor
            bytecodeMap.put(Instruction.constantCopyToAtPointer, (byte) 5); //honestly i forgor
            //bytecodeMap.put(Instruction.free, (byte) 6); //free variable
            //bytecodeMap.put(Instruction.malloc, (byte) 7); //runtime allocate for variable
            //Stack math
            bytecodeMap.put(Instruction.add, (byte) 10); //add on stack
            bytecodeMap.put(Instruction.subtract, (byte) 11); //subtract on stack
            bytecodeMap.put(Instruction.multiply, (byte) 12); //multiply on stack
            bytecodeMap.put(Instruction.divide, (byte) 13); //divide on stack
            bytecodeMap.put(Instruction.modulo, (byte) 14); //modulo on stack

            bytecodeMap.put(Instruction.lshift, (byte) 15); //left bit shift on stack
            bytecodeMap.put(Instruction.rshift, (byte) 16); //right bit shift on stack

            bytecodeMap.put(Instruction.greater, (byte) 17); //greaterThan on stack
            bytecodeMap.put(Instruction.equals, (byte) 18); //equals on stack
            bytecodeMap.put(Instruction.lesser, (byte) 19); //lesser on stack
            bytecodeMap.put(Instruction.nequals, (byte) 25); //not_equals on stack
            bytecodeMap.put(Instruction.greater_equals, (byte) 26); //greater_equals on stack
            bytecodeMap.put(Instruction.lesser_equals, (byte) 27); //lesser_equals on stack

            bytecodeMap.put(Instruction.not, (byte) 20); //bit not on stack
            bytecodeMap.put(Instruction.or, (byte) 22); //bit or on stack
            bytecodeMap.put(Instruction.xor, (byte) 23); //bit xor on stack
            bytecodeMap.put(Instruction.and, (byte) 24); //bit and on stack
            //Jumps
            bytecodeMap.put(Instruction.conditionalJumpRelative, (byte) 30); //Jump relatively if (stack pop == 1)
            bytecodeMap.put(Instruction.conditionalJumpExact, (byte) 31); //Jump exactly if (stack pop == 1)
            bytecodeMap.put(Instruction.conditionalNotJumpExact, (byte) 32); //Jump exactly if (stack pop != 1)
            //Stack access
            bytecodeMap.put(Instruction.push16bit, (byte) 40); //push address (and address+1)
            bytecodeMap.put(Instruction.pushByteAs16, (byte) 41); //push 8-bit number
            bytecodeMap.put(Instruction.pushConst16bit, (byte) 42); //push constant 16-bit number
            bytecodeMap.put(Instruction.popByte, (byte) 43); //pop address
            bytecodeMap.put(Instruction.pop16bit, (byte) 44); //pop address (and address+1)
            bytecodeMap.put(Instruction.popToOutput, (byte) 45); //pop output char to stdout
            bytecodeMap.put(Instruction.pushInputChar, (byte) 46); //push input char to stack
            //Definitions
            //bytecodeMap.put(Instruction.newtype, (byte) 51); //New type with custom byte width
            //bytecodeMap.put(Instruction.newmacro, (byte) 52); //New macro
            //bytecodeMap.put(Instruction.macroexec, (byte) 53); //Execute existing macro

            // SPECIAL THINGIES
            // Invoke a new interpreter mode.
            // The string it parses terminates with \0 and the interpreter will quit if it is not supported
            bytecodeMap.put(Instruction.invokeInterpreterMode, (byte) 60);
            compileTimeMemory = new MemorySpace(16128);

            //Set up file header
            //Signature of FLOOFI (F100F1)
            header.add((byte) -15); //F1
            header.add((byte) 0); //  00
            header.add((byte) -15); //F1
            header.add((byte) 0); //Header Length
            header.add((byte) 0); //Version Major
            header.add((byte) 5); //Version Minor
            header.add((byte) 1); //Revision
            header.add((byte) 1); //Instruction set version

            ArrayList<String> headerAdds = new ArrayList<>();
            headerAdds.add("cat_cafe");
            for (String s : headerAdds){
                for (char c : s.toCharArray()){
                    header.add((byte) c);
                }
                header.add((byte) '_');
            }


            header.set(3, (byte) header.size());
        } else {
            JoshLogger.error("Bytecode revision " + compilerVersion + " does not exist!\n" +
                                        "Are you sure you have given the compiler the correct arguments?");
        }
    }

    ArrayList<Byte> bytes = new ArrayList<>();
    ArrayList<Byte> debugSymbols = new ArrayList<>();

    HashMap<String, MacroInfo> macros = new HashMap<>();
    MemorySpace compileTimeMemory;

    public byte[] compile(ArrayList<Token> tokens, boolean isMain){
        int scopeCounter = 0;
        Stack<Integer> loops = new Stack<>();
        Stack<Integer> ifStatements = new Stack<>();
        Stack<Integer> ifStatementScopes = new Stack<>();
        for (int i = 0; i < tokens.size(); i++){
            switch (tokens.get(i).getTokenType()) {
                case include_file -> {
                    try {
                        i++;
                        String fileName = tokens.get(i).getValue().toString();
                        StringBuilder s = new StringBuilder();
                        Scanner scan = new Scanner(new File(fileName));
                        while (scan.hasNextLine()){
                            s.append(scan.nextLine()).append("\n");
                        }
                        Tokenizer tokenizer = new v2Tokenizer();
                        tokens.addAll(i+1, tokenizer.tokenize(s.toString(), fileName));
                    } catch (Exception e){
                        throw new RuntimeException(e.toString());
                    }
                }

                case macro_def -> {
                    if (scopeCounter != 0) JoshLogger.syntaxError("Non-include macro def must be at scope 0!", tokens.get(i).getLine());
                    ArrayList<Token> macroTokens = new ArrayList<>();
                    while (tokens.get(i-1).getTokenType() != TokenType.scope_up){
                        macroTokens.add(tokens.get(i));
                        if (tokens.get(i).getTokenType() == TokenType.scope_up){
                            scopeCounter++;
                        } else if (tokens.get(i).getTokenType() == TokenType.scope_down) {
                            scopeCounter--;
                        }
                        i++;
                    }
                    while (scopeCounter != 0) {
                        macroTokens.add(tokens.get(i));
                        if (tokens.get(i).getTokenType() == TokenType.scope_up){
                            scopeCounter++;
                        } else if (tokens.get(i).getTokenType() == TokenType.scope_down) {
                            scopeCounter--;
                        }
                        i++;
                    }
                    i--;
                    MacroInfo macro = new MacroInfo(macroTokens);
                    macros.put(macro.name, macro);
                }

                case exit -> {
                    bytes.add(bytecodeMap.get(Instruction.exit));
                    i++;
                    if (tokens.get(i).getTokenType() == TokenType.int_literal){
                        String sizeStr = tokens.get(i).getValue().toString();
                        byte byteval = Byte.parseByte(sizeStr);
                        bytes.add(byteval);
                    } else {
                        JoshLogger.syntaxError("Exit code must be constant byte!", tokens.get(i-1).getLine());
                    }
                }

                case free_variable -> {
                    i++;
                    if (tokens.get(i).getTokenType() == TokenType.name){
                        String name = tokens.get(i).getValue().toString();
                        if (compileTimeMemory.memoryMap.containsKey(name)){
                            compileTimeMemory.free(name);
                        } else {
                            JoshLogger.syntaxError("Variable \""+name+"\" does not exist!", tokens.get(i).getLine());
                        }
                    } else {
                        JoshLogger.syntaxError("Free requires variable name after usage!", tokens.get(i).getLine());
                    }
                }

                case static_global_allocate -> {
                    i++;
                    if (tokens.get(i).getTokenType() != TokenType.int_literal
                            || tokens.get(i+1).getTokenType() != TokenType.name
                            || tokens.get(i+2).getTokenType() != TokenType.semi
                            || !compileTimeMemory.variableTypes.get((String)tokens.get(i+1).getValue()).equals("int16")) {
                        JoshLogger.syntaxError("Static memory allocation must always be followed by an integer and a name referring to an integer!", tokens.get(i).getLine());
                    }
                    short address = compileTimeMemory.mapAlloc((short)tokens.get(i).getValue());
                    compileTimeMemory.blockOff(address, (short)tokens.get(i).getValue());
                    i++;
                    bytes.add(bytecodeMap.get(Instruction.setWordConstAddress));
                    Byte[] value = splitShort(compileTimeMemory.memoryMap.get(tokens.get(i).getValue().toString()));
                    bytes.add(value[0]);
                    bytes.add(value[1]);
                    value = splitShort(address);
                    bytes.add(value[0]);
                    bytes.add(value[1]);
                }

                case static_scoped_allocate -> {
                    i++;
                    if (tokens.get(i).getTokenType() != TokenType.int_literal
                            || tokens.get(i+1).getTokenType() != TokenType.name
                            || tokens.get(i+2).getTokenType() != TokenType.semi
                            || !compileTimeMemory.variableTypes.get((String)tokens.get(i+1).getValue()).equals("int16")) {
                        JoshLogger.syntaxError("Static memory allocation must always be followed by an integer and a name referring to an integer!", tokens.get(i).getLine());
                    }
                    short address = compileTimeMemory.mapAlloc((short)tokens.get(i).getValue());
                    compileTimeMemory.blockOffScoped(address, (short)tokens.get(i).getValue(), scopeCounter);
                    i++;
                    bytes.add(bytecodeMap.get(Instruction.setWordConstAddress));
                    Byte[] value = splitShort(compileTimeMemory.memoryMap.get(tokens.get(i).getValue().toString()));
                    bytes.add(value[0]);
                    bytes.add(value[1]);
                    value = splitShort(address);
                    bytes.add(value[0]);
                    bytes.add(value[1]);
                }

                case name -> {
                    String name = tokens.get(i).getValue().toString();
                    i++;
                    if (tokens.get(i).getTokenType() == TokenType.quick_assign ||
                            (tokens.get(i).getTokenType() == TokenType.equals
                                    && tokens.get(i+1).getTokenType() == TokenType.int_literal
                                    && tokens.get(i+2).getTokenType() == TokenType.semi)){ //Singular (no expression)
                        i++;
                        if (!compileTimeMemory.memoryMap.containsKey(name)) JoshLogger.syntaxError("Variable \""+name+"\" does not exist!", tokens.get(i).getLine());
                        Byte[] indexOfVariable = splitShort(compileTimeMemory.memoryMap.get(name));
                        if (tokens.get(i).getTokenType() == TokenType.name){
                            String thisType = compileTimeMemory.variableTypes.get(tokens.get(i).getValue().toString());
                            bytes.add(bytecodeMap.get(Instruction.setByteConstAddress));
                            bytes.add(indexOfVariable[0]);
                            bytes.add(indexOfVariable[1]);
                            i++;
                            if ((tokens.get(i).getTokenType() == TokenType.name)
                                    && (compileTimeMemory.variableTypes.get(tokens.get(i).getValue().toString()).equals(thisType))){
                                Byte[] indexOfVar2 = splitShort(compileTimeMemory.memoryMap.get(tokens.get(i).getValue().toString()));
                                bytes.add(indexOfVar2[0]);
                                bytes.add(indexOfVar2[1]);
                            }
                        } else {
                            if (tokens.get(i).getTokenType() == TokenType.int_literal) {
                                if (compileTimeMemory.variableTypes.get(name).equals("int16")) {
                                    bytes.add(bytecodeMap.get(Instruction.setWordConstAddress));
                                    bytes.add(indexOfVariable[0]);
                                    bytes.add(indexOfVariable[1]);
                                    i++;
                                    String sizeStr = tokens.get(i).getValue().toString();
                                    Byte[] value = splitShort(Short.parseShort(sizeStr));
                                    bytes.add(value[0]);
                                    bytes.add(value[1]);
                                } else if (compileTimeMemory.variableTypes.get(name).equals("int8")) {
                                    bytes.add(bytecodeMap.get(Instruction.setByteConstAddress));
                                    bytes.add(indexOfVariable[0]);
                                    bytes.add(indexOfVariable[1]);
                                    i++;
                                    String sizeStr = tokens.get(i).getValue().toString();
                                    try {
                                        byte byteval = Byte.parseByte(sizeStr);
                                        bytes.add(byteval);
                                    } catch (Exception e) {
                                        JoshLogger.syntaxError("Byte values must be between -127 and 127!", tokens.get(i).getLine());
                                    }
                                }
                            }
                        }
                    } else if (tokens.get(i).getTokenType() == TokenType.equals){
                        if (!compileTimeMemory.memoryMap.containsKey(name)) JoshLogger.syntaxError("Variable \""+name+"\" does not exist!", tokens.get(i).getLine());
                        i = evaluateIntExpr(tokens, i);
                        Byte[] value = splitShort(compileTimeMemory.memoryMap.get(name));
                        if (compileTimeMemory.variableSizes.get(name) > 1){
                            bytes.add(bytecodeMap.get(Instruction.pop16bit));
                            bytes.add(value[0]);
                            bytes.add(value[1]);
                        } else {
                            bytes.add(bytecodeMap.get(Instruction.popByte));
                            bytes.add(value[0]);
                            bytes.add(value[1]);
                        }
                    } else if (tokens.get(i).getTokenType() == TokenType.get_pointer) {
                        i++;
                        if (!compileTimeMemory.variableTypes.get(name).equals("int16")) JoshLogger.syntaxError("Pointers must be a short value!", tokens.get(i).getLine());
                        if (tokens.get(i).getTokenType() == TokenType.name) {
                            compileTimeMemory.pointerTypeMap.put(name, compileTimeMemory.variableTypes.get(tokens.get(i).getValue().toString()));
                            bytes.add(bytecodeMap.get(Instruction.setWordConstAddress));
                            Byte[] index = splitShort(compileTimeMemory.memoryMap.get(name));
                            bytes.add(index[0]);
                            bytes.add(index[1]);
                            index = splitShort(compileTimeMemory.memoryMap.get(tokens.get(i).getValue().toString()));
                            bytes.add(index[0]);
                            bytes.add(index[1]);
                        } else {
                            JoshLogger.syntaxError("Pointer declaration must be followed by a name!", tokens.get(i).getLine());
                        }
                    } else if (tokens.get(i).getTokenType() == TokenType.set_val_at_pointer) {
                        i++;
                        if (!compileTimeMemory.variableTypes.get(name).equals("int16")) JoshLogger.syntaxError("Pointers must be a short value!", tokens.get(i).getLine());
                        if (tokens.get(i).getTokenType() == TokenType.name) {
                            for (short n = 0; n < compileTimeMemory.variableSizes.get(tokens.get(i).getValue().toString()); n++){
                                bytes.add(bytecodeMap.get(Instruction.copyFromAtPointer));
                                //Address of pointer
                                Byte[] index = splitShort(compileTimeMemory.memoryMap.get(name));
                                bytes.add(index[0]);
                                bytes.add(index[1]);
                                //Address of value we set address at pointer to
                                index = splitShort(compileTimeMemory.memoryMap.get(tokens.get(i).getValue().toString()));
                                bytes.add(index[0]);
                                bytes.add(index[1]);
                                index = splitShort(n);
                                bytes.add(index[0]);
                                bytes.add(index[1]);
                            }
                        } else if (tokens.get(i).getTokenType() == TokenType.int_literal) {
                            //Is Byte
                            if (tokens.get(i).getValue() instanceof Byte){
                                bytes.add(bytecodeMap.get(Instruction.constantCopyToAtPointer));
                                //pointer to address
                                Byte[] index = splitShort(compileTimeMemory.memoryMap.get(name));
                                bytes.add(index[0]);
                                bytes.add(index[1]);
                                //const
                                bytes.add((byte) tokens.get(i).getValue());
                                //add to address
                                bytes.add((byte) 0);
                                bytes.add((byte) 0);
                            }
                            else {
                                bytes.add(bytecodeMap.get(Instruction.constantCopyToAtPointer));
                                //pointer to address
                                Byte[] index = splitShort(compileTimeMemory.memoryMap.get(name));
                                bytes.add(index[0]);
                                bytes.add(index[1]);
                                Byte[] shortVal = splitShort((short) tokens.get(i).getValue());
                                //const
                                bytes.add(shortVal[0]);
                                //add to address
                                bytes.add((byte) 0);
                                bytes.add((byte) 0);

                                //set lower byte
                                bytes.add(bytecodeMap.get(Instruction.constantCopyToAtPointer));
                                bytes.add(index[0]);
                                bytes.add(index[1]);
                                //const
                                bytes.add(shortVal[1]);
                                //add to address
                                bytes.add((byte) 1);
                                bytes.add((byte) 0);
                            }
                        }
                        else if (tokens.get(i).getTokenType() == TokenType.string_val){
                            char[] charArr = tokens.get(i).getValue().toString().toCharArray();
                            short n = 0;
                            for (; n < charArr.length; n++){
                                bytes.add(bytecodeMap.get(Instruction.constantCopyToAtPointer));
                                //pointer to address
                                Byte[] index = splitShort(compileTimeMemory.memoryMap.get(name));
                                bytes.add(index[0]);
                                bytes.add(index[1]);
                                //const
                                bytes.add((byte) charArr[n]);
                                //add to address
                                index = splitShort(n);
                                bytes.add(index[0]);
                                bytes.add(index[1]);
                            }
                            bytes.add(bytecodeMap.get(Instruction.constantCopyToAtPointer));
                            //pointer to grab address from
                            Byte[] index = splitShort(compileTimeMemory.memoryMap.get(name));
                            bytes.add(index[0]);
                            bytes.add(index[1]);
                            //stop
                            bytes.add((byte) 0);
                            index = splitShort(n);
                            bytes.add(index[0]);
                            bytes.add(index[1]);
                        } else {
                            JoshLogger.syntaxError("Pointer declaration must be followed by a name!", tokens.get(i+1).getLine());
                        }
                    } else if (tokens.get(i).getTokenType() == TokenType.get_val_at_pointer) {
                        i++;
                        if (!compileTimeMemory.variableTypes.get(name).equals("int16")) JoshLogger.syntaxError("Pointers must be a short value!", tokens.get(i).getLine());
                        if (tokens.get(i).getTokenType() == TokenType.name) {
                            for (short n = 0; n < compileTimeMemory.variableSizes.get(tokens.get(i).getValue().toString()); n++){
                                // Absolutely fucking horrid pointer implementation
                                // This goes for get at, set at, and get pointer
                                bytes.add(bytecodeMap.get(Instruction.copyFromGetPointer));
                                //pointer to grab address from
                                Byte[] index = splitShort((compileTimeMemory.memoryMap.get(name)));
                                bytes.add(index[0]);
                                bytes.add(index[1]);
                                //address
                                index = splitShort((compileTimeMemory.memoryMap.get(tokens.get(i).getValue().toString())));
                                bytes.add(index[0]);
                                bytes.add(index[1]);
                                index = splitShort(n);
                                bytes.add(index[0]);
                                bytes.add(index[1]);
                            }
                        } else {
                            JoshLogger.syntaxError("Pointer declaration must be followed by a name!", tokens.get(i+1).getLine());
                        }
                    } else if (macros.containsKey(name)){
                        JoshLogger.log("Inserting macro " + name + ".");
                        MacroInfo macro = macros.get(name);
                        HashMap<String, String> nameMatch = new HashMap<>();
                        int a = 0;
                        //Argument processing
                        while (tokens.get(i).getTokenType() != TokenType.semi) {
                            if (tokens.get(i).getTokenType() != TokenType.name)
                                JoshLogger.syntaxError("Unexpected token \"" + tokens.get(i).getTokenType() + "\" in macro usage!", tokens.get(i).getLine());
                            if (!compileTimeMemory.variableTypes.get(tokens.get(i).getValue().toString()).equals(macro.argTypes.get(a))){
                                JoshLogger.syntaxError("Non-matching types \"" + compileTimeMemory.variableTypes.get(tokens.get(i).getValue().toString()) + "\" and \"" + macro.argTypes.get(a) + "\" in macro usage!", tokens.get(i).getLine());
                            }
                            nameMatch.put(macro.argNames.get(a), tokens.get(i).getValue().toString());
                            a++;
                            i++;
                        }
                        List<Token> macroTokens = macro.use();
                        //Scope the macro.
                        for (int n = 0; n < macroTokens.size(); n++){
                            if (macroTokens.get(n).getTokenType() == TokenType.name
                            && nameMatch.containsKey(macroTokens.get(n).getValue().toString())){
                                macroTokens.get(n).value = nameMatch.get(macroTokens.get(n).getValue().toString());
                            }
                        }
                        tokens.addAll(i+1, macroTokens);
                        JoshLogger.log("Macro " + name + " successfully inserted and var-subbed.");
                    } else if (cexList.containsKey(name)) {
                        int temp = JoshLogger.logLevel;
                        JoshLogger.logLevel = 2;
                        JoshLogger.log("Using CEx " + name);
                        JoshLogger.logLevel = temp;
                        CompilerExtension extension = cexList.get(name);
                        ArrayList<Token> expression = new ArrayList<>();
                        while (tokens.get(i).getTokenType() != TokenType.semi){
                            expression.add(tokens.get(i));
                            i++;
                        }
                        bytes.addAll(extension.compile(expression, bytecodeMap));
                    } else {
                        JoshLogger.syntaxError("Error parsing name \"" + name + "\"! (Unexpected token \"" + tokens.get(i).getTokenType() + "\")", tokens.get(i).getLine());
                    }
                }

                case scope_up -> scopeCounter++;


                case loop -> loops.push(bytes.size());


                case loopback_if -> {
                    i = evaluateIntExpr(tokens, i);
                    if (loops.isEmpty()){
                        JoshLogger.syntaxError("Cannot use lif statement with no loop point!", tokens.get(i).getLine());
                    }
                    int a = loops.pop();

                    //if (false){ //testing reader
                    if (Math.abs(a - (bytes.size())) < 127){
                        bytes.add(bytecodeMap.get(Instruction.conditionalJumpRelative));
                        int bcl = (bytes.size());
                        bytes.add((byte) (a - bcl));
                    } else {
                        bytes.add(bytecodeMap.get(Instruction.conditionalJumpExact));
                        byte[] go_to = splitInt(a);
                        bytes.add(go_to[0]);
                        bytes.add(go_to[1]);
                        bytes.add(go_to[2]);
                        bytes.add(go_to[3]);
                    }
                }

                case _if -> {
                    i++;
                    ArrayList<Token> expression = new ArrayList<>();
                    while (tokens.get(i).getTokenType() != TokenType.scope_up){
                        expression.add(tokens.get(i));
                        i++;
                    }
                    i--; //counteract i++ to go back to scope_up
                    if (expression.size() == 4 //nothing but one var
                            && (expression.get(2).getTokenType() == TokenType.int_literal //third is int?
                            && ((int)expression.get(2).getValue() == 1))) //third is true?
                    {
                        continue;
                    }
                    expression = CompilerShuntingYard.infixToRpn(expression);
                    bytes.addAll(RPN.compRPN_ints(expression, bytecodeMap, compileTimeMemory));
                    bytes.add(bytecodeMap.get(Instruction.conditionalNotJumpExact));
                    ifStatements.push(bytes.size());
                    ifStatementScopes.push(scopeCounter);
                    bytes.add((byte) 0);
                    bytes.add((byte) 0);
                    bytes.add((byte) 0);
                    bytes.add((byte) 0);
                }

                case scope_down -> {
                    scopeCounter--;
                    if (!ifStatementScopes.isEmpty() && ifStatementScopes.peek() == scopeCounter){
                        int loc = ifStatements.pop();
                        ifStatementScopes.pop();
                        byte[] go_to = splitInt(bytes.size()); //Instruction after scope down
                        bytes.set(loc  , go_to[0]);
                        bytes.set(loc+1, go_to[1]);
                        bytes.set(loc+2, go_to[2]);
                        bytes.set(loc+3, go_to[3]);
                    }
                    LinkedList<String> temp = new LinkedList<>(compileTimeMemory.variableScopes.keySet());
                    for (String s : temp){
                        if (compileTimeMemory.variableScopes.get(s) > scopeCounter){
                            //Wipe variable from all references
                            compileTimeMemory.free(s);
                        }
                    }
                }

                case character_out -> {
                    i = evaluateIntExpr(tokens, i);
                    bytes.add(bytecodeMap.get(Instruction.popToOutput));
                }

                case character_input -> {
                    i++;
                    if (tokens.get(i).getTokenType() != TokenType.name) JoshLogger.syntaxError("Name must be after input!", tokens.get(i).getLine());
                    bytes.add(bytecodeMap.get(Instruction.pushInputChar));
                    bytes.add(bytecodeMap.get(Instruction.popByte));
                    Byte[] value = splitShort(compileTimeMemory.memoryMap.get(tokens.get(i).getValue().toString()));
                    bytes.add(value[0]);
                    bytes.add(value[1]);
                }


                case int16_variable -> {
                    bytes.add(bytecodeMap.get(Instruction.setWordConstAddress));
                    int line = tokens.get(i).getLine();
                    i++;
                    if (tokens.get(i).getTokenType() == TokenType.name && !compileTimeMemory.variableTypes.containsKey(tokens.get(i).getValue().toString())){
                        String name = tokens.get(i).getValue().toString();
                        i++;
                        if (tokens.get(i).getTokenType() == TokenType.quick_assign ||
                                (tokens.get(i).getTokenType() == TokenType.equals
                                        && tokens.get(i+1).getTokenType() == TokenType.int_literal
                                        && tokens.get(i+2).getTokenType() == TokenType.semi)){
                            i++;
                            if (tokens.get(i).getTokenType() == TokenType.int_literal) {
                                Short address = compileTimeMemory.mapAlloc(2);
                                JoshLogger.log("New short (size 2) \"" + name + "\", storing at " + address);
                                compileTimeMemory.addNew(name, "int16", address, scopeCounter, 2);
                                Byte[] value = splitShort(address);
                                bytes.add(value[0]);
                                bytes.add(value[1]);
                                String sizeStr = tokens.get(i).getValue().toString();
                                value = splitShort(Short.parseShort(sizeStr));
                                bytes.add(value[0]);
                                bytes.add(value[1]);
                            } else {
                                JoshLogger.syntaxError("Cannot assign non-short to short!", tokens.get(i).getLine());
                            }
                        }
                        else if (tokens.get(i).getTokenType() == TokenType.equals) {
                            i++;
                            ArrayList<Token> expression = new ArrayList<>();
                            while (tokens.get(i).getTokenType() != TokenType.semi){
                                expression.add(tokens.get(i));
                                i++;
                            }
                            Short address = compileTimeMemory.mapAlloc(2);
                            JoshLogger.log("New short (size 2) \"" + name + "\", storing at " + address);
                            compileTimeMemory.addNew(name, "int16", address, scopeCounter, 2);
                            Byte[] value = splitShort(address);
                            bytes.add(value[0]);
                            bytes.add(value[1]);
                            bytes.add((byte) 0);
                            bytes.add((byte) 0);
                            //Expression Eval
                            expression = CompilerShuntingYard.infixToRpn(expression);
                            bytes.addAll(RPN.compRPN_ints(expression, bytecodeMap, compileTimeMemory));
                            bytes.add(bytecodeMap.get(Instruction.pop16bit));
                            bytes.add(value[0]);
                            bytes.add(value[1]);
                        } else {
                            JoshLogger.syntaxError("Variable declaration must always assign a value!", line);
                        }
                    } else {
                        JoshLogger.syntaxError("Integer declaration must be followed by an unused name!", line);
                    }
                }

                case int8_variable -> {
                    bytes.add(bytecodeMap.get(Instruction.setByteConstAddress));
                    i++;
                    if (tokens.get(i).getTokenType() == TokenType.name && !compileTimeMemory.variableTypes.containsKey(tokens.get(i).getValue().toString())){
                        String name = tokens.get(i).getValue().toString();
                        i++;
                        if (tokens.get(i).getTokenType() == TokenType.quick_assign ||
                                (tokens.get(i).getTokenType() == TokenType.equals
                                        && tokens.get(i+1).getTokenType() == TokenType.int_literal
                                        && tokens.get(i+2).getTokenType() == TokenType.semi)){
                            i++;
                            if (tokens.get(i).getTokenType() == TokenType.int_literal) {
                                String sizeStr = tokens.get(i).getValue().toString();
                                Short address = compileTimeMemory.mapAlloc(1);
                                JoshLogger.log("New byte (size 1) \"" + name + "\", storing at " + address);
                                compileTimeMemory.addNew(name, "int8", address, scopeCounter, 1);
                                Byte[] value = splitShort(address);
                                bytes.add(value[0]);
                                bytes.add(value[1]);
                                try {
                                    byte byteval = Byte.parseByte(sizeStr);
                                    bytes.add(byteval);
                                } catch (Exception e) {
                                    JoshLogger.syntaxError("Byte values must be between -127 and 127!", tokens.get(i).getLine());
                                }
                            } else {
                                JoshLogger.syntaxError("Cannot assign non-integer to an integer type!", tokens.get(i).getLine());
                            }
                        }
                        else if (tokens.get(i).getTokenType() == TokenType.equals) {
                            i++;
                            ArrayList<Token> expression = new ArrayList<>();
                            while (tokens.get(i).getTokenType() != TokenType.semi){
                                expression.add(tokens.get(i));
                                i++;
                            }
                            {
                                //Address and allocation
                                Short address = compileTimeMemory.mapAlloc(1);
                                JoshLogger.log("New byte (size 1) \"" + name + "\", storing at " + address);
                                compileTimeMemory.addNew(name, "int8", address, scopeCounter, 1);
                                Byte[] value = splitShort(address);
                                bytes.add(value[0]);
                                bytes.add(value[1]);
                                //Set zero
                                bytes.add((byte) 0);
                                //Expression Eval
                                expression = CompilerShuntingYard.infixToRpn(expression);
                                bytes.addAll(RPN.compRPN_ints(expression, bytecodeMap, compileTimeMemory));
                                bytes.add(bytecodeMap.get(Instruction.popByte));
                                bytes.add(value[0]);
                                bytes.add(value[1]);
                            }
                        } else {
                            JoshLogger.syntaxError("Variable declaration must always assign a value!", tokens.get(i).getLine());
                        }
                    } else {
                        JoshLogger.syntaxError("Integer declaration must be followed by an unused name!", tokens.get(i).getLine());
                    }
                }
            }
        }
        bytes.addAll(0, header);
        byte[] returnVal = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++){
            returnVal[i] = bytes.get(i);
        }
        return returnVal;
    }

    private int evaluateIntExpr(ArrayList<Token> tokens, int i) {
        //Thanks, IntelliJ
        i++;
        ArrayList<Token> expression = new ArrayList<>();
        while (tokens.get(i).getTokenType() != TokenType.semi){
            expression.add(tokens.get(i));
            i++;
        }
        expression = CompilerShuntingYard.infixToRpn(expression);
        bytes.addAll(RPN.compRPN_ints(expression, bytecodeMap, compileTimeMemory));
        return i;
    }
    public static byte[] splitInt(int value) {
        return new byte[] {
                (byte)value,
                (byte)(value >>> 8),
                (byte)(value >>> 16),
                (byte)(value >>> 24),
                };
    }

    public static Byte[] splitShort(Short s){
        Byte[] ret = new Byte[2];
        ret[0] = (byte)(s & 0xff);
        ret[1] = (byte)((s >> 8) & 0xff);
        return ret;
    }
}