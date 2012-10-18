import com.sun.corba.se.spi.ior.Writeable;
import mp4.*;
import org.eclipse.jetty.server.BlockingHttpConnection;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Copyright (c) 2012 3Crowd Technologies, Inc.
 * <p/>
 * See LICENSE.txt for licensing terms covering this software.
 */
final class PseudoStreamingHandler extends AbstractHandler
{

	@Override public void handle(final String jettyTarget,
				     final Request request,
				     final HttpServletRequest httpRequest,
				     final HttpServletResponse response) throws IOException, ServletException
	{
		request.setHandled(true);

		OutputStream socketTarget = BlockingHttpConnection.getCurrentConnection().getOutputStream();
		WritableByteChannel outChannel = Channels.newChannel(socketTarget);
		String filename = httpRequest.getPathInfo();
		Mp4Metadata mp4 = null;
		File mp4File = new File(filename);
		try
		{
			mp4 = new Mp4Metadata(mp4File);
		}
		catch (Exception e)
		{
			response.setHeader("Failure-Reason", "file not found or bad mp4 file");
			response.setStatus(404);
			return;
		}

		//
		// we expect that there's a start=XXX query parameter, specifying the start time in
		// seconds.  If we don't find it, we'll just play the show from the beginning.
		//

		String query = httpRequest.getQueryString();
		String[] fields = query.split("&");
		String startTime = "0";
		for (String field : fields)
		{
			if (field.contains("start="))
			{
				String[] startFields = field.split("=");
				startTime = fields[1];
				break;
			}
		}
		Float seekTime = Float.parseFloat(startTime);
		long offset = Mp4Utils.findOffset(seekTime, mp4.trakStubs);
		long contentLength = mp4File.length() - offset;


		// read in the "raw" mp4metadata and adjust it.
		long stcoRebase = 0;

		//
		// do the needful to adjust the moov box substructures to make it seem
		// as though the movie starts at the time requested.
		//
		if (!mp4.mp4.moov.adjusted)
		{
			mp4.adjustMoov(seekTime);
		}

		long contentLen = 0;
		for (final Box box : mp4.mp4.boxes)
		{
			if (box instanceof Mdat || "free".equals(box.boxType))
			{
				// do nothing
			}
			else if (box instanceof Moov)
			{
				// use the adjusted moov box.
				contentLen += mp4.mp4.moov.boxLen;
				stcoRebase += mp4.mp4.moov.boxLen;
			}
			else
			{
				contentLen += box.boxLen;
				stcoRebase += box.boxLen;
			}
		}

		contentLen += 8 + (int) (mp4File.length() - offset);
		stcoRebase += 8;

		for (final Trak trak : mp4.mp4.moov.traks)
		{
			if (!trak.mdia.minf.stbl.stco.rebased)
			{
				trak.mdia.minf.stbl.stco.rebase(stcoRebase);
			}
		}

		response.setHeader("Content-Length", Long.toString(contentLen));

		//
		// send the data to the client.
		//
		for (final Box box : mp4.mp4.boxes)
		{
			if (box instanceof Ftyp)
			{
				((Ftyp) box).write(socketTarget);
			}
			else if (box instanceof Moov)
			{
				// use our adjusted moov.
				mp4.mp4.moov.write(socketTarget);
			}
			else if (box instanceof Mdat || "free".equals(box.boxType))
			{
				// do not write this one.
			}
			else
			{
				((Other) box).write(socketTarget);
			}
		}

		//
		// now try to save this off to disk for later.
		//

		//
		// here, we need to construct the beginning of an mdat box that reflects
		// the new length of the remaining movie data, taking into account the
		// requested offset.
		//

		Mdat mdat = new Mdat();
		mdat.boxLen = (int) (8 + mp4File.length() - offset);
		mdat.write(socketTarget);
		//
		// from here, read the rest of the data from the requested
		// offset into the cached datafile, as we would for any other
		// file.
		//

		FileChannel mp4FileChannel = new FileInputStream(mp4File).getChannel();
		long currentPosition = offset;
		long bytesRemaining = mp4File.length() - offset;
		long nbytes = 0;
		while (bytesRemaining > 0)
		{
			nbytes = mp4FileChannel.transferTo(currentPosition, bytesRemaining, outChannel);
			currentPosition += nbytes;
			bytesRemaining -= nbytes;
		}
		outChannel.close();
		mp4FileChannel.close();
	}
}
