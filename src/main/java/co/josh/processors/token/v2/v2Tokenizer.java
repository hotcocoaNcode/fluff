package co.josh.processors.token.v2;

import co.josh.JoshLogger;
import co.josh.processors.token.Token;
import co.josh.processors.token.TokenType;
import co.josh.processors.token.Tokenizer;

import java.util.ArrayList;
import java.util.HashMap;

public class v2Tokenizer implements co.josh.processors.token.Tokenizer {
    HashMap<String, TokenType> keywords = new HashMap<>();

    public v2Tokenizer(){
        //Types
        keywords.put("byte", TokenType.byte_var);
        keywords.put("int", TokenType.int_var);
        keywords.put("boolean", TokenType.boolean_var);
        keywords.put("float16", TokenType.float_var);
        //Functions
        keywords.put("println", TokenType.println);
        keywords.put("print", TokenType.print);
        keywords.put("exit", TokenType.exit);
        keywords.put("input", TokenType.input);
        keywords.put("include", TokenType.include);
        //Conditionals
        keywords.put("if", TokenType._if);
        keywords.put("lif", TokenType.loopback_if);
        //Loop
        keywords.put("loop", TokenType.loop);
        //Definitions
        //keywords.put("macro", TokenType.macro_def); //TODO macros
        //keywords.put("typedef", TokenType.define_type); //TODO custom types
    }

    @Override
    public ArrayList<Token> tokenize(String s) {
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
                    i++;
                    for (; i < s.length(); i++) {
                        if (Character.isDigit(s.charAt(i))) {
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
                        t.add(new Token(TokenType.int_val, Integer.valueOf(buf)));
                    } else {
                        t.add(new Token(TokenType.float_val, Float.valueOf(buf)));
                    }
                    buf = "";
                }
                //Bracket-like
                else if (s.charAt(i) == '(') {
                    t.add(new Token(TokenType.opening_parentheses, null));
                } else if (s.charAt(i) == ')') {
                    t.add(new Token(TokenType.closing_parentheses, null));
                } else if (s.charAt(i) == '{') {
                    t.add(new Token(TokenType.scope_up, null));
                } else if (s.charAt(i) == '}') {
                    t.add(new Token(TokenType.scope_down, null));
                } else if (s.charAt(i) == '[') {
                    t.add(new Token(TokenType.bracket_open, null));
                } else if (s.charAt(i) == ']') {
                    t.add(new Token(TokenType.bracket_close, null));
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
                //AssignNoEval
                else if (s.charAt(i) == '.' && s.charAt(i + 1) == '>') {
                    t.add(new Token(TokenType.quick_assign, null));
                    i++;
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
                    //character array
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
