package edu.coursera.parallel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Class wrapping methods for implementing reciprocal array sum in parallel.
 */
public final class ReciprocalArraySum {

    /**
     * Default constructor.
     */
    private ReciprocalArraySum() {
    }

    /**
     * Sequentially compute the sum of the reciprocal values for a given array.
     *
     * @param input Input array
     * @return The sum of the reciprocals of the array input
     */
    protected static double seqArraySum(final double[] input) {
        double sum = 0;

        // Compute sum of reciprocals of array elements
        for (int i = 0; i < input.length; i++) {
            sum += 1 / input[i];
        }

        return sum;
    }

    /**
     * Computes the size of each chunk, given the number of chunks to create
     * across a given number of elements.
     *
     * @param nChunks The number of chunks to create
     * @param nElements The number of elements to chunk across
     * @return The default chunk size
     */
    private static int getChunkSize(final int nChunks, final int nElements) {
        // Integer ceil
        return (nElements + nChunks - 1) / nChunks;
    }

    /**
     * Computes the inclusive element index that the provided chunk starts at,
     * given there are a certain number of chunks.
     *
     * @param chunk The chunk to compute the start of
     * @param nChunks The number of chunks created
     * @param nElements The number of elements to chunk across
     * @return The inclusive index that this chunk starts at in the set of
     *         nElements
     */
    private static int getChunkStartInclusive(final int chunk,
            final int nChunks, final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        return chunk * chunkSize;
    }

    /**
     * Computes the exclusive element index that the provided chunk ends at,
     * given there are a certain number of chunks.
     *
     * @param chunk The chunk to compute the end of
     * @param nChunks The number of chunks created
     * @param nElements The number of elements to chunk across
     * @return The exclusive end index for this chunk
     */
    private static int getChunkEndExclusive(final int chunk, final int nChunks,
            final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        final int end = (chunk + 1) * chunkSize;
        if (end > nElements) {
            return nElements;
        } else {
            return end;
        }
    }

    /**
     * This class stub can be filled in to implement the body of each task
     * created to perform reciprocal array sum in parallel.
     */
    private static class ReciprocalArraySumTask extends RecursiveAction {
        /**
         * Starting index for traversal done by this task.
         */
        private final int startIndexInclusive;
        /**
         * Ending index for traversal done by this task.
         */
        private final int endIndexExclusive;
        /**
         * Input array to reciprocal sum.
         */
        private final double[] input;
        private int numTasks;
        private int number;
        /**
         * Intermediate value produced by this task.
         */
        private double value;

        /**
         * Constructor.
         * @param setStartIndexInclusive Set the starting index to begin
         *        parallel traversal at.
         * @param setEndIndexExclusive Set ending index for parallel traversal.
         * @param setInput Input values
         */
        ReciprocalArraySumTask(final int setStartIndexInclusive,
                final int setEndIndexExclusive, final double[] setInput, final int numTasks, final int number) {
            this.startIndexInclusive = setStartIndexInclusive;
            this.endIndexExclusive = setEndIndexExclusive;
            this.input = setInput;
            this.numTasks = numTasks;
            this.number = number;

            System.out.println("Created task " + number);
        }

        /**
         * Getter for the value produced by this task.
         * @return Value produced by this task
         */
        public double getValue() {
            return value;
        }

        @Override
        protected void compute() {
            if (endIndexExclusive - startIndexInclusive <= getChunkSize(numTasks, input.length)) {
                for (int i = startIndexInclusive; i < endIndexExclusive; i++) {
                    value += 1.0d / input[i];
                }
                return;
            }
            int mid = (endIndexExclusive + startIndexInclusive) / 2;
            ReciprocalArraySumTask leftT = new ReciprocalArraySumTask(startIndexInclusive, mid, input, numTasks, number++);
            ReciprocalArraySumTask rightT = new ReciprocalArraySumTask(mid, endIndexExclusive, input, numTasks, number++);

            invokeAll(leftT, rightT);

            value += leftT.getValue() + rightT.getValue();
        }
    }


    /**
     * TODO: Modify this method to compute the same reciprocal sum as
     * seqArraySum, but use two tasks running in parallel under the Java Fork
     * Join framework. You may assume that the length of the input array is
     * evenly divisible by 2.
     *
     * @param input Input array
     * @return The sum of the reciprocals of the array input
     */
    protected static double parArraySum(final double[] input) {
        assert input.length % 2 == 0;

        double sum = 0;

        sum = parManyTaskArraySum(input, 2);
//        int left = 0;
//        int mid = input.length / 2;
//        int right = input.length;
//
//        ForkJoinPool pool = new ForkJoinPool();
//        List<ComputeReciprocalAction> list = new ArrayList<>();
//        list.add(new ComputeReciprocalAction(input, left, mid));
//        list.add(new ComputeReciprocalAction(input, mid, right));
//        sum = pool.invokeAll(list).stream().mapToDouble(t -> {
//            try {
//                return t.get();
//            } catch (InterruptedException | ExecutionException e) {
//                e.printStackTrace();
//            }
//            return -1;
//        }).sum();


        //sum = ForkJoinTask.invokeAll(list).stream().mapToDouble(t -> t.join()).sum();
        return sum;
    }


    static ForkJoinPool pool = new ForkJoinPool();

    /**
     * TODO: Extend the work you did to implement parArraySum to use a set
     * number of tasks to compute the reciprocal array sum. You may find the
     * above utilities getChunkStartInclusive and getChunkEndExclusive helpful
     * in computing the range of element indices that belong to each chunk.
     *
     * @param input Input array
     * @param numTasks The number of tasks to create
     * @return The sum of the reciprocals of the array input
     */
    protected static double parManyTaskArraySum(final double[] input,
            final int numTasks) {
        double sum = 0;

        System.out.println("New calc");
        ReciprocalArraySumTask task = new ReciprocalArraySumTask(0, input.length, input, numTasks, 1);
        pool.invoke(task);
//        List<ReciprocalArraySumTask> list = new ArrayList<>();
//        int size = numTasks;
//        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(size*10));
//        for (int i = 0; i < size; i++) {
//            ReciprocalArraySumTask task = new ReciprocalArraySumTask(
//                    getChunkStartInclusive(i, size, input.length),
//                    getChunkEndExclusive(i, size, input.length), input, size);
//            list.add(task);
//        }
//        for (ReciprocalArraySumTask t : list) {
//            t.fork();
//        }

        sum = task.getValue();
//        for (ReciprocalArraySumTask t : list) {
//
//            t.join();
//            sum += t.getValue();
//        }

        //ForkJoinPool.commonPool().invoke(task);
        //sum = task.getValue();

        return sum;
    }
}
