package uk.ac.cam.eim26.fjava.tick0;

/**
 * Interface for the external sort strategy. Part of the Strategy Design Pattern
 */
@Deprecated
public interface ExternalSortBase {
    /**
     * Initialises parameters and defines files to be used while sorting
     */
    void setFiles(String firstFile, String secondFile);

    /**
     * Sorts the first file set in {@link ExternalSortBase#setFiles(String, String)}
     */
    void sort() throws Exception;

    /**
     * Returns strategy name for debug purposes.
     */
    String getStrategy();
}
