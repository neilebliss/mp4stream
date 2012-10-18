package mp4;

import java.io.Serializable;

/**
 * Copyright (c) 2011 3Crowd Technologies, Inc.
 * <p/>
 * See LICENSE.txt for licensing terms covering this software.
 */
public final class Sample implements Serializable, Cloneable
{
	private static final long serialVersionUID = 2732750896284675560L;
	public long    sampleId;
	public long    duration;
	public long    startTime;
	public int     chunkGroupId;
	public int     chunkId;
	public long    offset;
	public long    length;
	public boolean sync;
	public long    chunkSubsample;

	public Sample()
	{

	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
}
