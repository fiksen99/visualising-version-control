package com.imperial.fiksen.codesimilarity.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResultsPrinter {
	
	private static final int MAX_LENGTH = 20;
	
	public static void printScore(double[][] scores, List<String> orderedProjects) {
		if(scores == null || 
			scores[0].length != scores.length || 
			orderedProjects.size() != scores.length) {
			throw new IllegalArgumentException("the list of names must be the same length as the scores");
		}
		printColNames(scores, orderedProjects);
		fillWhiteSpace(0, '_');
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
			fillWhiteSpace(name.length());
			for(int j = 0; j < scores[i].length; j++) {
				String score = String.format("%1$,.3f", scores[i][j]);
				if(score.length() < MAX_LENGTH) {
					System.out.print(score);
					fillWhiteSpace(score.length());
				} else {
					System.out.print(score.substring(0, MAX_LENGTH));
					System.out.print("|");
				}
			}
			System.out.println();
		}
		printColNames(scores, orderedProjects);
	}
	
	private static void printColNames(double[][] scores,
			List<String> orderedProjects) {
		fillWhiteSpace(0);
		for(int i = 0; i < scores.length ; i++) {
			String name = orderedProjects.get(i);
			System.out.print(name);
			fillWhiteSpace(name.length());
		}
		System.out.println();		
	}

	private static void fillWhiteSpace(int startingIndex) {
		fillWhiteSpace(startingIndex, ' ');
	}
	
	private static void fillWhiteSpace(int startingIndex, char fillChar) {
		for(int i = startingIndex; i < MAX_LENGTH; i++) {
			System.out.print(fillChar);
		}
		System.out.print("|");		
	}

	public static void tsvPrint(double[][] scores, List<String> orderedProjects) {
		System.out.print("[");
		for(String project: orderedProjects) {
			System.out.print("'"+project+"', ");
		}
		System.out.println("]");
		System.out.print("[");
		for(String project: orderedProjects) {
			System.out.print("'"+project+"', ");
		}
		System.out.println("]");
		System.out.println(orderedProjects.size());
		System.out.print("[");
		for(int i = 0; i < scores.length; i++) {
			System.out.print("[");
			for(int j = 0; j <i; j++) {
				System.out.print(scores[i][j] + ",");
//				if(j == i) {
//					System.out.println(i + "\t" + orderedProjects.get(i) + "\t" + j + "\t" + orderedProjects.get(j) + "\t" + scores[i][j]);
//				} else {
//					System.out.println(i + "\t" + orderedProjects.get(i) + "\t" + j + "\t" + orderedProjects.get(j) + "\t" + scores[i][j]);
//					System.out.println(j + "\t" + orderedProjects.get(j) + "\t" + i + "\t" + orderedProjects.get(i) + "\t" + scores[i][j]);
//				}
			}
			System.out.println("],");
		}
		System.out.println("]");
	}
	
	public static void orangeDistanceTablePrint(double[][] scores, List<String> orderedProjects, Set<Integer> toIgnore) {
		System.out.println(scores.length-toIgnore.size() + " labeled");
		for(int i = 0; i < scores.length; i++) {
			if(!toIgnore.contains(i)) {
				System.out.print(orderedProjects.get(i) + "\t");
				for(int j = 0; j <= i; j++) {
					if(!toIgnore.contains(j)) {
						String score = String.format("%1$,.6f", scores[i][j]);
						System.out.print(score);
						if(i!=j)
							System.out.print("\t");
					}
				}
				System.out.println();
			}
		}
	}
	
	public static void matlabDistanceTablePrint(double[][] scores, List<String> orderedProjects, Set<Integer> toIgnore) {
		for(int i = 0; i < scores.length; i++) {
			if(!toIgnore.contains(i)) {
				System.out.print("\t");
				for(int j = 0; j < scores[i].length; j++) {
					if(!toIgnore.contains(j)) {
						String score = String.format("%1$,.6f", scores[i][j]);
						System.out.print(score);
						if(j!=scores[i].length-1)
							System.out.print("\t");
					}
				}
				System.out.println();
			}
		}
	}
	
	public static void printOneUser(double[][] scores, List<String> orderedProjects, Set<Integer> toIgnore) {
		String user = "java_recusion_ma8512";
		int i = orderedProjects.indexOf(user);
		//System.out.println(user);
		for(int j = 0; j < orderedProjects.size(); j++) {
			if(!toIgnore.contains(j)) {
				System.out.print(orderedProjects.get(j) + "\t");
				String score = String.format("%1$,.6f", 1-scores[i][j]);
				System.out.println(score);
			}
		}
		System.out.println(orderedProjects.size()-toIgnore.size());
	}

	public static void print(double[][] scores, List<String> orderedProjects,
			Set<Integer> toIgnore) {
		printOneUser(scores, orderedProjects, toIgnore);
	}
	
}
