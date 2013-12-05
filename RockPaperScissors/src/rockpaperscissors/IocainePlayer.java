package rockpaperscissors;

/*
 * Entrant: Iocaine Powder (1) Dan Egnor (USA)
 * 
 * Winner of the First International RoShamBo Programming Competition
 * 
 * Iocaine Powder (from: http://ofb.net/~egnor/iocaine.html)
 * 
 * They were both poisoned.
 * 
 * Iocaine Powder is a heuristically designed compilation of strategies and meta-strategies,
 * entered in Darse Billings' excellent First International RoShamBo Programming Competition.
 * You may use its source code freely; I ask only that you give me credit for any derived works.
 * I attempt here to explain how it works.
 * 
 * Meta-Strategy
 * 
 * RoShamBo strategies attempt to predict what the opponent will do. Given a successful
 * prediction, it is easy to defeat the opponent (if you know they will play rock, you play
 * paper). However, straightforward prediction will often fail; the opponent may not be
 * vulnerable to prediction, or worse, they might have anticipated your predictive logic and
 * played accordingly. Iocaine Powder's meta-strategy expands any predictive algorithm P into
 * six possible strategies:
 * 
 * P.0: naive application Assume the opponent is vulnerable to prediction by P; predict their
 * next move, and play to beat it. If P predicts your opponent will play rock, play paper to
 * cover rock. This is the obvious application of P.
 * 
 * P.1: defeat second-guessing Assume the opponent thinks you will use P.0. If P predicts rock,
 * P.0 would play paper to cover rock, but the opponent could anticipate this move and play
 * scissors to cut paper. Instead, you play rock to dull scissors.
 * 
 * P.2: defeat triple-guessing Assume the opponent thinks you will use P.1. Your opponent thinks
 * you will play rock to dull the scissors they would have played to cut the paper you would
 * have played to cover the rock P would have predicted, so they will play paper to cover your
 * rock. But you one-up them, playing scissors to cut their paper.
 * 
 * At this point, you should be getting weary of the endless chain. "We could second-guess each
 * other forever," you say. But no; because of the nature of RoShamBo, P.3 recommends you play
 * paper -- just like P.0! So we're only left with these three strategies, each of which will
 * suggest a different alternative. (This may not seem useful to you, but have patience.)
 * 
 * P'.0: second-guess the opponent This strategy assumes the opponent uses P themselves against
 * you. Modify P (if necessary) to exchange the position of you and your opponent. If P'
 * predicts that you will play rock, you would expect your opponent to play paper, but instead
 * you play scissors.
 * 
 * P'.1, P'.2: variations on a theme As with P.1 and P.2, these represent "rotations" of the
 * basic idea, designed to counteract your opponent's second-guessing.
 * 
 * So, for even a single predictive algorithm P, we now have six possible strategies. One of
 * them may be correct -- but that's little more useful than saying "one of rock, scissors, or
 * paper will be the correct next move". We need a meta-strategy to decide between these six
 * strategies.
 * 
 * Iocaine Powder's basic meta-strategy is simple: Use past performance to judge future results.
 * 
 * The basic assumption made by this meta-strategy is that an opponent will not quickly vary
 * their strategy. Either they will play some predictive algorithm P, or they will play to
 * defeat it, or use some level of second-guessing; but whatever they do, they will do it
 * consistently. To take advantage of this (assumed) predictability, at every round Iocaine
 * Powder measures how well each of the strategies under consideration (six for every predictive
 * algorithm!) would have done, if it had played them. It assigns each one a score, using the
 * standard scoring scheme used by the tournament: +1 point if the strategy would have won the
 * hand, -1 if it would have lost, 0 if it would have drawn.
 * 
 * Then, to actually choose a move, Iocaine Powder simply picks the strategy variant with the
 * highest score to date.
 * 
 * The end result is that, for any given predictive technique, we will beat any contestant that
 * would be beaten by the technique, any contestant using the technique directly, and any
 * contestant designed to defeat the technique directly.
 * 
 * Strategies
 * 
 * All the meta-strategy in the world isn't useful without some predictive algorithms. Iocaine
 * Powder uses three predictors:
 * 
 * Random guess This "predictor" simply chooses a move at random. I include this algorithm as a
 * hedge; if someone is actually able to predict and defeat Iocaine Powder with any regularity,
 * before long the random predictor will be ranked with the highest score (since nobody can
 * defeat random!). At that point, the meta-strategy will ensure that the program "cuts its
 * losses" and starts playing randomly to avoid a devastating loss. (Thanks to Jesse Rosenstock
 * for discovering the necessity of such a hedge.)
 * 
 * Frequency analysis The frequency analyzer looks at the opponent's history, finds the move
 * they have made most frequently in the past, and predicts that they will choose it. While this
 * scores a resounding defeat again "Good Ole Rock", it isn't very useful against more
 * sophisticated opponents (which are usually quite symmetrical). I include it mostly to defeat
 * other competitors which use it as a predictive algorithm.
 * 
 * History matching This is easily the strongest predictor in Iocaine Powder's arsenal, and
 * variants of this technique are widely used in other strong entries. The version in Iocaine
 * Powder looks for a sequence in the past matching the last few moves. For example, if in the
 * last three moves, we played paper against rock, scissors against scissors, and scissors
 * against rock, the predictor will look for times in the past when the same three moves
 * occurred. (In fact, it looks for the longest match to recent history; a repeat of the last 30
 * moves is considered better than just the last 3 moves.) Once such a repeat is located, the
 * history matcher examines the move our opponent made afterwards, and assumes they will make it
 * again. (Thanks to Rudi Cilibrasi for introducing me to this algorithm, long ago.)
 * 
 * Once history is established, this predictor easily defeats many weak contestants. Perhaps
 * more importantly, the application of meta-strategy allows Iocaine to outguess other strong
 * opponents.
 * 
 * Details
 * 
 * If you look at Iocaine Powder's source code, you'll discover additional features which I
 * haven't treated in this simplified explanation. For example, the strategy arsenal includes
 * several variations of frequency analysis and history matching, each of which looks at a
 * different amount of history; in some cases, prediction using the last 5 moves is superior to
 * prediction using the last 500. We run each algorithm with history sizes of 1000, 100, 10, 5,
 * 2, and 1, and use the general meta-strategy to figure out which one does best.
 * 
 * In fact, Iocaine even varies the horizon of its meta-strategy analyzer! Strategies are
 * compared over the last 1000, 100, 10, 5, 2, and 1 moves, and a meta-meta-strategy decides
 * which meta-strategy to use (based on which picker performed best over the total history of
 * the game). This was designed to defeat contestants which switch strategy, and to outfox
 * variants of the simpler, older version of Iocaine Powder.
 * 
 * Summary
 * 
 * One must remember, when participating in a contest of this type, that we are not attempting
 * to model natural phenomena or predict user actions; instead, these programs are competing
 * against hostile opponents who are trying very hard not to be predictable. Iocaine Powder
 * doesn't use advanced statistical techniques or Markov models (though it could perhaps be
 * improved if it did), but it is very devious.
 * 
 * It is, however, by no means the last word. Iocaine may have defeated all comers in the first
 * tournament, but I have no doubt that my opponents will rise to the challenge in upcoming
 * events. ------------------------------------------------------------------------ Dan Egnor
 */

/*
 * -------------------------------------------------------------------------
 * 
 * Version translated to Java by Jeroen Donkers, 2002
 * 
 * -------------------------------------------------------------------------
 */

/**
 * http://www.cs.unimaas.nl/~donkers/games/roshambo03/
 * TODO - reset all fields
 */
public class IocainePlayer{

static final int ages[] = { 1000, 100, 10, 5, 2, 1 };

static final int num_ages = ages.length;

static final int my_hist = 0, opp_hist = 1, both_hist = 2;

int my_history[], opp_history[];

int lastmove, trials;

int bestmove[], best_length[];

result mres;

predict[][][] pr_history;

predict[][] pr_freq;

predict[] pr_meta;

predict pr_fixed, pr_random;

stat stats[];

// inner class result for stat.max and predict.do_predict

class result {

int move, score;
}

// Inner clas Stat

class stat {

int[][] sum;

int age;

stat() {
	sum = new int[ 1 + trials ][ 3 ];
	age = 0;
	for ( int i = 0; i < 3; ++i )
		sum[ age ][ i ] = 0;
}

void add( int i, int delta ) {
	sum[ age ][ i ] += delta;
}

void next() {
	if ( age < trials ) {
		++( age );
		for ( int i = 0; i < 3; ++i )
			sum[ age ][ i ] = sum[ age - 1 ][ i ];
	}
}

boolean max( int dage, result res ) {

	res.move = -1;
	for ( int i = 0; i < 3; ++i ) {
		int diff;
		if ( dage > age )
			diff = sum[ age ][ i ];
		else
			diff = sum[ age ][ i ] - sum[ age - dage ][ i ];
		if ( diff > res.score ) {
			res.score = diff;
			res.move = i;
		}
	}
	return ( -1 != res.move );
}

} // end class stats

// inner class predict

class predict {

stat st;

int last;

predict() {
	st = new stat();
	last = -1;
}

void do_predict( int olast, int guess ) {
	// olast: opponent's last move (-1 if none)
	// guess: algorithm's prediction of opponent's next move

	if ( -1 != olast ) {
		int diff = ( 3 + ( olast % 3 ) - ( last % 3 ) ) % 3;
		st.add( ( diff + 1 ) % 3, 1 ); // will_beat...
		st.add( ( diff + 2 ) % 3, -1 ); // will loose
		st.next();
	}
	last = guess;
}

void scan( int age, result res ) {
	int storemove = res.move;
	if ( st.max( age, res ) )
		res.move = ( ( last + res.move ) % 3 );
	else
		res.move = storemove;
}

} // end inner class predict

// History algorithm

int match_single( int i, int num, int[] history ) {
	int high = num;
	int low = i;
	while ( low > 0 && history[ low ] == history[ high ] ) {
		low--;
		high--;
	}
	return num - high;
}

int match_both( int i, int num ) {
	int j;
	for ( j = 0; ( j < i ) && ( opp_history[ num - j ] == opp_history[ i - j ] )
			&& ( my_history[ num - j ] == my_history[ i - j ] ); ++j ) {
		// Philip Tucker - all logic is in for loop
	}
	return j;
}

void do_history( int age, int best[] ) {
	int num = my_history[ 0 ];
	for ( int w = 0; w < 3; ++w )
		best[ w ] = best_length[ w ] = 0;
	for ( int i = num - 1; i > num - age && i > best_length[ my_hist ]; --i ) {
		int j = match_single( i, num, my_history );
		if ( j > best_length[ my_hist ] ) {
			best_length[ my_hist ] = j;
			best[ my_hist ] = i;
			if ( j > num / 2 )
				break;
		}
	}

	for ( int i = num - 1; i > num - age && i > best_length[ opp_hist ]; --i ) {
		int j = match_single( i, num, opp_history );
		if ( j > best_length[ opp_hist ] ) {
			best_length[ opp_hist ] = j;
			best[ opp_hist ] = i;
			if ( j > num / 2 )
				break;
		}
	}

	for ( int i = num - 1; i > num - age && i > best_length[ both_hist ]; --i ) {
		int j = match_both( i, num );
		if ( j > best_length[ both_hist ] ) {
			best_length[ both_hist ] = j;
			best[ both_hist ] = i;
			if ( j > num / 2 )
				break;
		}
	}
}

/**
 * @see java.lang.Object#toString()
 */
public String toString() {
	return getPlayerId();
}

/**
 * @see com.anji.tournament.Player#reset()
 */
public void reset() {
	reset( trials );
}

/**
 * @see com.anji.roshambo.RoshamboPlayer#reset(int)
 */
public void reset( int aTrials ) {

	// allocate all memory needed...

	my_history = new int[ aTrials + 1 ];
	opp_history = new int[ aTrials + 1 ];
	my_history[ 0 ] = opp_history[ 0 ] = 0;
	lastmove = 0;
	this.trials = aTrials;

	pr_history = new predict[ num_ages ][ 3 ][ 2 ];
	pr_freq = new predict[ num_ages ][ 2 ];
	pr_meta = new predict[ num_ages ];
	stats = new stat[ 2 ];

	for ( int i = 0; i < num_ages; i++ ) {
		pr_meta[ i ] = new predict();
		for ( int j = 0; j < 2; j++ ) {
			pr_freq[ i ][ j ] = new predict();
			for ( int k = 0; k < 3; k++ )
				pr_history[ i ][ k ][ j ] = new predict(); // order i-k-j!
		}
	}
	stats[ 0 ] = new stat();
	stats[ 1 ] = new stat();
	pr_fixed = new predict();
	pr_random = new predict();
	mres = new result();

	bestmove = new int[ 3 ];
	best_length = new int[ 3 ];
}

/**
 * @see com.anji.roshambo.RoshamboPlayer#storeMove(int, int)
 */
public void storeMove( int move, int score ) {
	opp_history[ ++opp_history[ 0 ] ] = move;
	my_history[ ++my_history[ 0 ] ] = lastmove;
}

    private int getRandomMove() {
        int randomMove = (int) Math.round(Math.random() * 2);
        return randomMove;
    }

/**
 * @see com.anji.roshambo.RoshamboPlayer#nextMove()
 */
public int nextMove() {

	int num = my_history[ 0 ];
	int last = ( num > 0 ) ? opp_history[ num ] : -1;
	int guess = getRandomMove();

	if ( num > 0 ) {
		stats[ 0 ].add( my_history[ num ], 1 );
		stats[ 1 ].add( opp_history[ num ], 1 );
	}

	for ( int a = 0; a < num_ages; ++a ) {
		do_history( ages[ a ], bestmove );
		for ( int w = 0; w < 3; ++w ) {
			int b = bestmove[ w ];
			if ( 0 == b ) {
				pr_history[ a ][ w ][ 0 ].do_predict( last, guess );
				pr_history[ a ][ w ][ 1 ].do_predict( last, guess );
				continue;
			}
			pr_history[ a ][ w ][ 0 ].do_predict( last, my_history[ b + 1 ] );
			pr_history[ a ][ w ][ 1 ].do_predict( last, opp_history[ b + 1 ] );
		}

		for ( int p = 0; p < 2; ++p ) {
			mres.score = -1;
			if ( stats[ p ].max( ages[ a ], mres ) )
				pr_freq[ a ][ p ].do_predict( last, mres.move );
			else
				pr_freq[ a ][ p ].do_predict( last, guess );
		}
	}

	pr_random.do_predict( last, guess );
	pr_fixed.do_predict( last, 0 );

	for ( int a = 0; a < num_ages; ++a ) {
		mres.score = -1;
		mres.move = -1;

		for ( int aa = 0; aa < num_ages; ++aa ) {
			for ( int p = 0; p < 2; ++p ) {
				for ( int w = 0; w < 3; ++w ) {
					pr_history[ aa ][ w ][ p ].scan( ages[ a ], mres );
				}
				pr_freq[ aa ][ p ].scan( ages[ a ], mres );
			}
		}

		pr_random.scan( ages[ a ], mres );
		pr_fixed.scan( ages[ a ], mres );
		pr_meta[ a ].do_predict( last, mres.move );
	}

	mres.score = -1;
	mres.move = -1;

	for ( int a = 0; a < num_ages; ++a ) {
		pr_meta[ a ].scan( trials, mres );
	}

	lastmove = mres.move;
	return mres.move;
}

/**
 * @see com.anji.tournament.Player#getPlayerId()
 */
public String getPlayerId() {
	return "Iocaine Powder";
}

/**
 * @see com.anji.roshambo.RoshamboPlayer#getAuthor()
 */
public String getAuthor() {
	return "Dan Egnor";
}

/**
 * @see java.lang.Object#hashCode()
 */
public int hashCode() {
	return getPlayerId().hashCode();
}

}
