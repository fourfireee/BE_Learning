// 演示面向对象的三个重点：接口、继承、多态。
// 运行：在本文件所在目录执行  java Polymorphism.java
import java.util.List;

public class Polymorphism {

    public static void main(String[] args) {
        // 用父类型(接口)的引用，指向不同的子类对象
        List<Shape> shapes = List.of(
            new Circle(2.0),
            new Rectangle(3.0, 4.0)
        );

        // 同一句 s.area()，运行时会根据真实对象调用对应实现，这就是多态
        for (Shape s : shapes) {
            System.out.println(s.name() + " 的面积 = " + s.area());
            System.out.println("  描述: " + s.describe());
        }
    }
}

// 接口：定义“能做什么”的契约，不关心怎么实现
interface Shape {
    double area();   // 抽象方法，实现类必须提供
    String name();
    String describe();
}

// 抽象类：提供部分公共实现，把差异留给子类
abstract class BaseShape implements Shape {
    // 模板方法：复用逻辑，内部调用子类实现的 area()
    public String describe() {
        return name() + " area=" + area();
    }
}

class Circle extends BaseShape {
    private final double r;
    Circle(double r) { this.r = r; }

    @Override // 标记重写，写错签名编译器会报错
    public double area() { return Math.PI * r * r; }

    @Override
    public String name() { return "圆"; }
}

class Rectangle extends BaseShape {
    private final double w;
    private final double h;
    Rectangle(double w, double h) { this.w = w; this.h = h; }

    @Override
    public double area() { return w * h; }

    @Override
    public String name() { return "矩形"; }
}
