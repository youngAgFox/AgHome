package com.ag.database;

import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamCounter extends OutputStream {

    private long writtenBytes;
    private OutputStream innerStream;

    public OutputStreamCounter() {

    }

    public OutputStreamCounter(OutputStream innerStream) {
        this.innerStream = innerStream;
    }

    @Override
    public void write(int b) throws IOException {
        writtenBytes++;
        if (null != innerStream) {
            innerStream.write(b);
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        writtenBytes += b.length;
        if (null != innerStream) {
            innerStream.write(b);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (len - off < 0) {
            return;
        }
        writtenBytes += len - off;
        if (null != innerStream) {
            innerStream.write(b);
        }
    }
    
    public long getTotalWrittenBytes() {
        return writtenBytes;
    }

    public void setInnerStream(OutputStream innerStream) {
        this.innerStream = innerStream;
    }

    @Override
    public void close() throws IOException {
        if (null != innerStream) {
            innerStream.close();
        }
    }
}
