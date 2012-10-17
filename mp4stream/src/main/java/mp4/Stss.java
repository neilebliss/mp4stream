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
public final class Stss extends FullBox
{
	private static final long serialVersionUID = 3431469713693954101L;
	private int entryCount;
	public int[] entries;

	public Stss()
	{

	}

	Stss(final FullBox box)
	{
		copy(box);
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		final Stss stss = (Stss) super.clone();
		stss.entryCount = entryCount;
		if (null != entries)
		{
			stss.entries = new int[entries.length];
			System.arraycopy(entries, 0, stss.entries, 0, entries.length);
		}
		return stss;
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
		entryCount = read32(buffer);
		entries = new int[(int) entryCount];
		for (int i = 0; i < entryCount; i++)
		{
			Arrays.fill(buffer, (byte) 0);
			bytes += in.read(buffer);
			entries[i] = read32(buffer);
		}
		return bytes;
	}

	@Override void write(final OutputStream out) throws IOException
	{
		super.write(out);
		out.write(write32(entryCount));
		for (final int entry : entries)
		{
			out.write(write32(entry));
		}
	}

	@Override void write(final ByteChannel out) throws IOException
	{
		super.write(out);
		out.write(ByteBuffer.wrap(write32(entryCount)));
		for (final int entry : entries)
		{
			out.write(ByteBuffer.wrap(write32(entry)));
		}
	}

	public long adjust(final Integer startSample)
	{
		long boxAdjust = 0;
		// find the offset of the first sync sample that's at or after our start sample ID
		int adjustIndex = 0;

		for (final long sampleId : entries)
		{
			if (sampleId < startSample)
			{
				adjustIndex++;
				boxAdjust += 4;
			}
			else
			{
				break;
			}
		}
		final int[] dest = new int[(int) (entryCount - adjustIndex)];
		for (int i = 0; i < dest.length; i++)
		{
			dest[i] = this.entries[i + adjustIndex] - startSample + 1;
		}
		entries = dest;
		entryCount = entries.length;
		boxLen -= boxAdjust;
		return boxAdjust;
	}

	public void createEntries(final int numentries)
	{
		int i;
		entries = new int[numentries];
		for (i = 0; i < numentries; i++)
		{
			entries[i] = i * 5;
		}
		entryCount = entries.length;
	}
}
