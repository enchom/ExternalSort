package uk.ac.cam.eim26.fjava.tick0;

import java.util.Arrays;

public class BucketIntegerSort {
    private static final int MAX_BUCKETS = 10000;
    private static final int BUCKET_THRESHOLD = 4;

    private static int[] bucketCounters = new int [MAX_BUCKETS];
    private static int bucketRange;
    private static int bucketSpace;
    private static int buckets;

    public static long sortTime = 0;
    public static long prepareTime = 0;
    public static long copyTime = 0;

    private static void sortBucket(int bucket, int[] arr) {
        int start = bucket * bucketSpace;
        int swp;

        for (int i = start + 1; i < bucketCounters[bucket]; i++) {
            for (int j = i; j > start; j--) {
                if (arr[j] < arr[j - 1]) {
                    swp = arr[j];
                    arr[j] = arr[j - 1];
                    arr[j - 1] = swp;
                }
                else {
                    break;
                }
            }
        }
    }

    public static int[] attemptBucketSort(int[] arr, int len, int rangeLeft, int rangeRight, int[] auxArr) {
        int maxBucket = 0;
        int bucketRangeTwoPower;
        long rem;

        rem = System.nanoTime();

        buckets = len / BUCKET_THRESHOLD;
        bucketRange = (rangeRight - rangeLeft + 1) / buckets + 1;
        bucketSpace = auxArr.length / buckets;

        bucketRangeTwoPower = 0;
        while( (1<<bucketRangeTwoPower) < bucketRange ) {
            bucketRangeTwoPower++;
        }
        //System.out.println("[INFO] Attempting bucket sort");

        if (buckets > auxArr.length || buckets > MAX_BUCKETS) {
            System.out.println("[INFO] Bucket sort failed: Too many buckets. Falling back to radix sort");
            return RadixIntegerSort.sortIntArray(arr, len);
        }

        for (int i = 0; i < buckets; i++) {
            bucketCounters[i] = i * bucketSpace;
        }

        for (int i = 0; i < len; i++) {
            int b = (arr[i] - rangeLeft)>>bucketRangeTwoPower;

            auxArr[ bucketCounters[b] ] = arr[i];

            if (bucketCounters[b] >= (b+1) * bucketSpace) {
                System.out.println("[INFO] Bucket sort failed: Not distributed enough. Falling back to radix sort");
                return RadixIntegerSort.sortIntArray(arr, len);
            }

            bucketCounters[b]++;
        }

        prepareTime += System.nanoTime() - rem;

        rem = System.nanoTime();
        int ptr = 0;
        for (int i = 0; i < buckets; i++) {
            //sortBucket(i, auxArr);
            Arrays.sort(auxArr, i * bucketSpace, bucketCounters[i]);

            System.arraycopy(auxArr, i * bucketSpace, arr, ptr, bucketCounters[i] - i * bucketSpace);
            ptr += bucketCounters[i] - i * bucketSpace;

            //maxBucket = Math.max(maxBucket, bucketCounters[i] - i*bucketSpace);
        }
        sortTime += System.nanoTime() - rem;

        //System.out.println("[INFO] Bucket sort succeeded. Maximum bucket was " + maxBucket);

        return arr;
    }
}
