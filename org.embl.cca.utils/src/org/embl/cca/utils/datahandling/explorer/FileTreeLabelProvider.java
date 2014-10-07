package org.embl.cca.utils.datahandling.explorer;

import java.io.File;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.embl.cca.utils.Activator;
import org.embl.cca.utils.ImageConstants;

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
      return Activator.getDefault().getImageRegistry().get(ImageConstants.IMG_FOLDER);
    }
    else
    {
      return Activator.getDefault().getImageRegistry().get(ImageConstants.IMG_FILE);
    }
  }
}