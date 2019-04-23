package com.neilbliss; /**
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

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

final class Mp4PseudoServer
{
	public static void main(String[] args) throws Exception
	{
		Server server = new Server(8080);
		ContextHandlerCollection cc = new ContextHandlerCollection();
		ContextHandler ch = cc.addContext("/", ".");
		PseudoStreamingHandler pseudoStreamingHandler = new PseudoStreamingHandler();
		ch.setHandler(pseudoStreamingHandler);
		ch.setAllowNullPathInfo(true);
		server.setHandler(cc);
		server.start();
	}
}
