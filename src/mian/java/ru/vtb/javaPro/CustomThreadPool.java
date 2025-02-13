package ru.vtb.javaPro;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CustomThreadPool {
    List<Worker> workerList = new ArrayList<>(); // список потоков
    List<Runnable> runnableList = new LinkedList<>(); // список задач
    CountDownLatch countDownLatch; // защелка для определения что все потоки завершились при shutdown
    private volatile boolean bShutDown = false;

    private final static Object monitor = new Object(); // монитор, по которому будем синхрониизироваться

    public CustomThreadPool(int cntThreads) {
        countDownLatch = new CountDownLatch(cntThreads);
        for (int i = 0; i < cntThreads; i++) {
            Worker worker = new Worker(i);
            workerList.add(worker);
            worker.start();
        }
    }

    public class Worker extends Thread {
        int numberThread;

        public Worker(int numberThread) {
            this.numberThread = numberThread;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    System.out.println("Запуск потока № " + numberThread);
                    synchronized (monitor) {
                        TimeUnit.SECONDS.sleep(2 * (int) (numberThread * Math.random()));
                        if (runnableList.isEmpty()) {
                            System.out.println("Попали в wait");
                            countDownLatch.countDown();
                            monitor.wait();
                        } else {
                            Runnable runnable = runnableList.get(0);
                            if (runnable != null) {
                                runnable.run();
                                runnableList.remove(0);
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("Работа потока " + numberThread + " завершена");
            }
        }
    }

    public void execute(Runnable runnable) {
        if (bShutDown) throw new IllegalStateException("Задачи больше не принимаются, выполнен метод shutdown");
        synchronized (monitor) {
            runnableList.add(runnable);
            monitor.notifyAll();
        }
    }

    public void shutdown() {
        System.out.println("Запуск shutdown");
        bShutDown = true;
        for (Worker worker: workerList) {
            worker.interrupt();
        }
    }

    public void awaitTermination() {
        System.out.println("Запуск awaitTermination");
        shutdown();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
