package org.embl.cca.utils.imageviewer;

public class TwoDimFloatArrayData {
	int width;
	int height;
	float[] data;

	public TwoDimFloatArrayData( int width, int height, float[] data ) {
		this.width = width;
		this.height = height;
		this.data = data;
	}

	/**
	 * The first dimension of the image
	 * @return
	 */
	public int getWidth() {
		return width;
	}

	public void setWidth( int width ) {
		this.width = width;
	}

	/**
	 * The second dimension of the image
	 * @return
	 */
	public int getHeight() {
		return height;
	}

	public void setHeight( int height ) {
		this.height = height;
	}

	public float[] getData() {
		return data;
	}

	public void setData(float [] data) {
		this.data = data;
	}

}
