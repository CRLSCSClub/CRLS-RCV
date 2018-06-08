package beta;

import java.util.ArrayList;
/**
 * A CandidateStack is like a pile of ballots for a given candidate
 * @author doug
 *
 */
public class CandidateStack
{
	private String candName;
	private ArrayList<Ballot> ballots;

	public CandidateStack(String name)
	{
		candName = name;
		ballots = new ArrayList<Ballot>();
	}
	
	public void addBallot(Ballot b)
	{
		ballots.add(b);
	}
	
	/**
	 * Removes n ballots randomly from the ballots list and returns
	 * the list of removed ballots. Only ballots that contain votes
	 * for other candidates are removable.
	 * This will remove n ballots if at least n are movable, otherwise
	 * it will remove all movable ballots.
	 * @param n - the number of ballots to remove
	 * @return - the list of removed ballots
	 */
	public ArrayList<Ballot> removeBallots(int n)
	{
		ArrayList<Ballot> removed = new ArrayList<Ballot>();
		ArrayList<Integer> indicesOfMovableBallots = getMovabaleBallotIndices();
		for (int i = 0; i < n && indicesOfMovableBallots.size() > 0; i++)
		{
			int randIndex = (int)(Math.random() * indicesOfMovableBallots.size());
			int ballotIndex = indicesOfMovableBallots.remove(randIndex);
			Ballot toMove = ballots.remove(ballotIndex);
			toMove.removeTop(); // this is the name of the stack the ballot is currently in
			removed.add(toMove);
			indicesOfMovableBallots = getMovabaleBallotIndices();
		}
		return removed;
	}
	
	private ArrayList<Integer> getMovabaleBallotIndices()
	{
		ArrayList<Integer> indicesOfMovableBallots = new ArrayList<Integer>();
		for (int i = 0; i < ballots.size(); i++)
		{
			if (ballots.get(i).getLength() > 1) // the ballot is movable
			{
				indicesOfMovableBallots.add(i);
			}
		}
		return indicesOfMovableBallots;
	}

	/**
	 * Gets the total number of ballots in this stack
	 * @return
	 */
	public int getBallotCount()
	{
		return ballots.size();
	}
	
	public boolean isEmpty()
	{
		return ballots.size() == 0;
	}
	
	public String getName()
	{
		return candName;
	}

	public void removeName(String name)
	{
		for (Ballot b : ballots)
			b.removeName(name);
	}

	public ArrayList<Ballot> removeAllBallots()
	{
		ArrayList<Ballot> removed = new ArrayList<Ballot>();
		while (!ballots.isEmpty())
		{
			removed.add(ballots.remove(0));
		}
		return removed;
	}
}
