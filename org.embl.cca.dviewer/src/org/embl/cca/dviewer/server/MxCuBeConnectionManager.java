package org.embl.cca.dviewer.server;

import java.io.File;
import java.io.IOException;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.core.runtime.Assert;
import org.embl.cca.dviewer.ui.editors.DViewerImageArrayEditorPart;
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
	protected final static String REMOTED_IMAGE_EDITOR = org.embl.cca.dviewer.ui.editors.DViewerImageArrayEditorPart.ID;

	public MxCuBeConnectionManager(final int port, final IMessageHandler messageHandler) throws IOException, InterruptedException { //TODO Make this more generic argumentwise?
		super(port, messageHandler);
		addMxCuBeEventListenerTry(this);
	}

	public void dispose() {
		super.dispose(); //TODO Test if disposing FIRST does not disturb removing listener SECOND
		removeMxCuBeEventListenerTry(this);
	}

	public void addMxCuBeEventListenerTry(final IMxCuBeEventListener listener) {
		Assert.isNotNull(listener, "The listener is null, it must not be null");
		((MxCuBeMessageAndEventTranslator)getMessageHandler()).addMxCuBeEventListener(listener);
	}

	public void removeMxCuBeEventListenerTry(final IMxCuBeEventListener listener) {
		Assert.isNotNull(listener, "The listener is null, it must not be null");
		((MxCuBeMessageAndEventTranslator)getMessageHandler()).removeMxCuBeEventListener(listener);
	}

	@Override
	public void fromMxCuBeConnectionStartedEvent(
			final ConnectionHandler connectionHandler) {
		try {
			connectionHandler.sendMessage(new String("Welcome to dViewer's service!\n").getBytes());
		} catch (IOException e) {
			logger.error("Can not send message to connection. " + e.getMessage());
			connectionHandler.stopServingAsynced();
		}
	}

	@Override
	public void fromMxCuBeConnectionTerminatingEvent(
			final ConnectionHandler connectionHandler) {
		try {
			connectionHandler.sendMessage(new String("Bye-bye from dViewer's service!\n").getBytes());
		} catch (IOException e) {
			logger.error("Can not send message to connection. " + e.getMessage());
			connectionHandler.stopServingAsynced();
		}
	}

	@Override
	public void fromMxCuBeLoadImageEvent(final ConnectionHandler connectionHandler,
			final String filePath) {
		remotedImageDisplayTracker = ExecutableManager.setRequest(new TrackableRunnable(remotedImageDisplayTracker) {
			public void runThis() {
				final FilePathEditorInput fPEI = new FilePathEditorInput(filePath, DViewerImageArrayEditorPart.REMOTED_IMAGE, new File(filePath).getName());
				try {
					CommonExtension.openEditor(fPEI, REMOTED_IMAGE_EDITOR, false, true);
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
