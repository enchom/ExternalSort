package uk.ac.cam.eim26.fjava.tick0;

import java.io.IOException;

/**
 * A class that chooses the sorting strategy. Employs the Strategy design pattern.
 */
public class StrategySelector {
    public static ExternalSortBase selectStrategy(String f1) throws IOException {
        Resources.allocateVitalResources(f1);

        if (Resources.totalSize >= 250000) {
            System.out.println("File size = " + Resources.totalSize * 4 + " bytes; = " + (Resources.totalSize / 250000) + "MB");
        } else {
            System.out.println("File size = " + Resources.totalSize * 4 + " bytes; = " + (Resources.totalSize / 250) + "KB");
        }

        //Return early to avoid pointless resource allocation
        if (Resources.totalSize <= Resources.blockSize) {

            return new InternalRadixSort();
        }

        Resources.allocateResources(f1);

        if ( (long)Resources.maxValue - (long)Resources.minValue < Resources.blockSize / 4 )
        {
            return new ExternalCountingSort();
        }

        int maxValue = 0;

        for (int i = 0; i < 256; i++) {
            if (Resources.count[i] > maxValue) {
                maxValue = Resources.count[i];
            }
        }

        System.out.println("The maximum value is " + maxValue);

        if (maxValue > Resources.blockSize) {
            if (Resources.criticals <= 2) {
                //return new ExternalBucketSpecificHybridSort();
                return new ExternalBucketSort();
            }
            else {
                return new ExternalMergeSort();
            }
        } else {
            return new ExternalBucketSort();
            //return new ExternalBucketSpecificHybridSort();
        }
    }
}
