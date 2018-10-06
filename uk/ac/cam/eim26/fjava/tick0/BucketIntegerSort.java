package uk.ac.cam.eim26.fjava.tick0;

/**
 * Bucket sort working on integer array. Tries to use at most 10000 buckets due to memory constraints and falls
 * back to integer radix sort if that's not possible.
 */
@Deprecated
public class BucketIntegerSort {
    private static final int MAX_BUCKETS = 10000;
    private static final int BUCKET_THRESHOLD = 4;

    private static int[] bucketCounters;

    public static int[] attemptBucketSort(int[] arr, int len, int rangeLeft, int rangeRight, int[] auxArr) {
        if (bucketCounters == null) {
            bucketCounters = new int[MAX_BUCKETS];
        }

        int bucketRangeTwoPower;

        int buckets = len / BUCKET_THRESHOLD;
        int bucketRange = (rangeRight - rangeLeft + 1) / buckets + 1;
        int bucketSpace = auxArr.length / buckets;

        bucketRangeTwoPower = 0;
        while ((1 << bucketRangeTwoPower) < bucketRange) {
            bucketRangeTwoPower++;
        }

        if (buckets > auxArr.length || buckets > MAX_BUCKETS) {
            return RadixIntegerSort.sortIntArray(arr, len);
        }

        for (int i = 0; i < buckets; i++) {
            bucketCounters[i] = i * bucketSpace;
        }

        for (int i = 0; i < len; i++) {
            int b = (arr[i] - rangeLeft) >> bucketRangeTwoPower;

            auxArr[bucketCounters[b]] = arr[i];
            int ind = bucketCounters[b];
            int val = auxArr[ind];

            ind--;
            while (ind >= b * bucketSpace && val < auxArr[ind]) {
                auxArr[ind + 1] = auxArr[ind];
                ind--;
            }
            auxArr[ind + 1] = val;

            if (bucketCounters[b] >= (b + 1) * bucketSpace) {
                return RadixIntegerSort.sortIntArray(arr, len);
            }

            bucketCounters[b]++;
        }

        int ptr = 0;
        for (int i = 0; i < buckets; i++) {
            System.arraycopy(auxArr, i * bucketSpace, arr, ptr, bucketCounters[i] - i * bucketSpace);
            ptr += bucketCounters[i] - i * bucketSpace;
        }

        return arr;
    }
}
