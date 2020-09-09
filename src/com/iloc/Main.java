package com.iloc;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Main {
    private static boolean hadError = false;
    private static boolean sFlag = false;
    private static boolean pFlag = false;
    private static boolean rFlag = false;

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            displayHelp();
            System.exit(64);
        } else if (args.length == 1 && !args[args.length - 1].matches("-[rsp]")) {
            pFlag = true;
            runFile(args[0]);
        } else {
            List<String> cmdline = Arrays.asList(args);
            if (cmdline.contains("-h") || args[args.length - 1].matches("-[rsp]")) {
                displayHelp();
                System.exit(69);
            } else if (cmdline.contains("-r")) rFlag = true;
            else if (cmdline.contains("-p")) pFlag = true;
            else if (cmdline.contains("-s")) sFlag = true;
            runFile(args[args.length - 1]);
        }
    }

    private static void displayHelp() {
        String x = "\nLab 1: Compiler for ILOC Instruction Set\n" +
                "Usage: ./341fe [flags] filename\n" +
                "Optional Flags:\n" +
                "-h = Shows a list of valid command-line arguments for the 341fe\n" +
                "-s = Reads the file passed and returns tokens from the scanner\n" +
                "-p = Reads the file, scans and parses it and   builds the intermediate representation and then reports success or all errors it encountered\n" +
                "-r = Reads the file, scans and parses it and display the intermediate representation  in readable form\n";
        System.out.println(x);
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) System.exit(69);
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Operators> ops = parser.parse();

        if(sFlag) {
            for(Token token: tokens) {
                System.out.println(token);
            }
        } else if(pFlag) {
            if(parser.hasError()) System.out.println("File Parsing Failed");
            else System.out.println("Parsed Successfully " + ops.size() + " ILOC operations");
        } else if(rFlag){
            if(parser.hasError()) System.out.println("File Parsing Failed");
            else {
                System.out.println("Parsing Successful " + ops.size() + " ILOC operations");
                for (Operators op : ops) {
                    System.out.println(new IRPrinter().print(op));
                }
            }
        }
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    private static void report(int line, String where, String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}