package org.embl.cca.dviewer.server;

import java.io.File;
import java.io.IOException;

import org.dawb.common.ui.util.EclipseUtils;
import org.embl.cca.dviewer.ui.editors.ImageEditor;
import org.embl.cca.utils.datahandling.FilePathEditorInput;
import org.embl.cca.utils.datahandling.socket.ConnectionHandler;
import org.embl.cca.utils.datahandling.socket.ConnectionManager;
import org.embl.cca.utils.datahandling.socket.IMessageHandler;
import org.embl.cca.utils.extension.CommonExtension;
import org.embl.cca.utils.server.IMxCuBeEventListener;
import org.embl.cca.utils.server.MxCuBeMessageAndEventTranslator;
import org.embl.cca.utils.threading.ExecutableManager;
import org.embl.cca.utils.threading.TrackableRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MxCuBeConnectionManager extends ConnectionManager implements IMxCuBeEventListener {
	private static Logger logger = LoggerFactory.getLogger(MxCuBeConnectionManager.class);

	protected ExecutableManager remotedImageDisplayTracker = null;
//	protected final static String DVIEWER_EDITOR = org.dawb.workbench.ui.editors.ImageEditor.ID;
	protected final static String REMOTED_IMAGE_EDITOR = org.embl.cca.dviewer.ui.editors.ImageEditor.ID;

	public MxCuBeConnectionManager(int port, IMessageHandler messageHandler) throws IOException, InterruptedException { //TODO Make this more generic argumentwise?
		super(port, messageHandler);
		addMxCuBeEventListenerTry(this);
	}

	public void dispose() {
		super.dispose(); //TODO Test if disposing FIRST does not disturb removing listener SECOND
		removeMxCuBeEventListenerTry(this);
	}

	public void addMxCuBeEventListenerTry(IMxCuBeEventListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("The listener argument can not be null");
		((MxCuBeMessageAndEventTranslator)getMessageHandler()).addMxCuBeEventListener(listener);
	}

	public void removeMxCuBeEventListenerTry(IMxCuBeEventListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("The listener argument can not be null");
		((MxCuBeMessageAndEventTranslator)getMessageHandler()).removeMxCuBeEventListener(listener);
	}

	@Override
	public void fromMxCuBeConnectionStartedEvent(
			ConnectionHandler connectionHandler) {
		try {
			connectionHandler.sendMessage(new String("Welcome to dViewer's service!\n").getBytes());
		} catch (IOException e) {
			logger.error("Can not send message to connection. " + e.getMessage());
			connectionHandler.stopServingAsynced();
		}
	}

	@Override
	public void fromMxCuBeConnectionTerminatingEvent(
			ConnectionHandler connectionHandler) {
		try {
			connectionHandler.sendMessage(new String("Bye-bye from dViewer's service!\n").getBytes());
		} catch (IOException e) {
			logger.error("Can not send message to connection. " + e.getMessage());
			connectionHandler.stopServingAsynced();
		}
	}

	@Override
	public void fromMxCuBeLoadImageEvent(ConnectionHandler connectionHandler,
			final String filePath) {
		remotedImageDisplayTracker = ExecutableManager.setRequest(new TrackableRunnable(remotedImageDisplayTracker) {
			public void runThis() {
				FilePathEditorInput fPEI = new FilePathEditorInput(filePath, ImageEditor.REMOTED_IMAGE, new File(filePath).getName());
				try {
					CommonExtension.openEditor(EclipseUtils.getPage(), fPEI, REMOTED_IMAGE_EDITOR, false, false);
				} catch (Exception e) { //PartInitException, and Exception from uk.ac.diamond.scisoft.analysis.rcp.editors.ImageEditor.createFile
					logger.error("Can not open editor. " + e.getMessage());
				}
			}
		});
		connectionHandler.stopServingAsynced();
	}

	@Override
	public void fromMxCuBeUnknownEvent(ConnectionHandler connectionHandler,
			String messageString) {
		connectionHandler.stopServingAsynced();
	}
}
