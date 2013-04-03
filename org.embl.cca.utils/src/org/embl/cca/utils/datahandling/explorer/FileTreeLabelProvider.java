package org.embl.cca.utils.datahandling.explorer;

import java.io.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.*;

public class FileTreeLabelProvider extends LabelProvider
{
  public String getText(Object element)
  {
    return ((File) element).getName();
  }

  public Image getImage(Object element)
  {
    if (((File) element).isDirectory())
    {
      return Util.getImageRegistry().get("folder");
    }
    else
    {
      return Util.getImageRegistry().get("file");
    }
  }
}