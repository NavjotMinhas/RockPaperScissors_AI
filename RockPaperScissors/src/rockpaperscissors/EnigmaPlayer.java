package rockpaperscissors;

import java.util.Arrays;

/**
 * http://www.cs.unimaas.nl/~donkers/games/roshambo03/
 */
public class EnigmaPlayer{

int games = 0;

int numberOfGames = 0;

int moves = 0;

int randomMoves = 0;

int moveType = 0;

int lookBack = 1;

int lookBackSize = 16;

byte results[][];

byte myMove = 0;

int wins = 0;

int who = 0;

int score = 0;

int myDistribution[] = { 0, 0, 0 };

int myDistSize = 0;

int playDistribution[] = { 0, 0, 0 };

    public static final int ROCK = 0;
    public static final int PAPER = 1;
    public static final int SCISSORS = 2;

/**
 * @see com.anji.roshambo.RoshamboPlayer#reset(int)
 */
public void reset( int trials ) {
	results = new byte[ 4 ][ trials + 1 ];

	wins = 0;
	lookBack = 1;

	moveType = 0; // Random for the first 30% of game
	randomMoves = ( 40 * trials ) / 100;

	if ( trials < 100 ) {
		lookBackSize = 3;
		randomMoves = 5;
	}
	else
		lookBackSize = 16;

	numberOfGames = trials;

	games = 0;
	moves = 0;
	Arrays.fill( myDistribution, 0 );
	Arrays.fill( playDistribution, 0 );
	myDistSize = 0;
	myMove = 0;
	score = 0;
	who = 0;
}

/**
 * @see com.anji.tournament.Player#reset()
 */
public void reset() {
	reset( numberOfGames );
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
public void storeMove( int opMove, int result ) {

	results[ 0 ][ games ] = myMove;
	results[ 1 ][ games ] = (byte) opMove;
	results[ 2 ][ games ] = (byte) result;
	results[ 3 ][ games ] = (byte) who;

	games++;

	score += result;
	if ( result >= 0 )
		wins++;

	// Playing strategy selection
	if ( games < randomMoves )
		moveType = 0;
	else if ( games > ( numberOfGames - randomMoves ) )
		moveType = 2;
	else
		moveType = 1;

	if ( moveType >= 1 ) {
		myDistSize++;
		myDistribution[ myMove ]++;
	}

}

/**
 * @see com.anji.roshambo.RoshamboPlayer#nextMove()
 */
public int nextMove() {

	double pRock, pPaper;

	pRock = pPaper = 1.0e0 / 3.0e0;

	switch ( moveType ) {

		case 0: // Random
			myMove = (byte) Coin.flip( 1.0e0 / 3, 1.0e0 / 3 );
			break;

		case 1: // Most probable opponent/our move
			lookBack++;
			if ( lookBack > lookBackSize )
				lookBack = 1;

			if ( score >= 0 )
				who = Coin.flip( 0.3, 0.7 ); // Monitor us or them
			else
				who = Coin.flip( 0.7, 0.3 );

			for ( int i = 0; i < 3; i++ ) {
				playDistribution[ i ] = 0;
			}

			for ( int g = games - lookBack - 1; g < games - 1; g++ ) {
				playDistribution[ results[ who ][ g ] ]++;
			}

			pRock = ( lookBack - playDistribution[ PAPER ] ) / ( 2.0 * lookBack );
			pPaper = ( lookBack - playDistribution[ SCISSORS ] ) / ( 2.0 * lookBack );

			myMove = (byte) Coin.flip( pRock, pPaper );
			break;

		case 2: // Balance out my moves.
			if ( myDistSize > 0 ) {
				pRock = ( myDistSize - myDistribution[ ROCK ] ) / ( 2.0 * myDistSize );
				pPaper = ( myDistSize - myDistribution[ PAPER ] ) / ( 2.0 * myDistSize );
				myMove = (byte) Coin.flip( pRock, pPaper );
			}
	}

	return myMove;
}

/**
 * @see com.anji.tournament.Player#getPlayerId()
 */
public String getPlayerId() {
	return "Enigma 0.1";
}

/**
 * @see com.anji.roshambo.RoshamboPlayer#getAuthor()
 */
public String getAuthor() {
	return "Michael, Matt & Jim - South Pasadena, CA";
}

/**
 * @see java.lang.Object#hashCode()
 */
public int hashCode() {
	return getPlayerId().hashCode();
}

}