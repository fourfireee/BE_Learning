# Java IO 与文件

- 后端代码经常要读配置、处理上传文件、写临时文件、读写网络流。
- Java IO 的核心心智模型：路径用 `Path`，文件快捷操作用 `Files`，资源用完要关闭。

## Path 和 Files

- 新代码优先用 `java.nio.file.Path` 和 `java.nio.file.Files`，少用老的 `java.io.File`。
- `Path` 表示路径，`Files` 提供读、写、复制、删除、判断存在等静态方法。

```java
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

Path path = Path.of("data.txt");
Files.writeString(path, "hello\n", StandardCharsets.UTF_8);
String text = Files.readString(path, StandardCharsets.UTF_8);
```

- 路径拼接用 `resolve()`，不要手写 `/` 或 `\`，这样跨平台更安全。

```java
Path dir = Path.of("logs");
Path file = dir.resolve("app.log");
```

## 文本 vs 二进制

- 文本文件要考虑字符编码，建议明确写 `StandardCharsets.UTF_8`。
- 二进制文件按 `byte[]` 或 `InputStream` / `OutputStream` 处理，不要当 String 读。

```java
byte[] bytes = Files.readAllBytes(Path.of("image.png"));
Files.write(Path.of("copy.png"), bytes);
```

## 小文件和大文件

- 小文件可以一次性读写：`readString`、`readAllLines`、`writeString`。
- 大文件不要一次性全部读进内存，应该用流式读取。

```java
try (var reader = Files.newBufferedReader(Path.of("large.log"), StandardCharsets.UTF_8)) {
    String line;
    while ((line = reader.readLine()) != null) {
        System.out.println(line);
    }
}
```

- `try-with-resources` 会自动关闭 reader，即使中途抛异常也会关。

## InputStream / OutputStream

- `InputStream` / `OutputStream` 处理字节流，适合文件、网络、上传下载。
- `Reader` / `Writer` 处理字符流，适合文本。
- 类比 C++：它们像一组带缓冲和编码语义的流对象，资源生命周期要明确关闭。

```java
try (var in = Files.newInputStream(Path.of("a.bin"));
     var out = Files.newOutputStream(Path.of("b.bin"))) {
    in.transferTo(out);
}
```

## 资源关闭

- GC 只管内存，不负责及时释放文件句柄、Socket、数据库连接。
- 只要对象实现 `AutoCloseable`，就优先放进 `try (...)` 里。
- 常见需要关闭的资源：文件流、网络连接、数据库连接、压缩流。

## 后端常见坑

- 不信任用户传来的文件名：上传文件要避免 `../../xxx` 这类路径穿越。
- 临时文件要有清理策略，不能无限堆在磁盘。
- 读配置时要区分“文件不存在”和“内容格式错误”，异常信息要能定位问题。
- 日志不要自己手写文件追加，真实项目用日志框架统一管理滚动、级别和格式。

## 可运行示例

- 代码：[`examples/io-files/FileIoDemo.java`](examples/io-files/FileIoDemo.java)
- 演示创建临时目录、写文本、逐行读取、复制二进制内容、自动关闭资源。
- 运行：

```bash
cd Java/examples/io-files
java FileIoDemo.java
```
