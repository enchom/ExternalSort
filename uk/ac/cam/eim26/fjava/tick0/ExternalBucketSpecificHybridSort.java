package uk.ac.cam.eim26.fjava.tick0;

import java.io.*; //TODO - split

public class ExternalBucketSpecificHybridSort extends ExternalBucketSortBase {
    public void sort() throws IOException {
        int len;

        sortByFirstByte();

        BufferedOutputStream firstFileStream = new BufferedOutputStream(new FileOutputStream(firstFile));
        InputStream d = new FileInputStream(secondFile); //TODO - More descriptive name


        for (int i = 0; i < 256; i++) {
            int realInd = i ^ 128;

            if (Resources.count[realInd] == 0) {
                continue;
            }

            if ( (long)Resources.maxVals[realInd] - (long)Resources.minVals[realInd] < Resources.count[realInd] / 4 ) {
                //Counting sort
            }
            else {
                //Bad counting sort
            }
        }

        d.close();
        firstFileStream.close();
    }

    @Override
    public String getStrategy() {
        return "External bucket specific hybrid sort";
    }
}
