package co.josh.compile;

public enum Instruction {

    exit,
    setVariableFConst,
    setVariableFVariable,
    newVariableFConst,
    newVariableFVariable,
    setRegister1Const,
    setRegister2Const,
    setRegister1Var,
    setRegister2Var,
    add,
    sub,
    mult,
    div,
    jump,
    jumpmarker,
    newtype,
    macroexec, newmacro
}
