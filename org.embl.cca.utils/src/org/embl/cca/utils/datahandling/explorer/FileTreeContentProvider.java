package org.embl.cca.utils.datahandling.explorer;

import java.io.*;

import org.eclipse.jface.viewers.*;

public class FileTreeContentProvider implements ITreeContentProvider
{
  public Object[] getChildren(Object element)
  {
    Object[] kids = ((File) element).listFiles();
    return kids == null ? new Object[0] : kids;
  }

  public Object[] getElements(Object element)
  {
    return getChildren(element);
  }

  public boolean hasChildren(Object element)
  {
    return getChildren(element).length > 0;
  }

  public Object getParent(Object element)
  {
    return ((File)element).getParent();
  }
  
  public void dispose()
  {
  }

  public void inputChanged(Viewer viewer, Object old_input, Object new_input)
  {
  }
}
