package org.embl.cca.utils.datahandling;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.net.URI;

import org.eclipse.core.runtime.IPath;

public class FileWithTag extends EFile {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2736494303942920443L;

//	public static final IFileFactory<FileWithTag> FileWithTagFactory = new IFileFactory<FileWithTag>() {
//		@Override
//		public FileWithTag create() {
//			return new FileWithTag();
//		}
//		@Override
//		public FileWithTag[] createMany(int n) {
//			return new FileWithTag[n];
//		}
//	};
//	public static final FileWithTag FileWithTagFactory = new FileWithTag();

	protected Object tag;

//	@Override
//	protected static FileWithTag create() {
//		return new FileWithTag();
//	}
//
//	@Override
//	protected static FileWithTag[] createMany(int n) {
//		return new FileWithTag[n];
//	}

	/* -- Constructors -- */

	public FileWithTag(File file) {
		super(file);
	}

	public FileWithTag(EFile file) {
		this((File)file);
	}

	public FileWithTag(FileWithTag file) {
		this((EFile)file);
		tag = file.tag;
	}

//	/**
//	 * Originally: Internal constructor for already-normalized pathname strings.
//	 * Currently: This constructor is same as
//	 * <tt>{@link #FileWithTag(String pathname)}
//	 * </tt> because can not do what the original private constructor, thus it
//	 * is kept for compatibility reason.
//	 */
//	protected FileWithTag(String pathname, int prefixLength) {
//		super(pathname, prefixLength);
//	}

	/**
	 * Creates a new <code>FileWithTag</code> instance by converting the given
	 * pathname string into an abstract pathname. If the given string is the
	 * empty string, then the result is the empty abstract pathname.
	 * 
	 * @param pathname
	 *            A pathname string
	 * @throws NullPointerException
	 *             If the <code>pathname</code> argument is <code>null</code>
	 */
	public FileWithTag(String pathname) {
		super(pathname);
	}

	/**
	 * Creates a new <code>FileWithTag</code> instance by converting the given
	 * pathname string into an abstract pathname. If the given string is the
	 * empty string, then the result is the empty abstract pathname.
	 * 
	 * @param pathname
	 *            A pathname string
	 * @param dummy
	 *            Not used
	 * @param tag
	 *            Caller defined object
	 * @throws NullPointerException
	 *             If the <code>pathname</code> argument is <code>null</code>
	 */
	public FileWithTag(String pathname, boolean dummy, Object tag) {
		this(pathname);
		this.tag = tag;
	}

	/*
	 * Note: The two-argument FileWithTag constructors do not interpret an empty
	 * parent abstract pathname as the current user directory. An empty parent
	 * instead causes the child to be resolved against the system-dependent
	 * directory defined by the FileSystem.getDefaultParent method. On Unix this
	 * default is "/", while on Microsoft Windows it is "\\". This is required
	 * for compatibility with the original behavior of this class.
	 */

	/**
	 * Creates a new <code>FileWithTag</code> instance from a parent pathname
	 * string and a child pathname string.
	 * 
	 * <p>
	 * If <code>parent</code> is <code>null</code> then the new
	 * <code>FileWithTag</code> instance is created as if by invoking the
	 * single-argument <code>FileWithTag</code> constructor on the given
	 * <code>child</code> pathname string.
	 * 
	 * <p>
	 * Otherwise the <code>parent</code> pathname string is taken to denote a
	 * directory, and the <code>child</code> pathname string is taken to denote
	 * either a directory or a file. If the <code>child</code> pathname string
	 * is absolute then it is converted into a relative pathname in a
	 * system-dependent way. If <code>parent</code> is the empty string then the
	 * new <code>FileWithTag</code> instance is created by converting
	 * <code>child</code> into an abstract pathname and resolving the result
	 * against a system-dependent default directory. Otherwise each pathname
	 * string is converted into an abstract pathname and the child abstract
	 * pathname is resolved against the parent.
	 * 
	 * @param parent
	 *            The parent pathname string
	 * @param child
	 *            The child pathname string
	 * @throws NullPointerException
	 *             If <code>child</code> is <code>null</code>
	 */
	public FileWithTag(String parent, String child) {
		super(parent, child);
	}

	/**
	 * Creates a new <code>FileWithTag</code> instance from a parent pathname
	 * string and a child pathname string.
	 * 
	 * <p>
	 * If <code>parent</code> is <code>null</code> then the new
	 * <code>FileWithTag</code> instance is created as if by invoking the
	 * single-argument <code>FileWithTag</code> constructor on the given
	 * <code>child</code> pathname string.
	 * 
	 * <p>
	 * Otherwise the <code>parent</code> pathname string is taken to denote a
	 * directory, and the <code>child</code> pathname string is taken to denote
	 * either a directory or a file. If the <code>child</code> pathname string
	 * is absolute then it is converted into a relative pathname in a
	 * system-dependent way. If <code>parent</code> is the empty string then the
	 * new <code>FileWithTag</code> instance is created by converting
	 * <code>child</code> into an abstract pathname and resolving the result
	 * against a system-dependent default directory. Otherwise each pathname
	 * string is converted into an abstract pathname and the child abstract
	 * pathname is resolved against the parent.
	 * 
	 * @param parent
	 *            The parent pathname string
	 * @param child
	 *            The child pathname string
	 * @param tag
	 *            Caller defined object
	 * @throws NullPointerException
	 *             If <code>child</code> is <code>null</code>
	 */
	public FileWithTag(String parent, String child, Object tag) {
		this(parent, child);
		this.tag = tag;
	}

	/**
	 * Creates a new <code>FileWithTag</code> instance from a parent abstract
	 * pathname and a child pathname string.
	 * 
	 * <p>
	 * If <code>parent</code> is <code>null</code> then the new
	 * <code>FileWithTag</code> instance is created as if by invoking the
	 * single-argument <code>FileWithTag</code> constructor on the given
	 * <code>child</code> pathname string.
	 * 
	 * <p>
	 * Otherwise the <code>parent</code> abstract pathname is taken to denote a
	 * directory, and the <code>child</code> pathname string is taken to denote
	 * either a directory or a file. If the <code>child</code> pathname string
	 * is absolute then it is converted into a relative pathname in a
	 * system-dependent way. If <code>parent</code> is the empty abstract
	 * pathname then the new <code>FileWithTag</code> instance is created by
	 * converting <code>child</code> into an abstract pathname and resolving the
	 * result against a system-dependent default directory. Otherwise each
	 * pathname string is converted into an abstract pathname and the child
	 * abstract pathname is resolved against the parent.
	 * 
	 * @param parent
	 *            The parent abstract pathname
	 * @param child
	 *            The child pathname string
	 * @throws NullPointerException
	 *             If <code>child</code> is <code>null</code>
	 */
	public FileWithTag(FileWithTag parent, String child) {
		super(parent, child);
	}

	/**
	 * Creates a new <code>FileWithTag</code> instance from a parent abstract
	 * pathname and a child pathname string.
	 * 
	 * <p>
	 * If <code>parent</code> is <code>null</code> then the new
	 * <code>FileWithTag</code> instance is created as if by invoking the
	 * single-argument <code>FileWithTag</code> constructor on the given
	 * <code>child</code> pathname string.
	 * 
	 * <p>
	 * Otherwise the <code>parent</code> abstract pathname is taken to denote a
	 * directory, and the <code>child</code> pathname string is taken to denote
	 * either a directory or a file. If the <code>child</code> pathname string
	 * is absolute then it is converted into a relative pathname in a
	 * system-dependent way. If <code>parent</code> is the empty abstract
	 * pathname then the new <code>FileWithTag</code> instance is created by
	 * converting <code>child</code> into an abstract pathname and resolving the
	 * result against a system-dependent default directory. Otherwise each
	 * pathname string is converted into an abstract pathname and the child
	 * abstract pathname is resolved against the parent.
	 * 
	 * @param parent
	 *            The parent abstract pathname
	 * @param child
	 *            The child pathname string
	 * @param tag
	 *            Caller defined object
	 * @throws NullPointerException
	 *             If <code>child</code> is <code>null</code>
	 */
	public FileWithTag(FileWithTag parent, String child, Object tag) {
		this(parent, child);
		this.tag = tag;
	}

	/**
	 * Creates a new <tt>FileWithTag</tt> instance by converting the given
	 * <tt>file:</tt> URI into an abstract pathname.
	 * 
	 * <p>
	 * The exact form of a <tt>file:</tt> URI is system-dependent, hence the
	 * transformation performed by this constructor is also system-dependent.
	 * 
	 * <p>
	 * For a given abstract pathname <i>f</i> it is guaranteed that
	 * 
	 * <blockquote><tt>
	 * new FileWithTag(</tt><i>&nbsp;f</i>
	 * <tt>.{@link #toURI() toURI}()).equals(</tt><i>&nbsp;f</i>
	 * <tt>.{@link #getAbsoluteFile() getAbsoluteFile}())
	 * </tt></blockquote>
	 * 
	 * so long as the original abstract pathname, the URI, and the new abstract
	 * pathname are all created in (possibly different invocations of) the same
	 * Java virtual machine. This relationship typically does not hold, however,
	 * when a <tt>file:</tt> URI that is created in a virtual machine on one
	 * operating system is converted into an abstract pathname in a virtual
	 * machine on a different operating system.
	 * 
	 * @param uri
	 *            An absolute, hierarchical URI with a scheme equal to
	 *            <tt>"file"</tt>, a non-empty path component, and undefined
	 *            authority, query, and fragment components
	 * 
	 * @throws NullPointerException
	 *             If <tt>uri</tt> is <tt>null</tt>
	 * 
	 * @throws IllegalArgumentException
	 *             If the preconditions on the parameter do not hold
	 * 
	 * @see #toURI()
	 * @see java.net.URI
	 * @since 1.4
	 */
	public FileWithTag(URI uri) {
		super(uri);
	}

	/**
	 * Creates a new <tt>FileWithTag</tt> instance by converting the given
	 * <tt>file:</tt> URI into an abstract pathname.
	 * 
	 * <p>
	 * The exact form of a <tt>file:</tt> URI is system-dependent, hence the
	 * transformation performed by this constructor is also system-dependent.
	 * 
	 * <p>
	 * For a given abstract pathname <i>f</i> it is guaranteed that
	 * 
	 * <blockquote><tt>
	 * new FileWithTag(</tt><i>&nbsp;f</i>
	 * <tt>.{@link #toURI() toURI}()).equals(</tt><i>&nbsp;f</i>
	 * <tt>.{@link #getAbsoluteFile() getAbsoluteFile}())
	 * </tt></blockquote>
	 * 
	 * so long as the original abstract pathname, the URI, and the new abstract
	 * pathname are all created in (possibly different invocations of) the same
	 * Java virtual machine. This relationship typically does not hold, however,
	 * when a <tt>file:</tt> URI that is created in a virtual machine on one
	 * operating system is converted into an abstract pathname in a virtual
	 * machine on a different operating system.
	 * 
	 * @param uri
	 *            An absolute, hierarchical URI with a scheme equal to
	 *            <tt>"file"</tt>, a non-empty path component, and undefined
	 *            authority, query, and fragment components
	 * @param tag
	 *            Caller defined object
	 * 
	 * @throws NullPointerException
	 *             If <tt>uri</tt> is <tt>null</tt>
	 * 
	 * @throws IllegalArgumentException
	 *             If the preconditions on the parameter do not hold
	 * 
	 * @see #toURI()
	 * @see java.net.URI
	 * @since 1.4
	 */
	public FileWithTag(URI uri, Object tag) {
		this(uri);
		this.tag = tag;
	}

	/**
	 * Creates a new <code>FileWithTag</code> instance by converting the given
	 * pathname IPath into an abstract pathname. If the given IPath is the
	 * empty IPath, then the result is the empty abstract pathname.
	 * 
	 * @param pathname
	 *            A pathname IPath
	 * @throws NullPointerException
	 *             If the <code>pathname</code> argument is <code>null</code>
	 */
	public FileWithTag(IPath pathname) {
		super(pathname.toOSString());
	}

	/**
	 * Creates a new <code>FileWithTag</code> instance by converting the given
	 * pathname IPath into an abstract pathname. If the given IPath is the
	 * empty IPath, then the result is the empty abstract pathname.
	 * 
	 * @param pathname
	 *            A pathname IPath
	 * @param tag
	 *            Caller defined object
	 * @throws NullPointerException
	 *             If the <code>pathname</code> argument is <code>null</code>
	 */
	public FileWithTag(IPath pathname, Object tag) {
		this(pathname);
		this.tag = tag;
	}

	/**
	 * Returns an array of abstract pathnames denoting the files in the
	 * directory denoted by this abstract pathname.
	 * 
	 * <p>
	 * If this abstract pathname does not denote a directory, then this method
	 * returns <code>null</code>. Otherwise an array of <code>File</code>
	 * objects is returned, one for each file or directory in the directory.
	 * Pathnames denoting the directory itself and the directory's parent
	 * directory are not included in the result. Each resulting abstract
	 * pathname is constructed from this abstract pathname using the
	 * <code>{@link #File(java.io.File, java.lang.String)
	 * File(File,&nbsp;String)}</code> constructor. Therefore if this pathname
	 * is absolute then each resulting pathname is absolute; if this pathname is
	 * relative then each resulting pathname will be relative to the same
	 * directory.
	 * 
	 * <p>
	 * There is no guarantee that the name strings in the resulting array will
	 * appear in any specific order; they are not, in particular, guaranteed to
	 * appear in alphabetical order.
	 * 
	 * @return An array of abstract pathnames denoting the files and directories
	 *         in the directory denoted by this abstract pathname. The array
	 *         will be empty if the directory is empty. Returns
	 *         <code>null</code> if this abstract pathname does not denote a
	 *         directory, or if an I/O error occurs.
	 * 
	 * @throws SecurityException
	 *             If a security manager exists and its <code>
	 *             {@link java.lang.SecurityManager#checkRead(java.lang.String)}
	 *             </code> method denies read access to the directory
	 * 
	 * @since 1.2
	 */
	public FileWithTag[] listFiles() {
		return (FileWithTag[])super.listFiles();
	}

	public FileWithTag[] listFiles(FilenameFilter filter) {
		return (FileWithTag[])super.listFiles(filter);
	}

	public FileWithTag[] listFiles(FileFilter filter) {
		return (FileWithTag[])super.listFiles(filter);
	}

	public static FileWithTag[] listRoots() {
		return (FileWithTag[])EFile.listRoots(FileWithTag.class);
	}

//	public static FileWithTag[] listRoots() {
//		return (FileWithTag[])listRoots(FileWithTagFactory);
//	}

	public Object getTag() {
		return tag;
	}

	public void setTag(Object tag) {
		this.tag = tag;
	}
}
