/*
 * Copyright 2014 Diamond Light Source Ltd. and EMBL
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.embl.cca.utils.ui.view.filenavigator;

import java.io.File;
import java.nio.file.Path;

import org.dawb.common.services.IFileIconService;
import org.dawb.common.services.ServiceManager;
import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.views.ImageMonitorView;
import org.dawb.common.util.list.ListenerList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.ViewPart;
import org.embl.cca.utils.Activator;
import org.embl.cca.utils.ImageConstants;
import org.embl.cca.utils.datahandling.EFile;
import org.embl.cca.utils.datahandling.FileEditorInput;
import org.embl.cca.utils.ui.view.filenavigator.FileSystemComparator.FileSortType;
import org.embl.cca.utils.ui.widget.support.treeviewer.TreeNode.TreeNodeState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileView extends ViewPart implements IFileView, IFileSystemContentProviderListener {

	public static final String ID = "org.embl.cca.utils.ui.view.filenavigator.FileView";
	public static final String FILE_NAVIGATOR_PREFENCE_PAGE_ID = "org.embl.cca.utils.ui.view.filenavigator.preference.fileNavigatorPreferencePage";
	
	private static final Logger logger = LoggerFactory.getLogger(FileView.class);

	protected final FileSortType defaultFileSortType = FileSortType.ALPHA_NUMERIC_DIRS_FIRST;

	protected TreeViewer tree;
	protected FileSystemContentProvider fscp;
	protected FileSystemLabelProvider fslp;
	protected FileContentProposalProvider fcpp;
	protected FileSystemComparator fsc;

	protected String savedPath; // This string stores the saved path for a short
								// time until it is set in savedSelection
	protected EFile savedSelection;
	protected Text filePath;

	protected final ListenerList<IOpenFileListener> openFileListener = new ListenerList<IOpenFileListener>();

	protected boolean initialized = false;

	public FileView() {
		super();
	}

	@Override
	public void init(final IViewSite site, final IMemento memento) throws PartInitException {

		super.init(site, memento);

		String path = null;
		if (memento != null)
			path = memento.getString("DIR");
		if (path == null)
			path = System
					.getProperty("org.embl.cca.utils.ui.view.filenavigator.default.file.view.location");
		if (path == null)
			path = System.getProperty("user.home");

		savedPath = path;
	}

	@Override
	public void saveState(final IMemento memento) {
		if (memento == null)
			return;
		final File selectedFile = getSelectedFile();
		if (selectedFile != null) {
			final String path = selectedFile.getAbsolutePath();
			memento.putString("DIR", path);
		}
	}

	@Override
	public void setFocus() {
		tree.getControl().setFocus();
	}

	/**
	 * Get the file path selected.
	 * 
	 * @return String
	 */
	@Override
	public EFile getSelectedFile() { //TODO rename as getSelectedNode
		final FileSystemEntryNode sel = (FileSystemEntryNode) ((TreeSelection) tree.getSelection())
				.getFirstElement();
//		return sel == null ? savedSelection : sel.getFile();
		//Not considering savedSelection, it should work on its own
		return sel == null ? null : sel.getFile();
	}

	/**
	 * Get the file path selected. This method is used by Java 1.7 compatible
	 * methods, which probably still does not know EFile, and EFile does not
	 * know about Java 1.7, thus using this method requires attention.
	 * Where possible, use <code>getSelectedFile()</code> instead.
	 * 
	 * @return Path of the selected file.
	 */
	@Override
	public Path getSelectedPath() {
		final EFile selectedFile = getSelectedFile();
		return selectedFile == null ? null : selectedFile.toFile().toPath();
	}

	/**
	 * Get the file paths selected.
	 * 
	 * @return String[]
	 */
	public String[] getSelectedFiles() {
		Object[] objects = ((TreeSelection) tree.getSelection()).toArray();
		if (objects.length == 0)
			objects = new Object[] { savedSelection };

		final String absolutePaths[] = new String[objects.length];
		for (int i = 0; i < objects.length; i++) {
			absolutePaths[i] = ((FileSystemEntryNode) (objects[i])).getAbsolutePathString();
		}
		return absolutePaths;
	}

	private boolean updatingTextFromTreeSelections = true;

	@Override
	public void createPartControl(final Composite parent) {

		parent.setLayout(new GridLayout(1, false));

		final Composite top = new Composite(parent, SWT.NONE);
		top.setLayout(new GridLayout(2, false));
		top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		final Label fileLabel = new Label(top, SWT.NONE);
		fileLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
				false));

		filePath = new Text(top, SWT.BORDER);
		filePath.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		tree = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.BORDER);
		tree.getTree().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));
		tree.getTree().setHeaderVisible(true);
		tree.setUseHashlookup(true);

		fscp = new FileSystemContentProvider(); //It gets tree on setting input
		fscp.addTreeContentProviderListener(this);
		tree.setContentProvider(fscp);
		fslp = new FileSystemLabelProvider(tree, false);
		tree.setLabelProvider(fslp);
		fcpp = new FileContentProposalProvider();
//		tree.addFilter(new AllowOnlyFoldersFilter());
		fsc = new FileSystemComparator(tree, defaultFileSortType);
		tree.setComparator(fsc);

		if (savedPath != null) {
			savedSelection = fcpp.stringToFile(savedPath);
		}
		if (savedSelection != null)
			filePath.setText(savedSelection.getAbsolutePath());
		final TextContentAdapter adapter = new TextContentAdapter();
		final ContentProposalAdapter ad = new ContentProposalAdapter(filePath,
				adapter, fcpp, null, null);
		ad.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		ad.addContentProposalListener(new IContentProposalListener() {
			@Override
			public void proposalAccepted(IContentProposal proposal) {
				setSelectedFile(proposal.getContent(), false);
			}
		});

		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (!updatingTextFromTreeSelections)
					return;
				final File file = getSelectedFile();
				if (file != null && file.isDirectory()) {
					try {
						ad.setEnabled(false);
						filePath.setText(fcpp.fileToString(
										file));
						filePath.setSelection(filePath.getText().length());
					} finally {
						ad.setEnabled(true);
					}
				}
			}
		});

		filePath.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == '\t') {
					if (ad.isProposalPopupOpen()) {
						logger.debug("return keyPressed on filePath");
						// hacking due to lame ContentProposalAdapter, it can
						// not tell the proposals (everything is private):
						IContentProposal[] proposals = fcpp.getProposals(
										null, 0);
						if (proposals != null) { // Can not be null because
													// isProposalPopupOpen,
													// anyway to be safe
							final String path = proposals[0].getContent();
							logger.debug("getFirstPath is " + path);

							filePath.getDisplay().asyncExec(new Runnable() {
								@Override
								public void run() {
									filePath.setFocus();
									filePath.setText(path);
									setSelectedFile(path, false);
									filePath.setFocus();
									filePath.setSelection(path.length(),
											path.length());
								}
							});
						}
					}
					// This else branch is unnecessary, because if pressed
					// RETURN and filePath is valid, then the proposal will
					// accept it (see addContentProposalListener)
//				} else if (/* e.character=='\t' || */e.character == '\n'
//						|| e.character == '\r') {
//					final String path = filePath.getText();
//					setSelectedFile(path, false);
				}
			}
		});

		fslp.createColumns(logger);
		getSite().setSelectionProvider(tree);

		try {
			final IFileIconService service = (IFileIconService) ServiceManager
					.getService(IFileIconService.class);
//			final Image icon = service //TODO Check how this works on windows (probably shows the root icon wrongly, because we need folder icon
//					.getIconForFile(isWindowsOS() ? new File(
//							"C:/Windows/") : new File("/"));
//			final Image icon = service.getIconForFile(findFileWithTag.listRoots()[0]);
//			fileLabel.setImage(icon);
		} catch (Exception e) {
			logger.error("Cannot get icon for system root!", e);
		}

		// Make drag source, it can then drag into projects
		final DragSource dragSource = new DragSource(tree.getControl(),
				DND.DROP_MOVE | DND.DROP_DEFAULT | DND.DROP_COPY);
		dragSource.setTransfer(new Transfer[] { FileTransfer.getInstance() });
		dragSource.addDragListener(new DragSourceAdapter() {
			@Override
			public void dragSetData(final DragSourceEvent event) {
				if (getSelectedFiles() == null)
					return;
				event.data = getSelectedFiles();
			}
		});

		// Add ability to open any file double clicked on (folders open in Image
		// Monitor View)
		tree.getTree().addListener(SWT.MouseDoubleClick, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				openSelectedFile();
			}
		});

		tree.getTree().addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(final KeyEvent e) {
			}
			@Override
			public void keyPressed(final KeyEvent e) {
				if (e.character == '\n' || e.character == '\r') {
					openSelectedFile();
				}
			}
		});

		tree.setInput(fscp.createSuperRootNode());

		createRightClickMenu();
		addToolbar();
	}

	@Override
	public void nodeReady(final EFile identifier, final TreeNodeState result) {
		try {
			if( !initialized && savedSelection != null) {
				if (savedSelection.exists()) {
					System.out.println("@-_-@ " + savedSelection.getAbsolutePath());
					fscp.setSelection(savedSelection);
				} else {
					// File is deleted, select its existing parent from parents.
					EFile prevParent = savedSelection;
					EFile nextParent;
					while ((nextParent = prevParent.getParentFile()) != null) {
						if (nextParent.exists()) {
							fscp.setSelection(nextParent);
							break;
						}
						prevParent = nextParent;
					}
					if (nextParent == null) //Nothing exists from path, clear selection
						fscp.setSelection(null);
				}
			} else if( fscp.isSuperRootValue(identifier) && tree.getTree().getItems().length == 1 )
				tree.expandToLevel(2);
		} finally {
			if( !initialized )
				initialized = true;
		}
	}

	@Override
	public void collapseAll() {
		tree.collapseAll();
	}

	@Override
	public void showPreferences() {
		PreferenceDialog pref = PreferencesUtil
				.createPreferenceDialogOn(
						getViewSite().getShell(),
						FILE_NAVIGATOR_PREFENCE_PAGE_ID,
						null, null);
		if (pref != null) {
			pref.open();
		}
	}

	public void refresh() {
		final EFile file = getSelectedFile();
		refresh(file);
	}

	protected void refresh(EFile file) {
		//Refreshing is done async, so restoring anything here is hopeless.
		//If need to restore something, that should be implemented in fscp.refresh.
		fscp.refresh(file);
	}

	public void setSelectedFile(final String path, final boolean updateFilePath) {
		setSelectedFile(new EFile(path), updateFilePath);
	}

	public void setSelectedFile(final EFile file, boolean updateFilePath) {
		try {
			if (!updateFilePath)
				updatingTextFromTreeSelections = false;
			fscp.setSelection(file);
		} finally {
			if (!updateFilePath)
				updatingTextFromTreeSelections = true;
		}
	}

	protected void setSort(final FileSortType fst) {
		fsc.setSort(fst);
	}

	protected void createRightClickMenu() {
		final MenuManager menuManager = new MenuManager();
		tree.getControl().setMenu(
				menuManager.createContextMenu(tree.getControl()));
		getSite().registerContextMenu(menuManager, tree);
	}

	/**
	 * Never really figured out how to made toggle buttons work properly with
	 * contributions. Use hard coded actions
	 * 
	 * TODO Move this to contributions
	 */
	private void addToolbar() {

		// TODO Save preference as property

		final IToolBarManager toolMan = getViewSite().getActionBars()
				.getToolBarManager();

		final CheckableActionGroup grp = new CheckableActionGroup();

		final Action dirsTop = new Action(
				"Sort alphanumeric, directories at top.", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				setSort(FileSortType.ALPHA_NUMERIC_DIRS_FIRST);
			}
		};
		dirsTop.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(ImageConstants.IMG_ALPHA_MODE_FOLDER));
		if (defaultFileSortType.equals(FileSortType.ALPHA_NUMERIC_DIRS_FIRST))
			dirsTop.setChecked(true);
		grp.add(dirsTop);
		toolMan.add(dirsTop);

		final Action alpha = new Action("Alphanumeric sort for everything.",
				IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				setSort(FileSortType.ALPHA_NUMERIC);
			}
		};
		alpha.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(ImageConstants.IMG_ALPHA_MODE));
		if (defaultFileSortType.equals(FileSortType.ALPHA_NUMERIC))
			alpha.setChecked(true);
		grp.add(alpha);
		toolMan.add(alpha);
	}

	public IEditorInput getEditorInput(final File file) {
		return new FileEditorInput(file.getAbsoluteFile());
	}

	public void addOpenFileListener(final IOpenFileListener listener) {
		openFileListener.add(listener);
	}

	public void removeOpenFileListener(final IOpenFileListener listener) {
		openFileListener.remove(listener);
	}

	public void openSelectedFile() {
		final EFile file = getSelectedFile();
		openFile(file);
	}

	protected void openFile(final EFile file) {
		if (file == null)
			return;

		if (file.isDirectory()) {
			final IWorkbenchPage page = EclipseUtils.getActivePage();
			if (page == null)
				return;

			IViewPart part = null;
			try {
				part = page
						.showView("org.dawb.workbench.views.imageMonitorView");
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				logger.error("TODO put description of error here", e);
				return;
			}
			if (part != null && part instanceof ImageMonitorView) {
				((ImageMonitorView) part).setDirectoryPath(file
						.getAbsolutePathWithoutProtocol());
			}

		} else { // Open file
			for(final IOpenFileListener e : openFileListener) {
				System.out.println("*** FileView: openFile: custom! ***");
				if( e.openFile(file) )
					return;
			}
			try {
				EclipseUtils.openExternalEditor(getEditorInput(file), file
						.getAbsolutePath());
			} catch (final PartInitException e) {
				logger.error("Cannot open file " + file, e);
			}
		}
	}

	@Override
	public void dispose() {
		openFileListener.removeAllElements();
		super.dispose();
	}

}
