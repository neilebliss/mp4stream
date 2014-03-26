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

public final class Tkhd extends FullBox
{
	private static final long serialVersionUID = -489652953590780715L;
	private long  creationTime;
	private long  modificationTime;
	public  int   trackId;
	private int[] reserved         = new int[4];
	long duration;
	private long  layer;
	private long  alternateGroup;
	private long  volume;
	private int[] matrix         = new int[9];
	private int   width;
	private int   height;

	long firstMediaTime;

	Tkhd()
	{

	}

	Tkhd(final FullBox box)
	{
		copy(box);
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		final Tkhd tkhd = (Tkhd) super.clone();
		if (null != reserved)
		{
			tkhd.reserved = new int[reserved.length];
			System.arraycopy(reserved, 0, tkhd.reserved, 0, reserved.length);
		}
		if (null != matrix)
		{
			tkhd.matrix = new int[matrix.length];
			System.arraycopy(matrix, 0, tkhd.matrix, 0, matrix.length);
		}
		return tkhd;
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
			trackId = read32(buffer);
			Arrays.fill(buffer, (byte) 0);
			bytes += in.read(buffer);
			reserved[0] = read32(buffer);
			buffer = new byte[8];
			bytes += in.read(buffer);
			duration = read64(buffer);
		}
		else // version == 0
		{
			bytes += in.read(buffer);
			creationTime = read32(buffer);
			Arrays.fill(buffer, (byte) 0);
			bytes += in.read(buffer);
			modificationTime = read32(buffer);
			Arrays.fill(buffer, (byte) 0);
			bytes += in.read(buffer);
			trackId = read32(buffer);
			Arrays.fill(buffer, (byte) 0);
			bytes += in.read(buffer);
			reserved[0] = read32(buffer);
			Arrays.fill(buffer, (byte) 0);
			bytes += in.read(buffer);
			duration = read32(buffer);
			Arrays.fill(buffer, (byte) 0);
		}
		buffer = new byte[4];
		bytes += in.read(buffer);
		reserved[1] = read32(buffer);
		Arrays.fill(buffer, (byte) 0);
		bytes += in.read(buffer);
		reserved[2] = read32(buffer);
		buffer = new byte[2];
		bytes += in.read(buffer);
		layer = ByteBuffer.wrap(buffer).getShort();
		Arrays.fill(buffer, (byte) 0);
		bytes += in.read(buffer);
		alternateGroup = ByteBuffer.wrap(buffer).getShort();
		Arrays.fill(buffer, (byte) 0);
		bytes += in.read(buffer);
		volume = ByteBuffer.wrap(buffer).getShort();
		Arrays.fill(buffer, (byte) 0);
		bytes += in.read(buffer);
		reserved[3] = ByteBuffer.wrap(buffer).getShort();
		buffer = new byte[4];
		for (int i = 0; 9 > i; i++)
		{
			Arrays.fill(buffer, (byte) 0);
			bytes += in.read(buffer);
			matrix[i] = read32(buffer);
		}
		Arrays.fill(buffer, (byte) 0);
		bytes += in.read(buffer);
		width = read32(buffer);
		Arrays.fill(buffer, (byte) 0);
		bytes += in.read(buffer);
		height = read32(buffer);
		return bytes;
	}

	@Override void write(final OutputStream out) throws IOException
	{
		super.write(out);
		if (1 == version)
		{
			out.write(write64(creationTime));
			out.write(write64(modificationTime));
			out.write(write32(trackId));
			out.write(write32(reserved[0]));
			out.write(write64(duration));
		}
		else // version == 0
		{
			out.write(write32((int) creationTime));
			out.write(write32((int) modificationTime));
			out.write(write32(trackId));
			out.write(write32(reserved[0]));
			out.write(write32((int) duration));
		}
		out.write(write32(reserved[1]));
		out.write(write32(reserved[2]));
		out.write(ByteBuffer.allocate(2).putShort((short) layer).array());
		out.write(ByteBuffer.allocate(2).putShort((short) alternateGroup).array());
		out.write(ByteBuffer.allocate(2).putShort((short) volume).array());
		out.write(ByteBuffer.allocate(2).putShort((short) reserved[3]).array());
		for (int i = 0; 9 > i; i++)
		{
			out.write(write32(matrix[i]));
		}
		out.write(write32(width));
		out.write(write32(height));
	}

	@Override void write(final ByteChannel out) throws IOException
	{
		super.write(out);
		if (1 == version)
		{
			out.write(ByteBuffer.wrap(write64(creationTime)));
			out.write(ByteBuffer.wrap(write64(modificationTime)));
			out.write(ByteBuffer.wrap(write32(trackId)));
			out.write(ByteBuffer.wrap(write32(reserved[0])));
			out.write(ByteBuffer.wrap(write64(duration)));
		}
		else // version == 0
		{
			out.write(ByteBuffer.wrap(write32((int) creationTime)));
			out.write(ByteBuffer.wrap(write32((int) modificationTime)));
			out.write(ByteBuffer.wrap(write32(trackId)));
			out.write(ByteBuffer.wrap(write32(reserved[0])));
			out.write(ByteBuffer.wrap(write32((int) duration)));
		}
		out.write(ByteBuffer.wrap(write32(reserved[1])));
		out.write(ByteBuffer.wrap(write32(reserved[2])));
		out.write(ByteBuffer.allocate(2).putShort((short) layer));
		out.write(ByteBuffer.allocate(2).putShort((short) alternateGroup));
		out.write(ByteBuffer.allocate(2).putShort((short) volume));
		out.write(ByteBuffer.allocate(2).putShort((short) reserved[3]));
		for (int i = 0; 9 > i; i++)
		{
			out.write(ByteBuffer.wrap(write32(matrix[i])));
		}
		out.write(ByteBuffer.wrap(write32(width)));
		out.write(ByteBuffer.wrap(write32(height)));
	}

	long adjust(final float time, final long mvhdTimescale)
	{
		final long boxAdjust = 0;

		final long delta = (long) (time * (float) mvhdTimescale);

		//
		// before we meddle with the duration, we need to take into account where this particular trak falls in
		// the overall timeline of the movie.  The firstMediaTime indicates the time offset of the first edit
		// in the trak.  If the requested timeseek is less than the firstMediaTime, then this trak is to be
		// presented whole, so the duration should not be adjusted.  Otherwise, subtract the firstMediaTime from
		// from the time offset, and adjust the duration accordingly.

		// what to do if delta - firstMediaTime > duration?  the trak is to be skipped entirely in this case.
		firstMediaTime -= delta;
		if (0 > firstMediaTime)
		{
			firstMediaTime = 0;
			duration -= delta;
		}
		return boxAdjust;
	}


}
