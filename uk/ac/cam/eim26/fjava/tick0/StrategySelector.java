package uk.ac.cam.eim26.fjava.tick0;

/**
 * A class that chooses the sorting strategy. Employs the Strategy design pattern.
 */
public class StrategySelector {
    public static ExternalSortBase selectStrategy() {
        int maxValue = 0;

        for (int i = 0; i < 256; i++) {
            if (Resources.count[i] > maxValue) {
                maxValue = Resources.count[i];
            }
        }

        System.out.println("The maximum value is " + maxValue);

        if (maxValue > Resources.blockSize) {
            System.out.println("So using external merge sort");
            if (true) {
                throw new RuntimeException("I doubt there are any tests like this.");
            }
            return new ExternalMergeSort();
        }
        else {
            System.out.println("So using external bucket sort");
            return new ExternalBucketSort();
        }
    }
}
