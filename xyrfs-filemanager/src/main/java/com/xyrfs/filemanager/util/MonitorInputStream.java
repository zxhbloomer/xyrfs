package com.xyrfs.filemanager.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 */
public abstract class MonitorInputStream extends BufferedInputStream {
    private static final int EOF = -1;
    private final AtomicBoolean finished = new AtomicBoolean(false);

    public MonitorInputStream(InputStream in) {
        super(in);
    }

    public MonitorInputStream(InputStream in, int size) {
        super(in, size);
    }

    @Override
    public synchronized int available() throws IOException {
        if (finished.get()) {
            return 0;
        }
        return super.available();
    }

    @Override
    public synchronized int read() throws IOException {
        if (finished.get()) {
            return EOF;
        }
        int c = super.read();
        if (EOF < c) {
            return c;
        }
        // END STREAM
        close();
        return EOF;
    }

    @Override
    public synchronized int read(byte[] buff, int off, int len) throws IOException {
        if (finished.get()) {
            return EOF;
        }

        final int readLen = super.read(buff, off, len);
        if (EOF < readLen) {
            return readLen;
        }

        // End-of-stream
        close();
        return EOF;
    }

    @Override
    public void close() throws IOException {
        if (finished.getAndSet(true)) {
            return;
        }

        try {
            super.close();
        } finally {
            onClosed();
        }
    }

    protected abstract void onClosed() throws IOException;

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }
}
