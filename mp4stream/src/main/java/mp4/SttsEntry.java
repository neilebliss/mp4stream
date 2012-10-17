package mp4;

import java.io.Serializable;

/**
 * Copyright (c) 2011 3Crowd Technologies, Inc.
 * <p/>
 * See LICENSE.txt for licensing terms covering this software.
 */
public class SttsEntry implements Serializable, Cloneable
{
	private static final long serialVersionUID = 5922926502226319993L;
	public long sampleCount;
	public long sampleDelta;
	public long startTime;
	public long endTime;

	@Override public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
}
