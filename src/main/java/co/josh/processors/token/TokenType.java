package co.josh.processors.token;

public enum TokenType {
    exit("exit"),
    function_old("function_old"),
    name("name"),
    println("println"),
    print("print"),
    input("input"),
    boolean_var("boolean_variable"),
    boolean_val("boolean_value"),
    int_val("integer_value"),
    int_var("integer_variable"),
    string_val("string_value"),
    @Deprecated
    string_var("deprecated_string_variable"),
    char_val("character_value"),
    char_var("character_variable"),
    @Deprecated
    double_val("deprecated_double_value"),
    @Deprecated
    double_var("deprecated_double_variable"),
    byte_val("byte_value"),
    byte_var("byte_variable"),
    pointer_var("unused_pointer_variable"),
    float_val("float_value"),
    float_var("float_variable"),
    opening_parentheses("("),
    closing_parentheses(")"),
    scope_up("{"),
    scope_down("}"),
    equals("="),
    add("+"),
    divide("/"),
    subtract("-"),
    multiply("*"),
    and_bool_op("&&"),
    or_bool_op("||"),
    not_bool_op("!"),
    xor_bool_op("^"),
    inequality_equals("=="),
    inequality_greater(">"),
    inequality_lesser("<"),
    _if("if"),
    loopback_if("loopback_if"),
    loop("loop_start"),
    _while("unused_while_start"),
    bracket_open("["),
    bracket_close("]"),
    include("include"),
    define_type("define_type"),
    macro_def("define_macro"),
    semi(";"),
    quick_assign("quick_assign"),
    memfree("free_memory");

    public String s;
    TokenType(String s) {
        this.s = s;
    }
}
