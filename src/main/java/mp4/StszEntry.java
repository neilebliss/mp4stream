package mp4;

import java.io.Serializable;

/**
 * Copyright (c) 2011 3Crowd Technologies, Inc.
 * <p/>
 * See LICENSE.txt for licensing terms covering this software.
 */
public class StszEntry implements Cloneable, Serializable
{
	private static final long serialVersionUID = -3767972210370822706L;
	public long sampleId;
	public long size;

	@Override public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
}
