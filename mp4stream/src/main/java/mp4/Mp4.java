package mp4;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Copyright (c) 2011 3Crowd Technologies, Inc.
 * <p/>
 * See LICENSE.txt for licensing terms covering this software.
 */
public final class Mp4 implements Serializable, Cloneable
{
	private static final long serialVersionUID = 6745384226446076499L;
	public ArrayList<Box> boxes = new ArrayList<Box>();
	public  Ftyp ftyp;
	public  Moov moov;
	private Mdat mdat;
	public  long wallclock;
	public  long datarate;

	public Mp4()
	{

	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		final Mp4 mp4 = (Mp4) super.clone();
		mp4.boxes = new ArrayList<Box>();
		for (final Box box : boxes)
		{
			Box newBox;
			if (box instanceof Ftyp)
			{
				newBox = (Ftyp) box.clone();
				mp4.ftyp = (Ftyp) newBox;
			}
			if (box instanceof Moov)
			{
				newBox = (Moov) box.clone();
				mp4.moov = (Moov) newBox;
			}
			if (box instanceof Mdat)
			{
				newBox = (Mdat) box.clone();
				mp4.mdat = (Mdat) newBox;
			}
			else
			{
				newBox = (Box) box.clone();
			}
			mp4.boxes.add(newBox);
		}
		return mp4;
	}
}
