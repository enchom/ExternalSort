package uk.ac.cam.eim26.fjava.tick0;

/**
 * Interface for the external sort strategy. Part of the Strategy Design Pattern
 */
public interface ExternalSortBase {
    void setFiles(String f1, String f2);

    void sort() throws Exception;

    /**
     * Returns strategy name for debug purposes.
     */
    String getStrategy();
}
