[![GitHub release](https://img.shields.io/github/release/Toby-Shi-cloud/Mars-with-BUAA-CO-extension.svg)](https://GitHub.com/Toby-Shi-cloud/Mars-with-BUAA-CO-extension/releases/)
[![Build Jar](https://github.com/Toby-Shi-cloud/Mars-with-BUAA-CO-extension/actions/workflows/build.yml/badge.svg)](https://github.com/Toby-Shi-cloud/Mars-with-BUAA-CO-extension/actions/workflows/build.yml)

## 这是什么？

基于 `Mars 4.5` 开发的魔改版，用于 BUAA 的**计算机组成（CO）**实验。核心用途：**把它当作"黄金模型"，输出 CPU 写寄存器/写内存的轨迹，与你自己的 CPU（Logisim/Verilog）对拍**；P7 还支持 **CP0 异常 / 外部中断 / Timer** 的建模，可与 P7 整机对拍。

> 绝大部分新增功能只在**命令行**使用：`java -jar Mars.jar <asm文件> [参数...]`。命令 `java -jar Mars.jar h` 可查看完整参数。参数不区分大小写。
>
> **声明：** P7 相关支持迁移自课程官方 Mars P7（参考官方 P7 版本实现）。

---

## 一、最常用：CO 对拍

把一段 `.asm` 跑成"标准答案轨迹"。最常用的命令：

```sh
# 非 P7（P4–P6）：打印寄存器/内存写、开延迟槽、干净输出
java -jar Mars.jar test.asm nc db mc CompactLargeText coL1

# P7：加 efc（异常/中断处理）；要测外部中断再加 p7irq=
java -jar Mars.jar test.asm nc db mc CompactLargeText efc coL1
java -jar Mars.jar test.asm nc db mc CompactLargeText efc coL1 p7irq=0x3100
```

`coL1` 输出格式（与课程 P4 要求一致，可直接与 testbench 的 `$display` 对拍）：

```
@00003000: $ 1 <= 00000001       # 寄存器写：@PC: $寄存器号 <= 值
@00003004: *00001004 <= 00000002 # 内存写：  @PC: *地址 <= 值
```

> **`coL1` 追踪范围（即与 Verilog testbench 对拍的约定）**：
> - ✅ 追踪：GRF 寄存器写（`$1`–`$31`，`$0` 不追踪）、数据存储器写（字对齐地址 + 全字值）
> - ❌ 不追踪：`hi`/`lo` 内部寄存器写（MDU 内部，testbench 不产生对应事件）、CP0 寄存器写（`mtc0`）、MMIO 区域写（Timer `0x7F00~0x7F1B`、中断响应 `0x7F20`）

### 核心参数（按重要性排序）

| 参数 | 作用 |
|---|---|
| **`coL1`** | **对拍核心**：打印寄存器写 / 内存写（P4 格式）。写 `$0` 不打印 |
| **`mc <config>`** | 内存配置。课程用 **`CompactLargeText`**（text@0x3000、异常入口 0x4180、最多 4096 条指令）；P4–P6 测试数据可能非法时可用 `FixedCompactLargeText`（异常处理段放到用户数据之外，便于单独编写） |
| **`db`** | 启用 MIPS 延迟槽（**P5/P6/P7 必须**；否则跳转/分支后那条指令不会执行，与流水线 CPU 不一致，导致对拍出错） |
| **`efc`** | **启用 P7 异常/中断处理**：CP0（SR/Cause/EPC）建模、异常派发到 0x4180、按 BUAA 语义设置 EPC/BD/EXL/ExcCode、定时器与中断 |
| **`p7irq=0x..,0x..`** | **P7 外部中断调度**：当"已提交 PC"命中列表中的某地址时注入外部中断（HWInt 第 2 位），每个地址只触发一次。会自动启用 `efc`。时序见[下文](#二p7-对拍专题重点) |
| **`nc`** | 不打印版权信息（重定向/管道时更干净） |

### 其他参数

| 参数 | 作用 |
|---|---|
| `coL2` | 调试级输出：逐条打印 `@PC -> 汇编 (机器码)` 及读写，便于单步查错 |
| `coERR` | 把本扩展打印的内容输出到 `stderr`（默认 `stdout`） |
| `ig` | 忽略全部算术溢出（**对拍 P7 溢出异常时不要加**） |
| `a` | 只汇编、不仿真（配合 `dump`） |
| `dump <段> <格式> <文件>` | 导出内存段。导出机器码：`a dump .text HexText code.txt test.asm`；导出内核段：`a dump 0x00004180-0x00004ffc HexText kernel.txt test.asm` |
| `cl <class>` | 加载 `.class` 扩展指令（见[教程](#五自定义额外指令教程)） |
| `cc` / `ccw <除:乘:跳:访存:其他>` | 统计指令并估算周期 / 设置各类指令周期权重（默认 `25:4:2:3:1`，浮点可用）。**注意：是估算，并非某具体流水线的精确周期** |

---

## 二、P7 对拍专题（重点）

`efc` 在原版基础上新增 P7 所需的异常与中断处理：

- **CP0 与异常**：`SR`（IM=bit15:10、EXL=bit1、IE=bit0）、`Cause`（BD=bit31、IP=bit15:10、ExcCode=bit6:2）、`EPC`；异常码 `Int=0, AdEL=4, AdES=5, Syscall=8, RI=10, Ov=12`。异常时设 ExcCode、`EXL←1`、`EPC←`故障 PC（延迟槽则置 BD 且 EPC←PC-4），派发到 **0x4180**；`eret` 恢复 PC=EPC 并清 EXL。支持 PC 未对齐/取指异常检测。这些与标准 BUAA CP0 逐位一致。
- **定时器外设**：Timer0 `0x7F00~0x7F0B`、Timer1 `0x7F10~0x7F1B`（CTRL/PRESET/COUNT 三寄存器，状态机 IDLE→LOAD→CNT→INT）。
- **中断机制**：全局 `HWInt`（bit0=Timer0，bit1=Timer1，bit2=外部中断）经 Cause.IP 检测；中断响应条件 `EXL=0 && IE=1 && (HWInt & IM)≠0`。
- **MMIO**：定时器寄存器与中断响应寄存器 `0x7F20`（写入即清除外部中断标志）均按 MMIO 访问，**不产生内存写轨迹**（与 Verilog 一致）。

### p7irq 中断注入时序（务必看懂"减 4"）

`p7irq=X` 采用**两周期延迟模型**：在 PC=X 处注入中断，但**指令 X 仍会执行**，被推迟的是**下一条 X+4**。

| 周期 | PC | 动作 | 结果 |
|------|-----|------|------|
| N   | X   | 置 `HWInt bit2`；用上一轮 `prevIRQ`(=false) 判断，不触发 | **指令 X 正常执行** |
| N+1 | X+4 | `prevIRQ`=true，满足中断条件，触发异常 | **指令 X+4 被推迟**，`EPC=X+4` |

即：X 执行 → 进异常处理 → `eret` 回到 `EPC=X+4` → **重新执行 X+4**。

若你的 CPU 按 **M 级宏观 PC（macroscopic_pc）** 采样：testbench 在 `macroscopic_pc == target` 时推迟 `target`（`EPC=target`）。要让两端推迟**同一条指令**、EPC 一致：

> ⚠️ **testbench 的 `target_pc` = MARS 的 `p7irq` 地址 + 4**（即 MARS `p7irq` = 目标地址 − 4）。
>
> 例：要让 `0x00400010` 被推迟执行 → MARS 用 `p7irq=0x0040000c`（0x...0c 执行、0x...10 推迟）。

### 异常处理程序约定（先清中断、再读 Cause）

外部中断需由程序写 **0x7F20** 来响应/清除，否则 testbench 会持续拉高 `interrupt` 造成中断风暴。**关键顺序：先写 0x7F20，再读 Cause**——本 Mars 在响应外部中断异常（ExcCode=0）时会清除外部 IP 位（HWInt bit2），但 Verilog testbench 要等程序写 0x7F20 后 `interrupt` 才落下；若先读 Cause，两端 IP 位会不一致导致对拍差异。推荐统一处理程序：

```mips
.ktext 0x4180
    ori  $k0, $0, 0x7f20    # 先清外部中断
    sw   $0, 0($k0)
    mfc0 $k0, $13           # 再读 Cause（此时两端 IP 都已清）
    andi $k1, $k0, 0x7c     # 取 ExcCode
    beq  $k1, $0, _ret      # ExcCode==0 → 外部中断：直接返回（重执行被推迟指令）
    nop
    mfc0 $k0, $14           # 其他异常：EPC += 4 跳过出错指令
    addi $k0, $k0, 4
    mtc0 $k0, $14
_ret:
    eret
```

### 两点重要差异

1. **复位 SR 差异**：本 Mars 复位 `SR=0x0000FF11`（IE=1、IM 全开），典型 Verilog CPU 复位 `SR=0`。对拍程序应在开头**显式设置 SR**（如 `ori $k0,$0,0x1001; mtc0 $k0,$12`）让两端一致后再触发中断。
2. **Timer 中断不易对拍**：本 Mars 的 Timer 按"每条指令"推进，Verilog 的 Timer 按"每个时钟周期"推进，二者计数无法对应。定时器中断的精确时序对拍不可行，请使用外部中断（`p7irq`）来测试中断机制。

---

## 四、运行示例

前往 [release](https://GitHub.com/Toby-Shi-cloud/Mars-with-BUAA-CO-extension/releases/) 下载 `Mars_CO.jar` 与 `Mars_CO_example.zip`：

```sh
java -jar Mars.jar testcode.asm mc CompactLargeText coL1 cl behlbal.class ig
```

> 0.4.0 之后，绝大部分新增功能也能在图形界面使用（集中在 `Setting` 最下方）。

---

## 五、自定义额外指令教程
> 若不想自行编写代码、需使用课程组提供的 `.class`，详见下方"使用课程组指令"。

### 准备工作

1. 如要扩展指令，建议下载本仓库源码（也可只下载 jar，编译时把本 jar 作为依赖）。
2. 在根目录（与 `Mars.java` 或 `Mars.jar` 同级）创建一个 Java 类，类名即指令名。例如指令 `behlbal` → 文件 `behlbal.java`。

### 编写代码

1. 类必须实现接口 `AdditionalInstruction`；若是跳转指令，还需实现 `BranchOperation` 里的方法（无需继承）。
2. `AdditionalInstruction` 要求实现 5 个方法：`simulate`、`getTemplate`、`getDescription`、`getFormatStr`、`getEncoding`。
   1. `void simulate(ProgramStatement statement) throws ProcessingException`：指令的具体实现。一般只需用 `int[] getOperands()` 和 `int getOperand(int)`（得到寄存器编号，用 `RegisterFile.getValue(int)` 取值）。需要跳转时，`BranchOperation` 提供 `processBranch(int displacement)`（相对寻址）、`processJump(int targetAddress)`（绝对寻址）、`processReturnAddress(int register)`（link，存返回地址）；它们会自动按设置处理延迟槽。
   2. `String getTemplate()`：图形界面里展示的示例字符串。
   3. `String getDescription()`：图形界面里显示的详细介绍。
   4. `String getFormatStr()`：指令类型，`R`/`I`/`J`/`B` 之一。
   5. `String getEncoding()`：32 位机器码组成。固定 0/1 处填 0/1，操作数处填 `f`/`s`/`t`（第 1/2/3 操作数），各部分用空格分隔。例如 `add $t1,$t2,$t3` → `000000 sssss ttttt fffff 00000 100000`。

### 编译

1. 有源码：根目录执行 `javac -encoding UTF-8 -cp ./ <类名>.java`。
2. 仅有 jar：`javac -encoding UTF-8 -cp Mars.jar <类名>.java`。

### 使用

1. 把你的类与 `Mars.jar` 放同一目录。
2. 命令行：加 `cl <类名>`。
3. 图形界面：`Settings` → `Load Instruction` 选择你的类。

## 六、使用课程组指令教程

1. 下载课程组提供的 `.class`，与 `Mars.jar` 放在**同一目录**。
2. 命令行加 `cl <类名>`，或图形界面 `Settings` → `Load Instruction` 选择。

> 加载、解析 class 部分的代码由 fernflower 工具协助完成。fernflower 工具的作者于 2024 年 10 月 20 日与世长辞，请允许我在此献上崇高的敬意。

## 七、周期计数器（Cycles Counter）

扩展工具，菜单 `Tools → Cycles Counter`：实时统计各类指令执行次数并计算周期数与 CPI。使用前先 `Connect to MIPS`，运行后查看；可改各类指令周期，下次统计生效；每轮结束需手动 `Reset` 清空。

![运行示例](images/CyclesCounter.png)

## 版权声明

请务必遵守[原版 Mars 版权声明](MARSlicense.txt)。本扩展和原版一致使用 MIT 协议。

---

## 附：与官方 P7 Mars / 课程规范的差异及注意事项

本 Mars 基于官方课程组提供的 P7 Mars 改造，新增了 trace 输出、中断调度等能力。

### CP0 模块

| 项目 | 官方 P7 Mars | 本 Mars | 说明 |
|------|-------------|---------|------|
| SR 复位值 | `0x00000000`（IE=0, IM=0, EXL=0） | `0x0000FF11`（IE=1, IM 全开, EXL=0） | 本 Mars 预设中断全开，方便直接触发外部中断。对拍程序开头须显式 `mtc0 $t0, $12` 统一两端 SR 值 |
| `updateCause()` | 有 | 有（实现一致） | 每个指令周期调用，从 `HWInt` 刷新 Cause.IP[15:10] |
| `isIter()` | 有 | 有（实现一致） | 中断响应条件：`(Cause & Status & 0xFC00)≠0 && EXL=0 && IE=1` |
| CP0 debug 打印 | `System.err.println(... change to ...)` | 已移除 | 无功能影响 |
| PRId 寄存器 | 无 | 无 | 均未实现；官方 P7 Mars 和本 Mars 都只有 SR/Cause/EPC/Vaddr |

### 中断机制

| 项目 | 官方 P7 Mars | 本 Mars | 说明 |
|------|-------------|---------|------|
| HWInt 存储 | `Simulator.IRQ` / `Simulator.tmp`（static 变量） | `Globals.HWInt`（static int） | 本 Mars 改用单一 `HWInt` 变量，bit0=Timer0, bit1=Timer1, bit2=外部中断 |
| 外部中断调度 | 不支持 | `p7irq=0x...,0x...` 参数 | 本 Mars 新增。在指定 PC 处注入 HWInt bit2，每个地址触发一次。`p7irq` 自动启用 `efc` |
| 中断两周期延迟 | 相同（`prevIRQ`/`tmp` 机制） | `takeInterrupt && irqNow` 双重校验 | 防止 p7irq 注入周期的指令通过 `mtc0` 修改 IE/IM 后错误触发中断 |
| 外部中断清除 | 不支持（官方无 p7irq） | 0x7F20 写入清除 HWInt bit2；Int 异常 (ExcCode=0) 入口时也清除 bit2 | Verilog testbench 仅靠 CPU 写 0x7F20 清除 `interrupt` 信号；两端清除时序不同，处理程序须先写 0x7F20 后读 Cause |

### 异常处理

| 项目 | 官方 P7 Mars | 本 Mars | 说明 |
|------|-------------|---------|------|
| 取指异常检测 | 仅在初始 fetch 时检查 | 额外在每次 fetch 前检查 PC 对齐和范围 | `INSTRUCTION_EXCEPTION_LOAD`（ExcCode=-4，内部用）→ EPC=PC（不-4）；本 Mars 更严格 |
| 异常码 | 标准 MIPS（0=Int, 4=AdEL, 5=AdES, 8=Syscall, 10=RI, 12=Ov） | 相同 |  |
| BD 位处理 | 有（DelayedBranch.isTrydelay） | 有（逻辑一致） | BD=1 时 EPC=故障指令地址−4（分支指令地址） |
| eret | `PC=EPC; EXL=0`（无延迟槽） | 相同 | 通过 `setProgramCounter` 直接跳转，不经过延迟分支机制 |
| EXL=1 时的内部异常 | 阻止（仅在 `!EXL` 时检查中断；内部异常通过 ProcessingException 自然阻止） | 不阻止（仍抛出 ProcessingException；但 handler 中通常无异常代码） | 边缘情况，正常对拍不触发 |

### Timer 外设

| 项目 | `P7_standard_timer_2019.v` | 本 Mars | 说明 |
|------|---------------------------|---------|------|
| 状态机 | IDLE→LOAD→CNT→INT→(回 IDLE) | 相同 | |
| Mode 00 (ctrl[2:1]=00) | INT 状态清零 ctrl[0]，IRQ 保持，定时器停止 | 相同 | |
| Mode 01/10/11 | INT 状态仅清零 IRQ，**ctrl[0] 保持**，回 IDLE 后自动重启 | 相同 | 修复前 Mode 10/11 错误清零了 ctrl[0] |
| 时钟模型 | `always @(posedge clk)` | `update()` 每指令周期调用一次 | Timer 中断时序无法精确对拍，应使用 `p7irq` 测试中断 |
| MMIO 写抑制 tick | 无（`WE` 为 1 时不执行 case 分支） | `setEnable(false)` 防止同周期 tick | 保守设计，等价于官方 Timer `else if (WE)` 分支跳过状态机 case |
| COUNT 寄存器 | 只读 | 可写（`updateRegister(COUNT, val)` 直接设值） | 边缘情况；测试程序不应写 COUNT 寄存器 |
| IRQ 输出 | `assign IRQ = ctrl[3] & _IRQ` | `updateIRQ()`：`IRQ≠0 && (CTRL&8)≠0 → HWInt` | 逻辑等价 |
| CTRL 写入掩码 | `{28'h0, Din[3:0]}`（只取低 4 位） | `val & 0xF`（相同） | |

### MMIO 地址空间

| 地址范围 | 课程 Tutorial / 系统桥 | 本 Mars | 说明 |
|---------|----------------------|---------|------|
| `0x0000_0000 ~ 0x0000_2FFF` | 数据存储器 | 数据段（`CompactLargeText`/`FixedCompactLargeText`） | 一致 |
| `0x0000_3000 ~ 0x0000_6FFF` | 指令存储器 | 文本段（`CompactLargeText`: 上限 0x6FFC） | 一致 |
| `0x0000_4180` | 异常处理入口 | `Memory.exceptionHandlerAddress` | 一致 |
| `0x0000_7F00 ~ 0x0000_7F0B` | Timer0（CTRL/PRESET/COUNT） | 同 | 一致 |
| `0x0000_7F10 ~ 0x0000_7F1B` | Timer1 | 同 | 一致 |
| `0x0000_7F20 ~ 0x0000_7F23` | 中断发生器响应 | 同；写入清除 HWInt bit2 | 一致 |

### Trace 输出

| 项目 | 官方 P7 Mars | 本 Mars | 说明 |
|------|-------------|---------|------|
| coL1 trace | 不支持 | `@PC: $reg <= value` / `@PC: *addr <= value` | 本 Mars 新增 |
| GRF $0 写入 | — | 不输出 | 与 testbench `w_grf_addr != 0` 过滤一致 |
| hi/lo 写入 | — | 不输出（$33/$34） | testbench 不追踪 MDU 内部寄存器 |
| CP0 写入 | — | 不输出 | mtc0 不可见于 testbench `$display` |
| MMIO 写入 | — | 不输出（Timer 0x7Fxx、中断响应 0x7F20） | testbench 的 DM 数组 `fixed_addr >> 2 < 4096` 保护同理 |
| 内存地址格式 | — | 字对齐（`addr & ~0x3`） | testbench `fixed_addr = m_data_addr & 32'hfffffffc` 一致 |
| 内存值格式 | — | 全字值（`outputValue`，含 byte-enable 拼合后的完整字） | testbench `fixed_wdata` 同理 |

### 扩展参数

| 参数 | 来源 | 说明 |
|------|------|------|
| `coL1` / `coL2` / `coERR` | 本 Mars 新增 | trace 输出和重定向 |
| `efc` | 本 Mars 新增（参考官方 P7 Mars 的 `SettingsExceptionForCourse` GUI 开关，改造为命令行参数） | 启用全部 P7 异常/中断/CP0/定时器处理 |
| `p7irq=0x..,0x..` | 本 Mars 新增 | 外部中断调度；自动启用 `efc` |
| `ig` | 本 Mars 新增 | 忽略算术溢出（对拍时通常不加） |
| `cl <class>` | 本 Mars 新增 | 加载额外指令 |
| `cc` / `ccw` | 本 Mars 新增 | 指令周期统计 |