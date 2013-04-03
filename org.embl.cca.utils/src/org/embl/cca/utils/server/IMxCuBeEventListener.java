package org.embl.cca.utils.server;

import org.embl.cca.utils.datahandling.socket.ConnectionHandler;

public interface IMxCuBeEventListener {
	public void fromMxCuBeConnectionStartedEvent(ConnectionHandler connectionHandler);
	public void fromMxCuBeConnectionTerminatingEvent(ConnectionHandler connectionHandler);
	public void fromMxCuBeLoadImageEvent(ConnectionHandler connectionHandler, String filePath);
	public void fromMxCuBeUnknownEvent(ConnectionHandler connectionHandler, String messageString);
}
