package ru.vtb.javaPro;

public class MainThread {
    public static void main(String[] args) throws InterruptedException{
        CustomThreadPool customThreadPool = new CustomThreadPool(5);
        System.out.println("----Потоки стартовали----");

        for (int i = 0; i < 10; i++) {
            int exec = i;
            customThreadPool.execute(() -> {
                System.out.println("Добавить задачу " + exec + " на исполнение");
            });
        }

        Thread.sleep(2000);
//        customThreadPool.shutdown();

        for (int i = 10; i < 20; i++) {
            int exec = i;
            customThreadPool.execute(() -> {
                System.out.println("2 итерация. Добавить задачу " + exec + " на исполнение");
            });
        }
        customThreadPool.awaitTermination();
    }
}
