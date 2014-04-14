package org.embl.cca.utils.datahandling;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;

import org.embl.cca.utils.datahandling.text.StringUtils;

public class EFile extends File implements Cloneable {
	private static final long serialVersionUID = 3394163669035862731L;

	public final static String FileProtocolSeparator = "://";

	public final static String FileProtocolID = "file";

	protected String protocolID = FileProtocolID;

//	static Field pathPrivateStringField = null;
//	static Field prefixLengthPrivateStringField = null;
//	static Field fsPrivateStringField = null;
//	static Method resolvePrivateStringMethod = null;
//	static Method prefixLengthPrivateIntMethod = null;

	/**
	 * Override this method if this class works with a different file protocol
	 * than its parent class.
	 * @return the accepted protocol by this kind of File
	 */
	public static String getAcceptedProtocol() {
		return FileProtocolID;
	}

	/**
	 * The FileSystem object representing the platform's local file system.
	 */
//	static protected FileSystem fs = FileSystem.getFileSystem();

	//Some hacking to be able to emulate what File's private constructors do
//	protected void setPath(String pathname) {
//		final String error = "setPath(pathname=" + pathname + ") error"; 
//		try {
//			pathPrivateStringField.set(this, pathname);
//		} catch (SecurityException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		} catch (IllegalArgumentException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		} catch (IllegalAccessException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		}
//	}
//
//	protected void setPrefixLength(int prefixLength) {
//		final String error = "setPrefixLength(prefixLength=" + prefixLength + ") error"; 
//		try {
//			prefixLengthPrivateStringField.setInt(this, prefixLength);
//		} catch (SecurityException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		} catch (IllegalArgumentException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		} catch (IllegalAccessException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		}
//	}

//	protected int getPrefixLength() {
//		return EFile.getPrefixLength(this);
//	}
//
//	protected static int getPrefixLength(File file) {
//		final String error = "getPrefixLength() error"; 
//		try {
//			return prefixLengthPrivateStringField.getInt(file);
//		} catch (SecurityException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		} catch (IllegalArgumentException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		} catch (IllegalAccessException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		}
//	}
//
//	protected static Object getFileSystem() {
//		final String error = "getFileSystem() error"; 
//		try {
//			return fsPrivateStringField.get(null);
//		} catch (SecurityException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		} catch (IllegalArgumentException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		} catch (IllegalAccessException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		}
//	}
//
//    /**
//     * Resolve the child pathname string against the parent.
//     * Both strings must be in normal form, and the result
//     * will be in normal form.
//     */
//    public String fsResolve(String parent, String child) {
//		final String error = "fsResolve(parent=" + parent + ", child=" + child + ") error";
//    	Object fs = getFileSystem();
//		try {
//			return (String)resolvePrivateStringMethod.invoke(fs, parent, child);
//		} catch (SecurityException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		} catch (IllegalArgumentException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		} catch (IllegalAccessException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		} catch (InvocationTargetException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		}
//    }
//
//    /**
//     * Resolve the child pathname string against the parent.
//     * Both strings must be in normal form, and the result
//     * will be in normal form.
//     */
//    public int fsPrefixLength(String path) {
//		final String error = "fsPrefixLength(path=" + path + ") error";
//    	Object fs = getFileSystem();
//		try {
//			return (Integer)prefixLengthPrivateIntMethod.invoke(fs, path);
//		} catch (SecurityException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		} catch (IllegalArgumentException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		} catch (IllegalAccessException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		} catch (InvocationTargetException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		}
//    }

//    protected void overridePrivate() {
//		final String error = "overridePrivate() error";
//		try {
//			pathPrivateStringField = File.class.getDeclaredField("path");
//			prefixLengthPrivateStringField = File.class.getDeclaredField("prefixLength");
//			fsPrivateStringField = File.class.getDeclaredField("fs");
//		} catch (SecurityException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		} catch (NoSuchFieldException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		}
//		pathPrivateStringField.setAccessible(true);
//		prefixLengthPrivateStringField.setAccessible(true);
//		fsPrivateStringField.setAccessible(true);
//    	Object fs = getFileSystem();
//		try {
//			resolvePrivateStringMethod = fs.getClass().getDeclaredMethod("resolve", new Class[] { String.class, String.class });
//		} catch (SecurityException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		} catch (NoSuchMethodException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		}
//    	resolvePrivateStringMethod.setAccessible(true);
//    }

    protected static EFile create(Class<? extends EFile> c, String pathname) {
		final String error = "create(c=" + c + ") error";
    	try {
			Constructor<?> resultConstructor = c.getConstructor(new Class []{String.class});
			Object resultObject = resultConstructor.newInstance(pathname);
			if( !c.isInstance(resultObject) ) //To be safe
				throw new IllegalArgumentException("The created object of specified class is not instance of " + c.getName());
			EFile result = (EFile)resultObject;
//			EFile result = (EFile) c.newInstance();
////			result.overridePrivate();
			return result;
		} catch (Exception e) {
			throw new RuntimeException(error, e);
		}
	}

//    protected EFile create() {
//    	return create(this.getClass());
//    }
//
    protected EFile create(String pathname) {
    	return create(this.getClass(), pathname);
    }

    protected static EFile create(Class<? extends EFile> c, EFile file, String pathname) {
		final String error = "create(c=" + c + ") error";
    	try {
			Constructor<?> resultConstructor = c.getConstructor(new Class []{c, String.class});
			Object resultObject = resultConstructor.newInstance(file, pathname);
			if( !c.isInstance(resultObject) ) //To be safe
				throw new IllegalArgumentException("The created object of specified class is not instance of " + c.getName());
			EFile result = (EFile)resultObject;
//			EFile result = (EFile) c.newInstance();
////			result.overridePrivate();
			return result;
		} catch (Exception e) {
			throw new RuntimeException(error + ", " + e.getMessage());
		}
	}

    protected EFile create(EFile file, String pathname) {
    	return create(this.getClass(), file, pathname);
    }

	protected static EFile[] createMany(Class<? extends EFile> c, int n) {
//		final String error = "create(c=" + c + ") error";
//    	try {
    		Object result = Array.newInstance(c, n);
//            for (int i = 0; i < n; i++) {
//                Array.set(result, i, c.newInstance());
//              }
			return (EFile[]) result;
//		} catch (InstantiationException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		} catch (IllegalAccessException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		}
	}

    protected EFile[] createMany(int n) {
    	return createMany(this.getClass(), n);
    }

    protected static String getAcceptedProtocol(Class<? extends EFile> c) {
		final String error = "getAcceptedProtocol(c=" + c + ") error";
		Exception firstException = null;
		Class<?> currentClass = c;
		do {
			try {
				Method getAcceptedProtocolMethod = currentClass.getDeclaredMethod("getAcceptedProtocol", new Class[] {});
				return (String)getAcceptedProtocolMethod.invoke(currentClass);
			} catch (Exception e) {
				if( firstException == null )
					firstException = e;
				if( EFile.class.equals(currentClass) )
					break;
				currentClass = currentClass.getSuperclass();
			}
		} while( true );
		throw new RuntimeException(error + ", " + firstException.getMessage());
	}

	/* -- Constructors -- */

//	public EFile() {
//		super(""); //Must call a constructor (since empty constructor is not defined), this is the cheapest way.
////		overridePrivate();
//	}
//
	public EFile(File file) {
//		this();
//		construct(file);
		super(getPathWithoutProtocol(file.getAbsolutePath()));
		protocolID = getProtocolFromPath(file.getAbsolutePath());
		if( protocolID == null )
			protocolID = FileProtocolID;
	}

//	protected void construct(File file) {
//		setPath(file.getPath());
//		setPrefixLength(getPrefixLength(file));
//	}
//
	public EFile(EFile file) {
		this((File)file);
	}

//	/**
//	 * Originally: Internal constructor for already-normalized pathname strings.
//	 * Currently: This constructor is same as
//	 * <tt>{@link #EFile(String pathname)}
//	 * </tt> because can not do what the original private constructor, thus it
//	 * is kept for compatibility reason.
//	 */
//	protected EFile(String pathname, int prefixLength) {
////		this();
////		setPath(pathname);
////		setPrefixLength(prefixLength);
//////		this.path = pathname;
//////		this.prefixLength = prefixLength;
//		super(pathname);
//	}

//	protected void construct(String pathname, int prefixLength) {
//		setPath(pathname);
//		setPrefixLength(prefixLength);
////		this.path = pathname;
////		this.prefixLength = prefixLength;
//	}

//	/**
//	 * Originally: Internal constructor for already-normalized pathname strings.
//	 * The parameter order is used to disambiguate this method from the
//	 * public(File, String) constructor. Currently: This constructor is same as
//	 * <tt>{@link #EFile(EFile parent, String child)}
//	 * </tt> because can not do what the original private constructor, thus it
//	 * is kept for compatibility reason.
//	 */
//	protected EFile(String child, EFile parent) {
//		super(parent, child); // Not so effective as calling a private constructor...
////		this();
////		construct(child, parent);
//	}

//	protected void construct(String child, EFile parent) {
//		assert parent.getPath() != null;
//		assert (!parent.getPath().equals(""));
//		setPath( fsResolve(parent.getPath(), child) );
//		setPrefixLength( parent.getPrefixLength() );
////		assert parent.path != null;
////		assert (!parent.path.equals(""));
////		this.path = fs.resolve(parent.path, child);
////		this.prefixLength = parent.prefixLength;
//	}

	/**
	 * Creates a new <code>EFile</code> instance by converting the given
	 * pathname string into an abstract pathname. If the given string is the
	 * empty string, then the result is the empty abstract pathname.
	 * 
	 * @param pathname
	 *            A pathname string
	 * @throws NullPointerException
	 *             If the <code>pathname</code> argument is <code>null</code>
	 */
	public EFile(String pathname) {
		super(getPathWithoutProtocol(pathname));
		protocolID = getProtocolFromPath(pathname);
		if( protocolID == null )
			protocolID = FileProtocolID;
	}

	/*
	 * Note: The two-argument EFile constructors do not interpret an empty
	 * parent abstract pathname as the current user directory. An empty parent
	 * instead causes the child to be resolved against the system-dependent
	 * directory defined by the FileSystem.getDefaultParent method. On Unix this
	 * default is "/", while on Microsoft Windows it is "\\". This is required
	 * for compatibility with the original behavior of this class.
	 */

	/**
	 * Creates a new <code>EFile</code> instance from a parent pathname
	 * string and a child pathname string.
	 * 
	 * <p>
	 * If <code>parent</code> is <code>null</code> then the new
	 * <code>EFile</code> instance is created as if by invoking the
	 * single-argument <code>EFile</code> constructor on the given
	 * <code>child</code> pathname string.
	 * 
	 * <p>
	 * Otherwise the <code>parent</code> pathname string is taken to denote a
	 * directory, and the <code>child</code> pathname string is taken to denote
	 * either a directory or a file. If the <code>child</code> pathname string
	 * is absolute then it is converted into a relative pathname in a
	 * system-dependent way. If <code>parent</code> is the empty string then the
	 * new <code>EFile</code> instance is created by converting
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
	public EFile(String parent, String child) {
		super(parent, getPathWithoutProtocol(child));
		protocolID = getProtocolFromPath(child);
		if( protocolID == null )
			protocolID = FileProtocolID;
	}

	/**
	 * Creates a new <code>EFile</code> instance from a parent abstract
	 * pathname and a child pathname string.
	 * 
	 * <p>
	 * If <code>parent</code> is <code>null</code> then the new
	 * <code>EFile</code> instance is created as if by invoking the
	 * single-argument <code>EFile</code> constructor on the given
	 * <code>child</code> pathname string.
	 * 
	 * <p>
	 * Otherwise the <code>parent</code> abstract pathname is taken to denote a
	 * directory, and the <code>child</code> pathname string is taken to denote
	 * either a directory or a file. If the <code>child</code> pathname string
	 * is absolute then it is converted into a relative pathname in a
	 * system-dependent way. If <code>parent</code> is the empty abstract
	 * pathname then the new <code>EFile</code> instance is created by
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
	public EFile(EFile parent, String child) {
		super(parent, getPathWithoutProtocol(child));
		protocolID = getProtocolFromPath(child);
		if( protocolID == null )
			protocolID = FileProtocolID;
	}

	/**
	 * Creates a new <tt>EFile</tt> instance by converting the given
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
	 * new EFile(</tt><i>&nbsp;f</i>
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
	public EFile(URI uri) {
		super(uri);
		protocolID = getProtocolFromPath(uri.getPath());
		if( protocolID == null )
			protocolID = FileProtocolID;
	}

	public String getProtocol() {
		return protocolID;
	}

	/* File Manager methods, maybe later will be separated into a File Manager */
	public static String[] splitPathByProtocol(String filePath) {
		String result[];
		if( filePath == null )
			result = new String[] {null, null};
		else {
			result = filePath.split(FileProtocolSeparator, 2);
			if( result.length < 2 )
				result = new String[] {null, result[0]};
		}
		return result;
	}

	public static String getProtocolFromPath(String pathname) {
		return splitPathByProtocol(pathname)[0];
	}

	public static String getPathWithoutProtocol(String pathname) {
		return splitPathByProtocol(pathname)[1];
	}

	public static boolean acceptProtocol(Class<? extends EFile> c, String protocol) {
		if( protocol == null )
			protocol = FileProtocolID;
		return getAcceptedProtocol(c).equals(protocol);
	}

	public static boolean acceptPath(Class<? extends EFile> c, String pathname) {
		return acceptProtocol(c, getProtocolFromPath(pathname));
	}

	public boolean acceptPath(String pathname) {
		return acceptProtocol(this.getClass(), getProtocolFromPath(pathname));
	}

	public static String getPathWithTrailingSeparator(String pathname) {
		String result = pathname;
		if( !result.endsWith(EFile.separator))
			result += EFile.separator;
		return result;
	}

    /**
     * Returns the pathname string of this abstract pathname's parent with protocol, or
     * <code>null</code> if this pathname does not name a parent directory.
     *
     * <p> The <em>parent</em> of an abstract pathname consists of the
     * pathname's protocol, prefix, if any, and each name in the pathname's name
     * sequence except for the last.  If the name sequence is empty then
     * the pathname does not name a parent directory.
     *
     * @return  The pathname string of the parent directory named by this
     *          abstract pathname, or <code>null</code> if this pathname
     *          does not name a parent
     */
    public String getParentWithProtocol() {
    	String parent = getParent();
    	if( parent != null )
    		parent = protocolID + FileProtocolSeparator + parent;
    	return parent;
    }

    /**
     * Returns the abstract pathname of this abstract pathname's parent,
     * or <code>null</code> if this pathname does not name a parent
     * directory.
     *
     * <p> The <em>parent</em> of an abstract pathname consists of the
     * pathname's prefix, if any, and each name in the pathname's name
     * sequence except for the last.  If the name sequence is empty then
     * the pathname does not name a parent directory.
     *
     * @return  The abstract pathname of the parent directory named by this
     *          abstract pathname, or <code>null</code> if this pathname
     *          does not name a parent
     *
     * @since 1.2
     */
	@Override
    public EFile getParentFile() {
	String p = this.getParent();
	if (p == null) return null;
//	EFile result = create();
//	result.construct(p, getPrefixLength());
////	return new EFile(p, this.prefixLength);
	EFile result = create(p);
	return result;
    }

    /**
     * Returns the absolute pathname string of this abstract pathname.
     *
     * <p> If this abstract pathname is already absolute, then the pathname
     * string is simply returned as if by the <code>{@link #getPath}</code>
     * method.  If this abstract pathname is the empty abstract pathname then
     * the pathname string of the current user directory, which is named by the
     * system property <code>user.dir</code>, is returned.  Otherwise this
     * pathname is resolved in a system-dependent way.  On UNIX systems, a
     * relative pathname is made absolute by resolving it against the current
     * user directory.  On Microsoft Windows systems, a relative pathname is made absolute
     * by resolving it against the current directory of the drive named by the
     * pathname, if any; if not, it is resolved against the current user
     * directory.
     *
     * @return  The absolute pathname string denoting the same file or
     *          directory as this abstract pathname
     *
     * @throws  SecurityException
     *          If a required system property value cannot be accessed.
     *
     * @see     java.io.File#isAbsolute()
     */
	@Override
    public String getAbsolutePath() {
    	return protocolID + FileProtocolSeparator + super.getAbsolutePath();
    }

    public String getAbsolutePathWithoutProtocol() {
    	return super.getAbsolutePath();
    }

    /**
     * Returns the absolute form of this abstract pathname.  Equivalent to
     * <code>new&nbsp;File(this.{@link #getAbsolutePath})</code>.
     *
     * @return  The absolute abstract pathname denoting the same file or
     *          directory as this abstract pathname
     *
     * @throws  SecurityException
     *          If a required system property value cannot be accessed.
     *
     * @since 1.2
     */
	@Override
    public EFile getAbsoluteFile() {
    	String absPath = getAbsolutePath();
//    	EFile result = create();
//    	result.construct(absPath, fsPrefixLength(absPath));
////        return new EFile(absPath, fs.prefixLength(absPath));
    	EFile result = create(absPath);
    	return result;
    }

	/**
	 * Get file extension (result will NOT include ".")
	 * 
	 * @return String file extension value, or empty String ("") if no extension
	 */
	public String getFileExtension() {
		return getFileExtension(this);
	}

	/**
	 * Get file extension (result will NOT include ".")
	 * 
	 * @param file File to get filename from
	 * @return String file extension value, or empty String ("") if no extension
	 */
	public static String getFileExtension(final File file) {
		return getFileExtension(file.getName());
	}

	/**
	 * Get file extension (result will NOT include ".")
	 * 
	 * @param fileName path to get filename of
	 * @return String file extension value, or empty String ("") if no extension
	 */
	public static String getFileExtension(final String fileName) {
		final int posExt = fileName.lastIndexOf('.');
		return posExt == -1 ? StringUtils.EMPTY_STRING : fileName.substring(posExt + 1);
	}

	/**
	 * Get Filename minus it's extension if present
	 * 
	 * @return String filename minus its extension
	 */
	public String getFileNameNoExtension() {
		return getFileNameNoExtension(this);
	}
	
	/**
	 * Get Filename minus it's extension if present
	 * 
	 * @param file
	 *            File to get filename from
	 * @return String filename minus its extension
	 */
	public static String getFileNameNoExtension(final File file) {
		return getFileNameNoExtension(file.getName());
	}
	
	/**
	 * Get Filename minus it's extension if present
	 * 
	 * @param fileName path to get filename of
	 * @return String filename minus its extension
	 */
	public static String getFileNameNoExtension(final String fileName) {
		int posExt = fileName.lastIndexOf('.');
		return posExt == -1 ? fileName : fileName.substring(0, posExt);

	}

    @Override
    public EFile clone() {
    	return getAbsoluteFile();
    }

    /**
     * Returns the canonical form of this abstract pathname.  Equivalent to
     * <code>new&nbsp;File(this.{@link #getCanonicalPath})</code>.
     *
     * @return  The canonical pathname string denoting the same file or
     *          directory as this abstract pathname
     *
     * @throws  IOException
     *          If an I/O error occurs, which is possible because the
     *          construction of the canonical pathname may require
     *          filesystem queries
     *
     * @throws  SecurityException
     *          If a required system property value cannot be accessed, or
     *          if a security manager exists and its <code>{@link
     *          java.lang.SecurityManager#checkRead}</code> method denies
     *          read access to the file
     *
     * @since 1.2
     */
	@Override
    public File getCanonicalFile() throws IOException {
        String canonPath = protocolID + FileProtocolSeparator + getCanonicalPath();
//    	EFile result = create();
//    	result.construct(canonPath, fsPrefixLength(canonPath));
////    	return new File(canonPath, fs.prefixLength(canonPath));
    	EFile result = create(canonPath);
    	return result;
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
	@Override
	public EFile[] listFiles() {
		String[] ss = list();
		if (ss == null)
			return null;
		int n = ss.length;
//		EFile[] fl = new EFile[n];
		EFile[] fl = createMany(n);
		for (int i = 0; i < n; i++) {
//			fl[i] = new EFile(ss[i], this);
//			fl[i] = create();
//			fl[i].construct(ss[i], this);
			fl[i] = create(this, ss[i]);
		}
		return fl;
	}

//	public EFile[] listFiles() {
//		String[] ss = list();
//		if (ss == null)
//			return null;
//		int n = ss.length;
//		EFile[] fs = new EFile[n];
//		for (int i = 0; i < n; i++) {
//			fs[i] = new EFile(ss[i], this);
//		}
//		return fs;
//	}

	/**
	 * Returns an array of abstract pathnames denoting the files and directories
	 * in the directory denoted by this abstract pathname that satisfy the
	 * specified filter. The behavior of this method is the same as that of the
	 * <code>{@link #listFiles()}</code> method, except that the pathnames in
	 * the returned array must satisfy the filter. If the given
	 * <code>filter</code> is <code>null</code> then all pathnames are accepted.
	 * Otherwise, a pathname satisfies the filter if and only if the value
	 * <code>true</code> results when the
	 * <code>{@link FilenameFilter#accept}</code> method of the filter is
	 * invoked on this abstract pathname and the name of a file or directory in
	 * the directory that it denotes.
	 * 
	 * @param filter
	 *            A filename filter
	 * 
	 * @return An array of abstract pathnames denoting the files and directories
	 *         in the directory denoted by this abstract pathname. The array
	 *         will be empty if the directory is empty. Returns
	 *         <code>null</code> if this abstract pathname does not denote a
	 *         directory, or if an I/O error occurs.
	 * 
	 * @throws SecurityException
	 *             If a security manager exists and its <code>{@link
	 *          java.lang.SecurityManager#checkRead(java.lang.String)}</code>
	 *             method denies read access to the directory
	 * 
	 * @since 1.2
	 */
	@Override
	public EFile[] listFiles(FilenameFilter filter) {
		String ss[] = list();
		if (ss == null)
			return null;
		ArrayList<EFile> v = new ArrayList<EFile>();
		for (int i = 0; i < ss.length; i++) {
			if ((filter == null) || filter.accept(this, ss[i])) {
//				v.add(new EFile(ss[i], this));
//				EFile newFile = create();
//				newFile.construct(ss[i], this);
//				v.add(newFile);
				v.add(create(this, ss[i]));
			}
		}
//		return (EFile[]) (v.toArray(new EFile[v.size()]));
		return (EFile[]) (v.toArray(createMany(v.size())));
	}

	/**
	 * Returns an array of abstract pathnames denoting the files and directories
	 * in the directory denoted by this abstract pathname that satisfy the
	 * specified filter. The behavior of this method is the same as that of the
	 * <code>{@link #listFiles()}</code> method, except that the pathnames in
	 * the returned array must satisfy the filter. If the given
	 * <code>filter</code> is <code>null</code> then all pathnames are accepted.
	 * Otherwise, a pathname satisfies the filter if and only if the value
	 * <code>true</code> results when the
	 * <code>{@link FileFilter#accept(java.io.File)}</code> method of the filter
	 * is invoked on the pathname.
	 * 
	 * @param filter
	 *            A file filter
	 * 
	 * @return An array of abstract pathnames denoting the files and directories
	 *         in the directory denoted by this abstract pathname. The array
	 *         will be empty if the directory is empty. Returns
	 *         <code>null</code> if this abstract pathname does not denote a
	 *         directory, or if an I/O error occurs.
	 * 
	 * @throws SecurityException
	 *             If a security manager exists and its <code>{@link
	 *          java.lang.SecurityManager#checkRead(java.lang.String)}</code>
	 *             method denies read access to the directory
	 * 
	 * @since 1.2
	 */
	@Override
	public EFile[] listFiles(FileFilter filter) {
		String ss[] = list();
		if (ss == null)
			return null;
		ArrayList<EFile> v = new ArrayList<EFile>();
		for (int i = 0; i < ss.length; i++) {
//			EFile f = new EFile(ss[i], this);
//			EFile f = create();
//			f.construct(ss[i], this);
			EFile f = create(this, ss[i]);
			if ((filter == null) || filter.accept(f)) {
				v.add(f);
			}
		}
//		return (EFile[]) (v.toArray(new EFile[v.size()]));
		return (EFile[]) (v.toArray(createMany(v.size())));
	}

	public static EFile[] filterFiles(Class<? extends EFile> c, File[] files, FileFilter filter) {
		if (files == null)
			return null;
		ArrayList<EFile> v = new ArrayList<EFile>();
		for (int i = 0; i < files.length; i++) {
//			EFile f = new EFile(files[i]);
//			EFile f = create(c);
//			f.construct(files[i]);
			EFile f = create(c, files[i].getAbsolutePath());
			if ((filter == null) || filter.accept(f)) {
				v.add(f);
			}
		}
//		return (EFile[]) (v.toArray(new EFile[v.size()]));
		return (EFile[]) (v.toArray(createMany(c, v.size())));
	}

	/**
	 * List the available filesystem roots.
	 * 
	 * <p>
	 * A particular Java platform may support zero or more
	 * hierarchically-organized file systems. Each file system has a
	 * <code>root</code> directory from which all other files in that file
	 * system can be reached. Windows platforms, for example, have a root
	 * directory for each active drive; UNIX platforms have a single root
	 * directory, namely <code>"/"</code>. The set of available filesystem roots
	 * is affected by various system-level operations such as the insertion or
	 * ejection of removable media and the disconnecting or unmounting of
	 * physical or virtual disk drives.
	 * 
	 * <p>
	 * This method returns an array of <code>File</code> objects that denote the
	 * root directories of the available filesystem roots. It is guaranteed that
	 * the canonical pathname of any file physically present on the local
	 * machine will begin with one of the roots returned by this method.
	 * 
	 * <p>
	 * The canonical pathname of a file that resides on some other machine and
	 * is accessed via a remote-filesystem protocol such as SMB or NFS may or
	 * may not begin with one of the roots returned by this method. If the
	 * pathname of a remote file is syntactically indistinguishable from the
	 * pathname of a local file then it will begin with one of the roots
	 * returned by this method. Thus, for example, <code>File</code> objects
	 * denoting the root directories of the mapped network drives of a Windows
	 * platform will be returned by this method, while <code>File</code> objects
	 * containing UNC pathnames will not be returned by this method.
	 * 
	 * <p>
	 * Unlike most methods in this class, this method does not throw security
	 * exceptions. If a security manager exists and its <code>{@link
	 * java.lang.SecurityManager#checkRead(java.lang.String)}</code> method
	 * denies read access to a particular root directory, then that directory
	 * will not appear in the result.
	 * 
	 * @return An array of <code>File</code> objects denoting the available
	 *         filesystem roots, or <code>null</code> if the set of roots could
	 *         not be determined. The array will be empty if there are no
	 *         filesystem roots.
	 * 
	 * @since 1.2
	 */
	public static EFile[] listRoots(Class<? extends EFile> c) {
		File[] files = File.listRoots();
		if( files == null )
			return null;
		int iMax = files.length;
		EFile[] result = createMany(c, files.length);
		for( int i = 0; i < iMax; i++ ) {
//			result[i] = new EFile(files[i]);
//			result[i] = create(c);
//			result[i].construct(files[i]);
			result[i] = create(c, files[i].getAbsolutePath());
		}
		return result;
	}

	/* -- Temporary files -- */
//
//	// lazy initialization of SecureRandom and temporary file directory
//	private static class LazyInitialization {
//		static final SecureRandom random = new SecureRandom();
//
//		static final String temporaryDirectory = temporaryDirectory();
//
//		static String temporaryDirectory() {
//			return fs.normalize(AccessController.doPrivileged(
//			// new GetPropertyAction("java.io.tmpdir"))); //Translated as
//			// written here:
//			// http://stackoverflow.com/questions/852453/accesscontroller-doprivileged
//					new PrivilegedAction<String>() {
//						public String run() {
//							return System.getProperty("java.io.tmpdir");
//						}
//					}));
//		}
//	}
//
//	private static File generateFile(String prefix, String suffix, File dir)
//			throws IOException {
//		long n = LazyInitialization.random.nextLong();
//		if (n == Long.MIN_VALUE) {
//			n = 0; // corner case
//		} else {
//			n = Math.abs(n);
//		}
//		return new File(dir, prefix + Long.toString(n) + suffix);
//	}
//
//	private static boolean checkAndCreate(String filename, SecurityManager sm,
//			boolean restrictive) throws IOException {
//		if (sm != null) {
//			try {
//				sm.checkWrite(filename);
//			} catch (AccessControlException x) {
//				/*
//				 * Throwing the original AccessControlException could disclose
//				 * the location of the default temporary directory, so we
//				 * re-throw a more innocuous SecurityException
//				 */
//				throw new SecurityException("Unable to create temporary file");
//			}
//		}
//		return fs.createFileExclusively(filename, restrictive);
//	}
//
//	// The resulting temporary file may have more restrictive access permission
//	// on some platforms, if restrictive is true.
//	private static File createTempFile0(String prefix, String suffix,
//			File directory, boolean restrictive) throws IOException {
//		if (prefix == null)
//			throw new NullPointerException();
//		if (prefix.length() < 3)
//			throw new IllegalArgumentException("Prefix string too short");
//		String s = (suffix == null) ? ".tmp" : suffix;
//		if (directory == null) {
//			String tmpDir = LazyInitialization.temporaryDirectory();
//			directory = new EFile(tmpDir, fs.prefixLength(tmpDir));
//		}
//		SecurityManager sm = System.getSecurityManager();
//		File f;
//		do {
//			f = generateFile(prefix, s, directory);
//		} while (!checkAndCreate(f.getPath(), sm, restrictive));
//		return f;
//	}

	/**
	 * <p>
	 * Creates a new empty file in the specified directory, using the given
	 * prefix and suffix strings to generate its name. If this method returns
	 * successfully then it is guaranteed that:
	 * 
	 * <ol>
	 * <li>The file denoted by the returned abstract pathname did not exist
	 * before this method was invoked, and
	 * <li>Neither this method nor any of its variants will return the same
	 * abstract pathname again in the current invocation of the virtual machine.
	 * </ol>
	 * 
	 * This method provides only part of a temporary-file facility. To arrange
	 * for a file created by this method to be deleted automatically, use the
	 * <code>{@link #deleteOnExit}</code> method.
	 * 
	 * <p>
	 * The <code>prefix</code> argument must be at least three characters long.
	 * It is recommended that the prefix be a short, meaningful string such as
	 * <code>"hjb"</code> or <code>"mail"</code>. The <code>suffix</code>
	 * argument may be <code>null</code>, in which case the suffix
	 * <code>".tmp"</code> will be used.
	 * 
	 * <p>
	 * To create the new file, the prefix and the suffix may first be adjusted
	 * to fit the limitations of the underlying platform. If the prefix is too
	 * long then it will be truncated, but its first three characters will
	 * always be preserved. If the suffix is too long then it too will be
	 * truncated, but if it begins with a period character (<code>'.'</code>)
	 * then the period and the first three characters following it will always
	 * be preserved. Once these adjustments have been made the name of the new
	 * file will be generated by concatenating the prefix, five or more
	 * internally-generated characters, and the suffix.
	 * 
	 * <p>
	 * If the <code>directory</code> argument is <code>null</code> then the
	 * system-dependent default temporary-file directory will be used. The
	 * default temporary-file directory is specified by the system property
	 * <code>java.io.tmpdir</code>. On UNIX systems the default value of this
	 * property is typically <code>"/tmp"</code> or <code>"/var/tmp"</code>; on
	 * Microsoft Windows systems it is typically <code>"C:\\WINNT\\TEMP"</code>.
	 * A different value may be given to this system property when the Java
	 * virtual machine is invoked, but programmatic changes to this property are
	 * not guaranteed to have any effect upon the temporary directory used by
	 * this method.
	 * 
	 * @param prefix
	 *            The prefix string to be used in generating the file's name;
	 *            must be at least three characters long
	 * 
	 * @param suffix
	 *            The suffix string to be used in generating the file's name;
	 *            may be <code>null</code>, in which case the suffix
	 *            <code>".tmp"</code> will be used
	 * 
	 * @param directory
	 *            The directory in which the file is to be created, or
	 *            <code>null</code> if the default temporary-file directory is
	 *            to be used
	 * 
	 * @return An abstract pathname denoting a newly-created empty file
	 * 
	 * @throws IllegalArgumentException
	 *             If the <code>prefix</code> argument contains fewer than three
	 *             characters
	 * 
	 * @throws IOException
	 *             If a file could not be created
	 * 
	 * @throws SecurityException
	 *             If a security manager exists and its <code>
	 *             {@link java.lang.SecurityManager#checkWrite(java.lang.String)}
	 *             </code> method does not allow a file to be created
	 * 
	 * @since 1.2
	 */
	public static EFile createTempFile(Class<? extends EFile> c, String prefix, String suffix,
			File directory) throws IOException {
//		return createTempFile0(prefix, suffix, directory, false);
		EFile result = new EFile(createTempFile(prefix, suffix, directory));
//		EFile result = EFile.create(c);
//		result.construct(createTempFile(prefix, suffix, directory));
		return result;
	}

	/**
	 * Creates an empty file in the default temporary-file directory, using the
	 * given prefix and suffix to generate its name. Invoking this method is
	 * equivalent to invoking <code>{@link #createTempFile(java.lang.String,
	 * java.lang.String, java.io.File)
	 * createTempFile(prefix,&nbsp;suffix,&nbsp;null)}</code>.
	 * 
	 * @param prefix
	 *            The prefix string to be used in generating the file's name;
	 *            must be at least three characters long
	 * 
	 * @param suffix
	 *            The suffix string to be used in generating the file's name;
	 *            may be <code>null</code>, in which case the suffix
	 *            <code>".tmp"</code> will be used
	 * 
	 * @return An abstract pathname denoting a newly-created empty file
	 * 
	 * @throws IllegalArgumentException
	 *             If the <code>prefix</code> argument contains fewer than three
	 *             characters
	 * 
	 * @throws IOException
	 *             If a file could not be created
	 * 
	 * @throws SecurityException
	 *             If a security manager exists and its <code>{@link
	 *          java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
	 *             method does not allow a file to be created
	 * 
	 * @since 1.2
	 */
	public static File createTempFile(Class<? extends EFile> c, String prefix, String suffix)
			throws IOException {
//		return createTempFile0(prefix, suffix, null, false);
		EFile result = new EFile(createTempFile(prefix, suffix));
//		EFile result = EFile.create(c);
//		result.construct(createTempFile(prefix, suffix));
		return result;
	}

//	static {
//		final String error = "overridePrivate() error";
//		try {
//			pathPrivateStringField = File.class.getDeclaredField("path");
//			prefixLengthPrivateStringField = File.class.getDeclaredField("prefixLength");
//			fsPrivateStringField = File.class.getDeclaredField("fs");
//		} catch (SecurityException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		} catch (NoSuchFieldException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		}
//		pathPrivateStringField.setAccessible(true);
//		prefixLengthPrivateStringField.setAccessible(true);
//		fsPrivateStringField.setAccessible(true);
//    	Object fs = EFile.getFileSystem();
//		try {
//			Method[] ms = fs.getClass().getDeclaredMethods();
//			for( Method m : ms )
//				System.out.println("fs." + m.getName() + ", details: " + m.toGenericString());
//			resolvePrivateStringMethod = fs.getClass().getDeclaredMethod("resolve", new Class[] { String.class, String.class });
//			prefixLengthPrivateIntMethod = fs.getClass().getDeclaredMethod("prefixLength", new Class[] { String.class });
//		} catch (SecurityException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		} catch (NoSuchMethodException e) {
//		    throw new RuntimeException(error + ", " + e.getMessage());
//		}
//    	resolvePrivateStringMethod.setAccessible(true);
//    	prefixLengthPrivateIntMethod.setAccessible(true);
//    }
}
