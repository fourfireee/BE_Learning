// 演示异常处理：捕获、主动抛出、自定义异常、try-with-resources 自动关资源。
// 运行：在本文件所在目录执行  java ExceptionDemo.java
public class ExceptionDemo {

    public static void main(String[] args) {
        // 1) 捕获异常：try/catch/finally
        try {
            int r = 10 / 0;           // 抛 ArithmeticException
            System.out.println(r);
        } catch (ArithmeticException e) {
            System.out.println("捕获到: " + e.getMessage());
        } finally {
            System.out.println("finally 一定执行");
        }

        // 2) 主动抛出 + 自定义异常
        try {
            setAge(-5);
        } catch (IllegalArgumentException e) {
            System.out.println("参数校验失败: " + e.getMessage());
        }

        try {
            findUser("Tom");
        } catch (UserNotFoundException e) {
            System.out.println("业务异常: " + e.getMessage());
        }

        // 3) try-with-resources：括号里的资源用完自动 close，替代 C++ 的 RAII
        try (FakeResource res = new FakeResource("数据库连接")) {
            res.use();
        }
        // 这里 res 已自动关闭

        System.out.println("程序正常结束");
    }

    static void setAge(int age) {
        if (age < 0) {
            throw new IllegalArgumentException("年龄不能为负: " + age);
        }
    }

    static void findUser(String name) {
        throw new UserNotFoundException(name); // 模拟查不到用户
    }
}

// 自定义 unchecked 异常：继承 RuntimeException，表达业务语义
class UserNotFoundException extends RuntimeException {
    UserNotFoundException(String name) {
        super("找不到用户: " + name);
    }
}

// 实现 AutoCloseable 的资源，才能用在 try-with-resources 里
class FakeResource implements AutoCloseable {
    private final String name;
    FakeResource(String name) {
        this.name = name;
        System.out.println("打开: " + name);
    }
    void use() {
        System.out.println("使用: " + name);
    }
    @Override
    public void close() { // try 块结束后自动调用，即使中途抛异常也会调用
        System.out.println("自动关闭: " + name);
    }
}
