package org.embl.cca.utils.datahandling.explorer;

//import java.net.*;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Display;

public class Util
{
  private static ImageRegistry image_registry;
//  private static ISharedImages image_registry;
  private static Clipboard clipboard;

  public static Clipboard getClipboard()
  {
    if (clipboard == null)
    {
      clipboard = new Clipboard(Display.getCurrent());
    }

    return clipboard;
  }
}