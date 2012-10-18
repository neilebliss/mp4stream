package mp4;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Copyright (c) 2011 3Crowd Technologies, Inc.
 * <p/>
 * See LICENSE.txt for licensing terms covering this software.
 */
public final class Ftyp extends Box
{
	private static final long serialVersionUID = 5396832476550917351L;
	private int               majorBrand;
	private int               minorVersion;
	private ArrayList<String> compatibleBrands;

	public Ftyp()
	{

	}

	public Ftyp(final Box box)
	{
		copy(box);
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		final Ftyp ftyp = (Ftyp) super.clone();
		ftyp.compatibleBrands = (ArrayList<String>) compatibleBrands.clone();
		return ftyp;
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
		buffer = new byte[4];
		bytes += in.read(buffer);
		boxRemaining -= 4;
		majorBrand = (int) read32(buffer);
		Arrays.fill(buffer, (byte) 0);
		bytes += in.read(buffer);
		boxRemaining -= 4;
		minorVersion = (int) read32(buffer);
		compatibleBrands = new ArrayList<String>();
		long loopBytes;
		while (0 < boxRemaining)
		{
			Arrays.fill(buffer, (byte) 0);
			loopBytes = in.read(buffer);
			bytes += loopBytes;
			boxRemaining -= loopBytes;
			compatibleBrands.add(new String(buffer));
		}
		return bytes;
	}

	@Override public void write(final OutputStream out) throws IOException
	{
		super.write(out);
		buffer = write32(majorBrand);
		out.write(buffer);
		buffer = write32(minorVersion);
		out.write(buffer);
		for (final String brand : compatibleBrands)
		{
			out.write(brand.getBytes());
		}
	}

	@Override public void write(final ByteChannel out) throws IOException
	{
		super.write(out);
		buffer = write32(majorBrand);
		out.write(ByteBuffer.wrap(buffer));
		buffer = write32(minorVersion);
		out.write(ByteBuffer.wrap(buffer));
		for (final String brand : compatibleBrands)
		{
			out.write(ByteBuffer.wrap(brand.getBytes()));
		}
	}
}
