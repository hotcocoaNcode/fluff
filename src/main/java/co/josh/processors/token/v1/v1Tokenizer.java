package co.josh.processors.token.v1;

import co.josh.JoshLogger;
import co.josh.processors.token.Token;
import co.josh.processors.token.TokenType;
import co.josh.processors.token.Tokenizer;

import java.util.ArrayList;
import java.util.HashMap;

public class v1Tokenizer implements Tokenizer {

    HashMap<String, co.josh.processors.token.TokenType> keywords = new HashMap<>();

    public v1Tokenizer(){
        //Types
        keywords.put("int", TokenType.int_var);
        keywords.put("string", TokenType.string_var);
        keywords.put("float", TokenType.float_var);
        keywords.put("boolean", TokenType.boolean_var);
        //Functions
        keywords.put("println", TokenType.println);
        keywords.put("print", TokenType.print);
        keywords.put("exit", TokenType.exit);
        keywords.put("input", TokenType.input);
        //Conditionals
        keywords.put("if", TokenType._if);
        keywords.put("lif", TokenType.loopback_if);
        //Loop
        keywords.put("loop", TokenType.loop);
    }

    @Override
    public ArrayList<Token> tokenize(String s){
        ArrayList<Token> t = new ArrayList<>();
        String buf = "";
        boolean inComment = false;
        for (int i = 0; i < s.length(); i++){
            if (s.charAt(i) == '/' && s.charAt(i+1) == '*'){
                inComment = true;
            }
            if (s.charAt(i) == '*' && s.charAt(i+1) == '/'){
                inComment = false;
                i += 2;
            }
            if (!inComment) {
                //keywords
                if (Character.isAlphabetic(s.charAt(i))) {
                    buf = buf + s.charAt(i);
                    i++;
                    for (; i < s.length(); i++) {
                        if (Character.isAlphabetic(s.charAt(i)) || Character.isDigit(s.charAt(i))) {
                            buf = buf + s.charAt(i);
                        } else {
                            break;
                        }
                    }
                    i--;
                    if (keywords.containsKey(buf)) {
                        t.add(new Token(keywords.get(buf), null));
                    } else if (buf.equals("true")) {
                        t.add(new Token(TokenType.boolean_val, true));
                    } else if (buf.equals("false")) {
                        t.add(new Token(TokenType.boolean_val, false));
                    } else {
                        t.add(new Token(TokenType.name, buf));
                    }
                    buf = "";
                } else if (Character.isDigit(s.charAt(i))) {
                    buf = buf + s.charAt(i);
                    boolean isInt = true;
                    int decimalCount = 0;
                    i++;
                    for (; i < s.length(); i++) {
                        if (Character.isDigit(s.charAt(i))) {
                            if (!isInt) {
                                decimalCount++;
                            }
                            buf = buf + s.charAt(i);
                        } else if (s.charAt(i) == '.' && isInt) {
                            buf = buf + s.charAt(i);
                            isInt = false;
                        } else {
                            break;
                        }
                    }
                    i--;
                    if (isInt) {
                        t.add(new Token(TokenType.int_literal, Integer.valueOf(buf)));
                    } else if (decimalCount < 7) {
                        t.add(new Token(TokenType.float_val, Float.valueOf(buf)));
                    } else {
                        t.add(new Token(TokenType.double_val, Double.valueOf(buf)));
                    }
                    buf = "";
                } else if (s.charAt(i) == '(') {
                    t.add(new Token(TokenType.opening_parentheses, null));
                } else if (s.charAt(i) == ')') {
                    t.add(new Token(TokenType.closing_parentheses, null));
                } else if (s.charAt(i) == '{') {
                    t.add(new Token(TokenType.scope_up, null));
                } else if (s.charAt(i) == '}') {
                    t.add(new Token(TokenType.scope_down, null));
                }
                //Booleans
                else if (s.charAt(i) == '&' && s.charAt(i + 1) == '&') {
                    t.add(new Token(TokenType.and_bool_op, null));
                    i++;
                } else if (s.charAt(i) == '|' && s.charAt(i + 1) == '|') {
                    t.add(new Token(TokenType.or_bool_op, null));
                    i++;
                } else if (s.charAt(i) == '^') {
                    t.add(new Token(TokenType.xor_bool_op, null));
                } else if (s.charAt(i) == '!') {
                    t.add(new Token(TokenType.not_bool_op, null));
                }
                //Inequalities
                else if (s.charAt(i) == '=' && s.charAt(i + 1) == '=') {
                    t.add(new Token(TokenType.inequality_equals, null));
                    i++;
                } else if (s.charAt(i) == '>') {
                    t.add(new Token(TokenType.inequality_greater, null));
                } else if (s.charAt(i) == '<') {
                    t.add(new Token(TokenType.inequality_lesser, null));
                }
                //Math
                else if (s.charAt(i) == '=') {
                    t.add(new Token(TokenType.equals, null));
                } else if (s.charAt(i) == '+') {
                    t.add(new Token(TokenType.add, null));
                } else if (s.charAt(i) == '-') {
                    t.add(new Token(TokenType.subtract, null));
                } else if (s.charAt(i) == '*') {
                    t.add(new Token(TokenType.multiply, null));
                } else if (s.charAt(i) == '/') {
                    t.add(new Token(TokenType.divide, null));
                }
                //Other random syntax things that may or may not be used (individually documented)
                else if (s.charAt(i) == '"') {
                    //string val
                    i++;
                    for (; i < s.length(); i++) {
                        if (s.charAt(i) != '"') {
                            buf = buf + s.charAt(i);
                        } else {
                            break;
                        }
                    }
                    t.add(new Token(TokenType.string_val, buf));
                    buf = "";
                } else if (s.charAt(i) == '\'') {
                    //char val
                    i++;
                    if (s.charAt(i + 1) == '\'') {
                        t.add(new Token(TokenType.char_val, s.charAt(i)));
                    } else {
                        JoshLogger.syntaxError("Character can only be one char in length!");
                    }
                    i++;
                } else if (s.charAt(i) == ';') {
                    //semicolon
                    t.add(new Token(TokenType.semi, null));
                }
            }
        }
        return t;
    }
}
