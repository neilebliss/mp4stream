

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

public final class Stsc extends FullBox
{
	private static final long serialVersionUID = 2821168028562190419L;
	public int entryCount;
	public StscEntry[] entries;

	public Stsc()
	{

	}

	Stsc(final FullBox box)
	{
		copy(box);
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		final Stsc stsc = (Stsc) super.clone();
		stsc.entryCount = entryCount;
		if (null != entries)
		{
			stsc.entries = new StscEntry[entries.length];
			for (int i = 0; i < entries.length; i++)
			{
				stsc.entries[i] = (StscEntry) entries[i].clone();
			}
		}
		return stsc;
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
		entries = new StscEntry[(int) entryCount];
		int sampleCountIndex = 1;
		for (int i = 0; i < entryCount; i++)
		{
			final StscEntry entry = new StscEntry();
			Arrays.fill(buffer, (byte) 0);
			bytes += in.read(buffer);
			entry.firstChunk = read32(buffer);
			Arrays.fill(buffer, (byte) 0);
			bytes += in.read(buffer);
			entry.samplesPerChunk = read32(buffer);
			Arrays.fill(buffer, (byte) 0);
			bytes += in.read(buffer);
			entry.sampleDescriptionIndex = read32(buffer);
			entry.chunkGroupId = i;
			entries[i] = entry;
			if (0 < i)
			{
				entries[i - 1].chunks = entries[i].firstChunk - entries[i - 1].firstChunk;
			}
			// todo - how to populate the number of chunks in the last stsc entry? I would need to know the total number of chunks in this track.  Where is this stored?
		}
		for (final StscEntry entry : entries)
		{
			// set up the sample counts.
			entry.firstSampleId = sampleCountIndex;
			sampleCountIndex += entry.samplesPerChunk * entry.chunks;
		}
		return bytes;
	}

	@Override void write(final OutputStream out) throws IOException
	{
		super.write(out);
		out.write(write32(entryCount));
		for (final StscEntry entry : entries)
		{
			out.write(write32(entry.firstChunk));
			out.write(write32(entry.samplesPerChunk));
			out.write(write32(entry.sampleDescriptionIndex));
		}
	}

	@Override void write(final ByteChannel out) throws IOException
	{
		super.write(out);
		out.write(ByteBuffer.wrap(write32(entryCount)));
		for (final StscEntry entry : entries)
		{
			out.write(ByteBuffer.wrap(write32(entry.firstChunk)));
			out.write(ByteBuffer.wrap(write32(entry.samplesPerChunk)));
			out.write(ByteBuffer.wrap(write32(entry.sampleDescriptionIndex)));
		}
	}

	public long adjust(final Integer startSample)
	{
		long boxAdjust = 0;
		int sampleIndex = 0;
		// we need to make this entries + 1 in case there's only 1 entry in the original and we need to split it.
		final StscEntry[] adjusted = new StscEntry[entries.length + 1];
		int adjustedIndex = 0;
		int chunkGroupIdIndex = 0;
		int nextChunkGroupStartValue = 0;
		if (null != startSample)
		{
			for (final StscEntry entry : entries)
			{
				final int originalNumberOfChunks = entry.chunks;
				final int firstSampleThisEntry = sampleIndex + 1;
				final int lastSampleThisEntry = sampleIndex + entry.samplesPerChunk * entry.chunks;
				// each stsc entry represents (samplesPerChunk * number of chunks) samples.
				if (lastSampleThisEntry < startSample)
				{
					// each entry is 12 bytes on disk.
					boxAdjust += 12;
				}
				else if (startSample == firstSampleThisEntry)
				{
					// the desired sample is exactly at the start of this stco entry.
					entry.firstChunk = 1;
					entry.firstSampleId = 1;
					entry.chunkGroupId = chunkGroupIdIndex++;
					nextChunkGroupStartValue = entry.firstChunk + entry.chunks;
					adjusted[adjustedIndex++] = entry;
				}
				else if (startSample > firstSampleThisEntry && startSample <= lastSampleThisEntry)
				{
					// this stsc record contains the startSample.  adjust.
					final long chunkNumberForStartSample = (startSample - 1 - sampleIndex) / entry.samplesPerChunk;
					final StscEntry newEntry = new StscEntry();
					boxAdjust -= 12;
					newEntry.firstChunk = 1;
					newEntry.samplesPerChunk = entry.samplesPerChunk - (startSample - firstSampleThisEntry) % entry.samplesPerChunk;
					newEntry.chunkGroupId = chunkGroupIdIndex++;
					newEntry.chunks = 1;
					newEntry.firstSampleId = 1;
					newEntry.sampleDescriptionIndex = entry.sampleDescriptionIndex;
					nextChunkGroupStartValue = 2;
					adjusted[adjustedIndex++] = newEntry;
					if (entry.chunks > chunkNumberForStartSample + 1)
					{
						entry.chunks -= chunkNumberForStartSample + 1;
						entry.firstChunk = 2;
						entry.firstSampleId = newEntry.firstSampleId + newEntry.samplesPerChunk * newEntry.chunks;
						entry.chunkGroupId = chunkGroupIdIndex++;
						nextChunkGroupStartValue += entry.chunks;
						adjusted[adjustedIndex++] = entry;
					}
					else
					{
						boxAdjust += 12;
					}
				}
				else
				{
					// downstream entries, we need to fix up the firstChunk value to match the new series, and also fix up firstSampleId to make things convenient for debugging purposes.
					entry.firstChunk = nextChunkGroupStartValue;
					entry.firstSampleId = entry.firstSampleId - startSample + 1;
					entry.chunkGroupId = chunkGroupIdIndex++;
					nextChunkGroupStartValue += entry.chunks;
					adjusted[adjustedIndex++] = entry;
				}
				sampleIndex += entry.samplesPerChunk * originalNumberOfChunks;
			}
			final StscEntry[] dest = new StscEntry[adjustedIndex];
			System.arraycopy(adjusted, 0, dest, 0, dest.length);
			entries = dest;
			entryCount = entries.length;
		}
		boxLen -= boxAdjust;
		return boxAdjust;
	}

	public int find(final long sampleId)
	{
		// return the integer value chunk number containing the specified sample.
		long sampleIndex = 0;
		int chunkIndex = 0;
		for (final StscEntry entry : entries)
		{
			if (sampleIndex + entry.samplesPerChunk * entry.chunks < sampleId)
			{
				sampleIndex += entry.samplesPerChunk * entry.chunks;
				chunkIndex += entry.chunks;
			}
			else if (sampleId >= sampleIndex && sampleIndex + entry.samplesPerChunk * entry.chunks >= sampleId)
			{
				chunkIndex += (sampleId - sampleIndex) / entry.samplesPerChunk;
				break;
			}
			else
			{
				// ruh-roh, we got past the sampleId and didn't find a chunk for it.
				chunkIndex = -1;
			}
		}
		return chunkIndex;
	}


}
