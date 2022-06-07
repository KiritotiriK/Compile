import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import backend.mytype.ArrayType;
import backend.mytype.CharType;
import backend.mytype.FloatType;
import backend.mytype.IntType;
import backend.mytype.StringType;
import backend.mytype.basicType;

class Machine {
  public static void main(String[] args)        
    throws FileNotFoundException, IOException {
    if (args.length == 0) 
      System.out.println("Usage: java Machine <programfile> <arg1> ...\n");
    else
      execute(args, false);
  }

  // These numeric instruction codes must agree with Machine.fs:

  final static int 
    CSTI = 0, ADD = 1, SUB = 2, MUL = 3, DIV = 4, MOD = 5, 
    EQ = 6, LT = 7, NOT = 8, 
    DUP = 9, SWAP = 10, 
    LDI = 11, STI = 12, 
    GETBP = 13, GETSP = 14, INCSP = 15, 
    GOTO = 16, IFZERO = 17, IFNZRO = 18, CALL = 19, TCALL = 20, RET = 21, 
    PRINTI = 22, PRINTC = 23, 
    LDARGS = 24,
    STOP = 25, CSTF = 26;

  final static int STACKSIZE = 1000;
  
  // Read code from file and execute it

  static void execute(String[] args, boolean trace) 
    throws FileNotFoundException, IOException {
    ArrayList<Integer> p = readfile(args[0]);                // Read the program from file
    basicType[] s = new basicType[STACKSIZE];               // The evaluation stack
    basicType[] iargs = new basicType[args.length-1];
    for (int i = 1; i < args.length; i++) {
      // 只要arg[i]中匹配到了(?i)[a-z]就返回true，一个个参数进去
      // 字符参数
      if(Pattern.compile("(?i)[a-z]").matcher(args[i]).find()){
          char[] input = args[i].toCharArray();
          CharType[] array = new CharType[input.length];
          for(int j = 0; j < input.length; ++j) {
              array[j] = new CharType(input[j]);
          }
          // 参数数组
          iargs[i-1] = new ArrayType(array);
      }
      // float参数
      else if(args[i].contains(".")){
        iargs[i-1] = new FloatType(new Float(args[i]).floatValue());
      }
      // Int参数
      else {
        iargs[i-1] = new IntType(new Integer(args[i]).intValue());
      }
  }
    long starttime = System.currentTimeMillis();
    execcode(p, s, iargs, trace);            // Execute program proper
    long runtime = System.currentTimeMillis() - starttime;
    System.err.println("\nRan " + runtime/1000.0 + " seconds");
  }

  // The machine: execute the code starting at p[pc] 

  static int execcode(ArrayList<Integer> p, basicType[] s, basicType[] iargs, boolean trace) {
    int bp = -999;	// Base pointer, for local variable access 
    int sp = -1;	// Stack top pointer
    int pc = 0;		// Program counter: next instruction
    for (;;) {
      if (trace) 
        printsppc(s, bp, sp, p, pc);
      switch (p.get(pc+1)) {
        case CSTI:
          s[sp + 1] = new IntType(p.get(pc++)); sp++; break;
        case ADD: 
          s[sp-1] = binaryOperator(s[sp-1], s[sp], "+"); sp--; break;
        case SUB: 
          s[sp-1] = binaryOperator(s[sp-1], s[sp], "-"); sp--; break;
        case MUL: 
          s[sp-1] = binaryOperator(s[sp-1], s[sp], "*"); sp--; break;
        case DIV: 
          s[sp-1] = binaryOperator(s[sp-1], s[sp], "*"); sp--; break;
        case MOD: 
          s[sp-1] = binaryOperator(s[sp-1], s[sp], "*"); sp--; break;
        case EQ: 
          s[sp-1] = binaryOperator(s[sp-1], s[sp], "*"); sp--; break;
        case LT: 
          s[sp-1] = binaryOperator(s[sp-1], s[sp], "*"); sp--; break;
        case NOT: 
          s[sp] = binaryOperator(s[sp-1], s[sp], "*"); break;
        case DUP: 
          s[sp+1] = s[sp]; sp++; break;
        case SWAP: 
          { basicType tmp = s[sp];  s[sp] = s[sp-1];  s[sp-1] = tmp; } break; 
        case LDI:                 // load indirect
          s[sp] = s[((IntType)s[sp]).getValue()]; break;
        case STI:                 // store indirect, keep value on top
          s[((IntType)s[sp-1]).getValue()] = s[sp]; s[sp-1] = s[sp]; sp--; break;
        case GETBP:
          s[sp+1] = new IntType(bp); sp++; break;
        case GETSP:
          s[sp+1] = new IntType(sp); sp++; break;
        case INCSP:
          sp = sp+p.get(pc++); break;
        case GOTO:
          pc = p.get(pc); break;
        case IFZERO:{
            Object result = null;
            int index = sp--;
            // 判断类型
            if(s[index] instanceof IntType){
                result = ((IntType)s[index]).getValue();
            }else if(s[index] instanceof FloatType){
                result = ((FloatType)s[index]).getValue();
            }
            // 是0就当前的指令，不是0就下一条指令
            pc = (Float.compare(new Float(result.toString()), 0.0f) == 0 ? p.get(pc) : pc + 1);
            break;
        }
        case IFNZRO:{
          Object result = null;
          int index = sp--;
          if (s[index] instanceof IntType) {
              result = ((IntType) s[index]).getValue();
          } else if (s[index] instanceof FloatType) {
              result = ((FloatType) s[index]).getValue();
          }
          //  不是0就当前的指令，不是0就下一条指令
          pc = (Float.compare(new Float(result.toString()), 0.0f) != 0 ? p.get(pc) : pc + 1);
          break;
        }
        case CALL: { 
          int argc = p.get(pc++);
          for (int i=0; i<argc; i++)	   // Make room for return address
            s[sp-i+2] = s[sp-i];		   // and old base pointer
          s[sp-argc+1] = new IntType(pc+1); sp++; 
          s[sp-argc+1] = new IntType(bp);   sp++; 
          bp = sp+1-argc;
          pc = p.get(pc); 
        } break; 
        case TCALL: { 
          int argc = p.get(pc++);                // Number of new arguments
          int pop  = p.get(pc++);		   // Number of variables to discard
          for (int i=argc-1; i>=0; i--)	   // Discard variables
            s[sp-i-pop] = s[sp-i];
          sp = sp - pop; p.get(pc); 
        } break; 
        case RET: { 
          basicType res = s[sp]; 
          sp = sp - p.get(pc); 
          bp = ((IntType)s[--sp]).getValue(); 
          pc = ((IntType)s[--sp]).getValue();
          s[sp] = res; 
        } break; 
        case PRINTI:
          System.out.print(s[sp] + " "); break; 
        case PRINTC:
          System.out.print((((CharType)s[sp])).getValue()); break;
        case LDARGS:
    for (int i=0; i<iargs.length; i++) // Push commandline arguments
      s[++sp] = iargs[i];
    break;
        case STOP:
          return sp;
        case CSTF:     //int字节转换为FLoat 入栈
          s[sp+1] = new FloatType(Float.intBitsToFloat(p.get(pc++))); sp++; break;
        default:                  
          throw new RuntimeException("Illegal instruction " + p.get(pc-1)
                                    + " at address " + (pc-1));
      }
    }
  }

  // Print the stack machine instruction at p[pc]

  public static basicType binaryOperator(basicType lhs, basicType rhs, String operator) {
      Object left = 1;
      Object right = 1;
      int flag = 0;
      // 判断左右值的类型
      if (lhs instanceof FloatType) {
          left = ((FloatType) lhs).getValue();
          flag = 1;
      } else if (lhs instanceof IntType) {
          left = ((IntType) lhs).getValue();
      } else if (operator=="+"&&lhs instanceof CharType) {
          left = (((CharType) lhs).getValue());
          flag = 2;
      } else if (operator=="+"&&lhs instanceof StringType){
          left = (((StringType)lhs).getValue());
          flag = 3;
      }
      else {
          // throw new TypeError("TypeError: Left type is not int or float");
      }
      if (rhs instanceof FloatType) {
          right = ((FloatType) rhs).getValue();;
          flag = 1;
      } else if (rhs instanceof IntType) {
          right = ((IntType) rhs).getValue();
      }  else if (operator=="+"&&rhs instanceof CharType) {
          right = (((CharType) rhs).getValue());
          flag = 2;
      } else if (operator=="+"&&rhs instanceof StringType){
          right = (((StringType)rhs).getValue());
          flag = 3;
      } 
      // else {
          // throw new TypeError("TypeError: Right type is not int or float");
      // }
      basicType result = null;

      switch(operator){
          case "+":{
              if (flag == 1) {
                  result =  new FloatType(Float.parseFloat(String.valueOf(left)) + Float.parseFloat(String.valueOf(right)));
              } else if(flag == 2){
                  StringType achar = new StringType((char)left);
                  String astring = achar.addChar(String.valueOf(right));
                  result = new StringType(astring);
                  // System.out.println("4:"+result);
              } else if(flag == 3){
                  StringType astring = new StringType(String.valueOf(left));
                  String astrings = astring.addChar(String.valueOf(right));
                  result = new StringType(astrings);
              } else {
                  result = new IntType(Integer.parseInt(String.valueOf(left)) + Integer.parseInt(String.valueOf(right)));
              }
              break;
          }
          case "-":{
              if (flag == 1) {
                  result = new FloatType(Float.parseFloat(String.valueOf(left)) - Float.parseFloat(String.valueOf(right)));
              } else {
                  result = new IntType(Integer.parseInt(String.valueOf(left)) - Integer.parseInt(String.valueOf(right)));
              }
              break;
          }
          case "*":{
              if (flag == 1) {
                  result = new FloatType(Float.parseFloat(String.valueOf(left)) * Float.parseFloat(String.valueOf(right)));
              } else {
                  result = new IntType(Integer.parseInt(String.valueOf(left)) * Integer.parseInt(String.valueOf(right)));
              }
              break;
          }
          case "/":{
              if(Float.compare(Float.parseFloat(String.valueOf(right)), 0.0f) == 0){
                  // throw new OperatorError("OpeatorError: Divisor can't not be zero");
              }
              if (flag == 1) {
                  result = new FloatType(Float.parseFloat(String.valueOf(left)) / Float.parseFloat(String.valueOf(right)));
              } else {
                  result = new IntType(Integer.parseInt(String.valueOf(left)) / Integer.parseInt(String.valueOf(right)));
              }
              break;
          }
          case "%":{
              if (flag == 1) {
                  // throw new OperatorError("OpeatorError: Float can't mod");
              } else {
                  result = new IntType(Integer.parseInt(String.valueOf(left)) % Integer.parseInt(String.valueOf(right)));
              }
              break;
          }
          case "==":{
              if (flag == 1) {
                  if((float) left == (float) right){
                      result = new IntType(1);
                  }
                  else{
                      result = new IntType(0);
                  }
              } else {
                  if((int) left == (int) right){
                      result = new IntType(1);
                  }
                  else{
                      result = new IntType(0);
                  }
              }
              break;
          }
          case "<":{
              if (flag == 1) {
                  if((float) left < (float) right){
                      result = new IntType(1);
                  }
                  else{
                      result = new IntType(0);
                  }
              } else {
                  if((int) left < (int) right){
                      result = new IntType(1);
                  }
                  else{
                      result = new IntType(0);
                  }
              }
              break;
          }
      }
      return result;
  }

  static String insname(ArrayList<Integer> p, int pc) {
    switch (p.get(pc)) {
    case CSTI:   return "CSTI " + p.get(pc+1);
    case ADD:    return "ADD";
    case SUB:    return "SUB";
    case MUL:    return "MUL";
    case DIV:    return "DIV";
    case MOD:    return "MOD";
    case EQ:     return "EQ";
    case LT:     return "LT";
    case NOT:    return "NOT";
    case DUP:    return "DUP";
    case SWAP:   return "SWAP";
    case LDI:    return "LDI";
    case STI:    return "STI";
    case GETBP:  return "GETBP";
    case GETSP:  return "GETSP";
    case INCSP:  return "INCSP " + p.get(pc+1);
    case GOTO:   return "GOTO " + p.get(pc+1);
    case IFZERO: return "IFZERO " + p.get(pc+1);
    case IFNZRO: return "IFNZRO " + p.get(pc+1);
    case CALL:   return "CALL " + p.get(pc+1) + " " + p.get(pc+1);
    case TCALL:  return "TCALL " + p.get(pc+1) + " " + p.get(pc+1) + " " + p.get(pc+1);
    case RET:    return "RET " + p.get(pc+1);
    case PRINTI: return "PRINTI";
    case PRINTC: return "PRINTC";
    case LDARGS: return "LDARGS";
    case STOP:   return "STOP";
    case CSTF:   return "CSTF " + p.get(pc+1);
    default:     return "<unknown>";
    }
  }

  // Print current stack and current instruction

  static void printsppc(basicType[] s, int bp, int sp, ArrayList<Integer> p, int pc) {
    System.out.print("[ ");
    for (int i=0; i<=sp; i++)
      System.out.print(s[i] + " ");
    System.out.print("]");
    System.out.println("{" + pc + ": " + insname(p, pc) + "}"); 
  }

  // Read instructions from a file

  public static ArrayList<Integer> readfile(String filename) 
    throws FileNotFoundException, IOException
  {
    ArrayList<Integer> program = new ArrayList<Integer>();
    Reader inp = new FileReader(filename);
    StreamTokenizer tstream = new StreamTokenizer(inp);
    tstream.parseNumbers();
    tstream.nextToken();
    while (tstream.ttype == StreamTokenizer.TT_NUMBER) {
      program.add(new Integer((int)tstream.nval));
      tstream.nextToken();
    }
    inp.close();
    return program;
  }
}

// Run the machine with tracing: print each instruction as it is executed

class Machinetrace {
  public static void main(String[] args)        
    throws FileNotFoundException, IOException {
    if (args.length == 0) 
      System.out.println("Usage: java Machinetrace <programfile> <arg1> ...\n");
    else
      Machine.execute(args, true);
  }
}
