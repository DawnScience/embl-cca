/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.4
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.iucr.cbflib;

public class intP {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  public intP(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(intP obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        cbfJNI.delete_intP(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public intP() {
    this(cbfJNI.new_intP(), true);
  }

  public void assign(int value) {
    cbfJNI.intP_assign(swigCPtr, this, value);
  }

  public int value() {
    return cbfJNI.intP_value(swigCPtr, this);
  }

  public SWIGTYPE_p_int cast() {
    long cPtr = cbfJNI.intP_cast(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_int(cPtr, false);
  }

  public static intP frompointer(SWIGTYPE_p_int t) {
    long cPtr = cbfJNI.intP_frompointer(SWIGTYPE_p_int.getCPtr(t));
    return (cPtr == 0) ? null : new intP(cPtr, false);
  }

}
