/**
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package mp4;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.Arrays;

public final class Ctts extends FullBox
{
	private static final long serialVersionUID = -2326999046813807810L;
	private int entryCount;
	int[] sampleCounts;
	int[] sampleOffsets;
	int[] startSampleIds;
	int[] endSampleIds;


	Ctts()
	{

	}

	Ctts(final FullBox box)
	{
		copy(box);
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		final Ctts ctts = (Ctts) super.clone();
		ctts.entryCount = entryCount;
/*
                ctts.entries = new CttsEntry[(int) entryCount];
                for (int i = 0; i < entries.length; i++)
                {
                        ctts.entries[i] = (CttsEntry) entries[i].clone();
                }
*/
		if (null != sampleCounts)
		{
			ctts.sampleCounts = new int[sampleCounts.length];
			System.arraycopy(sampleCounts, 0, ctts.sampleCounts, 0, sampleCounts.length);
		}
		if (null != sampleOffsets)
		{
			ctts.sampleOffsets = new int[sampleOffsets.length];
			System.arraycopy(sampleOffsets, 0, ctts.sampleOffsets, 0, sampleOffsets.length);
		}
		if (null != startSampleIds)
		{
			ctts.startSampleIds = new int[startSampleIds.length];
			System.arraycopy(startSampleIds, 0, ctts.startSampleIds, 0, startSampleIds.length);
		}
		if (null != endSampleIds)
		{
			ctts.endSampleIds = new int[endSampleIds.length];
			System.arraycopy(endSampleIds, 0, ctts.endSampleIds, 0, endSampleIds.length);
		}
		return ctts;
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
		entryCount = (int) read32(buffer);
		//entries = new CttsEntry[(int) entryCount];
		sampleCounts = new int[entryCount];
		sampleOffsets = new int[entryCount];
		startSampleIds = new int[entryCount];
		endSampleIds = new int[entryCount];

		long sampleCount = 1;
		for (int i = 0; i < entryCount; i++)
		{
			//CttsEntry entry = new CttsEntry();
			Arrays.fill(buffer, (byte) 0);
			bytes += in.read(buffer);
			sampleCounts[i] = (int) read32(buffer);
			Arrays.fill(buffer, (byte) 0);
			bytes += in.read(buffer);
			sampleOffsets[i] = (int) read32(buffer);
			startSampleIds[i] = (int) sampleCount;
			endSampleIds[i] = (int) sampleCount + this.sampleCounts[i] - 1;
			sampleCount = endSampleIds[i] + 1;
			//entries[i] = entry;
		}
		return bytes;
	}

	@Override void write(final OutputStream out) throws IOException
	{
		super.write(out);
		out.write(write32(entryCount));
/*
                for (CttsEntry entry : entries)
                {
                        out.write(write32(entry.sampleCount));
                        out.write(write32(entry.sampleOffset));
                }
*/
		for (int i = 0; i < entryCount; i++)
		{
			out.write(write32(sampleCounts[i]));
			out.write(write32(sampleOffsets[i]));
		}
	}

	@Override void write(final ByteChannel out) throws IOException
	{
		super.write(out);
		out.write(ByteBuffer.wrap(write32(entryCount)));
/*
                for (CttsEntry entry : entries)
                {
                        out.write(ByteBuffer.wrap(write32(entry.sampleCount)));
                        out.write(ByteBuffer.wrap(write32(entry.sampleOffset)));
                }
*/
		for (int i = 0; i < entryCount; i++)
		{
			out.write(ByteBuffer.wrap(write32(sampleCounts[i])));
			out.write(ByteBuffer.wrap(write32(sampleOffsets[i])));
		}
	}


	long adjust(final Integer startSample)
	{
		long boxAdjust = 0;
		int index = 0;
		int adjustIndex = 0;
		if (null != startSample)
		{
			for (int i = 0; i < entryCount; i++)
			{
				// if the last sample ID in this entry's range is smaller than the starting sample ID...
				if (endSampleIds[i] < startSample)
				{
					index += sampleCounts[i];
					adjustIndex++;
					// each entry is 8 bytes on disk.
					boxAdjust += 8;
				}
				else
				{
					// check to see if the starting sample ID falls in the middle of this entry someplace.
					// if the startOfEntrySampleRange is equal to the starting sample ID, then no adjustment of the entry is needed.
					if (startSample > this.startSampleIds[i] && startSample <= this.endSampleIds[i])
					{
						sampleCounts[i] = this.endSampleIds[i] - startSample + 1;
					}
					break;
				}
			}
			// now we know how many to throw away
			final int[] destSampleCounts = new int[entryCount - adjustIndex];
			System.arraycopy(sampleCounts, adjustIndex, destSampleCounts, 0, destSampleCounts.length);
			sampleCounts = destSampleCounts;
			final int[] destSampleOffsets = new int[entryCount - adjustIndex];
			System.arraycopy(sampleOffsets, adjustIndex, destSampleOffsets, 0, destSampleOffsets.length);
			sampleOffsets = destSampleOffsets;
			final int[] destStartSampleIds = new int[entryCount - adjustIndex];
			System.arraycopy(startSampleIds, adjustIndex, destStartSampleIds, 0, destStartSampleIds.length);
			startSampleIds = destStartSampleIds;
			final int[] destEndSampleIds = new int[entryCount - adjustIndex];
			System.arraycopy(endSampleIds, adjustIndex, destEndSampleIds, 0, destEndSampleIds.length);
			endSampleIds = destEndSampleIds;
			entryCount = sampleCounts.length;
		}
		boxLen -= boxAdjust;
		return boxAdjust;
	}
}
