[![GitHub release](https://img.shields.io/github/release/Toby-Shi-cloud/Mars-with-BUAA-CO-extension.svg)](https://GitHub.com/Toby-Shi-cloud/Mars-with-BUAA-CO-extension/releases/)
[![Build Jar](https://github.com/Toby-Shi-cloud/Mars-with-BUAA-CO-extension/actions/workflows/build.yml/badge.svg)](https://github.com/Toby-Shi-cloud/Mars-with-BUAA-CO-extension/actions/workflows/build.yml)

## 这是什么？

这是一个魔改版 Mars，专用于 BUAA 的计算机组成实验与编译原理实验。

基于 [Mars 4.5](http://courses.missouristate.edu/KenVollmar/MARS/) 开发

## 与原版有什么不同？

1. 支持输出课程实验要求 CPU 输出的内容，因此能方便与自己的 CPU 进行测试/对拍。
2. 新增支持：加载额外的指令！
3. 重大更新：在 0.4.0 以后，所有新增内容可在图形界面使用！(全都在 `Setting` 的最下方)
4. 新增支持：统计不同种类指令的执行次数，并计算周期数！
5. 新增支持：P7 异常与中断处理相关功能。

> **声明：** P7 相关支持迁移自课程官方 Mars P7（参考官方 P7 版本实现）

### P7 异常与中断功能

本扩展在原版 Mars 基础上新增了 P7 所需的异常与中断处理功能，包括：

1. **异常处理增强**：
   - 支持 P7 标准的异常处理流程，包括 BD（Branch Delay）位支持
   - 支持 PC 未对齐/取指异常检测
   - 异常发生时正确设置 EPC 和 CAUSE 寄存器

2. **定时器外设**：
   - Timer0：内存映射地址 `0x7F00~0x7F0B`（包含 CTRL、PRESET、COUNT 三个寄存器）
   - Timer1：内存映射地址 `0x7F10~0x7F1B`
   - 状态机模型：IDLE → LOAD → CNT → INT

3. **中断机制**：
   - 全局变量 `HWInt` 用于管理硬件中断待处理位（bit0 对应 Timer0，bit1 对应 Timer1）
   - 支持通过 Coprocessor0 的 Cause 寄存器 IP 位检测中断
   - 支持中断响应条件判断（Status.EXL=0, Status.IE=1）

4. **内存映射 I/O**：
   - 定时器寄存器通过 MMIO 方式访问
   - 中断响应寄存器地址 `0x7F20`（写入清除中断标志）

## 如何使用？

绝大部分新扩展的功能只支持在命令行使用。

在原版基础上新增命令行指令如下（在命令行界面输出 `java -jar Mars.jar h` 也能快速查看原版指令和新增指令的提示信息）（指令均不区分大小写）：

1. `coERR`：将本扩展打印的任何内容（扩展版本信息除外）打印到 `stderr` 而非默认的 `stdout`
2. `coL1`：打印寄存器修改和内存修改信息，与 `P4` 要求相同
3. `coL2`：打印额外信息，方便逐步查错和调试
4. `mc CompactLargeText`: 在原版 `mc CompactDataAtZero` 的基础上支持多达 $4096$ 条 $32$ 位机器码（此设置可在 `GUI` 界面使用）
5. `mc FixedCompactLargeText`: 针对`P4-P6`使用过超大测试数据但是数据可能并不合法的情况，可以使用这条配置以进行错误处理，这条配置将exception handler放置于userdata之外，以便单独编写代码。
6. `ig`：忽略全部算术溢出
7. `cl <class>`：加载 `.class` 文件以支持额外的指令。
   > 请务必把 `.class` 文件和 `Mars.jar` 放在相同目录下。
   > 若已获取源代码，请把 `.class` 文件和 `Mars.java` 放在相同目录下。
   > 
   > 若要创建受支持的 `.class` 文件，你的 `class` 必须实现 `mars.mips.instructions.InstructionLoad` 接口。详细示例请见源码中的 `bhelbal.java`。
   >
   > [详细教程](#自定义额外指令教程)。

8. `cc`：启用周期计数，会在程序运行结束时打印输出。
9. `ccw 25:4:2:3:1`：设置不同种类指令的周期数，使用 `:` 分隔，分别为除法指令、乘法指令、跳转指令、访存指令、其他指令。支持浮点数，默认值为 `25:4:2:3:1`。
10. `efc`：启用 P7 异常处理模式（Exception for Course），开启后将使用 P7 标准的异常处理流程，包括 BD 位支持和定时器中断。
11. `p7irq=addr1,addr2,...`：设置 P7 外部中断触发的 PC 地址列表，多个地址用逗号分隔。使用此参数会自动启用 P7 异常处理模式。**注意**：该参数的中断注入行为是两周期延迟模型（详见下方 [p7irq 中断注入时序说明](#p7irq-中断注入时序说明)）。

增加一个**拓展工具** `Cycles Counter`，可以实时统计不同种类指令的执行次数，并计算周期数与 CPI。位于菜单 `Tools -> Cycles Counter`。

使用时，需要首先 `Connect to MIPS`，随后运行即可查看结果。你可以更改不同种类指令的周期数，会在下一次统计时生效。每轮运行结束，需要手动点击 `Reset` 清空结果。

![运行示例](images/CyclesCounter.png)


### 运行示例

前往 [release](https://GitHub.com/Toby-Shi-cloud/Mars-with-BUAA-CO-extension/releases/) 下载 `Mars_CO.jar` 和 `Mars_CO_example.zip`，然后在命令行运行：

```sh
java -jar mars.jar testcode.asm mc CompactLargeText coL1 cl behlbal.class ig
```

## 注意事项

根据计组实验要求，建议搭配指令 `mc CompactLargeText` 使用

另外，若要禁用版本信息，请使用指令 `nc`

## p7irq 中断注入时序说明

`p7irq=X` 参数的中断注入采用**两周期延迟模型**，即在地址 X 处注入中断信号，但指令 X 本身会被执行，**指令 X+4 被推迟**。

### 执行时序

MARS 的模拟循环中，中断注入和响应分为两步：

```
周期 N   (PC=X)  : 注入 HWInt bit2 → 检测中断(用上周期状态) → 执行指令 X
周期 N+1 (PC=X+4): 检测中断(本周期生效) → 触发异常 → 指令 X+4 不执行
```

具体来说：

| 周期 | PC | 动作 | 结果 |
|------|-----|------|------|
| N | X | 设置 `HWInt bit2`；用上一轮 `prevIRQ`(=false) 判断，不触发中断 | **指令 X 正常执行** |
| N+1 | X+4 | `prevIRQ`=true，满足中断条件，触发异常 | **指令 X+4 被推迟**，EPC=X |

异常触发后，EPC 指向 X（已执行的指令），CPU 跳转到异常处理程序。`eret` 返回后从 EPC=X 处继续，即重新执行指令 X+4。

### 示例

若要让指令在地址 `0x00400010` 处被推迟执行（即中断发生在该指令处）：
- MARS 使用 `p7irq=0x0040000c`（目标减 4）
- 实际效果：指令 `0x0040000c` 执行，指令 `0x00400010` 被推迟到异常处理返回后

## 自定义额外指令教程
> 若不希望自行编写代码，需要使用课程组提供的 `.class` 文件，详见下方“使用课程组指令”部分说明。
### 准备工作

1. 如要进行指令扩展，建议下载本仓库源码（也可以只下载 jar，若只下载 jar，需要在编译时添加本 jar 作为依赖）
2. 在根目录（与 Mars.java 同级或与 Mars.jar 同级）下创建一个新 java 类，类名应该为指令的名字。例如，若要创建指令 `behlbal`，则类名应该为 `behlbal`，文件名为 `behlbal.java`。

### 编写代码

1. 你的类必须实现接口 `AdditionalInstruction`，如果你的类是跳转指令，还需要实现`BranchOperation`里面的方法（无需继承）。
2. `AdditionalInstruction` 要求你实现 5 个方法: `simulate`, `getTemplate`, `getDescription`, `getFormatStr`, `getEncoding`。
   1. `void simulate(ProgramStatement statement) throws ProcessingException` 方法是指令的具体实现，你需要在这里实现指令的功能。
    > 参数 `statement` 包含了本条指令的信息，一般情况下，你只需要使用到 `int[] getOperands()` 和 `int getOperand(int)` 方法，即获取所有操作数，和获取某个操作数（获取到的是寄存器编号，使用 `RegisterFile.getValue(int)` 方法可以获取寄存器的值）。  
    > 如果你需要进行跳转，`BranchOperation` 中提供了多个方法可以使用: `void processBranch(int displacement)` 方法采用 `displacement` 相对地址寻址，`void processJump(int targetAddress)` 方法采用 `targetAddress` 绝对地址寻址，`void processReturnAddress(int register)` 则是用于需要 link 的指令，将返回地址存入指定编号的寄存器中。这些指令都会自动根据设置处理延迟槽。
   2. `String getTemplate()` 方法用于在 Mars 图形化界面中展示示例。直接 return 一个指令使用的示例字符串即可。
   3. `String getDescription()` 方法用于在 Mars 图形化界面中显示指令详细介绍。直接 return 一个字符串即可。
   4. `String getFormatStr()` 方法用于标识你的指令的类型，目前有 `R`, `I`, `J`, `B` 可选。
   5. `String getEncoding()` 方法用于标识你的指令的机器码组成。需要返回一个包括 32 位的字符串，其中指令机器码一定为 0/1 的地方填上 0/1，而操作数的地方填上 `f`/`s`/`t`，分别代表第一个/第二/第三操作数，另外还要在机器码的不同部分直接填上空格分隔。例如，`add $t1,$t2,$t3` 指令的机器码组成为 `000000 sssss ttttt fffff 00000 100000`，这里 `f`/`s`/`t` 就分别代表 `$t1`/`$t2`/`$t3` 在机器码中的位置。

### 编译

1. 如果你下载了源代码，直接在根目录下执行 `javac -encoding UTF-8 -cp ./ <你的类名>.java`，编译你的类。
2. 如果你没有下载源代码，在根目录下执行 `javac -encoding UTF-8 -cp Mars.jar <你的类名>.java`，编译你的类。

### 使用

1. 将你的类和 Mars.jar 放在同一目录下。
2. 在命令行使用 Mars 时，加入指令使用 `cl <你的类名>` 即可使用你的指令。
3. 在图形化界面使用 Mars 时，点击 `Settings` -> `Load Instruction`，选择你的类，即可使用你的指令。

## 使用课程组指令教程

### 准备工作
1. 下载课程组提供的`.class`文件，并放于某个不相干的文件夹中备用。

> 加载、解析class部分的代码由fernflower工具协助完成。
> 
> fernflower工具的作者于2024年10月20日与世长辞，请允许我在此献上崇高的敬意。

### 使用

1. 将你的类和 Mars.jar 放在**同一目录**下。
2. 在命令行使用 Mars 时，加入指令使用 `cl <你的类名>` 即可使用你的指令。
3. 在图形化界面使用 Mars 时，点击 `Settings` -> `Load Instruction`，选择你的类，即可使用你的指令。

## 版权声明

请务必遵守[原版 Mars 版权声明](MARSlicense.txt)。

本扩展和原版一致使用 MIT 协议。
