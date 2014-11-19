package org.embl.cca.dviewer;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

import org.dawnsci.plotting.system.PlottingSystemActivator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dawnsci.plotting.api.preferences.PlottingConstants;
import org.eclipse.dawnsci.plotting.api.preferences.ToolbarConfigurationConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.services.IServiceLocator;
import org.embl.cca.dviewer.rcp.perspectives.DViewerPerspective;
import org.embl.cca.dviewer.server.MxCuBeConnectionManager;
import org.embl.cca.dviewer.ui.editors.DViewerImageArrayEditorPart;
import org.embl.cca.dviewer.ui.editors.preference.EditorPreferenceInitializer;
import org.embl.cca.utils.datahandling.FilePathEditorInput;
import org.embl.cca.utils.datahandling.JavaSystem;
import org.embl.cca.utils.datahandling.text.StringUtils;
import org.embl.cca.utils.extension.CommonExtension;
import org.embl.cca.utils.extension.EclipseBug362561Workaround;
import org.embl.cca.utils.extension.FirstPageCreatedPollingNotifier;
import org.embl.cca.utils.extension.IFirstPageCreatedListener;
import org.embl.cca.utils.server.MxCuBeMessageAndEventTranslator;
import org.embl.cca.utils.threading.CommonThreading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DViewerStartup implements IStartup {

	protected final static Logger logger = LoggerFactory.getLogger(DViewerStartup.class);

	public final static String dViewerEnabledJavaProperty = "dViewer";
	public final static String dViewerImagePortProperty = "dViewerImagePort";
	public final static String dViewerLogSettingsProperty = "dViewerLogSettings";

	protected final static HashSet<String> requiredMenus = new HashSet<String>(Arrays.asList(new String[] {"file", "edit", "window", "help"}));
	protected final IAction resetPreferencesAction = new Action("Reset Preferences") {
		public void run() {
			resetPreferences();
		}
	};
	protected final AbstractContributionFactory dViewerContribution = new AbstractContributionFactory(
			"menu:window?after=" + StringUtils.getLastOfSplitString(
				IWorkbenchCommandConstants.WINDOW_PREFERENCES, "\\."), DViewerActivator.PLUGIN_ID) {
		@Override
		public void createContributionItems(final IServiceLocator serviceLocator,
				final IContributionRoot additions) {
			additions.addContributionItem(new ActionContributionItem(resetPreferencesAction), null);
		}
	};

	protected final IPerspectiveListener perspectiveListener = new IPerspectiveListener() {
		@Override
		public void perspectiveChanged(final IWorkbenchPage page,
				final IPerspectiveDescriptor perspective, final String changeId) {
		}
		@Override
		public void perspectiveActivated(final IWorkbenchPage page,
				final IPerspectiveDescriptor perspective) {
			final boolean hide = perspective != null && DViewerPerspective.PERSPECTIVE_ID.equals(perspective.getId());
			final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window instanceof ApplicationWindow) {
				final MenuManager menuManager = ((ApplicationWindow) window).getMenuBarManager();
				for( final IContributionItem menuItem : menuManager.getItems() ) {
					final String menu = menuItem.getId();
					if( requiredMenus.contains(menu) )
						continue; //Do not touch menus which we use
					menuItem.setVisible(!hide);
				}
				menuManager.updateAll(true);
			}
			final IMenuService menuService = (IMenuService)PlatformUI.getWorkbench().getService(IMenuService.class);
			if( hide )
				menuService.addContributionFactory(dViewerContribution);
			else
				menuService.removeContributionFactory(dViewerContribution);
		}
	};

	protected final IFirstPageCreatedListener checkCommandLineArguments = new IFirstPageCreatedListener() {
		@Override
		public void firstPageCreated(final IWorkbenchPage page) {
			final String[] args = Platform.getCommandLineArgs();
			for (int i = 0; i < args.length; i++) {
//				if (args[i].equals("-mydir")) {
//					i++;
//					try {
//						mydir = new File(args[i]);
//						if (!mydir.exists() || !mydir.isDirectory()) {
//							mydir = null;
//						}
//					} catch (Exception e) {
//						mydir = null;
//					}
//				}
				if (args[i].equals("-openFile")) {
					i++;
					final File file = new File(args[i]);
					if( file.exists() ) {
						final FilePathEditorInput fPEI = new FilePathEditorInput(file.getAbsolutePath(), null, file.getName());
						try {
							CommonExtension.openEditor(fPEI, DViewerImageArrayEditorPart.ID, false, true);
						} catch (final PartInitException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

	};

	public void resetPreferences() {
		EditorPreferenceInitializer.DownsamplingType.resetValue();
		EditorPreferenceInitializer.ApplyPha.resetValue();
		EditorPreferenceInitializer.PhaRadius.resetValue();
	}

	protected String getPackageName() {
		return getClass().getName().split("\\." + getClass().getSimpleName())[0];
	}

	public class StringAndObject { //TODO could be generic in utils
		public IPreferenceStore preferenceStore;
		public String string;
		public Object object;
		StringAndObject(final IPreferenceStore preferenceStore, final String string, final Object object) {
			this.preferenceStore = preferenceStore;
			this.string = string;
			this.object = object;
		}
	}

	protected void setToDefault(final StringAndObject propertyDefault) {
		setToDefault(propertyDefault.preferenceStore, propertyDefault.string, propertyDefault.object);
	}

	protected void setToDefault(final IPreferenceStore preferenceStore, final String name, final Object value) {
		//Since setDefault does not fire event, it must be set AFTER the value
		if( value instanceof String ) {
			preferenceStore.setValue(name, (String)value); //To fire event
			preferenceStore.setDefault(name, (String)value); //No event from this, odd concept
		} else if( value instanceof Boolean ) {
			preferenceStore.setValue(name, (Boolean)value); //To fire event
			preferenceStore.setDefault(name, (Boolean)value); //No event from this, odd concept
		} else if( value instanceof Integer ) {
			preferenceStore.setValue(name, (Integer)value); //To fire event
			preferenceStore.setDefault(name, (Integer)value); //No event from this, odd concept
		} else if( value instanceof Long ) {
			preferenceStore.setValue(name, (Long)value); //To fire event
			preferenceStore.setDefault(name, (Long)value); //No event from this, odd concept
		} else if( value instanceof Float ) {
			preferenceStore.setValue(name, (Float)value); //To fire event
			preferenceStore.setDefault(name, (Float)value); //No event from this, odd concept
		} else if( value instanceof Double ) {
			preferenceStore.setValue(name, (Double)value); //To fire event
			preferenceStore.setDefault(name, (Double)value); //No event from this, odd concept
		}
	}

	protected void setDefaultPreferences() {
		final String defaultColourScheme = "Film Negative";
		final String defaultPollServerDirectory = StringUtils.EMPTY_STRING;
		//getLocalPreferenceStore for Toolbar*
//		final IPreferenceStore dviewerPS = DViewerActivator.getLocalPreferenceStore();
		final IPreferenceStore plottingPS = PlottingSystemActivator.getPlottingPreferenceStore();
		//getLocalPreferenceStore for Toolbar*
		final IPreferenceStore localPS = PlottingSystemActivator.getLocalPreferenceStore();
		 /* getAnalysisRCPPreferenceStore for PlottingConstants.PLOT_VIEW_PLOT2D_COLOURMAP,
		  * probably used outside of DAWN.
		  */
		final IPreferenceStore rpcPS = PlottingSystemActivator.getAnalysisRCPPreferenceStore();
		final StringAndObject[] propertyDefaults = {
			new StringAndObject(localPS, ToolbarConfigurationConstants.CONFIG.getId(), false),
			new StringAndObject(localPS, ToolbarConfigurationConstants.ANNOTATION.getId(), false),
			new StringAndObject(localPS, ToolbarConfigurationConstants.UNDO.getId(), false),
			new StringAndObject(localPS, ToolbarConfigurationConstants.ASPECT.getId(), false),
			new StringAndObject(localPS, ToolbarConfigurationConstants.EXPORT.getId(), false),
			new StringAndObject(plottingPS, PlottingConstants.COLOUR_SCHEME, defaultColourScheme),
			//No need poll server, the best we can do is setting directory to empty
			new StringAndObject(new ScopedPreferenceStore(InstanceScope.INSTANCE, "uk.ac.diamond.sda.polling"), "pathPreference", defaultPollServerDirectory), //TODO
//			new StringAndObject(dviewerPS, DViewerEditorConstants.PREFERENCE_DOWNSAMPLING_TYPE, EditorPreferenceHelper.getDefaultValue(DViewerEditorConstants.PREFERENCE_DOWNSAMPLING_TYPE)),
//			new StringAndObject(dviewerPS, DViewerEditorConstants.PREFERENCE_APPLY_PHA, EditorPreferenceHelper.getDefaultValue(DViewerEditorConstants.PREFERENCE_APPLY_PHA)),
//			new StringAndObject(dviewerPS, DViewerEditorConstants.PREFERENCE_PHA_RADIUS, EditorPreferenceHelper.getDefaultValue(DViewerEditorConstants.PREFERENCE_PHA_RADIUS)),
			};
		CommonThreading.execUISynced(new Runnable() {
			@Override
			public void run() {
				for( final StringAndObject propertyDefault : propertyDefaults ) {
					setToDefault(propertyDefault);
				}
			}
		});
	}


	/**
	 * Starts the connection manager.
	 */
	protected void startConnectionManager() {
		final DViewerActivator dViewerActivator = DViewerActivator.getDefault();
		if( dViewerActivator.getConnectionManager() != null )
			return;
		final String portString = JavaSystem.getProperty(dViewerImagePortProperty);
		if( portString != null) { //port is specified
			final String msgEnd = "for listening to MxCuBE by " + getPackageName() + " package.";
			int port = DViewerActivator.getDefaultPort();
			try {
				port = JavaSystem.getPropertyAsInt(dViewerImagePortProperty);
			} catch( final NumberFormatException e ) {
				CommonThreading.execUISynced(new Runnable() {
					@Override
					public void run() {
						MessageDialog.openWarning(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "dViewer startup warning", "The specified port " + portString + " is not valid\n" + msgEnd + "\n" + e.getMessage());
					}
				});
			}
			try {
				if( port > 0 ) {
					logger.info("Opening port " + port + " " + msgEnd);
					dViewerActivator.setConnectionManager(new MxCuBeConnectionManager(port, new MxCuBeMessageAndEventTranslator()));
				}
			} catch (final Exception e) {
				final int finalPort = port;
				CommonThreading.execUISynced(new Runnable() {
					@Override
					public void run() {
						MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "dViewer startup error", "Can not start socket listener on port " + finalPort + "\n" + msgEnd + "\n" + e.getMessage());
					}
				});
			}
		}
	}

	/**
	 * Stops the connection manager.
	 */
	protected void stopConnectionManager() {
		final DViewerActivator dViewerActivator = DViewerActivator.getDefault();
		if( dViewerActivator.getConnectionManager() != null ) {
			dViewerActivator.getConnectionManager().dispose();
			dViewerActivator.setConnectionManager(null);
		}
	}

	public void addPerspectiveSupport() {
		CommonThreading.execUISynced(new Runnable() {
			@Override
			public void run() {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(perspectiveListener);
				/* The initial perspective activation is missed, because can not
				   find a time and place for adding perspective listener after
				   workbench is created and before this event happens.
				   True, the init of editor would be the place, but why an
				   editor would contain a perspective support related code?
				   Other place: Application when creating workbench, the
				   workbench advisor can be specified. We do not create
				   workbench, only using it.
				   The consequence: we call the event handler manually.
				 */
				perspectiveListener.perspectiveActivated(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective());
			}
		});
	}

	public void removePerspectiveSupport() {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().removePerspectiveListener(perspectiveListener);
	}

	@Override
	public void earlyStartup() {
		if( JavaSystem.getPropertyAsBoolean(dViewerEnabledJavaProperty) ) {
			//TODO Add optional config somehow. Creating own logger is not accepted. How I could specify my own logger config?
			//final String logSettingsRelPath = JavaSystem.getProperty(dViewerLogSettingsProperty);
			logger.debug("Starting " + getPackageName());
	
			PlatformUI.getWorkbench().addWorkbenchListener( new IWorkbenchListener() {
				public boolean preShutdown( final IWorkbench workbench, final boolean forced ) {
					stopConnectionManager();
					removePerspectiveSupport();
					return true;
				}
				public void postShutdown( final IWorkbench workbench ) {
				}
			});

			final IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
			final IFileEditorMapping[] allEditors = editorRegistry.getFileEditorMappings();
			for( final IFileEditorMapping editor : allEditors ) {
				final IEditorDescriptor thisEditors[] = editor.getEditors();
				for( final IEditorDescriptor thisEditor : thisEditors ) {
					if( thisEditor.getId().equals(DViewerImageArrayEditorPart.ID)) { //If dViewer Editor is registered for this extension
						if( !editor.getDefaultEditor().getId().equals(DViewerImageArrayEditorPart.ID)) { //and it is not default editor
							editorRegistry.setDefaultEditor(editor.getLabel(), DViewerImageArrayEditorPart.ID);
						}
						break;
					}
				}
			}
			addPerspectiveSupport();
			setDefaultPreferences();
			startConnectionManager();
			new FirstPageCreatedPollingNotifier(checkCommandLineArguments, 100);
			new EclipseBug362561Workaround();
		}
		logger.debug("Started " + getPackageName());
	}

}
