package uk.ac.cam.eim26.fjava.tick0;

import static uk.ac.cam.eim26.fjava.tick0.PartialByteUtil.byteSwap;

public class RadixIntegerSort {
    private static int[] counting = new int[256];
    private static int[] blockBegin = new int[256];
    private static int[] blockPointer = new int[256];
    private static final int THRESHOLD = (1 << 5);

    private static void integerInsertionSort(int[] arr, int L, int R) {
        int swapVar;

        for (int i = L + 1; i <= R; i++) {
            for (int j = i; j > L; j--) {
                if (arr[j] < arr[j - 1]) {
                    swapVar = arr[j];
                    arr[j] = arr[j - 1];
                    arr[j - 1] = swapVar;
                }
                else {
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
            counting[ (arr[i]>>>shift)&0xff ]++;
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

            val = (arr[cur]>>>shift)&0xff;

            if (blockPointer[val] != cur) {
                swapVar = arr[cur];
                arr[cur] = arr[ blockPointer[val] ];
                arr[ blockPointer[val] ] = swapVar;
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
            if ( ((arr[i]>>>shift)&0xff) != ((arr[i-1]>>>shift)&0xff) ) {
                recSolve(arr, start, i - 1, rad + 1);
                start = i;
            }
        }
        recSolve(arr, start, R, rad + 1);
    }

    public static int[] sortByteArray(int[] arr, int len, int rad) {
        recSolve(arr, 0, len - 1, rad);
        return arr;
    }

    public static int[] sortByteArray(int[] arr, int len) {
        return sortByteArray(arr, len, 0);
    }
}
