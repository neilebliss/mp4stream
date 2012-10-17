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
public class FullBox extends Box
{
	private static final long serialVersionUID = -1194465771141735998L;
	int version;
	private int flags;

	public FullBox()
	{

	}

	FullBox(final Box box)
	{
		boxLen = box.boxLen;
		boxRemaining = box.boxRemaining;
		boxType = box.boxType;
		largeSize = box.largeSize;
		extendedType = box.extendedType;
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

	void copy(final FullBox box)
	{
		boxLen = box.boxLen;
		boxRemaining = box.boxRemaining;
		boxType = box.boxType;
		largeSize = box.largeSize;
		extendedType = box.extendedType;
		version = box.version;
		flags = box.flags;
	}

	@Override long read(final InputStream in) throws IOException
	{
		return read(in, null);
	}

	long read(final InputStream in, final Box box) throws IOException
	{
		long bytes = 0;
		if (null == box)
		{
			bytes += super.read(in);
		}
		buffer = new byte[1];
		bytes += in.read(buffer);
		boxRemaining -= 1;
		version = (int) read8(buffer);
		buffer = new byte[4];
		bytes += in.read(buffer, 1, 3);
		boxRemaining -= 3;
		flags = ByteBuffer.wrap(buffer).getInt();
		return bytes;
	}

	@Override void write(final OutputStream out) throws IOException
	{
		super.write(out);
		buffer = new byte[1];
		buffer = write8(version);
		out.write(buffer);
		buffer = new byte[4];
		buffer = write32(flags);
		out.write(buffer, 1, 3);
	}

	@Override void write(final ByteChannel out) throws IOException
	{
		super.write(out);
		out.write(ByteBuffer.wrap(write8(version)));
		buffer = write32(flags);
		out.write(ByteBuffer.wrap(buffer, 1, 3));
	}
}
