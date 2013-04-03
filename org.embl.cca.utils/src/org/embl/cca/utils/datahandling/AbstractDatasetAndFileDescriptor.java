package org.embl.cca.utils.datahandling;

public class AbstractDatasetAndFileDescriptor {
	protected String collectionName;
	protected int indexInCollection;
	protected long length;
	protected long lastModified;

	public AbstractDatasetAndFileDescriptor(String collectionName, int indexInCollection, long length, long lastModified) {
		this.collectionName = collectionName;
		this.indexInCollection = indexInCollection;
		this.length = length;
		this.lastModified = lastModified;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public int getIndexInCollection() {
		return indexInCollection;
	}

	public long getLength() {
		return length;
	}

	public long getLastModified() {
		return lastModified;
	}

}
