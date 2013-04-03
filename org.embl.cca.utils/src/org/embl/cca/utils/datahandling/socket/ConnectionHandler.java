package org.embl.cca.utils.datahandling.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.EnumSet;

public class ConnectionHandler {
	protected static enum ConnectionServingState {
		NOT_SERVING, STARTING_SERVING, SERVING, STOPPING_SERVING;
	}
	protected final static EnumSet<ConnectionServingState> servingSet = EnumSet.of(ConnectionServingState.STARTING_SERVING, ConnectionServingState.SERVING);
	protected final static EnumSet<ConnectionServingState> notServingSet = EnumSet.complementOf(servingSet);

	protected final static int WAIT_TIME_UNTIL_NEXT_READ = 10;
	protected final static int WAIT_TIME_UNTIL_NEXT_STATE_CHECK = 10;

	protected final Socket clientSocket;
	protected final ConnectionManager connectionManager;
	protected Thread ConnectionHandlerThread;
	protected ConnectionServingState connectionServingState;
	protected boolean stopServingClient;
	protected final Boolean waitStopServingClientLock; //This is a lock, has no value

	protected BufferedInputStream bis;
	protected BufferedOutputStream bos;

	public ConnectionHandler(final Socket clientSocket, final ConnectionManager connectionManager) throws IOException {
		this.clientSocket = clientSocket;
		this.connectionManager = connectionManager;
		ConnectionHandlerThread = null;
		connectionServingState = ConnectionServingState.NOT_SERVING;
		stopServingClient = false;
		waitStopServingClientLock = new Boolean(true);
		bis = new BufferedInputStream(clientSocket.getInputStream());
		bos = new BufferedOutputStream(clientSocket.getOutputStream());
	}

	public void startServing() throws InterruptedException {
		do {
			//Sync with stopListeningSynced, to avoid starting while waiting for stop
			synchronized (waitStopServingClientLock) {
				synchronized (connectionServingState) {
					if( servingSet.contains(connectionServingState) ) {
						if( stopServingClient )
							stopServingClient = false;
						return;
					}
					if( connectionServingState.equals(ConnectionServingState.NOT_SERVING) )
						connectionServingState = ConnectionServingState.STARTING_SERVING;
				}
			}
			if( connectionServingState.equals(ConnectionServingState.STOPPING_SERVING) )
				stopServingSynced();
		} while( !connectionServingState.equals(ConnectionServingState.STARTING_SERVING) );
		ConnectionHandlerThread = new Thread() {
			@Override
			public void run() {
				try {
					byte b[] = new byte[1024]; //TODO Hard coded buffer size
					int result = 0; //result=0 means can not read without blocking, aka no data available
					synchronized (connectionServingState) {
						connectionServingState = ConnectionServingState.SERVING;
					}
					while( !stopServingClient && result >= 0 ) {
						result = bis.read(b, 0, bis.available());
						if( result > 0 ) {
							connectionManager.getMessageHandler().connectionMessageReceived(ConnectionHandler.this, b, result);
						} else if( result == 0 )
							try {
								sleep(WAIT_TIME_UNTIL_NEXT_READ);
							} catch (InterruptedException e) {
								break;
							}
					}
				} catch( Exception e ) {
				}
				connectionServingState = ConnectionServingState.STOPPING_SERVING;
				synchronized (connectionServingState) {
					connectionManager.connectionTerminatingListener(ConnectionHandler.this);
					if( !clientSocket.isClosed() ) {
						try {
							clientSocket.close();
						} catch (IOException e) {
						}
					}
					try {
						bis.close();
					} catch (IOException e) {
					}
					bis = null;
					try {
						bos.close();
					} catch (IOException e) {
					}
					bos = null;
					ConnectionHandlerThread = null;
					connectionServingState = ConnectionServingState.NOT_SERVING;
					connectionManager.connectionTerminatedListener(ConnectionHandler.this);
				}
			}
		};
		ConnectionHandlerThread.start();
	}

	public void stopServingAsynced() {
		synchronized (ConnectionHandlerThread) {
			if( servingSet.contains(connectionServingState) && !stopServingClient )
				stopServingClient = true;
		}
	}

	public void stopServingSynced() throws InterruptedException {
		stopServingAsynced();
		if( stopServingClient ) {
			synchronized (waitStopServingClientLock) {
				do {
					synchronized (connectionServingState) {
						if( connectionServingState.equals(ConnectionServingState.NOT_SERVING) )
							break;
					}
					Thread.sleep(WAIT_TIME_UNTIL_NEXT_STATE_CHECK);
				} while( true );
			}
		}
	}

	public void sendMessage(final byte message[]) throws IOException {
		bos.write(message, 0, message.length);
		bos.flush();
	}
}
