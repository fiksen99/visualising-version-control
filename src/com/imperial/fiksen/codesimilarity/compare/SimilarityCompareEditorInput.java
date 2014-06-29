/*******************************************************************************
 * Copyright (C) 2007, Robin Rosenberg <robin.rosenberg@dewire.com>
 * Copyright (C) 2008, Roger C. Soares <rogersoares@intelinet.com.br>
 * Copyright (C) 2013, Robin Stocker <robin@nibor.org>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.imperial.fiksen.codesimilarity.compare;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.ICompareContainer;
import org.eclipse.compare.IEditableContent;
import org.eclipse.compare.IResourceProvider;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffContainer;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.history.FileRevisionTypedElement;
import org.eclipse.team.internal.ui.synchronize.EditableSharedDocumentAdapter.ISharedDocumentAdapterListener;
import org.eclipse.team.internal.ui.synchronize.LocalResourceSaveableComparison;
import org.eclipse.team.internal.ui.synchronize.LocalResourceTypedElement;
import org.eclipse.team.ui.synchronize.SaveableCompareEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.SaveablesLifecycleEvent;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * The input provider for the compare editor when working on resources under Git
 * control.
 */
public class SimilarityCompareEditorInput extends SaveableCompareEditorInput {

	private ITypedElement left;
	private ITypedElement right;

	/**
	 * Creates a new CompareFileRevisionEditorInput.
	 * 
	 * @param left
	 * @param right
	 * @param page
	 */
	public SimilarityCompareEditorInput(ITypedElement left, ITypedElement right,
			CompareConfiguration config, IWorkbenchPage page) {
		super(config, page);
		this.left = left;
		this.right = right;
	}

	private boolean isLeftEditable(ICompareInput input) {
		return false;
	}

	private boolean isRightEditable(ICompareInput input) {
		return false;
	}

	private IResource getResource() {
		if (left instanceof IResourceProvider) {
			IResourceProvider resourceProvider = (IResourceProvider) left;
			return resourceProvider.getResource();
		}
		return null;
	}

	private ICompareInput createCompareInput() {
		return compare(left, right);
	}

	private DiffNode compare(ITypedElement actLeft, ITypedElement actRight) {
		return new DiffNode(actLeft, actRight);
	}

	private void initLabels(ICompareInput input) {
		CompareConfiguration cc = getCompareConfiguration();
			cc.setLeftLabel(left.getName());
			cc.setRightLabel(right.getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.compare.CompareEditorInput#getToolTipText() TODO:
	 * tooltip
	 */
	public String getToolTipText() {
		return "Comparison of " + left.getName() + " and " + right.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.compare.CompareEditorInput#getTitle()
	 */
	public String getTitle() {
		return "Comparison of " + left.getName() + " and " + right.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.compare.CompareEditorInput#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IFile.class || adapter == IResource.class) {
			return getResource();
		}
		return super.getAdapter(adapter);
	}

	@Override
	protected void fireInputChange() {
		// TODO: ?maybe, don't want a change
		// have the diff node notify its listeners of a change
		// ((NotifiableDiffNode) getCompareResult()).fireChange();
	}

	@Override
	protected ICompareInput prepareCompareInput(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		ICompareInput input = createCompareInput();
		getCompareConfiguration().setLeftEditable(isLeftEditable(input));
		getCompareConfiguration().setRightEditable(isRightEditable(input));
		initLabels(input);
		setTitle("Similarity Comparison of " + left.getName() + " and " + right.getName());
		return input;
	}

	@Override
	public void registerContextMenu(MenuManager menu,
			final ISelectionProvider selectionProvider) {
		super.registerContextMenu(menu, selectionProvider);
	}
}
