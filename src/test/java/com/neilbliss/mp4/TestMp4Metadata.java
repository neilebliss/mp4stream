package com.neilbliss.mp4;

import junit.framework.TestCase;

import java.io.*;

public final class TestMp4Metadata extends TestCase
{
	private final static String TEST_FILE_LOCATION = "./test_data/simpsons.com.neilbliss.mp4";

	public void setUp()
	{
	}

	public void testProcessFile()
	{
		Mp4Metadata mp4meta = new Mp4Metadata(new File(TEST_FILE_LOCATION));
		assertTrue("expected valid Mp4 object, got null", mp4meta.mp4 != null);
		assertTrue("expected valid Moov, didn't get one.", mp4meta.mp4.moov != null);
		assertTrue("bad determined wallclock length: " + mp4meta.mp4.wallclock, mp4meta.mp4.wallclock == 137);
	}

	public void testFindOffset()
	{
		Mp4Metadata mp4meta = new Mp4Metadata(null);
		assertNotNull("Expected valid mp4metadata object for null input file", mp4meta);
		long offset = mp4meta.findOffset(0);
		assertTrue("expected 0 return, got " + offset, offset == 0);
		mp4meta = new Mp4Metadata(new File(TEST_FILE_LOCATION));
		assertNotNull(mp4meta);
		assertNotNull(mp4meta.mp4);
		offset = mp4meta.findOffset(0);
		assertTrue("expected 71545 return, got " + offset, offset == 71545);
		offset = mp4meta.findOffset(5);
		assertTrue("expected 654002 return, got " + offset, offset == 654002);
		offset = mp4meta.findOffset(10);
		assertTrue("expected 5079379 return, got " + offset, offset == 5079379);
		offset = mp4meta.findOffset(15);
		assertTrue("expected 11620520 return, got " + offset, offset == 11620520);
		offset = mp4meta.findOffset(20);
		assertTrue("expected 17183248 return, got " + offset, offset == 17183248);

	}

	public void testSanity()
	{
		File inputFile = new File(TEST_FILE_LOCATION);
		Mp4Metadata mp4meta = new Mp4Metadata(inputFile);
		assertNotNull(mp4meta);
		Mp4 mp4 = mp4meta.mp4;
		assertNotNull(mp4);
		for (Trak trak : mp4.moov.traks)
		{
			// check all stco entries to validate that the offset + size isn't past the end of the file.
			for (int i = 0; i < trak.mdia.minf.stbl.stco.offsets.length; i++)
			{
				assertTrue("stco entry, trak: " + trak.tkhd.trackId + ", offset: " + trak.mdia.minf.stbl.stco.offsets[i] + ", size: " + trak.mdia.minf.stbl.stco.sizes[i] + " (" + (trak.mdia.minf.stbl.stco.offsets[i] + trak.mdia.minf.stbl.stco.sizes[i]) + "), is beyond EOF (" + inputFile.length() + ").",
					   trak.mdia.minf.stbl.stco.offsets[i] + trak.mdia.minf.stbl.stco.sizes[i] <= inputFile.length());
			}

			// validate that the firstChunk for all stsc lists starts from 1.
			if (trak.mdia.minf.stbl.stsc.entryCount > 0)
			{
				assertTrue("trak: " + trak.tkhd.trackId + ", has an incorrect starting firstChunk in the stsc list: " + trak.mdia.minf.stbl.stsc.entries[0].firstChunk,
					   trak.mdia.minf.stbl.stsc.entries[0].firstChunk == 1);
			}
		}
	}

	public void testAdjust()
	{
		Mp4Metadata mp4meta = new Mp4Metadata(new File(TEST_FILE_LOCATION));
		assertNotNull(mp4meta);
		long offset = mp4meta.findOffset(10f);
		mp4meta.adjustMoov(10f);
		assertTrue("expected valid Moov, didn't get one", mp4meta.mp4.moov != null);
		//assertTrue("bad duration.  expected 107593, got " + moov.mvhd.duration, moov.mvhd.duration == 107593);
		for (Trak trak : mp4meta.mp4.moov.traks)
		{
			// validate that the firstChunk for all stsc lists starts from 1.
			if (trak.mdia.minf.stbl.stsc.entryCount > 0)
			{
				assertTrue("trak: " + trak.tkhd.trackId + ", has an incorrect starting firstChunk in the stsc list: " + trak.mdia.minf.stbl.stsc.entries[0].firstChunk,
					   trak.mdia.minf.stbl.stsc.entries[0].firstChunk == 1);
			}
		}
		mp4meta = new Mp4Metadata(new File(TEST_FILE_LOCATION));
		offset = mp4meta.findOffset(20f);
		mp4meta.adjustMoov(20f);
		assertTrue("expected valid Moov, didn't get one", mp4meta.mp4.moov != null);
		assertTrue("bad duration.  expected 70440, got " + mp4meta.mp4.moov.mvhd.duration, mp4meta.mp4.moov.mvhd.duration == 70440);

		mp4meta = new Mp4Metadata(new File(TEST_FILE_LOCATION));
		offset = mp4meta.findOffset(30f);
		mp4meta.adjustMoov(30f);
		assertTrue("expected valid Moov, didn't get one", mp4meta.mp4.moov != null);
		assertTrue("bad duration.  expected 64534, got " + mp4meta.mp4.moov.mvhd.duration, mp4meta.mp4.moov.mvhd.duration == 64534);
		mp4meta = new Mp4Metadata(new File(TEST_FILE_LOCATION));
		offset = mp4meta.findOffset(40f);
		mp4meta.adjustMoov(40f);
		assertTrue("expected valid Moov, didn't get one", mp4meta.mp4.moov != null);
		assertTrue("bad duration.  expected 58578, got " + mp4meta.mp4.moov.mvhd.duration, mp4meta.mp4.moov.mvhd.duration == 58578);
		mp4meta = new Mp4Metadata(new File(TEST_FILE_LOCATION));
		offset = mp4meta.findOffset(50f);
		mp4meta.adjustMoov(50f);
		assertTrue("expected valid Moov, didn't get one", mp4meta.mp4.moov != null);
		assertTrue("bad duration.  expected 52422, got " + mp4meta.mp4.moov.mvhd.duration, mp4meta.mp4.moov.mvhd.duration == 52422);
		mp4meta = new Mp4Metadata(new File(TEST_FILE_LOCATION));
		offset = mp4meta.findOffset(60f);
		mp4meta.adjustMoov(60f);
		assertTrue("expected valid Moov, didn't get one", mp4meta.mp4.moov != null);
		assertTrue("bad duration.  expected 46516, got " + mp4meta.mp4.moov.mvhd.duration, mp4meta.mp4.moov.mvhd.duration == 46516);


	}

	public void testPerformance()
	{
		File f = new File(TEST_FILE_LOCATION);
		Mp4Metadata mp4 = new Mp4Metadata(f);
		long total = 0;
		Long delta = 0l;
		long start, end;
		for (int i = 0; i < 100; i++)
		{
			start = System.currentTimeMillis();
			mp4 = new Mp4Metadata(f);
			end = System.currentTimeMillis();
			delta = end - start;
			total += delta;
		}
		long avg = total / 100;

		assertTrue("average time too long: " + avg + " ms.", avg < 100);
	}


	private int getBoxPosition(String fileName, int[] box)
	{
		File file = new File(fileName);

		int filepos;
		filepos = 0;
		double currentoffset;
		currentoffset = 0;
		try
		{
			InputStream is = new FileInputStream(file);
			long length = file.length();

			int i;
			int ch[] = new int[4];
			Boolean mdat_found;

			for (i = 0; i < 4; i++)
			{
				ch[i] = is.read();
			}

			mdat_found = false;
			filepos = 0;
			while (!mdat_found)
			{
				// check if this is an mdat
				if (ch[0] == box[0] && ch[1] == box[1] && ch[2] == box[2] && ch[3] == box[3])
				{
					mdat_found = true;
				}
				else
				{
					for (i = 1; i < 4; i++)
					{
						ch[i - 1] = ch[i];
					}
					ch[3] = is.read();
					filepos++;
				}
			}

			// subtract 4 from mdat position to account for atom size
			filepos -= 4;

		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}

		return filepos;

	}

	public void testStss()
	{
		// testing the Stss box
		// stss is:
		// marking of random access points in the file
		// if box is not present, every sample is random access

		Stss theStss = new Stss();
		int numentries = 100;
		int entries_per_sync = 5;
		int removed_entries;
		int startentry;
		int this_entries;
		int i, j;

		theStss.createEntries(numentries);

		Stss testStss;

		try
		{
			for (j = 1; j < (numentries * entries_per_sync); j++)
			{
				testStss = (Stss) theStss.clone();

				// adjust the Stss atom
				testStss.adjust(j);

				removed_entries = (j - ((j - 1) % entries_per_sync)) / entries_per_sync;

				this_entries = numentries - 1 - removed_entries;
				assertTrue("Stss: After adjust of " + j + " there should be " + this_entries + " entries, found " + testStss.entries.length,
					   this_entries == testStss.entries.length);

				if (testStss.entries.length > 0)
				{
					startentry = entries_per_sync - ((j - 1) % entries_per_sync);

					assertTrue("Stss: first entry should be " + startentry + ", found " + testStss.entries[0],
						   startentry == testStss.entries[0]);
				}

			}

		}
		catch (CloneNotSupportedException ex)
		{
		}
	}

	public void testStts()
	{
		// test the stts box
		// stts is:
		// decoding from decoding time to sample number
		// each entry give number of samples with same time delta

		Stts theStts = new Stts();
		int numentries = 100;
		int i, j;
		int current_time;
		int current_entry;
		int this_sample_count;
		long time_delta;
		long time_rem;
		long num_skipped_samples;
		long curr_samples_in_group;

		theStts.createEntries(numentries);

		// create some data for the entries
		current_time = 0;
		for (i = 0; i < numentries; i++)
		{
			// the number of samples with the same sampleDelta
			theStts.sampleCounts[i] = (i + 1);

			// set the sampleDelta of each sample in this group
			theStts.sampleDeltas[i] = (i + 1) * 2;

			theStts.startTimes[i] = current_time;

			// increment the current time:
			// multiply sampleCount (number of samples with the same delta) by the sampleDelta
			current_time += theStts.sampleDeltas[i] * theStts.sampleCounts[i];

			// set the end time of this sample
			theStts.endTimes[i] = current_time;
		}

		Stts testStts;
		try
		{
			current_entry = 0;
			for (j = 0; j < current_time; j++)
			{
				testStts = (Stts) theStts.clone();
				testStts.adjust((long) j);

				// should we increment current sample?
				if (j >= theStts.endTimes[current_entry])
				{
					// this test time is later than the end time of the current entry
					// step to the next one
					current_entry++;
				}

				// how far are we into this sample group?
				time_delta = j - theStts.startTimes[current_entry];

				// with this time delta, how many samples will we need to skip?
				time_rem = time_delta % theStts.sampleDeltas[current_entry];
				num_skipped_samples = ((time_delta - time_rem) / theStts.sampleDeltas[current_entry]);

				curr_samples_in_group = theStts.sampleCounts[current_entry] - num_skipped_samples;

				// what should happen if the end time of one entry matches the start time of the next?

				// current number of samples should match
				assertTrue("Stts: samples in adjusted Stts should be " + curr_samples_in_group + ", found " + testStts.sampleCounts[0],
					   curr_samples_in_group == testStts.sampleCounts[0]);

				// current sampleDelta should match
				assertTrue("Stts: sample delta should be " + theStts.sampleDeltas[current_entry] + ", found " + testStts.sampleDeltas[0],
					   theStts.sampleDeltas[current_entry] == testStts.sampleDeltas[0]);

			}

		}
		catch (CloneNotSupportedException ex)
		{
		}

	}


	public void testStz2()
	{
		// should also test 64 bit version of this box

		// stsz is:
		// sample count and size in bytes of each sample

		// Stsz.adjust function takes a start sample index
		// if sample_size = 0, then each entry has a different size

		// create a test Stsz object
		int i, j;
		int numentries;
		int shortened_entries;
		Stz2 theStsz = new Stz2();
		assertNotNull(theStsz);
		Stz2 s2 = new Stz2(new FullBox());
		assertNotNull(s2);

		theStsz.sampleSize = 0;
		numentries = 100;
		theStsz.createEntries(numentries);

		for (i = 0; i < theStsz.entryCount; i++)
		{
			theStsz.entries[i].sampleId = i;
			theStsz.entries[i].size = 10;
		}

		Stz2 testStsz;

		try
		{
			for (j = 0; j < numentries; j++)
			{
				testStsz = (Stz2) theStsz.clone();
				testStsz.adjust(j);

				//debugPrint("adjusting to sample "+j);
				//debugPrint("entryCount is: "+testStsz.entryCount);

				shortened_entries = numentries - j;

				assertTrue("Should be " + shortened_entries + " entries, there were " + testStsz.entryCount,
					   shortened_entries == testStsz.entryCount);


				//for (i = 0; i < testStsz.entryCount; i++)
				//{
				//    debugPrint("entry "+i+" "+theStsz.entries[i].sampleId+" size "+theStsz.entries[i].size);
				//}


			}

			try
			{
				theStsz.boxType = "stsZ";
				theStsz.write(new FileOutputStream(new File("./stz2.out")));
			}
			catch (Exception e)
			{
				e.printStackTrace();
				assertTrue(false);
			}
			try
			{
				Stz2 s3 = new Stz2();
				s3.read(new FileInputStream(new File("./stz2.out")));
				assertTrue(s3.boxType.equals("stsZ"));
			}
			catch (Exception e)
			{
				e.printStackTrace();
				assertTrue(false);
			}
			finally
			{
				new File("./stz2.out").delete();
			}
		}
		catch (CloneNotSupportedException ex)
		{
		}
		// should also test 64 bit version of this box

		// stsz is:
		// sample count and size in bytes of each sample

		// Stsz.adjust function takes a start sample index
		// if sample_size = 0, then each entry has a different size

		// create a test Stsz object
		theStsz = new Stz2();

		theStsz.sampleSize = 0;
		numentries = 100;
		theStsz.createEntries(numentries);

		for (i = 0; i < theStsz.entryCount; i++)
		{
			theStsz.entries[i].sampleId = i;
			theStsz.entries[i].size = 10;
		}

		try
		{
			for (j = 0; j < numentries; j++)
			{
				testStsz = (Stz2) theStsz.clone();
				testStsz.adjust(j);

				//debugPrint("adjusting to sample "+j);
				//debugPrint("entryCount is: "+testStsz.entryCount);

				shortened_entries = numentries - j;

				assertTrue("Should be " + shortened_entries + " entries, there were " + testStsz.entryCount,
					   shortened_entries == testStsz.entryCount);


				//for (i = 0; i < testStsz.entryCount; i++)
				//{
				//    debugPrint("entry "+i+" "+theStsz.entries[i].sampleId+" size "+theStsz.entries[i].size);
				//}


			}
		}
		catch (CloneNotSupportedException ex)
		{
		}
	}

	// test for stsc box
	public void testStsc()
	{

		// the stsc atom:
		// contains a series of entries
		// each entry give the index of the first chunk in a run of chunks
		// with the same characteristics
		// subtract an entry with the previous one to give the number
		// of chunks in this run


		int num_stsc_entries;
		int currentChunk;
		int samplesInThisChunk;
		int currSampleId;
		int chunksInRun;
		int i;

		int skipped_runs;
		int skipped_remainder;

		int skipped_chunks;
		int skipped_chunk_remainder;

		int testStartSample;
		int testSamplesPerChunk;
		int currentRun;

		int chunksInFirstRun;
		int chunksInNextRun;

		// create stsc object
		Stsc theStsc = new Stsc();
		Stsc testStsc;
		StscEntry theStscEntry;

		num_stsc_entries = 10;
		currentChunk = 1;
		currSampleId = 1;
		samplesInThisChunk = 5;
		chunksInRun = 10;

		// create stsc entries
		theStsc.entries = new StscEntry[num_stsc_entries];
		for (i = 0; i < num_stsc_entries; i++)
		{
			theStscEntry = new StscEntry();

			//theStscEntry = theStsc.entries[i];

			// populate entry with sample values
			theStscEntry.chunkGroupId = i;

			// set first chunk
			theStscEntry.firstChunk = currentChunk;

			// set sample id
			theStscEntry.firstSampleId = currSampleId;

			// set samples per chunk
			theStscEntry.samplesPerChunk = samplesInThisChunk;

			// set sample description id
			theStscEntry.sampleDescriptionIndex = 1;

			// set number of chunks
			theStscEntry.chunks = chunksInRun;

			// put this entry into the list of stsc entries
			theStsc.entries[i] = theStscEntry;
			theStsc.entryCount++;


			// increment chunk number
			currentChunk += chunksInRun;

			// increment sample number
			currSampleId += samplesInThisChunk * chunksInRun;
		}

		// output the stsc
		for (i = 0; i < theStsc.entryCount; i++)
		{
			theStscEntry = theStsc.entries[i];
		}

		// now try adjusting this stsc
		// move to specified sample

		for (testStartSample = 1; testStartSample < 60; testStartSample++)
		{
			try
			{
				testStsc = (Stsc) theStsc.clone();
				testStsc.adjust(testStartSample);


				// output the stsc
				for (i = 0; i < testStsc.entryCount; i++)
				{
					theStscEntry = testStsc.entries[i];
				}


				// now need to check if the new run of stsc entries are correct
				// total number of chunks should be original - offset

				// remove chunks that are completely skipped
				// determine how many runs of chunks to skip

				// how many samples remain after fully skipping runs of chunks?
				skipped_remainder = (testStartSample - 1) % (chunksInRun * samplesInThisChunk);

				// how many runs of chunks are fully skipped?
				skipped_runs = ((testStartSample - 1) - skipped_remainder) / (chunksInRun * samplesInThisChunk);

				// now determine how many full chunks to skip from the remaining samples
				skipped_chunk_remainder = skipped_remainder % samplesInThisChunk;
				skipped_chunks = (skipped_remainder - skipped_chunk_remainder) / samplesInThisChunk;

				currentRun = 0;
				if (skipped_chunk_remainder > 0 || skipped_chunks > 0)
				{
					// if remainder is not equal to samplesPerChunk, there will be a run of one chunk in the beginning
					testSamplesPerChunk = samplesInThisChunk - skipped_chunk_remainder;

					// get samples per chunk in the first run of chunks
					assertTrue("SamplesPerChunk in run " + currentRun + " should be " + testSamplesPerChunk + ", found " + testStsc.entries[currentRun].samplesPerChunk,
						   testSamplesPerChunk == testStsc.entries[currentRun].samplesPerChunk);

					currentRun++;

					// check for skipped chunks within the run

					// if any chunks are skipped within the first run, a new run of 1 chunk is created
					chunksInFirstRun = 1;
					chunksInNextRun = chunksInRun - chunksInFirstRun - skipped_chunks;

					if (chunksInNextRun == 0)
					{
						chunksInNextRun = chunksInRun;
					}
					assertTrue("Chunks in run " + currentRun + " should be " + chunksInNextRun + ", found " + testStsc.entries[currentRun].chunks,
						   chunksInNextRun == testStsc.entries[currentRun].chunks);

					currentRun++;
				}

			}
			catch (CloneNotSupportedException ex)
			{
			}
		}
	}


	public void testStco()
	{
		// create a new stco object and try adjusting it.
		// put in some bogus offsets and try to adjust it

		// stco contains disk offset information for the storage chunks associated with
		// this track's samples
		// these are absolute file positions, not relative to any other structure

		// co64 - 64 bit version of stco

		// create stco objecc
		int test_stco_entries = 10;
		Stco theStco = new Stco();
		// come up with some stco entries
		//theStco.entries = new StcoEntry[test_stco_entries];
		theStco.offsets = new long[test_stco_entries];
		theStco.sizes = new long[test_stco_entries];
		theStco.firstSampleIds = new int[test_stco_entries];
		theStco.lastSampleIds = new int[test_stco_entries];
		theStco.alignedOffsetsLists = new long[test_stco_entries][];

		int i, j, thisSampleId, k;
		double droppedSamples;
		int curr_offset = 1000; // this is the test file offset
		int samples_per_chunk = 10; // sample value for samples in each chunk
		int curr_sample = 0;
		int total_samples = 0;
		int entrysize = 100;
		long offset;
		double remainder;

		for (i = 0; i < test_stco_entries; i++)
		{
			// create a new Stco entry
			//theStco.entries[i] = new StcoEntry();

			// add the current offset to the Stco entry
			theStco.offsets[i] = curr_offset;
			theStco.sizes[i] = entrysize;

			// set number of samples per chunk
			theStco.firstSampleIds[i] = curr_sample;
			theStco.lastSampleIds[i] = curr_sample + samples_per_chunk - 1;

			// set the positions of the samples within the chunk
			// allocate samples_per_chunk entries for this chunk
			theStco.alignedOffsetsLists[i] = new long[samples_per_chunk];
			// loop through these entries and give them absolute offsets
			for (j = 0; j < samples_per_chunk; j++)
			{
				// give each of the samples an offset
				theStco.alignedOffsetsLists[i][j] = curr_offset + (j * samples_per_chunk);
			}

			// increment current sample
			curr_sample += samples_per_chunk;

			curr_offset += entrysize;
		}
		total_samples = curr_sample;
		Stco testStco;
		Sample theStartSample = new Sample();
		int curr_entry = 0;
		int curr_entry_sample = 0;
		for (i = 0; i < total_samples - 1; i++)
		{
			theStartSample.sampleId = i;
			theStartSample.offset = theStco.alignedOffsetsLists[curr_entry][curr_entry_sample];
			curr_entry_sample++;
			// step to the next entry if needed
			if (theStco.lastSampleIds[curr_entry] <= i)
			{
				curr_entry++;
				curr_entry_sample = 0;
			}
			try
			{
				testStco = (Stco) theStco.clone();
				offset = 99;
				testStco.adjust(offset, (int) theStartSample.sampleId, theStartSample.offset);
				assertTrue("Sample " + i + ", start offsets do not match.  Expected: " + theStartSample.offset + ", actual: " + (testStco.offsets[0] + offset),
					   theStartSample.offset == (testStco.offsets[0] + offset));

				// get number of entries that should have been dropped
				k = 0;
				while (theStco.offsets[k] < theStartSample.offset && k < (test_stco_entries - 1))
				{
					k++;
				}
				remainder = theStartSample.offset - theStco.offsets[k];
				if (remainder < 0)
				{
					k--;
					remainder += entrysize;
				}
				assertTrue("Size of first stco entry does not match", testStco.sizes[0] == (entrysize - remainder));
			}
			catch (CloneNotSupportedException ex)
			{
			}

		}
	}

	public void testCo64()
	{
		// create a new stco object and try adjusting it.
		// put in some bogus offsets and try to adjust it

		// stco contains disk offset information for the storage chunks associated with
		// this track's samples
		// these are absolute file positions, not relative to any other structure

		// co64 - 64 bit version of stco

		// create stco objecc
		int test_stco_entries = 10;
		Co64 theStco = new Co64();
		// come up with some stco entries
		//theStco.entries = new StcoEntry[test_stco_entries];
		theStco.offsets = new long[test_stco_entries];
		theStco.sizes = new long[test_stco_entries];
		theStco.firstSampleIds = new int[test_stco_entries];
		theStco.lastSampleIds = new int[test_stco_entries];
		theStco.alignedOffsetsLists = new long[test_stco_entries][];

		int i, j, thisSampleId, k;
		double droppedSamples;
		int curr_offset = 1000; // this is the test file offset
		int samples_per_chunk = 10; // sample value for samples in each chunk
		int curr_sample = 0;
		int total_samples = 0;
		int curr_entry = 0;
		int curr_entry_sample = 0;
		int entrysize = 100;
		long offset;
		double remainder;

		for (i = 0; i < test_stco_entries; i++)
		{
			// create a new Stco entry
			//theStco.entries[i] = new StcoEntry();

			// add the current offset to the Stco entry
			theStco.offsets[i] = curr_offset;
			theStco.sizes[i] = entrysize;

			// set number of samples per chunk
			theStco.firstSampleIds[i] = curr_sample;
			theStco.lastSampleIds[i] = curr_sample + samples_per_chunk - 1;

			// set the positions of the samples within the chunk
			// allocate samples_per_chunk entries for this chunk
			theStco.alignedOffsetsLists[i] = new long[samples_per_chunk];
			// loop through these entries and give them absolute offsets
			for (j = 0; j < samples_per_chunk; j++)
			{
				// give each of the samples an offset
				theStco.alignedOffsetsLists[i][j] = curr_offset + (j * samples_per_chunk);
			}

			// increment current sample
			curr_sample += samples_per_chunk;

			curr_offset += entrysize;
		}
		total_samples = curr_sample;

		//theStco.entryCount = theStco.entries.length;

		//StcoEntry thisStcoEntry;
		Stco testStco;
		Sample theStartSample = new Sample();
		// choose different offsets to test

		//thisStcoEntry = theStco.entries[curr_entry];
		for (i = 0; i < total_samples - 1; i++)
		{
			// set the sample id
			theStartSample.sampleId = i;

			theStartSample.offset = theStco.alignedOffsetsLists[curr_entry][curr_entry_sample];

			curr_entry_sample++;

			// step to the next entry if needed
			if (theStco.lastSampleIds[curr_entry] <= i)
			{
				curr_entry++;
				//thisStcoEntry = theStco.entries[curr_entry];
				curr_entry_sample = 0;
			}

			// now run the stco adjust test
			// copy the existing stco
			try
			{
				testStco = (Stco) theStco.clone();

				offset = 99;

				// adjust the stco using the current start sample
				testStco.adjust(offset, (int) theStartSample.sampleId, theStartSample.offset);

				// assert that the starting offset of the first entry in the new stco
				// must equal the start sample's offset plus the offset passed to the adjust parameter
				assertTrue("Start offsets do not match.  Expected: " + theStartSample.offset + ", actual: " + (testStco.offsets[0] + offset), theStartSample.offset == (testStco.offsets[0] + offset));

				// get number of entries that should have been dropped
				k = 0;
				while (theStco.offsets[k] < theStartSample.offset && k < (test_stco_entries - 1))
				{
					k++;
				}

				// calculate remainder
				remainder = theStartSample.offset - theStco.offsets[k];
				if (remainder < 0)
				{
					k--;
					remainder += entrysize;
				}

				// assert that the size of the first entry should equal the entry size
				// minus the remainder after full chunks are removed
				assertTrue("Size of first stco entry does not match", testStco.sizes[0] == (entrysize - remainder));

			}
			catch (CloneNotSupportedException ex)
			{
			}

		}
	}

	public void testBox()
	{
		Box b = new Box();
		assertNotNull(b);
		Box bb = new Box(b);
		assertNotNull(bb);

		assertTrue(0 == b.adjust());
		byte[] buf = b.write8(16l);
		long value = b.read8(buf);
		assertTrue(value == 16l);
		buf = b.write16(16l);
		value = b.read16(buf);
		assertTrue(value == 16l);
		buf = b.write32(16);
		value = b.read32(buf);
		assertTrue(value == 16l);
		buf = b.write64(16l);
		value = b.read64(buf);
		assertTrue(value == 16l);
	}
}
