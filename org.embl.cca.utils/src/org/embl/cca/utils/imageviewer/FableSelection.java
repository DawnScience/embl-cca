package org.embl.cca.utils.imageviewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.part.EditorPart;

public class FableSelection implements IStructuredSelection {

	public enum SelectionType {
		EMPTY,
		SELECTION_2D;
	}

	final private Rectangle[] rectangles;
	final private EditorPart editor;
	private SelectionType selectionType;

	public FableSelection() {
		this.rectangles = new Rectangle[0];
		editor = null;
		selectionType = SelectionType.EMPTY;
	}

	public FableSelection(Rectangle rectangle, EditorPart editor) {
		this.rectangles = new Rectangle[1];
		this.rectangles[0] = rectangle;
		this.editor = editor;
		this.selectionType = SelectionType.SELECTION_2D;
	}

	public EditorPart getEditor() {
		return isEmpty() ? null : editor;
	}

	public SelectionType getType() {
		return selectionType;
	}

	@Override
	public String toString() {
		if (isEmpty())
			return "Null selection";
		return selectionType.toString() + ", " + Arrays.toString(rectangles);
	}

	@Override
	public boolean isEmpty() {
		return selectionType == SelectionType.EMPTY;
	}

	@Override
	public Object getFirstElement() {
		return isEmpty() ? null : rectangles[0];
	}

	@Override
	public Iterator<Rectangle> iterator() {
		return toList().iterator();
	}

	@Override
	public int size() {
		return isEmpty() ? 0 : rectangles.length;
	}

	@Override
	public Object[] toArray() {
		return rectangles;
	}

	@Override
	public List<Rectangle> toList() {
		List<Rectangle> c = new ArrayList<Rectangle>();
		if (!isEmpty())
			Collections.addAll(c, rectangles);
		return c;
	}
}