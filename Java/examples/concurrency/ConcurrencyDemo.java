// 演示并发：竞态问题、原子类修复、线程池、CompletableFuture 异步。
// 运行：在本文件所在目录执行  java ConcurrencyDemo.java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrencyDemo {

    public static void main(String[] args) throws Exception {
        // 1) 竞态条件：多个线程对普通 int 自增，结果通常小于预期
        UnsafeCounter unsafe = new UnsafeCounter();
        runConcurrently(unsafe::inc);
        System.out.println("不安全计数(期望100000): " + unsafe.get());

        // 2) 用原子类修复：incrementAndGet 是原子操作，无需加锁
        AtomicInteger safe = new AtomicInteger(0);
        runConcurrently(safe::incrementAndGet);
        System.out.println("原子计数(期望100000): " + safe.get());

        // 3) 线程池：复用线程执行任务，别手动 new Thread
        ExecutorService pool = Executors.newFixedThreadPool(3);
        for (int i = 1; i <= 3; i++) {
            int id = i;
            pool.submit(() -> System.out.println("任务" + id + " 在 " + Thread.currentThread().getName()));
        }
        pool.shutdown(); // 不再接收新任务，等已提交的跑完

        // 4) CompletableFuture：异步计算 + 链式处理（类似 JS 的 Promise）
        CompletableFuture<Void> cf = CompletableFuture
            .supplyAsync(() -> 1 + 1)
            .thenApply(sum -> sum * 10)
            .thenAccept(r -> System.out.println("异步结果: " + r)); // 20
        cf.join(); // 等异步完成，避免 main 提前退出
    }

    // 开 10 个线程，每个线程把任务执行 10000 次
    static void runConcurrently(Runnable task) throws InterruptedException {
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 10000; j++) task.run();
            });
            threads[i].start();
        }
        for (Thread t : threads) t.join(); // 等所有线程结束
    }
}

class UnsafeCounter {
    private int count = 0;
    void inc() { count++; } // 非原子，多线程下会丢更新
    int get() { return count; }
}
