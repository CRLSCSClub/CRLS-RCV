package beta;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * This class provides the tools for doing ranked choice voting
 * Given: a list of candidates, a list of ballots (containing only names on the list of
 * candidates), and the number of seats to fill
 * Three lists of candidate stacks are maintained: elected candidates (winners),
 * candidate out of the running (eliminated), and active ballots (stacks)
 * We assert after each public method that the ballots in the stacks only contain names
 * of active candidates
 * 
 * @author dmcglathery
 *
 */
public class Tabulator
{
	final static int CELL_WIDTH = 8;
	
	private static PrintStream outputFile;

	private ArrayList<String>[] reportRows;

	private ArrayList<CandidateStack> stacks; // stacks for active candidates
	private ArrayList<CandidateStack> winners;
	private ArrayList<CandidateStack> eliminated;
	private CandidateStack exhausted; // ballots that can't be moved to an active candidate
	private ArrayList<Ballot> ballots;

	private int threshold;
	private int seats;
	private int longestName;

	/**
	 * A Tabulator object performs all the operations needed to model ranked choice voting
	 * @param candList  The list of candidates on the ballots
	 * @param ballots  The ballots cast in the election
	 * @param seats  The number of seats to fill
	 */
	public Tabulator(ArrayList<String> candList, ArrayList<Ballot> ballots, int seats)
	{
		stacks = new ArrayList<CandidateStack>();
		for (int i = 0; i < candList.size(); i++)
			stacks.add(new CandidateStack(candList.get(i)));

		winners = new ArrayList<CandidateStack>();
		eliminated = new ArrayList<CandidateStack>();

		exhausted = new CandidateStack("Exhausted");
		this.ballots = ballots;

		this.seats = seats;
		threshold = ballots.size() / (seats + 1) + 1;

		initializeReport(candList);
		
		File outfile = new File("voting report" + ".txt"); 
		try
		{
			outputFile = new PrintStream(outfile);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	public void makeInitialBallotStacks()
	{
		distributeBallots(ballots);
		updateReport();
	}

	/**
	 * Candidates with no ballots, and thus none to be transfered to other
	 * candidates, are deleted from the stacks list
	 * This method is called one time, after the initial distribution of ballots
	 * and distributing of surplus ballots from potential winners in the first round.
	 * 
	 * Post condition: all remaining in the stack list have some active ballots
	 */
	public void eliminateAllWithNoBallots()
	{
		int i = 0;
		while (i < stacks.size())
		{
			if (stacks.get(i).isEmpty())
			{
				deleteCandidate(stacks.get(i).getName());
			}
			else
			{
				i++;
			}
		}
		updateReport();
	}

	/**
	 * Finds the appropriate stack to put the ballot in according to the highest
	 * ranking name. If ballot is empty it is moved to the exhausted list.
	 * Precondition: all candidates on the ballot being moved are active
	 * 
	 * @param b  ballot to be placed
	 */
	private void putInStack(Ballot b)
	{
		System.out.println("Moving ballot: " + b);
		outputFile.println("Moving ballot: " + b);
		if (b.isEmpty())
			exhausted.addBallot(b);
		else
		{
			String topName = b.getRank(0);
			stacks.get(getStack(topName)).addBallot(b);
		}
	}

	/**
	 * Returns the index of the candidates stack in the list of stacks
	 * @param name  name of the candidiate
	 * @return  index of the stack in the stacks list
	 */
	private int getStack(String name)
	{
		int i = 0;
		while (!stacks.get(i).getName().equals(name))
			i++;
		return i;
	}

	/**
	 * This method is called when a candidate is no longer an active candidate
	 * The candidate's name is removed from all active ballots
	 * 
	 * @param name  candidate's name being eliminated
	 */
	public void deleteCandidate(String name)
	{
		// go through all active ballots and remove candidate
		// redistribute ballots for this candidate
		removeNameFromActiveBallots(name);
		ArrayList<Ballot> toDistribute = stacks.get(getStack(name)).removeAllBallots();
		System.out.println(name + " is eliminated");
		outputFile.println(name + " is eliminated");
		distributeBallots(toDistribute);
		eliminated.add(stacks.remove(getStack(name)));
	}

	private void removeNameFromActiveBallots(String name)
	{
		for (CandidateStack s : stacks)
		{
			s.removeName(name);
		}
	}

	public int numberOfWinners()
	{
		return winners.size();
	}

	public boolean anyNewWinners()
	{
		for (CandidateStack s : stacks)
		{
			if (s.getBallotCount() >= threshold)
				return true;
		}
		return false;
	}

	public void declareWinners()
	{
		int i = 0;
		while (i < stacks.size())
		{
			if (stacks.get(i).getBallotCount() >= threshold)
			{
				String name = stacks.get(i).getName();
				System.out.println(name + " is elected");
				outputFile.println(name + " is elected");
				winners.add(stacks.remove(i));
				if (winners.size() < seats)
				{
					removeNameFromActiveBallots(name);
					distributeWinnerSurplus();
				}
			}
			else
			{
				i++;
			}
		}
		updateReport();
	}

	/**
	 * Takes the winner just added to the end of the winners list
	 * and distributes their surplus ballots
	 */
	public void distributeWinnerSurplus()
	{
		CandidateStack winner = winners.get(winners.size() - 1);
		String name = winner.getName();
		int totalVotes = winner.getBallotCount();
		ArrayList<Ballot> surplusBallots = winner.removeBallots(totalVotes - threshold);
		for (Ballot b : surplusBallots)
		{
			b.removeName(name);
		}
		distributeBallots(surplusBallots);
	}

	/**
	 * Distributes a given list of ballots to candidates on the active stacks list
	 * 
	 * @param toDistribute
	 *            - list of ballots to distribute (ballots only contain names of
	 *            active candidates)
	 */
	private void distributeBallots(ArrayList<Ballot> toDistribute)
	{
		System.out.println("\nDistributing ballots:");
		outputFile.println("\nDistributing ballots:");
		int count = 0;
		for (Ballot b : toDistribute)
		{
			putInStack(b);
			count++;
		}
		System.out.println("\nA total of " + count + " ballots were distributed.");
		outputFile.println("\nA total of " + count + " ballots were distributed.");
	}

	public void eliminateLastCandidate()
	{
		int minIndex = 0;
		for (int i = 1; i < stacks.size(); i++)
		{
			if (stacks.get(i).getBallotCount() < stacks.get(minIndex).getBallotCount())
				minIndex = i;
		}
		deleteCandidate(stacks.get(minIndex).getName());
		updateReport();
	}

	private void initializeReport(ArrayList<String> candList)
	{
		reportRows = new ArrayList[candList.size() + 2];
		longestName = 0;

		int i = 1;
		for (String name : candList)
		{
			reportRows[i] = new ArrayList<String>();
			reportRows[i].add(name);
			if (name.length() > longestName)
				longestName = name.length();
			i++;
		}
		reportRows[i] = new ArrayList<String>();
		reportRows[i].add(center("Exhausted", longestName));
		reportRows[0] = new ArrayList<String>();
		reportRows[0].add(center("Names", longestName));
	}

	public void printReport()
	{
		System.out.println("\nFinal results:\n");
		outputFile.println("\nFinal results:\n");
		System.out.println("Threshold = " + threshold + "\n");
		outputFile.println("Threshold = " + threshold + "\n");
		
		printHorizontalLine();
		for (int row = 0; row < reportRows.length; row++)
		{
			printRow(row);
			printHorizontalLine();
		}
		System.out.println("Winner" + ((winners.size() == 1) ? " is" : "s are") + ": " + winnerList());
		outputFile.println("Winner" + ((winners.size() == 1) ? " is" : "s are") + ": " + winnerList());
	}

	private String winnerList()
	{
		String result = "";
		for (CandidateStack winner : winners)
		{
			result += winner.getName() + ", ";
		}
		return result.substring(0, result.length() - 2);
	}

	private void printRow(int row)
	{
		System.out.print("|" + rightJustify(reportRows[row].get(0), longestName));
		outputFile.print("|" + rightJustify(reportRows[row].get(0), longestName));
		for (int col = 1; col < reportRows[row].size(); col++)
		{
			System.out.print("|" + center(reportRows[row].get(col), CELL_WIDTH));
			outputFile.print("|" + center(reportRows[row].get(col), CELL_WIDTH));
		}
		System.out.println("|");
		outputFile.println("|");
	}

	private String rightJustify(String string, int width)
	{
		String result = string;
		for (int i = 0; i < width - string.length(); i++)
			result += " ";
		return result;
	}

	private void printHorizontalLine()
	{
		System.out.print("+");
		outputFile.print("+");
		for (int i = 0; i < longestName; i++)
		{
			System.out.print("-");
			outputFile.print("-");
		}
		for (int col = 1; col < reportRows[0].size(); col++)
		{
			System.out.print("+");
			outputFile.print("+");
			for (int ch = 0; ch < CELL_WIDTH; ch++)
			{
				System.out.print("-");
				outputFile.print("-");
			}
		}
		System.out.println("+");
		outputFile.println("+");
	}

	private static String center(String text, int width)
	{
		if (text.length() > width)
			return text.substring(0, width - 1) + ">";
		String centered = "";
		int rightSpaces = (width - text.length()) / 2;
		for (int c = 0; c < rightSpaces; c++)
			centered += " ";
		centered = text + centered;
		for (int c = centered.length(); c < width; c++)
			centered = " " + centered;
		return centered;
	}

	/**
	 * Creates the results grid
	 */
	public void updateReport()
	{
		// add the column heading
		int round = reportRows[0].size();
		reportRows[0].add(round + "");
		// for each name in table, get current ballot count from tabulator
		for (int row = 1; row < reportRows.length-1; row++)
		{
			String name = reportRows[row].get(0);
			int count = getCount(reportRows[row].get(0));
			if (inGroup(winners, name))
			{
				reportRows[row].add(count + " *");
			}
			else if (inGroup(eliminated, name))
			{
				reportRows[row].add("E");
			}
			else
			{
				reportRows[row].add(count + "");
			}
		}
		reportRows[reportRows.length-1].add(exhausted.getBallotCount() + "");
	}

	private boolean inGroup(ArrayList<CandidateStack> group, String name)
	{
		for (CandidateStack w : group)
		{
			if (w.getName().equals(name))
				return true;
		}
		return false;
	}

	private int getCount(String name)
	{
		for (CandidateStack w : winners)
		{
			if (w.getName().equals(name))
				return w.getBallotCount();
		}
		for (CandidateStack s : stacks)
		{
			if (s.getName().equals(name))
				return s.getBallotCount();
		}
		for (CandidateStack e : eliminated)
		{
			if (e.getName().equals(name))
				return e.getBallotCount();
		}
		return -1;
	}

	public boolean emptyStacksExist()
	{
		for (CandidateStack s : stacks)
		{
			if (s.isEmpty())
			{
				return true;
			}
		}
		return false;
	}

	public int numberOfActiveCandidiates()
	{
		return stacks.size();
	}

}
