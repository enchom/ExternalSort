package uk.ac.cam.eim26.fjava.tick0;

/**
 * This class provides utility functions for manipulation of byte arrays that in reality represent
 * integer arrays (4 bytes = 1 integer)
 * Functions are separated for efficiency
 */
public class PartialByteUtil {

    private static boolean isSmaller0(byte[] arr, int a, int b) {
        return
                ( ((arr[a] & 0xff) << 24) | ((arr[a+1] & 0xff) << 16) |
                        ((arr[a+2] & 0xff) << 8) | (arr[a+3] & 0xff) ) <
                        ( ((arr[b] & 0xff) << 24) | ((arr[b+1] & 0xff) << 16) |
                                ((arr[b+2] & 0xff) << 8) | (arr[b+3] & 0xff) );

    }

    private static boolean isSmaller1(byte[] arr, int a, int b) {
        return
                ( ((arr[a+1] & 0xff) << 16) |
                        ((arr[a+2] & 0xff) << 8) | (arr[a+3] & 0xff) ) <
                        ( ((arr[b+1] & 0xff) << 16) |
                                ((arr[b+2] & 0xff) << 8) | (arr[b+3] & 0xff) );

    }

    private static boolean isSmaller2(byte[] arr, int a, int b) {
        return
                ( ((arr[a+2] & 0xff) << 8) | (arr[a+3] & 0xff) ) <
                        ( ((arr[b+2] & 0xff) << 8) | (arr[b+3] & 0xff) );

    }

    private static boolean isSmaller3(byte[] arr, int a, int b) {
        return ( (arr[a+3] & 0xff) < (arr[b+3] & 0xff) );

    }

    public static boolean isSmaller(byte[] arr, int a, int b, int rad) {
        a <<= 2;
        b <<= 2;

        switch (rad) {
            case 0:
                return isSmaller0(arr, a, b);
            case 1:
                return isSmaller1(arr, a, b);
            case 2:
                return isSmaller2(arr, a, b);
            case 3:
                return isSmaller3(arr, a, b);
            default:
                return isSmaller0(arr, a, b);
        }
    }

    private static void byteSwap0(byte[] arr, int a, int b) {
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

    private static void byteSwap1(byte[] arr, int a, int b) {
        byte tmp;

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

    private static void byteSwap2(byte[] arr, int a, int b) {
        byte tmp;

        tmp = arr[a+2];
        arr[a+2] = arr[b+2];
        arr[b+2] = tmp;

        tmp = arr[a+3];
        arr[a+3] = arr[b+3];
        arr[b+3] = tmp;
    }

    private static void byteSwap3(byte[] arr, int a, int b) {
        byte tmp;

        tmp = arr[a+3];
        arr[a+3] = arr[b+3];
        arr[b+3] = tmp;
    }


    public static void byteSwap(byte[] arr, int a, int b, int rad) {
        a <<= 2;
        b <<= 2;

        switch (rad) {
            case 0:
                byteSwap0(arr, a, b);
                break;
            case 1:
                byteSwap1(arr, a, b);
                break;
            case 2:
                byteSwap2(arr, a, b);
                break;
            case 3:
                byteSwap3(arr, a, b);
                break;
            default:
                byteSwap0(arr, a, b);
        }
    }
}
