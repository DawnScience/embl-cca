package org.embl.cca.utils.ui.view.fileexplorer;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.dawb.common.util.io.SortingUtils;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IEditorInput;
import org.embl.cca.utils.datahandling.FileEditorInput;
import org.embl.cca.utils.datahandling.FileWithTag;
import org.embl.cca.utils.datahandling.file.FileLoader;
import org.embl.cca.utils.ui.view.fileexplorer.FileView.FileSortType;

public class FileContentProvider /*extends uk.ac.diamond.sda.navigator.views.FileContentProvider*/ implements ITreeContentProvider, IFileContentProvider/*, FileListener*/ {

	protected TreeViewer treeViewer;
//	private FileSortType sort = FileSortType.ALPHA_NUMERIC_DIRS_FIRST;
	protected Boolean singleRoot = null;

	protected FileSortType sort = FileSortType.ALPHA_NUMERIC_DIRS_FIRST;

	protected final FileWithTag[] EMPTY_FILES = new FileWithTag[0];

	public FileContentProvider() {
	}

	public boolean isSingleRoot() {
		if( singleRoot == null ) { 
			FileWithTag[] rootFiles = (FileWithTag[])FileWithTag.listRoots(FileWithTag.class);
			singleRoot = new Boolean(rootFiles.length == 1 && rootFiles[0].getAbsolutePath().equals("/"));
		}
		return singleRoot.booleanValue();
	}

//Start of implementing IContentProvider
	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		treeViewer = (TreeViewer) viewer;
		if( newInput == null )
			treeViewer.getTree().clearAll(true);
		else
			treeViewer.refresh();
	}
//End of implementing IContentProvider

//Start of implementing ILazyTreeContentProvider and ITreeContentProvider
	@Override
	public Object getParent(Object element) {
//long t1 = System.currentTimeMillis();
		if (element==null || !(element instanceof File)) {
			return null;
		}
		final File node = ((File) element);
//long t2 = System.currentTimeMillis();
//System.out.println("getParent took " + (t2-t1) + " msecs.");
		return node.getParentFile();
	}

//Stop of implementing ILazyTreeContentProvider and ITreeContentProvider
	protected File getCollectionFile(File file) {
		File result = file;
		FileLoader fl = new FileLoader();
//		try {
			fl.setFilePath(file.getAbsolutePath());
			if( fl.isCollection() )
				result = new File(fl.getCollectionAbsoluteName());
//		} catch (FileNotFoundException e) {
//		}
		return result;
	}

	protected FileWithTag[] packCollections(final FileWithTag[] filesDirs) {
		ArrayList<FileWithTag> result = new ArrayList<FileWithTag>();
		@SuppressWarnings("serial")
		Vector<FileWithTag> thisFilesDirs = new Vector<FileWithTag>(0) {{
			this.elementData = (Object[])filesDirs.clone();
			this.elementCount = this.elementData.length;
		}};
		FileLoader fl = new FileLoader() {
			FileWithTag[] thisFilesDirs = filesDirs;
			protected FileWithTag[] listFiles(FileWithTag parent, FileFilter filter) {
				return (FileWithTag[])FileWithTag.filterFiles(FileWithTag.class, thisFilesDirs, filter);
			}
		};
		while( thisFilesDirs.size() > 0 ) {
			FileWithTag file = thisFilesDirs.get(0);
//			try {
				if( file.isFile() ) {
					fl.setFilePath(file.getAbsolutePath());
					if( fl.isCollection() ) {
						result.add(fl.getCollectionDelegate());
						thisFilesDirs.removeAll(fl.getFile().getFilesFromAll());
						continue;
					}
				}
				result.add(file);
//			} catch (FileNotFoundException e) {
//			}
			thisFilesDirs.remove(0);
		}

		return result.toArray(new FileWithTag[0]);
	}

	protected FileWithTag[] getFileArray(FileWithTag node) {
		FileWithTag[] files = node.listFiles();
		if( files == null )
			return null;
		files = packCollections(files);
		List<File> sorted;
		switch(sort) {
			case ALPHA_NUMERIC:
				sorted = SortingUtils.getSortedFileList(files, SortingUtils.DEFAULT_COMPARATOR);
				break;
			case ALPHA_NUMERIC_DIRS_FIRST:
				sorted = SortingUtils.getSortedFileList(files, SortingUtils.DEFAULT_COMPARATOR_DIRS_FIRST);
				break;
			default:
				throw new RuntimeException("Invalid sort type: " + sort.name());
		}
		return sorted == null ? null : sorted.toArray(new FileWithTag[sorted.size()]);
	}

//Start of implementing ITreeContentProvider
	@Override
	public FileWithTag[] getChildren(final Object parentElement) {
//		System.out.println("DEBUG getChildren(" + parentElement + ")");
		FileWithTag[] kids = null;
		if (parentElement!=null && (parentElement instanceof FileWithTag)) {
			kids = getFileArray((FileWithTag)parentElement);
		}
		return kids == null ? EMPTY_FILES : kids;
	}

	public boolean isRootElement(Object element) {
		return element instanceof String;
	}

	@Override
	public FileWithTag[] getElements(Object inputElement) {
		if( isRootElement(inputElement) ) {
			FileWithTag[] rootFiles = (FileWithTag[])FileWithTag.listRoots(FileWithTag.class);
//			System.out.println("DEBUG getElements(" + inputElement + "), result=" + rootFiles);
			if( isSingleRoot() )
				inputElement = rootFiles[0];
			else
				return rootFiles;
		}
		return getChildren(inputElement);
	}

	@Override
	public boolean hasChildren(Object element) {
		System.out.println("DEBUG hasChildren(" + element + ")");
		return ((File)element).isDirectory(); //TODO use this faster solution, or the next?
		//This solution checks content of directories, of course much slower.
		//This behaviour is implemented in DLS version with LazyTree callbacks.
//		if( element == null || !(element instanceof FileWithTag) )
//			return false;
//		FileWithTag[] files = ((FileWithTag)element).listFiles();
//		if( files == null || files.length == 0 )
//			return false;
//		return true;
	}

//End of implementing ITreeContentProvider

//	protected List<FileWithTag> getFileList(FileWithTag node) {
//		FileWithTag[] kids = getFileArray(node);
//		if (kids==null) return null;
//		return Arrays.asList(kids);
//		
//	}

	protected List<File> getSegments(File file) {
		final List<File> segs = new ArrayList<File>();
		while( file != null ) {
			File parent = file.getParentFile();
			if( isSingleRoot() && parent == null )
				break; //Then file = root, which must not be part of segments
			segs.add(0, file);
//			System.out.println("DEBUG getSegments: file=" + file.getAbsolutePath());
			file = parent;
		}
		return segs;
	}

	@Override
	public TreePath getTreePath(File file) {
		file = getCollectionFile(file);
//		System.out.println("DEBUG getTreePath: " + file.getAbsolutePath());
		final List<File> segs = getSegments(file);
		return new TreePath(segs.toArray());
	}

	@Override
	public TreePath getTreePath(Object object) {
		if( object instanceof File)
			return getTreePath((File)object);
		else if(isRootElement(object))
			return new TreePath(new Object[]{ object });
		return null;
	}

	@Override
	public FileSortType getSort() {
		return sort;
	}

	@Override
	public void setSort(FileSortType sort) {
		this.sort = sort;
	}

	@Override
	public void refresh(File file) {
		// TODO Auto-generated method stub
	}

	@Override
	public IEditorInput getEditorInput(File file) {
		return new FileEditorInput(file.getAbsoluteFile());
	}
}
