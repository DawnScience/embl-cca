package org.embl.cca.utils.datahandling.explorer;

import java.io.File;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class FileTableContentProvider implements IStructuredContentProvider
{
  public Object[] getElements(Object element)
  {
    Object[] kids = null;
    kids = ((File) element).listFiles();
    return kids == null ? new Object[0] : kids;
  }

  public void dispose()
  {
  }

  public void inputChanged(Viewer viewer, Object old_object, Object new_object)
  {
  }
}
