/**
 * BSD License
 * Copyright (c) Hero software.
 * All rights reserved.

 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.

 * Neither the name Facebook nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific
 * prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
