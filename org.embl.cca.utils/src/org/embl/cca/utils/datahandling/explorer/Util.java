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

//  public static URL newURL(String url_name)
//  {
//    try
//    {
//      return new URL(url_name);
//    }
//    catch (MalformedURLException e)
//    {
//      throw new RuntimeException("Malformed URL " + url_name, e);
//    }
//  }
//
  public static ImageRegistry getImageRegistry()
  {
    if (image_registry == null)
    {
//      image_registry = new ImageRegistry();
      	image_registry = Activator.getDefault().getImageRegistry();
      image_registry.put(
        "folder",
//        ImageDescriptor.createFromURL(newURL("file:icons/folder.gif")));
		Activator.getImageDescriptor("/icons/folder.gif"));
      image_registry.put(
        "file",
//        ImageDescriptor.createFromURL(newURL("file:icons/file.gif")));
		Activator.getImageDescriptor("/icons/file.gif"));
      image_registry.put(
    	        "close",
//    	        ImageDescriptor.createFromURL(newURL("file:icons/close.gif")));
    			Activator.getImageDescriptor("/icons/close.gif"));
      image_registry.put(
  	        "run",
//  	        ImageDescriptor.createFromURL(newURL("file:icons/run.gif")));
  			Activator.getImageDescriptor("/icons/run.gif"));
      image_registry.put(
    	        "copy",
//    	        ImageDescriptor.createFromURL(newURL("file:icons/copy.gif")));
    	        Activator.getImageDescriptor("/icons/copy.gif"));

    }
    return image_registry;
  }

  public static Clipboard getClipboard()
  {
    if (clipboard == null)
    {
      clipboard = new Clipboard(Display.getCurrent());
    }

    return clipboard;
  }
}