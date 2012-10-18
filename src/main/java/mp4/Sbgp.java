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
public final class Sbgp extends FullBox
{
	private static final long serialVersionUID = -365235008258690668L;
	private int groupingType;
	private int entryCount;
	private SbgpEntry[] entries;

	public Sbgp()
	{

	}

	Sbgp(final FullBox box)
	{
		copy(box);
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		final Sbgp sbgp = (Sbgp) super.clone();
		sbgp.entryCount = entryCount;
		if (null != entries)
		{
			sbgp.entries = new SbgpEntry[entries.length];
			for (int i = 0; i < entries.length; i++)
			{
				sbgp.entries[i] = (SbgpEntry) entries[i].clone();
			}
		}
		return sbgp;
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
		groupingType = read32(buffer);
		Arrays.fill(buffer, (byte) 0);
		bytes += in.read(buffer);
		entryCount = read32(buffer);
		entries = new SbgpEntry[(int) entryCount];
		for (int i = 0; i < entryCount; i++)
		{
			final SbgpEntry entry = new SbgpEntry();
			Arrays.fill(buffer, (byte) 0);
			bytes += in.read(buffer);
			entry.sampleCount = read32(buffer);
			Arrays.fill(buffer, (byte) 0);
			bytes += in.read(buffer);
			entry.groupDescriptionIndex = read32(buffer);
			entries[i] = entry;
		}
		return bytes;
	}

	@Override void write(final OutputStream out) throws IOException
	{
		super.write(out);
		out.write(write32(groupingType));
		out.write(write32(entryCount));
		for (final SbgpEntry entry : entries)
		{
			out.write(write32(entry.sampleCount));
			out.write(write32(entry.groupDescriptionIndex));
		}
	}

	@Override void write(final ByteChannel out) throws IOException
	{
		super.write(out);
		out.write(ByteBuffer.wrap(write32(groupingType)));
		out.write(ByteBuffer.wrap(write32(entryCount)));
		for (final SbgpEntry entry : entries)
		{
			out.write(ByteBuffer.wrap(write32(entry.sampleCount)));
			out.write(ByteBuffer.wrap(write32(entry.groupDescriptionIndex)));
		}
	}


	long adjust(final Integer startSample)
	{
		final long boxAdjust;
		int adjustment = 0;
		int index = 0;
		for (int i = 0; i < entries.length; i++)
		{
			if (index + this.entries[i].sampleCount < startSample)
			{
				index += entries[i].sampleCount;
			}
			else
			{
				adjustment = i;
				break;
			}
		}
		final SbgpEntry[] adjusted = new SbgpEntry[entries.length - adjustment];
		System.arraycopy(entries, adjustment, adjusted, 0, adjusted.length);
		entries = adjusted;
		entryCount = entries.length;
		boxAdjust = adjustment * 8;
		boxLen -= boxAdjust;
		return boxAdjust;
	}

}
