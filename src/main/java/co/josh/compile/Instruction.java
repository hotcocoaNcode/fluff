package co.josh.compile;

public enum Instruction {

    exit,
    setVariableFConst,
    setVariableFVariable,
    newVariableFConst,
    newVariableFVariable,
    add,
    sub,
    div,
    mult,
    pushConstByte,
    pushConst16bit,
    pushByte,
    push16bit,
    pop16bit,
    popByte,
    jump,
    jumpmarker,
    newtype,
    macroexec,
    newmacro,
    free,
}
