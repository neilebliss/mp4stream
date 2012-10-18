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

public final class Co64 extends Stco
{
	private static final long serialVersionUID = 7574114108234894315L;

	public Co64()
	{

	}

	Co64(final FullBox box)
	{
		copy(box);
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		final Co64 co64 = (Co64) super.clone();
		co64.entryCount = entryCount;
/*
                co64.entries = new StcoEntry[entries.length];
                for (int i = 0; i < entries.length; i++)
                {
                        co64.entries[i] = (StcoEntry) entries[i].clone();
                }
*/
		if (null != offsets)
		{
			co64.offsets = new long[offsets.length];
			System.arraycopy(offsets, 0, co64.offsets, 0, offsets.length);
		}

		if (null != sizes)
		{
			co64.sizes = new long[sizes.length];
			System.arraycopy(sizes, 0, co64.sizes, 0, sizes.length);
		}

		if (null != firstSampleIds)
		{
			co64.firstSampleIds = new int[firstSampleIds.length];
			System.arraycopy(firstSampleIds, 0, co64.firstSampleIds, 0, firstSampleIds.length);
		}

		if (null != lastSampleIds)
		{
			co64.lastSampleIds = new int[lastSampleIds.length];
			System.arraycopy(lastSampleIds, 0, co64.lastSampleIds, 0, lastSampleIds.length);
		}

		if (null != alignedOffsetsLists)
		{
			co64.alignedOffsetsLists = new long[alignedOffsetsLists.length][];
			for (int i = 0; i < alignedOffsetsLists.length; i++)
			{
				co64.alignedOffsetsLists[i] = new long[alignedOffsetsLists[i].length];
				System.arraycopy(alignedOffsetsLists[i], 0, co64.alignedOffsetsLists[i], 0, alignedOffsetsLists[i].length);
			}
		}

		return co64;
	}

	long read(final InputStream in, final long filesize) throws IOException
	{
		return read(in, null, filesize);
	}

	long read(final InputStream in, final FullBox box, final long filesize) throws IOException
	{
		long bytes = 0;

		if (null == box)
		{
			bytes += super.read(in);
		}

		buffer = new byte[8];
		bytes += in.read(buffer);
		entryCount = read32(buffer);
		//entries = new StcoEntry[(int) entryCount];
		offsets = new long[(int) entryCount];
		sizes = new long[(int) entryCount];
		firstSampleIds = new int[(int) entryCount];
		lastSampleIds = new int[(int) entryCount];
		alignedOffsetsLists = new long[(int) entryCount][];

		for (int i = 0; i < entryCount; i++)
		{
			//entries[i] = new StcoEntry();
			Arrays.fill(buffer, (byte) 0);
			bytes += in.read(buffer);
			offsets[i] = read64(buffer);
			if (0 < i)
			{
				sizes[i - 1] = this.offsets[i] - this.offsets[i - 1];
			}
		}
		if (1 < sizes.length)
		{
			sizes[sizes.length - 1] = filesize - offsets[offsets.length - 2];
		}
		else
		{
			sizes[sizes.length - 1] = filesize;
		}
		return bytes;
	}

	@Override void write(final OutputStream out) throws IOException
	{
		super.write(out);
		out.write(write64(entryCount));
		for (int i = 0; i < offsets.length; i++)
		{
			out.write(write64(offsets[i]));
		}
	}

	@Override void write(final ByteChannel out) throws IOException
	{
		super.write(out);
		out.write(ByteBuffer.wrap(write64(entryCount)));
		for (int i = 0; i < offsets.length; i++)
		{
			out.write(ByteBuffer.wrap(write64(offsets[i])));
		}
	}
}
