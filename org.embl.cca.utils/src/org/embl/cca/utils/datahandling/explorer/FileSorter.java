package org.embl.cca.utils.datahandling.explorer;

import java.io.*;
import org.eclipse.jface.viewers.*;

public class FileSorter extends ViewerSorter
{
	@Override
  public int category(Object element)
  {
    return ((File) element).isDirectory() ? 0 : 1;
  }
}
