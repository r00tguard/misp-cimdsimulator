package com.ixonos.cimd.simulator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CIMD server simulator.
 * 
 * @author Marko Asplund
 */
public class CIMDSimulator {
	private static final Logger logger = LoggerFactory.getLogger(CIMDSimulator.class);
	private IoAcceptor cimdAcceptor;
	private int port;
	private IoAcceptor msgAcceptor;
	private int messagePort;

	public CIMDSimulator(int port, int messagePort) {
		this.port = port;
		this.messagePort = messagePort;
		
		cimdAcceptor = new NioSocketAcceptor();
    cimdAcceptor.getFilterChain().addLast("logger", new LoggingFilter() );
    cimdAcceptor.getFilterChain().addLast("protocol", new ProtocolCodecFilter(new CIMDCodecFactory()));
		CIMDMessageHandler handler = new CIMDMessageHandler();
		cimdAcceptor.setHandler(handler);
    
    msgAcceptor = new NioSocketAcceptor();
    msgAcceptor.getFilterChain().addLast("logger", new LoggingFilter() );
    msgAcceptor.getFilterChain().addLast("protocol",
    		new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
    TextMessageHandler textHandler = new TextMessageHandler(cimdAcceptor.getManagedSessions());
    msgAcceptor.setHandler(textHandler);
	}
	
	public void start() throws IOException {
		cimdAcceptor.bind(new InetSocketAddress(port));
		msgAcceptor.bind(new InetSocketAddress(messagePort));
    logger.info("running");
	}
	
	
	public static void main(String ... args) throws IOException {
		String propBase = CIMDSimulator.class.getSimpleName().toLowerCase();
		CIMDSimulator simu = new CIMDSimulator(Integer.getInteger(propBase+".port"),
				Integer.getInteger(propBase+".messagePort"));
		simu.start();
	}

}
