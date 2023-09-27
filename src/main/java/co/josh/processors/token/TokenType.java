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
    int_literal("integer_literal"),
    int_var("integer_variable"),
    string_val("string_value"),
    @Deprecated
    string_var("deprecated_string_variable"),
    @Deprecated
    char_val("character_value"),
    @Deprecated
    char_var("character_variable"),
    @Deprecated
    double_val("deprecated_double_value"),
    @Deprecated
    double_var("deprecated_double_variable"),
    @Deprecated
    byte_val("byte_value"),
    byte_var("byte_variable"),
    get_pointer("reference"),
    set_val_at_pointer("dereference_set"),
    get_val_at_pointer("dereference_get"),
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
    modulo("mod"),
    and_bool_op("&&"),
    or_bool_op("||"),
    not_bool_op("!"),
    xor_bool_op("^"),
    inequality_equals("=="),
    inequality_greater(">"),
    inequality_lesser("<"),
    bit_shift_left("<<"),
    bit_shift_right(">>"),
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
    memfree("free_memory"),
    static_scoped_allocate("static_scoped_allocate"),
    raw_out("cout"),
    static_global_allocate("static_global_allocate"),
    inequality_not_equals("!="), inequality_greater_equals(">="), inequality_lesser_equals("<=");

    public String s;
    TokenType(String s) {
        this.s = s;
    }
}
