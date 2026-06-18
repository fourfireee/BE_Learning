// 一个最小的 Maven 项目入口：演示如何使用从中央仓库拉下来的第三方依赖。
// 包名 com.example 必须和目录结构 src/main/java/com/example 对应。
package com.example;

// 来自 pom.xml 里声明的 commons-lang3 依赖，由 Maven 自动下载
import org.apache.commons.lang3.StringUtils;

public class App {
    public static void main(String[] args) {
        String name = "  java maven  ";
        // 调用第三方库的工具方法：去掉首尾空格，并把第一个字母大写
        String pretty = StringUtils.capitalize(name.trim());
        System.out.println("原始: [" + name + "]");
        System.out.println("处理: [" + pretty + "]");
        System.out.println("是否空白: " + StringUtils.isBlank("   "));
    }
}
