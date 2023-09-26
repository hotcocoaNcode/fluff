package co.josh.processors.token.v2;

import co.josh.JoshLogger;
import co.josh.processors.token.Token;
import co.josh.processors.token.TokenType;

import java.util.ArrayList;
import java.util.HashMap;

public class v2Tokenizer implements co.josh.processors.token.Tokenizer {
    HashMap<String, TokenType> keywords = new HashMap<>();

    public v2Tokenizer(){
        //Types
        keywords.put("byte", TokenType.byte_var);
        keywords.put("short", TokenType.int_var);
        //Functions
        keywords.put("cout", TokenType.raw_out);
        keywords.put("exit", TokenType.exit);
        keywords.put("cin", TokenType.input);
        keywords.put("include", TokenType.include);
        keywords.put("free", TokenType.memfree);
        keywords.put("csalloc", TokenType.static_scoped_allocate);
        keywords.put("cgalloc", TokenType.static_global_allocate);
        //Conditionals
        keywords.put("if", TokenType._if);
        keywords.put("lif", TokenType.loopback_if);
        //Loop
        keywords.put("loop", TokenType.loop);
        //Definitions
        keywords.put("macro", TokenType.macro_def);
        //keywords.put("newtype", TokenType.define_type); //TODO custom types
    }

    @Override
    public ArrayList<Token> tokenize(String s, String fn) {
        ArrayList<Token> t = new ArrayList<>();
        String buf = "";
        int line = 1;
        boolean inComment = false;
        for (int i = 0; i < s.length(); i++){
            if (s.charAt(i) == '\n'){
                line++;
                continue;
            }
            if (s.charAt(i) == '/' && s.charAt(i+1) == '*'){
                inComment = true;
                continue;
            }
            if (s.charAt(i) == '*' && s.charAt(i+1) == '/'){
                inComment = false;
                i += 2;
                continue;
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
                        t.add(new Token(keywords.get(buf), null, line));
                    } else if (buf.equals("true")) {
                        t.add(new Token(TokenType.int_literal, 1, line));
                    } else if (buf.equals("false")) {
                        t.add(new Token(TokenType.int_literal, -1, line));
                    } else {
                        t.add(new Token(TokenType.name, buf, line));
                    }
                    buf = "";
                } else if (Character.isDigit(s.charAt(i)) || (s.charAt(i) == '-' && Character.isDigit(s.charAt(i+1)))) {
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
                        t.add(new Token(TokenType.int_literal, Short.valueOf(buf), line));
                    } else {
                        t.add(new Token(TokenType.float_val, Float.valueOf(buf), line));
                    }
                    buf = "";
                }
                //Bracket-like
                else if (s.charAt(i) == '(') {
                    t.add(new Token(TokenType.opening_parentheses, null, line));
                } else if (s.charAt(i) == ')') {
                    t.add(new Token(TokenType.closing_parentheses, null, line));
                } else if (s.charAt(i) == '{') {
                    t.add(new Token(TokenType.scope_up, null, line));
                } else if (s.charAt(i) == '}') {
                    t.add(new Token(TokenType.scope_down, null, line));
                } else if (s.charAt(i) == '[') {
                    t.add(new Token(TokenType.bracket_open, null, line));
                } else if (s.charAt(i) == ']') {
                    t.add(new Token(TokenType.bracket_close, null, line));
                }
                //Have to slap this before booleans (nequals)
                else if (s.charAt(i) == '!' && s.charAt(i+1) == '=') {
                    t.add(new Token(TokenType.inequality_not_equals, null, line));
                    i++;
                }
                //Booleans
                else if (s.charAt(i) == '&' && s.charAt(i + 1) == '&') {
                    t.add(new Token(TokenType.and_bool_op, null, line));
                    i++;
                } else if (s.charAt(i) == '|' && s.charAt(i + 1) == '|') {
                    t.add(new Token(TokenType.or_bool_op, null, line));
                    i++;
                } else if (s.charAt(i) == '^') {
                    t.add(new Token(TokenType.xor_bool_op, null, line));
                } else if (s.charAt(i) == '!') {
                    t.add(new Token(TokenType.not_bool_op, null, line));
                }
                //AssignNoEval
                else if (s.charAt(i) == '.' && s.charAt(i + 1) == '>') {
                    t.add(new Token(TokenType.quick_assign, null, line));
                    i++;
                }
                //Slapping in bit shifts here so inequalities don't override them
                else if (s.charAt(i) == '<' && s.charAt(i+1) == '<'){
                    t.add(new Token(TokenType.bit_shift_left, null, line));
                    i++;
                } else if (s.charAt(i) == '>' && s.charAt(i+1) == '>'){
                    t.add(new Token(TokenType.bit_shift_right, null, line));
                    i++;
                }
                //Pointer syntax
                else if (s.charAt(i) == '@'){
                    t.add(new Token(TokenType.get_pointer, null, line));
                } else if (s.charAt(i) == '<' && s.charAt(i+1) == '-'){
                    t.add(new Token(TokenType.set_val_at_pointer, null, line));
                    i++;
                } else if (s.charAt(i) == '-' && s.charAt(i+1) == '>'){
                    t.add(new Token(TokenType.get_val_at_pointer, null, line));
                    i++;
                }
                //Inequalities
                else if (s.charAt(i) == '=' && s.charAt(i + 1) == '=') {
                    t.add(new Token(TokenType.inequality_equals, null, line));
                    i++;
                } else if (s.charAt(i) == '>') {
                    t.add(new Token(TokenType.inequality_greater, null, line));
                } else if (s.charAt(i) == '<') {
                    t.add(new Token(TokenType.inequality_lesser, null, line));
                }
                //Math
                else if (s.charAt(i) == '=') {
                    t.add(new Token(TokenType.equals, null, line));
                } else if (s.charAt(i) == '+') {
                    t.add(new Token(TokenType.add, null, line));
                } else if (s.charAt(i) == '-') {
                    t.add(new Token(TokenType.subtract, null, line));
                } else if (s.charAt(i) == '*') {
                    t.add(new Token(TokenType.multiply, null, line));
                } else if (s.charAt(i) == '/') {
                    t.add(new Token(TokenType.divide, null, line));
                } else if (s.charAt(i) == '%'){
                    t.add(new Token(TokenType.modulo, null, line));
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
                    t.add(new Token(TokenType.string_val, buf, line));
                    buf = "";
                } else if (s.charAt(i) == '\'') {
                    //char val
                    i++;
                    if (s.charAt(i + 1) == '\'') {
                        t.add(new Token(TokenType.int_literal, (byte)s.charAt(i), line));
                    } else {
                        JoshLogger.syntaxError("Character can only be one char in length!", line);
                    }
                    i++;
                } else if (s.charAt(i) == ';') {
                    //semicolon
                    t.add(new Token(TokenType.semi, null, line));
                }
            }
        }
        JoshLogger.log("Tokenized "+fn);
        return t;
    }
}
