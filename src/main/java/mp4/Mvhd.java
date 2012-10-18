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

import mp4.FullBox;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.Arrays;

public final class Mvhd extends FullBox
{
	private static final long serialVersionUID = 3939703325308595432L;
	private long  creationTime;
	private long  modificationTime;
	public  int   timescale;
	public  long  duration;
	private int   rate;
	private long  volume;
	private int[] reserved         = new int[3];
	private int[] matrix           = new int[9];
	private int[] preDefined       = new int[6];
	private int   nextTrackId;

	Mvhd()
	{

	}

	Mvhd(final FullBox box)
	{
		copy(box);
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		final Mvhd mvhd = (Mvhd) super.clone();
		if (null != reserved)
		{
			mvhd.reserved = new int[reserved.length];
			System.arraycopy(reserved, 0, mvhd.reserved, 0, reserved.length);
		}
		if (null != matrix)
		{
			mvhd.matrix = new int[matrix.length];
			System.arraycopy(matrix, 0, mvhd.matrix, 0, matrix.length);
		}
		if (null != preDefined)
		{
			mvhd.preDefined = new int[preDefined.length];
			System.arraycopy(preDefined, 0, mvhd.preDefined, 0, preDefined.length);
		}
		return mvhd;
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
		if (1 == version)
		{
			buffer = new byte[8];
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
		}
		else
		{
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
		}
		buffer = new byte[4];
		bytes += in.read(buffer);
		rate = read32(buffer);
		buffer = new byte[2];
		bytes += in.read(buffer);
		volume = read16(buffer);
		Arrays.fill(buffer, (byte) 0);
		bytes += in.read(buffer);
		reserved[0] = (int) read16(buffer);
		buffer = new byte[4];
		bytes += in.read(buffer);
		reserved[1] = read32(buffer);
		Arrays.fill(buffer, (byte) 0);
		bytes += in.read(buffer);
		reserved[2] = read32(buffer);
		int i;
		for (i = 0; 9 > i; i++)
		{
			Arrays.fill(buffer, (byte) 0);
			bytes += in.read(buffer);
			matrix[i] = read32(buffer);
		}
		for (i = 0; 6 > i; i++)
		{
			Arrays.fill(buffer, (byte) 0);
			bytes += in.read(buffer);
			preDefined[i] = read32(buffer);
		}
		Arrays.fill(buffer, (byte) 0);
		bytes += in.read(buffer);
		nextTrackId = read32(buffer);

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
		else
		{
			out.write(write32((int) creationTime));
			out.write(write32((int) modificationTime));
			out.write(write32(timescale));
			out.write(write32((int) duration));
		}
		out.write(write32(rate));
		out.write(write16(volume));
		out.write(write16(reserved[0]));
		out.write(write32(reserved[1]));
		out.write(write32(reserved[2]));
		int i;
		for (i = 0; 9 > i; i++)
		{
			out.write(write32(matrix[i]));
		}
		for (i = 0; 6 > i; i++)
		{
			out.write(write32(preDefined[i]));
		}
		out.write(write32(nextTrackId));
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
		else
		{
			out.write(ByteBuffer.wrap(write32((int) creationTime)));
			out.write(ByteBuffer.wrap(write32((int) modificationTime)));
			out.write(ByteBuffer.wrap(write32(timescale)));
			out.write(ByteBuffer.wrap(write32((int) duration)));
		}
		out.write(ByteBuffer.wrap(write32(rate)));
		out.write(ByteBuffer.wrap(write16(volume)));
		out.write(ByteBuffer.wrap(write16(reserved[0])));
		out.write(ByteBuffer.wrap(write32(reserved[1])));
		out.write(ByteBuffer.wrap(write32(reserved[2])));
		int i;
		for (i = 0; 9 > i; i++)
		{
			out.write(ByteBuffer.wrap(write32(matrix[i])));
		}
		for (i = 0; 6 > i; i++)
		{
			out.write(ByteBuffer.wrap(write32(preDefined[i])));
		}
		out.write(ByteBuffer.wrap(write32(nextTrackId)));
	}

	long adjust(final float time)
	{
		final long boxAdjust = 0;
		final long delta = (long) (time * (float) timescale);
		duration -= delta;
		return boxAdjust;
	}
}
