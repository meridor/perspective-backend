package org.meridor.perspective.sql.impl;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.IntStream;

/**
 * SQL queries should be parsed as case insensitive (except string literals)
 * @link https://gist.github.com/sharwell/9424666
 */
public class CaseInsensitiveInputStream extends ANTLRInputStream {

    private final char[] lookaheadData;

    public CaseInsensitiveInputStream(String input) {
        super(input);
        lookaheadData = input.toLowerCase().toCharArray();
    }

    @Override
    public int LA(int i) {
        if (i == 0) {
            return 0; // undefined
        }
        if (i < 0) {
            i++; // e.g., translate LA(-1) to use offset i=0; then data[p+0-1]
            if ((p + i - 1) < 0) {
                return IntStream.EOF; // invalid; no char before first char
            }
        }

        if ((p + i - 1) >= n) {
            return IntStream.EOF;
        }

        return lookaheadData[p + i - 1];
    }

}