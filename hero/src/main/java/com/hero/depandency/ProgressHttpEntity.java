package com.hero.depandency;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

public class ProgressHttpEntity extends HttpEntityWrapper {

    // service -- handlerThread
    private final ProgressListener listener;
    private long totalSize;

    public ProgressHttpEntity(final HttpEntity entity, final ProgressListener listener) {
        super(entity);
        this.listener = listener;
        totalSize = entity.getContentLength();
    }

    public static class CountingOutputStream extends FilterOutputStream {

        private final ProgressListener listener;
        private long transferred;
        private long totalSize;

        CountingOutputStream(final OutputStream out, final ProgressListener listener, final long total) {
            super(out);
            this.listener = listener;
            this.transferred = 0;
            totalSize = total;
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            out.write(b, off, len);
            this.transferred += len;
            this.listener.transferred(this.transferred, this.totalSize);
        }

        @Override
        public void write(final int b) throws IOException {
            out.write(b);
            this.transferred++;
            this.listener.transferred(this.transferred, this.totalSize);
        }

    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        this.wrappedEntity.writeTo(out instanceof CountingOutputStream ? out : new CountingOutputStream(out, this.listener, totalSize));
    }

    public interface ProgressListener {
        public void transferred(long transferredBytes, long total);
    }
}
