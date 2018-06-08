package beta;

import java.util.ArrayList;

/**
 * A ballot contains a list of candidiates in the order of preference (highest
 * preference at the head of the list)
 * The constructor expects input to be a String in comma-separated format, that
 * may or may not have a header field at the beginning (like a timestamp), followed
 * by a list of names in order of preference
 * 
 * @author dmcglathery
 *
 */
public class Ballot
{
	final static boolean HAS_HEADER_FIELD = true;

	private String headerField;
	private ArrayList<String> candidateNamesByRank;

	/**
	 * 
	 * @param lineFromFile
	 */
	public Ballot(String lineFromFile)
	{
		char delimiter = ',';
		candidateNamesByRank = new ArrayList<String>();
		ArrayList<String> tokenizedLine = VoteTools.tokenizeString(lineFromFile, delimiter);
		int firstCandidateIndex = 0;
		if (HAS_HEADER_FIELD)
		{
			headerField = tokenizedLine.get(0);
			firstCandidateIndex = 1;
		}

		for (int i = firstCandidateIndex; i < tokenizedLine.size(); i++)
		{
			if (tokenizedLine.get(i).length() != 0)
			{
				candidateNamesByRank.add(tokenizedLine.get(i));
			}
		}
		if (!isValidBallot())
		{
			throw new IllegalArgumentException("Invalid ballot");
		}
	}

	/**
	 * An invalid index is one with names listed more than once
	 * @return false if any name appears more than once, otherwise true
	 */
	private boolean isValidBallot()
	{
		ArrayList<String> checker = new ArrayList<String>();
		for (String s : candidateNamesByRank)
		{
			if (checker.contains(s))
				return false;
			else 
				checker.add(s);
		}
		return true;
	}

	public int rankingOf(String name)
	{
		return this.candidateNamesByRank.indexOf(name);
	}

	public String getRank(int rank)
	{
		return this.candidateNamesByRank.get(rank);
	}

	public int getLength()
	{
		return this.candidateNamesByRank.size();
	}

	public void removeTop()
	{
		this.candidateNamesByRank.remove(0);
	}

	public void removeName(String name)
	{
		int i = this.candidateNamesByRank.indexOf(name);
		if (i != -1)
			this.candidateNamesByRank.remove(i);
	}

	public boolean isEmpty()
	{
		return candidateNamesByRank.isEmpty();
	}

	public String toString()
	{
		return headerField + ":" + candidateNamesByRank;
	}
}
