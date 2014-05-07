package utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import comparison.SimilarityAnalyser;

public class ResultsPrinter {
	
	private static final int MAX_LENGTH = 20;
	
	public static void PrintMap(Map<String, Double> results) {
		Stack<String> s = new Stack<String>();
		Map<String, Map<String, Double>> res = new HashMap<String, Map<String, Double>>();
		Set<String> projects = new HashSet<String>();
		for(Entry<String, Double> key : results.entrySet()) {
			String combined = key.getKey();
			String[] projectNames = combined.split(SimilarityAnalyser.RESULTS_SEPARATOR);
			projects.add(projectNames[0]);
			projects.add(projectNames[1]);
			Map<String, Double> m1 = res.get(projectNames[0]);
			if(m1 == null) {
				m1 = new HashMap<String, Double>();
				res.put(projectNames[1], m1);
			}
			m1.put(projectNames[1], key.getValue());
			Map<String, Double> m2 = res.get(projectNames[1]);
			if(m2 == null) {
				m2 = new HashMap<String, Double>();
				res.put(projectNames[1], m2);
			}
			m2.put(projectNames[0], key.getValue());
			if(!s.contains(projectNames[0])) {
				s.push(projectNames[0]);
			}
			if(!s.contains(projectNames[1])) {
				s.push(projectNames[1]);
			}
		}
		LinkedList<String> order = new LinkedList<String>();
		System.out.print("\t\t");
		for(String user:projects) {
			order.add(user);
			System.out.println(user + "\t");
		}
		
		//TODO:print
		
	}

	public static void printScore(double[][] scores, List<String> orderedProjects) {
		if(scores == null || 
			scores[0].length != scores.length || 
			orderedProjects.size() != scores.length) {
			throw new IllegalArgumentException("the list of names must be the same length as the scores");
		}
		System.out.print("            |");
		for(int i = 0; i < scores.length ; i++) {
			String name = orderedProjects.get(i);
			System.out.print(name);
			for(int j = name.length(); j < MAX_LENGTH; j++) {
				System.out.print(" ");
			}
			System.out.print("|");
		}
		System.out.println();
		for(int i = 0; i < MAX_LENGTH; i++) {
			System.out.print("_");
		}
		System.out.print("|");
		for(int i = 0; i < scores.length; i++) {
			for(int j = 0; j < MAX_LENGTH; j++) {
				System.out.print("_");
			}
			System.out.print("|");
		}
		System.out.println();
		for(int i = 0; i < scores.length; i++) {
			String name = orderedProjects.get(i);
			System.out.print(name);
			for(int j = name.length(); j < MAX_LENGTH; j++) {
				System.out.print(" ");
			}
			System.out.print("|");
			for(int j = 0; j < scores[i].length; j++) {
				String score = Double.toString(scores[i][j]);
				if(score.length() < MAX_LENGTH) {
					System.out.print(score);
					for(int k = score.length(); k < MAX_LENGTH; k++) {
						System.out.print(" ");
					}
				} else {
					System.out.print(score.substring(0, MAX_LENGTH));
				}
				System.out.print("|");
			}
			System.out.println();
		}
	}

}
