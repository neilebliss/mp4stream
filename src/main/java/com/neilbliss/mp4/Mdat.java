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
import java.nio.channels.ByteChannel;

public final class Mdat extends Box
{
	private static final long serialVersionUID = 9167926454385309003L;
	//
	// the mdat container is used to hold the movie data for an com.neilbliss.mp4.
	// for our purposes, we don't really want to hold a ton of data in it,
	// so we'll just leave it as a Box wrapper.
	//

	public Mdat()
	{

	}

	public Mdat(final Box box)
	{
		copy(box);
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

	@Override public long read(final InputStream in) throws IOException
	{
		long bytes = 0;
		long loopBytes = 0;
		while (0 < boxRemaining)
		{
			loopBytes = in.skip(boxRemaining);
			boxRemaining -= loopBytes;
			bytes += loopBytes;
		}
		return bytes;
	}

	@Override public void write(final ByteChannel out) throws IOException
	{
		super.write(out);
	}

	@Override public void write(final OutputStream out) throws IOException
	{
		super.write(out);
	}

	public long adjust(final long offset)
	{
		final long boxAdjust = 0;
		boxLen -= offset;
		return boxAdjust;
	}
}
