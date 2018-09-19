//TODO - MAKE THIS WORK EVEN IF TEST DATA IS UNKNOWN
package uk.ac.cam.eim26.fjava.tick0;

import java.io.*; //TODO - split (in all files)

public class ExternalBucketSpecificHybridSort extends ExternalBucketSortBase {
    private int[] countingSortArr;
    private int COUNTING_SIZE;

    public void sort() throws IOException {
        int num;
        int skipped = 0;

        /*
        COUNTING_SIZE = Resources.blockSize / 4;
        countingSortArr = new int[COUNTING_SIZE];

        sortByFirstByte();

        BufferedOutputStream firstFileOutputStream = new BufferedOutputStream(new FileOutputStream(firstFile));
        InputStream secondFileInputStream = new FileInputStream(secondFile);

        for (int i = 0; i < 256; i++) {
            int realInd = i ^ 128;

            if (Resources.count[realInd] == 0) {
                continue;
            }


            int len;
            int dataLeft;

            ///TODO - Finish the idea of doing a counting sort on small groups followed by insertion sort
            if (Resources.maxVals[realInd] - Resources.minVals[realInd] + 1 <= COUNTING_SIZE) {
                while(true) {
                    len = arr.length;
                    while(len % 3 != 0) {
                        len--;
                    }

                    if (dataLeft < len) {
                        len = dataLeft;
                    }

                    len = secondFileInputStream.read(arr, 0, len);
                    dataLeft -= len;

                    if (len <= 0) {
                        break;
                    }

                    for (int j = 0; j < len; j += 3) {
                        num = ((realInd & 0xff) << 24) |
                                ((arr[j] & 0xff) << 16) |
                                ((arr[j + 1] & 0xff) << 8) |
                                (arr[j + 2] & 0xff);
                    }
                }
            } else {
                int splitLevel = 1;

                ///Assume uniform distribution
                while ((Resources.maxVals[realInd] - Resources.minVals[realInd] + 1) / splitLevel > Resources.blockSize / 3) {
                    splitLevel++;
                }

            }


            int len;
            long leftEnd = Resources.minVals[realInd], rightEnd;

            while (leftEnd <= Resources.maxVals[realInd]) {
                int dataLeft = Resources.count[realInd] * 3;

                rightEnd = leftEnd + COUNTING_SIZE - 1;

                while(true) {
                    len = arr.length;
                    while (len % 3 != 0) {
                        len--;
                    }

                    if (dataLeft < len) {
                        len = dataLeft;
                    }

                    len = secondFileInputStream.read(arr, 0, len);
                    dataLeft -= len;

                    if (len <= 0) {
                        break;
                    }

                    for (int j = 0; j < len; j += 3) {
                        num = ((realInd & 0xff) << 24) |
                                ((arr[j] & 0xff) << 16) |
                                ((arr[j + 1] & 0xff) << 8) |
                                (arr[j + 2] & 0xff);

                        if (num >= leftEnd && num <= rightEnd) {
                            countingSortArr[num - (int) leftEnd]++;
                        }
                    }
                }

                for (long j = leftEnd; j <= rightEnd; j++) {
                    for (int in = 0; in < countingSortArr[(int) (j - leftEnd)]; in++) {
                        int jint = (int) j;
                        firstFileOutputStream.write(((jint >> 24) & 0xff));
                        firstFileOutputStream.write(((jint >> 16) & 0xff));
                        firstFileOutputStream.write(((jint >> 8) & 0xff));
                        firstFileOutputStream.write((jint & 0xff));
                    }
                    countingSortArr[(int) (j - leftEnd)] = 0;
                }

                leftEnd = rightEnd + 1;

                if (leftEnd <= Resources.maxVals[realInd]) {
                    secondFileInputStream.close();

                    RandomAccessFile randomAccessFile = new RandomAccessFile(secondFile, "r");
                    randomAccessFile.skipBytes(skipped);
                    secondFileInputStream = new FileInputStream(randomAccessFile.getFD());
                }
            }

            skipped += Resources.count[realInd] * 3;
            //System.out.println("SKIPPED " + skipped);
        }

        firstFileOutputStream.close();
        secondFileInputStream.close();*/
    }

    @Override
    public String getStrategy() {
        return "External bucket specific hybrid sort";
    }
}
