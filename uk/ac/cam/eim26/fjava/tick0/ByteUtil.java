package uk.ac.cam.eim26.fjava.tick0;

/**
 * Stateless class the provides utility functions for manipulatin integers in 4-byte format.
 */
public class ByteUtil {

    /**
     * Compares two integer values in their 4-byte encoding. Indices given to the function are as if the
     * array is of integers and not bytes (i.e. the integer on index k is formed by the bytes on indices
     * [4k; 4k+3] in the byte array)
     * @param arr The byte array containing the values to be compared
     * @param a The index of the integer tested to be smaller.
     * @param b The index of the integer tested to be larger or equal.
     * @return True if the first integer is smaller and false otherwise.
     */
    public static boolean isSmaller(byte[] arr, int a, int b) {
        a <<= 2;
        b <<= 2;

        return
                ( ((arr[a] & 0xff) << 24) | ((arr[a+1] & 0xff) << 16) |
                        ((arr[a+2] & 0xff) << 8) | (arr[a+3] & 0xff) ) <
                        ( ((arr[b] & 0xff) << 24) | ((arr[b+1] & 0xff) << 16) |
                                ((arr[b+2] & 0xff) << 8) | (arr[b+3] & 0xff) );
    }

    /**
     * Swaps two integers in their 4-byte encoding. Indices given to the function are as if the
     * array is of integers and not bytes (i.e. the integer on index k is formed by the bytes on indices
     * [4k; 4k+3] in the byte array)
     * @param arr The byte array containing the values to be swapped
     * @param a The index of the first integer.
     * @param b The index of the second integer.
     */
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

    /**
     * Converts an integer in its 4-byte encoding to an actual int type. Indices given to the function are as if the
     * array is of integers and not bytes (i.e. the integer on index k is formed by the bytes on indices
     * [4k; 4k+3] in the byte array)
     * @param arr The byte array containing the value
     * @param a The index of the integer to be converted
     * @return An int-type representation of the integer
     */
    public static int bytesToInteger(byte[] arr, int a) {
        a <<= 2;

        return ( ((arr[a] & 0xff) << 24) | ((arr[a+1] & 0xff) << 16) |
                ((arr[a+2] & 0xff) << 8) | (arr[a+3] & 0xff) );
    }
}
