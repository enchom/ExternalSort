package uk.ac.cam.eim26.fjava.tick0;

import java.io.File;
import java.io.IOException;

/**
 * A class that chooses the sorting strategy. Employs the Strategy design pattern.
 */
public class StrategySelector {
    public static ExternalSortBase selectStrategy(String f1) throws IOException {
        Resources.allocateVitalResources(f1);

        if (Resources.totalSize > 1000000) {
            System.out.println("File size = " + Resources.totalSize + " bytes; = " + (Resources.totalSize / 1000000) + "MB");
        } else {
            System.out.println("File size = " + Resources.totalSize + " bytes; = " + (Resources.totalSize / 1000) + "KB");
        }

        //Return early to avoid pointless resource allocation
        if (Resources.totalSize <= Resources.blockSize) {

            return new InternalRadixSort();
        }

        Resources.allocateResources(f1);

        int maxValue = 0;

        for (int i = 0; i < 256; i++) {
            if (Resources.count[i] > maxValue) {
                maxValue = Resources.count[i];
            }
        }

        System.out.println("The maximum value is " + maxValue);

        if (maxValue > Resources.blockSize) {
            System.out.println("So using external merge sort");
            return new ExternalMergeSort();
        } else {
            System.out.println("So using external bucket sort");
            return new ExternalBucketSort();
        }
    }
}
