package utils;

import java.util.List;

public class ResultsPrinter {
	
	private static final int MAX_LENGTH = 20;
	
	public static void printScore(double[][] scores, List<String> orderedProjects) {
		if(scores == null || 
			scores[0].length != scores.length || 
			orderedProjects.size() != scores.length) {
			throw new IllegalArgumentException("the list of names must be the same length as the scores");
		}
		fillWhiteSpace(0);
		for(int i = 0; i < scores.length ; i++) {
			String name = orderedProjects.get(i);
			System.out.print(name);
			fillWhiteSpace(name.length());
		}
		System.out.println();
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

}
