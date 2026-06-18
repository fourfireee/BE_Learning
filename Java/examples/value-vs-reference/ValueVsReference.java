// 演示 Java 的“基本类型按值、引用类型按引用”这个核心区别。
// 运行：在本文件所在目录执行  java ValueVsReference.java
public class ValueVsReference {

    public static void main(String[] args) {
        // 1) 基本类型：变量直接存值，赋值是拷贝一份值
        int a = 10;
        int b = a;   // 把 a 的值拷贝给 b
        b = 20;      // 改 b 不会影响 a
        System.out.println("基本类型: a=" + a + ", b=" + b); // a=10, b=20

        // 2) 引用类型：变量存的是指向堆上对象的引用，赋值拷贝的是引用
        int[] arr1 = {1, 2, 3};
        int[] arr2 = arr1;   // arr1 和 arr2 指向同一个数组对象
        arr2[0] = 99;        // 通过 arr2 改，arr1 看到的也是同一个对象
        System.out.println("引用类型: arr1[0]=" + arr1[0]); // 99

        // 3) 方法传参同理：传基本类型是值拷贝，传引用是引用拷贝
        int x = 5;
        tryChangeInt(x);
        System.out.println("传基本类型后: x=" + x); // 仍是 5，方法内改的是副本

        int[] data = {1, 1, 1};
        tryChangeArray(data);
        System.out.println("传引用后: data[0]=" + data[0]); // 变成 100

        // 4) String 是引用类型且不可变，比较内容要用 equals 而不是 ==
        String s1 = "hello";
        String s2 = new String("hello");
        System.out.println("s1 == s2 : " + (s1 == s2));       // false（比的是引用）
        System.out.println("s1.equals(s2) : " + s1.equals(s2)); // true（比的是内容）
    }

    // 形参 v 是实参值的副本，改它不影响外面
    static void tryChangeInt(int v) {
        v = 999;
    }

    // 形参 arr 是实参引用的副本，但指向同一个数组对象，改对象内容外面可见
    static void tryChangeArray(int[] arr) {
        arr[0] = 100;
    }
}
