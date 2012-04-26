package loaders.pilatus;

public class PilatusData {
	int width;
	int height;
	float data[];

	public PilatusData( int width, int height, float[] data ) {
		this.width = width;
		this.height = height;
		this.data = data;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public float[] getData() {
		return data;
	}

}
