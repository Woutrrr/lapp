package nl.wvdzwan.lapp.IRDotMerger;

import java.io.IOException;
import java.io.Reader;

class MergedInputReader extends Reader {

    private Reader f1;
    private Reader f2;
    private boolean read_from_second_file = false;
    private boolean skipped_header = false;

    MergedInputReader(Reader f1, Reader f2) {
        this.f1 = f1;
        this.f2 = f2;
    }


    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {

        if (!read_from_second_file) {
            int res = f1.read(cbuf, off, len);


            // Reached end of file 1?
            if (res < len) {

                int last_non_white_index = res-1;
                while (Character.isWhitespace(cbuf[last_non_white_index])) {
                    last_non_white_index--;
                }



                read_from_second_file = true;
                res = last_non_white_index -1;


                // Skip graph header of file 2
                while (!skipped_header) {
                    int temp = f2.read();
                    if (temp == -1 || temp == '{') {
                        skipped_header = true;
                    }
                }


                int subRes = f2.read(cbuf, off + res, len - res);
                if (subRes < 0) {
                    return subRes;
                }
                return res + subRes;
            }

            return res;

        } else {
            // Read only from second file
            return f2.read(cbuf, off, len);
        }
    }


    @Override
    public void close() throws IOException {
        f1.close();
        f2.close();
    }
}