package uk.ac.cam.eim26.fjava.tick0;

import java.io.IOException;

/**
 * Chooses the sorting strategy. Employs the Strategy design pattern.
 */
@Deprecated
public class StrategySelector {
    private static final int LIGHTWEIGHT_INTERNAL_THRESHOLD = 100;

    public static ExternalSortBase selectStrategy(String dataFile) throws IOException {
        Resources.allocateVitalResources(dataFile);

        //Return early to avoid pointless resource allocation
        if (Resources.totalSize <= Resources.blockSize) {
            if (Resources.totalSize <= LIGHTWEIGHT_INTERNAL_THRESHOLD) {
                return new LightweightInternalSort();
            } else {
                return new InternalRadixSort();
            }
        }

        Resources.allocateResources(dataFile);

        return new ExternalMergeSort();

        /*if ( (long)Resources.maxValue - (long)Resources.minValue < Resources.blockSize / 4 ) {
            return new ExternalCountingSort();
        }

        if (Resources.specialStructure) {
            return new ExternalCustomSort();
        }

        int maxValue = 0;

        for (int i = 0; i < 256; i++) {
            if (Resources.count[i] > maxValue) {
                maxValue = Resources.count[i];
            }
        }

        if (maxValue > Resources.blockSize) {
            return new ExternalMergeSort();
        }
        else {
            return new ExternalBucketSort();
        }*/
    }
}
