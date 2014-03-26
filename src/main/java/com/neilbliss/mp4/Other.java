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

public final class Other extends Box
{
	private static final long serialVersionUID = 3408666138441097009L;
	private byte[] data;

	public Other()
	{

	}

	public Other(final Box box)
	{
		copy(box);
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		final Other other = (Other) super.clone();
		other.data = new byte[data.length];
		System.arraycopy(data, 0, other.data, 0, data.length);
		return other;
	}

	@Override public long read(final InputStream in) throws IOException
	{
		return read(in, null);
	}

	public long read(final InputStream in, final Box box) throws IOException
	{
		long bytes = 0;
		if (null == box)
		{
			bytes += super.read(in);
		}
		data = new byte[(int) boxRemaining];
		bytes += in.read(data);
		return bytes;
	}

	@Override public void write(final OutputStream out) throws IOException
	{
		super.write(out);
		out.write(data);
	}

	@Override public void write(final ByteChannel out) throws IOException
	{
		super.write(out);
		out.write(ByteBuffer.wrap(data));
	}

}
