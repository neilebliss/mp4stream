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

public final class Stz2 extends FullBox
{
	private static final long serialVersionUID = 4764223125851220715L;
	public  int    sampleSize;
	private byte[] reserved   = new byte[3];
	private int    fieldSize;
	public  int    entryCount;
	public StszEntry[] entries;

	public Stz2()
	{

	}

	public Stz2(final FullBox box)
	{
		copy(box);
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		final Stz2 stz2 = (Stz2) super.clone();
		if (null != reserved)
		{
			stz2.reserved = new byte[reserved.length];
			System.arraycopy(reserved, 0, stz2.reserved, 0, reserved.length);
		}
		stz2.entryCount = entryCount;
		if (null != entries)
		{
			stz2.entries = new StszEntry[entries.length];
			System.arraycopy(entries, 0, stz2.entries, 0, entries.length);
		}
		return stz2;
	}

	@Override public long read(final InputStream in) throws IOException
	{
		return read(in, null);
	}

	public long read(final InputStream in, final FullBox box) throws IOException
	{
		long bytes = 0;
		if (null == box)
		{
			bytes += super.read(in);
		}
		buffer = new byte[3];
		bytes += in.read(buffer);
		reserved = buffer;
		buffer = new byte[1];
		bytes += in.read(buffer);
		fieldSize = (int) read8(buffer);
		buffer = new byte[4];
		bytes += in.read(buffer);
		entryCount = read32(buffer);
		entries = new StszEntry[(int) entryCount];
		for (int i = 0; i < entryCount; i++)
		{
			if (4 == fieldSize)
			{
				buffer = new byte[1];
				bytes += in.read(buffer);
				final short value = (short) this.read8(this.buffer);
				entries[i].size = value << 4;
				entries[i].sampleId = i + 1;
				entries[++i].size = value >> 4;
				entries[i].sampleId = i + 1;
			}
			else if (8 == fieldSize)
			{
				buffer = new byte[1];
				bytes += in.read(buffer);
				entries[i].size = read8(buffer);
				entries[i].sampleId = i + 1;
			}
			else if (16 == fieldSize)
			{
				buffer = new byte[2];
				bytes += in.read(buffer);
				entries[i].size = read16(buffer);
				entries[i].sampleId = i + 1;
			}
		}
		return bytes;
	}

	@Override public void write(final OutputStream out) throws IOException
	{
		super.write(out);
		out.write(reserved);
		out.write(write8(fieldSize));
		out.write(write32(entryCount));
		for (int i = 0; i < entryCount; i++)
		{
			if (4 == fieldSize)
			{
				final short a = (short) entries[i].size;
				final short b = (short) entries[++i].size;
				final short c = (short) (a >> 4 & b << 4);
				out.write(ByteBuffer.allocate(1).putShort(c).array());
			}
			else if (8 == fieldSize)
			{
				out.write(write8(entries[i].size));
			}
			else if (16 == fieldSize)
			{
				out.write(write16(entries[i].size));
			}
		}
	}

	@Override public void write(final ByteChannel out) throws IOException
	{
		super.write(out);
		out.write(ByteBuffer.wrap(reserved));
		out.write(ByteBuffer.wrap(write8(fieldSize)));
		out.write(ByteBuffer.wrap(write32(entryCount)));
		for (int i = 0; i < entryCount; i++)
		{
			if (4 == fieldSize)
			{
				final short a = (short) entries[i].size;
				final short b = (short) entries[++i].size;
				final short c = (short) (a >> 4 & b << 4);
				out.write(ByteBuffer.allocate(1).putShort(c));
			}
			else if (8 == fieldSize)
			{
				out.write(ByteBuffer.wrap(write8(entries[i].size)));
			}
			else if (16 == fieldSize)
			{
				out.write(ByteBuffer.wrap(write16(entries[i].size)));
			}
		}
	}

	public long adjust(final Integer startSample)
	{
		long boxAdjust = 0;
		if (null != startSample)
		{
			int adjustIndex = 0;
			boolean halfByteRemoved = false;
			for (final StszEntry entry : entries)
			{
				if (entry.sampleId < startSample)
				{
					adjustIndex++;
					if (4 == fieldSize)
					{
						if (halfByteRemoved)
						{
							halfByteRemoved = false;
							boxAdjust++;
						}
						else
						{
							halfByteRemoved = true;
						}
					}
					else
					{
						boxAdjust += fieldSize / 8;
					}
				}
				else
				{
					break;
				}
			}
			final StszEntry[] dest = new StszEntry[(int) (entryCount - adjustIndex)];
			for (int i = 0; i < dest.length; i++)
			{
				dest[i] = new StszEntry();
				dest[i].sampleId = this.entries[i + adjustIndex].sampleId - startSample + 1;
			}
			entries = dest;
			entryCount = entries.length;
		}
		boxLen -= boxAdjust;
		return boxAdjust;
	}

	public void createEntries(final int numentries)
	{
		int i;
		entries = new StszEntry[numentries];
		for (i = 0; i < numentries; i++)
		{
			entries[i] = new StszEntry();
		}

		entryCount = numentries;
	}

}
