package org.eclipse.viatra2.emf.incquery.queryexplorer.content.detail;

import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.viatra2.emf.incquery.databinding.runtime.DatabindingAdapter;
import org.eclipse.viatra2.emf.incquery.queryexplorer.content.matcher.ObservablePatternMatch;
import org.eclipse.viatra2.emf.incquery.queryexplorer.content.matcher.ObservablePatternMatcher;
import org.eclipse.viatra2.emf.incquery.queryexplorer.util.DatabindingUtil;
import org.eclipse.viatra2.emf.incquery.runtime.api.IPatternMatch;
import org.eclipse.viatra2.emf.incquery.tooling.generator.util.EMFPatternLanguageJvmModelInferrerUtil;

public class TableViewerUtil {

	EMFPatternLanguageJvmModelInferrerUtil inferrerUtil;
	
	private static TableViewerUtil instance;
	
	public static TableViewerUtil getInstance() {
		if (instance == null) {
			instance = new TableViewerUtil();
		}
		return instance;
	}
	
	protected TableViewerUtil() {
		inferrerUtil = new EMFPatternLanguageJvmModelInferrerUtil();
	}
	
	public void prepareTableViewerForObservableInput(ObservablePatternMatch match, TableViewer viewer) {
		clearTableViewerColumns(viewer);
		String[] titles = { "Parameter", "Value" };
		createColumns(viewer, titles);
		viewer.setContentProvider(new ObservableListContentProvider());
		viewer.setLabelProvider(new DetailElementLabelProvider());
		
		DatabindingAdapter<IPatternMatch> databindableMatcher = 
				DatabindingUtil.getDatabindingAdapter(match.getPatternMatch().patternName(), match.getParent().isGenerated());
		
		if (databindableMatcher == null) {
			viewer.setInput(null);
		}
		else {
			DetailObserver observer = new DetailObserver(databindableMatcher, match);
			viewer.setInput(observer);
		}
	}
	
	public void prepareTableViewerForMatcherConfiguration(ObservablePatternMatcher observableMatcher, TableViewer viewer) {
		clearTableViewerColumns(viewer);
		String[] titles = { "Parameter", "Class", "Value" };
		createColumns(viewer, titles);
		viewer.setUseHashlookup(true);
		viewer.setColumnProperties(titles);
		viewer.setContentProvider(new MatcherConfigurationContentProvider());
		viewer.setLabelProvider(new MatcherConfigurationLabelProvider());
		viewer.setCellModifier(new MatcherConfigurationCellModifier(viewer));
		
		Table table = viewer.getTable();
		CellEditor[] editors = new CellEditor[titles.length];

		editors[0] = new TextCellEditor(table);
		editors[1] = new TextCellEditor(table);
		editors[2] = new ModelElementCellEditor(table, observableMatcher.getParent().getNotifier());
		
		viewer.setCellEditors(editors);
		
		String[] parameterNames = observableMatcher.getMatcher().getParameterNames();
		MatcherConfiguration[] input = new MatcherConfiguration[parameterNames.length];
		for (int i = 0;i<parameterNames.length;i++) {
			input[i] = new MatcherConfiguration(parameterNames[i], Integer.class, "");
		}
		
//		String matchClassName = inferrerUtil.matchClassName(PatternRegistry.getInstance().getPatternByFqn(observableMatcher.getPatternName()));
//		try {
//			Class<?> clazz = Class.forName(matchClassName);
//			Object match = clazz.newInstance();
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		} catch (InstantiationException e) {
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		}
		
		viewer.setInput(input);
	}
	
	public void clearTableViewerColumns(TableViewer viewer) {
				
		if (viewer.getContentProvider() != null) {
			viewer.setInput(null);
		}
		while (viewer.getTable().getColumnCount() > 0 ) {
			viewer.getTable().getColumns()[ 0 ].dispose();
		}
		
		viewer.refresh();
	}
	
	private void createColumns(TableViewer viewer, String[] titles) {
		for (int i = 0;i<titles.length;i++) {
			createTableViewerColumn(viewer, titles[i], i);
		}
	}
	
	private TableViewerColumn createTableViewerColumn(TableViewer viewer, String title, int index) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE, index);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setResizable(true);
		column.setMoveable(true);
		column.setWidth(200);
		return viewerColumn;
	}
}
