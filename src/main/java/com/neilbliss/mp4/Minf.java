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
import java.util.ArrayList;

public final class Minf extends Box
{
	private static final long serialVersionUID = 5227671091800113108L;
	private ArrayList<Box> boxes = new ArrayList<Box>();
	public Stbl stbl;

	Minf()
	{

	}

	Minf(final Box box)
	{
		copy(box);
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		final Minf minf = (Minf) super.clone();
		minf.boxes = new ArrayList<Box>();
		for (final Box box : boxes)
		{
			final Box newBox;
			if (box instanceof Stbl)
			{
				newBox = (Stbl) box.clone();
				minf.stbl = (Stbl) newBox;
			}
			else
			{
				newBox = (Other) box.clone();
			}
			minf.boxes.add(newBox);
		}
		return minf;
	}

	long read(final InputStream in, final long filesize) throws IOException
	{
		return read(in, null, filesize);
	}

	long read(final InputStream in, final Box box, final long filesize) throws IOException
	{
		long bytes = 0;
		if (null == box)
		{
			bytes += super.read(in);
		}
		while (0 < boxRemaining)
		{
			long loopBytes = 0;
			final Box subBox = new Box();
			loopBytes += subBox.read(in);
			if ("stbl".equals(subBox.boxType))
			{
				stbl = new Stbl(subBox);
				loopBytes += stbl.read(in, subBox, filesize);
				boxes.add(stbl);
			}
			else
			{
				final Other other = new Other(subBox);
				loopBytes += other.read(in, subBox);
				boxes.add(other);
			}

			bytes += loopBytes;
			boxRemaining -= loopBytes;
		}
		return bytes;
	}

	@Override void write(final OutputStream out) throws IOException
	{
		super.write(out);
		for (final Box box : boxes)
		{
			box.write(out);
		}
	}

	@Override void write(final ByteChannel out) throws IOException
	{
		super.write(out);
		for (final Box box : boxes)
		{
			box.write(out);
		}
	}


	long adjust(final int startSample, final long time, final long offset, final long sample_offset)
	{
		long boxAdjust = 0;
		for (final Box box : boxes)
		{
			if (box instanceof Stbl)
			{
				boxAdjust += ((Stbl) box).adjust(startSample, time, offset, sample_offset);
			}
		}
		boxLen -= boxAdjust;
		return boxAdjust;
	}
}
