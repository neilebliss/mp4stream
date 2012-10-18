package mp4;

import java.io.File;
import java.io.IOException;

/**
 * Copyright (c) 2011 3Crowd Technologies, Inc.
 * <p/>
 * See LICENSE.txt for licensing terms covering this software.
 */
public final class Mp4Utils
{
	private Mp4Utils()
	{
	}

	public static synchronized Mp4Metadata extractMp4Metadata(String inputFilename) throws IOException
	{
		final Mp4Metadata mp4meta;
		mp4meta = new Mp4Metadata(new File(inputFilename));
		if (null != mp4meta.mp4 && null != mp4meta.mp4.moov && null != mp4meta.mp4.moov.traks)
		{
			mp4meta.trakStubs = new TrakStub[mp4meta.mp4.moov.traks.size()];
			int trakstubindex = 0;
			for (final Trak trak : mp4meta.mp4.moov.traks)
			{
				final TrakStub stub = new TrakStub();
				stub.time_map_keys_float = trak.time_map_keys;
				stub.time_map_values = trak.time_map_values;
				stub.sample_map_values = trak.sample_map_values;
				stub.audioTrack = trak.audioTrack;
				stub.videoTrack = trak.videoTrack;
				mp4meta.trakStubs[trakstubindex++] = stub;
			}
		}
		return mp4meta;
	}

	public static long findOffset(final float requestedTime, final TrakStub[] traks)
	{
		long derivedOffset = 0;
		float videoTime = 0;
		// find the video start offset.
		if (null != traks)
		{
			for (final TrakStub trak : traks)
			{
				if (null == trak.time_map_keys_float && null != trak.time_map_keys)
				{
					trak.time_map_keys_float = new float[trak.time_map_keys.length];
					for (int i = 0; i < trak.time_map_keys.length; i++)
					{
						trak.time_map_keys_float[i] = (float) trak.time_map_keys[i];
					}
				}
				if (trak.videoTrack)
				{
					//videoTime = trak.timeMap.floorKey(requestedTime);
					long newOffset = 0;
					for (int i = 0; i < trak.time_map_keys_float.length; i++)
					{
						if (-1 < trak.time_map_keys_float[i] && trak.time_map_keys_float[i] <= requestedTime)
						{
							videoTime = trak.time_map_keys_float[i];
							newOffset = trak.time_map_values[i];
						}
					}
					//long newOffset = trak.timeMap.get(videoTime);

					if (0 == derivedOffset || newOffset < derivedOffset)
					{
						derivedOffset = newOffset;
					}
				}
			}
			// now find the audio start offset.
			for (final TrakStub trak : traks)
			{
				if (trak.audioTrack)
				{
					long audioOffset = 0; // trak.timeMap.ceilingEntry(videoTime).getValue();
					for (int i = 0; i < trak.time_map_keys_float.length; i++)
					{
						if (-1 < trak.time_map_keys_float[i] && trak.time_map_keys_float[i] <= videoTime)
						{
							audioOffset = trak.time_map_values[i];
						}
					}
					if (audioOffset < derivedOffset)
					{
						derivedOffset = audioOffset;
					}
				}
			}
		}
		return derivedOffset;
	}
}
