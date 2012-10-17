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
public final class Stsz extends FullBox
{
	private static final long serialVersionUID = 8077523461371897396L;
	public int sampleSize;
	public int entryCount;
	//public StszEntry[] entries;
	public int[] sampleIds;
	public int[] sizes;

	public Stsz()
	{

	}

	Stsz(final FullBox box)
	{
		copy(box);
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		final Stsz stsz = (Stsz) super.clone();
		stsz.entryCount = entryCount;
/*
                if (entries != null)
                {
                        stsz.entries = new StszEntry[entries.length];
                        for (int i = 0; i < entries.length; i++)
                        {
                                stsz.entries[i] = (StszEntry) entries[i].clone();
                        }
                }
*/
		if (null != sampleIds)
		{
			stsz.sampleIds = new int[sampleIds.length];
			System.arraycopy(sampleIds, 0, stsz.sampleIds, 0, sampleIds.length);
		}
		if (null != sizes)
		{
			stsz.sizes = new int[sizes.length];
			System.arraycopy(sizes, 0, stsz.sizes, 0, sizes.length);
		}

		return stsz;
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
		sampleSize = read32(buffer);
		Arrays.fill(buffer, (byte) 0);
		bytes += in.read(buffer);
		entryCount = read32(buffer);
		if (0 == sampleSize)
		{
			//entries = new StszEntry[(int) entryCount];
			sampleIds = new int[(int) entryCount];
			sizes = new int[(int) entryCount];
			for (int i = 0; i < entryCount; i++)
			{
				//entries[i] = new StszEntry();
				Arrays.fill(buffer, (byte) 0);
				bytes += in.read(buffer);
				//entries[i].sampleId = i + 1;
				sampleIds[i] = i + 1;
				//entries[i].size = read32(buffer);
				sizes[i] = read32(buffer);
			}
		}
		return bytes;
	}

	@Override void write(final OutputStream out) throws IOException
	{
		super.write(out);
		out.write(write32(sampleSize));
		out.write(write32(entryCount));
		if (0 == sampleSize)
		{
/*
                        for (StszEntry entry : entries)
                        {
                                out.write(write32(entry.size));
                        }
*/
			for (final int size : sizes)
			{
				out.write(write32(size));
			}
		}
	}

	@Override void write(final ByteChannel out) throws IOException
	{
		super.write(out);
		out.write(ByteBuffer.wrap(write32(sampleSize)));
		out.write(ByteBuffer.wrap(write32(entryCount)));
		if (0 == sampleSize)
		{
/*
                        for (StszEntry entry : entries)
                        {
                                out.write(ByteBuffer.wrap(write32(entry.size)));
                        }
*/
			for (final int size : sizes)
			{
				out.write(ByteBuffer.wrap(write32(size)));
			}
		}
	}

	public long adjust(final Integer startSample)
	{
		long boxAdjust = 0;
		if (null != startSample)
		{
			if (0 == sampleSize)
			{
				int adjustIndex = 0;
				for (final long sampleId : sampleIds)
				{
					if (sampleId < startSample)
					{
						boxAdjust += 4;
						adjustIndex++;
					}
					else
					{
						break;
					}
				}
/*
                                for (StszEntry entry : entries)
                                {
                                        if (entry.sampleId < startSample)
                                        {
                                                boxAdjust += 4;
                                                adjustIndex++;
                                        }
                                        else
                                        {
                                                break;
                                        }
                                }
*/
				//StszEntry[] dest = new StszEntry[(int) (entryCount - adjustIndex)];
				final int[] destSampleIds = new int[(int) (entryCount - adjustIndex)];
				final int[] destSizes = new int[(int) (entryCount - adjustIndex)];
				for (int i = 0; i < destSizes.length; i++)
				{
					destSizes[i] = sizes[i + adjustIndex];
					//dest[i].sampleId = (dest[i].sampleId - startSample) + 1;
					destSampleIds[i] = this.sampleIds[i + adjustIndex] - startSample + 1;
				}
				//entries = dest;
				sizes = destSizes;
				sampleIds = destSampleIds;
				entryCount = sizes.length;
			}
		}
		boxLen -= boxAdjust;
		return boxAdjust;
	}

	public void createEntries(final int numentries)
	{
		int i;
		//entries = new StszEntry[numentries];
		sizes = new int[numentries];
		sampleIds = new int[numentries];
/*
		for (i = 0; i < numentries; i++)
		{
			entries[i] = new StszEntry();
		}
*/

		entryCount = numentries;
	}


}
