package mp4;

import java.io.Serializable;

/**
 * Copyright (c) 2011 3Crowd Technologies, Inc.
 * <p/>
 * See LICENSE.txt for licensing terms covering this software.
 */
class SbgpEntry implements Serializable, Cloneable
{
	private static final long serialVersionUID = -7000411662184116437L;
	int sampleCount;
	int groupDescriptionIndex;

	@Override public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
}
