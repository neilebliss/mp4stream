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

package com.neilbliss.mp4;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.Arrays;

public final class Stts extends FullBox
{
	private static final long serialVersionUID = 2952427395084751069L;
	private int entryCount;
	//public SttsEntry[] entries;
	public int[] sampleCounts;
	public int[] sampleDeltas;
	public int[] startTimes;
	public int[] endTimes;

	public Stts()
	{

	}

	Stts(final FullBox box)
	{
		copy(box);
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		final Stts stts = (Stts) super.clone();
		// stts.entryCount = entryCount;
/*
                if (entries != null)
                {
                        stts.entries = new SttsEntry[entries.length];
                        for (int i = 0; i < entries.length; i++)
                        {
                                stts.entries[i] = (SttsEntry) entries[i].clone();
                        }
                }
*/
		if (null != sampleCounts)
		{
			stts.sampleCounts = new int[sampleCounts.length];
			System.arraycopy(sampleCounts, 0, stts.sampleCounts, 0, sampleCounts.length);
		}
		if (null != sampleDeltas)
		{
			stts.sampleDeltas = new int[sampleDeltas.length];
			System.arraycopy(sampleDeltas, 0, stts.sampleDeltas, 0, sampleDeltas.length);
		}
		if (null != startTimes)
		{
			stts.startTimes = new int[startTimes.length];
			System.arraycopy(startTimes, 0, stts.startTimes, 0, startTimes.length);
		}
		if (null != endTimes)
		{
			stts.endTimes = new int[endTimes.length];
			System.arraycopy(endTimes, 0, stts.endTimes, 0, endTimes.length);
		}
		return stts;
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
		//entries = new SttsEntry[(int) entryCount];
		sampleCounts = new int[(int) entryCount];
		sampleDeltas = new int[(int) entryCount];
		startTimes = new int[(int) entryCount];
		endTimes = new int[(int) entryCount];
		long timeIndex = 0;
		for (int i = 0; i < entryCount; i++)
		{
			//SttsEntry entry = new SttsEntry();
			Arrays.fill(buffer, (byte) 0);
			bytes += in.read(buffer);
			sampleCounts[i] = read32(buffer);
			Arrays.fill(buffer, (byte) 0);
			bytes += in.read(buffer);
			sampleDeltas[i] = read32(buffer);
			startTimes[i] = (int) timeIndex;
			endTimes[i] = startTimes[i] + this.sampleCounts[i] * this.sampleDeltas[i];
			timeIndex += sampleCounts[i] * sampleDeltas[i];
			//entries[i] = entry;
		}
		return bytes;
	}

	@Override void write(final OutputStream out) throws IOException
	{
		super.write(out);
		out.write(write32(entryCount));
/*
                for (SttsEntry entry : entries)
                {
                        out.write(write32(entry.sampleCount));
                        out.write(write32(entry.sampleDelta));
                }
*/
		for (int i = 0; i < entryCount; i++)
		{
			out.write(write32(sampleCounts[i]));
			out.write(write32(sampleDeltas[i]));
		}
	}

	@Override void write(final ByteChannel out) throws IOException
	{
		super.write(out);
		out.write(ByteBuffer.wrap(write32(entryCount)));
/*
                for (SttsEntry entry : entries)
                {
                        out.write(ByteBuffer.wrap(write32(entry.sampleCount)));
                        out.write(ByteBuffer.wrap(write32(entry.sampleDelta)));
                }
*/
		for (int i = 0; i < entryCount; i++)
		{
			out.write(ByteBuffer.wrap(write32(sampleCounts[i])));
			out.write(ByteBuffer.wrap(write32(sampleDeltas[i])));
		}
	}

	public long adjust(final long time)
	{
		long boxAdjust = 0;
		//SttsEntry[] adjusted = new SttsEntry[entries.length];
		final int[] adjustedSampleCounts = new int[sampleCounts.length];
		final int[] adjustedSampleDeltas = new int[sampleDeltas.length];
		final int[] adjustedStartTimes = new int[startTimes.length];
		final int[] adjustedEndTimes = new int[endTimes.length];
		int adjustedIndex = 0;
		for (int i = 0; i < entryCount; i++)
		{
			if (endTimes[i] <= time)
			{
				// this entry is prior to the requested time
				boxAdjust += 8;
			}
			else if (time >= this.startTimes[i] && this.endTimes[i] > time)
			{
				// this entry contains the requested time
				// subdivide
				final long removeTime = time - startTimes[i];
				sampleCounts[i] -= removeTime / sampleDeltas[i];
				startTimes[i] -= time;
				if (0 > startTimes[i])
				{
					startTimes[i] = 0;
				}
				endTimes[i] -= time;
				//adjusted[adjustedIndex++] = entry;
				adjustedSampleCounts[adjustedIndex] = sampleCounts[i];
				adjustedSampleDeltas[adjustedIndex] = sampleDeltas[i];
				adjustedStartTimes[adjustedIndex] = startTimes[i];
				adjustedEndTimes[adjustedIndex] = endTimes[i];
				adjustedIndex++;
			}
			else
			{
				// this entry is after the requested time.
				startTimes[i] -= time;
				endTimes[i] -= time;
				//adjusted[adjustedIndex++] = entry;
				adjustedSampleCounts[adjustedIndex] = sampleCounts[i];
				adjustedSampleDeltas[adjustedIndex] = sampleDeltas[i];
				adjustedStartTimes[adjustedIndex] = startTimes[i];
				adjustedEndTimes[adjustedIndex] = endTimes[i];
				adjustedIndex++;
			}
		}
		//SttsEntry[] dest = new SttsEntry[adjustedIndex];
		//System.arraycopy(adjusted, 0, dest, 0, dest.length);
		//entries = dest;
		final int[] destSampleCounts = new int[adjustedIndex];
		System.arraycopy(adjustedSampleCounts, 0, destSampleCounts, 0, destSampleCounts.length);
		sampleCounts = destSampleCounts;

		final int[] destSampleDeltas = new int[adjustedIndex];
		System.arraycopy(adjustedSampleDeltas, 0, destSampleDeltas, 0, destSampleDeltas.length);
		sampleDeltas = destSampleDeltas;

		final int[] destStartTimes = new int[adjustedIndex];
		System.arraycopy(adjustedStartTimes, 0, destStartTimes, 0, destStartTimes.length);
		startTimes = destStartTimes;

		final int[] destEndTimes = new int[adjustedIndex];
		System.arraycopy(adjustedEndTimes, 0, destEndTimes, 0, destEndTimes.length);
		endTimes = destEndTimes;

		entryCount = sampleCounts.length;
		boxLen -= boxAdjust;
		return boxAdjust;
	}

	public int find(final float time)
	{
		// find the sampleId corresponding to the time specified.
		Long sampleIndex = 0l;
		for (int i = 0; i < entryCount; i++)
		{
			if (endTimes[i] < time)
			{
				// this entry is before the requested time.
				sampleIndex += sampleCounts[i];
			}
			else if (time >= this.startTimes[i] && this.endTimes[i] >= time)
			{
				// this entry contains the requested time.
				sampleIndex += (long) (time / sampleDeltas[i]);
				break;
			}
		}
		return sampleIndex.intValue();
	}

	public void createEntries(final int numentries)
	{
/*
		int i;
		entries = new SttsEntry[numentries];
		for (i = 0; i < numentries; i++)
		{
			entries[i] = new SttsEntry();
		}
*/
		sampleCounts = new int[numentries];
		sampleDeltas = new int[numentries];
		startTimes = new int[numentries];
		endTimes = new int[numentries];
		entryCount = numentries;
	}

}
