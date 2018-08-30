package ca.gobits.bnf.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

public class IOUtils {

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    @Deprecated
    public static String toString(InputStream input)
            throws IOException {
        return toString(input, Charset.defaultCharset());
    }

    public static String toString(InputStream input, Charset encoding)
            throws IOException {
        StringBuilderWriter sw = new StringBuilderWriter();
        copy(input, sw, encoding);
        return sw.toString();
    }


    @Deprecated
    public static void copy(InputStream input, Writer output)
            throws IOException {
        copy(input, output, Charset.defaultCharset());
    }

    public static void copy(InputStream input, Writer output, Charset inputEncoding)
            throws IOException {
        InputStreamReader in = new InputStreamReader(input, Charsets.toCharset(inputEncoding));
        copy(in, output);
    }


    public static int copy(Reader input, Writer output)
            throws IOException {
        long count = copyLarge(input, output);
        if (count > 2147483647L) {
            return -1;
        }
        return (int) count;
    }

    public static long copyLarge(Reader input, Writer output)
            throws IOException {
        return copyLarge(input, output, new char[DEFAULT_BUFFER_SIZE]);
    }

    public static long copyLarge(Reader input, Writer output, char[] buffer)
            throws IOException {
        long count = 0L;
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

}
