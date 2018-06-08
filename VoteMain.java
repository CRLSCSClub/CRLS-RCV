package beta;

import java.util.ArrayList;

/**
 * This is the main class for a tool to use in conjunction with a Google form (see usage notes below).
 * This will run a single transferable vote form of ranked choice voting based on the data
 * collected in the form. It can be used in multi-seat or single-seat elections (for single-seat
 * elections it is equivalent to the instant-runoff from of RCV).
 * This tool is composed of the following classes:
 * 	Ballot.java
 * 	BallotReader.java
 * 	CandidateStack.java
 * 	GoogleCSVConverter.java
 * 	Tabulator.java
 * 	VoteMain.java (this file)
 * 	VoteTools.java
 * 	
 * @author dmcglathery
 *
 * Note: to be maintained by the CRLS Computer Science Club
 */
public class VoteMain
{
	/*
	 * Usage notes:
	 * 1. Setup a Google form with a multiple choice grid (square) question
	 *    a. Candidates go down the rows, rankings go across columns
	 *    b. Do not require a response in each row
	 *    c. In settings (gear icon) restrict to CPS users *and* limit to one response
	 *    d. In preferences (three dots) limit to one response per column
	 * 2. Share and have the vote
	 * 3. Once closed goto the responses and view as spreadsheet
	 *    (each ballot will appear as a row and start with a timestamp followed by numbers for rankings)
	 * 4. Download the sheet in comma separated values form (.csv)
	 * 5. Move this csv file to this project's folder in workspace
	 * 6. Set the number of seats to be filled below
	 * 7. Set filename below to the first part of the name of the csv file
	 * 8. Run this file
	 */
	public static void main(String[] args)
	{
		int seatsToBeFilled = 3;
		String filename = "iceCreamTest";
		
		GoogleCSVConverter.convertCSVGoogleFile(filename);
		ArrayList<String> candidates = VoteTools.makeCandidateList(filename + ".txt");
		BallotReader getBallots = new BallotReader(filename + ".txt");
		ArrayList<Ballot> ballots = getBallots.makeBallotList();
		Tabulator tallier = new Tabulator(candidates, ballots, seatsToBeFilled);
		
		/*
		 * The ranked choice voting method:
		 * 1. Make stacks of ballots according to the first rank on each ballot
		 * 2. Declare as a winner anyone who is already over the threshold and
		 *    distribute their surplus ballots
		 * 3. If there are any seats left and there are candidates with no first
		 *    choice ballots, then those candidates are eliminated
		 * 4. As long as there are seats to fill
		 *    a. if there are new winners, distribute their surplus ballots
		 *    b. otherwise, eliminate the lowest candidate and distribute their ballots
		 */
		tallier.makeInitialBallotStacks();
		if (tallier.anyNewWinners())
		{
			tallier.declareWinners();
		}
		
		if(tallier.numberOfWinners() < seatsToBeFilled && tallier.emptyStacksExist())
			tallier.eliminateAllWithNoBallots();
		
		while(tallier.numberOfWinners() < seatsToBeFilled && tallier.numberOfActiveCandidiates() > 0)
		{
			if (tallier.anyNewWinners())
			{
				tallier.declareWinners();
			}
			else
			{
				tallier.eliminateLastCandidate();
			}
		}
		System.out.println("\nRanked Choice Voting simulation complete");
		if (tallier.numberOfWinners() < seatsToBeFilled)
		{
			System.out.println("Election could not be completed because not enough candidates reached the threshold.");
		}
		tallier.printReport();
	}

}
