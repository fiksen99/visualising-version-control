package com.imperial.fiksen.codesimilarity.compare;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.compare.ICompareFilter;
import org.eclipse.compare.internal.CompareContentViewerSwitchingPane;
import org.eclipse.compare.internal.CompareMessages;
import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.compare.internal.DocLineComparator;
import org.eclipse.compare.internal.MergeViewerContentProvider;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.compare.internal.merge.DocumentMerger;
import org.eclipse.compare.internal.merge.DocumentMerger.Diff;
import org.eclipse.compare.internal.merge.DocumentMerger.IDocumentMergerInput;
import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

@SuppressWarnings("restriction")
public class ParseTreeDocumentMerger extends DocumentMerger {
	
	private IDocumentMergerInput fInput;
	
	public ParseTreeDocumentMerger(IDocumentMergerInput input) {
		super(input);
		this.fInput = input;
	}
	

	
	/**
	 * Perform a two level 2- or 3-way diff.
	 * The first level is based on line comparison, the second level on token comparison.
	 * @throws CoreException 
	 */
	public void doDiff() throws CoreException {
						
		fChangeDiffs= new ArrayList();
		IDocument lDoc = getDocument(MergeViewerContentProvider.LEFT_CONTRIBUTOR);
		IDocument rDoc = getDocument(MergeViewerContentProvider.RIGHT_CONTRIBUTOR);
		
		if (lDoc == null || rDoc == null)
			return;
			
		Position lRegion= getRegion(MergeViewerContentProvider.LEFT_CONTRIBUTOR);
		Position rRegion= getRegion(MergeViewerContentProvider.RIGHT_CONTRIBUTOR);
		
		IDocument aDoc = null;
		Position aRegion= null;
		if (isThreeWay() && !isIgnoreAncestor()) {
			aDoc= getDocument(MergeViewerContentProvider.ANCESTOR_CONTRIBUTOR);
			aRegion= getRegion(MergeViewerContentProvider.ANCESTOR_CONTRIBUTOR);
		}
		
		resetPositions(lDoc);
		resetPositions(rDoc);
		resetPositions(aDoc);
		
		boolean ignoreWhiteSpace= isIgnoreWhitespace();		
		ICompareFilter[] compareFilters = getCompareFilters();

		DocLineComparator sright = new DocLineComparator(rDoc,
				toRegion(rRegion), ignoreWhiteSpace, compareFilters,
				MergeViewerContentProvider.RIGHT_CONTRIBUTOR);
		DocLineComparator sleft = new DocLineComparator(lDoc,
				toRegion(lRegion), ignoreWhiteSpace, compareFilters,
				MergeViewerContentProvider.LEFT_CONTRIBUTOR);
		DocLineComparator sancestor = null;
		if (aDoc != null) {
			sancestor = new DocLineComparator(aDoc, toRegion(aRegion),
					ignoreWhiteSpace, compareFilters,
					MergeViewerContentProvider.ANCESTOR_CONTRIBUTOR);
			/*if (isPatchHunk()) {
				if (isHunkOnLeft()) {
					sright= new DocLineComparator(aDoc, toRegion(aRegion), ignoreWhiteSpace);
				} else {
					sleft= new DocLineComparator(aDoc, toRegion(aRegion), ignoreWhiteSpace);
				}
			}*/
		}

		final Object[] result= new Object[1];
		final DocLineComparator sa= sancestor, sl= sleft, sr= sright;
		IRunnableWithProgress runnable= new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				monitor.beginTask(CompareMessages.DocumentMerger_0, 100);
				try {
					result[0]= RangeDifferencer.findRanges(monitor, sa, sl, sr);
				} catch (OutOfMemoryError ex) {
					System.gc();
					throw new InvocationTargetException(ex);
				}
				if (monitor.isCanceled())	{ // canceled
					throw new InterruptedException();
				}
				monitor.done();
			}
		};
		
		RangeDifference[] e= null;
		try {
			getCompareConfiguration().getContainer().run(true, true, runnable);
			e= (RangeDifference[]) result[0];
		} catch (InvocationTargetException ex) {
			// something bad happened!
			throw new CoreException(new Status(IStatus.ERROR, CompareUIPlugin.PLUGIN_ID, 0, CompareMessages.DocumentMerger_1, ex.getTargetException()));
		} catch (InterruptedException ex) {
			// more bad happened!
			return;
		}
		fInput.getCompareConfiguration().setProperty(
				CompareContentViewerSwitchingPane.OPTIMIZED_ALGORITHM_USED,
				new Boolean(false));

		ArrayList newAllDiffs = new ArrayList();
		for (int i= 0; i < e.length; i++) {
			RangeDifference es= e[i];
			
			int ancestorStart= 0;
			int ancestorEnd= 0;
			if (sancestor != null) {
				ancestorStart= sancestor.getTokenStart(es.ancestorStart());
				ancestorEnd= getTokenEnd2(sancestor, es.ancestorStart(), es.ancestorLength());
			}
			
			int leftStart= sleft.getTokenStart(es.leftStart());
			int leftEnd= getTokenEnd2(sleft, es.leftStart(), es.leftLength());
			
			int rightStart= sright.getTokenStart(es.rightStart());
			int rightEnd= getTokenEnd2(sright, es.rightStart(), es.rightLength());

			/*if (isPatchHunk()) {
				if (isHunkOnLeft()) {
					rightStart = rightEnd = getHunkStart();
				} else {
					leftStart = leftEnd = getHunkStart();
				}
			}*/

			Diff diff= null;
			
			newAllDiffs.add(diff);	// remember all range diffs for scrolling
	
			if (isPatchHunk()) {
				if (useChange(diff)) {
					recordChangeDiff(diff);
				}
			} else {
				if (ignoreWhiteSpace || useChange(es.kind())) {
					
					// Extract the string for each contributor.
					String a= null;
					if (sancestor != null)
						a= extract2(aDoc, sancestor, es.ancestorStart(), es.ancestorLength());
					String s= extract2(lDoc, sleft, es.leftStart(), es.leftLength());
					String d= extract2(rDoc, sright, es.rightStart(), es.rightLength());
				
					// Indicate whether all contributors are whitespace
					if (ignoreWhiteSpace 
							&& (a == null || a.trim().length() == 0) 
							&& s.trim().length() == 0 
							&& d.trim().length() == 0) {
						diff.fIsWhitespace= true;
					}
					
					// If the diff is of interest, record it and generate the token diffs
					if (useChange(diff)) {
						recordChangeDiff(diff);
						if (s.length() > 0 && d.length() > 0) {
							if (a == null && sancestor != null)
								a= extract2(aDoc, sancestor, es.ancestorStart(), es.ancestorLength());
							if (USE_MERGING_TOKEN_DIFF)
								mergingTokenDiff(diff, aDoc, a, rDoc, d, lDoc, s);
							else
								simpleTokenDiff(diff, aDoc, a, rDoc, d, lDoc, s);
						}
					}
				}
			}
		}
		fAllDiffs = newAllDiffs;
	}

}
