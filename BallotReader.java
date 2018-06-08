package beta;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This class reads a text file with the assumption that each line in the text
 * file represents a ballot with candidates listed in order of preference from
 * highest to lowest and hands off the creating of a ballot from the line of
 * text to the ballot constructor
 * 
 * @author doug
 *
 */
public class BallotReader
{
	final static boolean HAS_HEADER_RECORD = false;

	private String filename;

	public BallotReader(String filename)
	{
		this.filename = filename;
	}

	public ArrayList<Ballot> makeBallotList()
	{
		FileReader file = null;
		try
		{
			file = new FileReader(filename);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		Scanner fileScanner = new Scanner(file);
		if (HAS_HEADER_RECORD)
		{
			fileScanner.nextLine(); // throw away header (first line)
		}
		String line;
		ArrayList<Ballot> ballots = new ArrayList<Ballot>();

		while (fileScanner.hasNextLine())
		{
			line = fileScanner.nextLine();
			try
			{
				Ballot b = new Ballot(line);
				ballots.add(b);
			}
			catch (Exception e)
			{
				System.out.println("This ballot is considered invalid: " + line);
			}
		}

		fileScanner.close();

		return ballots;
	}

}
