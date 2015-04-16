package com.clover.spika.enterprise.chat.networking;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CustomMultiPartEntity implements HttpEntity  {

	private final ProgressListener listener;
	private HttpEntity httpEntity;

	public CustomMultiPartEntity(final ProgressListener listener, HttpEntity http) {
		super();
		this.listener = listener;
		httpEntity = http;
	}

	public static interface ProgressListener {
		void transferred(long num, long total);
	}

	@Override
	public void consumeContent() throws IOException {
		httpEntity.consumeContent();  
	}

	@Override
	public InputStream getContent() throws IOException, IllegalStateException {
		return httpEntity.getContent();
	}

	@Override
	public Header getContentEncoding() {
		return httpEntity.getContentEncoding();
	}

	@Override
	public long getContentLength() {
		return httpEntity.getContentLength();
	}

	@Override
	public Header getContentType() {
		return httpEntity.getContentType();
	}

	@Override
	public boolean isChunked() {
		return httpEntity.isChunked();
	}

	@Override
	public boolean isRepeatable() {
		return httpEntity.isRepeatable();
	}

	@Override
	public boolean isStreaming() {
		return httpEntity.isStreaming();
	}

	@Override
	public void writeTo(OutputStream arg0) throws IOException {
		class ProxyOutputStream extends FilterOutputStream {
            /**
             * @author Stephen Colebourne
             */

            public ProxyOutputStream(OutputStream proxy) {
                super(proxy);    
            }
            public void write(int idx) throws IOException {
                out.write(idx);
            }
            public void write(byte[] bts) throws IOException {
                out.write(bts);
            }
            public void write(byte[] bts, int st, int end) throws IOException {
                out.write(bts, st, end);
            }
            public void flush() throws IOException {
                out.flush();
            }
            public void close() throws IOException {
                out.close();
            }
        } // CONSIDER import this class (and risk more Jar File Hell)

        class ProgressiveOutputStream extends ProxyOutputStream {
        	
        	long transferred = 0;
        	long total = getContentLength();
        	
            public ProgressiveOutputStream(OutputStream proxy) {
                super(proxy);
            }
            public void write(byte[] bts, int st, int end) throws IOException {
                out.write(bts, st, end);
                transferred += end;
                listener.transferred(transferred, total);
            }
        }

        httpEntity.writeTo(new ProgressiveOutputStream(arg0));
		
	}
	
	

}