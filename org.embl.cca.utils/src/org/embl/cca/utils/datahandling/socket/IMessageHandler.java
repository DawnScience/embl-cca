package org.embl.cca.utils.datahandling.socket;

public interface IMessageHandler {
	public void connectionStartedMessage(ConnectionHandler connectionHandler);
	public void connectionMessageReceived(ConnectionHandler connectionHandler, byte message[], int messageLength);
	public void connectionTerminatingMessage(ConnectionHandler connectionHandler);

}
