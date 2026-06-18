// 演示 Lambda、方法引用、Stream 流式处理。
// 运行：在本文件所在目录执行  java StreamDemo.java
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class StreamDemo {

    public static void main(String[] args) {
        // 1) Lambda + 函数式接口：一段可传递的判断逻辑
        Predicate<Integer> isEven = n -> n % 2 == 0;
        System.out.println("4 是偶数? " + isEven.test(4));

        List<String> names = List.of("Alice", "Bob", "Carol", "Dan", "Eve");

        // 2) Stream 流水线：过滤 -> 转换 -> 排序 -> 收集
        List<String> result = names.stream()
            .filter(name -> name.length() > 3)   // 留下长度>3
            .map(String::toUpperCase)            // 方法引用：转大写
            .sorted()                            // 排序
            .collect(Collectors.toList());       // 终止操作：收集成 List
        System.out.println("处理结果: " + result); // [ALICE, CAROL]

        // 3) reduce 聚合：所有偶数的平方和
        int total = List.of(1, 2, 3, 4, 5, 6).stream()
            .filter(n -> n % 2 == 0)   // 2,4,6
            .map(n -> n * n)           // 4,16,36
            .reduce(0, Integer::sum);  // 56
        System.out.println("偶数平方和: " + total);

        // 4) 分组：按名字首字母分组
        Map<Character, List<String>> byFirst = names.stream()
            .collect(Collectors.groupingBy(name -> name.charAt(0)));
        System.out.println("按首字母分组: " + byFirst);

        // 5) 惰性求值：中间操作不会立刻执行，遇到终止操作才真正处理
        long count = names.stream()
            .filter(name -> {
                System.out.println("  检查: " + name); // 只在终止操作时才打印
                return name.startsWith("A") || name.startsWith("E");
            })
            .count();
        System.out.println("以 A 或 E 开头的个数: " + count);
    }
}
