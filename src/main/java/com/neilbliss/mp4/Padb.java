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

public final class Padb extends FullBox
{
	private static final long serialVersionUID = 5538752455192075458L;
	private int entryCount;
	private byte[] entries;

	Padb()
	{

	}

	Padb(final FullBox box)
	{
		copy(box);
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		final Padb padb = (Padb) super.clone();
		padb.entryCount = entryCount;
		if (null != entries)
		{
			padb.entries = new byte[entries.length];
			System.arraycopy(entries, 0, padb.entries, 0, entries.length);
		}
		return padb;
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
		entries = new byte[(int) entryCount];
		for (int i = 0; i < entryCount; i++)
		{
			buffer = new byte[1];
			bytes += in.read(buffer);
			entries[i] = buffer[0];
		}
		return bytes;
	}

	@Override void write(final OutputStream out) throws IOException
	{
		super.write(out);
		out.write(write32(entryCount));
		for (final byte entry : entries)
		{
			out.write(entry);
		}
	}

	@Override void write(final ByteChannel out) throws IOException
	{
		super.write(out);
		out.write(ByteBuffer.wrap(write32(entryCount)));
		final byte[] buffer = new byte[1];
		for (final byte entry : entries)
		{
			buffer[0] = entry;
			out.write(ByteBuffer.wrap(buffer));
		}
	}

	long adjust(final Integer startSample)
	{
		long boxAdjust = 0;
		if (null != startSample)
		{
			final byte[] adjusted = new byte[(int) (entryCount - startSample)];
			System.arraycopy(entries, 0 + startSample - 1, adjusted, 0, adjusted.length);
			entries = adjusted;
			entryCount = entries.length;
			boxAdjust = startSample * 4;
		}
		boxLen -= boxAdjust;
		return boxAdjust;
	}
}
