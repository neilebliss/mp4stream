package mp4;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

/**
 * Copyright (c) 2011 3Crowd Technologies, Inc.
 * <p/>
 * See LICENSE.txt for licensing terms covering this software.
 */
public final class Other extends Box
{
	private static final long serialVersionUID = 3408666138441097009L;
	private byte[] data;

	public Other()
	{

	}

	public Other(final Box box)
	{
		copy(box);
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		final Other other = (Other) super.clone();
		other.data = new byte[data.length];
		System.arraycopy(data, 0, other.data, 0, data.length);
		return other;
	}

	@Override public long read(final InputStream in) throws IOException
	{
		return read(in, null);
	}

	public long read(final InputStream in, final Box box) throws IOException
	{
		long bytes = 0;
		if (null == box)
		{
			bytes += super.read(in);
		}
		data = new byte[(int) boxRemaining];
		bytes += in.read(data);
		return bytes;
	}

	@Override public void write(final OutputStream out) throws IOException
	{
		super.write(out);
		out.write(data);
	}

	@Override public void write(final ByteChannel out) throws IOException
	{
		super.write(out);
		out.write(ByteBuffer.wrap(data));
	}

}
