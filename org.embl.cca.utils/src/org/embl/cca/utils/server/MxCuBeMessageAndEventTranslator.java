package org.embl.cca.utils.server;

import java.io.IOException;

import org.dawb.common.util.list.ListenerList;
import org.embl.cca.utils.datahandling.socket.ConnectionHandler;
import org.embl.cca.utils.datahandling.socket.IMessageHandler;

public class MxCuBeMessageAndEventTranslator implements IMessageHandler {
	public MxCuBeMessageAndEventTranslator() {
	}

	public void connectionStartedMessage(ConnectionHandler connectionHandler) {
		fireFromMxCuBeConnectionStartedEvent(connectionHandler);
	}

	public void connectionTerminatingMessage(ConnectionHandler connectionHandler) {
		fireFromMxCuBeConnectionTerminatingEvent(connectionHandler);
	}

	public void connectionMessageReceived(ConnectionHandler connectionHandler, byte message[], int messageLength) {
		final String LoadImage = "load_image";
		String messageString = new String(message, 0, messageLength).trim();
		if( messageString.startsWith(LoadImage))
			fireFromMxCuBeLoadImageEvent(connectionHandler, messageString.substring(LoadImage.length()).trim());
		else
			fireFromMxCuBeUnknownEvent(connectionHandler, messageString);
//		System.out.println("SOCKET: read " + messageLength + " bytes: " + messageString);
//		try { //This is the simplest service: echo, for TESTING purpose
//			connectionHandler.sendMessage(Arrays.copyOf(message, messageLength)); //For TESTING purpose
//		} catch (IOException e) {
//			connectionHandler.stopServingAsynced(); //For TESTING purpose
//		} //For TESTING purpose, as echo server
//		if( messageString.equals( "quit") ) //For TESTING purpose
//			connectionHandler.stopServingAsynced(); //For TESTING purpose
	}

	public void toMxCubeWelcomeEvent(ConnectionHandler connectionHandler, String welcome) throws IOException {
		connectionHandler.sendMessage(welcome.getBytes());
	}

	protected final ListenerList<IMxCuBeEventListener> mxCuBeMessageListener = new ListenerList<IMxCuBeEventListener>();

	public void addMxCuBeEventListener(IMxCuBeEventListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("The user argument can not be null");
		mxCuBeMessageListener.add(listener);
	}

	public void removeMxCuBeEventListener(IMxCuBeEventListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("The user argument can not be null");
		mxCuBeMessageListener.remove(listener);
	}

	protected void fireFromMxCuBeConnectionStartedEvent(ConnectionHandler connectionHandler) {
		for( IMxCuBeEventListener listener : mxCuBeMessageListener )
			listener.fromMxCuBeConnectionStartedEvent(connectionHandler);
	}

	protected void fireFromMxCuBeConnectionTerminatingEvent(ConnectionHandler connectionHandler) {
		for( IMxCuBeEventListener listener : mxCuBeMessageListener )
			listener.fromMxCuBeConnectionTerminatingEvent(connectionHandler);
	}

	protected void fireFromMxCuBeLoadImageEvent(ConnectionHandler connectionHandler, String filePath) {
		for( IMxCuBeEventListener listener : mxCuBeMessageListener )
			listener.fromMxCuBeLoadImageEvent(connectionHandler, filePath);
	}

	protected void fireFromMxCuBeUnknownEvent(ConnectionHandler connectionHandler, String messageString) {
		for( IMxCuBeEventListener listener : mxCuBeMessageListener )
			listener.fromMxCuBeUnknownEvent(connectionHandler, messageString);
	}
}
