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

import java.io.Serializable;
import java.util.ArrayList;

public final class Mp4 implements Serializable, Cloneable
{
	private static final long serialVersionUID = 6745384226446076499L;
	public ArrayList<Box> boxes = new ArrayList<Box>();
	public  Ftyp ftyp;
	public  Moov moov;
	private Mdat mdat;
	public  long wallclock;
	public  long datarate;

	public Mp4()
	{

	}

	@Override public Object clone() throws CloneNotSupportedException
	{
		final Mp4 mp4 = (Mp4) super.clone();
		mp4.boxes = new ArrayList<Box>();
		for (final Box box : boxes)
		{
			Box newBox;
			if (box instanceof Ftyp)
			{
				newBox = (Ftyp) box.clone();
				mp4.ftyp = (Ftyp) newBox;
			}
			if (box instanceof Moov)
			{
				newBox = (Moov) box.clone();
				mp4.moov = (Moov) newBox;
			}
			if (box instanceof Mdat)
			{
				newBox = (Mdat) box.clone();
				mp4.mdat = (Mdat) newBox;
			}
			else
			{
				newBox = (Box) box.clone();
			}
			mp4.boxes.add(newBox);
		}
		return mp4;
	}
}
