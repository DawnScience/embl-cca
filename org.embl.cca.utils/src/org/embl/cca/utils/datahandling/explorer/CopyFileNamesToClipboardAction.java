package org.embl.cca.utils.datahandling.explorer;

import java.io.*;
import java.util.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.dnd.*;
import org.embl.cca.utils.Activator;

public class CopyFileNamesToClipboardAction extends Action
{
  Explorer window;

  public CopyFileNamesToClipboardAction(Explorer w)
  {
    window = w;
    setToolTipText("Copy absolute file names of selected files to the clipboard");
    setText("Copy File &Names@Ctrl+Shift+C");
    setImageDescriptor(
//      ImageDescriptor.createFromURL(Util.newURL("file:icons/copy.gif")));
    	Util.getImageRegistry().getDescriptor("copy"));
  }

  public void run()
  {
    Clipboard clipboard = Util.getClipboard();
    TextTransfer text_transfer = TextTransfer.getInstance();
    
    IStructuredSelection selection = window.getTableSelection();
    if (selection.isEmpty())
    {
      return;
    }

    StringBuffer string_buffer = new StringBuffer();
    for (@SuppressWarnings("unchecked")
		Iterator<File> i = (Iterator<File>)selection.iterator(); i.hasNext();)
    {
      File file = (File) i.next();
      string_buffer.append(" ");
      string_buffer.append(file.getAbsolutePath());
    }

    clipboard.setContents(
      new Object[] { string_buffer.toString()},
      new Transfer[] { text_transfer });
      
    window.setStatus(string_buffer.toString());
  }
}
