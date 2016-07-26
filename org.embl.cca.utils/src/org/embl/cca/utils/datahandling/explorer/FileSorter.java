package org.embl.cca.utils.datahandling.explorer;

import java.io.File;

import org.eclipse.jface.viewers.ViewerSorter;

public class FileSorter extends ViewerSorter
{
	@Override
  public int category(Object element)
  {
    return ((File) element).isDirectory() ? 0 : 1;
  }
}
