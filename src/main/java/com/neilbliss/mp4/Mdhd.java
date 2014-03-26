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

public final class Mdhd extends FullBox
{
	private static final long serialVersionUID = -3503501038022540217L;
	private long creationTime;
	private long modificationTime;
	public  int  timescale;
	public  long duration;

	// the spec for this box specifies a 1 bit pad.  I have no idea how to only read 1 bit off disk.
	// Since the remaining data in this box (pad, language, pre-defined) doesn't need to be adjusted,
	// we will simply store the rest of this box in a byte array.
	private byte[] boxRemainder;

	Mdhd()
	{

	}

	Mdhd(final FullBox box)
	{
		copy(box);
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		final Mdhd mdhd = (Mdhd) super.clone();
		if (null != boxRemainder)
		{
			mdhd.boxRemainder = boxRemainder.clone();
		}
		return mdhd;
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
		buffer = new byte[8];
		if (1 == version)
		{
			bytes += in.read(buffer);
			creationTime = read64(buffer);
			Arrays.fill(buffer, (byte) 0);
			bytes += in.read(buffer);
			modificationTime = read64(buffer);
			buffer = new byte[4];
			bytes += in.read(buffer);
			timescale = read32(buffer);
			buffer = new byte[8];
			bytes += in.read(buffer);
			duration = read64(buffer);
			boxRemaining -= 28;
		}
		else // version == 0
		{
			buffer = new byte[4];
			bytes += in.read(buffer);
			creationTime = read32(buffer);
			Arrays.fill(buffer, (byte) 0);
			bytes += in.read(buffer);
			modificationTime = read32(buffer);
			Arrays.fill(buffer, (byte) 0);
			bytes += in.read(buffer);
			timescale = read32(buffer);
			Arrays.fill(buffer, (byte) 0);
			bytes += in.read(buffer);
			duration = read32(buffer);
			Arrays.fill(buffer, (byte) 0);
			boxRemaining -= 16;
		}

		// shove the rest of the data, unaltered, into a byte buffer.
		boxRemainder = new byte[(int) boxRemaining];
		bytes += in.read(boxRemainder);
		return bytes;
	}

	@Override void write(final OutputStream out) throws IOException
	{
		super.write(out);
		if (1 == version)
		{
			out.write(write64(creationTime));
			out.write(write64(modificationTime));
			out.write(write32(timescale));
			out.write(write64(duration));
		}
		else // version == 0
		{
			out.write(write32((int) creationTime));
			out.write(write32((int) modificationTime));
			out.write(write32(timescale));
			out.write(write32((int) duration));
		}
		out.write(boxRemainder);
	}

	@Override void write(final ByteChannel out) throws IOException
	{
		super.write(out);
		if (1 == version)
		{
			out.write(ByteBuffer.wrap(write64(creationTime)));
			out.write(ByteBuffer.wrap(write64(modificationTime)));
			out.write(ByteBuffer.wrap(write32(timescale)));
			out.write(ByteBuffer.wrap(write64(duration)));
		}
		else // version == 0
		{
			out.write(ByteBuffer.wrap(write32((int) creationTime)));
			out.write(ByteBuffer.wrap(write32((int) modificationTime)));
			out.write(ByteBuffer.wrap(write32(timescale)));
			out.write(ByteBuffer.wrap(write32((int) duration)));
		}
		out.write(ByteBuffer.wrap(boxRemainder));
	}


	long adjust(final float time)
	{
		final long boxAdjust = 0;
		duration -= (long) (time * (float) timescale);
		return boxAdjust;
	}
}
