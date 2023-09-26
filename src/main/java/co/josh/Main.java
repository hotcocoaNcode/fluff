package co.josh;

import co.josh.bytecode.compile.brainfuck.BrainfuckCompilerExtension;
import co.josh.bytecode.compile.cex.CompilerExtension;
import co.josh.bytecode.compile.fluff.FluffCompiler;
import co.josh.bytecode.local_interpret.BytecodeInterpreter;
import co.josh.bytecode.debug.BytecodeReader;
import co.josh.processors.token.Token;
import co.josh.processors.token.v1.v1Tokenizer;
import co.josh.processors.token.v2.v2Tokenizer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0){
            JoshLogger.error("No file provided.");
        }

        String fileName = args[args.length-1];

        boolean interpreterV1 = Arrays.stream(args).toList().contains("-Iv1");

        boolean readBytecode = Arrays.stream(args).toList().contains("-Mreverse");

        boolean interpretMode = Arrays.stream(args).toList().contains("-Minterp");

        boolean verboseLogger = Arrays.stream(args).toList().contains("-Lverbose");

        boolean warningsLogger = Arrays.stream(args).toList().contains("-Lwarn");

        if (warningsLogger) JoshLogger.logLevel = 1;
        if (verboseLogger) JoshLogger.logLevel = 2;

        JoshLogger.log("JavaFluff, version 1.0");
        JoshLogger.log("Arguments: " + Arrays.toString(args));

        int compilerVersion = 0;

        File f = new File(fileName);

        StringBuilder built = new StringBuilder();
        try {
            Scanner scanner = new Scanner(f);
            while (scanner.hasNextLine()){
                built.append("\n").append(scanner.nextLine());
            }
        } catch (IOException e){
            JoshLogger.error("IOException on file read, are you sure it exists?");
        }

        if (interpreterV1){
            JoshLogger.warn("Using interpreter V1, not recommended!");
            JoshLogger.log("Got file data");

            v1Tokenizer tokenizer = new v1Tokenizer();
            ArrayList<Token> tokens = tokenizer.tokenize(built.toString(), null);

            JoshLogger.log("Tokenized");
            co.josh.interpret.v1.Interpreter.interpret(tokens);
            return;
        }
        ArrayList<CompilerExtension> cexes = new ArrayList<>();
        cexes.add(new BrainfuckCompilerExtension());
        FluffCompiler bytecodeCompiler = new FluffCompiler(compilerVersion, cexes);

        if (interpretMode){
            try{
                byte[] a = Files.readAllBytes(f.toPath());
                BytecodeInterpreter.interpretBytecode(a, bytecodeCompiler, 65023);
            } catch (IOException e){
                throw new RuntimeException("IOException");
            }
            return;
        }

        if (readBytecode){
            try{
                byte[] a = Files.readAllBytes(f.toPath());
                BytecodeReader.read(a, bytecodeCompiler.bytecodeMap);
            } catch (IOException e){
                throw new RuntimeException("IOException");
            }
            return;
        }

        JoshLogger.importantPurple("Compiling to bytecode...");
        v2Tokenizer tokenizer = new v2Tokenizer();
        ArrayList<Token> tokens = tokenizer.tokenize(built.toString(), fileName);
        JoshLogger.log("Bytecode Compiler instantiated with version " + compilerVersion);
        byte[] bytecode = bytecodeCompiler.compile(tokens, true);
        JoshLogger.importantGreen("Done compiling!");
        String outputFileName = fileName.substring(0, fileName.indexOf('.')) + ".fb";
        JoshLogger.importantGreen("Will write to " + outputFileName);
        File outputFile = new File(outputFileName);
        try {
            boolean a = outputFile.createNewFile();
        } catch (IOException e){
            JoshLogger.error("Could not write to file: " + fileName.substring(0, fileName.indexOf('.')) + ".fb");
        }
        try (FileOutputStream fos = new FileOutputStream(outputFileName)) {
            fos.write(bytecode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}