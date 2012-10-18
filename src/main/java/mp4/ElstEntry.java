package mp4;

import java.io.Serializable;

/**
 * Copyright (c) 2011 3Crowd Technologies, Inc.
 * <p/>
 * See LICENSE.txt for licensing terms covering this software.
 */
final class ElstEntry implements Serializable, Cloneable
{
	private static final long serialVersionUID = 994012983338794610L;
	// @TODO - remove
	long segmentDuration;
	long mediaTime;
	long mediaRateInteger;
	long mediaRateFraction;

	@Override public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
}
