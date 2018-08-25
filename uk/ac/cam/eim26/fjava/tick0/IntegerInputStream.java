package uk.ac.cam.eim26.fjava.tick0;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class IntegerInputStream {
    private InputStream in;

    public IntegerInputStream(InputStream inputStream) {
        in = inputStream;
    }

    public void read(int arr[], int offset, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            int b1 = in.read(), b2 = in.read(), b3 = in.read(), b4 = in.read();
            if (b4 < 0 || b3 < 0 || b2 < 0 || b1 < 0) {
                throw new EOFException();
            }

            arr[i + offset] = (b1 << 24) | (b2 << 16) | (b3 << 8) | (b4);
        }
    }

    public void read(int[] arr) throws IOException {
        read(arr, 0, arr.length);
    }

    public int read() throws IOException {
        return in.read();
    }
}
