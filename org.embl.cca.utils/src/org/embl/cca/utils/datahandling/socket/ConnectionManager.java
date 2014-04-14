package org.embl.cca.utils.datahandling.socket;

import java.io.IOException;
import java.util.Vector;

import org.embl.cca.utils.datahandling.socket.SocketListener.WrappedBoolean;

public class ConnectionManager {
	protected final static int WAIT_TIME_UNTIL_NEXT_STATE_CHECK = 10;

	protected Boolean servingConnectionsState;
	protected Boolean stopServingConnections;
	protected final Boolean waitStopServingConnectionLock; //This is a lock, has no value
	protected Vector<ConnectionHandler> connections;
	protected SocketListener socketListener;
	protected IMessageHandler messageHandler;

	public ConnectionManager() {
		servingConnectionsState = false;
		stopServingConnections = false;
		waitStopServingConnectionLock = new Boolean(true);
		connections = new Vector<ConnectionHandler> ();
		socketListener = null;
		messageHandler = null;
	}

	public ConnectionManager(final int port, final IMessageHandler messageHandler) throws IOException, InterruptedException { //TODO Make this more generic argumentwise?
		this();
		this.messageHandler = messageHandler;
		socketListener = new SocketListener(port, this);
		IOException ioE = null;
		InterruptedException interruptedE = null;
		try {
			socketListener.open();
			startServingConnections();
			try {
				socketListener.startListening();
			} catch (InterruptedException e) {
				interruptedE = e;
			}
		} catch (IOException e) {
			ioE = e;
		}
		if( ioE != null || interruptedE != null ) {
			try {
				socketListener.close();
			} catch (IOException e1) {
			} catch (InterruptedException e1) {
			}
			socketListener = null;
			if( ioE != null )
				throw ioE;
			else
				throw interruptedE;
		}
	}

	public void dispose() {
		if( socketListener != null ) {
			try {
				socketListener.close();
			} catch (IOException e) {
			} catch (InterruptedException e) {
			}
			socketListener = null;
		}
		try {
			stopServingConnectionsSynced();
			connections = null;
		} catch (InterruptedException e) {
		}
	}

	public IMessageHandler getMessageHandler() {
		return messageHandler;
	}

	protected void connectionStarted(final ConnectionHandler connectionHandler, final WrappedBoolean connectionStartedAnswer) {
		getMessageHandler().connectionStartedMessage(connectionHandler);
		if( servingConnectionsState && !stopServingConnections )
			connectionStartedAnswer.setServeConnection( true );
		if( connectionStartedAnswer.getServeConnection() )
			connections.add(connectionHandler);
	}

	protected void connectionTerminatingListener(final ConnectionHandler connectionHandler) {
		getMessageHandler().connectionTerminatingMessage(connectionHandler);
	}

	protected void connectionTerminatedListener(final ConnectionHandler connectionHandler) {
		connections.removeElement(connectionHandler);
	}

	public int getConnectionAmount() {
		return connections.size();
	}

	public void startServingConnections() {
		synchronized (waitStopServingConnectionLock) {
			synchronized (servingConnectionsState) {
				if( servingConnectionsState || stopServingConnections )
					return;
				servingConnectionsState = true;
			}
		}
	}

	public void stopServingConnectionsAsynced() {
		synchronized (servingConnectionsState) {
			if( servingConnectionsState && !stopServingConnections ) {
				stopServingConnections = true;
				for( ConnectionHandler connection : connections ) {
					connection.stopServingAsynced();
				}
			}
		}
	}

	public void stopServingConnectionsSynced() throws InterruptedException {
		stopServingConnectionsAsynced();
		if( stopServingConnections ) {
			synchronized (waitStopServingConnectionLock) {
				do {
					if( getConnectionAmount() == 0 )
						break;
					Thread.sleep(WAIT_TIME_UNTIL_NEXT_STATE_CHECK);
				} while( true );
			}
		}
	}

	public static void main(String args[]) {
		ConnectionManager connectionManager = new ConnectionManager();
		SocketListener socketListener = new SocketListener(9999, connectionManager);
		do {
			try {
				socketListener.open();
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
			connectionManager.startServingConnections();
			try {
				socketListener.startListening();
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				break;
			}
		} while( false );
		try {
			socketListener.close();
		} catch (IOException e) {
		} catch (InterruptedException e) {
		}
		socketListener = null;
		try {
			connectionManager.stopServingConnectionsSynced();
		} catch (InterruptedException e) {
		}
		connectionManager = null;
	}
}
