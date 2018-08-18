package uk.ac.cam.eim26.fjava.tick0;

/**
 * A heapsort working on a subarray of consecutive elements
 */
public class PartialByteHeapSort {

    private static byte[] arr;
    private static int len;
    private static int offset;

    private static final boolean isSmaller(int a, int b) {
        a += offset;
        b += offset;

        return ByteUtil.isSmaller(arr, a, b);
    }

    private static final void byteSwap(int a, int b) {
        a += offset;
        b += offset;

        ByteUtil.byteSwap(arr, a, b);
    }

    private static final void byteHeapify(int ind, int len) {
        while(2 * ind + 1 < len) {
            if (2 * ind + 2 >= len) {
                if ( isSmaller(ind, 2 * ind + 1) ) {
                    byteSwap(ind, 2 * ind + 1);
                    ind = 2 * ind + 1;
                }
                else {
                    break;
                }
            }
            else {
                if ( isSmaller(2 * ind + 1, 2 * ind + 2) ) {
                    if ( isSmaller(ind, 2 * ind + 2) ) {
                        byteSwap(ind, 2 * ind + 2);
                        ind = 2 * ind + 2;
                    }
                    else {
                        break;
                    }
                }
                else if ( isSmaller(ind, 2 * ind + 1) ) {
                    byteSwap(ind, 2 * ind + 1);
                    ind = 2 * ind + 1;
                }
                else {
                    break;
                }
            }
        }
    }

    //TODO: Move somewhere more general (some byte utils)
    public static int bytesToInteger(byte[] arr, int a) {
        a <<= 2;

        return ( ((arr[a] & 0xff) << 24) | ((arr[a+1] & 0xff) << 16) |
                ((arr[a+2] & 0xff) << 8) | (arr[a+3] & 0xff) );
    }

    public static void byteHeapSort(byte[] localArr, int L, int R) {
        arr = localArr;
        len = R - L + 1;
        offset = L;

        for (int i = len - 1; i >= 0; i--) {
            byteHeapify(i, len);
        }

        for (int i = len - 1; i > 0; i--) {
            byteSwap(0, i);

            byteHeapify(0, i);
        }

        arr = null; //Don't obstruct GC
    }
}
