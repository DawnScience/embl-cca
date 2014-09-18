package org.embl.cca.utils.datahandling.explorer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import uk.ac.diamond.sda.navigator.views.FileView;

public class FileView extends ViewPart {

	public static final String ID = "uk.ac.diamond.sda.navigator.views.FileView";
	
    private static final Logger logger = LoggerFactory.getLogger(FileView.class);

    protected Explorer explorer;

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		final Composite top = new Composite(parent, SWT.NONE);
		top.setLayout(new GridLayout(2, false));
		top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		// TODO Auto-generated method stub
		explorer.createContents(top);
	}

	public FileView() {
		super();
	    explorer = new Explorer();
//		checkCommandLineArguments();
	}
	
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		
		super.init(site, memento);
		
		String path = null;
//		if (defaultFile!=null) try {path=defaultFile.getAbsolutePath();} catch (Exception e) {};
		if (path==null && memento!=null) path = memento.getString("DIR");
		if (path==null) path = System.getProperty("uk.ac.diamond.sda.navigator.default.file.view.location");
		if (path==null) path = System.getProperty("user.home");
		
//		if (path!=null){
//			savedSelection = new File(path);
//		}
		
	}

	@Override
	public void saveState(IMemento memento) {
		
		if (memento==null) return;
//		if ( getSelectedFile() != null ) {
//		    final String path = getSelectedFile().getAbsolutePath();
//		    memento.putString("DIR", path);
//		}
	}

	@Override
	public void setFocus() {
//		tree.getControl().setFocus();
		// TODO Auto-generated method stub
		
	}

}
