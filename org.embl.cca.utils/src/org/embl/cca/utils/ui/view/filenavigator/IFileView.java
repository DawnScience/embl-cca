/*
 * Copyright 2014 Diamond Light Source Ltd. and EMBL
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

package org.embl.cca.utils.ui.view.filenavigator;

import org.dawb.common.util.io.IFileSelector;
import org.embl.cca.utils.datahandling.EFile;

/**
 * This class is the interface for FileView. 
 * @see org.embl.cca.utils.ui.view.filenavigator.FileView
 */
public interface IFileView extends IFileSelector {
	public void collapseAll();
	public void showPreferences();
	public void refresh();
	public void openSelectedFile();
	public EFile getSelectedFile();
}
