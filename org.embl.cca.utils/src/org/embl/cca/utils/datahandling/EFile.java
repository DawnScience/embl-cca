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
import org.embl.cca.utils.general.Util;

/**
 * Extension class of File.
 * @author naray
 * @see File
 *
 */
public class EFile extends File implements Cloneable {
	private static final long serialVersionUID = 3394163669035862731L;

	public final static String FileProtocolSeparator = "://";

	public final static String FileProtocolID = "file";

	protected String protocolID = FileProtocolID;

	/**
	 * Override this method if this class works with a different file protocol
	 * than its parent class.
	 * @return the accepted protocol by this kind of File
	 */
	public static String getAcceptedProtocol() {
		return FileProtocolID;
	}

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
	 * Creates a new EFile object.
	 * @param pathname
	 * @see File#File(String)
	 */
	public EFile(String pathname) {
		super(getPathWithoutProtocol(pathname));
		protocolID = getProtocolFromPath(pathname);
		if( protocolID == null )
			protocolID = FileProtocolID;
	}

	/**
	 * Creates a new EFile object.
	 * @param parent
	 * @param child
	 * @see File#File(String, String)
	 */
	public EFile(String parent, String child) {
		super(parent, getPathWithoutProtocol(child));
		protocolID = getProtocolFromPath(child);
		if( protocolID == null )
			protocolID = FileProtocolID;
	}

	/**
	 * Creates a new EFile object.
	 * @param parent
	 * @param child
	 * @see File#File(File, String)
	 */
	public EFile(EFile parent, String child) {
		super(parent, getPathWithoutProtocol(child));
		protocolID = getProtocolFromPath(child);
		if( protocolID == null )
			protocolID = FileProtocolID;
	}

	/**
	 * Creates a new EFile object.
	 * @param uri
	 * @see File#File(URI)
	 */
	public EFile(URI uri) {
		super(uri);
		protocolID = getProtocolFromPath(uri.getPath());
		if( protocolID == null )
			protocolID = FileProtocolID;
	}

	/**
	 * Returns a File object representing the same absolute path as this
	 * file. It is compatible conversion if the protocol of this file
	 * is "file". In other cases, the processing of path of returned file
	 * requires attention.
	 * @return the file object representing the same absolute path as this file.
	 */
	public File toFile() {
		return new File(getAbsolutePathWithoutProtocol());
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

	@Override
	public EFile getParentFile() {
		final String p = this.getParent();
		if (p == null) return null;
//	EFile result = create();
//	result.construct(p, getPrefixLength());
////	return new EFile(p, this.prefixLength);
		final EFile result = create(p);
		return result;
	}

	public String getPathWithoutProtocol() {
		return super.getPath();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * If the file protocol equals to FileProtocolID, then it is omitted from
	 * the returned path. This is done for compatilibility reasons.
	 */
	@Override
	public String getAbsolutePath() {
		String result = getAbsolutePathWithoutProtocol();
		if( !protocolID.equals(FileProtocolID) )
			result = protocolID + FileProtocolSeparator + result;
		return result;
	}

	/**
	 * Returns the absolute pathname string of this abstract pathname.
	 * @return The absolute pathname string of this abstract pathname.
	 * @see File#getAbsolutePath()
	 */
	public String getAbsolutePathWithoutProtocol() {
		return super.getAbsolutePath();
	}

	@Override
	public EFile getAbsoluteFile() {
		final String absPath = getAbsolutePath();
//    	EFile result = create();
//    	result.construct(absPath, fsPrefixLength(absPath));
////        return new EFile(absPath, fs.prefixLength(absPath));
		final EFile result = create(absPath);
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
	 * Get Filename minus its extension if present
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

	@Override
	public File getCanonicalFile() throws IOException {
		final String canonPath = protocolID + FileProtocolSeparator + getCanonicalPath();
//    	EFile result = create();
//    	result.construct(canonPath, fsPrefixLength(canonPath));
////    	return new File(canonPath, fs.prefixLength(canonPath));
		final EFile result = create(canonPath);
		return result;
	}

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
	 * @param c The class of returned temp file.
	 * @param prefix
	 * @param suffix
	 * @param directory
	 * @return The created temp file.
	 * @throws IOException
	 * @see File#createTempFile(String, String, File)
	 */
	public static EFile createTempFile(Class<? extends EFile> c, String prefix, String suffix,
			File directory) throws IOException {
//		return createTempFile0(prefix, suffix, directory, false);
		final EFile result = new EFile(createTempFile(prefix, suffix, directory));
//		EFile result = EFile.create(c);
//		result.construct(createTempFile(prefix, suffix, directory));
		return result;
	}

	/**
	 * @param c The class of returned temp file.
	 * @param prefix
	 * @param suffix
	 * @return
	 * @throws IOException
	 * @see File#createTempFile(String, String)
	 */
	public static File createTempFile(Class<? extends EFile> c, String prefix, String suffix)
			throws IOException {
//		return createTempFile0(prefix, suffix, null, false);
		EFile result = new EFile(createTempFile(prefix, suffix));
//		EFile result = EFile.create(c);
//		result.construct(createTempFile(prefix, suffix));
		return result;
	}

	public boolean isReadable() {
		return canRead();
	}

	public boolean isWritable() {
		return canWrite();
	}

	@Override
	public boolean equals(final Object object) {
		if( object == this ) return true;
		if( object instanceof EFile ) {
			final EFile efile = (EFile)object;
			if( !this.protocolID.equals(efile.protocolID) )
				return false;
		} else if( object instanceof File ) {
			if( !this.protocolID.equals(FileProtocolID) )
				return false;
		}
		return super.equals(object);
	}

	@Override
	public int hashCode() {
		return Util.hashCode(new Object[] {super.hashCode(), protocolID});
	}

}
