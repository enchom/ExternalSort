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
    public static boolean[] naturelySorted = new boolean[256];
    public static int[] minVals = new int[256];
    public static int[] maxVals = new int[256];
    public static long[] averageValue = new long[256];
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

    private static boolean cornerNumber(int num) {
        if (num < Integer.MIN_VALUE + MAX_PAD) {
            return true;
        }
        else if (num > Integer.MAX_VALUE - MAX_PAD) {
            return true;
        }
        else {
            return false;
        }
    }

    public static void computeCount(String f) throws IOException {
        int len;
        InputStream d = new FileInputStream(f);
        int val;

        for (int i = 0; i < 256; i++) {
            //auxMinInd[i] = 1000000000;

            lastValue[i] = Integer.MIN_VALUE;
            naturelySorted[i] = true;
            minVals[i] = Integer.MAX_VALUE;
            maxVals[i] = Integer.MIN_VALUE;
        }

        int lastNumber = 0;
        boolean isSorted = true, isReversed = true;
        int smallValue = 0, largeValue = 0;
        int index = 0;

        ///READ FOR SPEED TEST
        long saveTime = System.nanoTime();
        while(true) {
            len = d.read(Resources.arr);

            if (len == -1) {
                break;
            }

            for (int i = 0; i < len; i += 4) {
                Resources.count[arr[i] & 0xff]++;
            }
        }
        System.out.println("Simple read took " + (System.nanoTime() - saveTime)/1000000 + "ms");

        d.close();
        d = new FileInputStream(f);
        
        while (true) {
            len = d.read(Resources.arr);

            if (len == -1) {
                break;
            }

            totalSize += len;

            for (int i = 0; i < len; i += 4) {
                //Resources.count[arr[i] & 0xff]++;

                val = PartialByteHeapSort.bytesToInteger(arr, i / 4);

                minValue = Math.min(minValue, val);
                maxValue = Math.max(maxValue, val);

                if (lastValue[arr[i] & 0xff] > val) {
                    naturelySorted[arr[i] & 0xff] = false;
                }

                //pairCount[ arr[i]&0xff ][ arr[i+1]&0xff ]++;

                /*if ( (arr[i]&0xff) == 127) {
                    auxMaxInd[ arr[i+1]&0xff ] = Math.max(auxMaxInd[ arr[i+1]&0xff ], index);
                    auxMinInd[ arr[i+1]&0xff ] = Math.min(auxMinInd[ arr[i+1]&0xff ], index);
                }*/

                minVals[ arr[i]&0xff ] = Math.min(minVals[ arr[i]&0xff ], val);
                maxVals[ arr[i]&0xff ] = Math.max(maxVals[ arr[i]&0xff ], val);

                lastValue[Resources.arr[i] & 0xff] = val;
                averageValue[Resources.arr[i] & 0xff] += (long)val;

                if (specialStructure) {
                    if (cornerNumber(val)) {
                        if (passedCorners) {
                            specialStructure = false;
                        }
                        else {
                            cornerCases.add(val);

                            if (cornerCases.size() > CORNER_LIMIT_COUNT || cornerCases.size() > blockSize) {
                                specialStructure = false;
                            }
                        }
                    }
                    else {
                        passedCorners = true;
                        if (cornerEnding == 0) {
                            cornerEnding = index;
                        }
                    }
                }

                if (passedCorners) {
                    if (val < Integer.MIN_VALUE + MAX_PAD || val > Integer.MAX_VALUE - MAX_PAD) {
                        specialStructure = false;
                    }

                    if (leftEnds.size() <= 5) {
                        if (leftEnds.isEmpty()) {
                            leftEnds.add(index);
                            isSorted = true;
                            isReversed = true;

                            smallValue = val;
                            largeValue = val;
                        }
                        else if ( Math.abs((long)val - (long)lastNumber) > BLOCK_SEPPARATOR ) {
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
                        else {
                            smallValue = Math.min(smallValue, val);
                            largeValue = Math.max(largeValue, val);

                            if (val > lastNumber) {
                                isReversed = false;
                            }
                            else if (val < lastNumber) {
                                isSorted = false;
                            }
                        }
                    }
                }

                lastNumber = val;

                index++;
            }
        }
        leftEnds.add(index);
        sorted.add(isSorted);
        reversed.add(isReversed);
        smallestValue.add(smallValue);
        largestValue.add(largeValue);

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
