package org.embl.cca.dviewer.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;

/**
 * Command exposed by this plugin to allow the PHA to be added to any plot.
 * 
 * @author Matthew Gerring
 *
 */
public class PHACommand extends AbstractHandler implements IHandler {
	
	private PHAJob job;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		if (job==null) job = new PHAJob("PHA Algorithm");
		job.schedule(event);
		return Boolean.TRUE;
	}

}
