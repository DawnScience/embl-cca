package org.embl.cca.utils.datahandling.file;

import java.io.FileNotFoundException;
import java.util.Vector;

import org.embl.cca.utils.datahandling.EFile;
import org.embl.cca.utils.datahandling.FileWithTag;

public interface ICollectionFile {
	/**
	 * Check and refresh new files, replacing the already processed files.
	 * 
	 * @return true if the set of files differs from previously determined set.
	 */
	public boolean refreshAllFiles();

	/**
	 * Check and refresh only new files, not touching the already processed
	 * files (thus their file descriptor remains the same, even if should be
	 * updated). This way the loading of files based on already processed files
	 * is not disturbed at all.
	 * 
	 * @return true if some new files were added.
	 */
	public boolean refreshNewAllFiles();

	/**
	 * Returns the name of the file or directory denoted by this abstract
	 * pathname. This is just the last name in the pathname's name sequence. If
	 * the pathname's name sequence is empty, then the empty string is returned.
	 * 
	 * @return The name of the file or directory denoted by this abstract
	 *         pathname, or the empty string if this pathname's name sequence is
	 *         empty
	 */
	public String getName();

	/**
	 * Returns the absolute pathname string of this abstract pathname. This
	 * absolute pathname in addition contains a protocol prefix like in a URL
	 * and unlike in <code>java.io.File</code>. For example:
	 * file:///etc/something, or file://C:/Windows/something.
	 * 
	 * <p>
	 * If this abstract pathname is already absolute, then the pathname string
	 * is simply returned as if by the <code>{@link #getPath}</code> method. If
	 * this abstract pathname is the empty abstract pathname then the pathname
	 * string of the current user directory, which is named by the system
	 * property <code>user.dir</code>, is returned. Otherwise this pathname is
	 * resolved in a system-dependent way. On UNIX systems, a relative pathname
	 * is made absolute by resolving it against the current user directory. On
	 * Microsoft Windows systems, a relative pathname is made absolute by
	 * resolving it against the current directory of the drive named by the
	 * pathname, if any; if not, it is resolved against the current user
	 * directory.
	 * 
	 * @return The absolute pathname string denoting the same file or directory
	 *         as this abstract pathname
	 * 
	 * @throws SecurityException
	 *             If a required system property value cannot be accessed.
	 * 
	 * @see java.io.File#getAbsolutePath()
	 */
	public String getAbsolutePath();

	public String getAbsolutePathWithoutProtocol();

	/**
	 * Returns the absolute form of this abstract pathname. Equivalent to
	 * <code>new&nbsp;File(this.{@link #getAbsolutePath})</code>.
	 * 
	 * @return The absolute abstract pathname denoting the same file or
	 *         directory as this abstract pathname
	 * 
	 * @throws SecurityException
	 *             If a required system property value cannot be accessed.
	 * 
	 * @since 1.2
	 */
	public EFile getAbsoluteFile();

	/**
	 * Returns the pathname string of this abstract pathname's parent, or
	 * <code>null</code> if this pathname does not name a parent directory. This
	 * pathname does not contain protocol prefix like
	 * <code>{@link #getAbsolutePath}</code>, because it is determined by
	 * cutting last segment of pathname, and this way its correct protocol can
	 * not be guaranteed.
	 * 
	 * <p>
	 * The <em>parent</em> of an abstract pathname consists of the pathname's
	 * prefix, if any, and each name in the pathname's name sequence except for
	 * the last. If the name sequence is empty then the pathname does not name a
	 * parent directory.
	 * 
	 * @return The pathname string of the parent directory named by this
	 *         abstract pathname, or <code>null</code> if this pathname does not
	 *         name a parent
	 */
	public String getParent();

	/**
	 * Returns human readable name of collection.
	 * 
	 * @return the name of collection.
	 */
	public String getCollectionName();

	/**
	 * Return the number of files in the collection.
	 * 
	 * @return number of files in the collection.
	 */
	public int getAllLength();

	/**
	 * Returns the Vector containing the files of collection. The resulting
	 * Vector is affected by subsequent refreshes.
	 * <p>
	 * Note: Callers of this method <b>must not</b> modify the elements of
	 * returned Vector.
	 * 
	 * @return the Vector containing the files of collection.
	 */
	public Vector<FileWithTag> getFilesFromAll();

	/**
	 * Returns the Vector containing the files in the specified range of
	 * collection. The resulting Vector is affected by subsequent refreshes.
	 * <p>
	 * Note: Callers of this method <b>must not</b> modify the returned Vector.
	 * 
	 * @return the Vector containing the files in the specified range of
	 *         collection.
	 */
	public Vector<FileWithTag> getFilesFromAll(int from, int amount);

	/**
	 * Returns the container index of file in the collection.
	 * 
	 * @param filePath
	 * @return index of file in the collection. If index is less than 0, then
	 *         file is not found (anyway the file would be at -(index+1)
	 *         position considering the ordering).
	 */
	public int getIndexOfFile(String filePath);

    /**
     * Tests whether this collection file exists.
     *
     * @return  <code>true</code> if and only if the collection file exists;
     * <code>false</code> otherwise.
     * A collection file exists if it is emulated or at least one file of
     * the collection is found (implies existing parent folder).
     *
     * @throws  SecurityException
     *          If a security manager exists and its <code>{@link
     *          java.lang.SecurityManager#checkRead(java.lang.String)}</code>
     *          method denies read access to the file or directory
     */
	public boolean exists();
}
