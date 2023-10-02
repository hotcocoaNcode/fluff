package co.josh.processors.token.v2;

import co.josh.JoshLogger;
import co.josh.processors.token.Token;
import co.josh.processors.token.TokenType;

import java.util.ArrayList;
import java.util.HashMap;

public class v2Tokenizer implements co.josh.processors.token.Tokenizer {
    final HashMap<String, TokenType> keywords = new HashMap<>();

    public v2Tokenizer(){
        //Built in types
        keywords.put("int8", TokenType.int8_variable);
        keywords.put("int16", TokenType.int16_variable);
        //Functions
        keywords.put("cout", TokenType.character_out);
        keywords.put("exit", TokenType.exit);
        keywords.put("cin", TokenType.character_input);
        keywords.put("include", TokenType.include_file);
        keywords.put("free", TokenType.free_variable);
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
        boolean multiLineComment = false;
        boolean singleLineComment = false;
        for (int i = 0; i < s.length(); i++){
            if (s.charAt(i) == '\n'){
                line++;
                singleLineComment = false;
                continue;
            }
            if (s.charAt(i) == '/' && s.charAt(i+1) == '*'){
                multiLineComment = true;
                continue;
            }

            if (s.charAt(i) == '/' && s.charAt(i+1) == '/'){
                singleLineComment = true;
                continue;
            }

            if (s.charAt(i) == '*' && s.charAt(i+1) == '/'){
                multiLineComment = false;
                i += 2;
                continue;
            }
            if (!multiLineComment && !singleLineComment) {
                //Keywords
                if (Character.isAlphabetic(s.charAt(i))) {
                    buf = buf + s.charAt(i);
                    i++;
                    for (; i < s.length(); i++) {
                        if (Character.isLetterOrDigit(s.charAt(i)) || s.charAt(i) == '_') {
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
                        t.add(new Token(TokenType.int_literal, 0, line));
                    } else {
                        t.add(new Token(TokenType.name, buf, line));
                    }
                    buf = "";
                }
                //Hexadecimal
                else if (s.charAt(i) == '0' && s.charAt(i+1) == 'x'){
                    buf = buf + s.charAt(i);
                    i+=3;
                    for (; i < s.length(); i++) {
                        if (Character.isLetterOrDigit(s.charAt(i))) {
                            buf = buf + s.charAt(i);
                        } else {
                            break;
                        }
                    }
                    i--;
                    // THREE COMMANDMENTS OF FLUFF LITERALS (only matters for storing to pointer locations)
                    // Hexadecimal will always assume the smallest data type (if below 0xFF is byte)
                    // Decimal will always be short
                    // Chars will always be bytes
                    try {
                        t.add(new Token(TokenType.int_literal, Byte.valueOf(buf, 16), line));
                    } catch (NumberFormatException e) {
                        t.add(new Token(TokenType.int_literal, Short.valueOf(buf, 16), line));
                    }
                    buf = "";
                }
                //Short
                else if (Character.isDigit(s.charAt(i)) || (s.charAt(i) == '-' && Character.isDigit(s.charAt(i+1)))) {
                    buf = buf + s.charAt(i);
                    i++;
                    for (; i < s.length(); i++) {
                        if (Character.isDigit(s.charAt(i))) {
                            buf = buf + s.charAt(i);
                        } else {
                            break;
                        }
                    }
                    i--;
                    t.add(new Token(TokenType.int_literal, Short.valueOf(buf), line));
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
                } else if (s.charAt(i) == '>' && s.charAt(i + 1) == '=') {
                    t.add(new Token(TokenType.inequality_greater_equals, null, line));
                    i++;
                } else if (s.charAt(i) == '<' && s.charAt(i + 1) == '=') {
                    t.add(new Token(TokenType.inequality_lesser_equals, null, line));
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
                //Strings
                else if (s.charAt(i) == '"') {
                    i++;
                    for (; i < s.length(); i++) {
                        if (s.charAt(i) != '"') {
                            buf = buf + s.charAt(i);
                        } else {
                            break;
                        }
                    }
                    t.add(new Token(TokenType.string_val, buf.translateEscapes(), line));
                    buf = "";
                }
                //Char values
                else if (s.charAt(i) == '\'') {
                    i++;
                    for (; i < s.length(); i++) {
                        if (s.charAt(i) != '\'') {
                            buf = buf + s.charAt(i);
                        } else {
                            break;
                        }
                    }
                    buf = buf.translateEscapes();
                    if (buf.length() == 1) {
                        t.add(new Token(TokenType.int_literal, (byte)buf.charAt(0), line));
                    } else {
                        JoshLogger.syntaxError("Character can only be one char in length!", line);
                    }
                    buf = "";
                }
                // Semi
                else if (s.charAt(i) == ';') {
                    t.add(new Token(TokenType.semi, null, line));
                }
            }
        }
        JoshLogger.log("Tokenized "+fn);
        return t;
    }
}
