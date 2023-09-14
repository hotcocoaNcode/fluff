package co.josh;

public class JoshLogger {
    public static int logLevel = 0;
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static void log(String description){
        if (logLevel > 1){
            System.out.println(ANSI_WHITE + description + ANSI_RESET);
        }
    }

    public static void importantPurple(String description){
        System.out.println(ANSI_PURPLE + description + ANSI_RESET);
    }

    public static void importantGreen(String description){
        System.out.println(ANSI_GREEN + description + ANSI_RESET);
    }

    public static void importantNormal(String description){
        System.out.println(ANSI_RESET + description);
    }

    public static void warn(String description){
        if (logLevel > 0) {
            System.out.println(ANSI_YELLOW + "Warning: " + description + ANSI_RESET);
        }
    }

    public static void error(String description){
        System.out.println(ANSI_RED + "Error: " + description + ANSI_RESET);
        System.exit(1);
    }

    public static void syntaxError(String description){
        System.out.println(ANSI_RED + "Syntax Error: " + description + ANSI_RESET);
        System.exit(1);
    }
}
