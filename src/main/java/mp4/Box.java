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
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.Arrays;

public class Box implements Serializable, Cloneable
{
	private static final long serialVersionUID = 8871777217006226359L;
	public int    boxLen;
	public int    boxRemaining;
	public String boxType;
	public int    largeSize;
	public String extendedType;
	byte[] buffer;

	public Box()
	{

	}

	public Box(final Box b)
	{
		boxLen = b.boxLen;
		boxRemaining = b.boxRemaining;
		boxType = b.boxType;
		largeSize = b.largeSize;
		extendedType = b.extendedType;
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

	public void copy(final Box b)
	{
		boxLen = b.boxLen;
		boxRemaining = b.boxRemaining;
		boxType = b.boxType;
		largeSize = b.largeSize;
		extendedType = b.extendedType;

	}

	long read(final InputStream in) throws IOException
	{
		// reading from disk
		long bytes = 0;
		buffer = new byte[4];
		bytes += in.read(buffer);
		boxLen = (int) read32(buffer);
		boxRemaining = boxLen;
		Arrays.fill(buffer, (byte) 0);
		bytes += in.read(buffer);
		boxType = new String(buffer);
		boxRemaining -= 8;
		if (1 == boxLen)
		{
			buffer = new byte[8];
			bytes += in.read(buffer);
			largeSize = (int) read64(buffer);
			boxRemaining = largeSize - 16;
		}
		if ("uuid".equals(boxType))
		{
			buffer = new byte[16];
			bytes += in.read(buffer);
			boxRemaining -= 16;
			extendedType = new String(buffer);
		}
		return bytes;
	}

	void write(final OutputStream out) throws IOException
	{
		out.write(write32(boxLen));
		out.write(boxType.getBytes());
		if (0 != largeSize)
		{
			out.write(write64(largeSize));
		}
		if (null != extendedType)
		{
			out.write(extendedType.getBytes());
		}
	}

	void write(final ByteChannel out) throws IOException
	{
		buffer = ByteBuffer.allocate(4).putInt((int) (boxLen & 0xffffffffL)).array();
		out.write(ByteBuffer.wrap(buffer));
		out.write(ByteBuffer.wrap(boxType.getBytes()));
		if (0 != largeSize)
		{
			buffer = ByteBuffer.allocate(4).putInt((int) (largeSize & 0xffffffffL)).array();
			out.write(ByteBuffer.wrap(buffer));
		}
		if (null != extendedType)
		{
			out.write(ByteBuffer.wrap(extendedType.getBytes()));
		}
	}

	public long adjust()
	{
		return 0;
	}


	public long read8(final byte[] buffer)
	{
		return buffer[0] & 0xffffffffL;
	}

	public byte[] write8(final long input)
	{
		return ByteBuffer.allocate(1).put((byte) (input & 0xffffffffL)).array();
	}

	public long read16(final byte[] buffer)
	{
		return ByteBuffer.wrap(buffer).getShort() & 0xffffffffL;
	}

	public byte[] write16(final long input)
	{
		return ByteBuffer.allocate(2).putShort((short) (input & 0xffffffffL)).array();
	}

	public int read32(final byte[] buffer)
	{
		// @TODO - switch all the ByteBuffer.get(int/long/short/etc) to this style of reading.  it's much more efficient.
		long value = 0;
		for (int i = 0; i < buffer.length; i++)
		{
			value = (value << 8) + (buffer[i] & 0xff);
		}
		return (int) (value & 0xffffffffL);
	}

	public byte[] write32(final int input)
	{
		return ByteBuffer.allocate(4).putInt((int) (input & 0xffffffffL)).array();
	}

	public long read64(final byte[] buffer)
	{
		return ByteBuffer.wrap(buffer).getLong() & 0xffffffffL;
	}

	public byte[] write64(final long input)
	{
		return ByteBuffer.allocate(8).putLong(input & 0xffffffffL).array();
	}

	long consumeBoxRemainder(final InputStream in, final long boxSize) throws IOException
	{
		final byte[] buffer;
		long subBytes = 0;
		buffer = new byte[1];
		for (long i = 0; i < boxSize; i++)
		{
			subBytes += in.read(buffer);
		}
		return subBytes;
	}
}
