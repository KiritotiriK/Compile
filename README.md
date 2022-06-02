# 编译原理大作业
## Miroc


### 6.2新增 `wyl`：
* AbstractSyntax.fs—— 抽象语法树文件
  * 新增声明：Sleep
* MyLex.fsl—— 词法分析器
  * 新增关键词 `"sleep"  -> SLEEP`
* MyPar.fsy—— 语法分析器
  * 新增指令 `%token SLEEP`
  * 新增 StmtM
  * 新增`SLEEP Expr`,参考PRINT Expr    
* Assembly.fs—— 汇编编译指令集
  * 新增指令 `SLEEP` 
  * 新增数字指令 `CODESLEEP = 27` ,参考前面,有样学样
  * 新增makelabenv和emitints `SLEEP`,参考STOP
* Contcompile.fs—— 将抽象语法树转化为中间表示
  * 新增cExpr`"sleep" -> SLEEP`，参考`printc -> PRINTC`



### 6.1新增 `wyl`：
* #### `文件进行了重构`
* MyPar.fsy—— 语法分析器
    * ExprNotAccess
      * Type:
        | STRING                              { TypeString   }
        | FLOAT                               { TypeFloat    }
        | STRUCT  NAME                        { TypeStruct($2) }
* MyLex.fsl—— 词法分析器
    * float -> FLOAT 
    * Token
      * `['0'-'9']+'.'['0'-'9']+`  // System.Single.Parse将字符串转换为单精度浮点数，参考`['0'-'9']+` 
* MyPar.fsy—— 语法分析器
    * %token FLOAT STRING  `//新增float,string`
    * %token CSTFLOAT CSTCHAR
    * ConstFloat `| MINUS CSTINT` ,参考`Const`
* Assembly.fs—— 汇编编译指令集
  * 定义指令CSTF `CSTF of int32 ` 参考了CSTI,且float为32位
  * 新增数字指令 `CODECSTF = 26` ,参考前面,有样学样
  * 新增makelabenv和emitints `CSTF i `,参考CSTI
  * 新增反编译`| CODECSTF :: i :: ints_rest -> CSTF i :: decomp `,参考CODECSTI
* Contcompile.fs—— 将抽象语法树转化为中间表示
  * 新增`addCSTF i C`，参考`addCST i C`




### 5.31新增 `wyl`：

* MyLex.fsl—— 词法分析器
    * 修改注释的表示方式，添加了`(* *)`,参考`/* */`
    * 添加了双引号`" "`,参考`' '`



### 5.29新增 `wyl`：
* AbstractSyntax.fs—— 抽象语法树文件
    * 定义基础类型：TypString，TypeFloat，TypeVoid
    * 定义了基础表达式：常数Float值
    * 定义了for声明
* MyLex.fsl—— 词法分析器
    * for -> FOR 
    * string -> STRING 
* MyPar.fsy—— 语法分析器
    * ConstString
    * ConstFloat
    * ConstChar 

