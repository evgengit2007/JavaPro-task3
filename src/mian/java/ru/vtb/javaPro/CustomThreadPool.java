package ru.vtb.javaPro;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class CustomThreadPool {
    private List<Worker> workerList = new ArrayList<>(); // список потоков
    List<Runnable> runnableList = new LinkedList<>(); // список задач
    CountDownLatch countDownLatch; // защелка для определения что все потоки завершились при shutdown
    private AtomicBoolean bShutDown = new AtomicBoolean();

    public CustomThreadPool(int cntThreads) {
        countDownLatch = new CountDownLatch(cntThreads);
        for (int i = 0; i < cntThreads; i++) {
            Worker worker = new Worker();
            worker.setName(String.valueOf(i));
            workerList.add(worker);
            worker.start();
        }
    }

    public class Worker extends Thread {

        @Override
        public void run() {
            int numberThread = Integer.parseInt(this.getName());
            try {
                while (true) {
                    System.out.println("Запуск потока № " + numberThread);
                    synchronized (runnableList) {
                        TimeUnit.SECONDS.sleep(2 * (int) (numberThread * Math.random()));
                        if (runnableList.isEmpty() && !bShutDown.get()) {
                            System.out.println("Работа потока " + numberThread + " завершена по сигналу awaitTermination");
                            countDownLatch.countDown();
                            break;
                        }
                        if (runnableList.isEmpty()) {
                            System.out.println("Попали в wait, поток " + numberThread);
                            runnableList.wait();
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
                System.out.println("Работа потока " + numberThread + " завершена с ошибкой");
                countDownLatch.countDown();
            }
        }
    }

    public void execute(Runnable runnable) {
        if (bShutDown.get()) throw new IllegalStateException("Задачи больше не принимаются, выполнен метод shutdown");
        synchronized (runnableList) {
            runnableList.add(runnable);
            runnableList.notifyAll();
        }
    }

    public void shutdown() {
        System.out.println("Запуск shutdown");
        bShutDown.set(true);
        for (Worker worker: workerList) {
            worker.interrupt();
        }
    }

    public void awaitTermination() throws InterruptedException{
        System.out.println("Запуск awaitTermination");
        countDownLatch.await();
    }
}
