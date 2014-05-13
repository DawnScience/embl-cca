package org.embl.cca.utils.datahandling.file;

public class XDSHKLRecord {
	protected final int h;
	protected final int k;
	protected final int l;
	protected final double iobs;
	protected final double sigma;
	protected final double xcal;
	protected final double ycal;
	protected final double zcal;
	protected final double rlp;
	protected final int peak;
	protected final int corr;
	protected final int maxc;
	protected final double xobs;
	protected final double yobs;
	protected final double zobs;
	protected final double alf0;
	protected final double bet0;
	protected final double alf1;
	protected final double bet1;
	protected final double psi;

	public XDSHKLRecord(final int h, final int k, final int l,
		final double iobs, final double sigma,
		final double xcal, final double ycal, final double zcal,
		final double rlp, final int peak, final int corr,
		final int maxc, final double xobs, final double yobs,
		final double zobs, final double alf0, final double bet0,
		final double alf1, final double bet1, final double psi) {
		this.h = h;
		this.k = k;
		this.l = l;
		this.iobs = iobs;
		this.sigma = sigma;
		this.xcal = xcal;
		this.ycal = ycal;
		this.zcal = zcal;
		this.rlp = rlp;
		this.peak = peak;
		this.corr = corr;
		this.maxc = maxc;
		this.xobs = xobs;
		this.yobs = yobs;
		this.zobs = zobs;
		this.alf0 = alf0;
		this.bet0 = bet0;
		this.alf1 = alf1;
		this.bet1 = bet1;
		this.psi = psi;
	}
}
