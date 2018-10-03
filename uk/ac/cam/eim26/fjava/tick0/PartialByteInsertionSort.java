package uk.ac.cam.eim26.fjava.tick0;

import static uk.ac.cam.eim26.fjava.tick0.PartialByteUtil.byteSwap;
import static uk.ac.cam.eim26.fjava.tick0.PartialByteUtil.isSmaller;

/**
 * An insertion sort working on a subarray of consecutive elements
 */
public class PartialByteInsertionSort {
    public static void byteInsertionSort(byte[] arr, int L, int R, int rad) {
        for (int i = L + 1; i <= R; i++) {
            int cur = i - 1;
            while(cur >= L && ByteUtil.isSmaller(arr, cur + 1, cur)) {
                ByteUtil.byteSwap(arr, cur, cur+1);
                cur--;
            }
        }
    }
}
