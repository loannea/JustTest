package com.loannea.mgr.fork;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

/**
 * {@code CountTask} fork-join
 * @author zjw
 */
public class CountTask extends RecursiveTask<Integer> {

    private static final int THRESHOLD = 2; //阀值
    private int start;
    private int end;

    public CountTask(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    protected Integer compute() {
        int sum = 0;
        boolean canCompute = (end - start) <= THRESHOLD;
        if (canCompute) {
            for (int i = start; i <= end; i++) {
                sum += i;
            }
        } else {
            int middle = (start + end) / 2;
            CountTask leftTask = new CountTask(start, middle);
            CountTask rightTask = new CountTask(middle + 1, end);
            //执行子任务
            leftTask.fork();
            rightTask.fork();
            //等待子任务执行完，并得到其结果
            Integer rightResult = rightTask.join();
            Integer leftResult = leftTask.join();
            //合并子任务
            sum = leftResult + rightResult;
        }
        return sum;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        CountTask countTask = new CountTask(1, 200);
        ForkJoinTask<Integer> forkJoinTask = forkJoinPool.submit(countTask);
        System.out.println(forkJoinTask.get());
    }

}