package uk.ac.cam.eim26.fjava.tick0;

import java.io.IOException;

/**
 * Chooses the sorting strategy. Employs the Strategy design pattern.
 */
public class StrategySelector {
    public static ExternalSortBase selectStrategy(String dataFile) throws IOException {
        Resources.allocateVitalResources(dataFile);

        if (Resources.totalSize >= 250000) {
            System.out.println("File size = " + Resources.totalSize * 4 + " bytes; = " + (Resources.totalSize / 250000) + "MB");
        }
        else {
            System.out.println("File size = " + Resources.totalSize * 4 + " bytes; = " + (Resources.totalSize / 250) + "KB");
        }

        //Return early to avoid pointless resource allocation
        if (Resources.totalSize <= Resources.blockSize) {
            if (Resources.totalSize <= 100) {
                return new LightweightInternalRadixSort();
            }
            else {
                return new InternalRadixSort();
            }
        }

        Resources.allocateResources(dataFile);

        return new ExternalMergeSort();

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

        if (Resources.specialStructure) {
            return new ExternalCustomSort();
        }
        else if (maxValue > Resources.blockSize) {
            return new ExternalMergeSort();
        }
        else {
            return new ExternalBucketSort();
        }
    }
}
