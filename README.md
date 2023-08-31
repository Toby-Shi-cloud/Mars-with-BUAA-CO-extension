[![GitHub release](https://img.shields.io/github/release/Toby-Shi-cloud/Mars-with-BUAA-CO-extension.svg)](https://GitHub.com/Toby-Shi-cloud/Mars-with-BUAA-CO-extension/releases/)
[![Build Jar](https://github.com/Toby-Shi-cloud/Mars-with-BUAA-CO-extension/actions/workflows/build.yml/badge.svg)](https://github.com/Toby-Shi-cloud/Mars-with-BUAA-CO-extension/actions/workflows/build.yml)

## 这是什么？

这是一个魔改版 Mars，专用于 BUAA 的计算机组成实验。

基于 [Mars 4.5](http://courses.missouristate.edu/KenVollmar/MARS/) 开发

## 与原版有什么不同？

1. 支持输出课程实验要求 CPU 输出的内容，因此能方便与自己的 CPU 进行测试/对拍。
2. 新增支持：加载额外的指令！
3. 重大更新：在 0.4.0 以后，所有新增内容可在图形界面使用！

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
> 详细教程可能会在不久后给出，就敬请期待。

### 运行示例

前往 [release](https://GitHub.com/Toby-Shi-cloud/Mars-with-BUAA-CO-extension/releases/) 下载 `Mars_CO.jar` 和 `Mars_CO_example.zip`，然后在命令行运行：

```sh
java -jar mars.jar testcode.asm mc CompactLargeText coL1 cl bhelbal.class ig
```

## 注意事项

根据计组实验要求，建议搭配指令 `mc CompactLargeText` 使用

另外，若要禁用版本信息，请使用指令 `nc`

## 版权声明

请务必遵守[原版 Mars 版权声明](MARSlicense.txt)。

本扩展和原版一致使用 MIT 协议。
