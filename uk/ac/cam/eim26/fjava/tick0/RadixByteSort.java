package uk.ac.cam.eim26.fjava.tick0;

/**
 * Radix sort that is replaced with heapsort once blocks are small enough
 */

public class RadixByteSort {
    private static int[] counting = new int[256];
    private static int[] blockBegin = new int[256];
    private static int[] blockPointer = new int[256];
    private static final int THRESHOLD = (1 << 5);

    private static void recSolve(byte[] arr, int L, int R, int rad) {
        if (R - L < THRESHOLD) {
            PartialByteInsertionSort.byteInsertionSort(arr, L, R, rad);
            return;
        }

        int val;

        for (int i = 0; i < 256; i++) {
            counting[i] = 0;
        }

        for (int i = L; i <= R; i++) {
            val = (arr[(i << 2) + rad] & 0xff);

            if (rad == 0) {
                val ^= 128;
            }

            counting[val]++;
        }

        blockBegin[0] = L;
        blockPointer[0] = L;
        for (int i = 1; i < 256; i++) {
            blockBegin[i] = blockBegin[i - 1] + counting[i - 1];
            blockPointer[i] = blockBegin[i];
        }

        int cur = L;
        int curBlock = 0;

        while (cur <= R) {
            while (curBlock < 255 && blockBegin[curBlock + 1] <= cur) {
                curBlock++;
            }

            if (blockPointer[curBlock] > cur) {
                cur = blockPointer[curBlock];
                continue;
            }

            val = (arr[(cur << 2) + rad] & 0xff);

            if (rad == 0) {
                val ^= 128;
            }

            if (blockPointer[val] != cur) {
                ByteUtil.byteSwap(arr, blockPointer[val], cur);
            } else {
                cur++;
            }

            blockPointer[val]++;
        }

        if (rad == 3) {
            return;
        }

        int start = L;
        for (int i = L + 1; i <= R; i++) {
            if ((arr[(i << 2) + rad] & 0xff) != (arr[((i - 1) << 2) + rad] & 0xff)) {
                recSolve(arr, start, i - 1, rad + 1);
                start = i;
            }
        }
        recSolve(arr, start, R, rad + 1);
    }

    public static byte[] sortByteArray(byte[] arr, int len, int rad) {
        recSolve(arr, 0, len - 1, rad);
        return arr;
    }

    public static byte[] sortByteArray(byte[] arr, int len) {
        return sortByteArray(arr, len, 0);
    }
}
