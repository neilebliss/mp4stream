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
public final class Stsh extends FullBox
{
	private static final long serialVersionUID = 6838781764962880678L;
	private int entryCount;
	private StshEntry[] entries;

	Stsh()
	{

	}

	Stsh(final FullBox box)
	{
		copy(box);
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		final Stsh stsh = (Stsh) super.clone();
		stsh.entryCount = entryCount;
		if (null != entries)
		{
			stsh.entries = new StshEntry[entries.length];
			for (int i = 0; i < entries.length; i++)
			{
				stsh.entries[i] = (StshEntry) entries[i].clone();
			}
		}
		return stsh;
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
		entries = new StshEntry[(int) entryCount];
		for (int i = 0; i < entryCount; i++)
		{
			final StshEntry entry = new StshEntry();
			Arrays.fill(buffer, (byte) 0);
			bytes += in.read(buffer);
			entry.shadowedSampleNumber = read32(buffer);
			Arrays.fill(buffer, (byte) 0);
			bytes += in.read(buffer);
			entry.syncSampleNumber = read32(buffer);
			entries[i] = entry;
		}
		return bytes;
	}

	@Override void write(final OutputStream out) throws IOException
	{
		super.write(out);
		out.write(write32(entryCount));
		for (final StshEntry entry : entries)
		{
			out.write(write32(entry.shadowedSampleNumber));
			out.write(write32(entry.syncSampleNumber));
		}
	}

	@Override void write(final ByteChannel out) throws IOException
	{
		super.write(out);
		out.write(ByteBuffer.wrap(write32(entryCount)));
		for (final StshEntry entry : entries)
		{
			out.write(ByteBuffer.wrap(write32(entry.shadowedSampleNumber)));
			out.write(ByteBuffer.wrap(write32(entry.syncSampleNumber)));
		}
	}


	long adjust(final Integer startSample)
	{
		long boxAdjust = 0;
		int adjustment = 0;
		if (null != startSample)
		{
			for (int i = 0; i < entryCount; i++)
			{
				if (this.entries[i].shadowedSampleNumber >= startSample && this.entries[i].syncSampleNumber >= startSample)
				{
					adjustment = i;
					break;
				}
			}
			final StshEntry[] adjusted = new StshEntry[(int) (entryCount - adjustment)];
			System.arraycopy(entries, adjustment, adjusted, 0, adjusted.length);
			entries = adjusted;
			entryCount = entries.length;
			boxAdjust = 8 * adjustment;
		}
		boxLen -= boxAdjust;
		return boxAdjust;
	}


}
