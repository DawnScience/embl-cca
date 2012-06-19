/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.4
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.iucr.cbflib;

public class cbf_handle_struct {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  public cbf_handle_struct(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(cbf_handle_struct obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        cbfJNI.delete_cbf_handle_struct(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setNode(SWIGTYPE_p_cbf_node value) {
    cbfJNI.cbf_handle_struct_node_set(swigCPtr, this, SWIGTYPE_p_cbf_node.getCPtr(value));
  }

  public SWIGTYPE_p_cbf_node getNode() {
    long cPtr = cbfJNI.cbf_handle_struct_node_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_cbf_node(cPtr, false);
  }

  public void setDictionary(cbf_handle_struct value) {
    cbfJNI.cbf_handle_struct_dictionary_set(swigCPtr, this, cbf_handle_struct.getCPtr(value), value);
  }

  public cbf_handle_struct getDictionary() {
    long cPtr = cbfJNI.cbf_handle_struct_dictionary_get(swigCPtr, this);
    return (cPtr == 0) ? null : new cbf_handle_struct(cPtr, false);
  }

  public void setFile(SWIGTYPE_p_cbf_file value) {
    cbfJNI.cbf_handle_struct_file_set(swigCPtr, this, SWIGTYPE_p_cbf_file.getCPtr(value));
  }

  public SWIGTYPE_p_cbf_file getFile() {
    long cPtr = cbfJNI.cbf_handle_struct_file_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_cbf_file(cPtr, false);
  }

  public void setStartcolumn(int value) {
    cbfJNI.cbf_handle_struct_startcolumn_set(swigCPtr, this, value);
  }

  public int getStartcolumn() {
    return cbfJNI.cbf_handle_struct_startcolumn_get(swigCPtr, this);
  }

  public void setStartline(int value) {
    cbfJNI.cbf_handle_struct_startline_set(swigCPtr, this, value);
  }

  public int getStartline() {
    return cbfJNI.cbf_handle_struct_startline_get(swigCPtr, this);
  }

  public void setLogfile(SWIGTYPE_p_FILE value) {
    cbfJNI.cbf_handle_struct_logfile_set(swigCPtr, this, SWIGTYPE_p_FILE.getCPtr(value));
  }

  public SWIGTYPE_p_FILE getLogfile() {
    long cPtr = cbfJNI.cbf_handle_struct_logfile_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_FILE(cPtr, false);
  }

  public void setWarnings(int value) {
    cbfJNI.cbf_handle_struct_warnings_set(swigCPtr, this, value);
  }

  public int getWarnings() {
    return cbfJNI.cbf_handle_struct_warnings_get(swigCPtr, this);
  }

  public void setErrors(int value) {
    cbfJNI.cbf_handle_struct_errors_set(swigCPtr, this, value);
  }

  public int getErrors() {
    return cbfJNI.cbf_handle_struct_errors_get(swigCPtr, this);
  }

  public void setRefcount(int value) {
    cbfJNI.cbf_handle_struct_refcount_set(swigCPtr, this, value);
  }

  public int getRefcount() {
    return cbfJNI.cbf_handle_struct_refcount_get(swigCPtr, this);
  }

  public void setRow(int value) {
    cbfJNI.cbf_handle_struct_row_set(swigCPtr, this, value);
  }

  public int getRow() {
    return cbfJNI.cbf_handle_struct_row_get(swigCPtr, this);
  }

  public void setSearch_row(int value) {
    cbfJNI.cbf_handle_struct_search_row_set(swigCPtr, this, value);
  }

  public int getSearch_row() {
    return cbfJNI.cbf_handle_struct_search_row_get(swigCPtr, this);
  }

  public cbf_handle_struct() {
    this(cbfJNI.new_cbf_handle_struct(), true);
  }

}
