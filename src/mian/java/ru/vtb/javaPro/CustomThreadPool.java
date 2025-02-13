package ru.vtb.javaPro;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CustomThreadPool {
    private List<Worker> workerList = new ArrayList<>(); // список потоков
    List<Runnable> runnableList = new LinkedList<>(); // список задач
    CountDownLatch countDownLatch; // защелка для определения что все потоки завершились при shutdown
    private AtomicBoolean bShutDown = new AtomicBoolean();

//    private final static Object monitor = new Object(); // монитор, по которому будем синхрониизироваться

    public CustomThreadPool(int cntThreads) {
        countDownLatch = new CountDownLatch(cntThreads);
        for (int i = 0; i < cntThreads; i++) {
            Worker worker = new Worker(i);
            workerList.add(worker);
            worker.start();
        }
    }

    public class Worker extends Thread {
        private int numberThread;

        public Worker(int numberThread) {
            this.numberThread = numberThread;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    System.out.println("Запуск потока № " + numberThread);
                    synchronized (workerList) {
                        TimeUnit.SECONDS.sleep(2 * (int) (numberThread * Math.random()));
                        if (runnableList.isEmpty()) {
                            System.out.println("Попали в wait");
                            countDownLatch.countDown();
                            workerList.wait();
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
        if (bShutDown.get()) throw new IllegalStateException("Задачи больше не принимаются, выполнен метод shutdown");
        synchronized (workerList) {
            runnableList.add(runnable);
            workerList.notifyAll();
        }
    }

    public void shutdown() {
        System.out.println("Запуск shutdown");
        bShutDown.set(true);
        for (Worker worker: workerList) {
            worker.interrupt();
        }
    }

    public boolean awaitTermination() {
        System.out.println("Запуск awaitTermination");
        try {
            countDownLatch.await();
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
}
