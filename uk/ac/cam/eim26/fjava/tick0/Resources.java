package uk.ac.cam.eim26.fjava.tick0;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
import java.util.ArrayList;

/**
 * This class is used as global state across all classes in order to reduce memory and pointless argument passing.
 * It also computes a large amount of metadata to be used by various sorting strategies.
 */
public class Resources {
    public static int blockSize;
    public static byte[] arr;
    public static int[] integerArr;
    public static int[] secondIntegerArr;

    public static int[] count;
    public static long totalSize = 0;

    public static int minValue = Integer.MAX_VALUE;
    public static int maxValue = Integer.MIN_VALUE;

    public static int[] lastValue;

    public static ArrayList<Integer> leftEnds;
    public static ArrayList<Boolean> sorted;
    public static ArrayList<Boolean> reversed;
    public static ArrayList<Integer> smallestValue;
    public static ArrayList<Integer> largestValue;
    public static boolean passedCorners = false;
    public static int cornerEnding = 0;

    public static final double MEMORY_USAGE_FRACTION = 0.6;
    public static final int BLOCK_SIZE_PART = 6;
    public static final int CORNER_LIMIT_COUNT = 100000;
    public static final long BLOCK_SEPPARATOR = 20000000;
    public static final int SPECIAL_STRUCTURE_GROUPS_LIMIT = 5;
    public static final long MAX_PAD = 100000;
    public static boolean specialStructure = true;
    public static boolean countingSortStructure = true;

    private static boolean cornerNumber(int num) {
        return (num < Integer.MIN_VALUE + MAX_PAD || num > Integer.MAX_VALUE - MAX_PAD);
    }

    public static void convertToIntegers(int len) {
        for (int i = 0; i < len; i++) {
            integerArr[i] = (((arr[i * 4] & 0xff) << 24) | ((arr[i * 4 + 1] & 0xff) << 16) |
                    ((arr[i * 4 + 2] & 0xff) << 8) | (arr[i * 4 + 3] & 0xff));
        }
    }

    public static void convertToBytes(int len) {
        for (int i = 0; i < 4 * len; i += 4) {
            arr[i] = (byte) ((integerArr[i >> 2] >>> 24) & 0xff);
            arr[i + 1] = (byte) ((integerArr[i >> 2] >>> 16) & 0xff);
            arr[i + 2] = (byte) ((integerArr[i >> 2] >>> 8) & 0xff);
            arr[i + 3] = (byte) (integerArr[i >> 2] & 0xff);
        }
    }

    public static void computeMetadata(String f) throws IOException {
        int len;
        FileInputStream inputStream = new FileInputStream(f);
        int val;

        count = new int[256];
        lastValue = new int[256];
        leftEnds = new ArrayList<>();
        sorted = new ArrayList<>();
        reversed = new ArrayList<>();
        smallestValue = new ArrayList<>();
        largestValue = new ArrayList<>();

        for (int i = 0; i < 256; i++) {
            lastValue[i] = Integer.MIN_VALUE;
        }

        int lastNumber = 0;
        boolean isSorted = true, isReversed = true;
        int smallValue = 0, largeValue = 0;
        int index = 0;
        int cornersCount = 0;

        while (true) {
            len = inputStream.read(arr);

            if (len == -1) {
                break;
            }

            totalSize += len;

            if (specialStructure) {
                for (int i = 0; i < len; i += 4) {
                    int firstByte = arr[i] & 0xff;
                    count[firstByte]++;

                    val = (((arr[i] & 0xff) << 24) | ((arr[i + 1] & 0xff) << 16) |
                            ((arr[i + 2] & 0xff) << 8) | (arr[i + 3] & 0xff));

                    minValue = Math.min(minValue, val);
                    maxValue = Math.max(maxValue, val);

                    lastValue[firstByte] = val;

                    if (specialStructure) {
                        if (passedCorners) {
                            if (!cornerNumber(val)) {
                                if (leftEnds.size() <= SPECIAL_STRUCTURE_GROUPS_LIMIT) {
                                    if (Math.abs((long) val - (long) lastNumber) <= BLOCK_SEPPARATOR) {
                                        smallValue = Math.min(smallValue, val);
                                        largeValue = Math.max(largeValue, val);

                                        if (val > lastNumber) {
                                            isReversed = false;
                                        } else if (val < lastNumber) {
                                            isSorted = false;
                                        }
                                    } else {
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
                            } else {
                                specialStructure = false;
                            }
                        } else {
                            if (cornerNumber(val)) {
                                cornersCount++;

                                if (cornersCount > CORNER_LIMIT_COUNT || cornersCount > blockSize) {
                                    specialStructure = false;
                                }
                            } else {
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
            } else if (countingSortStructure) {
                for (int i = 0; i < len; i += 4) {
                    count[arr[i] & 0xff]++;

                    val = (((arr[i] & 0xff) << 24) | ((arr[i + 1] & 0xff) << 16) |
                            ((arr[i + 2] & 0xff) << 8) | (arr[i + 3] & 0xff));

                    minValue = Math.min(minValue, val);
                    maxValue = Math.max(maxValue, val);
                }
            } else {
                for (int i = 0; i < len; i += 4) {
                    count[arr[i] & 0xff]++;
                }
            }

            if ((long) maxValue - (long) minValue >= blockSize / 4) {
                countingSortStructure = false;
            }
        }

        leftEnds.add(index);
        sorted.add(isSorted);
        reversed.add(isReversed);
        smallestValue.add(smallValue);
        largestValue.add(largeValue);

        if (leftEnds.size() > SPECIAL_STRUCTURE_GROUPS_LIMIT) {
            specialStructure = false;
        } else {
            for (int i = 0; i < leftEnds.size() - 1; i++) {
                if (leftEnds.get(i + 1) - leftEnds.get(i) > blockSize && !sorted.get(i) && !reversed.get(i)) {
                    specialStructure = false;
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

                    specialStructure = false;
                    break;
                }

                if (!specialStructure) {
                    break;
                }
            }
        }

        inputStream.close();
    }

    /**
     * Computes a lot of metadata that may be used by only some of the sorting strategies.
     */
    public static void allocateResources(String f) throws IOException {
        blockSize /= 3;
        arr = new byte[4 * blockSize];
        integerArr = new int[blockSize];
        secondIntegerArr = new int[blockSize];

        computeMetadata(f);
    }

    /**
     * Computes metadata that will be used by all sorting strategies.
     */
    public static void allocateVitalResources(String f) {
        long maxMemory = Runtime.getRuntime().maxMemory();
        long usableMemory = (long) ((double) maxMemory * MEMORY_USAGE_FRACTION);

        totalSize = (new File(f)).length() / 4;

        blockSize = (int) (usableMemory / BLOCK_SIZE_PART);
    }
}
