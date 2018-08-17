package uk.ac.cam.eim26.fjava.tick0;

/**
 * Interface for the external sort strategy. Part of the Strategy Design Pattern
 */
public interface ExternalSortBase {
    void sort(String f1, String f2) throws Exception;

    /**
     * Returns strategy name for debug purposes.
     */
    String getStrategy();
}
