/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.neilbliss.mp4;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public final class Mp4Metadata implements Serializable
{
	private static final long serialVersionUID = -5208297962907971599L;
	public Mp4        mp4;
	public TrakStub[] trakStubs;

	public Mp4Metadata(final File infile)
	{
		try
		{
			final InputStream in;
			if (null != infile)
			{
				in = new BufferedInputStream(new FileInputStream(infile));
				final long fileSize = infile.length();
				final long offset = 0;
				mp4 = processFiledata(in, fileSize, offset);
			}
			if (null != mp4 && null != mp4.moov && null != mp4.moov.traks)
			{
				for (final Trak trak : mp4.moov.traks)
				{
					int sampleCount = 0;
					// build an array of all samples based on the contents of the stts table
					for (int i = 0; i < trak.mdia.minf.stbl.stts.sampleCounts.length; i++)
					{
						sampleCount += trak.mdia.minf.stbl.stts.sampleCounts[i];
					}
					//trak.samples = new Sample[sampleCount];
					trak.sample_duration = new int[sampleCount];
					trak.sample_sampleId = new int[sampleCount];
					trak.sample_duration = new int[sampleCount];
					trak.sample_startTime = new int[sampleCount];
					trak.sample_chunkGroupId = new int[sampleCount];
					trak.sample_chunkId = new int[sampleCount];
					trak.sample_offset = new long[sampleCount];
					trak.sample_length = new int[sampleCount];
					trak.sample_sync = new boolean[sampleCount];
					trak.sample_chunkSubsample = new int[sampleCount];

					int sampleIndex = 0;
					int startTime = 0;
					for (int i = 0; i < trak.mdia.minf.stbl.stts.sampleCounts.length; i++)
					{
						for (int j = 0; j < trak.mdia.minf.stbl.stts.sampleCounts[i]; j++)
						{
							//Sample sample = new Sample();
							trak.sample_duration[sampleIndex] = trak.mdia.minf.stbl.stts.sampleDeltas[i];
							trak.sample_startTime[sampleIndex] = startTime;
							startTime += trak.sample_duration[sampleIndex];
							trak.sample_sampleId[sampleIndex] = sampleIndex + 1;
							//trak.samples[sampleIndex++] = sample;
							sampleIndex++;
						}
					}

					for (final StscEntry stscEntry : trak.mdia.minf.stbl.stsc.entries)
					{
						for (int currentChunkWithinThisEntry = 0; currentChunkWithinThisEntry < stscEntry.chunks; currentChunkWithinThisEntry++)
						{
							final int chunkNumber = (int) stscEntry.firstChunk + currentChunkWithinThisEntry;
							//StcoEntry stcoEntry = trak.mdia.minf.stbl.stco.entries[chunkNumber - 1];
							final int stcoIndex = chunkNumber - 1;
							final long startingSampleInThisChunk = stscEntry.firstSampleId + currentChunkWithinThisEntry * stscEntry.samplesPerChunk;
							for (int currentSample = 0; currentSample < stscEntry.samplesPerChunk; currentSample++)
							{
								final long currentSampleInThisChunk = startingSampleInThisChunk + currentSample;
								final int sampleTableLookupIndex = (int) currentSampleInThisChunk - 1;
								trak.sample_chunkGroupId[sampleTableLookupIndex] = stscEntry.chunkGroupId;
								trak.sample_chunkId[sampleTableLookupIndex] = chunkNumber;
								trak.sample_chunkSubsample[sampleTableLookupIndex] = currentSample;
								long suboffset = trak.mdia.minf.stbl.stco.offsets[stcoIndex];
								for (int w = 1; w <= currentSample; w++)
								{
									suboffset += trak.sample_length[sampleTableLookupIndex - w];
								}

								trak.sample_offset[sampleTableLookupIndex] = suboffset;
								if (0 == trak.mdia.minf.stbl.stsz.sampleSize)
								{
									trak.sample_length[sampleTableLookupIndex] = trak.mdia.minf.stbl.stsz.sizes[sampleTableLookupIndex];
								}
								else
								{
									trak.sample_length[sampleTableLookupIndex] = trak.mdia.minf.stbl.stsz.sampleSize;
								}

								// while we're at it, populate the first and last sample ID's for the stco entries.
								if (0 == trak.mdia.minf.stbl.stco.firstSampleIds[stcoIndex] ||
									trak.mdia.minf.stbl.stco.firstSampleIds[stcoIndex] > sampleTableLookupIndex + 1)
								{
									trak.mdia.minf.stbl.stco.firstSampleIds[stcoIndex] = sampleTableLookupIndex + 1;
								}

								if (0 == trak.mdia.minf.stbl.stco.lastSampleIds[stcoIndex] ||
									trak.mdia.minf.stbl.stco.lastSampleIds[stcoIndex] < sampleTableLookupIndex + 1)
								{
									trak.mdia.minf.stbl.stco.lastSampleIds[stcoIndex] = sampleTableLookupIndex + 1;
								}
							}
						}
					}

					if (null != trak.mdia.minf.stbl.stss)
					{
						for (final long sampleNumber : trak.mdia.minf.stbl.stss.entries)
						{
							trak.sample_sync[(int) (sampleNumber - 1)] = true;
						}
					}
					trak.time_map_keys = new float[trak.sample_duration.length];
					trak.time_map_values = new long[trak.sample_duration.length];
					for (int i = 0; i < trak.time_map_values.length; i++)
					{
						trak.time_map_keys[i] = -1;
						trak.time_map_values[i] = -1;
					}
					trak.sample_map_keys = new float[trak.sample_duration.length];
					trak.sample_map_values = new int[trak.sample_duration.length];
					for (int i = 0; i < trak.sample_map_keys.length; i++)
					{
						trak.sample_map_keys[i] = -1;
						trak.sample_map_values[i] = -1;
					}
					int final_array_size = 0;
					for (int i = 0; i < trak.sample_duration.length; i++)
					{
						if (null == trak.mdia.minf.stbl.stss || trak.sample_sync[i])
						{
							trak.time_map_keys[i] = (float) trak.sample_startTime[i] / (float) trak.mdia.mdhd.timescale;
							trak.time_map_values[i] = trak.sample_offset[i];

							trak.sample_map_keys[i] = (float) trak.sample_startTime[i] / (float) trak.mdia.mdhd.timescale;
							trak.sample_map_values[i] = (int) trak.sample_sampleId[i];

							final_array_size++;
						}
					}
					// shrink the map arrays
					int final_array_index = 0;
					final float[] final_time_map_keys = new float[final_array_size];
					final long[] final_time_map_values = new long[final_array_size];
					final float[] final_sample_map_keys = new float[final_array_size];
					final int[] final_sample_map_values = new int[final_array_size];
					for (int i = 0; i < trak.time_map_keys.length; i++)
					{
						if (-1 != trak.time_map_keys[i])
						{
							final_time_map_keys[final_array_index] = trak.time_map_keys[i];
							final_time_map_values[final_array_index] = trak.time_map_values[i];
							final_sample_map_keys[final_array_index] = trak.sample_map_keys[i];
							final_sample_map_values[final_array_index] = trak.sample_map_values[i];
							final_array_index++;
						}
					}
					trak.time_map_keys = final_time_map_keys;
					trak.time_map_values = final_time_map_values;
					trak.sample_map_keys = final_sample_map_keys;
					trak.sample_map_values = final_sample_map_values;
				}
				if (0 < mp4.moov.mvhd.timescale)
				{
					mp4.wallclock = mp4.moov.mvhd.duration / mp4.moov.mvhd.timescale;
				}
				else
				{
					mp4.wallclock = -1;
				}
				if (0 < mp4.wallclock)
				{
					mp4.datarate = infile.length() / mp4.wallclock;
				}
				else
				{
					mp4.datarate = -1;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private Mp4 processFiledata(final InputStream in, final long fileSize, long offset) throws IOException
	{
		Moov moov;
		Ftyp ftyp;
		if (null == in)
		{
			return null;
		}

		final Mp4 mp4 = new Mp4();

		byte[] buffer;
		while (offset < fileSize)
		{
			buffer = new byte[4];
			in.read(buffer);
			offset += buffer.length;

			//
			// check if this is actually a WebM video. If so, return null.
			//

			if (26 == buffer[0] && 69 == buffer[1] && -33 == buffer[2] && -93 == buffer[3])
			{
				return null;
			}

			final long size = ByteBuffer.wrap(buffer).getInt() & 0xffffffffL;
			Arrays.fill(buffer, (byte) 0);
			in.read(buffer);
			offset += buffer.length;
			final String boxType = new String(buffer);
			long boxSize = size;
			boxSize -= 8;
			long largeSize = 0;
			if (1 == size)
			{
				buffer = new byte[8];
				in.read(buffer);
				offset += buffer.length;
				largeSize = ByteBuffer.wrap(buffer).getLong() & 0xffffffffL;
				boxSize = largeSize;
				boxSize -= 16;
			}
			final String extendedType = null;
			if ("ftyp".equals(boxType))
			{
				final Box box = new Box();
				box.boxLen = (int) size;
				box.boxRemaining = (int) boxSize;
				box.largeSize = (int) largeSize;
				box.boxType = boxType;
				box.extendedType = extendedType;
				ftyp = new Ftyp(box);
				mp4.boxes.add(ftyp);
				mp4.ftyp = ftyp;
				offset += ftyp.read(in, box);
			}
			else if ("moov".equals(boxType))
			{
				final Box box = new Box();
				box.boxLen = (int) size;
				box.boxRemaining = (int) boxSize;
				box.largeSize = (int) largeSize;
				box.boxType = boxType;
				box.extendedType = extendedType;
				moov = new Moov(box);
				mp4.boxes.add(moov);
				mp4.moov = moov;
				offset += moov.read(in, box, fileSize);
			}
			else if ("mdat".equals(boxType))
			{
				final Box box = new Box();
				box.boxLen = (int) size;
				box.boxRemaining = (int) boxSize;
				box.largeSize = (int) largeSize;
				box.boxType = boxType;
				box.extendedType = extendedType;
				final Mdat mdat = new Mdat(box);
				offset += mdat.read(in);
				mp4.boxes.add(mdat);
				// don't read in the rest of the mdat, just break outta here.
				//break;
			}
			else if ("free".equals(boxType))
			{
				// treat a free box the same as an mdat - skip over it to find out what's beyond it.
				final Box box = new Box();
				box.boxLen = (int) size;
				box.boxRemaining = (int) boxSize;
				box.largeSize = (int) largeSize;
				box.boxType = boxType;
				box.extendedType = extendedType;
				final Mdat free = new Mdat(box);
				offset += free.read(in);
				// don't add this to the struct.
			}
			else
			{
				if (boxSize >= Runtime.getRuntime().freeMemory())
				{
					return null;
				}
				final Box box = new Box();
				box.boxLen = (int) size;
				box.boxRemaining = (int) boxSize;
				box.largeSize = (int) largeSize;
				box.boxType = boxType;
				box.extendedType = extendedType;
				final Other other = new Other(box);
				final long subBytes = other.read(in, box);
				mp4.boxes.add(other);
				offset += subBytes;
			}
		}
		return mp4;
	}

	private long consumeBoxRemainder(final InputStream in, final long boxSize) throws IOException
	{
		final byte[] buffer;
		long subBytes = 0;
		buffer = new byte[1];
		for (long i = 0; i < boxSize; i++)
		{
			subBytes += in.read(buffer);
		}
		return subBytes;
	}

	public long findOffset(final float requestedTime)
	{
		long derivedOffset = 0;
		if (null != mp4 && null != mp4.moov && null != mp4.moov.traks)
		{
			float videoTime = 0;
			// find the video start offset.
			for (final Trak trak : mp4.moov.traks)
			{
				if (trak.videoTrack)
				{
					//videoTime = trak.timeMap.floorKey(requestedTime);
					long newOffset = 0;
					for (int i = 0; i < trak.time_map_keys.length; i++)
					{
						if (-1 < trak.time_map_keys[i] && trak.time_map_keys[i] <= requestedTime)
						{
							videoTime = trak.time_map_keys[i];
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
			for (final Trak trak : mp4.moov.traks)
			{
				if (trak.audioTrack)
				{
					long audioOffset = 0; // trak.timeMap.ceilingEntry(videoTime).getValue();
					for (int i = 0; i < trak.time_map_keys.length; i++)
					{
						if (-1 < trak.time_map_keys[i] && trak.time_map_keys[i] <= videoTime)
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

	public void adjustMoov(final float requestedTime)
	{
		long offset = 0;
		float videoTime = 0;
		// find the starting time and offset for the video trak, then base the audio trak off of that.
		if (null != mp4 && null != mp4.moov && null != mp4.moov.traks)
		{
			for (final Trak trak : mp4.moov.traks)
			{
				if (trak.videoTrack)
				{
					videoTime = 0; // trak.timeMap.floorKey(requestedTime);
					for (int i = 0; i < trak.time_map_keys.length; i++)
					{
						if (-1 < trak.time_map_keys[i] && trak.time_map_keys[i] <= requestedTime)
						{
							videoTime = trak.time_map_keys[i];
						}
					}
					//trak.startingSample = trak.sampleMap.get(videoTime);
					for (int i = 0; i < trak.sample_map_keys.length; i++)
					{
						if (trak.sample_map_keys[i] == videoTime)
						{
							trak.startingSample = trak.sample_map_values[i];
							break;
						}
					}
					offset = 0; // trak.timeMap.get(videoTime);
					for (int i = 0; i < trak.time_map_keys.length; i++)
					{
						if (trak.time_map_keys[i] == videoTime)
						{
							offset = trak.time_map_values[i];
							break;
						}
					}
					break;
				}
			}

			for (final Trak trak : mp4.moov.traks)
			{
				if (trak.audioTrack)
				{
					//trak.startingSample = trak.sampleMap.ceilingEntry(videoTime).getValue();
					for (int i = 0; i < trak.sample_map_keys.length; i++)
					{
						if (-1 < trak.sample_map_keys[i] && trak.sample_map_keys[i] <= videoTime)
						{
							trak.startingSample = trak.sample_map_values[i];
						}
					}
					for (int i = 0; i < trak.sample_sampleId.length; i++)
					{
						if (trak.sample_sampleId[i] == trak.startingSample)
						{
							if (trak.sample_offset[i] < offset)
							{
								offset = trak.sample_offset[i];
							}
						}
					}
					break;
				}
			}
			mp4.moov.adjust(videoTime, offset);
		}
	}

/*
	public static void main(final String[] args)
	{
		// standalone interface to allow a test of the compatability of a given com.neilbliss.mp4 file with our processing.
		final Mp4Metadata m = new Mp4Metadata(new File(args[0]));
	}
*/
}


