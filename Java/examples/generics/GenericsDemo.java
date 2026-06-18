// 演示泛型类、泛型方法、上界，以及类型擦除带来的运行时限制。
// 运行：在本文件所在目录执行  java GenericsDemo.java
import java.util.List;

public class GenericsDemo {

    public static void main(String[] args) {
        // 1) 泛型类：同一套 Box 适配不同类型，且有编译期类型检查
        Box<String> sb = new Box<>();
        sb.set("hello");
        String s = sb.get();   // 取出来就是 String，不需要强制转换
        System.out.println("Box<String> -> " + s);

        // 2) 泛型方法：从列表里取第一个元素
        List<Integer> nums = List.of(10, 20, 30);
        Integer first = firstOf(nums);
        System.out.println("firstOf -> " + first);

        // 3) 上界 <T extends Number>：限定为 Number 子类，才能调用 doubleValue()
        System.out.println("sum -> " + sum(List.of(1, 2, 3, 4))); // 10.0

        // 4) 类型擦除的体现：运行时 Box<String> 和 Box<Integer> 是同一个类
        Box<Integer> ib = new Box<>();
        System.out.println("同一个运行时类? " + (sb.getClass() == ib.getClass())); // true
    }

    // 泛型方法：<T> 声明类型参数
    static <T> T firstOf(List<T> list) {
        return list.get(0);
    }

    // 上界：T 必须是 Number 或其子类
    static <T extends Number> double sum(List<T> list) {
        double total = 0;
        for (T n : list) total += n.doubleValue();
        return total;
    }
}

// 一个能装任意类型的简单泛型容器
class Box<T> {
    private T value;
    void set(T value) { this.value = value; }
    T get() { return value; }
}
