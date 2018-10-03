package uk.ac.cam.eim26.fjava.tick0;

/**
 * This class provides utility functions for manipulation of byte arrays that in reality represent
 * integer arrays (4 bytes = 1 integer)
 */
public class ByteUtil {
    public static boolean isSmaller(byte[] arr, int a, int b) {
        a <<= 2;
        b <<= 2;

        return
                ( ((arr[a] & 0xff) << 24) | ((arr[a+1] & 0xff) << 16) |
                        ((arr[a+2] & 0xff) << 8) | (arr[a+3] & 0xff) ) <
                        ( ((arr[b] & 0xff) << 24) | ((arr[b+1] & 0xff) << 16) |
                                ((arr[b+2] & 0xff) << 8) | (arr[b+3] & 0xff) );
    }

    public static void byteSwap(byte[] arr, int a, int b) {
        a <<= 2;
        b <<= 2;

        byte tmp;

        tmp = arr[a];
        arr[a] = arr[b];
        arr[b] = tmp;

        tmp = arr[a+1];
        arr[a+1] = arr[b+1];
        arr[b+1] = tmp;

        tmp = arr[a+2];
        arr[a+2] = arr[b+2];
        arr[b+2] = tmp;

        tmp = arr[a+3];
        arr[a+3] = arr[b+3];
        arr[b+3] = tmp;
    }
}
