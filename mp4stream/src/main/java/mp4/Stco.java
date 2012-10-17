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
public class Stco extends FullBox
{
	private static final long serialVersionUID = -6291034305334014191L;
	int entryCount;
	//public StcoEntry[] entries;
	public long[]   offsets;
	public long[]   sizes;
	public int[]    firstSampleIds;
	public int[]    lastSampleIds;
	public long[][] alignedOffsetsLists;

	public boolean rebased;

	public Stco()
	{

	}

	Stco(final FullBox box)
	{
		copy(box);
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		final Stco stco = (Stco) super.clone();
		stco.entryCount = entryCount;
/*
                if (entries != null)
                {
                        stco.entries = new StcoEntry[entries.length];
                        for (int i = 0; i < entries.length; i++)
                        {
                                stco.entries[i] = (StcoEntry) entries[i].clone();
                        }
                }
*/
		if (null != offsets)
		{
			stco.offsets = new long[offsets.length];
			System.arraycopy(offsets, 0, stco.offsets, 0, offsets.length);
		}

		if (null != sizes)
		{
			stco.sizes = new long[sizes.length];
			System.arraycopy(sizes, 0, stco.sizes, 0, sizes.length);
		}

		if (null != firstSampleIds)
		{
			stco.firstSampleIds = new int[firstSampleIds.length];
			System.arraycopy(firstSampleIds, 0, stco.firstSampleIds, 0, firstSampleIds.length);
		}

		if (null != lastSampleIds)
		{
			stco.lastSampleIds = new int[lastSampleIds.length];
			System.arraycopy(lastSampleIds, 0, stco.lastSampleIds, 0, lastSampleIds.length);
		}

		if (null != alignedOffsetsLists)
		{
			stco.alignedOffsetsLists = new long[alignedOffsetsLists.length][];
			for (int i = 0; i < alignedOffsetsLists.length; i++)
			{
				stco.alignedOffsetsLists[i] = new long[alignedOffsetsLists[i].length];
				System.arraycopy(alignedOffsetsLists[i], 0, stco.alignedOffsetsLists[i], 0, alignedOffsetsLists[i].length);
			}
		}
		return stco;
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
		//entries = new StcoEntry[(int) entryCount];
		offsets = new long[entryCount];
		sizes = new long[entryCount];
		firstSampleIds = new int[entryCount];
		lastSampleIds = new int[entryCount];
		alignedOffsetsLists = new long[entryCount][];
		for (int i = 0; i < entryCount; i++)
		{
			//entries[i] = new StcoEntry();
			Arrays.fill(buffer, (byte) 0);
			bytes += in.read(buffer);
			offsets[i] = read32(buffer);
		}
		return bytes;
	}

	@Override void write(final OutputStream out) throws IOException
	{
		super.write(out);
		out.write(write32(entryCount));
		for (int i = 0; i < offsets.length; i++)
		{
			out.write(write32((int) offsets[i]));
		}
	}

	@Override void write(final ByteChannel out) throws IOException
	{
		super.write(out);
		out.write(ByteBuffer.wrap(write32(entryCount)));
		for (int i = 0; i < offsets.length; i++)
		{
			out.write(ByteBuffer.wrap(write32((int) offsets[i])));
		}
	}

	public long adjust(final long offset, final int startSample, final long sample_offset)
	{
		long boxAdjust = 0;
		int adjustIndex = 0;

		entryCount = offsets.length;

		for (int i = 0; i < entryCount; i++)
		{
			// this entry is entirely before the requested offset
			if (this.offsets[i] + this.sizes[i] <= sample_offset)
			{
				// we'll drop this entry
				boxAdjust += 4;
				adjustIndex++;
			}
			// this entry is entirely after the requested offset, check the sample ID's to see where the chunk needs to start.
			else if (sample_offset + this.sizes[i] < offsets[i])
			{
				if (startSample <= lastSampleIds[i])
				{
					for (long sampleId = firstSampleIds[i]; sampleId <= lastSampleIds[i]; sampleId++)
					{
						if (sampleId >= startSample)
						{
							offsets[i] = alignedOffsetsLists[i][(int) (startSample - firstSampleIds[i])];
							sizes[i] -= alignedOffsetsLists[i][(int) (startSample - firstSampleIds[i])] - alignedOffsetsLists[i][0];
							break;
						}
					}
					// we won't need to drop any more entries.
					break;
				}
				else
				{
					// drop this one and keep going until we find the chunk containing our start sample.
					boxAdjust += 4;
					adjustIndex++;
				}
			}
			// this entry contains the requested offset
			else
			{
				//
				// the requested offset may not be aligned to the start of a sample within the chunk.
				// check to see if it is...
				//

				long alignmentPoint = 0;
				for (final long a : alignedOffsetsLists[i])
				{
					if (a >= sample_offset)
					{
						alignmentPoint = a;
						break;
					}
				}
				// adjust the chunk's size, presuming that the requested offset is an aligned value.
				sizes[i] -= alignmentPoint - offsets[i];

				// now set the entry's offset to the aligned value.
				offsets[i] = alignmentPoint;

				// we won't need to drop any more entries.
				break;
			}
		}

		// shift the entries that we're keeping to the beginning of the output array.
		//StcoEntry[] dest = new StcoEntry[(int) (entryCount - adjustIndex)];
		final long[] destOffsets = new long[(int) entryCount - adjustIndex];
		final long[] destSizes = new long[(int) entryCount - adjustIndex];
		final int[] destFirstSampleIds = new int[(int) entryCount - adjustIndex];
		final int[] destLastSampleIds = new int[(int) entryCount - adjustIndex];
		final long[][] destAlignedOffsetsLists = new long[(int) entryCount - adjustIndex][];
		for (int i = 0; i < destOffsets.length; i++)
		{
			destOffsets[i] = offsets[i + adjustIndex] - offset;
			if (0 >= destOffsets[i])
			{
				destOffsets[i] = 0;
			}
			destSizes[i] = sizes[i + adjustIndex];
			destFirstSampleIds[i] = firstSampleIds[i + adjustIndex];
			destLastSampleIds[i] = lastSampleIds[i + adjustIndex];
			destAlignedOffsetsLists[i] = new long[alignedOffsetsLists[i + adjustIndex].length];
			System.arraycopy(alignedOffsetsLists[i + adjustIndex], 0, destAlignedOffsetsLists[i], 0, alignedOffsetsLists[i + adjustIndex].length);
		}
		//entries = dest;
		offsets = destOffsets;
		sizes = destSizes;
		firstSampleIds = destFirstSampleIds;
		lastSampleIds = destLastSampleIds;
		alignedOffsetsLists = destAlignedOffsetsLists;
		entryCount = offsets.length;
		boxLen -= boxAdjust;
		return boxAdjust;
	}

	public void rebase(final long offset)
	{
		for (int i = 0; i < entryCount; i++)
		{
			offsets[i] += offset;
		}
		rebased = true;
	}


}
