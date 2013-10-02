package org.embl.cca.utils.ui.view.fileexplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dawb.common.services.IFileIconService;
import org.dawb.common.services.ServiceManager;
import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.views.ImageMonitorView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.ViewPart;
import org.embl.cca.utils.Activator;
import org.embl.cca.utils.datahandling.FileWithTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.embl.cca.utils.ui.view.fileexplorer.preference.FileExplorerPreferenceConstants;
import uk.ac.diamond.sda.navigator.views.IFileView;
import uk.ac.gda.util.OSUtils;

/**
 * This class navigates a file system and remembers where you last left it.
 * 
 * It is lazy in loading the file tree.
 * 
 */
public class FileView extends ViewPart implements IFileView {
	public class MyTreeViewer extends TreeViewer {

		public MyTreeViewer(Composite parent, int style) {
			super(parent, style);
		}

		/**
		 * Creates a single item for the given parent and synchronizes it with the
		 * given element.
		 *
		 * @param parent
		 *            the parent widget
		 * @param element
		 *            the element
		 * @param index
		 *            if non-negative, indicates the position to insert the item
		 *            into its parent
		 */
		protected void createTreeItem2(Object element, int index) {
			createTreeItem(tree.getControl(), element, index);
		}
	}

	public static final String ID = "org.embl.cca.utils.ui.view.fileexplorer.FileView";

	private static final Logger logger = LoggerFactory
			.getLogger(FileView.class);

	public enum FileSortType {
		ALPHA_NUMERIC, ALPHA_NUMERIC_DIRS_FIRST;
	}

	private final FileSortType defaultFileSortType = FileSortType.ALPHA_NUMERIC_DIRS_FIRST;

	private MyTreeViewer tree;

	private String savedPath; // This string stores the saved path for a short
								// time until it is set in savedSelection
	private File savedSelection;
	private Text filePath;

	protected IFileviewContentProvider fileviewContentProvider = null;

	public FileView() {
		super();
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {

		super.init(site, memento);

		String path = null;
		if (memento != null)
			path = memento.getString("DIR");
		if (path == null)
			path = System
					.getProperty("org.embl.cca.utils.ui.view.fileexplorer.default.file.view.location");
		if (path == null)
			path = System.getProperty("user.home");

		savedPath = path;

	}

	@Override
	public void saveState(IMemento memento) {

		if (memento == null)
			return;
		File selectedFile = getSelectedFile();
		if (selectedFile != null) {
			final String path = selectedFile.getAbsolutePath();
			memento.putString("DIR", path);
		}
	}

	protected IFileviewContentProvider getContentProvider() {
		return new FileviewContentProvider();
	}

	/**
	 * Get the file path selected
	 * 
	 * @return String
	 */
	@Override
	public File getSelectedFile() {
		File sel = (File) ((TreeSelection) tree.getSelection())
				.getFirstElement();
		if (sel == null)
			sel = savedSelection;
		return sel;
	}

	/**
	 * Get the file paths selected
	 * 
	 * @return String[]
	 */
	public String[] getSelectedFiles() {
		Object[] objects = ((TreeSelection) tree.getSelection()).toArray();
		if (objects.length == 0)
			objects = new Object[] { savedSelection };

		String absolutePaths[] = new String[objects.length];
		for (int i = 0; i < objects.length; i++) {
			absolutePaths[i] = ((File) (objects[i])).getAbsolutePath();
		}
		return absolutePaths;
	}

	private boolean updatingTextFromTreeSelections = true;

	@Override
	public void createPartControl(final Composite parent) {
		fileviewContentProvider = getContentProvider();
		if (savedPath != null) {
			savedSelection = fileviewContentProvider
					.getFileContentProposalProvider().stringToFile(savedPath);
		}

		parent.setLayout(new GridLayout(1, false));

		final Composite top = new Composite(parent, SWT.NONE);
		top.setLayout(new GridLayout(2, false));
		top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		final Label fileLabel = new Label(top, SWT.NONE);
		fileLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
				false));
		try {
			IFileIconService service = (IFileIconService) ServiceManager
					.getService(IFileIconService.class);
			final Image icon = service
					.getIconForFile(OSUtils.isWindowsOS() ? new File(
							"C:/Windows/") : new File("/"));
			fileLabel.setImage(icon);
		} catch (Exception e) {
			logger.error("Cannot get icon for system root!", e);
		}

		this.filePath = new Text(top, SWT.BORDER);
		if (savedSelection != null)
			filePath.setText(savedSelection.getAbsolutePath());
		filePath.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final IContentProposalProvider prov = fileviewContentProvider
				.getContentProposalProvider();
		final TextContentAdapter adapter = new TextContentAdapter();
		final ContentProposalAdapter ad = new ContentProposalAdapter(filePath,
				adapter, prov, null, null);
		ad.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		ad.addContentProposalListener(new IContentProposalListener() {
			@Override
			public void proposalAccepted(IContentProposal proposal) {
				setSelectedFile(proposal.getContent(), false);
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
						IContentProposal[] proposals = fileviewContentProvider
								.getContentProposalProvider().getProposals(
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

		boolean lazyTreeImplementation = fileviewContentProvider
				.getContentProvider() instanceof ILazyTreeContentProvider;
		tree = new MyTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.BORDER
				| (lazyTreeImplementation ? SWT.VIRTUAL : SWT.NONE));
		tree.getTree().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));
		tree.getTree().setHeaderVisible(true);
		tree.setUseHashlookup(true);

		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (!updatingTextFromTreeSelections)
					return;
				final File file = getSelectedFile();
				if (file != null && file.isDirectory()) {
					try {
						ad.setEnabled(false);
						filePath.setText(fileviewContentProvider
								.getFileContentProposalProvider().fileToString(
										file));
						filePath.setSelection(filePath.getText().length());
					} finally {
						ad.setEnabled(true);
					}
				}
			}
		});

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		// we listen to the preference store property changes
		store.addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(
						FileExplorerPreferenceConstants.SHOW_DATE_COLUMN)) {
					setColumnVisible(1, (Boolean) event.getNewValue());
				} else if (event.getProperty().equals(
						FileExplorerPreferenceConstants.SHOW_TYPE_COLUMN)) {
					setColumnVisible(2, (Boolean) event.getNewValue());
				} else if (event.getProperty().equals(
						FileExplorerPreferenceConstants.SHOW_SIZE_COLUMN)) {
					setColumnVisible(3, (Boolean) event.getNewValue());
				} else if (event.getProperty().equals(
						FileExplorerPreferenceConstants.SHOW_COMMENT_COLUMN)) {
					setColumnVisible(4, (Boolean) event.getNewValue());
				} else if (event.getProperty().equals(
						FileExplorerPreferenceConstants.SHOW_SCANCMD_COLUMN)) {
					setColumnVisible(5, (Boolean) event.getNewValue());
				}
			}
		});

		TreeViewerColumn tVCol;
		IFileColumnsLabelProvider fclProv = fileviewContentProvider
				.getFileColumnsLabelProvider();
		int iSup = fclProv.getColumnAmount();
		for (int i = 0; i < iSup; i++) {
			tVCol = new TreeViewerColumn(tree, SWT.NONE);
			try {
				fclProv.setColumn(i, tVCol);
			} catch (Exception e1) {
				logger.error("Cannot set FileView's column: " + i, e1);
			}
		}
		getSite().setSelectionProvider(tree);

		createContent();

		// Make drag source, it can then drag into projects
		final DragSource dragSource = new DragSource(tree.getControl(),
				DND.DROP_MOVE | DND.DROP_DEFAULT | DND.DROP_COPY);
		dragSource.setTransfer(new Transfer[] { FileTransfer.getInstance() });
		dragSource.addDragListener(new DragSourceAdapter() {
			@Override
			public void dragSetData(DragSourceEvent event) {
				if (getSelectedFiles() == null)
					return;
				event.data = getSelectedFiles();
			}
		});

		tree.createTreeItem2(new FileWithTag("abcdefghijklmnop"), -1);

		// Add ability to open any file double clicked on (folders open in Image
		// Monitor View)
		tree.getTree().addListener(SWT.MouseDoubleClick, new Listener() {

			@Override
			public void handleEvent(Event event) {
				openSelectedFile();
			}
		});

		tree.getTree().addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == '\n' || e.character == '\r') {
					openSelectedFile();
				}
			}
		});

		createRightClickMenu();
		addToolbar();

		if (savedSelection != null) {
			final IFileContentProvider fcProv = fileviewContentProvider
					.getFileContentProvider();
			if (savedSelection.exists()) {
				tree.setSelection(new TreeSelection(fcProv
						.getTreePath(savedSelection)));
			} else {
				// File is deleted, select its existing parent from parents.
				File prevParent = savedSelection;
				File nextParent;
				while ((nextParent = prevParent.getParentFile()) != null) {
					if (nextParent.exists()) {
						tree.setSelection(new TreeSelection(fcProv
								.getTreePath(nextParent)));
						break;
					}
					prevParent = nextParent;
				}
				if (nextParent == null)
					tree.setSelection(new TreeSelection(fcProv
							.getTreePath("Root")));
			}
		}

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				TreePath path = fileviewContentProvider
						.getFileContentProvider().getTreePath(tree.getInput());
				tree.setExpandedState(path, true); // to make more parents of
													// selected savedSelection
													// visible
			}
		});

	}

	@Override
	public void collapseAll() {
		this.tree.collapseAll();
	}

	@Override
	public void showPreferences() { //TODO use own preference?
		PreferenceDialog pref = PreferencesUtil
				.createPreferenceDialogOn(
						getViewSite().getShell(),
						"org.embl.cca.utils.ui.view.fileexplorer.rcp.fileExplorerPreferencePage",
						null, null);
		if (pref != null) {
			pref.open();
		}
	}

	private void setColumnVisible(final int col, boolean isVis) {
		IFileColumnsLabelProvider fclProv = fileviewContentProvider
				.getFileColumnsLabelProvider();
		TreeColumn tCol = tree.getTree().getColumn(col);
		fclProv.setColumnVisible(col, tCol, isVis);
	}

	@Override
	public void refresh() {
		final File file = getSelectedFile();
		refresh(file);
	}

	protected void refresh(File file) {

		final Object[] elements = file == null ? this.tree
				.getExpandedElements() : null;
		final IFileContentProvider fcProv = (IFileContentProvider) tree
				.getContentProvider();
		fcProv.refresh(file);

		tree.refresh(file != null ? file.getParentFile() : tree.getInput());

		if (file != null) {
			setSelectedFile(file, true);
		} else {
			this.tree.setExpandedElements(elements);
		}
	}

	private void createContent() {
		tree.setContentProvider(fileviewContentProvider.getContentProvider());
		((IFileContentProvider) tree.getContentProvider())
				.setSort(defaultFileSortType);
		tree.setInput("Root"); // Must be string with any value (not visible)
		tree.expandToLevel(1);
	}

	public void setSelectedFile(String path, boolean updateFilePath) {
		setSelectedFile(new File(path), updateFilePath);
	}

	public void setSelectedFile(final File file, boolean updateFilePath) {
		if (file.exists()) { // TODO not necessarily required to check if
								// exists, tree can jump to last existing folder
								// part of file?
			try {
				if (!updateFilePath)
					updatingTextFromTreeSelections = false;
				TreePath tp = fileviewContentProvider.getFileContentProvider()
						.getTreePath(file);
				int iSup = tp.getSegmentCount();
				for (int i = 0; i < iSup; i++) {
					File f = (File) tp.getSegment(i);
					if (!tree.getExpandedState(f)) {
						tree.setExpandedState(f, true);
					}
				}
				tree.setSelection(new TreeSelection(tp));
			} finally {
				if (!updateFilePath)
					updatingTextFromTreeSelections = true;
			}
		}
	}

	private List<File> getSegments(File file) {
		final List<File> segs = new ArrayList<File>();
		segs.add(file);
		File par = file.getParentFile();
		while (par != null) {
			segs.add(0, par);
			par = par.getParentFile();
		}
		return segs;
	}

	private void createRightClickMenu() {
		final MenuManager menuManager = new MenuManager();
		tree.getControl().setMenu(
				menuManager.createContextMenu(tree.getControl()));
		getSite().registerContextMenu(menuManager, tree);
	}

	protected void setSort(FileSortType fst) {
		final File selection = getSelectedFile();
		TreePath treePath = fileviewContentProvider.getFileContentProvider()
				.getTreePath(selection);
		Object[] expandedElements = tree.getExpandedElements();
		((IFileContentProvider) tree.getContentProvider()).setSort(fst);
		tree.refresh();
		tree.setExpandedElements(expandedElements);
		if (selection != null) {
			tree.setSelection(new TreeSelection(treePath));
		}
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
		dirsTop.setImageDescriptor(Activator
				.getImageDescriptor("icons/alpha_mode_folder.png"));
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
		alpha.setImageDescriptor(Activator
				.getImageDescriptor("icons/alpha_mode.gif"));
		if (defaultFileSortType.equals(FileSortType.ALPHA_NUMERIC))
			alpha.setChecked(true);
		grp.add(alpha);
		toolMan.add(alpha);

//		toolMan.add(new Separator(
//				"uk.ac.diamond.sda.navigator.views.monitorSep"));

		// NO MONITORING! There are some issues with monitoring, the Images
		// Monitor part should
		// be used for this.

	}

	@Override
	public void openSelectedFile() {
		final File file = getSelectedFile();
		openFile(file);
	}

	protected void openFile(File file) {
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
						.getAbsolutePath());
			}

		} else { // Open file
			try {
				EclipseUtils.openExternalEditor(fileviewContentProvider
						.getFileContentProvider().getEditorInput(file), file
						.getAbsolutePath());
			} catch (PartInitException e) {
				logger.error("Cannot open file " + file, e);
			}
		}
	}

	@Override
	public void dispose() {
		super.dispose();

		// TODO Any other disposals?
	}

	@Override
	public void setFocus() {
		tree.getControl().setFocus();
	}

	/**
	 * The adapter IContentProvider gives the value of the H5Dataset
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(final Class clazz) {

		return super.getAdapter(clazz);

		// TODO returns an adapter part for 'IPage' which is a page summary for
		// the file or folder?
	}

}
