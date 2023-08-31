[![GitHub release](https://img.shields.io/github/release/Toby-Shi-cloud/Mars-with-BUAA-CO-extension.svg)](https://GitHub.com/Toby-Shi-cloud/Mars-with-BUAA-CO-extension/releases/)
[![Build Jar](https://github.com/Toby-Shi-cloud/Mars-with-BUAA-CO-extension/actions/workflows/build.yml/badge.svg)](https://github.com/Toby-Shi-cloud/Mars-with-BUAA-CO-extension/actions/workflows/build.yml)

## 这是什么？

这是一个魔改版 Mars，专用于 BUAA 的计算机组成实验。

基于 [Mars 4.5](http://courses.missouristate.edu/KenVollmar/MARS/) 开发

## 与原版有什么不同？

1. 支持输出课程实验要求 CPU 输出的内容，因此能方便与自己的 CPU 进行测试/对拍。
2. 新增支持：加载额外的指令！
3. 重大更新：在 0.4.0 以后，所有新增内容可在图形界面使用！(全都在 `Setting` 的最下方)

## 如何使用？

新扩展的功能只支持在命令行使用。

在原版基础上新增命令行指令如下（在命令行界面输出 `java -jar Mars.jar h` 也能快速查看原版指令和新增指令的提示信息）（指令均不区分大小写）：

1. `coERR`：将本扩展打印的任何内容（扩展版本信息除外）打印到 `stderr` 而非默认的 `stdout`
2. `coL1`：打印寄存器修改和内存修改信息，与 `P4` 要求相同
3. `coL2`：打印额外信息，方便逐步查错和调试
4. `mc CompactLargeText`: 在原版 `mc CompactDataAtZero` 的基础上支持多达 $4096$ 条 $32$ 位机器码（此设置可在 `GUI` 界面使用）
5. `ig`：忽略全部算术溢出
6. `cl <class>`：加载 `.class` 文件以支持额外的指令。
> 请务必把 `.class` 文件和 `Mars.jar` 放在相同目录下。
> 
> 若要创建受支持的 `.class` 文件，你的 `class` 必须实现 `mars.mips.instructions.AdditionalInstruction` 接口。详细示例请见源码中的 `bhelbal.java`。
>
> [详细教程](#自定义额外指令教程)。

### 运行示例

前往 [release](https://GitHub.com/Toby-Shi-cloud/Mars-with-BUAA-CO-extension/releases/) 下载 `Mars_CO.jar` 和 `Mars_CO_example.zip`，然后在命令行运行：

```sh
java -jar mars.jar testcode.asm mc CompactLargeText coL1 cl bhelbal.class ig
```

## 注意事项

根据计组实验要求，建议搭配指令 `mc CompactLargeText` 使用

另外，若要禁用版本信息，请使用指令 `nc`

## 自定义额外指令教程

### 准备工作

1. 如要进行指令扩展，建议下载本仓库源码（也可以只下载 jar，若只下载 jar，需要在编译时添加本 jar 作为依赖）
2. 在根目录（与 Mars.java 同级或与 Mars.jar 同级）下创建一个新 java 类，类名应该为指令的名字。例如，若要创建指令 `bhelbal`，则类名应该为 `bhelbal`，文件名为 `bhelbal.java`。

### 编写代码

1. 你的类必须实现接口 `AdditionalInstruction`，如果你的类是跳转指令，还需要继承 `BranchOperation`。
2. `AdditionalInstruction` 要求你实现 5 个方法: `simulate`, `getExample`, `getDescription`, `getInstructionFormat`, `getOperationMask`。
   1. `void simulate(ProgramStatement statement) throws ProcessingException` 方法是指令的具体实现，你需要在这里实现指令的功能。
    > 参数 `statement` 包含了本条指令的信息，一般情况下，你只需要使用到 `int[] getOperands()` 和 `int getOperand(int)` 方法，即获取所有操作数，和获取某个操作数（获取到的是寄存器编号，使用 `RegisterFile.getValue(int)` 方法可以获取寄存器的值）。  
    > 如果你需要进行跳转，`BranchOperation` 中提供了多个方法可以使用: `void processBranch(int displacement)` 方法采用 `displacement` 相对地址寻址，`void processJump(int targetAddress)` 方法采用 `targetAddress` 绝对地址寻址，`void processReturnAddress(int register)` 则是用于需要 link 的指令，将返回地址存入指定编号的寄存器中。这些指令都会自动根据设置处理延迟槽。
   2. `String getExample()` 方法用于在 Mars 图形化界面中展示示例。直接 return 一个指令使用的示例字符串即可。
   3. `String getDescription()` 方法用于在 Mars 图形化界面中显示指令详细介绍。直接 return 一个字符串即可。
   4. `BasicInstructionFormat getInstructionFormat()` 方法用于标识你的指令的类型，目前有 `R_FORMAT`, `I_FORMAT`, `J_FORMAT`, `I_BRANCH_FORMAT` 可选。
   5. `String getOperationMask()` 方法用于标识你的指令的机器码组成。需要返回一个包括 32 位的字符串，其中指令机器码一定为 0/1 的地方填上 0/1，而操作数的地方填上 `f`/`s`/`t`，分别代表第一个/第二/第三操作数，另外还要在机器码的不同部分直接填上空格分隔。例如，`add $t1,$t2,$t3` 指令的机器码组成为 `000000 sssss ttttt fffff 00000 100000`，这里 `f`/`s`/`t` 就分别代表 `$t1`/`$t2`/`$t3` 在机器码中的位置。

### 编译

1. 如果你下载了源代码，直接在根目录下执行 `javac -encoding UTF-8 <你的类名>.java`，编译你的类。
2. 如果你没有下载源代码，在根目录下执行 `javac -encoding UTF-8 -cp Mars.jar <你的类名>.java`，编译你的类。

### 使用

1. 将你的类和 Mars.jar 放在同一目录下。
2. 在命令行使用 Mars 时，加入指令使用 `cl <你的类名>` 即可使用你的指令。
3. 在图形化界面使用 Mars 时，点击 `Settings` -> `Load Instruction`，选择你的类，即可使用你的指令。

## 版权声明

请务必遵守[原版 Mars 版权声明](MARSlicense.txt)。

本扩展和原版一致使用 MIT 协议。
