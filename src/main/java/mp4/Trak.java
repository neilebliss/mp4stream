package mp4;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ByteChannel;
import java.util.ArrayList;

/**
 * Copyright (c) 2011 3Crowd Technologies, Inc.
 * <p/>
 * See LICENSE.txt for licensing terms covering this software.
 */
public final class Trak extends Box
{
	private static final long serialVersionUID = -47135657447259629L;
	// similar to the moov box, the trak box is in theory an "empty" box serving only to delineate the sub objects of the container.
	private ArrayList<Box> boxes          = new ArrayList<Box>();
	public  Tkhd           tkhd;
	public  Mdia           mdia;
	private Edts           edts;
	private long           firstMediaTime;
	//public  Sample[]       samples        = null;
	public int[]     sample_sampleId;
	public int[]     sample_duration;
	public int[]     sample_startTime;
	public int[]     sample_chunkGroupId;
	public int[]     sample_chunkId;
	public long[]    sample_offset;
	public int[]     sample_length;
	public boolean[] sample_sync;
	public int[]     sample_chunkSubsample;

	public float[] sample_map_keys;
	public int[]    sample_map_values;
	public float[] time_map_keys;
	public long[]   time_map_values;
	public int startingSample;

	public boolean audioTrack;
	public boolean videoTrack;

	public Trak()
	{

	}

	Trak(final Box box)
	{
		copy(box);
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		final Trak trak = (Trak) super.clone();
		trak.boxes = new ArrayList<Box>();
		for (final Box box : boxes)
		{
			final Box newBox;
			if (box instanceof Tkhd)
			{
				newBox = (Tkhd) box.clone();
				trak.tkhd = (Tkhd) newBox;
			}
			else if (box instanceof Mdia)
			{
				newBox = (Mdia) box.clone();
				trak.mdia = (Mdia) newBox;
			}
			else if (box instanceof Edts)
			{
				newBox = (Edts) box.clone();
			}
			else
			{
				newBox = (Other) box.clone();
			}
			trak.boxes.add(newBox);
		}
		return trak;
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
			if ("tkhd".equals(subBox.boxType))
			{
				final FullBox fbox = new FullBox(subBox);
				loopBytes += fbox.read(in, subBox);
				tkhd = new Tkhd(fbox);
				loopBytes += tkhd.read(in, fbox);
				boxes.add(tkhd);
			}
			else if ("edts".equals(subBox.boxType))
			{
				final Edts edts = new Edts(subBox);
				// set up the firstMediaTime.
				loopBytes += edts.read(in, subBox);
				if (null != edts.elst)
				{
					for (final ElstEntry entry : edts.elst.entries)
					{
						if (entry.mediaTime < this.firstMediaTime || 0 == firstMediaTime)
						{
							firstMediaTime = entry.mediaTime;
						}
					}
				}
				boxes.add(edts);
			}
			else if ("mdia".equals(subBox.boxType))
			{
				mdia = new Mdia(subBox);
				loopBytes += mdia.read(in, subBox, filesize);
				boxes.add(mdia);
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
		if (null != tkhd)
		{
			tkhd.firstMediaTime = firstMediaTime;
		}

		if ("soun".equals(mdia.hdlr.handlerType))
		{
			audioTrack = true;
		}

		if ("vide".equals(mdia.hdlr.handlerType))
		{
			videoTrack = true;
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


	long adjust(final float time, final long mvhdTimescale, final long offset)
	{
		long boxAdjust = 0;
		for (final Box box : boxes)
		{
			if (box instanceof Tkhd)
			{
				boxAdjust += ((Tkhd) box).adjust(time, mvhdTimescale);
				if (0 >= ((Tkhd) box).duration)
				{
					boxLen -= boxAdjust;
					break;
				}
			}
			else if (box instanceof Edts)
			{
				boxAdjust += ((Edts) box).adjust(time, mvhdTimescale);
			}
			else if (box instanceof Mdia)
			{
				// startingSample values of 0 or less indicate that this trak is non-adjustable.
				if (0 < startingSample)
				{

					//Sample startSample = samples[startingSample - 1];
					boxAdjust += ((Mdia) box).adjust(startingSample, time, offset, sample_offset[startingSample - 1]);
				}
			}
			/*
						else if (box instanceof Tref)
						{
							boxAdjust += ((Tref) box).adjust();
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

	public long insertEmptyEdit(final long time)
	{
		long boxAdjust = 0;
		if (null == edts)
		{
			// create a new edts/elst setup.
			edts = new Edts();
			edts.boxType = "edts";
			edts.boxLen += 8;
			edts.elst = new Elst();
			edts.elst.boxType = "elst";
			edts.elst.boxLen += 12;
			edts.elst.version = 0;
			boxAdjust += 20;
		}
		boxAdjust += edts.insertEmptyEdit(time);
		return boxAdjust;
	}


}
