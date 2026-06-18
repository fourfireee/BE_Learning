// 演示 Path、Files、文本读写、流式读取和自动关闭资源。
// 运行：在本文件所在目录执行  java FileIoDemo.java
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileIoDemo {
    public static void main(String[] args) throws Exception {
        // 创建一个临时目录，避免把示例输出写到源码目录里
        Path dir = Files.createTempDirectory("java-io-demo-");
        Path textFile = dir.resolve("hello.txt");
        Path copyFile = dir.resolve("hello-copy.txt");

        // 小文本文件可以一次性写入，明确使用 UTF-8
        Files.writeString(textFile, "hello\njava\nio\n", StandardCharsets.UTF_8);
        System.out.println("写入文件: " + textFile);

        // 小文本文件可以一次性读出
        String all = Files.readString(textFile, StandardCharsets.UTF_8);
        System.out.println("完整内容:");
        System.out.print(all);

        // 较大文件更适合用 BufferedReader 逐行读，try-with-resources 会自动关闭
        try (var reader = Files.newBufferedReader(textFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("逐行读取: " + line);
            }
        }

        // 字节流复制：文本、图片、压缩包等都可以按字节处理
        try (var in = Files.newInputStream(textFile);
             var out = Files.newOutputStream(copyFile)) {
            in.transferTo(out);
        }
        System.out.println("复制后大小: " + Files.size(copyFile) + " bytes");

        // 示例收尾：删除临时文件。真实服务里通常会有统一的临时文件清理策略
        Files.deleteIfExists(copyFile);
        Files.deleteIfExists(textFile);
        Files.deleteIfExists(dir);
    }
}
