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
public final class Edts extends Box
{
	private static final long serialVersionUID = 6039813326244175717L;
	private ArrayList<Box> boxes = new ArrayList<Box>();
	public Elst elst;

	public Edts()
	{

	}

	public Edts(final Box box)
	{
		copy(box);
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		final Edts edts = (Edts) super.clone();
		edts.boxes = new ArrayList<Box>();
		for (final Box box : boxes)
		{
			final Box newBox;
			if (box instanceof Elst)
			{
				newBox = (Elst) box.clone();
				edts.elst = (Elst) newBox;
			}
			else
			{
				newBox = (Other) box.clone();
			}
			edts.boxes.add(newBox);
		}
		return edts;
	}

	@Override long read(final InputStream in) throws IOException
	{
		return read(in, null);
	}

	long read(final InputStream in, final Box box) throws IOException
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
			if ("elst".equals(subBox.boxType))
			{
				final FullBox fbox = new FullBox(subBox);
				loopBytes += fbox.read(in, subBox);
				elst = new Elst(fbox);
				loopBytes += elst.read(in, fbox);
				boxes.add(elst);
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


	long adjust(final float time, final long mvhdTimescale)
	{
		long boxAdjust = 0;
		for (final Box box : boxes)
		{
			if (box instanceof Elst)
			{
				boxAdjust += ((Elst) box).adjust(time, mvhdTimescale);
			}
			/*
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
		return elst.insertEmptyEdit(time);
	}
}
