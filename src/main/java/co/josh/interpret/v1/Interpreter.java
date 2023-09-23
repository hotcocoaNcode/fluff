package co.josh.interpret.v1;

import co.josh.processors.token.Token;
import co.josh.processors.token.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

import static co.josh.processors.expression.Expression.evalExpression;

public class Interpreter {

    public static int scopeCounter = 0;

    public static HashMap<String, Object> runtimeVars = new HashMap<>();

    public static HashMap<String, Integer> functionIndices = new HashMap<>();

    public static HashMap<String, HashMap<String, Object>> functionArguments = new HashMap<>();

    public static Stack<ArrayList<String>> variablesThisScope = new Stack<>();

    public static boolean foundStart = false;

    public static int constructorType = -1;

    public static Stack<Boolean> skip = new Stack<>();

    public static Stack<Integer> loops = new Stack<>();

    public static String workingOnConstructorName = "";

    public static HashMap<String, Object> constructorArguments = new HashMap<>();

    public static ArrayList<Token> flattenVars(ArrayList<Token> tokens){
        ArrayList<Token> out = new ArrayList<>();
        for (Token t : tokens){
            if (t.getTokenType() == TokenType.name){
                if (runtimeVars.containsKey((String)t.getValue())){
                    Object variableData = runtimeVars.get((String)t.getValue());
                    if (variableData instanceof String){
                        out.add(new Token(TokenType.string_val, variableData));
                    } else if (variableData instanceof Integer){
                        out.add(new Token(TokenType.int_literal, variableData));
                    } else if (variableData instanceof Boolean) {
                        out.add(new Token(TokenType.boolean_val, variableData));
                    }
                }
            } else {
                out.add(t);
            }
        }
        return out;
    }

    public static void interpret(ArrayList<Token> tokens){
        Scanner uin = new Scanner(System.in);
        variablesThisScope.push(new ArrayList<>());
        skip.push(false);
        loops.push(-1);
        for (int i = 0; i < tokens.size(); i++){
            if (tokens.get(i).getTokenType() == TokenType.scope_up){
                scopeCounter++;
                variablesThisScope.push(new ArrayList<>());
                skip.push(false);
                if (tokens.get(i-1).getTokenType() == TokenType.loop){
                    loops.push(i);
                } else {
                    loops.push(-1);
                }
                if (constructorType == 0){
                    functionIndices.put(workingOnConstructorName, i);
                    functionArguments.put(workingOnConstructorName, constructorArguments);
                    constructorType = -1;
                    constructorArguments = new HashMap<>();
                }
                continue;
            }
            if (tokens.get(i).getTokenType() == TokenType.scope_down){
                scopeCounter--;
                for (String toRemove : variablesThisScope.pop()) runtimeVars.remove(toRemove);
                skip.pop();
                loops.pop();
                continue;
            }
            boolean skipCheck = false;
            for (boolean b : skip){
                skipCheck = skipCheck || b;
            }
            if (!skipCheck){
                if (constructorType == -1){
                    //Is Function
                    if (tokens.get(i).getTokenType() == TokenType.function_old){
                        constructorType = 0;
                    }

                    //Is Exit
                    else if (tokens.get(i).getTokenType() == TokenType.exit){
                        i++;
                        if (tokens.get(i).getTokenType() == TokenType.int_literal){
                            System.out.println("Program exited with code " + (int)tokens.get(i).getValue());
                            break;
                        } else if (tokens.get(i).getTokenType() == TokenType.name){
                            if (runtimeVars.containsKey((String)tokens.get(i).getValue())){
                                System.out.println("Program exited with code " + (int)runtimeVars.get((String)tokens.get(i).getValue()));
                                break;
                            } else {
                                System.out.println("Variable \"" + tokens.get(i).getValue() + "\" does not exist!");
                                System.exit(1);
                            }
                        } else {
                            System.out.println("Exit called with a non-int following!");
                            System.exit(1);
                        }
                    }

                    //Is Integer Variable
                    else if (tokens.get(i).getTokenType() == TokenType.int_var){
                        i++;
                        if (tokens.get(i).getTokenType() == TokenType.name){
                            String name = (String)tokens.get(i).getValue();
                            i++;
                            if (tokens.get(i).getTokenType() != TokenType.equals){
                                System.out.println("Unassigned variable creation!");
                                System.exit(1);
                            }
                            ArrayList<Token> expression = new ArrayList<>();
                            i++;
                            while (tokens.get(i).getTokenType() != TokenType.semi) {
                                expression.add(tokens.get(i));
                                i++;
                            }
                            variablesThisScope.peek().add(name);
                            runtimeVars.put(name, evalExpression(flattenVars(expression)));
                        }
                    }

                    //Is Boolean Variable
                    else if (tokens.get(i).getTokenType() == TokenType.boolean_var){
                        i++;
                        if (tokens.get(i).getTokenType() == TokenType.name){
                            String name = (String)tokens.get(i).getValue();
                            i++;
                            if (tokens.get(i).getTokenType() != TokenType.equals){
                                System.out.println("Unassigned variable creation!");
                                System.exit(1);
                            }
                            ArrayList<Token> expression = new ArrayList<>();
                            i++;
                            while (tokens.get(i).getTokenType() != TokenType.semi) {
                                expression.add(tokens.get(i));
                                i++;
                            }
                            variablesThisScope.peek().add(name);
                            runtimeVars.put(name, evalExpression(flattenVars(expression)));
                        }
                    }

                    //Is String Variable
                    else if (tokens.get(i).getTokenType() == TokenType.string_var){
                        i++;
                        if (tokens.get(i).getTokenType() == TokenType.name){
                            String name = (String)tokens.get(i).getValue();
                            i++;
                            if (tokens.get(i).getTokenType() != TokenType.equals){
                                System.out.println("Unassigned variable creation!");
                                System.exit(1);
                            }
                            ArrayList<Token> expression = new ArrayList<>();
                            i++;
                            while (tokens.get(i).getTokenType() != TokenType.semi) {
                                expression.add(tokens.get(i));
                                i++;
                            }
                            variablesThisScope.peek().add(name);
                            runtimeVars.put(name, evalExpression(flattenVars(expression)));
                        }
                    }

                    else if (tokens.get(i).getTokenType() == TokenType.float_var){
                        i++;
                        if (tokens.get(i).getTokenType() == TokenType.name){
                            String name = (String)tokens.get(i).getValue();
                            i++;
                            if (tokens.get(i).getTokenType() != TokenType.equals){
                                System.out.println("Unassigned variable creation!");
                                System.exit(1);
                            }
                            ArrayList<Token> expression = new ArrayList<>();
                            i++;
                            while (tokens.get(i).getTokenType() != TokenType.semi) {
                                expression.add(tokens.get(i));
                                i++;
                            }
                            variablesThisScope.peek().add(name);
                            runtimeVars.put(name, evalExpression(flattenVars(expression)));
                        }
                    }

                    //Print command
                    else if (tokens.get(i).getTokenType() == TokenType.print){
                        i++;
                        if (tokens.get(i).getTokenType() == TokenType.string_val
                                || tokens.get(i).getTokenType() == TokenType.int_literal
                                || tokens.get(i).getTokenType() == TokenType.float_val
                                || tokens.get(i).getTokenType() == TokenType.boolean_val){
                            System.out.print(tokens.get(i).getValue());
                        } else if (tokens.get(i).getTokenType() == TokenType.name){
                            if (runtimeVars.containsKey((String)tokens.get(i).getValue())){
                                System.out.print(runtimeVars.get((String)tokens.get(i).getValue()));
                            } else {
                                System.out.println("Variable \"" + tokens.get(i).getValue() + "\" does not exist!");
                                System.exit(1);
                            }
                        } else {
                            System.out.println("Print called with a non-printable constant following!");
                            System.exit(1);
                        }
                    }

                    //PrintLine command
                    else if (tokens.get(i).getTokenType() == TokenType.println){
                        i++;
                        if (tokens.get(i).getTokenType() == TokenType.string_val
                                || tokens.get(i).getTokenType() == TokenType.int_literal
                                || tokens.get(i).getTokenType() == TokenType.float_val
                                || tokens.get(i).getTokenType() == TokenType.boolean_val){
                            System.out.println(tokens.get(i).getValue());
                        } else if (tokens.get(i).getTokenType() == TokenType.name){
                            if (runtimeVars.containsKey((String)tokens.get(i).getValue())){
                                System.out.println(runtimeVars.get((String)tokens.get(i).getValue()));
                            } else {
                                System.out.println("Variable \"" + tokens.get(i).getValue() + "\" does not exist!");
                                System.exit(1);
                            }
                        } else {
                            System.out.println("PrintLine called with a non-printable constant following!");
                            System.exit(1);
                        }
                    }

                    //Case of name
                    else if (tokens.get(i).getTokenType() == TokenType.name){
                        String name = (String) tokens.get(i).getValue();
                        i++;
                        if (tokens.get(i).getTokenType() == TokenType.equals){
                            if (runtimeVars.containsKey(name)){
                                //Setting variable
                                ArrayList<Token> expression = new ArrayList<>();
                                i++;
                                boolean cont = true;
                                while (cont){
                                    expression.add(tokens.get(i));
                                    i++;
                                    if (tokens.get(i).getTokenType() == TokenType.semi){
                                        cont = false;
                                    }
                                }
                                runtimeVars.replace(name, evalExpression(flattenVars(expression)));
                            } else {
                                System.out.println("Variable " + name + " does not exist!");
                                System.exit(1);
                            }
                        }
                    } else if (tokens.get(i).getTokenType() == TokenType.loop){
                        if (tokens.get(i+1).getTokenType() != TokenType.scope_up){
                            System.out.println("Scope container (bracket) not after loop!");
                            System.exit(1);
                        }
                    } else if (tokens.get(i).getTokenType() == TokenType.loopback_if){
                        ArrayList<Token> expression = new ArrayList<>();
                        i++;
                        boolean cont = true;
                        while (cont){
                            expression.add(tokens.get(i));
                            i++;
                            if (tokens.get(i).getTokenType() == TokenType.semi){
                                cont = false;
                            }
                        }
                        Object finalVal = evalExpression(flattenVars(expression));
                        if (finalVal instanceof Boolean){
                            if ((boolean)finalVal){
                                if (loops.peek() != -1){
                                    i = loops.peek();
                                } else {
                                    System.out.println("lif: no loop in current scope!");
                                    System.exit(1);
                                }
                            }
                        } else {
                            System.out.println("lif: expression was not a boolean!");
                            System.exit(1);
                        }
                    } else if (tokens.get(i).getTokenType() == TokenType.input){
                        if (tokens.get(i+1).getTokenType() == TokenType.name){
                            i++;
                            if (runtimeVars.containsKey((String)tokens.get(i).getValue())){
                                runtimeVars.replace((String)tokens.get(i).getValue(), uin.nextLine());
                            } else {
                                System.out.println("No output variable after input keyword!");
                                System.exit(1);
                            }
                        }
                    } else if (tokens.get(i).getTokenType() == TokenType._if){
                        ArrayList<Token> expression = new ArrayList<>();
                        i++;
                        boolean cont = true;
                        while (cont){
                            expression.add(tokens.get(i));
                            i++;
                            if (tokens.get(i).getTokenType() == TokenType.scope_up){
                                cont = false;
                            }
                        }
                        //Do normal SCOPE_UP things (increment naturally at loop end)
                        scopeCounter++;
                        variablesThisScope.push(new ArrayList<>());
                        loops.push(-1);
                        Object finalVal = evalExpression(flattenVars(expression));
                        if (finalVal instanceof Boolean){
                            skip.push(!(boolean)finalVal);
                        } else {
                            System.out.println("if: expression was not a boolean!");
                            System.exit(1);
                        }
                    }

                    else if (tokens.get(i).getTokenType() != TokenType.semi) {
                        System.out.println("No meaning for token " + i);
                        System.exit(1);
                    }
                } else {
                    //In function constructor
                    if (tokens.get(i).getTokenType() == TokenType.name && constructorType == 0){
                        String name = (String)tokens.get(i).getValue();
                        functionIndices.put(name, null);
                        if (name.equals("start")){
                            foundStart = true;
                        }
                    }
                }
            }
        }
        System.exit(0);
        /*
        if (foundStart) System.exit(0);
        else {
            System.out.println("Your program did not have a start!");
            System.exit(1);
        }

         */
    }
}
