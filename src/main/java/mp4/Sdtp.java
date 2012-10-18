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
public final class Sdtp extends FullBox
{
	private static final long serialVersionUID = -6039275315154741796L;
	private byte[] entries;

	Sdtp()
	{

	}

	Sdtp(final FullBox box)
	{
		copy(box);
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		final Sdtp sdtp = (Sdtp) super.clone();
		if (null != entries)
		{
			sdtp.entries = new byte[entries.length];
			System.arraycopy(entries, 0, sdtp.entries, 0, entries.length);
		}
		return sdtp;
	}

	@Override long read(final InputStream in) throws IOException
	{
		return read(in, null);
	}

	long read(final InputStream in, final FullBox box) throws IOException
	{
		long bytes = 0;
		if (null == box)
		{
			bytes += super.read(in);
		}

		final int capacity = (int) boxRemaining;
		entries = new byte[capacity];
		bytes += in.read(entries);
		return bytes;
	}

	@Override void write(final OutputStream out) throws IOException
	{
		super.write(out);
		out.write(entries);
	}

	@Override void write(final ByteChannel out) throws IOException
	{
		super.write(out);
		out.write(ByteBuffer.wrap(entries));
	}


	long adjust(final Integer startSample)
	{
		long boxAdjust = 0;
		if (null != startSample)
		{
			final byte[] adjusted = new byte[entries.length - startSample];
			System.arraycopy(entries, 0 + startSample, adjusted, 0, adjusted.length);
			entries = adjusted;
			boxAdjust = startSample;
		}
		boxLen -= boxAdjust;
		return boxAdjust;
	}
}
