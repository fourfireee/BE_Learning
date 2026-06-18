// 演示三类最常用容器：List、Set、Map。
// 运行：在本文件所在目录执行  java CollectionsDemo.java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollectionsDemo {

    public static void main(String[] args) {
        // 1) List：有序、可重复。变量声明用接口 List，实现用 ArrayList
        List<String> names = new ArrayList<>();
        names.add("Alice");
        names.add("Bob");
        names.add("Alice");          // 允许重复
        System.out.println("List: " + names + ", size=" + names.size());
        System.out.println("第一个: " + names.get(0)); // 用 get(i) 访问

        // 2) Set：自动去重
        Set<String> tags = new HashSet<>();
        tags.add("a");
        tags.add("a");               // 重复添加无效
        tags.add("b");
        System.out.println("Set: " + tags + ", contains a? " + tags.contains("a"));

        // 3) Map：键值对，后端用得最多
        Map<String, Integer> ages = new HashMap<>();
        ages.put("Alice", 18);
        ages.put("Bob", 20);
        System.out.println("Map get Alice: " + ages.get("Alice"));
        // key 不存在时给默认值，避免拿到 null
        System.out.println("getOrDefault: " + ages.getOrDefault("Carol", 0));

        // 遍历 Map 的键值对
        for (Map.Entry<String, Integer> e : ages.entrySet()) {
            System.out.println("  " + e.getKey() + " -> " + e.getValue());
        }

        // 4) 用 Map 做单词计数：merge 让“没有就放、有就累加”一行搞定
        String[] words = {"red", "blue", "red", "red", "blue"};
        Map<String, Integer> count = new HashMap<>();
        for (String w : words) {
            count.merge(w, 1, Integer::sum);
        }
        System.out.println("词频统计: " + count); // {red=3, blue=2}
    }
}
