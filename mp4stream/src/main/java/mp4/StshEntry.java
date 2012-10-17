package mp4;

import java.io.Serializable;

/**
 * Copyright (c) 2011 3Crowd Technologies, Inc.
 * <p/>
 * See LICENSE.txt for licensing terms covering this software.
 */
class StshEntry implements Serializable, Cloneable
{
	private static final long serialVersionUID = -8798385821011895400L;
	int shadowedSampleNumber;
	int syncSampleNumber;

	@Override public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
}
