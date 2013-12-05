package rockpaperscissors;

/**
 * http://www.cs.unimaas.nl/~donkers/games/roshambo03/
 * TODO - reset fields
 */
public class MarshalbotPlayer{

private final int historylength = 1000;

private final int nrofpatterns = 50;

    public static final int ROCK = 0;
    public static final int PAPER = 1;
    public static final int SCISSORS = 2;
/**
 * @see com.anji.tournament.Player#reset()
 */
public void reset() {
	reset( history[ 0 ].length );
}

/**
 * @see com.anji.roshambo.RoshamboPlayer#reset(int)
 */
public void reset( int trials ) {
	history = new int[ 2 ][ trials ];
	movenr = totscore = 0;
	patterns = new int[ nrofpatterns ][ historylength ];
	p_move = new int[ nrofpatterns ];
	p_chosen = new int[ nrofpatterns ];
	pi_index = 0;
	stats = new int[ 2 ][ 3 ];
	stats_nxt = new int[ 3 ][ 3 ];
	f_lastset = 0;
	unbeatable = false;
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
	//System.out.println("my: " + history[MINE][movenr] + " his: " + move + " ("+ score +")
	// "+p_move[24]);
	totscore += score;
	for ( int i = 0; i < patterns.length; i++ )
		patterns[ i ][ movenr % historylength ] = score( prediction( i ), move );
	history[ HIS ][ movenr ] = move;
	movenr++;
	updatePiIndex();
	setFreq();
	calculated = false;
}

/**
 * @see com.anji.roshambo.RoshamboPlayer#nextMove()
 */
public int nextMove() {
	int nextmove;
	//sets unbeatable flag if no pattern is found
	unbeatable = true;
	for ( int i = 0; i < nrofpatterns; i++ ) {
		if ( ( patternscore( i ) > movenr / 5 ) || ( patternscore( i ) > historylength / 5 ) ) {
			unbeatable = false;
			break;
		}
	}
	//determine the next move
	if ( movenr < 3 || unbeatable )
		nextmove = Coin.flip();

	else {
		int best_p = bestpattern();
		p_chosen[ best_p ]++;
		nextmove = prediction( best_p );
	}
	//check op mogelijke illegale zet
	if ( ( nextmove > 2 ) || ( nextmove < 0 ) ) {
		nextmove = Coin.flip();
	}
	//store move
	history[ MINE ][ movenr ] = nextmove;

	if ( !unbeatable )
		bp++;
	else
		ubp++;
	return nextmove;
}

private void calculate() {
	int i, a;
	i = 0;

	p_move[ i++ ] = ROCK; //0
	p_move[ i++ ] = PAPER;
	p_move[ i++ ] = SCISSORS;
	p_move[ i++ ] = historySearch( HIS, 3, 3, 1 );
	a = i - 1; //3
	p_move[ i++ ] = ( p_move[ a ] + 1 ) % 3;
	p_move[ i++ ] = ( p_move[ a ] + 2 ) % 3;
	p_move[ i++ ] = historySearch( MINE, 3, 3, 1 );
	a = i - 1; //6
	p_move[ i++ ] = ( p_move[ a ] + 1 ) % 3;
	p_move[ i++ ] = ( p_move[ a ] + 2 ) % 3;
	p_move[ i++ ] = historySearch( HIS, 0, 9, -1 );
	a = i - 1; //9
	p_move[ i++ ] = ( p_move[ a ] + 1 ) % 3;
	p_move[ i++ ] = ( p_move[ a ] + 2 ) % 3;
	p_move[ i++ ] = historySearch( MINE, 0, 9, -1 );
	a = i - 1; //12
	p_move[ i++ ] = ( p_move[ a ] + 1 ) % 3;
	p_move[ i++ ] = ( p_move[ a ] + 2 ) % 3;
	p_move[ i++ ] = doubleHistorySearch( MINE );
	a = i - 1; //15
	p_move[ i++ ] = ( p_move[ a ] + 1 ) % 3;
	p_move[ i++ ] = ( p_move[ a ] + 2 ) % 3;
	p_move[ i++ ] = doubleHistorySearch( HIS );
	a = i - 1; //18
	p_move[ i++ ] = ( p_move[ a ] + 1 ) % 3;
	p_move[ i++ ] = ( p_move[ a ] + 2 ) % 3;
	p_move[ i++ ] = stats_max( HIS );
	a = i - 1; //21
	p_move[ i++ ] = ( p_move[ a ] + 1 ) % 3;
	p_move[ i++ ] = ( p_move[ a ] + 2 ) % 3;
	p_move[ i++ ] = stats_max( MINE );
	a = i - 1; //24
	p_move[ i++ ] = ( p_move[ a ] + 1 ) % 3;
	p_move[ i++ ] = ( p_move[ a ] + 2 ) % 3;
	p_move[ i++ ] = stats_best( HIS );
	a = i - 1; //27
	p_move[ i++ ] = ( p_move[ a ] + 1 ) % 3;
	p_move[ i++ ] = ( p_move[ a ] + 2 ) % 3;
	p_move[ i++ ] = stats_best( MINE );
	a = i - 1; //30
	p_move[ i++ ] = ( p_move[ a ] + 1 ) % 3;
	p_move[ i++ ] = ( p_move[ a ] + 2 ) % 3;
	p_move[ i++ ] = stats_nxt_best();
	a = i - 1; //33
	p_move[ i++ ] = ( p_move[ a ] + 1 ) % 3;
	p_move[ i++ ] = ( p_move[ a ] + 2 ) % 3;
	p_move[ i++ ] = detectcopy( HIS );
	a = i - 1; //36
	p_move[ i++ ] = ( p_move[ a ] + 1 ) % 3;
	p_move[ i++ ] = ( p_move[ a ] + 2 ) % 3;
	p_move[ i++ ] = detectcopy( MINE );
	a = i - 1; //39
	p_move[ i++ ] = ( p_move[ a ] + 1 ) % 3;
	p_move[ i++ ] = ( p_move[ a ] + 2 ) % 3;
	p_move[ i++ ] = copyrotate( HIS );
	a = i - 1; //42
	p_move[ i++ ] = ( p_move[ a ] + 1 ) % 3;
	p_move[ i++ ] = ( p_move[ a ] + 2 ) % 3;
	p_move[ i++ ] = copyrotate( MINE );
	a = i - 1; //45
	p_move[ i++ ] = ( p_move[ a ] + 1 ) % 3;
	p_move[ i++ ] = ( p_move[ a ] + 2 ) % 3;
	p_move[ i++ ] = beat_deBruijn(); //48
	p_move[ i++ ] = beat_Pi();
	calculated = true;
}

private int prediction( int p ) {
	//calculates next move for pattern p
	if ( !calculated )
		calculate();
	return p_move[ p ];
}

private int score( int mymove, int oppmove ) {
	int score = 0;
	if ( mymove < 0 )
		score = 0;
	else if ( oppmove != mymove ) {
		if ( ( mymove == PAPER && oppmove == ROCK )
				|| ( mymove == ROCK && oppmove == SCISSORS )
				|| ( mymove == SCISSORS && oppmove == PAPER ) )
			score = 1;
		else
			score = -1;
	}

	return score;
}

private int patternscore( int i ) {
	int total = 0;
	int weight = 10;
	for ( int j = 0; j < patterns[ i ].length; j++ ) {
		if ( j == 1 || j == 2 || j == 5 || j == 10 || j == 20 || j == 40 || j == 80 || j == 150
				|| j == 300 || j == 1000 )
			weight--;
		total += weight * patterns[ i ][ j ];
	}
	return total;
}

private int bestpattern() {
	//searches highest value in array patterns
	int max = patternscore( 0 );
	int maxidx = 0;
	for ( int i = 1; i < nrofpatterns; i++ ) {
		if ( patternscore( i ) > max && prediction( i ) >= 0 ) {
			max = patternscore( i );
			maxidx = i;
		}
	}
	return maxidx;
}

private void setFreq() {
	if ( f_lastset < movenr ) {
		stats[ HIS ][ history[ HIS ][ movenr - 1 ] ]++;
		stats[ MINE ][ history[ MINE ][ movenr - 1 ] ]++;

		if ( movenr > 1 )
			stats_nxt[ history[ HIS ][ movenr - 2 ] ][ history[ HIS ][ movenr - 1 ] ]++;
		f_lastset = movenr;
	}

}

/*
 * returns the move of who_nextmove that is expected to follow the current sequence of elements
 * in history[who_hist] according to past patterns who_patt=whose recent pattern to compare with
 * who_hist=what history array to examine who_nextmove = whose next move to predict minfound =
 * minimum number of moves in a pattern maxfound = maximum number of moves in a pattern
 * searchdepth = search stops if any probability exceeds the depth (-1 to search till the end) d =
 * the factor difference there must at least be between the probabilities to make a decision
 * buzz = number to add to every number in the pattern to match sv = bias for recent patterns
 * (1,0)
 */
private int historySearch( int who, int minfound, int maxfound, int searchdepth ) {
	return historySearch( who, who, HIS, minfound, maxfound, 1, movenr, searchdepth, 0, 1 );
}

private int historySearch( int who_patt, int who_hist, int who_nextmove, int minfound,
		int maxfound, int minback, int maxback, int searchdepth, int buzz, int sv ) {
	int[] probability = new int[ 3 ];
	if ( searchdepth == -1 )
		searchdepth = Integer.MAX_VALUE;
	int lastidx = movenr - 1; //index of last move in history array
	int start; //index of first element of the sequence currently evaluating
	int found; //number of elements found that coincide with the most recent pattern

	for ( start = minback; ( start < lastidx - minfound ) && ( start <= maxback ); start++ ) {
		found = 0;
		//starting at start, counts the number of spots the pattern coincides with the current
		// pattern
		while ( ( ( lastidx - start - found ) > 0 )
				&& ( history[ who_hist ][ lastidx - start - found ] == ( history[ who_patt ][ lastidx
						- found ] + buzz ) % 3 ) && ( found <= maxfound ) )
			found++;

		if ( found > minfound ) {
			int move = history[ who_nextmove ][ lastidx - start + 1 ];
			probability[ move ] += ( 1000 * ( found - minfound ) ^ 2 ) / ( sv * start + 1 );
			if ( probability[ move ] > searchdepth )
				break;
		}
	}

	return bestmove( probability );

}

private int bestmove( int[] moves ) {
	int bestmove = -1;
	int bestscore = -1;
	if ( bestscore < moves[ SCISSORS ] - moves[ PAPER ] ) {
		bestmove = ROCK;
		bestscore = moves[ SCISSORS ] - moves[ PAPER ];
	}
	if ( bestscore < moves[ PAPER ] - moves[ ROCK ] ) {
		bestmove = SCISSORS;
		bestscore = moves[ PAPER ] - moves[ ROCK ];
	}
	if ( bestscore < moves[ ROCK ] - moves[ SCISSORS ] ) {
		bestmove = PAPER;
		bestscore = moves[ ROCK ] - moves[ PAPER ];
	}
	return bestmove;
}

private int doubleHistorySearch( int who ) {
	int maxfound = 30;
	int lastidx = movenr - 1; //index of last move in history array
	int start; //index of first element of the sequence currently evaluating
	int found; //number of elements found that coincide with the most recent pattern
	int bestmatch = 0;
	int[] nextmoves = new int[ 3 ];

	for ( start = 1; start < lastidx; start++ ) {
		found = 0;
		//starting at start, counts the number of spots the pattern coincides with the current
		// pattern
		while ( ( ( lastidx - start - found ) > 0 ) && ( found <= maxfound )
				&& ( history[ HIS ][ lastidx - start - found ] == history[ HIS ][ lastidx - found ] )
				&& ( history[ MINE ][ lastidx - start - found ] == history[ MINE ][ lastidx - found ] ) )
			found++;

		if ( found == bestmatch )
			nextmoves[ history[ who ][ lastidx - start + 1 ] ]++;
		else if ( found > bestmatch ) {
			bestmatch = found;
			nextmoves = new int[ 3 ];
			nextmoves[ history[ who ][ lastidx - start + 1 ] ]++;
		}
		if ( found == maxfound )
			break;
	}
	return bestmove( nextmoves );
}

private int detectcopy( int who ) {
	if ( movenr < 2 )
		return Coin.flip();
	int length = 10;
	if ( movenr < 5 )
		length = movenr - 1;
	if ( movenr < 10 )
		length = movenr - 3;
	int other = ( who + 1 ) % 2;
	for ( int i = 1; i < 5; i++ ) {
		int a = historySearch( who, other, other, length, length, i, i, 1, 0, 1 );
		if ( a != -3 )
			return a;
		int b = historySearch( who, other, other, length, length, i, i, 1, 1, 1 );
		if ( b != -3 )
			return b;
		int c = historySearch( who, other, other, length, length, i, i, 1, 2, 1 );
		if ( c != -3 )
			return c;
	}
	return -3;
}

private int copyrotate( int who ) {
	if ( movenr == 0 )
		return Coin.flip();
	return ( history[ who ][ movenr - 1 ] + movenr ) % 3;
}

private int stats_max( int who ) {
	int move;
	if ( ( stats[ who ][ ROCK ] >= stats[ who ][ PAPER ] )
			&& ( stats[ who ][ ROCK ] >= stats[ who ][ SCISSORS ] ) )
		move = ROCK;
	else if ( ( stats[ who ][ SCISSORS ] >= stats[ who ][ PAPER ] ) )
		move = SCISSORS;
	else
		move = PAPER;
	return move;
}

private int stats_best( int who ) {
	return bestmove( stats[ who ] );
}

private int stats_nxt_best() {
	if ( movenr > 1 ) {
		int lastmve = history[ HIS ][ movenr - 1 ];
		return bestmove( stats_nxt[ lastmve ] );
	}
	return 0;
}

/**
 * @see com.anji.tournament.Player#getPlayerId()
 */
public String getPlayerId() {
	return "MarshalBot v2.0";
}

/**
 * @see com.anji.roshambo.RoshamboPlayer#getAuthor()
 */
public String getAuthor() {
	return "Vincent de Boer";
}

int ubp, bp;

/*
 * methods beat_Pi() and beat_deBruijn() were taken from Roshambo bot "Urza" by Martijn Muurman
 * Enno Peters
 * 
 * they were designed to beat the corresponding bots deBruijn and Pi that are otherwisely
 * unbeatable
 */

int db_table[] = /* De Bruijn sequence: */
{ 1, 0, 2, 0, 0, 2, 0, 2, 0, 1, 1, 0, 0, 2, 2, 1, 0, 0, 1, 1, 2, 2, 0, 0, 1, 2, 1, 0, 2, 2, 2,
		2, 0, 1, 2, 0, 2, 2, 0, 2, 1, 1, 2, 1, 1, 0, 1, 1, 1, 2, 0, 0, 0, 0, 2, 1, 0, 1, 0, 1, 2,
		2, 1, 2, 0, 1, 0, 0, 0, 1, 0, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 1, 0, 2,
		2, 2, 0, 0, 2, 2, 0, 2, 0, 1, 0, 1, 1, 0, 2, 1, 1, 2, 2, 2, 2, 1, 1, 1, 2, 0, 1, 2, 2, 1,
		2, 0, 0, 0, 1, 0, 0, 0, 0, 2, 0, 2, 2, 1, 0, 0, 1, 2, 1, 2, 2, 0, 1, 1, 2, 1, 1, 0, 0, 2,
		1, 0, 1, 2, 0, 2, 1, 2, 1, 0, 2, 1, 1, 2, 0, 0, 1, 0, 1, 2, 2, 0, 1, 0, 0, 2, 0, 1, 2, 0,
		1, 1, 2, 1, 1, 1, 1, 0, 2, 0, 2, 1, 0, 2, 2, 0, 2, 2, 2, 2, 0, 0, 0, 1, 2, 1, 2, 2, 2, 1,
		1, 0, 1, 1, 0, 0, 0, 0, 2, 1, 2, 0, 2, 0, 0, 2, 2, 1, 0, 0, 1, 1, 1, 2, 2, 1, 2, 1, 0, 1,
		0, 2, 1, 0, 1, 0, 2, 0, 2, 0, 0, 1, 2, 2, 2, 0, 2, 1, 0, 0, 1, 1, 1, 2, 2, 1, 1, 0, 2, 2,
		0, 0, 0, 2, 2, 2, 2, 1, 2, 2, 0, 1, 2, 0, 0, 2, 0, 1, 1, 2, 1, 2, 1, 1, 1, 1, 0, 0, 2, 1,
		2, 0, 1, 0, 0, 0, 0, 1, 0, 1, 1, 0, 1, 2, 1, 0, 2, 1, 1, 2, 0, 2, 2, 2, 2, 1, 1, 1, 1, 0,
		0, 2, 0, 2, 2, 2, 1, 2, 1, 0, 2, 1, 0, 0, 0, 0, 2, 1, 1, 2, 2, 1, 0, 1, 0, 0, 1, 1, 1, 2,
		1, 1, 0, 1, 2, 2, 2, 2, 0, 0, 1, 2, 0, 2, 0, 1, 2, 1, 2, 0, 1, 0, 1, 1, 2, 0, 0, 0, 1, 0,
		2, 2, 0, 2, 1, 2, 2, 0, 1, 1, 0, 2, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2, 1, 1, 0, 0, 1, 1, 1,
		1, 0, 2, 0, 2, 1, 2, 0, 2, 2, 1, 2, 2, 2, 1, 1, 1, 2, 1, 2, 1, 0, 0, 2, 0, 1, 1, 0, 1, 0,
		2, 1, 0, 2, 2, 2, 2, 0, 2, 0, 0, 2, 2, 0, 0, 1, 2, 2, 1, 0, 1, 1, 2, 0, 1, 2, 1, 1, 2, 2,
		0, 1, 0, 1, 2, 2, 2, 0, 2, 0, 0, 2, 0, 2, 1, 2, 2, 2, 2, 0, 0, 0, 0, 2, 2, 1, 0, 0, 0, 1,
		2, 0, 1, 2, 1, 2, 0, 0, 1, 0, 2, 0, 1, 0, 0, 2, 1, 0, 1, 2, 2, 1, 1, 2, 0, 2, 2, 2, 1, 2,
		1, 0, 2, 2, 0, 1, 1, 0, 2, 1, 1, 0, 0, 1, 1, 2, 1, 1, 1, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 2,
		1, 0, 0, 0, 0, 1, 1, 0, 2, 1, 2, 1, 2, 2, 2, 0, 0, 1, 2, 0, 1, 0, 1, 2, 1, 1, 2, 2, 0, 2,
		0, 2, 1, 1, 0, 0, 1, 0, 2, 0, 0, 2, 0, 1, 1, 2, 0, 2, 2, 1, 1, 1, 1, 0, 1, 0, 0, 2, 2, 2,
		2, 1, 2, 0, 0, 0, 2, 1, 0, 2, 2, 0, 1, 2, 2, 1, 0, 2, 1, 0, 1, 0, 1, 1, 1, 1, 2, 1, 1, 0,
		1, 2, 1, 2, 2, 2, 2, 1, 2, 0, 0, 0, 1, 1, 2, 0, 2, 0, 2, 1, 0, 0, 0, 0, 2, 0, 0, 1, 0, 0,
		2, 2, 2, 0, 0, 2, 1, 1, 2, 2, 0, 1, 2, 0, 1, 1, 0, 0, 1, 2, 2, 1, 1, 1, 0, 2, 0, 1, 0, 2,
		2, 0, 2, 2, 1, 0, 2, 1, 2, 2, 2, 1, 0, 1, 0, 2, 2, 1, 2, 0, 2, 1, 0, 2, 0, 0, 0, 0, 1, 2,
		1, 0, 0, 2, 0, 2, 2, 0, 1, 0, 1, 1, 2, 1, 1, 0, 0, 1, 0, 0, 0, 2, 1, 1, 2, 0, 0, 2, 2, 2,
		2, 0, 0, 1, 1, 1, 0, 2, 1, 2, 1, 2, 2, 1, 1, 1, 1, 2, 2, 0, 2, 0, 1, 2, 0, 1, 1, 0, 1, 1,
		0, 0, 1, 1, 0, 1, 2, 0, 1, 2, 1, 2, 2, 1, 0, 0, 2, 0, 2, 1, 0, 1, 0, 2, 2, 0, 1, 1, 2, 1,
		0, 2, 0, 0, 1, 0, 1, 1, 1, 2, 2, 2, 2, 1, 2, 0, 2, 2, 1, 1, 2, 0, 0, 2, 1, 2, 1, 1, 1, 1,
		0, 2, 1, 1, 0, 0, 0, 0, 2, 2, 2, 0, 0, 0, 1, 2, 2, 0, 2, 0, 2, 2, 0, 2, 1, 1, 2, 0, 2, 0,
		0, 1, 1, 1, 0, 0, 1, 2, 1, 1, 0, 1, 1, 0, 2, 2, 0, 0, 2, 2, 1, 1, 1, 1, 2, 1, 2, 1, 0, 2,
		0, 2, 2, 2, 2, 1, 2, 0, 0, 0, 1, 0, 0, 2, 1, 0, 0, 0, 0, 2, 0, 1, 1, 2, 2, 2, 0, 1, 2, 2,
		1, 0, 1, 0, 1, 2, 0, 1, 0, 2, 1, 0, 2, 0, 2, 1, 1, 1, 0, 0, 2, 2, 2, 0, 1, 1, 2, 2, 1, 2,
		0, 0, 0, 1, 0, 1, 2, 1, 0 };

private int beat_deBruijn() {
	return ( db_table[ ( movenr + 1 ) % 1000 ] + 1 ) % 3;
}

int pi_index;

int pi_table[] = { 3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5, 8, 9, 7, 9, 3, 2, 3, 8, 4, 6, 2, 6, 4, 3,
		3, 8, 3, 2, 7, 9, 5, 0, 2, 8, 8, 4, 1, 9, 7, 1, 6, 9, 3, 9, 9, 3, 7, 5, 1, 0, 5, 8, 2, 0,
		9, 7, 4, 9, 4, 4, 5, 9, 2, 3, 0, 7, 8, 1, 6, 4, 0, 6, 2, 8, 6, 2, 0, 8, 9, 9, 8, 6, 2, 8,
		0, 3, 4, 8, 2, 5, 3, 4, 2, 1, 1, 7, 0, 6, 7, 9, 8, 2, 1, 4, 8, 0, 8, 6, 5, 1, 3, 2, 8, 2,
		3, 0, 6, 6, 4, 7, 0, 9, 3, 8, 4, 4, 6, 0, 9, 5, 5, 0, 5, 8, 2, 2, 3, 1, 7, 2, 5, 3, 5, 9,
		4, 0, 8, 1, 2, 8, 4, 8, 1, 1, 1, 7, 4, 5, 0, 2, 8, 4, 1, 0, 2, 7, 0, 1, 9, 3, 8, 5, 2, 1,
		1, 0, 5, 5, 5, 9, 6, 4, 4, 6, 2, 2, 9, 4, 8, 9, 5, 4, 9, 3, 0, 3, 8, 1, 9, 6, 4, 4, 2, 8,
		8, 1, 0, 9, 7, 5, 6, 6, 5, 9, 3, 3, 4, 4, 6, 1, 2, 8, 4, 7, 5, 6, 4, 8, 2, 3, 3, 7, 8, 6,
		7, 8, 3, 1, 6, 5, 2, 7, 1, 2, 0, 1, 9, 0, 9, 1, 4, 5, 6, 4, 8, 5, 6, 6, 9, 2, 3, 4, 6, 0,
		3, 4, 8, 6, 1, 0, 4, 5, 4, 3, 2, 6, 6, 4, 8, 2, 1, 3, 3, 9, 3, 6, 0, 7, 2, 6, 0, 2, 4, 9,
		1, 4, 1, 2, 7, 3, 7, 2, 4, 5, 8, 7, 0, 0, 6, 6, 0, 6, 3, 1, 5, 5, 8, 8, 1, 7, 4, 8, 8, 1,
		5, 2, 0, 9, 2, 0, 9, 6, 2, 8, 2, 9, 2, 5, 4, 0, 9, 1, 7, 1, 5, 3, 6, 4, 3, 6, 7, 8, 9, 2,
		5, 9, 0, 3, 6, 0, 0, 1, 1, 3, 3, 0, 5, 3, 0, 5, 4, 8, 8, 2, 0, 4, 6, 6, 5, 2, 1, 3, 8, 4,
		1, 4, 6, 9, 5, 1, 9, 4, 1, 5, 1, 1, 6, 0, 9, 4, 3, 3, 0, 5, 7, 2, 7, 0, 3, 6, 5, 7, 5, 9,
		5, 9, 1, 9, 5, 3, 0, 9, 2, 1, 8, 6, 1, 1, 7, 3, 8, 1, 9, 3, 2, 6, 1, 1, 7, 9, 3, 1, 0, 5,
		1, 1, 8, 5, 4, 8, 0, 7, 4, 4, 6, 2, 3, 7, 9, 9, 6, 2, 7, 4, 9, 5, 6, 7, 3, 5, 1, 8, 8, 5,
		7, 5, 2, 7, 2, 4, 8, 9, 1, 2, 2, 7, 9, 3, 8, 1, 8, 3, 0, 1, 1, 9, 4, 9, 1, 2, 9, 8, 3, 3,
		6, 7, 3, 3, 6, 2, 4, 4, 0, 6, 5, 6, 6, 4, 3, 0, 8, 6, 0, 2, 1, 3, 9, 4, 9, 4, 6, 3, 9, 5,
		2, 2, 4, 7, 3, 7, 1, 9, 0, 7, 0, 2, 1, 7, 9, 8, 6, 0, 9, 4, 3, 7, 0, 2, 7, 7, 0, 5, 3, 9,
		2, 1, 7, 1, 7, 6, 2, 9, 3, 1, 7, 6, 7, 5, 2, 3, 8, 4, 6, 7, 4, 8, 1, 8, 4, 6, 7, 6, 6, 9,
		4, 0, 5, 1, 3, 2, 0, 0, 0, 5, 6, 8, 1, 2, 7, 1, 4, 5, 2, 6, 3, 5, 6, 0, 8, 2, 7, 7, 8, 5,
		7, 7, 1, 3, 4, 2, 7, 5, 7, 7, 8, 9, 6, 0, 9, 1, 7, 3, 6, 3, 7, 1, 7, 8, 7, 2, 1, 4, 6, 8,
		4, 4, 0, 9, 0, 1, 2, 2, 4, 9, 5, 3, 4, 3, 0, 1, 4, 6, 5, 4, 9, 5, 8, 5, 3, 7, 1, 0, 5, 0,
		7, 9, 2, 2, 7, 9, 6, 8, 9, 2, 5, 8, 9, 2, 3, 5, 4, 2, 0, 1, 9, 9, 5, 6, 1, 1, 2, 1, 2, 9,
		0, 2, 1, 9, 6, 0, 8, 6, 4, 0, 3, 4, 4, 1, 8, 1, 5, 9, 8, 1, 3, 6, 2, 9, 7, 7, 4, 7, 7, 1,
		3, 0, 9, 9, 6, 0, 5, 1, 8, 7, 0, 7, 2, 1, 1, 3, 4, 9, 9, 9, 9, 9, 9, 8, 3, 7, 2, 9, 7, 8,
		0, 4, 9, 9, 5, 1, 0, 5, 9, 7, 3, 1, 7, 3, 2, 8, 1, 6, 0, 9, 6, 3, 1, 8, 5, 9, 5, 0, 2, 4,
		4, 5, 9, 4, 5, 5, 3, 4, 6, 9, 0, 8, 3, 0, 2, 6, 4, 2, 5, 2, 2, 3, 0, 8, 2, 5, 3, 3, 4, 4,
		6, 8, 5, 0, 3, 5, 2, 6, 1, 9, 3, 1, 1, 8, 8, 1, 7, 1, 0, 1, 0, 0, 0, 3, 1, 3, 7, 8, 3, 8,
		7, 5, 2, 8, 8, 6, 5, 8, 7, 5, 3, 3, 2, 0, 8, 3, 8, 1, 4, 2, 0, 6, 1, 7, 1, 7, 7, 6, 6, 9,
		1, 4, 7, 3, 0, 3, 5, 9, 8, 2, 5, 3, 4, 9, 0, 4, 2, 8, 7, 5, 5, 4, 6, 8, 7, 3, 1, 1, 5, 9,
		5, 6, 2, 8, 6, 3, 8, 8, 2, 3, 5, 3, 7, 8, 7, 5, 9, 3, 7, 5, 1, 9, 5, 7, 7, 8, 1, 8, 5, 7,
		7, 8, 0, 5, 3, 2, 1, 7, 1, 2, 2, 6, 8, 0, 6, 6, 1, 3, 0, 0, 1, 9, 2, 7, 8, 7, 6, 6, 1, 1,
		1, 9, 5, 9, 0, 9, 2, 1, 6, 4, 2, 0, 1, 9, 8, 9, 3, 8, 0, 9, 5, 2, 5, 7, 2, 0, 1, 0, 6, 5,
		4, 8, 5, 8, 6, 3, 2, 7, 8, 8, 6, 5, 9, 3, 6, 1, 5, 3, 3, 8, 1, 8, 2, 7, 9, 6, 8, 2, 3, 0,
		3, 0, 1, 9, 5, 2, 0, 3, 5, 3, 0, 1, 8, 5, 2, 9, 6, 8, 9, 9, 5, 7, 7, 3, 6, 2, 2, 5, 9, 9,
		4, 1, 3, 8, 9, 1, 2, 4, 9, 7, 2, 1, 7, 7, 5, 2, 8, 3, 4, 7, 9, 1, 3, 1, 5, 1, 5, 5, 7, 4,
		8, 5, 7, 2, 4, 2, 4, 5, 4, 1, 5, 0, 6, 9, 5, 9, 5, 0, 8, 2, 9, 5, 3, 3, 1, 1, 6, 8, 6, 1,
		7, 2, 7, 8, 5, 5, 8, 8, 9, 0, 7, 5, 0, 9, 8, 3, 8, 1, 7, 5, 4, 6, 3, 7, 4, 6, 4, 9, 3, 9,
		3, 1, 9, 2, 5, 5, 0, 6, 0, 4, 0, 0, 9, 2, 7, 7, 0, 1, 6, 7, 1, 1, 3, 9, 0, 0, 9, 8, 4, 8,
		8, 2, 4, 0, 1 };

private int beat_Pi() {
	int result = ( pi_table[ pi_index ] + 1 ) % 3;
	return ( result );
}

private void updatePiIndex() {
	pi_index++;
	pi_index %= 1200;
	while ( pi_table[ pi_index ] == 0 )
		pi_index++;
	pi_index %= 1200;
}

//attributen
private int movenr, totscore;

//private int nextmove, totscore, self, opp;
//array with all previous moves
private int[][] history = new int[ 2 ][ 1 ];

//array with scoring of the available patterns
private int[][] patterns;

private boolean calculated;

private int[] p_chosen;

private int[] p_move;

private int[][] stats;

private int[][] stats_nxt;

private int f_lastset;

private final int HIS = 1;

private final int MINE = 0;

private boolean unbeatable = false;

/**
 * @see java.lang.Object#hashCode()
 */
public int hashCode() {
	return getPlayerId().hashCode();
}
}
