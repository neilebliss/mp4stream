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
import java.util.Collections;

public final class Moov extends Box
{
	private static final long serialVersionUID = 6655271426897921117L;
	// per the spec, the moov box is an empty container just indicating the length of the moov sub-objects.
	// for our purposes, however, we want to actually correlate the sub objects.

	// a moov will have an mvhd box, a set of trak boxes, possibly an mvex box, and possibly an ipmc box
	private ArrayList<Box>  boxes    = new ArrayList<Box>();
	public Mvhd mvhd;
	public  ArrayList<Trak> traks    = new ArrayList<Trak>();
	public  boolean         adjusted;

	public Moov()
	{

	}

	public Moov(final Box box)
	{
		copy(box);
	}

	public Object clone() throws CloneNotSupportedException
	{
		final Moov moov = (Moov) super.clone();
		moov.boxes = new ArrayList<Box>();
		moov.traks = new ArrayList<Trak>();
		for (final Box box : boxes)
		{
			final Box newBox;
			if (box instanceof Trak)
			{
				newBox = (Trak) box.clone();
				moov.traks.add((Trak) newBox);
			}
			else if (box instanceof Mvhd)
			{
				newBox = (Mvhd) box.clone();
				moov.mvhd = (Mvhd) newBox;
			}
			else
			{
				newBox = (Other) box.clone();
			}
			moov.boxes.add(newBox);

		}
		return moov;
	}

	public long read(final InputStream in, final long filesize) throws IOException
	{
		return read(in, null, filesize);
	}

	public long read(final InputStream in, final Box box, final long filesize) throws IOException
	{
		long bytes = 0;
		if (null == box)
		{
			bytes += super.read(in);
		}

		// read the sub objects
		while (0 < boxRemaining)
		{
			long loopBytes = 0;
			final Box subBox = new Box();
			loopBytes += subBox.read(in);
			if ("mvhd".equals(subBox.boxType))
			{
				final FullBox fbox = new FullBox(subBox);
				loopBytes += fbox.read(in, subBox);
				mvhd = new Mvhd(fbox);
				loopBytes += mvhd.read(in, fbox);
				boxes.add(mvhd);
			}
			else if ("trak".equals(subBox.boxType))
			{
				final Trak trak = new Trak(subBox);
				loopBytes += trak.read(in, subBox, filesize);
				boxes.add(trak);
				traks.add(trak);
			}
			else
			{
				// anything that doesn't get altered in adjust() is an Other for this purpose.
				// just record the box, so we can spit it back out on replay.
				final Other other = new Other(subBox);
				loopBytes += other.read(in, subBox);
				boxes.add(other);
			}
			bytes += loopBytes;
			boxRemaining -= loopBytes;
		}
		// now that we've read in everything, we can go back and calculate some data that can only be known at this point.
		// step 1, calculate chunk sizes.
		// build a list of all chunks from all traks
		ArrayList<StcoEntry> chunks = new ArrayList<StcoEntry>();
		for (int t = 0; t < traks.size(); t++)
		{
			// java.util.Collections.addAll(chunks, trak.mdia.minf.stbl.stco.entries);
			for (int i = 0; i < traks.get(t).mdia.minf.stbl.stco.offsets.length; i++)
			{
				final StcoEntry entry = new StcoEntry();
				entry.offset = traks.get(t).mdia.minf.stbl.stco.offsets[i];
				entry.trak = t;
				entry.subTrakIndex = i;
				chunks.add(entry);
			}
		}
		// sort the chunks list into ascending offset order.  this uses the compareTo method in the StcoEntry class.
		Collections.sort(chunks);
		// calculate the sizes based on the offsets.  NOTE that this assumes the chunks are contiguously laid out on disk.
		for (int i = 1; i < chunks.size(); i++)
		{
			chunks.get(i - 1).size = chunks.get(i).offset - chunks.get(i - 1).offset;
		}
		// for the last chunk, get the size by subtracting the chunk's offset from the total file size.  This assumes that the last chunk is the last thing in the file.
		chunks.get(chunks.size() - 1).size = filesize - chunks.get(chunks.size() - 1).offset;

		// now we know all the sizes, but this information back into the traks.
		for (StcoEntry entry : chunks)
		{
			traks.get((int) entry.trak).mdia.minf.stbl.stco.sizes[(int) entry.subTrakIndex] = entry.size;
			entry = null;
		}
		chunks = null;
		return bytes;
	}

	@Override public void write(final OutputStream out) throws IOException
	{
		super.write(out);
		for (final Box box : boxes)
		{
			if (box instanceof Trak)
			{
				if (0 < ((Trak) box).tkhd.duration)
				{
					box.write(out);
				}
			}
			else
			{
				box.write(out);
			}
		}
	}

	@Override public void write(final ByteChannel out) throws IOException
	{
		super.write(out);
		for (final Box box : boxes)
		{
			if (box instanceof Trak)
			{
				if (0 < ((Trak) box).tkhd.duration)
				{
					box.write(out);
				}
			}
			else
			{
				box.write(out);
			}
		}
	}


	public void adjust(final float time, final long offset)
	{
		long boxAdjust = 0;
		for (final Box box : boxes)
		{
			if (box instanceof Mvhd)
			{
				boxAdjust += ((Mvhd) box).adjust(time);
			}
			else if (box instanceof Trak)
			{
				final Trak trak = (Trak) box;
				boxAdjust += trak.adjust(time, mvhd.timescale, offset);
			}
		}
		boxLen -= boxAdjust;
		for (final Trak trak : traks)
		{
			// if this trak has a duration longer than the mvhd duration, make the mvhd duration reflect that longer duration.
			if (trak.mdia.mdhd.duration / trak.mdia.mdhd.timescale > this.mvhd.duration / this.mvhd.timescale)
			{
				mvhd.duration = trak.mdia.mdhd.duration / trak.mdia.mdhd.timescale * mvhd.timescale;
			}
		}
		for (final Trak trak : traks)
		{
			final float mdiaDuration = (float) (trak.mdia.mdhd.duration / trak.mdia.mdhd.timescale) * mvhd.timescale;
			if (mdiaDuration < mvhd.duration)
			{
				boxAdjust += trak.insertEmptyEdit(mvhd.duration - (long) mdiaDuration);
			}
		}
		adjusted = true;
	}
}
