# 编译原理大作业
## Miroc

### 6.1新增 `wyl`：
* #### `文件进行了重构`
* MyPar.fsy—— 语法分析器
    * ExprNotAccess
      * Type:
        | STRING                              { TypeString   }
        | FLOAT                               { TypeFloat    }
        | STRUCT  NAME                        { TypeStruct($2) }
    


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

