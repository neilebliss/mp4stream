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
import java.nio.channels.ByteChannel;
import java.util.ArrayList;

public final class Mdia extends Box
{
	private static final long serialVersionUID = 1460106141704857603L;
	private ArrayList<Box> boxes = new ArrayList<Box>();
	public  Mdhd           mdhd;
	public Minf minf;
	public  Hdlr           hdlr;

	Mdia()
	{

	}

	Mdia(final Box box)
	{
		copy(box);
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		final Mdia mdia = (Mdia) super.clone();
		mdia.boxes = new ArrayList<Box>();
		for (final Box box : boxes)
		{
			final Box newBox;
			if (box instanceof Mdhd)
			{
				newBox = (Mdhd) box.clone();
				mdia.mdhd = (Mdhd) newBox;
			}
			else if (box instanceof Minf)
			{
				newBox = (Minf) box.clone();
				mdia.minf = (Minf) newBox;
			}
			else if (box instanceof Hdlr)
			{
				newBox = (Hdlr) box.clone();
			}
			else
			{
				newBox = (Other) box.clone();
			}
			mdia.boxes.add(newBox);
		}
		return mdia;
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
			if ("mdhd".equals(subBox.boxType))
			{
				final FullBox fbox = new FullBox(subBox);
				loopBytes += fbox.read(in, subBox);
				mdhd = new Mdhd(fbox);
				loopBytes += mdhd.read(in, fbox);
				boxes.add(mdhd);
			}
			else if ("hdlr".equals(subBox.boxType))
			{
				final FullBox fbox = new FullBox(subBox);
				loopBytes += fbox.read(in, subBox);
				hdlr = new Hdlr(fbox);
				loopBytes += hdlr.read(in, fbox);
				boxes.add(hdlr);
			}
			else if ("minf".equals(subBox.boxType))
			{
				minf = new Minf(subBox);
				loopBytes += minf.read(in, subBox, filesize);
				boxes.add(minf);
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


	long adjust(final int startSample, final float time, final long offset, final long sample_offset)
	{
		long boxAdjust = 0;
		final long mdiaTime = (long) (time * mdhd.timescale);

		for (final Box box : boxes)
		{
			if (box instanceof Minf)
			{
				boxAdjust += ((Minf) box).adjust(startSample, mdiaTime, offset, sample_offset);
			}
			else if (box instanceof Mdhd)
			{
				boxAdjust += ((Mdhd) box).adjust(time);
			}
			/*
						else if (box instanceof Hdlr)
						{
							boxAdjust += ((Hdlr) box).adjust();
						}
						else
						{
							boxAdjust += ((Other) box).adjust();
						}
						*/
		}
		boxLen -= boxAdjust;
		return boxAdjust;
	}
}
