package uk.ac.cam.eim26.fjava.tick0;

/**
 * Radix sort for in-memory data. Uses insertion sort when blocks become small.
 * Essentially identical to {@link RadixByteSort} but operates on integers stored in int-type variables.
 */
@Deprecated
public class RadixIntegerSort {
    private static int[] counting;
    private static int[] blockBegin;
    private static int[] blockPointer;
    private static final int THRESHOLD = (1 << 5);

    private static void integerInsertionSort(int[] arr, int L, int R) {
        int swapVar;

        for (int i = L + 1; i <= R; i++) {
            for (int j = i; j > L; j--) {
                if (arr[j] < arr[j - 1]) {
                    swapVar = arr[j];
                    arr[j] = arr[j - 1];
                    arr[j - 1] = swapVar;
                } else {
                    break;
                }
            }
        }
    }

    private static void recSolve(int[] arr, int L, int R, int rad) {
        if (R - L < THRESHOLD) {
            integerInsertionSort(arr, L, R);
            return;
        }

        int val;
        int swapVar;
        int shift = 24 - 8 * rad;

        for (int i = 0; i < 256; i++) {
            counting[i] = 0;
        }

        for (int i = L; i <= R; i++) {
            int bit = (arr[i] >>> shift) & 0xff;

            if (rad == 0) {
                bit ^= 128;
            }

            counting[bit]++;
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

            val = (arr[cur] >>> shift) & 0xff;

            if (rad == 0) {
                val ^= 128;
            }

            if (blockPointer[val] != cur) {
                swapVar = arr[cur];
                arr[cur] = arr[blockPointer[val]];
                arr[blockPointer[val]] = swapVar;
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
            if (((arr[i] >>> shift) & 0xff) != ((arr[i - 1] >>> shift) & 0xff)) {
                recSolve(arr, start, i - 1, rad + 1);
                start = i;
            }
        }
        recSolve(arr, start, R, rad + 1);
    }

    public static void sortIntArray(int[] arr, int len, int rad) {
        if (counting == null) {
            counting = new int[256];
        }
        if (blockBegin == null) {
            blockBegin = new int[256];
        }
        if (blockPointer == null) {
            blockPointer = new int[256];
        }

        recSolve(arr, 0, len - 1, rad);
    }

    public static void sortIntArray(int[] arr, int len) {
        sortIntArray(arr, len, 0);
    }
}
