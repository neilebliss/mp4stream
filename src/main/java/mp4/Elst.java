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

public final class Elst extends FullBox
{
	private static final long serialVersionUID = 3813668149584113026L;
	private int entryCount;
	public ElstEntry[] entries;

	Elst()
	{

	}

	Elst(final FullBox box)
	{
		copy(box);
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		final Elst elst = (Elst) super.clone();
		elst.entryCount = entryCount;
		elst.entries = new ElstEntry[entries.length];
		for (int i = 0; i < entries.length; i++)
		{
			elst.entries[i] = (ElstEntry) entries[i].clone();
		}
		return elst;
	}

	@Override long read(final InputStream in) throws IOException
	{
		return read(in, null);
	}

	long read(final InputStream in, final FullBox fbox) throws IOException
	{
		long bytes = 0;
		if (null == fbox)
		{
			bytes += super.read(in);
		}
		buffer = new byte[4];
		bytes += in.read(buffer);
		entryCount = (int) read32(buffer);
		entries = new ElstEntry[entryCount];
		for (int i = 0; i < entryCount; i++)
		{
			final ElstEntry entry = new ElstEntry();
			if (1 == version)
			{
				buffer = new byte[8];
				bytes += in.read(buffer);
				entry.segmentDuration = read64(buffer);
				Arrays.fill(buffer, (byte) 0);
				bytes += in.read(buffer);
				entry.mediaTime = ByteBuffer.wrap(buffer).getLong();
			}
			else // version == 0
			{
				buffer = new byte[4];
				bytes += in.read(buffer);
				entry.segmentDuration = read32(buffer);
				Arrays.fill(buffer, (byte) 0);
				bytes += in.read(buffer);
				entry.mediaTime = ByteBuffer.wrap(buffer).getInt();
			}
			buffer = new byte[2];
			bytes += in.read(buffer);
			entry.mediaRateInteger = ByteBuffer.wrap(buffer).getShort();
			Arrays.fill(buffer, (byte) 0);
			bytes += in.read(buffer);
			entry.mediaRateFraction = ByteBuffer.wrap(buffer).getShort();
			entries[i] = entry;
		}
		return bytes;
	}

	@Override void write(final OutputStream out) throws IOException
	{
		super.write(out);
		out.write(write32(entryCount));
		for (final ElstEntry entry : entries)
		{
			if (1 == version)
			{
				out.write(ByteBuffer.allocate(8).putLong(entry.segmentDuration).array());
				out.write(ByteBuffer.allocate(8).putLong(entry.mediaTime).array());
			}
			else
			{
				out.write(ByteBuffer.allocate(4).putInt((int) entry.segmentDuration).array());
				out.write(ByteBuffer.allocate(4).putInt((int) entry.mediaTime).array());
			}
			out.write(ByteBuffer.allocate(2).putShort((short) entry.mediaRateInteger).array());
			out.write(ByteBuffer.allocate(2).putShort((short) entry.mediaRateFraction).array());
		}
	}

	@Override void write(final ByteChannel out) throws IOException
	{
		super.write(out);
		out.write(ByteBuffer.wrap(write32(entryCount)));
		for (final ElstEntry entry : entries)
		{
			if (1 == version)
			{
				out.write(ByteBuffer.allocate(8).putLong(entry.segmentDuration));
				out.write(ByteBuffer.allocate(8).putLong(entry.mediaTime));
			}
			else
			{
				out.write(ByteBuffer.allocate(4).putInt((int) entry.segmentDuration));
				out.write(ByteBuffer.allocate(4).putInt((int) entry.mediaTime));
			}
			out.write(ByteBuffer.allocate(2).putShort((short) entry.mediaRateInteger));
			out.write(ByteBuffer.allocate(2).putShort((short) entry.mediaRateFraction));
		}
	}

	long adjust(final float time, final long mvhdTimescale)
	{
		long boxAdjust = 0;
		final ElstEntry[] adjusted = new ElstEntry[entries.length];
		int index = 0;
		final long remove = (long) (time * (float) mvhdTimescale);
		for (final ElstEntry entry : entries)
		{
			final long entryStartTime = entry.mediaTime;
			final long entryEndTime = entryStartTime + entry.segmentDuration;
			if (entryEndTime < remove)
			{
				if (1 == version)
				{
					boxAdjust += 20;
				}
				else
				{
					boxAdjust += 12;
				}

			}
			else if (remove + entry.segmentDuration < entryEndTime)
			{
				entry.mediaTime -= remove;
				adjusted[index++] = entry;
			}
			else // the removal time is within this entry.
			{
				entry.mediaTime -= remove;
				if (0 > entry.mediaTime)
				{
					entry.mediaTime = 0;
				}
				entry.segmentDuration -= remove;
				adjusted[index++] = entry;
			}
		}
		final ElstEntry[] dest = new ElstEntry[index];
		System.arraycopy(adjusted, 0, dest, 0, dest.length);
		entries = dest;
		entryCount = entries.length;
		boxLen -= boxAdjust;
		return boxAdjust;
	}

	public long insertEmptyEdit(final long time)
	{
		long boxAdjust = 0;
		final ElstEntry[] adjusted;
		if (null != entries)
		{
			adjusted = new ElstEntry[entries.length + 1];
		}
		else
		{
			adjusted = new ElstEntry[1];
		}
		int index = 0;
		final ElstEntry dwell = new ElstEntry();
		dwell.mediaTime = -1;
		dwell.segmentDuration = time;
		dwell.mediaRateInteger = 1;
		dwell.mediaRateFraction = 0;
		adjusted[index++] = dwell;
		if (1 == version)
		{
			boxAdjust -= 20;
		}
		else
		{
			boxAdjust -= 12;
		}
		if (null != entries)
		{
			for (final ElstEntry entry : entries)
			{
				adjusted[index++] = entry;
			}
		}
		entries = adjusted;
		entryCount = entries.length;
		return boxAdjust;
	}
}




