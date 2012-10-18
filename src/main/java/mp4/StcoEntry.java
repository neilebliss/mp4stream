package mp4;

import java.io.Serializable;

/**
 * Copyright (c) 2011 3Crowd Technologies, Inc.
 * <p/>
 * See LICENSE.txt for licensing terms covering this software.
 */
public class StcoEntry implements Cloneable, Serializable, Comparable
{
	private static final long serialVersionUID = -2893237213684612411L;
	public long offset;
	public long size;
	public long trak;
	public long subTrakIndex;

	@Override public Object clone() throws CloneNotSupportedException
	{
		return (StcoEntry) super.clone();
	}

	@Override public int compareTo(final Object compare) throws ClassCastException
	{
		return (int) (offset - ((StcoEntry) compare).offset);
	}

}
