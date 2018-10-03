package uk.ac.cam.eim26.fjava.tick0;

public class BucketIntegerSort {
    private static final int MAX_BUCKETS = 10000;
    private static final int BUCKET_THRESHOLD = 10;

    private static int[] bucketCounters = new int [MAX_BUCKETS];
    private static int bucketRange;
    private static int bucketSpace;
    private static int buckets;

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
        buckets = len / BUCKET_THRESHOLD;
        bucketRange = (rangeRight - rangeLeft + 1) / buckets + 1;
        bucketSpace = auxArr.length / buckets;

        //System.out.println("[INFO] Attempting bucket sort");

        if (buckets > auxArr.length || buckets > MAX_BUCKETS) {
            System.out.println("[INFO] Bucket sort failed: Too many buckets. Falling back to radix sort");
            return RadixIntegerSort.sortIntArray(arr, len);
        }

        for (int i = 0; i < buckets; i++) {
            bucketCounters[i] = i * bucketSpace;
        }

        for (int i = 0; i < len; i++) {
            int b = (arr[i] - rangeLeft) / bucketRange;

            auxArr[ bucketCounters[b] ] = arr[i];

            if (bucketCounters[b] >= (b+1) * bucketSpace) {
                System.out.println("[INFO] Bucket sort failed: Not distributed enough. Falling back to radix sort");
                return RadixIntegerSort.sortIntArray(arr, len);
            }

            bucketCounters[b]++;
        }

        int ptr = 0;
        for (int i = 0; i < buckets; i++) {
            sortBucket(i, auxArr);

            for (int j = i * bucketSpace; j < bucketCounters[i]; j++) {
                arr[ptr] = auxArr[j];
                ptr++;
            }
        }

        //System.out.println("[INFO] Bucket sort succeeded.");

        return arr;
    }
}
