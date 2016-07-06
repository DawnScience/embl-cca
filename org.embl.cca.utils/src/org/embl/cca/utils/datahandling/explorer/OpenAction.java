package org.embl.cca.utils.datahandling.explorer;

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.program.Program;
import org.embl.cca.utils.Activator;
import org.embl.cca.utils.ImageConstants;

public class OpenAction
  extends Action
  implements ISelectionChangedListener, IDoubleClickListener
{
  Explorer window;

  public OpenAction(Explorer w)
  {
    window = w;
    setText("Run");
    setToolTipText("Run the associated program on a file");
    setEnabled(false);
    setImageDescriptor(
//      ImageDescriptor.createFromURL(Util.newURL("file:icons/run.gif")));
        Activator.getDefault().getImageRegistry().getDescriptor(ImageConstants.IMG_RUN));
  }

  public void run()
  {
    IStructuredSelection selection = window.getTableSelection();
    if (selection.size() != 1)
    {
      return;
    }

    File selected_file = (File) selection.getFirstElement();
    if (selected_file.isFile())
    {
      Program.launch(selected_file.getAbsolutePath());
    }
  }

  public void selectionChanged(SelectionChangedEvent event)
  {
    setText("Run");
    setToolTipText("Run the associated program on a file");
    setEnabled(false);

    IStructuredSelection selection = window.getTableSelection();
    if (selection.size() != 1)
    {
      setToolTipText(
        getToolTipText() + " (Only enabled when exactly one item is selected)");
      return;
    }

    File file = (File) selection.getFirstElement();
    if (file.isFile())
    {
      setEnabled(true);
      setText("Run the associated program on " + file.getName());
      setToolTipText(
        "Run the program asociated with "
          + file.getName()
          + " with this file as the argument");
    }
  }

  public void doubleClick(DoubleClickEvent event)
  {
    run();
  }
}
