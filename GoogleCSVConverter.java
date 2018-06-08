package beta;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

public class GoogleCSVConverter
{
	
	/*
	 * This class provides a method for converting a cvs file generated from a Google form
	 * into a ballot file where each record contains names of candidates in order of their 
	 * ranking (expected by the Ballot constructor)
	 * 
	 * The input file is expected to have a csv extension
	 * The output file this class writes will have a txt extension
	 * 
	 * If the input line from the cvs file for a ballot looks like this
	 * 5/28/2018 9:17:12,2,,1,3,,5
	 * and the candidiates are Fred, Wilma, Betty, Barney, Dino, Bammm-bamm
	 * The output file would look like this
	 * 5/28/2018 9:17:12,Betty,Fred,Barney,,Bamm-bamm
	 * because Fred is ranked 2nd, Wilma is blank, Betty was ranked 1st, Barney 3rd, no one 4th, Bamm-bamm 5th
	 */

	private static PrintStream outputFile;

	public static void convertCSVGoogleFile(String filename)
	{
		
		File outfile = new File(filename + ".txt"); 
		try
		{
			outputFile = new PrintStream(outfile);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}

		FileReader inputFile = null;
		try
		{
			inputFile = new FileReader(filename + ".csv");
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		Scanner fileScanner = new Scanner(inputFile);
		String header = fileScanner.nextLine();
		ArrayList<String> candList = getCandidateList(header);
		String line;

		while (fileScanner.hasNextLine())
		{
			line = fileScanner.nextLine();
			ArrayList<String> tokenizedRankings = VoteTools.tokenizeString(line, ',');
			String timestamp = tokenizedRankings.remove(0);
			ArrayList<String> slots = makeSlotsList(candList.size());
			int rankIndex = 0;
			for (String candidateName : candList)
			{
				if (tokenizedRankings.get(rankIndex).length() > 0) // if the ranking isn't blank
				{
					int pos = Integer.parseInt(tokenizedRankings.get(rankIndex)) - 1;
					slots.set(pos, candidateName);
				}
				rankIndex++;
			}
			writeTheConvertedBallotToOutput(timestamp, slots);
		//	System.out.println("Record: " + timestamp + " " + slots);
		}

		fileScanner.close();
	}

	private static void writeTheConvertedBallotToOutput(String timestamp, ArrayList<String> slots)
	{
		outputFile.print(timestamp);
		for (String name : slots)
		{
			outputFile.print("," + name);
		}
		outputFile.println();
	}

	private static ArrayList<String> makeSlotsList(int size)
	{
		ArrayList<String> result = new ArrayList<String>();
		for (int i = 0; i < size; i++)
		{
			result.add("");
		}
		return result;
	}

	private static ArrayList<String> getCandidateList(String header)
	{
		ArrayList<String> tokenizedHeader = VoteTools.tokenizeString(header, ',');
		tokenizedHeader.remove(0); // discard timestamp column label
		for (int i = 0; i < tokenizedHeader.size(); i++)
		{
			int openBracket = tokenizedHeader.get(i).indexOf('[');
			int closeBracket = tokenizedHeader.get(i).indexOf(']');
			String nameOnly = tokenizedHeader.get(i).substring(openBracket + 1, closeBracket);
			tokenizedHeader.set(i, nameOnly);
		}
		return tokenizedHeader;
	}
}
