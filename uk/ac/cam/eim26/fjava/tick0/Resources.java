package uk.ac.cam.eim26.fjava.tick0;

//TODO - Experiment with blockSize formula
//TODO - Remove Resources. class reference

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * This class simulates global state across all classes in order to reduce memory and
 * pointless argument passing.
 */
public class Resources {
    public static int blockSize;
    public static byte[] arr;
    public static int[] count = new int[256];
    public static long totalSize = 0;

    public static int minValue = Integer.MAX_VALUE;
    public static int maxValue = Integer.MIN_VALUE;

    public static int[] lastValue = new int[256];
    public static boolean[] naturelySorted = new boolean[256];
    public static int[] minVals = new int[256];
    public static int[] maxVals = new int[256];
    public static long[] averageValue = new long[256];
    public static int criticals = 0;

    public static void computeCount(String f) throws IOException {
        int len;
        InputStream d = new FileInputStream(f);
        int val;

        for (int i = 0; i < 256; i++) {
            lastValue[i] = Integer.MIN_VALUE;
            naturelySorted[i] = true;
            minVals[i] = Integer.MAX_VALUE;
            maxVals[i] = Integer.MIN_VALUE;
        }

        while (true) {
            len = d.read(Resources.arr);

            if (len == -1) {
                break;
            }

            totalSize += len;

            for (int i = 0; i < len; i += 4) {
                Resources.count[arr[i] & 0xff]++;

                val = PartialByteHeapSort.bytesToInteger(arr, i / 4);

                minValue = Math.min(minValue, val);
                maxValue = Math.max(maxValue, val);

                if (lastValue[arr[i] & 0xff] > val) {
                    naturelySorted[arr[i] & 0xff] = false;
                }

                minVals[ arr[i]&0xff ] = Math.min(minVals[ arr[i]&0xff ], val);
                maxVals[ arr[i]&0xff ] = Math.max(maxVals[ arr[i]&0xff ], val);

                lastValue[Resources.arr[i] & 0xff] = val;
                averageValue[Resources.arr[i] & 0xff] += (long)val;
            }
        }

        for (int i = 0; i < 256; i++) {
            if (count[i] > 0) {
                System.out.println("Byte group " + i + " has naturely_sorted status = " + naturelySorted[i]);
                System.out.println("It also has " + count[i] + " members in the range [" + minVals[i] + ", " + maxVals[i] + "]");
                System.out.println("Average value = " + (averageValue[i] / count[i]));
                System.out.println();
            }

            if (count[i] > 0 && !naturelySorted[i]) {
                criticals++;
            }
        }

        System.out.println("Value range " + minValue + " to " + maxValue + " with length " + (maxValue - minValue));
        System.out.println("Critical bytes = " + criticals);

        d.close();
    }

    public static void allocateResources(String f) throws IOException {
        arr = new byte[4 * blockSize];

        computeCount(f);
    }

    public static void allocateVitalResources(String f) {
        long maxMemory = Runtime.getRuntime().maxMemory();
        long usableMemory = (maxMemory / 10) * 6;

        totalSize = (new File(f)).length() / 4;

        System.out.println("Memory = " + maxMemory + " i.e. " + ((double) (maxMemory) / 1000000.0) + "MB");

        blockSize = (int) (usableMemory / 6);

        System.out.println("Block size = " + blockSize);
    }
}
