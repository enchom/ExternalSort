package uk.ac.cam.eim26.fjava.tick0;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Pass {
    public static void wasteTime(String f) throws IOException {
        for (int i = 0; i < 100; i++) {
            InputStream inputStream = new FileInputStream(f);
            int len = 0;

            while(len != -1) {
                len = inputStream.read(Resources.arr);
            }

            inputStream.close();
        }
    }
}
