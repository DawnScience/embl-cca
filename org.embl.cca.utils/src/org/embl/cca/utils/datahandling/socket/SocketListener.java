package org.embl.cca.utils.datahandling.socket;

import java.io.*;
import java.net.*;
import java.util.EnumSet;

//Goal is: receive "load_image %s\n", where %s is full path of a file

public class SocketListener {
	protected static enum ServerSocketListeningState {
		NOT_LISTENING, STARTING_LISTENING, LISTENING, STOPPING_LISTENING;
	}
	protected final static EnumSet<ServerSocketListeningState> listeningSet = EnumSet.of(ServerSocketListeningState.STARTING_LISTENING, ServerSocketListeningState.LISTENING);
	protected final static EnumSet<ServerSocketListeningState> notListeningSet = EnumSet.complementOf(listeningSet);

	protected final static int WAIT_TIME_UNTIL_NEXT_STATE_CHECK = 10;

	protected class WrappedBoolean {
		protected boolean bool;

		public WrappedBoolean(final boolean bool) {
			this.bool = bool;
		}

		public void setServeConnection(final boolean bool) {
			this.bool = bool;
		}

		public boolean getServeConnection() {
			return bool;
		}
	}

/*
	protected class StopRequestedException extends IOException {
		StopRequestedException() {
			super();
		}

		StopRequestedException( String message ) {
			super( message );
		}

		StopRequestedException( String message, Throwable cause ) {
			super( message, cause );
		}

		StopRequestedException( Throwable cause ) {
			super( cause );
		}

	}
*/
	protected final ConnectionManager connectionManager;
	protected int port;
	protected ServerSocket serverSocket;
//	protected DatagramSocket datagramSocket; //For UDP
	protected ServerSocketListeningState serverSocketListeningState;
	protected Boolean stopListening;
	protected final Boolean waitStopListeningLock; //This is a lock, has no value
	protected Thread serverSocketListener;

	public SocketListener() {
		this(0, null);
	}

	public SocketListener(final int port, final ConnectionManager connectionManager) {
		this.port = port;
		this.connectionManager = connectionManager;
		serverSocket = null;
		serverSocketListeningState = ServerSocketListeningState.NOT_LISTENING;
		stopListening = false;
		waitStopListeningLock = new Boolean(true);
		serverSocketListener = null;
	}

	public int getPort() {
		return port;
	}

	public void setPort(final int port) throws IOException, InterruptedException {
		if( this.port != port ) {
			ServerSocketListeningState savedServerSocketState;
			synchronized (serverSocketListeningState) {
				savedServerSocketState = serverSocketListeningState;
			}
			close();
			this.port = port;
			if( listeningSet.contains(savedServerSocketState) ) {
				open();
				startListening();
			}
		}
	}

	public int getLocalPort() {
		return serverSocket.getLocalPort();
	}

	public InetAddress getInetAddress() {
		return serverSocket.getInetAddress();
	}

	public void open() throws IOException {
		serverSocket = new ServerSocket(port); //Creates a bound socket (no need to bind it)
		serverSocket.setReuseAddress(true); //Force reusing, even if it is in timeout state (TODO could be option)
		serverSocket.setSoTimeout(50); //TODO Could be option
	}

	protected boolean checkStopListening(final boolean forceStopListening) {
		synchronized (serverSocketListeningState) {
			if( forceStopListening && !stopListening )
				stopListening = true;
			if( stopListening ) {
				serverSocketListeningState = ServerSocketListeningState.STOPPING_LISTENING;
				return true;
			}
		}
		return false;
	}

	public void startListening() throws InterruptedException {
		do {
			//Sync with stopListeningSynced, to avoid starting while waiting for stop
			synchronized (waitStopListeningLock) {
				synchronized (serverSocketListeningState) {
					if( listeningSet.contains(serverSocketListeningState) ) {
						if( stopListening )
							stopListening = false;
						return;
					}
					if( serverSocketListeningState.equals(ServerSocketListeningState.NOT_LISTENING) )
						serverSocketListeningState = ServerSocketListeningState.STARTING_LISTENING;
				}
			}
			if( serverSocketListeningState.equals(ServerSocketListeningState.STOPPING_LISTENING) )
				stopListeningSynced();
		} while( !serverSocketListeningState.equals(ServerSocketListeningState.STARTING_LISTENING) );
		serverSocketListener = new Thread() {
			@Override
			public void run() {
				Socket clientSocket = null;
				synchronized (serverSocketListeningState) {
					serverSocketListeningState = ServerSocketListeningState.LISTENING;
				}
				do {
					//Thread.interrupted() is for compatibility, this way converted to stopListening which is more flexible
					if( checkStopListening(Thread.interrupted()) )
						break;
					try {
						clientSocket = serverSocket.accept();
					} catch(SocketTimeoutException e) { //Conception bug in java: this is not exception, this is absolutely normal event
						continue;
					} catch (IOException e) { //In case of exception, we convert it to stopListening
						//Note: 'if' could be omitted, because always returns true for forcing true
						if( checkStopListening(true) )
							break;
					}
					//Thread.interrupted() is for compatibility, this way converted to stopListening which is more flexible
					if( checkStopListening(Thread.interrupted()) )
						break;
					final ConnectionHandler connectionHandler;
					try {
						connectionHandler = new ConnectionHandler(clientSocket, connectionManager);
					} catch (IOException e1) {
						continue;
					}
					clientSocket = null;
					final WrappedBoolean connectionStartedAnswer = new WrappedBoolean(false);
					//Here could send the client a MOTD (as not part of serving), for example
					connectionManager.connectionStarted(connectionHandler, connectionStartedAnswer);
					if( !connectionStartedAnswer.getServeConnection() )
						continue;
					try {
						connectionHandler.startServing();
					} catch (InterruptedException e) {
						connectionHandler.stopServingAsynced();
					}
				} while( true );
				synchronized (serverSocketListeningState) {
					stopListening = false;
					serverSocketListener = null;
					serverSocketListeningState = ServerSocketListeningState.NOT_LISTENING;
				}
			}
		};
		serverSocketListener.setDaemon(true);
		serverSocketListener.start();
	}

	public void stopListeningAsynced() {
		synchronized (serverSocketListeningState) {
			if( listeningSet.contains(serverSocketListeningState) && !stopListening )
				stopListening = true;
		}
	}

	public void stopListeningSynced() throws InterruptedException {
		stopListeningAsynced();
		if( stopListening ) {
			synchronized (waitStopListeningLock) {
				do {
					synchronized (serverSocketListeningState) {
						if( serverSocketListeningState.equals(ServerSocketListeningState.NOT_LISTENING) )
							break;
					}
					Thread.sleep(WAIT_TIME_UNTIL_NEXT_STATE_CHECK);
				} while( true );
			}
		}
	}

	public void close() throws IOException, InterruptedException {
		stopListeningSynced();
		if( serverSocket != null ) {
			synchronized (serverSocket) {
				serverSocket.close();
				serverSocket = null;
			}
		}
	}
}
