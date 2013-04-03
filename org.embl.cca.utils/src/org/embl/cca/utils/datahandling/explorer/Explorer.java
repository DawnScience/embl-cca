//http://www.ibm.com/developerworks/opensource/library/os-ecgui1/
//http://stackoverflow.com/questions/2590955/eclipse-rcp-file-explorer
//http://wiki.eclipse.org/Common_Navigator_Framework
//http://www.techjava.de/topics/2009/04/eclipse-common-navigator-framework/
//http://www.vogella.com/blog/2009/06/23/eclipse-rcp-file-browser/
package org.embl.cca.utils.datahandling.explorer;

import java.io.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.widgets.*;

public class Explorer extends ApplicationWindow
{
  private TableViewer tbv;
  private TreeViewer tv;
  private OpenAction open_action;
  private ExitAction exit_action;
  private CopyFileNamesToClipboardAction copy_action;

  public Explorer()
  {
    super(null);

    exit_action = new ExitAction(this);
    copy_action = new CopyFileNamesToClipboardAction(this);
    open_action = new OpenAction(this);

//    addStatusLine();
//    addMenuBar();
//    addToolBar(SWT.FLAT | SWT.WRAP);
  }

  protected Control createContents(Composite parent)
  {
//    getShell().setText("JFace File Explorer");
    SashForm sash_form = new SashForm(parent, SWT.HORIZONTAL | SWT.NULL);

    tv = new TreeViewer(sash_form);
    tv.setContentProvider(new FileTreeContentProvider());
    tv.setLabelProvider(new FileTreeLabelProvider());
//    tv.setInput(new File("C:\\"));
    tv.setInput(File.listRoots()[0]);
    tv.addFilter(new AllowOnlyFoldersFilter());
    tv.setSorter(new FileSorter());

    tbv =
      new TableViewer(sash_form, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
    tbv.setContentProvider(new FileTableContentProvider());
    tbv.setLabelProvider(new FileTableLabelProvider());
    tbv.setSorter(new FileSorter());

    TableColumn column = new TableColumn(tbv.getTable(), SWT.LEFT);
    column.setText("Name");
    column.setWidth(200);

    column = new TableColumn(tbv.getTable(), SWT.RIGHT);
    column.setText("Size");
    column.setWidth(100);

    tbv.getTable().setHeaderVisible(true);

    tv.addSelectionChangedListener(new ISelectionChangedListener()
    {
      public void selectionChanged(SelectionChangedEvent event)
      {
        IStructuredSelection selection =
          (IStructuredSelection) event.getSelection();

        Object selected_file = selection.getFirstElement();
        tbv.setInput(selected_file);
      }
    });

    tbv.addSelectionChangedListener(new ISelectionChangedListener()
    {
      public void selectionChanged(SelectionChangedEvent event)
      {
        IStructuredSelection selection =
          (IStructuredSelection) event.getSelection();

        setStatus("Number of items selected is " + selection.size());
      }
    });

    tbv.addSelectionChangedListener(open_action);

    MenuManager menu_manager = new MenuManager();
    tbv.getTable().setMenu(menu_manager.createContextMenu(tbv.getTable()));

    menu_manager.add(exit_action);
    menu_manager.add(copy_action);
    menu_manager.add(open_action);

    return sash_form;
  }

  public static void main(String[] args)
  {
    Explorer w = new Explorer();
    w.setBlockOnOpen(true);
    w.open();
    Display.getCurrent().dispose();
    Util.getClipboard().dispose();
  }

  protected MenuManager createMenuManager()
  {
    MenuManager bar_menu = new MenuManager("");

    MenuManager file_menu = new MenuManager("&File");
    MenuManager edit_menu = new MenuManager("&Edit");
    MenuManager view_menu = new MenuManager("&View");

    bar_menu.add(file_menu);
    bar_menu.add(edit_menu);
    bar_menu.add(view_menu);

    file_menu.add(exit_action);
    edit_menu.add(copy_action);
    edit_menu.add(open_action);

    return bar_menu;
  }

  public IStructuredSelection getTableSelection()
  {
    return (IStructuredSelection) (tbv.getSelection());
  }

  protected ToolBarManager createToolBarManager(int style)
  {

    ToolBarManager tool_bar_manager = new ToolBarManager(style);

    tool_bar_manager.add(exit_action);
    tool_bar_manager.add(copy_action);
    tool_bar_manager.add(open_action);

    return tool_bar_manager;
  }
}