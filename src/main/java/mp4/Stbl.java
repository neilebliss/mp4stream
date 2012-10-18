package mp4;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ByteChannel;
import java.util.ArrayList;

/**
 * Copyright (c) 2011 3Crowd Technologies, Inc.
 * <p/>
 * See LICENSE.txt for licensing terms covering this software.
 */
public final class Stbl extends Box
{
	private static final long serialVersionUID = -8587433058479605202L;
	private ArrayList<Box> boxes = new ArrayList<Box>();
	public Stss stss;
	public Stts stts;
	public  Stsc           stsc;
	public  Stco           stco;
	private Co64           co64;
	public  Stsz           stsz;
	private Stz2           stz2;

	Stbl()
	{

	}

	Stbl(final Box box)
	{
		copy(box);
	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		final Stbl stbl = (Stbl) super.clone();
		stbl.boxes = new ArrayList<Box>();
		for (final Box box : boxes)
		{
			final Box newBox;
			if (box instanceof Stss)
			{
				newBox = (Stss) box.clone();
				stbl.stss = (Stss) newBox;
			}
			else if (box instanceof Stts)
			{
				newBox = (Stts) box.clone();
				stbl.stts = (Stts) newBox;
			}
			else if (box instanceof Stsc)
			{
				newBox = (Stsc) box.clone();
				stbl.stsc = (Stsc) newBox;
			}
			else if (box instanceof Stco)
			{
				newBox = (Stco) box.clone();
				stbl.stco = (Stco) newBox;
			}
			else if (box instanceof Ctts)
			{
				newBox = (Ctts) box.clone();
			}
			else if (box instanceof Stsz)
			{
				newBox = (Stsz) box.clone();
				stbl.stsz = (Stsz) newBox;
			}
			else if (box instanceof Stz2)
			{
				newBox = (Stz2) box.clone();
				stbl.stz2 = (Stz2) newBox;
			}
			else if (box instanceof Co64)
			{
				newBox = (Co64) box.clone();
				stbl.co64 = (Co64) newBox;
			}
			else if (box instanceof Stsh)
			{
				newBox = (Stsh) box.clone();
			}
			else if (box instanceof Padb)
			{
				newBox = (Padb) box.clone();
			}
			else if (box instanceof Sdtp)
			{
				newBox = (Sdtp) box.clone();
			}
			else
			{
				newBox = (Other) box.clone();
			}
			stbl.boxes.add(newBox);
		}
		return stbl;
	}

	long read(final InputStream in, final long filesize) throws IOException
	{
		return read(in, null, filesize);
	}

	long read(final InputStream in, final Box box, final long filesize) throws IOException
	{
		long bytes = 0;
		if (null == box)
		{
			bytes += super.read(in);
		}
		while (0 < boxRemaining)
		{
			long loopBytes = 0;
			final Box subBox = new Box();
			loopBytes += subBox.read(in);
			if ("stts".equals(subBox.boxType))
			{
				final FullBox fbox = new FullBox(subBox);
				loopBytes += fbox.read(in, subBox);
				stts = new Stts(fbox);
				loopBytes += stts.read(in, fbox);
				boxes.add(stts);

			}
			else if ("ctts".equals(subBox.boxType))
			{
				final FullBox fbox = new FullBox(subBox);
				loopBytes += fbox.read(in, subBox);
				final Ctts ctts = new Ctts(fbox);
				loopBytes += ctts.read(in, fbox);
				boxes.add(ctts);
			}
			else if ("stsc".equals(subBox.boxType))
			{
				final FullBox fbox = new FullBox(subBox);
				loopBytes += fbox.read(in, subBox);
				stsc = new Stsc(fbox);
				loopBytes += stsc.read(in, fbox);
				boxes.add(stsc);
			}
			else if ("stsz".equals(subBox.boxType))
			{
				final FullBox fbox = new FullBox(subBox);
				loopBytes += fbox.read(in, subBox);
				stsz = new Stsz(fbox);
				loopBytes += stsz.read(in, fbox);
				boxes.add(stsz);
			}
			else if ("stz2".equals(subBox.boxType))
			{
				final FullBox fbox = new FullBox(subBox);
				loopBytes += fbox.read(in, subBox);
				stz2 = new Stz2(fbox);
				loopBytes += stz2.read(in, fbox);
				boxes.add(stz2);
			}
			else if ("stco".equals(subBox.boxType))
			{
				final FullBox fbox = new FullBox(subBox);
				loopBytes += fbox.read(in, subBox);
				stco = new Stco(fbox);
				loopBytes += stco.read(in, fbox);
				boxes.add(stco);
			}
			else if ("co64".equals(subBox.boxType))
			{
				final FullBox fbox = new FullBox(subBox);
				loopBytes += fbox.read(in, subBox);
				co64 = new Co64(fbox);
				loopBytes += co64.read(in, fbox, filesize);
				boxes.add(co64);
			}
			else if ("stss".equals(subBox.boxType))
			{
				final FullBox fbox = new FullBox(subBox);
				loopBytes += fbox.read(in, subBox);
				stss = new Stss(fbox);
				loopBytes += stss.read(in, fbox);
				boxes.add(stss);
			}
			else if ("stsh".equals(subBox.boxType))
			{
				final FullBox fbox = new FullBox(subBox);
				loopBytes += fbox.read(in, subBox);
				final Stsh stsh = new Stsh(fbox);
				loopBytes += stsh.read(in, fbox);
				boxes.add(stsh);
			}
			else if ("padb".equals(subBox.boxType))
			{
				final FullBox fbox = new FullBox(subBox);
				loopBytes += fbox.read(in, subBox);
				final Padb padb = new Padb(fbox);
				loopBytes += padb.read(in, fbox);
				boxes.add(padb);
			}
			else if ("sdtp".equals(subBox.boxType))
			{
				final FullBox fbox = new FullBox(subBox);
				loopBytes += fbox.read(in, subBox);
				final Sdtp sdtp = new Sdtp(fbox);
				loopBytes += sdtp.read(in, fbox);
				boxes.add(sdtp);
			}
			else if ("sbgp".equals(subBox.boxType))
			{
				final FullBox fbox = new FullBox(subBox);
				loopBytes += fbox.read(in, subBox);
				final Sbgp sbgp = new Sbgp(fbox);
				loopBytes += sbgp.read(in, fbox);

			}
			else
			{
				final Other other = new Other(subBox);
				loopBytes += other.read(in, subBox);
				boxes.add(other);
			}

			bytes += loopBytes;
			boxRemaining -= loopBytes;
		}

		// goofy fixup time!
		// we want to be able to set the number of chunks in the last stsc entry for each trak.
		// We can't really do that until we've read in the stco for the trak to know how many chunks were in the trak.
		// Since this only becomes important during the adjust() phase, we can wait until here and then go back and fix up the
		// end stsc records with the data from the stco box.
		stsc.entries[(int) (stsc.entryCount - 1)].chunks = this.stco.offsets.length - this.stsc.entries[(int) (this.stsc.entryCount - 1)].firstChunk + 1;

		//
		// populate the alignment points of each chunk in the stco table
		//

		for (final StscEntry stscEntry : stsc.entries)
		{
			for (int chunk = 0; chunk < stscEntry.chunks; chunk++)
			{
				final int stcoIndex = (int) stscEntry.firstChunk + chunk - 1;
				//StcoEntry stcoEntry = stco.entries[((int) stscEntry.firstChunk + chunk) - 1];
				stco.alignedOffsetsLists[stcoIndex] = new long[(int) stscEntry.samplesPerChunk];
				long cumulativeSampleSizes = 0;
				stco.alignedOffsetsLists[stcoIndex][0] = stco.offsets[stcoIndex];
				long sampleSize = stsz.sampleSize;
				if (0 == sampleSize)
				{
					sampleSize = stsz.sizes[(int) (stscEntry.firstSampleId + chunk * stscEntry.samplesPerChunk) - 1];
				}
				cumulativeSampleSizes += sampleSize;
				for (int i = 1; i < stco.alignedOffsetsLists[stcoIndex].length; i++)
				{
					stco.alignedOffsetsLists[stcoIndex][i] = stco.offsets[stcoIndex] + (int) cumulativeSampleSizes;
					//
					// stscEntry.firstSampleId is 1-based, but the stsc.entries array is 0-based, so be sure to use sampleNumber - 1 to get the correct sample entry.
					//
					sampleSize = stsz.sampleSize;
					if (0 == sampleSize)
					{
						sampleSize = stsz.sizes[(int) (stscEntry.firstSampleId + chunk * stscEntry.samplesPerChunk + i) - 1];
					}
					cumulativeSampleSizes += sampleSize;
				}
			}
		}

		return bytes;
	}

	@Override void write(final OutputStream out) throws IOException
	{
		super.write(out);
		for (final Box box : boxes)
		{
			if (null != box)
			{
				box.write(out);
			}
		}
	}

	@Override void write(final ByteChannel out) throws IOException
	{
		super.write(out);
		for (final Box box : boxes)
		{
			if (null != box)
			{
				box.write(out);
			}
		}
	}


	long adjust(final int startSample, final long time, final long offset, final long sample_offset)
	{
		long boxAdjust = 0;
		for (final Box box : boxes)
		{
			if (box instanceof Stss)
			{
				boxAdjust += ((Stss) box).adjust(startSample);
			}
			else if (box instanceof Stts)
			{
				boxAdjust += ((Stts) box).adjust(time);
			}
			else if (box instanceof Stsc)
			{
				boxAdjust += ((Stsc) box).adjust(startSample);
			}
			else if (box instanceof Ctts)
			{
				boxAdjust += ((Ctts) box).adjust(startSample);
			}
			else if (box instanceof Stsz)
			{
				boxAdjust += ((Stsz) box).adjust(startSample);
			}
			else if (box instanceof Stz2)
			{
				boxAdjust += ((Stz2) box).adjust(startSample);
			}
			else if (box instanceof Stsh)
			{
				boxAdjust += ((Stsh) box).adjust(startSample);
			}
			else if (box instanceof Padb)
			{
				boxAdjust += ((Padb) box).adjust(startSample);
			}
			else if (box instanceof Sdtp)
			{
				boxAdjust += ((Sdtp) box).adjust(startSample);
			}
			else if (box instanceof Co64)
			{
				boxAdjust += ((Co64) box).adjust(offset, startSample, sample_offset);
			}
			else if (box instanceof Stco)
			{
				boxAdjust += ((Stco) box).adjust(offset, startSample, sample_offset);
			}
			/* -- these sub boxes are not actually modified during the adjust process.  skip processing of them
						      to save a little time.
						else if (box instanceof Stsd)
						{
						       //boxAdjust += ((Stsd) box).adjust();
						}
						else
						{
							// boxAdjust += ((Other) box).adjust();
						}
						*/
		}
		boxLen -= boxAdjust;
		return boxAdjust;
	}


}
