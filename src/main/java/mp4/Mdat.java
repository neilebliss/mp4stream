package mp4;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ByteChannel;

/**
 * Copyright (c) 2011 3Crowd Technologies, Inc.
 * <p/>
 * See LICENSE.txt for licensing terms covering this software.
 */
public final class Mdat extends Box
{
	private static final long serialVersionUID = 9167926454385309003L;
	//
	// the mdat container is used to hold the movie data for an mp4.
	// for our purposes, we don't really want to hold a ton of data in it,
	// so we'll just leave it as a Box wrapper.
	//

	public Mdat()
	{

	}

	public Mdat(final Box box)
	{
		copy(box);
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

	@Override public long read(final InputStream in) throws IOException
	{
		long bytes = 0;
		long loopBytes = 0;
		while (0 < boxRemaining)
		{
			loopBytes = in.skip(boxRemaining);
			boxRemaining -= loopBytes;
			bytes += loopBytes;
		}
		return bytes;
	}

	@Override public void write(final ByteChannel out) throws IOException
	{
		super.write(out);
	}

	@Override public void write(final OutputStream out) throws IOException
	{
		super.write(out);
	}

	public long adjust(final long offset)
	{
		final long boxAdjust = 0;
		boxLen -= offset;
		return boxAdjust;
	}
}
