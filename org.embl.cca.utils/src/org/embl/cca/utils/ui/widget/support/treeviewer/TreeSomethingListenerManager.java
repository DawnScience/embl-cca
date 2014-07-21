/*
 * Copyright 2014 EMBL
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.embl.cca.utils.ui.widget.support.treeviewer;

import org.dawb.common.util.list.ListenerList;
import org.embl.cca.utils.general.Disposable;

//Marked as abstract, because it does not contain any fire* method.
public abstract class TreeSomethingListenerManager<T> implements Disposable {

	protected boolean disposed = false;

	protected final ListenerList<T> treeSomethingListeners;

	public TreeSomethingListenerManager() {
		treeSomethingListeners = new ListenerList<T>();
	}

	public void addListener(final T listener) {
		treeSomethingListeners.add(listener);
	}

	public void removeListener(final T listener) {
		treeSomethingListeners.remove(listener);
	}

	public void removeListeners() {
		treeSomethingListeners.clear();
	}

	@Override
	public void dispose() {
		if( isDisposed() )
			return;
		//dispose this object
		treeSomethingListeners.clear();
		disposed = true;
	}

	@Override
	public boolean isDisposed() {
		return disposed;
	}
}
