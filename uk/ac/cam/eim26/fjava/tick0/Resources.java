package uk.ac.cam.eim26.fjava.tick0;

//TODO - Experiment with blockSize formula

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class simulates global state across all classes in order to reduce memory and
 * pointless argument passing.
 */
public class Resources {
    public static int blockSize;
    public static byte[] arr;
    public static int[] count = new int[256];
    public static int totalSize = 0;

    public static int minValue = Integer.MAX_VALUE;
    public static int maxValue = Integer.MIN_VALUE;

    public static void computeCount(String f) throws IOException {
        int len;
        InputStream d = new FileInputStream(f);
        int val;

        while(true) {
            len = d.read(Resources.arr);

            if (len == -1) {
                break;
            }

            totalSize += len;

            for (int i = 0; i < len; i += 4) {
                Resources.count[ Resources.arr[i]&0xff ]++;

                val = PartialByteHeapSort.bytesToInteger(Resources.arr, i/4);

                minValue = Math.min(minValue, val);
                maxValue = Math.max(maxValue, val);
            }
        }

        System.out.println("Value range " + minValue + " to " + maxValue + " with length " + (maxValue - minValue));

        if (totalSize > 1000000) {
            System.out.println("File size = " + totalSize + " bytes; = " + (totalSize/1000000) + "MB");
        } else {
            System.out.println("File size = " + totalSize + " bytes; = " + (totalSize/1000) + "KB");
        }

        d.close();
    }

    public static void allocateResources(String f) throws IOException {
        long maxMemory = Runtime.getRuntime().maxMemory();
        long usableMemory = (maxMemory / 10) * 6;

        System.out.println("Memory = " + maxMemory + " i.e. " + ((double)(maxMemory) / 1000000.0) + "MB");

        blockSize = (int)(usableMemory / 6);

        System.out.println("Block size = " + blockSize);

        arr = new byte[4 * blockSize];

        computeCount(f);
    }
}
