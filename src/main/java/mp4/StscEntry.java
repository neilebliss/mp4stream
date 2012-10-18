package mp4;

import java.io.Serializable;

/**
 * Copyright (c) 2011 3Crowd Technologies, Inc.
 * <p/>
 * See LICENSE.txt for licensing terms covering this software.
 */
public class StscEntry implements Serializable, Cloneable
{
	private static final long serialVersionUID = 7362735636871313193L;
	public int chunkGroupId;
	public int firstChunk;
	public int firstSampleId;
	public int samplesPerChunk;
	public int sampleDescriptionIndex;
	public int chunks;

	@Override public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}


}
