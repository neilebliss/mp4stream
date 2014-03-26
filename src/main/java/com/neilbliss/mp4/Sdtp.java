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

public final class Sdtp extends FullBox
{
	private static final long serialVersionUID = -6039275315154741796L;
	private byte[] entries;

	Sdtp()
	{

	}

	Sdtp(final FullBox box)
	{
		copy(box);
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		final Sdtp sdtp = (Sdtp) super.clone();
		if (null != entries)
		{
			sdtp.entries = new byte[entries.length];
			System.arraycopy(entries, 0, sdtp.entries, 0, entries.length);
		}
		return sdtp;
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

		final int capacity = (int) boxRemaining;
		entries = new byte[capacity];
		bytes += in.read(entries);
		return bytes;
	}

	@Override void write(final OutputStream out) throws IOException
	{
		super.write(out);
		out.write(entries);
	}

	@Override void write(final ByteChannel out) throws IOException
	{
		super.write(out);
		out.write(ByteBuffer.wrap(entries));
	}


	long adjust(final Integer startSample)
	{
		long boxAdjust = 0;
		if (null != startSample)
		{
			final byte[] adjusted = new byte[entries.length - startSample];
			System.arraycopy(entries, 0 + startSample, adjusted, 0, adjusted.length);
			entries = adjusted;
			boxAdjust = startSample;
		}
		boxLen -= boxAdjust;
		return boxAdjust;
	}
}
