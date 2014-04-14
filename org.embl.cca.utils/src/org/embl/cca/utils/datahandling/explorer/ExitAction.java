package org.embl.cca.utils.datahandling.explorer;

import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.window.*;
import org.embl.cca.utils.Activator;
import org.embl.cca.utils.ImageConstants;

public class ExitAction extends Action
{
  ApplicationWindow window;

  public ExitAction(ApplicationWindow w)
  {
    window = w;
    setText("E&xit@Ctrl+W");
    setToolTipText("Exit the application");
    setImageDescriptor(
//      ImageDescriptor.createFromURL(Util.newURL("file:icons/close.gif")));
      Activator.getDefault().getImageRegistry().getDescriptor(ImageConstants.IMG_CLOSE));
  }

  public void run()
  {
    window.close();
  }
}
