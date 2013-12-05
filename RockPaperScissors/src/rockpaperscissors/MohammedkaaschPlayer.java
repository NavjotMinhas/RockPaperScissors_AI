package rockpaperscissors;

/*
 * Roshambo competition bot "Mohammed Kaasch", by Tim Harbers (NL), November 2003.
 * 
 * Mohammed Kaasch is partly based on Dan Egnor's "Iocaine Powder" and "Urza", made by Enno
 * Peter and Martijn Muurman. Thanks to them for providing lots of good ideas.
 * 
 * 
 * Working of Mohammed Kaasch: ---------------------------
 * 
 * At its highest level, where the important decisions are made, Mohammed Kaasch uses 3 basic
 * strategies. These strategies can switch over time, but not very easily, which make them quite
 * solid.
 * 
 * The first basic strategy is 'stratmove', the history-search pattern, based on the ideas first
 * introduced in Iocaine Powder. It is created against simple bots which play in a certain fixed
 * pattern, like rotating bots, by looking at the history and find from different periods the
 * longest same pattern which has been played recently. The working of this strategy, creating
 * an amount of 60 different searches and checking which is best, is like Urza, but with a
 * little less depth and has a few extra tools, which help finding a correct pattern quicker.
 * Further, like most bots, it adapted 'Sicilian Reasoning', adding '+1' and '+2' to the
 * original move, also based on the ideas of Dan Egnor.
 * 
 * The second basic strategy, 'bayesmove', is known as the 'naive Bayes classifier', which uses
 * Bayesian learning, named after the mathematician Thomas Bayes (1702) because it is based on
 * his theory. This strategy looks for many kinds of game statistics, tries to discover any
 * strange numbers and making use of it. Bots like Foxtrot, Switch and Flat are supposed to be
 * recognized in a short period by it. This strategy has two options: the specialized version,
 * which looks at one statistic at a time, and the general version, which looks at all
 * statistics in the same time and is better able to generalize if a lot of random moves are
 * involved.
 * 
 * Finally the third strategy is the most difficult one, because it tries to predict other
 * competition bots, which is very difficult, because they try very hard to be unpredictable.
 * It's impossible to do this, without making certain assumptions, on which this strategy is
 * based. To beat a competition bot, it is necessary to think the same way as them. The most
 * important assumption is that the opponent uses different strategies like Iocaine and Urza and
 * decides in a logical way which strategy to use. Moreover, it assumes that the opponent has
 * CERTAIN strategies in common with Mohammed Kaasch, which is likely, because these bots want
 * to defeat bots like 'Foxtrot' and 'Switch', and therefore have strategies which check these
 * kind of things too. Mohammed Kaasch has 65 POSSIBLE strategies the opponent might use. Along
 * the game, it discards some strategy of which it is almost certain that the opponent doesn't
 * know them. Also, when it thinks the opponent is using a strategy it doesn't know itself, it
 * tries to 'lure' the opponent to play a known strategy, by playing moves according to the
 * closest one. The initial bias is almost the same as Urza's, but can be switched and is usable
 * on many other choosing methods. This strategy is tested very often on Urza and defeats it
 * with an average of about 50. Unfortunately, Iocaine has too less in common, probably because
 * it was not made for tournaments with that much 'dummy bots' and is immune against it. I'm
 * very curious how well it will work in the real tournament.
 * -------------------------------------------------------------------------------- Tim Harbers
 */

/**
 * http://www.cs.unimaas.nl/~donkers/games/roshambo03/
 * TODO - reset fields
 */
public class MohammedkaaschPlayer{

int game;

int current_score;

int[][] strategymove;

static final int ME = 0;

static final int OP = 1;

static final int STRATNUMBER = 61;

int[] my_history = new int[ 1 ];

int[] opp_history;

int[] best_history;

int[] worst_history;

//historysums
int[] my_historysum;

int[] opp_historysum;

int longestseq;

//metametaniveau
int bayeshis;

int strathis;

int metahis;

int bayesmove;

int stratmove;

int metamove;

//pi/debruijn/euler-detector
int patternsearchindex;

//counter statistics
int merock, mepaper, mescissors, oprock, oppaper, opscissors;

//bayesfactors
int[][] bayesmylastmove;

int[][] bayesoplastmove;

int[][] bayesmysecondlastmove;

int[][] bayesopsecondlastmove;

int[][] bayesevengame;

int[][] bayesmymostplayed;

int[][] bayesopmostplayed;

int[][] bayesmyleastplayed;

int[][] bayesopleastplayed;

int[][] bayesmylastdiff;

int[][] bayesoplastdiff;

int[][] bayesmysecondlastdiff;

int[][] bayesopsecondlastdiff;

int[][] bayeswonafter;

int[] my_difference;

int[] opp_difference;

//bayesselection variables
static final int BAYESNUMBER = 14;

int[] bayesselectionmoves;

int[] bayesselectionhis;

//bayesselection vs. bayesgeneral
int bselection;

int bgeneral;

int bselectionlast;

int bgenerallast;

//metastrategy variables
static final int OPSTRATNUMBER = STRATNUMBER + 15;

int[][] opstrategymove;

boolean unknownstrat;

int minimax;

int[] credits;

int[] biasmove;

int[] biashis;

int[] his_best_history;

int[] his_worst_history;

int metaindex;

int nocreditindex;

int bestgeneralscore;

static final int BIASNUMBER = 6;

int bestbias = 4;

    public static final int ROCK = 0;
    public static final int PAPER = 1;
    public static final int SCISSORS = 2;

/**
 * @see com.anji.tournament.Player#reset()
 */
public void reset() {
	reset( my_history.length );
}

/**
 * @see com.anji.roshambo.RoshamboPlayer#reset(int)
 */
public void reset( int gamenumber ) {
	game = 0;
	current_score = 0;
	my_history = new int[ gamenumber ];
	opp_history = new int[ gamenumber ];
	strategymove = new int[ gamenumber ][ STRATNUMBER ];
	best_history = new int[ gamenumber ];
	worst_history = new int[ gamenumber ];
	longestseq = gamenumber + 1;
	my_historysum = new int[ gamenumber ];
	opp_historysum = new int[ gamenumber ];
	my_historysum[ 0 ] = 0;
	opp_historysum[ 0 ] = 0;
	bayeshis = 0;
	strathis = 0;
	metahis = 0;
	patternsearchindex = 2;

	//initiations bayes
	bayesmylastmove = new int[ 3 ][ 3 ];
	bayesoplastmove = new int[ 3 ][ 3 ];
	bayesmysecondlastmove = new int[ 3 ][ 3 ];
	bayesopsecondlastmove = new int[ 3 ][ 3 ];
	bayesevengame = new int[ 3 ][ 2 ];
	bayesmymostplayed = new int[ 3 ][ 7 ];
	bayesopmostplayed = new int[ 3 ][ 7 ];
	bayesmyleastplayed = new int[ 3 ][ 7 ];
	bayesopleastplayed = new int[ 3 ][ 7 ];
	bayesmylastdiff = new int[ 3 ][ 3 ];
	bayesoplastdiff = new int[ 3 ][ 3 ];
	bayesmysecondlastdiff = new int[ 3 ][ 3 ];
	bayesopsecondlastdiff = new int[ 3 ][ 3 ];
	bayeswonafter = new int[ 3 ][ 3 ];
	my_difference = new int[ gamenumber ];
	opp_difference = new int[ gamenumber ];
	my_difference[ 0 ] = 0;
	opp_difference[ 0 ] = 0;
	bayesselectionmoves = new int[ BAYESNUMBER ];
	bayesselectionhis = new int[ BAYESNUMBER ];

	//meta initialisations
	opstrategymove = new int[ gamenumber ][ OPSTRATNUMBER ];
	unknownstrat = false;
	credits = new int[ OPSTRATNUMBER ];
	biasmove = new int[ BIASNUMBER ];
	biashis = new int[ BIASNUMBER ];
	his_best_history = new int[ gamenumber ];
	his_worst_history = new int[ gamenumber ];
}

/**
 * @see java.lang.Object#toString()
 */
public String toString() {
	return getPlayerId();
}

/**
 * @see com.anji.roshambo.RoshamboPlayer#storeMove(int, int)
 */
public void storeMove( int move, int score ) {
	//strategyhis bijwerken
	best_history[ game ] = ( move + 1 ) % 3;
	worst_history[ game ] = ( move + 2 ) % 3;

	//bayesselectionhis bijwerken
	for ( int b = 0; b < BAYESNUMBER; b++ ) {
		if ( bayesselectionmoves[ b ] == move )
			bayesselectionhis[ b ]++;
		else if ( bayesselectionmoves[ b ] == ( move + 1 ) % 3 )
			bayesselectionhis[ b ]--;
	}

	int good = ( move + 1 ) % 3;
	int bad = ( move + 2 ) % 3;
	if ( bselectionlast == good )
		bselection++;
	else if ( bselectionlast == bad )
		bselection--;
	if ( bgenerallast == good )
		bgeneral++;
	else if ( bgenerallast == bad )
		bgeneral--;
	if ( bayesmove == good )
		bayeshis++;
	else if ( bayesmove == bad )
		bayeshis--;
	if ( stratmove == good )
		strathis++;
	else if ( stratmove == bad )
		strathis--;
	if ( metamove == good )
		metahis++;
	else if ( metamove == bad )
		metahis--;

	//bayesupdates
	bayesUpdate( move );

	//update metastrategy variables
	metaUpdate( move );

	//update historysum
	int previous;
	if ( game == 0 )
		previous = 0;
	else
		previous = game - 1;
	if ( move == ROCK ) {
		opp_historysum[ game ] = opp_historysum[ previous ];
		oprock++;
	}
	else if ( move == PAPER ) {
		opp_historysum[ game ] = opp_historysum[ previous ] + 1;
		oppaper++;
	}
	else {
		opp_historysum[ game ] = opp_historysum[ previous ] + longestseq;
		opscissors++;
	}

	//update opp_history
	opp_history[ game ] = move;
	current_score += score;
	game++;
}

/**
 * @see com.anji.roshambo.RoshamboPlayer#nextMove()
 */
public int nextMove() {
	//get nextmove
	int nextmove = metaMetaStrat();

	//my_historysum, statistic uptdate
	int previous;
	if ( game == 0 )
		previous = 0;
	else
		previous = game - 1;

	if ( nextmove == ROCK ) {
		my_historysum[ game ] = my_historysum[ previous ];
		merock++;
	}
	else if ( nextmove == PAPER ) {
		my_historysum[ game ] = my_historysum[ previous ] + 1;
		mepaper++;
	}
	else {
		my_historysum[ game ] = my_historysum[ previous ] + longestseq;
		mescissors++;
	}

	//metahistories update
	his_best_history[ game ] = ( nextmove + 1 ) % 3;
	his_worst_history[ game ] = ( nextmove + 2 ) % 3;

	my_history[ game ] = nextmove;
	return nextmove;
}

private int metaMetaStrat() {
	//get strategy moves
	bayesmove = bayes();
	stratmove = strategyMove();
	metamove = metaStrategy();

	//check fixed pattern
	if ( comparePattern( dbtable, patternsearchindex, patternsearchindex + 9, opp_history,
			game - 10, game - 1 ) ) {
		patternsearchindex++;
		return ( dbtable[ ( patternsearchindex + 9 ) % dbtable.length ] + 1 ) % 3;
	}
	else if ( comparePattern( pitable, patternsearchindex, patternsearchindex + 9, opp_history,
			game - 10, game - 1 ) ) {
		patternsearchindex++;
		return ( pitable[ ( patternsearchindex + 9 ) % pitable.length ] + 1 ) % 3;
	}
	else if ( comparePattern( eulertable, patternsearchindex, patternsearchindex + 9,
			opp_history, game - 10, game - 1 ) ) {
		patternsearchindex++;
		return ( eulertable[ ( patternsearchindex + 9 ) % eulertable.length ] + 1 ) % 3;
	}
	else
		patternsearchindex = 2;

	//if obviously losing, play random
	if ( current_score > -30 ) {
		if ( strathis > metahis && strathis > bayeshis )
			return stratmove;
		else if ( bayeshis > metahis )
			return bayesmove;
		else
			return metamove;
	}
	return getRandomMove();
}

    private int getRandomMove() {
        int randomMove = (int) Math.round(Math.random() * 2);
        return randomMove;
    }


private int strategyMove() {
	int opt;
	strategymove[ game ][ 0 ] = freq( merock, mepaper, mescissors );
	strategymove[ game ][ 1 ] = ( strategymove[ game ][ 0 ] + 1 ) % 3;
	strategymove[ game ][ 2 ] = ( strategymove[ game ][ 0 ] + 2 ) % 3;
	strategymove[ game ][ 3 ] = freq( oprock, oppaper, opscissors );
	strategymove[ game ][ 4 ] = ( strategymove[ game ][ 3 ] + 1 ) % 3;
	strategymove[ game ][ 5 ] = ( strategymove[ game ][ 3 ] + 2 ) % 3;
	opt = searchPattern( ME, 1 );
	strategymove[ game ][ 6 ] = my_history[ opt ];
	strategymove[ game ][ 7 ] = ( strategymove[ game ][ 6 ] + 1 ) % 3;
	strategymove[ game ][ 8 ] = ( strategymove[ game ][ 6 ] + 2 ) % 3;
	strategymove[ game ][ 9 ] = opp_history[ opt ];
	strategymove[ game ][ 10 ] = ( strategymove[ game ][ 9 ] + 1 ) % 3;
	strategymove[ game ][ 11 ] = ( strategymove[ game ][ 9 ] + 2 ) % 3;
	opt = searchPattern( OP, 1 );
	strategymove[ game ][ 12 ] = my_history[ opt ];
	strategymove[ game ][ 13 ] = ( strategymove[ game ][ 12 ] + 1 ) % 3;
	strategymove[ game ][ 14 ] = ( strategymove[ game ][ 12 ] + 2 ) % 3;
	strategymove[ game ][ 15 ] = opp_history[ opt ];
	strategymove[ game ][ 16 ] = ( strategymove[ game ][ 15 ] + 1 ) % 3;
	strategymove[ game ][ 17 ] = ( strategymove[ game ][ 15 ] + 2 ) % 3;
	opt = searchPattern( ME, 10 );
	strategymove[ game ][ 18 ] = my_history[ opt ];
	strategymove[ game ][ 19 ] = ( strategymove[ game ][ 18 ] + 1 ) % 3;
	strategymove[ game ][ 20 ] = ( strategymove[ game ][ 18 ] + 2 ) % 3;
	strategymove[ game ][ 21 ] = opp_history[ opt ];
	strategymove[ game ][ 22 ] = ( strategymove[ game ][ 21 ] + 1 ) % 3;
	strategymove[ game ][ 23 ] = ( strategymove[ game ][ 21 ] + 2 ) % 3;
	opt = searchPattern( OP, 10 );
	strategymove[ game ][ 24 ] = my_history[ opt ];
	strategymove[ game ][ 25 ] = ( strategymove[ game ][ 24 ] + 1 ) % 3;
	strategymove[ game ][ 26 ] = ( strategymove[ game ][ 24 ] + 2 ) % 3;
	strategymove[ game ][ 27 ] = opp_history[ opt ];
	strategymove[ game ][ 28 ] = ( strategymove[ game ][ 27 ] + 1 ) % 3;
	strategymove[ game ][ 29 ] = ( strategymove[ game ][ 27 ] + 2 ) % 3;
	opt = searchPattern( ME, 20 );
	strategymove[ game ][ 30 ] = my_history[ opt ];
	strategymove[ game ][ 31 ] = ( strategymove[ game ][ 30 ] + 1 ) % 3;
	strategymove[ game ][ 32 ] = ( strategymove[ game ][ 30 ] + 2 ) % 3;
	strategymove[ game ][ 33 ] = opp_history[ opt ];
	strategymove[ game ][ 34 ] = ( strategymove[ game ][ 33 ] + 1 ) % 3;
	strategymove[ game ][ 35 ] = ( strategymove[ game ][ 33 ] + 2 ) % 3;
	opt = searchPattern( OP, 20 );
	strategymove[ game ][ 36 ] = my_history[ opt ];
	strategymove[ game ][ 37 ] = ( strategymove[ game ][ 36 ] + 1 ) % 3;
	strategymove[ game ][ 38 ] = ( strategymove[ game ][ 36 ] + 2 ) % 3;
	strategymove[ game ][ 39 ] = opp_history[ opt ];
	strategymove[ game ][ 40 ] = ( strategymove[ game ][ 39 ] + 1 ) % 3;
	strategymove[ game ][ 41 ] = ( strategymove[ game ][ 39 ] + 2 ) % 3;
	opt = searchPattern( ME, 50 );
	strategymove[ game ][ 42 ] = my_history[ opt ];
	strategymove[ game ][ 43 ] = ( strategymove[ game ][ 42 ] + 1 ) % 3;
	strategymove[ game ][ 44 ] = ( strategymove[ game ][ 42 ] + 2 ) % 3;
	strategymove[ game ][ 45 ] = opp_history[ opt ];
	strategymove[ game ][ 46 ] = ( strategymove[ game ][ 45 ] + 1 ) % 3;
	strategymove[ game ][ 47 ] = ( strategymove[ game ][ 45 ] + 2 ) % 3;
	opt = searchPattern( OP, 50 );
	strategymove[ game ][ 48 ] = my_history[ opt ];
	strategymove[ game ][ 49 ] = ( strategymove[ game ][ 48 ] + 1 ) % 3;
	strategymove[ game ][ 50 ] = ( strategymove[ game ][ 48 ] + 2 ) % 3;
	strategymove[ game ][ 51 ] = opp_history[ opt ];
	strategymove[ game ][ 52 ] = ( strategymove[ game ][ 51 ] + 1 ) % 3;
	strategymove[ game ][ 53 ] = ( strategymove[ game ][ 51 ] + 2 ) % 3;
	opt = matchBoth();
	strategymove[ game ][ 54 ] = my_history[ opt ];
	strategymove[ game ][ 55 ] = ( strategymove[ game ][ 54 ] + 1 ) % 3;
	strategymove[ game ][ 56 ] = ( strategymove[ game ][ 54 ] + 2 ) % 3;
	strategymove[ game ][ 57 ] = opp_history[ opt ];
	strategymove[ game ][ 58 ] = ( strategymove[ game ][ 57 ] + 1 ) % 3;
	strategymove[ game ][ 59 ] = ( strategymove[ game ][ 57 ] + 2 ) % 3;
	//pi without 0's
	strategymove[ game ][ 60 ] = beatPI2();

	int counter1, counter2;
	int bestindex = 0;
	int worstindex = 0;
	int bestscore = -1000;
	int worstscore = 1000;

	//the performance on the last 10 moves of a strategy counts
	if ( game >= 10 ) {
		for ( int a = 0; a < STRATNUMBER; a++ ) {
			counter1 = counter2 = 0;
			for ( int b = 0; b < 10; b++ ) {
				if ( strategymove[ game - ( b + 1 ) ][ a ] == best_history[ game - ( b + 1 ) ] )
					counter2++;
				else if ( strategymove[ game - ( b + 1 ) ][ a ] == worst_history[ game - ( b + 1 ) ] )
					counter2--;
				if ( b == 0 )
					counter1 += 10 * counter2;
				if ( b == 1 )
					counter1 += 5 * counter2;
				if ( b == 4 )
					counter1 += 2 * counter2;
				if ( b == 9 )
					counter1 += counter2;
			}
			if ( counter1 > bestscore ) {
				bestscore = counter1;
				bestindex = a;
			}
			else if ( counter1 < worstscore ) {
				worstscore = counter1;
				worstindex = a;
			}
		}
		//the worst strategy is sometime used to confuse the opponent
		if ( worstscore < -bestscore )
			return ( ( strategymove[ game ][ worstindex ] + 2 ) % 3 );
		return ( strategymove[ game ][ bestindex ] );
	}
	return getRandomMove();
}

//choosing by looking at statistics
private int freq( int rocknum, int papernum, int scissorsnum ) {
	if ( ( rocknum > papernum ) && ( rocknum > scissorsnum ) )
		return PAPER;
	else if ( papernum > scissorsnum )
		return SCISSORS;
	else
		return ROCK;
}

//check history for patterns in all kinds of lengths
private int searchPattern( int player, int period ) {
	int[] history;
	int[] historysum;
	if ( player == ME ) {
		history = my_history;
		historysum = my_historysum;
	}
	else {
		history = opp_history;
		historysum = opp_historysum;
	}
	int q = 0;
	boolean done = false;
	if ( game >= 2 * period ) {
		int totalsum = historysum[ game - 1 ] - historysum[ game - period - 1 ];
		for ( q = game - period - 1; ( q - period >= 0 ) && ( !done ); q-- ) {
			if ( historysum[ q ] - historysum[ q - period ] == totalsum
					&& history[ q ] == history[ game - 1 ] ) {
				boolean failure = false;
				for ( int r = q; ( r >= q - period ) && ( !failure ); r-- ) {
					if ( !( history[ r ] == history[ game - 1 - ( q - r ) ] ) )
						failure = true;
					else if ( r == q - period )
						done = true;
				}
			}
		}
	}
	if ( done ) {
		return q + 2;
	}
	return getRandomMove() * game / 3;
}

//both histories compared
private int matchBoth() {
	int q, r, bestq, searchlimit;
	searchlimit = 50;
	if ( game > 10 )
		bestq = game - 11;
	else
		bestq = 0;
	int bestlength = 0;

	if ( game > 1 ) {
		for ( q = game - 2; ( q >= bestlength && bestlength < searchlimit ); q-- ) {
			//q meer dan searchlimit!
			if ( my_historysum[ q ] - my_historysum[ q - bestlength ] == my_historysum[ game - 1 ]
					- my_historysum[ game - 1 - bestlength ]
					&& opp_historysum[ q ] - opp_historysum[ q - bestlength ] == opp_historysum[ game - 1 ]
							- opp_historysum[ game - 1 - bestlength ]
					&& my_history[ q ] == my_history[ game - 1 ]
					&& opp_history[ q ] == opp_history[ game - 1 ] ) {
				r = 0;
				while ( ( r <= q ) && ( my_history[ q - r ] == my_history[ game - 1 - r ] )
						&& ( opp_history[ q - r ] == opp_history[ game - 1 - r ] ) ) {
					r++;
				}
				if ( r > bestlength ) {
					bestlength = r;
					bestq = q;
				}

			}
		}
		return bestq;
	}
	return 0;
}

//the naive bayes classifier, specialized version
private int bayes() {
	if ( game > 1 ) {
		int[] besthis = new int[ BAYESNUMBER ];
		for ( int a = 0; a < 3; a++ ) {
			if ( besthis[ 0 ] < bayesmylastmove[ a ][ my_history[ game - 1 ] ] ) {
				besthis[ 0 ] = bayesmylastmove[ a ][ my_history[ game - 1 ] ];
				bayesselectionmoves[ 0 ] = a;
			}
			if ( besthis[ 1 ] < bayesoplastmove[ a ][ opp_history[ game - 1 ] ] ) {
				besthis[ 1 ] = bayesoplastmove[ a ][ opp_history[ game - 1 ] ];
				bayesselectionmoves[ 1 ] = a;
			}
			if ( besthis[ 2 ] < bayesmysecondlastmove[ a ][ my_history[ game - 2 ] ] ) {
				besthis[ 2 ] = bayesmysecondlastmove[ a ][ my_history[ game - 2 ] ];
				bayesselectionmoves[ 2 ] = a;
			}
			if ( besthis[ 3 ] < bayesopsecondlastmove[ a ][ opp_history[ game - 2 ] ] ) {
				besthis[ 3 ] = bayesopsecondlastmove[ a ][ opp_history[ game - 2 ] ];
				bayesselectionmoves[ 3 ] = a;
			}
			if ( besthis[ 4 ] < bayesevengame[ a ][ game % 2 ] ) {
				besthis[ 4 ] = bayesevengame[ a ][ game % 2 ];
				bayesselectionmoves[ 4 ] = a;
			}
			if ( besthis[ 5 ] < bayesmymostplayed[ a ][ freqStats( ME, true ) ] ) {
				besthis[ 5 ] = bayesmymostplayed[ a ][ freqStats( ME, true ) ];
				bayesselectionmoves[ 5 ] = a;
			}
			if ( besthis[ 6 ] < bayesopmostplayed[ a ][ freqStats( OP, true ) ] ) {
				besthis[ 6 ] = bayesopmostplayed[ a ][ freqStats( OP, true ) ];
				bayesselectionmoves[ 6 ] = a;
			}
			if ( besthis[ 7 ] < bayesmyleastplayed[ a ][ freqStats( ME, false ) ] ) {
				besthis[ 7 ] = bayesmyleastplayed[ a ][ freqStats( ME, false ) ];
				bayesselectionmoves[ 7 ] = a;
			}
			if ( besthis[ 8 ] < bayesopleastplayed[ a ][ freqStats( OP, false ) ] ) {
				besthis[ 8 ] = bayesopleastplayed[ a ][ freqStats( OP, false ) ];
				bayesselectionmoves[ 8 ] = a;
			}
			if ( besthis[ 9 ] < bayesmylastdiff[ ( a - my_history[ game - 1 ] + 3 ) % 3 ][ my_difference[ game - 1 ] ] ) {
				besthis[ 9 ] = bayesmylastdiff[ ( a - my_history[ game - 1 ] + 3 ) % 3 ][ my_difference[ game - 1 ] ];
				bayesselectionmoves[ 9 ] = a;
			}
			if ( besthis[ 10 ] < bayesoplastdiff[ ( a - opp_history[ game - 1 ] + 3 ) % 3 ][ opp_difference[ game - 1 ] ] ) {
				besthis[ 10 ] = bayesoplastdiff[ ( a - opp_history[ game - 1 ] + 3 ) % 3 ][ opp_difference[ game - 1 ] ];
				bayesselectionmoves[ 10 ] = a;
			}
			if ( besthis[ 11 ] < bayesmysecondlastdiff[ ( a - my_history[ game - 1 ] + 3 ) % 3 ][ my_difference[ game - 2 ] ] ) {
				besthis[ 11 ] = bayesmysecondlastdiff[ ( a - my_history[ game - 1 ] + 3 ) % 3 ][ my_difference[ game - 2 ] ];
				bayesselectionmoves[ 11 ] = a;
			}
			if ( besthis[ 12 ] < bayesopsecondlastdiff[ ( a - opp_history[ game - 1 ] + 3 ) % 3 ][ opp_difference[ game - 2 ] ] ) {
				besthis[ 12 ] = bayesopsecondlastdiff[ ( a - opp_history[ game - 1 ] + 3 ) % 3 ][ opp_difference[ game - 2 ] ];
				bayesselectionmoves[ 12 ] = a;
			}
			if ( besthis[ 13 ] < bayeswonafter[ a ][ opp_history[ game - 1 ] ] ) {
				besthis[ 13 ] = bayeswonafter[ a ][ opp_history[ game - 1 ] ];
				bayesselectionmoves[ 13 ] = ( a + 2 ) % 3;
			}
		}
	}
	int selectionlength = -1000;
	for ( int z = 0; z < BAYESNUMBER; z++ ) {
		if ( bayesselectionhis[ z ] > selectionlength ) {
			selectionlength = bayesselectionhis[ z ];
			bselectionlast = ( bayesselectionmoves[ z ] + 1 ) % 3;
		}
	}
	bgenerallast = getBayes();
	if ( bselection > bgeneral )
		return bselectionlast;
	return bgenerallast;
}

//naive bayes classifier, general version
private int getBayes() {
	int bayesnormal = getRandomMove();
	double bestlength = 0;
	if ( game > 1 ) {
		for ( int a = 0; a < 3; a++ ) {
			double product = Math
					.abs( (double) ( bayesmylastmove[ a ][ my_history[ game - 1 ] ] + 1 )
							* ( bayesoplastmove[ a ][ opp_history[ game - 1 ] ] + 1 )
							* ( bayesmysecondlastmove[ a ][ my_history[ game - 2 ] ] + 1 )
							* ( bayesopsecondlastmove[ a ][ opp_history[ game - 2 ] ] + 1 )
							* ( bayesevengame[ a ][ game % 2 ] + 1 )
							* ( bayesmymostplayed[ a ][ freqStats( ME, true ) ] + 1 )
							* ( bayesopmostplayed[ a ][ freqStats( OP, true ) ] + 1 )
							* ( bayesmyleastplayed[ a ][ freqStats( ME, false ) ] + 1 )
							* ( bayesopleastplayed[ a ][ freqStats( OP, false ) ] + 1 )
							* ( bayesmylastdiff[ ( a - my_history[ game - 1 ] + 3 ) % 3 ][ my_difference[ game - 1 ] ] + 1 )
							* ( bayesoplastdiff[ ( a - opp_history[ game - 1 ] + 3 ) % 3 ][ opp_difference[ game - 1 ] ] + 1 )
							* ( bayesmysecondlastdiff[ ( a - my_history[ game - 1 ] + 3 ) % 3 ][ my_difference[ game - 2 ] ] + 1 )
							* ( bayesopsecondlastdiff[ ( a - opp_history[ game - 1 ] + 3 ) % 3 ][ opp_difference[ game - 2 ] ] + 1 )
							* ( bayeswonafter[ a ][ opp_history[ game - 1 ] ] + 1 ) );

			if ( product > bestlength ) {
				bayesnormal = ( a + 1 ) % 3;
				bestlength = product;
			}
		}
	}
	return bayesnormal;
}

//possible opponent fixed pattern strategies
private int beatPI() {
	int prediction = pitable[ game % pitable.length ];
	return ( prediction + 1 ) % 3;
}

int pi_index;

private int beatPI2() {
	int move = ( pitable[ pi_index ] + 1 ) % 3;
	pi_index++;
	pi_index %= pitable.length;
	while ( pitable[ pi_index ] == 0 ) {
		pi_index++;
		pi_index %= pitable.length;
	}
	return ( move );
}

private int beatDeBruijn() {
	int prediction = dbtable[ ( game + 1 ) % dbtable.length ];
	return ( prediction + 1 ) % 3;
}

private int beatEuler() {
	int prediction = eulertable[ game % eulertable.length ];
	return ( prediction + 1 ) % 3;
}

int pitable[] = { 3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5, 8, 9, 7, 9, 3, 2, 3, 8, 4, 6, 2, 6, 4, 3, 3,
		8, 3, 2, 7, 9, 5, 0, 2, 8, 8, 4, 1, 9, 7, 1, 6, 9, 3, 9, 9, 3, 7, 5, 1, 0, 5, 8, 2, 0, 9,
		7, 4, 9, 4, 4, 5, 9, 2, 3, 0, 7, 8, 1, 6, 4, 0, 6, 2, 8, 6, 2, 0, 8, 9, 9, 8, 6, 2, 8, 0,
		3, 4, 8, 2, 5, 3, 4, 2, 1, 1, 7, 0, 6, 7, 9, 8, 2, 1, 4, 8, 0, 8, 6, 5, 1, 3, 2, 8, 2, 3,
		0, 6, 6, 4, 7, 0, 9, 3, 8, 4, 4, 6, 0, 9, 5, 5, 0, 5, 8, 2, 2, 3, 1, 7, 2, 5, 3, 5, 9, 4,
		0, 8, 1, 2, 8, 4, 8, 1, 1, 1, 7, 4, 5, 0, 2, 8, 4, 1, 0, 2, 7, 0, 1, 9, 3, 8, 5, 2, 1, 1,
		0, 5, 5, 5, 9, 6, 4, 4, 6, 2, 2, 9, 4, 8, 9, 5, 4, 9, 3, 0, 3, 8, 1, 9, 6, 4, 4, 2, 8, 8,
		1, 0, 9, 7, 5, 6, 6, 5, 9, 3, 3, 4, 4, 6, 1, 2, 8, 4, 7, 5, 6, 4, 8, 2, 3, 3, 7, 8, 6, 7,
		8, 3, 1, 6, 5, 2, 7, 1, 2, 0, 1, 9, 0, 9, 1, 4, 5, 6, 4, 8, 5, 6, 6, 9, 2, 3, 4, 6, 0, 3,
		4, 8, 6, 1, 0, 4, 5, 4, 3, 2, 6, 6, 4, 8, 2, 1, 3, 3, 9, 3, 6, 0, 7, 2, 6, 0, 2, 4, 9, 1,
		4, 1, 2, 7, 3, 7, 2, 4, 5, 8, 7, 0, 0, 6, 6, 0, 6, 3, 1, 5, 5, 8, 8, 1, 7, 4, 8, 8, 1, 5,
		2, 0, 9, 2, 0, 9, 6, 2, 8, 2, 9, 2, 5, 4, 0, 9, 1, 7, 1, 5, 3, 6, 4, 3, 6, 7, 8, 9, 2, 5,
		9, 0, 3, 6, 0, 0, 1, 1, 3, 3, 0, 5, 3, 0, 5, 4, 8, 8, 2, 0, 4, 6, 6, 5, 2, 1, 3, 8, 4, 1,
		4, 6, 9, 5, 1, 9, 4, 1, 5, 1, 1, 6, 0, 9, 4, 3, 3, 0, 5, 7, 2, 7, 0, 3, 6, 5, 7, 5, 9, 5,
		9, 1, 9, 5, 3, 0, 9, 2, 1, 8, 6, 1, 1, 7, 3, 8, 1, 9, 3, 2, 6, 1, 1, 7, 9, 3, 1, 0, 5, 1,
		1, 8, 5, 4, 8, 0, 7, 4, 4, 6, 2, 3, 7, 9, 9, 6, 2, 7, 4, 9, 5, 6, 7, 3, 5, 1, 8, 8, 5, 7,
		5, 2, 7, 2, 4, 8, 9, 1, 2, 2, 7, 9, 3, 8, 1, 8, 3, 0, 1, 1, 9, 4, 9, 1, 2, 9, 8, 3, 3, 6,
		7, 3, 3, 6, 2, 4, 4, 0, 6, 5, 6, 6, 4, 3, 0, 8, 6, 0, 2, 1, 3, 9, 4, 9, 4, 6, 3, 9, 5, 2,
		2, 4, 7, 3, 7, 1, 9, 0, 7, 0, 2, 1, 7, 9, 8, 6, 0, 9, 4, 3, 7, 0, 2, 7, 7, 0, 5, 3, 9, 2,
		1, 7, 1, 7, 6, 2, 9, 3, 1, 7, 6, 7, 5, 2, 3, 8, 4, 6, 7, 4, 8, 1, 8, 4, 6, 7, 6, 6, 9, 4,
		0, 5, 1, 3, 2, 0, 0, 0, 5, 6, 8, 1, 2, 7, 1, 4, 5, 2, 6, 3, 5, 6, 0, 8, 2, 7, 7, 8, 5, 7,
		7, 1, 3, 4, 2, 7, 5, 7, 7, 8, 9, 6, 0, 9, 1, 7, 3, 6, 3, 7, 1, 7, 8, 7, 2, 1, 4, 6, 8, 4,
		4, 0, 9, 0, 1, 2, 2, 4, 9, 5, 3, 4, 3, 0, 1, 4, 6, 5, 4, 9, 5, 8, 5, 3, 7, 1, 0, 5, 0, 7,
		9, 2, 2, 7, 9, 6, 8, 9, 2, 5, 8, 9, 2, 3, 5, 4, 2, 0, 1, 9, 9, 5, 6, 1, 1, 2, 1, 2, 9, 0,
		2, 1, 9, 6, 0, 8, 6, 4, 0, 3, 4, 4, 1, 8, 1, 5, 9, 8, 1, 3, 6, 2, 9, 7, 7, 4, 7, 7, 1, 3,
		0, 9, 9, 6, 0, 5, 1, 8, 7, 0, 7, 2, 1, 1, 3, 4, 9, 9, 9, 9, 9, 9, 8, 3, 7, 2, 9, 7, 8, 0,
		4, 9, 9, 5, 1, 0, 5, 9, 7, 3, 1, 7, 3, 2, 8, 1, 6, 0, 9, 6, 3, 1, 8, 5, 9, 5, 0, 2, 4, 4,
		5, 9, 4, 5, 5, 3, 4, 6, 9, 0, 8, 3, 0, 2, 6, 4, 2, 5, 2, 2, 3, 0, 8, 2, 5, 3, 3, 4, 4, 6,
		8, 5, 0, 3, 5, 2, 6, 1, 9, 3, 1, 1, 8, 8, 1, 7, 1, 0, 1, 0, 0, 0, 3, 1, 3, 7, 8, 3, 8, 7,
		5, 2, 8, 8, 6, 5, 8, 7, 5, 3, 3, 2, 0, 8, 3, 8, 1, 4, 2, 0, 6, 1, 7, 1, 7, 7, 6, 6, 9, 1,
		4, 7, 3, 0, 3, 5, 9, 8, 2, 5, 3, 4, 9, 0, 4, 2, 8, 7, 5, 5, 4, 6, 8, 7, 3, 1, 1, 5, 9, 5,
		6, 2, 8, 6, 3, 8, 8, 2, 3, 5, 3, 7, 8, 7, 5, 9, 3, 7, 5, 1, 9, 5, 7, 7, 8, 1, 8, 5, 7, 7,
		8, 0, 5, 3, 2, 1, 7, 1, 2, 2, 6, 8, 0, 6, 6, 1, 3, 0, 0, 1, 9, 2, 7, 8, 7, 6, 6, 1, 1, 1,
		9, 5, 9, 0, 9, 2, 1, 6, 4, 2, 0, 1, 9, 8, 9, 3, 8, 0, 9, 5, 2, 5, 7, 2, 0, 1, 0, 6, 5, 4,
		8, 5, 8, 6, 3, 2, 7, 8, 8, 6, 5, 9, 3, 6, 1, 5, 3, 3, 8, 1, 8, 2, 7, 9, 6, 8, 2, 3, 0, 3,
		0, 1, 9, 5, 2, 0, 3, 5, 3, 0, 1, 8, 5, 2, 9, 6, 8, 9, 9, 5, 7, 7, 3, 6, 2, 2, 5, 9, 9, 4,
		1, 3, 8, 9, 1, 2, 4, 9, 7, 2, 1, 7, 7, 5, 2, 8, 3, 4, 7, 9, 1, 3, 1, 5, 1, 5, 5, 7, 4, 8,
		5, 7, 2, 4, 2, 4, 5, 4, 1, 5, 0, 6, 9, 5, 9, 5, 0, 8, 2, 9, 5, 3, 3, 1, 1, 6, 8, 6, 1, 7,
		2, 7, 8, 5, 5, 8, 8, 9, 0, 7, 5, 0, 9, 8, 3, 8, 1, 7, 5, 4, 6, 3, 7, 4, 6, 4, 9, 3, 9, 3,
		1, 9, 2, 5, 5, 0, 6, 0, 4, 0, 0, 9, 2, 7, 7, 0, 1, 6, 7, 1, 1, 3, 9, 0, 0, 9, 8, 4, 8, 8,
		2, 4, 0, 1 };

int dbtable[] = { 1, 0, 2, 0, 0, 2, 0, 2, 0, 1, 1, 0, 0, 2, 2, 1, 0, 0, 1, 1, 2, 2, 0, 0, 1, 2,
		1, 0, 2, 2, 2, 2, 0, 1, 2, 0, 2, 2, 0, 2, 1, 1, 2, 1, 1, 0, 1, 1, 1, 2, 0, 0, 0, 0, 2, 1,
		0, 1, 0, 1, 2, 2, 1, 2, 0, 1, 0, 0, 0, 1, 0, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 0, 0, 1, 1, 1,
		1, 0, 1, 0, 2, 2, 2, 0, 0, 2, 2, 0, 2, 0, 1, 0, 1, 1, 0, 2, 1, 1, 2, 2, 2, 2, 1, 1, 1, 2,
		0, 1, 2, 2, 1, 2, 0, 0, 0, 1, 0, 0, 0, 0, 2, 0, 2, 2, 1, 0, 0, 1, 2, 1, 2, 2, 0, 1, 1, 2,
		1, 1, 0, 0, 2, 1, 0, 1, 2, 0, 2, 1, 2, 1, 0, 2, 1, 1, 2, 0, 0, 1, 0, 1, 2, 2, 0, 1, 0, 0,
		2, 0, 1, 2, 0, 1, 1, 2, 1, 1, 1, 1, 0, 2, 0, 2, 1, 0, 2, 2, 0, 2, 2, 2, 2, 0, 0, 0, 1, 2,
		1, 2, 2, 2, 1, 1, 0, 1, 1, 0, 0, 0, 0, 2, 1, 2, 0, 2, 0, 0, 2, 2, 1, 0, 0, 1, 1, 1, 2, 2,
		1, 2, 1, 0, 1, 0, 2, 1, 0, 1, 0, 2, 0, 2, 0, 0, 1, 2, 2, 2, 0, 2, 1, 0, 0, 1, 1, 1, 2, 2,
		1, 1, 0, 2, 2, 0, 0, 0, 2, 2, 2, 2, 1, 2, 2, 0, 1, 2, 0, 0, 2, 0, 1, 1, 2, 1, 2, 1, 1, 1,
		1, 0, 0, 2, 1, 2, 0, 1, 0, 0, 0, 0, 1, 0, 1, 1, 0, 1, 2, 1, 0, 2, 1, 1, 2, 0, 2, 2, 2, 2,
		1, 1, 1, 1, 0, 0, 2, 0, 2, 2, 2, 1, 2, 1, 0, 2, 1, 0, 0, 0, 0, 2, 1, 1, 2, 2, 1, 0, 1, 0,
		0, 1, 1, 1, 2, 1, 1, 0, 1, 2, 2, 2, 2, 0, 0, 1, 2, 0, 2, 0, 1, 2, 1, 2, 0, 1, 0, 1, 1, 2,
		0, 0, 0, 1, 0, 2, 2, 0, 2, 1, 2, 2, 0, 1, 1, 0, 2, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2, 1, 1,
		0, 0, 1, 1, 1, 1, 0, 2, 0, 2, 1, 2, 0, 2, 2, 1, 2, 2, 2, 1, 1, 1, 2, 1, 2, 1, 0, 0, 2, 0,
		1, 1, 0, 1, 0, 2, 1, 0, 2, 2, 2, 2, 0, 2, 0, 0, 2, 2, 0, 0, 1, 2, 2, 1, 0, 1, 1, 2, 0, 1,
		2, 1, 1, 2, 2, 0, 1, 0, 1, 2, 2, 2, 0, 2, 0, 0, 2, 0, 2, 1, 2, 2, 2, 2, 0, 0, 0, 0, 2, 2,
		1, 0, 0, 0, 1, 2, 0, 1, 2, 1, 2, 0, 0, 1, 0, 2, 0, 1, 0, 0, 2, 1, 0, 1, 2, 2, 1, 1, 2, 0,
		2, 2, 2, 1, 2, 1, 0, 2, 2, 0, 1, 1, 0, 2, 1, 1, 0, 0, 1, 1, 2, 1, 1, 1, 1, 0, 1, 0, 1, 1,
		1, 1, 1, 1, 2, 1, 0, 0, 0, 0, 1, 1, 0, 2, 1, 2, 1, 2, 2, 2, 0, 0, 1, 2, 0, 1, 0, 1, 2, 1,
		1, 2, 2, 0, 2, 0, 2, 1, 1, 0, 0, 1, 0, 2, 0, 0, 2, 0, 1, 1, 2, 0, 2, 2, 1, 1, 1, 1, 0, 1,
		0, 0, 2, 2, 2, 2, 1, 2, 0, 0, 0, 2, 1, 0, 2, 2, 0, 1, 2, 2, 1, 0, 2, 1, 0, 1, 0, 1, 1, 1,
		1, 2, 1, 1, 0, 1, 2, 1, 2, 2, 2, 2, 1, 2, 0, 0, 0, 1, 1, 2, 0, 2, 0, 2, 1, 0, 0, 0, 0, 2,
		0, 0, 1, 0, 0, 2, 2, 2, 0, 0, 2, 1, 1, 2, 2, 0, 1, 2, 0, 1, 1, 0, 0, 1, 2, 2, 1, 1, 1, 0,
		2, 0, 1, 0, 2, 2, 0, 2, 2, 1, 0, 2, 1, 2, 2, 2, 1, 0, 1, 0, 2, 2, 1, 2, 0, 2, 1, 0, 2, 0,
		0, 0, 0, 1, 2, 1, 0, 0, 2, 0, 2, 2, 0, 1, 0, 1, 1, 2, 1, 1, 0, 0, 1, 0, 0, 0, 2, 1, 1, 2,
		0, 0, 2, 2, 2, 2, 0, 0, 1, 1, 1, 0, 2, 1, 2, 1, 2, 2, 1, 1, 1, 1, 2, 2, 0, 2, 0, 1, 2, 0,
		1, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 2, 0, 1, 2, 1, 2, 2, 1, 0, 0, 2, 0, 2, 1, 0, 1, 0, 2, 2,
		0, 1, 1, 2, 1, 0, 2, 0, 0, 1, 0, 1, 1, 1, 2, 2, 2, 2, 1, 2, 0, 2, 2, 1, 1, 2, 0, 0, 2, 1,
		2, 1, 1, 1, 1, 0, 2, 1, 1, 0, 0, 0, 0, 2, 2, 2, 0, 0, 0, 1, 2, 2, 0, 2, 0, 2, 2, 0, 2, 1,
		1, 2, 0, 2, 0, 0, 1, 1, 1, 0, 0, 1, 2, 1, 1, 0, 1, 1, 0, 2, 2, 0, 0, 2, 2, 1, 1, 1, 1, 2,
		1, 2, 1, 0, 2, 0, 2, 2, 2, 2, 1, 2, 0, 0, 0, 1, 0, 0, 2, 1, 0, 0, 0, 0, 2, 0, 1, 1, 2, 2,
		2, 0, 1, 2, 2, 1, 0, 1, 0, 1, 2, 0, 1, 0, 2, 1, 0, 2, 0, 2, 1, 1, 1, 0, 0, 2, 2, 2, 0, 1,
		1, 2, 2, 1, 2, 0, 0, 0, 1, 0, 1, 2, 1, 0 };

int eulertable[] = { 2, 7, 1, 8, 2, 8, 1, 8, 2, 8, 4, 5, 9, 0, 4, 5, 2, 3, 5, 3, 6, 0, 2, 8, 7,
		4, 7, 1, 3, 5, 2, 6, 6, 2, 4, 9, 7, 7, 5, 7, 2, 4, 7, 0, 9, 3, 6, 9, 9, 9, 5, 9, 5, 7, 4,
		9, 6, 6, 9, 6, 7, 6, 2, 7, 7, 2, 4, 0, 7, 6, 6, 3, 0, 3, 5, 3, 5, 4, 7, 5, 9, 4, 5, 7, 1,
		3, 8, 2, 1, 7, 8, 5, 2, 5, 1, 6, 6, 4, 2, 7, 4, 2, 7, 4, 6, 6, 3, 9, 1, 9, 3, 2, 0, 0, 3,
		0, 5, 9, 9, 2, 1, 8, 1, 7, 4, 1, 3, 5, 9, 6, 6, 2, 9, 0, 4, 3, 5, 7, 2, 9, 0, 0, 3, 3, 4,
		2, 9, 5, 2, 6, 0, 5, 9, 5, 6, 3, 0, 7, 3, 8, 1, 3, 2, 3, 2, 8, 6, 2, 7, 9, 4, 3, 4, 9, 0,
		7, 6, 3, 2, 3, 3, 8, 2, 9, 8, 8, 0, 7, 5, 3, 1, 9, 5, 2, 5, 1, 0, 1, 9, 6, 3, 9, 1, 9, 3,
		2, 0, 0, 3, 0, 5, 9, 9, 2, 1, 8, 1, 7, 4, 1, 3, 5, 9, 6, 6, 2, 9, 0, 4, 3, 5, 7, 2, 9, 0,
		0, 3, 3, 4, 2, 9, 5, 2, 6, 0, 5, 9, 5, 6, 3, 0, 7, 3, 8, 1, 3, 2, 3, 2, 8, 6, 2, 7, 9, 4,
		3, 4, 9, 0, 7, 6, 3, 2, 3, 3, 8, 2, 9, 8, 8, 0, 7, 5, 3, 1, 9, 5, 2, 5, 1, 0, 1, 9 };

//advanced statistic search for bayes use
private int freqStats( int player, boolean most ) {
	if ( most ) {
		if ( player == ME )
			return calculateFreqStats( merock, mepaper, mescissors );
		return calculateFreqStats( oprock, oppaper, opscissors );
	}
	if ( player == ME )
		return calculateLeastStats( merock, mepaper, mescissors );
	return calculateLeastStats( oprock, oppaper, opscissors );
}

private int calculateFreqStats( int rock, int paper, int scissors ) {
	if ( rock > paper ) {
		if ( rock > scissors )
			return 0;
		else if ( scissors > rock )
			return 2;
		else
			return 3;
	}
	else if ( paper > rock ) {
		if ( paper > scissors )
			return 1;
		else if ( scissors > paper )
			return 2;
		else
			return 5;
	}
	else {
		if ( rock > scissors )
			return 4;
		else if ( scissors > rock )
			return 2;
		else
			return 6;
	}
}

private int calculateLeastStats( int rock, int paper, int scissors ) {
	if ( rock < paper ) {
		if ( rock < scissors )
			return 0;
		else if ( scissors < rock )
			return 2;
		else
			return 3;
	}
	else if ( paper < rock ) {
		if ( paper < scissors )
			return 1;
		else if ( scissors < paper )
			return 2;
		else
			return 5;
	}
	else {
		if ( rock < scissors )
			return 4;
		else if ( scissors < rock )
			return 2;
		else
			return 6;
	}
}

//pattern search method against Pi/deBruijn/Euler-bots
private boolean comparePattern( int[] history1, int begin1, int end1, int[] history2,
		int begin2, int end2 ) {
	if ( end1 - begin1 == end2 - begin2 && ( end1 > begin1 ) && end1 < history1.length
			&& begin1 >= 0 && begin2 >= 0 && end2 < history2.length ) {
		int counter1 = begin1;
		int counter2 = begin2;
		while ( history1[ counter1 ] == history2[ counter2 ] && ( counter1 < end1 ) ) {
			counter1++;
			counter2++;
		}
		if ( counter1 == end1 )
			return true;
		return false;

	}
	return false;
}

//begin metastrategy against competitionbots
private int metaStrategy() {
	for ( int c = 0; c < STRATNUMBER; c++ )
		opstrategymove[ game ][ c ] = strategymove[ game ][ c ];

	opstrategymove[ game ][ STRATNUMBER ] = beatPI();
	opstrategymove[ game ][ STRATNUMBER + 1 ] = beatDeBruijn();
	opstrategymove[ game ][ STRATNUMBER + 2 ] = beatEuler();
	opstrategymove[ game ][ STRATNUMBER + 3 ] = bayesselectionmoves[ 0 ];
	opstrategymove[ game ][ STRATNUMBER + 4 ] = bayesselectionmoves[ 1 ];
	opstrategymove[ game ][ STRATNUMBER + 5 ] = bayesselectionmoves[ 5 ];
	opstrategymove[ game ][ STRATNUMBER + 6 ] = bayesselectionmoves[ 6 ];
	opstrategymove[ game ][ STRATNUMBER + 7 ] = bayesselectionmoves[ 12 ];
	opstrategymove[ game ][ STRATNUMBER + 8 ] = bayesselectionmoves[ 13 ];
	opstrategymove[ game ][ STRATNUMBER + 9 ] = lastMoved( ME );
	opstrategymove[ game ][ STRATNUMBER + 10 ] = ( opstrategymove[ game ][ 57 ] + 1 ) % 3;
	opstrategymove[ game ][ STRATNUMBER + 11 ] = ( opstrategymove[ game ][ 57 ] + 1 ) % 3;
	opstrategymove[ game ][ STRATNUMBER + 12 ] = lastMoved( OP );
	opstrategymove[ game ][ STRATNUMBER + 13 ] = ( opstrategymove[ game ][ 60 ] + 1 ) % 3;
	opstrategymove[ game ][ STRATNUMBER + 14 ] = ( opstrategymove[ game ][ 60 ] + 1 ) % 3;

	int[] bestbiasscore = new int[ BIASNUMBER ];
	int[] biasscore = new int[ BIASNUMBER ];
	bestbiasscore[ bestbias ] = -10000;
	nocreditindex = -1;
	int curscore;
	for ( int a = 0; a < OPSTRATNUMBER; a++ ) {
		curscore = 0;

		if ( game > 0 )
			curscore += calcStratScore( a, 1 );

		if ( game > 3 )
			curscore += calcStratScore( a, 4 );
		biasscore[ 0 ] = curscore;
		biasscore[ 1 ] = curscore;
		biasscore[ 2 ] = curscore;
		biasscore[ 3 ] = curscore;
		biasscore[ 4 ] = curscore;

		if ( game > 9 )
			curscore = calcStratScore( a, 10 );
		biasscore[ 1 ] += curscore;
		biasscore[ 2 ] += curscore;
		biasscore[ 3 ] += curscore;
		biasscore[ 4 ] += curscore;

		if ( game > 29 )
			curscore = calcStratScore( a, 30 );
		biasscore[ 2 ] += curscore;
		biasscore[ 3 ] += curscore;
		biasscore[ 4 ] += curscore;

		if ( game > 99 )
			curscore = calcStratScore( a, 100 );
		biasscore[ 3 ] += curscore;
		biasscore[ 4 ] += curscore;

		if ( game > 249 )
			curscore = calcStratScore( a, 250 );
		biasscore[ 4 ] += curscore;
		biasscore[ 5 ] = curscore;

		for ( int b = 0; b < BIASNUMBER; b++ ) {
			if ( hasCredits( a ) ) {
				if ( biasscore[ b ] > bestbiasscore[ b ]
						|| ( biasscore[ b ] == bestbiasscore[ b ] && credits[ a ] > credits[ metaindex ] ) ) {
					bestbiasscore[ b ] = biasscore[ b ];
					biasmove[ b ] = opstrategymove[ game ][ a ];
					if ( b == bestbias )
						metaindex = a;
				}
			}

			if ( b == bestbias && biasscore[ b ] > bestgeneralscore ) {
				bestgeneralscore = biasscore[ b ];
				if ( !hasCredits( a ) ) {
					nocreditindex = a;
				}
			}
		}
	}
	if ( bestbiasscore[ bestbias ] < minimax && game > 200 )
		unknownstrat = true;
	else if ( bestbiasscore[ bestbias ] * .9 < minimax )
		minimax = (int) ( bestbiasscore[ bestbias ] * .9 );

	if ( unknownstrat )
		return ( opstrategymove[ game ][ metaindex ] + 2 ) % 3;
	return ( opstrategymove[ game ][ metaindex ] + 1 ) % 3;
}

//check whether a strategy might be used by the opponent
private boolean hasCredits( int index ) {
	return ( credits[ index ] > ( -4 ) );
}

//update metavariables
private void metaUpdate( int move ) {
	if ( opstrategymove[ game ][ metaindex ] == move ) {
		credits[ metaindex ]++;
		unknownstrat = false;
	}
	else {
		if ( !unknownstrat ) {
			credits[ metaindex ]--;
			if ( game > 200 )
				unknownstrat = true;
		}
		else
			unknownstrat = false;
	}
	if ( nocreditindex > -1 && opstrategymove[ game ][ nocreditindex ] == move )
		credits[ nocreditindex ]++;

	for ( int a = 0; a < BIASNUMBER; a++ ) {
		if ( biasmove[ a ] == move ) {
			biashis[ a ]++;
			if ( biashis[ a ] > biashis[ bestbias ] )
				bestbias = a;
		}
		else
			biashis[ a ]--;
	}
}

//update bayesvariables
private void bayesUpdate( int move ) {
	if ( game > 0 ) {
		my_difference[ game ] = ( move - my_history[ game - 1 ] + 3 ) % 3;
		opp_difference[ game ] = ( move - opp_history[ game - 1 ] + 3 ) % 3;
	}
	if ( game > 1 ) {
		bayesmylastmove[ move ][ my_history[ game - 1 ] ]++;
		bayesoplastmove[ move ][ opp_history[ game - 1 ] ]++;
		bayesmysecondlastmove[ move ][ my_history[ game - 2 ] ]++;
		bayesopsecondlastmove[ move ][ opp_history[ game - 2 ] ]++;
		bayesevengame[ move ][ game % 2 ]++;
		bayesmymostplayed[ move ][ freqStats( ME, true ) ]++;
		bayesopmostplayed[ move ][ freqStats( OP, true ) ]++;
		bayesmyleastplayed[ move ][ freqStats( ME, false ) ]++;
		bayesopleastplayed[ move ][ freqStats( OP, false ) ]++;
		bayesmylastdiff[ my_difference[ game ] ][ my_difference[ game - 1 ] ]++;
		bayesoplastdiff[ opp_difference[ game ] ][ opp_difference[ game - 1 ] ]++;
		bayesmysecondlastdiff[ my_difference[ game ] ][ my_difference[ game - 2 ] ]++;
		bayesopsecondlastdiff[ opp_difference[ game ] ][ opp_difference[ game - 2 ] ]++;
		bayeswonafter[ move ][ opp_history[ game - 1 ] ]++;
		if ( bayeswonafter[ ( move + 2 ) % 3 ][ opp_history[ game - 1 ] ] > 0 )
			bayeswonafter[ ( move + 2 ) % 3 ][ opp_history[ game - 1 ] ]--;
	}
}

//check successes of opponentstrategies
private int calcStratScore( int index, int period ) {
	int value = 0;
	for ( int i = game - period; i < game; i++ ) {
		if ( opstrategymove[ i ][ index ] == his_best_history[ i ] )
			value++;
		if ( opstrategymove[ i ][ index ] == his_worst_history[ i ] )
			value--;
	}
	return value;
}

//simple method often used by opponents
private int lastMoved( int mode ) {
	if ( game == 0 )
		return getRandomMove();
	else if ( mode == ME )
		return my_history[ game - 1 ];
	else
		return opp_history[ game - 1 ];
}

/**
 * @see com.anji.tournament.Player#getPlayerId()
 */
public String getPlayerId() {
	return "Mohammed Kaasch";
}

/**
 * @see com.anji.roshambo.RoshamboPlayer#getAuthor()
 */
public String getAuthor() {
	return "Tim Harbers";
}

/**
 * @see java.lang.Object#hashCode()
 */
public int hashCode() {
	return getPlayerId().hashCode();
}

}
