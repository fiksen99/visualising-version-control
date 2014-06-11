package com.imperial.fiksen.codesimilarity.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class OrangeUtils {
	
	private static Process orange = null;
	
	public static final String PATH_TO_RESOURCES = System.getProperty("java.io.tmpdir");
	public static final String SAVE_FILE = "similarity_results.tab";
	
	private static final String ORANGE_LOCATION = "/Applications/Orange.app/";
	private static final String ORANGE_PROJECT_FILE = System.getProperty("java.io.tmpdir") + "results.ows";
	
	private static final String GENERIC_PROJECT = "<?xml version='1.0' encoding='utf-8'?>\n"
			+ "<scheme description=\"\" title=\"test\" version=\"2.0\">\n"
			+ "	<nodes>\n"
			+ "		<node id=\"0\" name=\"Distance File\" position=\"(150, 150)\" project_name=\"Orange\" qualified_name=\"Orange.OrangeWidgets.Unsupervised.OWDistanceFile.OWDistanceFile\" title=\"Distance File\" version=\"\" />\n"
			+ "		<node id=\"1\" name=\"Distance Map\" position=\"(300, 120)\" project_name=\"Orange\" qualified_name=\"Orange.OrangeWidgets.Unsupervised.OWDistanceMap.OWDistanceMap\" title=\"Distance Map\" version=\"\" />\n"
			+ "	</nodes>\n"
			+ "	<links>\n"
			+ "		<link enabled=\"true\" id=\"0\" sink_channel=\"Distances\" sink_node_id=\"1\" source_channel=\"Distances\" source_node_id=\"0\" />\n"
			+ "	</links>\n"
			+ "	<annotations />\n"
			+ "	<thumbnail />\n"
			+ "	<node_properties>\n"
			+ "		<properties format=\"literal\" node_id=\"0\">{'recentFiles': [u'"+PATH_TO_RESOURCES+SAVE_FILE+"', u'/']}</properties>\n"
			+ "		<properties format=\"literal\" node_id=\"1\">{'CutEnabled': False, 'ShowItemsInBalloon': 1, '__settingsDataVersion': None, 'selectedSchemaIndex': 0, 'Sort': 4, 'ShowLegend': 1, 'ShowLabels': 1, 'savedGrid': 1, 'colorSettings': [['temp', [[('unknown', 4288716964L), ('overflow', 4278190080L), ('underflow', 4294967295L), ('background', 4294967295L), ('cellOutline', 4288716964L), ('selection', 4278190080L)], [], [], [('palette', (4278190335L, 4294967040L, True, [(4294901760L, True), (4278190080L, False), (4278255360L, False)]))]]]], 'Grid': False, 'SquareCells': True,  'widgetShown': 0, 'Merge': 1.0, 'SendOnRelease': 1, 'Gamma': 1.0, 'ShowBalloon': 1}</properties>\n"
			+ "	</node_properties>\n"
			+ "</scheme>\n";
	
	public static void createProcess() {
		if( orange == null ) {
			try {
				startOrange();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				orange.exitValue(); //reopen if the process has exited (in any state)	
				startOrange();
			} catch(IllegalThreadStateException e) {
				//nop - haven't terminated previous process
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void startOrange() throws IOException {
		PrintWriter writer = new PrintWriter(ORANGE_PROJECT_FILE, "UTF-8");
		writer.print(GENERIC_PROJECT);
		writer.close();
		orange = Runtime.getRuntime().exec("open " + ORANGE_LOCATION + " --args " + ORANGE_PROJECT_FILE);	
	}

}
