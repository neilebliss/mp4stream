import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

/**
 * Copyright (c) 2012 3Crowd Technologies, Inc.
 * <p/>
 * See LICENSE.txt for licensing terms covering this software.
 */
final class Mp4PseudoServer
{
	public static void main(String[] args) throws Exception
	{
		Server server = new Server();
		SocketConnector sc = new SocketConnector();
		sc.setPort(8080);
		Connector[] connectors = new Connector[1];
		connectors[0] = sc;
		server.setConnectors(connectors);
		ContextHandlerCollection cc = new ContextHandlerCollection();
		ContextHandler ch = cc.addContext("/", ".");
		PseudoStreamingHandler pseudoStreamingHandler = new PseudoStreamingHandler();
		ch.setHandler(pseudoStreamingHandler);
		ch.setAllowNullPathInfo(true);
		server.setHandler(cc);
		server.start();
	}
}
