package microc.backend;

public class Instruction {
    public final static int CSTI = 0;
    public final static int ADD = 1;
    public final static int SUB = 2;
    public final static int MUL = 3;
    public final static int DIV = 4;
    public final static int MOD = 5;
    public final static int EQ = 6;
    public final static int LT = 7;
    public final static int NOT = 8;
    public final static int DUP = 9;
    public final static int SWAP = 10;
    public final static int LDI = 11;
    public final static int STI = 12;
    public final static int GETBP = 13;
    public final static int GETSP = 14;
    public final static int INCSP = 15;
    public final static int GOTO = 16;
    public final static int IFZERO = 17;
    public final static int IFNZRO = 18;
    public final static int CALL = 19;
    public final static int TCALL = 20;
    public final static int RET = 21;
    public final static int PRINTI = 22;
    public final static int PRINTC = 23;
    public final static int LDARGS = 24;
    public final static int STOP = 25;
    public final static int CSTF = 26;
    public final static int SLEEP = 27;

}
