package uk.ac.cam.eim26.fjava.tick0;

/**
 * Bucket sort working on integer array. Tries to use at most 10000 buckets due to memory constraints and falls
 * back to integer radix sort if that's not possible.
 */
public class BucketIntegerSort {
    private static final int MAX_BUCKETS = 10000;
    private static final int BUCKET_THRESHOLD = 4;

    private static int[] bucketCounters;

    public static long sortTime = 0;
    public static long prepareTime = 0;
    public static long copyTime = 0;

    public static int[] attemptBucketSort(int[] arr, int len, int rangeLeft, int rangeRight, int[] auxArr) {
        if (bucketCounters == null) {
            bucketCounters = new int[MAX_BUCKETS];
        }

        int bucketRangeTwoPower;
        long rem;

        rem = System.nanoTime();

        int buckets = len / BUCKET_THRESHOLD;
        int bucketRange = (rangeRight - rangeLeft + 1) / buckets + 1;
        int bucketSpace = auxArr.length / buckets;

        bucketRangeTwoPower = 0;
        while( (1<<bucketRangeTwoPower) < bucketRange ) {
            bucketRangeTwoPower++;
        }

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
            int ind = bucketCounters[b];
            int val = auxArr[ind];

            ind--;
            while(ind >= b * bucketSpace && val < auxArr[ind]) {
                auxArr[ind+1] = auxArr[ind];
                ind--;
            }
            auxArr[ind+1] = val;

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
            System.arraycopy(auxArr, i * bucketSpace, arr, ptr, bucketCounters[i] - i * bucketSpace);
            ptr += bucketCounters[i] - i * bucketSpace;
        }
        sortTime += System.nanoTime() - rem;

        return arr;
    }
}
