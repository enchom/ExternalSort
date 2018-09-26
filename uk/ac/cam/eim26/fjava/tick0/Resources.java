package uk.ac.cam.eim26.fjava.tick0;

//TODO - Experiment with blockSize formula
//TODO - Remove Resources. class reference

import java.io.*;
import java.util.ArrayList;
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
    public static int criticals = 0;

    //public static int pairCount[][] = new int[256][256];

    //public static int auxMinInd[] = new int[256];
    //public static int auxMaxInd[] = new int[256];

    public static ArrayList<Integer> leftEnds = new ArrayList<>();
    public static ArrayList<Boolean> sorted = new ArrayList<>();
    public static ArrayList<Boolean> reversed = new ArrayList<>();
    public static ArrayList<Integer> smallestValue = new ArrayList<>();
    public static ArrayList<Integer> largestValue = new ArrayList<>();
    public static ArrayList<Integer> cornerCases = new ArrayList<>();
    public static boolean passedCorners = false;
    public static int cornerEnding = 0;

    public static final int CORNER_LIMIT_COUNT = 100000;
    public static final long MAX_PAD = 100000;
    public static final long BLOCK_SEPPARATOR = 20000000;
    public static boolean specialStructure = true;
    public static boolean countingSortStructure = true;

    private static boolean cornerNumber(int num) {
        return (num < Integer.MIN_VALUE + MAX_PAD || num > Integer.MAX_VALUE - MAX_PAD);
    }

    public static void computeCount(String f) throws IOException {
        int len;
        InputStream d = new FileInputStream(f);
        int val;

        for (int i = 0; i < 256; i++) {
            lastValue[i] = Integer.MIN_VALUE;
        }

        int lastNumber = 0;
        boolean isSorted = true, isReversed = true;
        int smallValue = 0, largeValue = 0;
        int index = 0;

        long saveTime = System.nanoTime();
        while (true) {
            len = d.read(Resources.arr);

            if (len == -1) {
                break;
            }

            totalSize += len;

            if (specialStructure) {
                for (int i = 0; i < len; i += 4) {
                    int firstByte = arr[i] & 0xff;
                    Resources.count[firstByte]++;

                    val = ( ((arr[i] & 0xff) << 24) | ((arr[i+1] & 0xff) << 16) |
                            ((arr[i+2] & 0xff) << 8) | (arr[i+3] & 0xff) );

                    minValue = Math.min(minValue, val);
                    maxValue = Math.max(maxValue, val);

                    lastValue[firstByte] = val;

                    if (specialStructure) {
                        if (passedCorners) {
                            if (!cornerNumber(val)) {
                                if (leftEnds.size() <= 5) {
                                    if ( Math.abs((long)val - (long)lastNumber) <= BLOCK_SEPPARATOR ) {
                                        smallValue = Math.min(smallValue, val);
                                        largeValue = Math.max(largeValue, val);

                                        if (val > lastNumber) {
                                            isReversed = false;
                                        }
                                        else if (val < lastNumber) {
                                            isSorted = false;
                                        }
                                    }
                                    else {
                                        leftEnds.add(index);
                                        sorted.add(isSorted);
                                        reversed.add(isReversed);
                                        smallestValue.add(smallValue);
                                        largestValue.add(largeValue);

                                        isSorted = true;
                                        isReversed = true;

                                        smallValue = val;
                                        largeValue = val;
                                    }
                                }
                            }
                            else {
                                specialStructure = false;
                            }
                        }
                        else {
                            if (cornerNumber(val)) {
                                cornerCases.add(val);

                                if (cornerCases.size() > CORNER_LIMIT_COUNT || cornerCases.size() > blockSize) {
                                    specialStructure = false;
                                }
                            }
                            else {
                                passedCorners = true;
                                isSorted = true;
                                isReversed = true;
                                smallValue = val;
                                largeValue = val;
                                leftEnds.add(index);
                                cornerEnding = index;
                            }
                        }
                    }

                    lastNumber = val;
                    index++;
                }
            }
            else if (countingSortStructure) {
                for (int i = 0; i < len; i += 4) {
                    Resources.count[arr[i] & 0xff]++;

                    val = (((arr[i] & 0xff) << 24) | ((arr[i + 1] & 0xff) << 16) |
                            ((arr[i + 2] & 0xff) << 8) | (arr[i + 3] & 0xff));

                    minValue = Math.min(minValue, val);
                    maxValue = Math.max(maxValue, val);
                }
            }
            else {
                for (int i = 0; i < len; i += 4) {
                    Resources.count[arr[i] & 0xff]++;
                }
            }

            if ( (long)maxValue - (long)minValue >= Resources.blockSize / 4 ) {
                countingSortStructure = false;
            }
        }

        leftEnds.add(index);
        sorted.add(isSorted);
        reversed.add(isReversed);
        smallestValue.add(smallValue);
        largestValue.add(largeValue);

        System.out.println("MAIN PART IS " + (System.nanoTime() - saveTime)/1000000 + "ms");

        /*for (int i = 0; i < 256; i++) {
            if (auxMaxInd[i] != 0) {
                //System.out.println("Range for bit " + i + " is [" + auxMinInd[i] + "; " + auxMaxInd[i] + "]");
            }
        }*/

        for (int i = 0; i < 256; i++) {
            if (count[i] > 0) {
                //System.out.println("Byte group " + i + " has naturely_sorted status = " + naturelySorted[i]);
                //System.out.println("It also has " + count[i] + " members in the range [" + minVals[i] + ", " + maxVals[i] + "]");
                //System.out.println("Average value = " + (averageValue[i] / count[i]));
                //System.out.println();
            }

            if (count[i] > 0) {
                criticals++;
            }
        }

        if (leftEnds.size() > 5) {
            specialStructure = false;
            System.out.println("[REASON] Too many left ends");

            for (int i = 0; i < leftEnds.size() - 1; i++) {
                System.out.println("Interval from " + leftEnds.get(i) + " to " + leftEnds.get(i + 1) +
                                    " with values in the range [" + smallestValue.get(i) + "; " + largestValue.get(i) + "]");
            }
        }
        else {
            for (int i = 0; i < leftEnds.size() - 1; i++) {
                if (leftEnds.get(i + 1) - leftEnds.get(i) > blockSize && !sorted.get(i) && !reversed.get(i)) {
                    specialStructure = false;
                    System.out.println("[REASON] Too big unsorted/unreversed interval");
                    break;
                }

                for (int j = i + 1; j < leftEnds.size() - 1; j++) {
                    int firstLeft = smallestValue.get(i);
                    int firstRight = largestValue.get(i);
                    int secondLeft = smallestValue.get(j);
                    int secondRight = largestValue.get(j);

                    if (firstRight < secondLeft) {
                        continue;
                    }
                    if (secondRight < firstLeft) {
                        continue;
                    }


                    System.out.println("[REASON] Interval intersection");
                    specialStructure = false;
                    break;
                }

                if (!specialStructure) {
                    break;
                }
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
