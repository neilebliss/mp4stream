package mp4;

import java.io.Serializable;

/**
 * Copyright (c) 2011 3Crowd Technologies, Inc.
 * <p/>
 * See LICENSE.txt for licensing terms covering this software.
 */
public final class TrakStub implements Serializable
{
	static final long serialVersionUID = 1073490440065649080l;
	public double[] time_map_keys;
	public float[] time_map_keys_float;
	public long[]   time_map_values;
	public double[] sample_map_keys;
	public float[] sample_map_keys_float;
	public int[]    sample_map_values;
	public boolean  audioTrack;
	public boolean  videoTrack;
}
