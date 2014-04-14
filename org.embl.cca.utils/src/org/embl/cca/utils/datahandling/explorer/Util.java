package org.embl.cca.utils.datahandling.explorer;

//import java.net.*;

import org.eclipse.jface.resource.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.embl.cca.utils.Activator;

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