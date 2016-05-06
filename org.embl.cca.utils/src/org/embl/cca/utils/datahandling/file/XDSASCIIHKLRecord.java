package org.embl.cca.utils.datahandling.file;

import org.embl.cca.utils.general.Util;

public class XDSASCIIHKLRecord {
	protected final int h;
	protected final int k;
	protected final int l;
	protected final double iobs;
	protected final double sigma;
	protected final double xd;
	protected final double yd;
	protected final double zd;
	protected final double rlp;
	protected final int peak;
	protected final int corr;
	protected final double psi;
	protected final int startImageIndex;
	protected final int endImageIndex;

	public XDSASCIIHKLRecord(final int h, final int k, final int l,
			final double iobs, final double sigma, final double xd,
			final double yd, final double zd, final double rlp, final int peak,
			final int corr, final double psi, final double oscillationRange,
			final double reflectingRange) {
		this.h = h;
		this.k = k;
		this.l = l;
		this.iobs = iobs;
		this.sigma = sigma;
		this.xd = xd;
		this.yd = yd;
		this.zd = zd;
		this.rlp = rlp;
		this.peak = peak;
		this.corr = corr;
		this.psi = psi;
		this.startImageIndex = (int) Math
				.round((this.zd - (reflectingRange - 1) / 2) / oscillationRange);
		this.endImageIndex = (int) Math
				.round((this.zd + (reflectingRange - 1) / 2) / oscillationRange);
	}

	/**
	 * Returns a string representation of this record and its values. The
	 * returned string may be empty but may not be <code>null</code>.
	 * 
	 * @return a string representation of this point
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(getClass().getName())
				.append('[');
		sb.append("h=").append(h);
		sb.append(", k=").append(k);
		sb.append(", l=").append(l);
		sb.append(", iobs=").append(iobs);
		sb.append(", sigma=").append(sigma);
		sb.append(", xd=").append(xd);
		sb.append(", yd=").append(yd);
		sb.append(", zd=").append(zd);
		sb.append(", rlp=").append(rlp);
		sb.append(", peak=").append(peak);
		sb.append(", corr=").append(corr);
		sb.append(", psi=").append(psi);
		sb.append(", startImageIndex=").append(startImageIndex);
		sb.append(", endImageIndex=").append(endImageIndex);
		sb.append(']');
		return sb.toString();
	}

	/**
	 * Compares the argument to the receiver, and returns true
	 * if they represent the <em>same</em> object using a class
	 * specific comparison.
	 *
	 * @param object the object to compare with this object
	 * @return <code>true</code> if the object is the same as this object and <code>false</code> otherwise
	 *
	 * @see #hashCode()
	 */
	public boolean equals(final Object object) {
		if (object == this) return true;
		if (!(object instanceof XDSASCIIHKLRecord)) return false;
		final XDSASCIIHKLRecord p = (XDSASCIIHKLRecord)object;
		return (p.h == this.h) && (p.k == this.k) && (p.l == this.l) && (p.iobs == this.iobs) && (p.sigma == this.sigma)
			&& (p.xd == this.xd) && (p.yd == this.yd) && (p.zd == this.zd) && (p.rlp == this.rlp)
			&& (p.peak == this.peak) && (p.corr == this.corr) && (p.psi == this.psi)
			&& (p.startImageIndex == this.startImageIndex) && (p.endImageIndex == this.endImageIndex);
	}

	/**
	 * Returns an integer hash code for the receiver. Any two 
	 * objects that return <code>true</code> when passed to 
	 * <code>equals</code> must return the same value for this
	 * method.
	 *
	 * @return the receiver's hash
	 *
	 * @see #equals(Object)
	 */
	@Override
	public int hashCode() {
		return Util.hashCode(new Object[] {h, k, l, iobs, sigma, xd, yd, zd, rlp, peak, corr, psi, startImageIndex, endImageIndex });
	}

	public int getH() {
		return h;
	}

	public int getK() {
		return k;
	}

	public int getL() {
		return l;
	}

	public double getX() {
		return xd;
	}

	public double getY() {
		return yd;
	}

	public double getZ() {
		return zd;
	}

	public int getStartImageIndex() {
		return startImageIndex;
	}

	public int getEndImageIndex() {
		return endImageIndex;
	}
}
