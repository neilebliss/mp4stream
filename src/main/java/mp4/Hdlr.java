package mp4;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.Arrays;

/**
 * Copyright (c) 2011 3Crowd Technologies, Inc.
 * <p/>
 * See LICENSE.txt for licensing terms covering this software.
 */
public final class Hdlr extends FullBox
{
	private static final long serialVersionUID = 5952475848660305323L;
	private int    preDefined;
	public  String handlerType;
	private int[] reserved;
	private String name;

	Hdlr()
	{

	}

	Hdlr(final FullBox box)
	{
		copy(box);
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		final Hdlr hdlr = (Hdlr) super.clone();
		if (null != reserved)
		{
			hdlr.reserved = new int[reserved.length];
			System.arraycopy(reserved, 0, hdlr.reserved, 0, reserved.length);
		}
		return hdlr;
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
		buffer = new byte[4];
		bytes += in.read(buffer);
		preDefined = (int) read32(buffer);
		Arrays.fill(buffer, (byte) 0);
		bytes += in.read(buffer);
		handlerType = new String(buffer);
		reserved = new int[3];
		for (int i = 0; 3 > i; i++)
		{
			Arrays.fill(buffer, (byte) 0);
			bytes += in.read(buffer);
			reserved[i] = (int) read32(buffer);
		}
		boxRemaining -= 20;
		buffer = new byte[(int) boxRemaining];
		bytes += in.read(buffer);
		name = new String(buffer);
		return bytes;
	}

	@Override void write(final OutputStream out) throws IOException
	{
		super.write(out);
		out.write(write32(preDefined));
		out.write(handlerType.getBytes());
		for (int i = 0; 3 > i; i++)
		{
			out.write(write32(reserved[i]));
		}
		out.write(name.getBytes());
	}

	@Override void write(final ByteChannel out) throws IOException
	{
		super.write(out);
		out.write(ByteBuffer.wrap(write32(preDefined)));
		out.write(ByteBuffer.wrap(handlerType.getBytes()));
		for (int i = 0; 3 > i; i++)
		{
			out.write(ByteBuffer.wrap(write32(reserved[i])));
		}
		out.write(ByteBuffer.wrap(name.getBytes()));
	}
}
