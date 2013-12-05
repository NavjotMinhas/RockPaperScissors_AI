package rockpaperscissors;

/*
 * -------------------------------------------------------------------------
 * 
 * GnoBot: =======
 * 
 * Toby Hudson (Australia, studying in the UK), 2003
 * 
 * This is an overhaul of Iocaine Powder by Dan Egnor (detail below) which was translated to
 * Java by Jeroen Donkers.
 * 
 * Apologies for the tersity of this introduction, I don't have long to write it. :-)
 * 
 * I have added many predictors, including better statistical history matchers. There are also
 * specific predictors for some of the dummy bots for the 3rd international RoShamBo Programming
 * Competition (Nov 2003), including those with bugs in them (commented below in code). Some of
 * the predictors are unlikely to be used in this tournament (eg the square roots), but I've put
 * them in for added range.
 * 
 * I have also got a periodic analysis for programs which employ one strategy every F turns. (or
 * some kind of cycling causes them to do this)
 * 
 * All these extra predictors mean slower identification of very simple strategies but they do
 * extend the predictive power into even complicated patterns, so I think it's worth it.
 * 
 * I took the De Bruijn sequence from Urza, although note that the dummy bot plays this sequence
 * displaced by 1 because the counter starts at 1 not 0. Otherwise however I have not done
 * specific predictors for either Urza or Iocaine Powder.
 * 
 * Some strategies say "if R <P <S" or some such thing, but don't deal explicitly with the cases
 * of equality (eg R=P <S). The best thing to do here would be to make one predictor with either
 * outcome. However because there are so many of these singular cases, it rapidly multiplies the
 * number of predictors required. I have commented out this kind of splitting which I originally
 * put in, but it was taking up too much memory and CPU.
 * 
 * Anyone who has the common reaction that this kind of tournament is pointless, please see the
 * documents on the web by Darse Billings.
 * 
 * Best wishes to my competitors.
 * 
 * Toby Hudson 29/10/2003
 * 
 * My personal website: (no RoShamBo on it at the time of writing) www.toby.hudsonclan.net
 * 
 * 
 * -------------------------------------------------------------------------
 */

/*
 * Iocaine Powder (1) written by Dan Egnor (USA)
 * 
 * Winner of the First International RoShamBo Programming Competition
 * 
 * Iocaine Powder (from: http://ofb.net/~egnor/iocaine.html)
 */

/*
 * -------------------------------------------------------------------------
 * 
 * Iocaine Powder translated to Java by Jeroen Donkers, 2002
 * 
 * -------------------------------------------------------------------------
 */

/**
 * http://www.cs.unimaas.nl/~donkers/games/roshambo03/
 * TODO - reset all fields
 */
public class GnobotPlayer {

private static final short AGES[] = { 1000, 50, 5, 1 };

/**
 * must put greatest first (and don't make too big)
 */
private static final short STATAGES[] = { 3, 2, 1, 0 };

/**
 * must leave 1 first
 */
static final short FREQ_PERIODS[] = { 1, 6 /* ,2,3,5,12 */};

static final short NUM_FREQ_PERIODS = (short) FREQ_PERIODS.length;

private static final short NUM_STATAGES = (short) STATAGES.length;

private static final short NUM_AGES = (short) AGES.length;

private static final short MY_HIST = 0, OPP_HIST = 1, BOTH_HIST = 2;

private static final short DEBUG = 0;

short my_history[], opp_history[];

short lastmove, trials, pi_index, e_index;

/**
 * sqrt_3_index, sqrt_5_index, sqrt_6_index, sqrt_7_index, sqrt_8_index, sqrt_10_index
 */
short sqrt_2_index;

short bestmove[], bestscorer[][][], shortbestplay[][][], shortflatplay[][][],
		shortflatbugplay[][][], best_length[];

//short bestplay[][][][][][][];

/* tables for square roots and e are from http://antwrp.gsfc.nasa.gov/htmltest/rjn_dig.html */
short sqrt_2_table[] = { 1, 4, 1, 4, 2, 1, 3, 5, 6, 2, 3, 7, 3, 0, 9, 5, 0, 4, 8, 8, 0, 1, 6,
		8, 8, 7, 2, 4, 2, 0, 9, 6, 9, 8, 0, 7, 8, 5, 6, 9, 6, 7, 1, 8, 7, 5, 3, 7, 6, 9, 4, 8, 0,
		7, 3, 1, 7, 6, 6, 7, 9, 7, 3, 7, 9, 9, 0, 7, 3, 2, 4, 7, 8, 4, 6, 2, 1, 0, 7, 0, 3, 8, 8,
		5, 0, 3, 8, 7, 5, 3, 4, 3, 2, 7, 6, 4, 1, 5, 7, 2, 7, 3, 5, 0, 1, 3, 8, 4, 6, 2, 3, 0, 9,
		1, 2, 2, 9, 7, 0, 2, 4, 9, 2, 4, 8, 3, 6, 0, 5, 5, 8, 5, 0, 7, 3, 7, 2, 1, 2, 6, 4, 4, 1,
		2, 1, 4, 9, 7, 0, 9, 9, 9, 3, 5, 8, 3, 1, 4, 1, 3, 2, 2, 2, 6, 6, 5, 9, 2, 7, 5, 0, 5, 5,
		9, 2, 7, 5, 5, 7, 9, 9, 9, 5, 0, 5, 0, 1, 1, 5, 2, 7, 8, 2, 0, 6, 0, 5, 7, 1, 4, 7, 0, 1,
		0, 9, 5, 5, 9, 9, 7, 1, 6, 0, 5, 9, 7, 0, 2, 7, 4, 5, 3, 4, 5, 9, 6, 8, 6, 2, 0, 1, 4, 7,
		2, 8, 5, 1, 7, 4, 1, 8, 6, 4, 0, 8, 8, 9, 1, 9, 8, 6, 0, 9, 5, 5, 2, 3, 2, 9, 2, 3, 0, 4,
		8, 4, 3, 0, 8, 7, 1, 4, 3, 2, 1, 4, 5, 0, 8, 3, 9, 7, 6, 2, 6, 0, 3, 6, 2, 7, 9, 9, 5, 2,
		5, 1, 4, 0, 7, 9, 8, 9, 6, 8, 7, 2, 5, 3, 3, 9, 6, 5, 4, 6, 3, 3, 1, 8, 0, 8, 8, 2, 9, 6,
		4, 0, 6, 2, 0, 6, 1, 5, 2, 5, 8, 3, 5, 2, 3, 9, 5, 0, 5, 4, 7, 4, 5, 7, 5, 0, 2, 8, 7, 7,
		5, 9, 9, 6, 1, 7, 2, 9, 8, 3, 5, 5, 7, 5, 2, 2, 0, 3, 3, 7, 5, 3, 1, 8, 5, 7, 0, 1, 1, 3,
		5, 4, 3, 7, 4, 6, 0, 3, 4, 0, 8, 4, 9, 8, 8, 4, 7, 1, 6, 0, 3, 8, 6, 8, 9, 9, 9, 7, 0, 6,
		9, 9, 0, 0, 4, 8, 1, 5, 0, 3, 0, 5, 4, 4, 0, 2, 7, 7, 9, 0, 3, 1, 6, 4, 5, 4, 2, 4, 7, 8,
		2, 3, 0, 6, 8, 4, 9, 2, 9, 3, 6, 9, 1, 8, 6, 2, 1, 5, 8, 0, 5, 7, 8, 4, 6, 3, 1, 1, 1, 5,
		9, 6, 6, 6, 8, 7, 1, 3, 0, 1, 3, 0, 1, 5, 6, 1, 8, 5, 6, 8, 9, 8, 7, 2, 3, 7, 2, 3, 5, 2,
		8, 8, 5, 0, 9, 2, 6, 4, 8, 6, 1, 2, 4, 9, 4, 9, 7, 7, 1, 5, 4, 2, 1, 8, 3, 3, 4, 2, 0, 4,
		2, 8, 5, 6, 8, 6, 0, 6, 0, 1, 4, 6, 8, 2, 4, 7, 2, 0, 7, 7, 1, 4, 3, 5, 8, 5, 4, 8, 7, 4,
		1, 5, 5, 6, 5, 7, 0, 6, 9, 6, 7, 7, 6, 5, 3, 7, 2, 0, 2, 2, 6, 4, 8, 5, 4, 4, 7, 0, 1, 5,
		8, 5, 8, 8, 0, 1, 6, 2, 0, 7, 5, 8, 4, 7, 4, 9, 2, 2, 6, 5, 7, 2, 2, 6, 0, 0, 2, 0, 8, 5,
		5, 8, 4, 4, 6, 6, 5, 2, 1, 4, 5, 8, 3, 9, 8, 8, 9, 3, 9, 4, 4, 3, 7, 0, 9, 2, 6, 5, 9, 1,
		8, 0, 0, 3, 1, 1, 3, 8, 8, 2, 4, 6, 4, 6, 8, 1, 5, 7, 0, 8, 2, 6, 3, 0, 1, 0, 0, 5, 9, 4,
		8, 5, 8, 7, 0, 4, 0, 0, 3, 1, 8, 6, 4, 8, 0, 3, 4, 2, 1, 9, 4, 8, 9, 7, 2, 7, 8, 2, 9, 0,
		6, 4, 1, 0, 4, 5, 0, 7, 2, 6, 3, 6, 8, 8, 1, 3, 1, 3, 7, 3, 9, 8, 5, 5, 2, 5, 6, 1, 1, 7,
		3, 2, 2, 0, 4, 0, 2, 4, 5, 0, 9, 1, 2, 2, 7, 7, 0, 0, 2, 2, 6, 9, 4, 1, 1, 2, 7, 5, 7, 3,
		6, 2, 7, 2, 8, 0, 4, 9, 5, 7, 3, 8, 1, 0, 8, 9, 6, 7, 5, 0, 4, 0, 1, 8, 3, 6, 9, 8, 6, 8,
		3, 6, 8, 4, 5, 0, 7, 2, 5, 7, 9, 9, 3, 6, 4, 7, 2, 9, 0, 6, 0, 7, 6, 2, 9, 9, 6, 9, 4, 1,
		3, 8, 0, 4, 7, 5, 6, 5, 4, 8, 2, 3, 7, 2, 8, 9, 9, 7, 1, 8, 0, 3, 2, 6, 8, 0, 2, 4, 7, 4,
		4, 2, 0, 6, 2, 9, 2, 6, 9, 1, 2, 4, 8, 5, 9, 0, 5, 2, 1, 8, 1, 0, 0, 4, 4, 5, 9, 8, 4, 2,
		1, 5, 0, 5, 9, 1, 1, 2, 0, 2, 4, 9, 4, 4, 1, 3, 4, 1, 7, 2, 8, 5, 3, 1, 4, 7, 8, 1, 0, 5,
		8, 0, 3, 6, 0, 3, 3, 7, 1, 0, 7, 7, 3, 0, 9, 1, 8, 2, 8, 6, 9, 3, 1, 4, 7, 1, 0, 1, 7, 1,
		1, 1, 1, 6, 8, 3, 9, 1, 6, 5, 8, 1, 7, 2, 6, 8, 8, 9, 4, 1, 9, 7, 5, 8, 7, 1, 6, 5, 8, 2,
		1, 5, 2, 1, 2, 8, 2, 2, 9, 5, 1, 8, 4, 8, 8, 4, 7, 2, 0, 8, 9, 6, 9, 4, 6, 3, 3, 8, 6, 2,
		8, 9, 1, 5, 6, 2, 8, 8, 2, 7, 6, 5, 9, 5, 2, 6, 3, 5, 1, 4, 0, 5, 4, 2, 2, 6, 7, 6, 5, 3,
		2, 3, 9, 6, 9, 4, 6, 1, 7, 5, 1, 1, 2, 9, 1, 6, 0, 2, 4, 0, 8, 7, 1, 5, 5, 1, 0, 1, 3, 5,
		1, 5, 0, 4, 5, 5, 3, 8, 1, 2, 8, 7, 5, 6, 0, 0, 5, 2, 6, 3, 1, 4, 6, 8, 0, 1, 7, 1, 2, 7,
		4, 0, 2, 6, 5, 3, 9, 6, 9, 4, 7, 0, 2, 4, 0, 3, 0, 0, 5, 1, 7, 4, 9, 5, 3, 1, 8, 8, 6, 2,
		9, 2, 5, 6, 3, 1, 3, 8, 5, 1, 8, 8, 1, 6, 3, 4, 7, 8, 0, 0, 1, 5, 6, 9, 3, 6, 9, 1, 7, 6,
		8, 8, 1, 8, 5, 2, 3, 7, 8, 6, 8, 4, 0, 5, 2, 2, 8, 7, 8, 3, 7, 6, 2, 9, 3, 8, 9, 2, 1, 4,
		3, 0, 0, 6, 5, 5, 8 };

short e_table[] = { 2, 7, 1, 8, 2, 8, 1, 8, 2, 8, 4, 5, 9, 0, 4, 5, 2, 3, 5, 3, 6, 0, 2, 8, 7,
		4, 7, 1, 3, 5, 2, 6, 6, 2, 4, 9, 7, 7, 5, 7, 2, 4, 7, 0, 9, 3, 6, 9, 9, 9, 5, 9, 5, 7, 4,
		9, 6, 6, 9, 6, 7, 6, 2, 7, 7, 2, 4, 0, 7, 6, 6, 3, 0, 3, 5, 3, 5, 4, 7, 5, 9, 4, 5, 7, 1,
		3, 8, 2, 1, 7, 8, 5, 2, 5, 1, 6, 6, 4, 2, 7, 4, 2, 7, 4, 6, 6, 3, 9, 1, 9, 3, 2, 0, 0, 3,
		0, 5, 9, 9, 2, 1, 8, 1, 7, 4, 1, 3, 5, 9, 6, 6, 2, 9, 0, 4, 3, 5, 7, 2, 9, 0, 0, 3, 3, 4,
		2, 9, 5, 2, 6, 0, 5, 9, 5, 6, 3, 0, 7, 3, 8, 1, 3, 2, 3, 2, 8, 6, 2, 7, 9, 4, 3, 4, 9, 0,
		7, 6, 3, 2, 3, 3, 8, 2, 9, 8, 8, 0, 7, 5, 3, 1, 9, 5, 2, 5, 1, 0, 1, 9, 0, 1, 1, 5, 7, 3,
		8, 3, 4, 1, 8, 7, 9, 3, 0, 7, 0, 2, 1, 5, 4, 0, 8, 9, 1, 4, 9, 9, 3, 4, 8, 8, 4, 1, 6, 7,
		5, 0, 9, 2, 4, 4, 7, 6, 1, 4, 6, 0, 6, 6, 8, 0, 8, 2, 2, 6, 4, 8, 0, 0, 1, 6, 8, 4, 7, 7,
		4, 1, 1, 8, 5, 3, 7, 4, 2, 3, 4, 5, 4, 4, 2, 4, 3, 7, 1, 0, 7, 5, 3, 9, 0, 7, 7, 7, 4, 4,
		9, 9, 2, 0, 6, 9, 5, 5, 1, 7, 0, 2, 7, 6, 1, 8, 3, 8, 6, 0, 6, 2, 6, 1, 3, 3, 1, 3, 8, 4,
		5, 8, 3, 0, 0, 0, 7, 5, 2, 0, 4, 4, 9, 3, 3, 8, 2, 6, 5, 6, 0, 2, 9, 7, 6, 0, 6, 7, 3, 7,
		1, 1, 3, 2, 0, 0, 7, 0, 9, 3, 2, 8, 7, 0, 9, 1, 2, 7, 4, 4, 3, 7, 4, 7, 0, 4, 7, 2, 3, 0,
		6, 9, 6, 9, 7, 7, 2, 0, 9, 3, 1, 0, 1, 4, 1, 6, 9, 2, 8, 3, 6, 8, 1, 9, 0, 2, 5, 5, 1, 5,
		1, 0, 8, 6, 5, 7, 4, 6, 3, 7, 7, 2, 1, 1, 1, 2, 5, 2, 3, 8, 9, 7, 8, 4, 4, 2, 5, 0, 5, 6,
		9, 5, 3, 6, 9, 6, 7, 7, 0, 7, 8, 5, 4, 4, 9, 9, 6, 9, 9, 6, 7, 9, 4, 6, 8, 6, 4, 4, 5, 4,
		9, 0, 5, 9, 8, 7, 9, 3, 1, 6, 3, 6, 8, 8, 9, 2, 3, 0, 0, 9, 8, 7, 9, 3, 1, 2, 7, 7, 3, 6,
		1, 7, 8, 2, 1, 5, 4, 2, 4, 9, 9, 9, 2, 2, 9, 5, 7, 6, 3, 5, 1, 4, 8, 2, 2, 0, 8, 2, 6, 9,
		8, 9, 5, 1, 9, 3, 6, 6, 8, 0, 3, 3, 1, 8, 2, 5, 2, 8, 8, 6, 9, 3, 9, 8, 4, 9, 6, 4, 6, 5,
		1, 0, 5, 8, 2, 0, 9, 3, 9, 2, 3, 9, 8, 2, 9, 4, 8, 8, 7, 9, 3, 3, 2, 0, 3, 6, 2, 5, 0, 9,
		4, 4, 3, 1, 1, 7, 3, 0, 1, 2, 3, 8, 1, 9, 7, 0, 6, 8, 4, 1, 6, 1, 4, 0, 3, 9, 7, 0, 1, 9,
		8, 3, 7, 6, 7, 9, 3, 2, 0, 6, 8, 3, 2, 8, 2, 3, 7, 6, 4, 6, 4, 8, 0, 4, 2, 9, 5, 3, 1, 1,
		8, 0, 2, 3, 2, 8, 7, 8, 2, 5, 0, 9, 8, 1, 9, 4, 5, 5, 8, 1, 5, 3, 0, 1, 7, 5, 6, 7, 1, 7,
		3, 6, 1, 3, 3, 2, 0, 6, 9, 8, 1, 1, 2, 5, 0, 9, 9, 6, 1, 8, 1, 8, 8, 1, 5, 9, 3, 0, 4, 1,
		6, 9, 0, 3, 5, 1, 5, 9, 8, 8, 8, 8, 5, 1, 9, 3, 4, 5, 8, 0, 7, 2, 7, 3, 8, 6, 6, 7, 3, 8,
		5, 8, 9, 4, 2, 2, 8, 7, 9, 2, 2, 8, 4, 9, 9, 8, 9, 2, 0, 8, 6, 8, 0, 5, 8, 2, 5, 7, 4, 9,
		2, 7, 9, 6, 1, 0, 4, 8, 4, 1, 9, 8, 4, 4, 4, 3, 6, 3, 4, 6, 3, 2, 4, 4, 9, 6, 8, 4, 8, 7,
		5, 6, 0, 2, 3, 3, 6, 2, 4, 8, 2, 7, 0, 4, 1, 9, 7, 8, 6, 2, 3, 2, 0, 9, 0, 0, 2, 1, 6, 0,
		9, 9, 0, 2, 3, 5, 3, 0, 4, 3, 6, 9, 9, 4, 1, 8, 4, 9, 1, 4, 6, 3, 1, 4, 0, 9, 3, 4, 3, 1,
		7, 3, 8, 1, 4, 3, 6, 4, 0, 5, 4, 6, 2, 5, 3, 1, 5, 2, 0, 9, 6, 1, 8, 3, 6, 9, 0, 8, 8, 8,
		7, 0, 7, 0, 1, 6, 7, 6, 8, 3, 9, 6, 4, 2, 4, 3, 7, 8, 1, 4, 0, 5, 9, 2, 7, 1, 4, 5, 6, 3,
		5, 4, 9, 0, 6, 1, 3, 0, 3, 1, 0, 7, 2, 0, 8, 5, 1, 0, 3, 8, 3, 7, 5, 0, 5, 1, 0, 1, 1, 5,
		7, 4, 7, 7, 0, 4, 1, 7, 1, 8, 9, 8, 6, 1, 0, 6, 8, 7, 3, 9, 6, 9, 6, 5, 5, 2, 1, 2, 6, 7,
		1, 5, 4, 6, 8, 8, 9, 5, 7, 0, 3, 5, 0, 3, 5, 4, 0, 2, 1, 2, 3, 4, 0, 7, 8, 4, 9, 8, 1, 9,
		3, 3, 4, 3, 2, 1, 0, 6, 8, 1, 7, 0, 1, 2, 1, 0, 0, 5, 6, 2, 7, 8, 8, 0, 2, 3, 5, 1, 9, 3,
		0, 3, 3, 2, 2, 4, 7, 4, 5, 0, 1, 5, 8, 5, 3, 9, 0, 4, 7, 3, 0, 4, 1, 9, 9, 5, 7, 7, 7, 7,
		0, 9, 3, 5, 0, 3, 6, 6, 0, 4, 1, 6, 9, 9, 7, 3, 2, 9, 7, 2, 5, 0, 8, 8, 6, 8, 7, 6, 9, 6,
		6, 4, 0, 3, 5, 5, 5, 7, 0, 7, 1, 6, 2, 2, 6, 8, 4, 4, 7, 1, 6, 2, 5, 6, 0, 7, 9, 8, 8, 2,
		6, 5, 1, 7, 8, 7, 1, 3, 4, 1, 9, 5, 1, 2, 4, 6, 6, 5, 2, 0, 1, 0, 3, 0, 5, 9, 2, 1, 2, 3,
		6, 6, 7, 7, 1, 9, 4, 3, 2, 5, 2, 7, 8, 6, 7, 5, 3, 9, 8, 5, 5, 8, 9, 4, 4, 8, 9, 6, 9, 7,
		0, 9, 6, 4, 0 };

short pi_table[] = /* (from Urza) */
{ 3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5, 8, 9, 7, 9, 3, 2, 3, 8, 4, 6, 2, 6, 4, 3, 3, 8, 3, 2, 7, 9,
		5, 0, 2, 8, 8, 4, 1, 9, 7, 1, 6, 9, 3, 9, 9, 3, 7, 5, 1, 0, 5, 8, 2, 0, 9, 7, 4, 9, 4, 4,
		5, 9, 2, 3, 0, 7, 8, 1, 6, 4, 0, 6, 2, 8, 6, 2, 0, 8, 9, 9, 8, 6, 2, 8, 0, 3, 4, 8, 2, 5,
		3, 4, 2, 1, 1, 7, 0, 6, 7, 9, 8, 2, 1, 4, 8, 0, 8, 6, 5, 1, 3, 2, 8, 2, 3, 0, 6, 6, 4, 7,
		0, 9, 3, 8, 4, 4, 6, 0, 9, 5, 5, 0, 5, 8, 2, 2, 3, 1, 7, 2, 5, 3, 5, 9, 4, 0, 8, 1, 2, 8,
		4, 8, 1, 1, 1, 7, 4, 5, 0, 2, 8, 4, 1, 0, 2, 7, 0, 1, 9, 3, 8, 5, 2, 1, 1, 0, 5, 5, 5, 9,
		6, 4, 4, 6, 2, 2, 9, 4, 8, 9, 5, 4, 9, 3, 0, 3, 8, 1, 9, 6, 4, 4, 2, 8, 8, 1, 0, 9, 7, 5,
		6, 6, 5, 9, 3, 3, 4, 4, 6, 1, 2, 8, 4, 7, 5, 6, 4, 8, 2, 3, 3, 7, 8, 6, 7, 8, 3, 1, 6, 5,
		2, 7, 1, 2, 0, 1, 9, 0, 9, 1, 4, 5, 6, 4, 8, 5, 6, 6, 9, 2, 3, 4, 6, 0, 3, 4, 8, 6, 1, 0,
		4, 5, 4, 3, 2, 6, 6, 4, 8, 2, 1, 3, 3, 9, 3, 6, 0, 7, 2, 6, 0, 2, 4, 9, 1, 4, 1, 2, 7, 3,
		7, 2, 4, 5, 8, 7, 0, 0, 6, 6, 0, 6, 3, 1, 5, 5, 8, 8, 1, 7, 4, 8, 8, 1, 5, 2, 0, 9, 2, 0,
		9, 6, 2, 8, 2, 9, 2, 5, 4, 0, 9, 1, 7, 1, 5, 3, 6, 4, 3, 6, 7, 8, 9, 2, 5, 9, 0, 3, 6, 0,
		0, 1, 1, 3, 3, 0, 5, 3, 0, 5, 4, 8, 8, 2, 0, 4, 6, 6, 5, 2, 1, 3, 8, 4, 1, 4, 6, 9, 5, 1,
		9, 4, 1, 5, 1, 1, 6, 0, 9, 4, 3, 3, 0, 5, 7, 2, 7, 0, 3, 6, 5, 7, 5, 9, 5, 9, 1, 9, 5, 3,
		0, 9, 2, 1, 8, 6, 1, 1, 7, 3, 8, 1, 9, 3, 2, 6, 1, 1, 7, 9, 3, 1, 0, 5, 1, 1, 8, 5, 4, 8,
		0, 7, 4, 4, 6, 2, 3, 7, 9, 9, 6, 2, 7, 4, 9, 5, 6, 7, 3, 5, 1, 8, 8, 5, 7, 5, 2, 7, 2, 4,
		8, 9, 1, 2, 2, 7, 9, 3, 8, 1, 8, 3, 0, 1, 1, 9, 4, 9, 1, 2, 9, 8, 3, 3, 6, 7, 3, 3, 6, 2,
		4, 4, 0, 6, 5, 6, 6, 4, 3, 0, 8, 6, 0, 2, 1, 3, 9, 4, 9, 4, 6, 3, 9, 5, 2, 2, 4, 7, 3, 7,
		1, 9, 0, 7, 0, 2, 1, 7, 9, 8, 6, 0, 9, 4, 3, 7, 0, 2, 7, 7, 0, 5, 3, 9, 2, 1, 7, 1, 7, 6,
		2, 9, 3, 1, 7, 6, 7, 5, 2, 3, 8, 4, 6, 7, 4, 8, 1, 8, 4, 6, 7, 6, 6, 9, 4, 0, 5, 1, 3, 2,
		0, 0, 0, 5, 6, 8, 1, 2, 7, 1, 4, 5, 2, 6, 3, 5, 6, 0, 8, 2, 7, 7, 8, 5, 7, 7, 1, 3, 4, 2,
		7, 5, 7, 7, 8, 9, 6, 0, 9, 1, 7, 3, 6, 3, 7, 1, 7, 8, 7, 2, 1, 4, 6, 8, 4, 4, 0, 9, 0, 1,
		2, 2, 4, 9, 5, 3, 4, 3, 0, 1, 4, 6, 5, 4, 9, 5, 8, 5, 3, 7, 1, 0, 5, 0, 7, 9, 2, 2, 7, 9,
		6, 8, 9, 2, 5, 8, 9, 2, 3, 5, 4, 2, 0, 1, 9, 9, 5, 6, 1, 1, 2, 1, 2, 9, 0, 2, 1, 9, 6, 0,
		8, 6, 4, 0, 3, 4, 4, 1, 8, 1, 5, 9, 8, 1, 3, 6, 2, 9, 7, 7, 4, 7, 7, 1, 3, 0, 9, 9, 6, 0,
		5, 1, 8, 7, 0, 7, 2, 1, 1, 3, 4, 9, 9, 9, 9, 9, 9, 8, 3, 7, 2, 9, 7, 8, 0, 4, 9, 9, 5, 1,
		0, 5, 9, 7, 3, 1, 7, 3, 2, 8, 1, 6, 0, 9, 6, 3, 1, 8, 5, 9, 5, 0, 2, 4, 4, 5, 9, 4, 5, 5,
		3, 4, 6, 9, 0, 8, 3, 0, 2, 6, 4, 2, 5, 2, 2, 3, 0, 8, 2, 5, 3, 3, 4, 4, 6, 8, 5, 0, 3, 5,
		2, 6, 1, 9, 3, 1, 1, 8, 8, 1, 7, 1, 0, 1, 0, 0, 0, 3, 1, 3, 7, 8, 3, 8, 7, 5, 2, 8, 8, 6,
		5, 8, 7, 5, 3, 3, 2, 0, 8, 3, 8, 1, 4, 2, 0, 6, 1, 7, 1, 7, 7, 6, 6, 9, 1, 4, 7, 3, 0, 3,
		5, 9, 8, 2, 5, 3, 4, 9, 0, 4, 2, 8, 7, 5, 5, 4, 6, 8, 7, 3, 1, 1, 5, 9, 5, 6, 2, 8, 6, 3,
		8, 8, 2, 3, 5, 3, 7, 8, 7, 5, 9, 3, 7, 5, 1, 9, 5, 7, 7, 8, 1, 8, 5, 7, 7, 8, 0, 5, 3, 2,
		1, 7, 1, 2, 2, 6, 8, 0, 6, 6, 1, 3, 0, 0, 1, 9, 2, 7, 8, 7, 6, 6, 1, 1, 1, 9, 5, 9, 0, 9,
		2, 1, 6, 4, 2, 0, 1, 9, 8, 9, 3, 8, 0, 9, 5, 2, 5, 7, 2, 0, 1, 0, 6, 5, 4, 8, 5, 8, 6, 3,
		2, 7, 8, 8, 6, 5, 9, 3, 6, 1, 5, 3, 3, 8, 1, 8, 2, 7, 9, 6, 8, 2, 3, 0, 3, 0, 1, 9, 5, 2,
		0, 3, 5, 3, 0, 1, 8, 5, 2, 9, 6, 8, 9, 9, 5, 7, 7, 3, 6, 2, 2, 5, 9, 9, 4, 1, 3, 8, 9, 1,
		2, 4, 9, 7, 2, 1, 7, 7, 5, 2, 8, 3, 4, 7, 9, 1, 3, 1, 5, 1, 5, 5, 7, 4, 8, 5, 7, 2, 4, 2,
		4, 5, 4, 1, 5, 0, 6, 9, 5, 9, 5, 0, 8, 2, 9, 5, 3, 3, 1, 1, 6, 8, 6, 1, 7, 2, 7, 8, 5, 5,
		8, 8, 9, 0, 7, 5, 0, 9, 8, 3, 8, 1, 7, 5, 4, 6, 3, 7, 4, 6, 4, 9, 3, 9, 3, 1, 9, 2, 5, 5,
		0, 6, 0, 4, 0, 0, 9, 2, 7, 7, 0, 1, 6, 7, 1, 1, 3, 9, 0, 0, 9, 8, 4, 8, 8, 2, 4, 0, 1 };

short db_table[] = /* De Bruijn sequence: (from Urza) */
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

result mres;

predict[][][] pr_history;

predict[][][] pr_stathistory;

/* predict[][][][][][][] pr_bstathistory; */
predict[][][] pr_shortbstathistory;

predict[][][] pr_shortbflathistory;

predict[][][] pr_shortbflatbughistory;

predict[][] pr_freq;

predict[][] pr_meta;

predict pr_fixed, pr_foxtrot_my, pr_foxtrot_opp, pr_random, pr_pi, pr_e, pr_db, pr_db_2;

predict pr_sqrt_2/* , pr_sqrt_3, pr_sqrt_5, pr_sqrt_6, pr_sqrt_7, pr_sqrt_8, pr_sqrt_10 */;

stat stats[];

// inner class result for stat.max and predict.do_predict

class result {

short move, score;
}

// Inner clas Stat

class stat {

short[][][] sum;

short age;

short freq;

stat() {
	/* System.out.println("Full array"); */
	sum = new short[ 1 + trials ][ NUM_FREQ_PERIODS ][ 3 ];
	freq = NUM_FREQ_PERIODS;

	age = 0;
	for ( short f = 0; f < freq; f++ )
		for ( short i = 0; i < 3; ++i )
			sum[ age ][ f ][ i ] = (short) ( 1 - FREQ_PERIODS[ f ] );
	/*
	 * start larger periods at a disadvantage since they'll get a multiplier later
	 */
}

void add( short i, short delta ) {
	for ( short f = 0; f < freq; f++ )
		sum[ age ][ f ][ i ] += delta * FREQ_PERIODS[ f ]; /*
																																	  * longer periods get
																																	  * multiple of period since
																																	  * they only get a chance to
																																	  * score every now and then
																																	  */
}

void next() {
	if ( age < trials ) {
		++( age );
		for ( short f = 0; f < freq; f++ )
			for ( short i = 0; i < 3; ++i ) {
				if ( FREQ_PERIODS[ f ] <= age )
					sum[ age ][ f ][ i ] = sum[ age - FREQ_PERIODS[ f ] ][ f ][ i ];
				/* else sum remains 0 */
			}
	}
}

boolean max( short dage, short f, result res ) {

	res.move = -1;
	for ( short i = 2; i > -1; i-- ) { /*
																					 * just reversed the order to beat "freq" bot should
																					 * really deal with equal frequencies explicitly. No
																					 * time I'm afraid.
																					 */
		short diff;
		if ( dage > age )
			diff = sum[ age ][ f ][ i ];
		else
			diff = (short) ( sum[ age ][ f ][ i ] - sum[ age - ( dage / FREQ_PERIODS[ f ] )
					* FREQ_PERIODS[ f ] ][ f ][ i ] );
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

short last;

short freq;

predict() {
	st = new stat();
	last = -1;
	freq = NUM_FREQ_PERIODS;
}

void do_predict( short olast, short guess ) {
	// olast: opponent's last move (-1 if none)
	// guess: algorithm's prediction of opponent's next move

	if ( -1 != olast ) {
		short diff = (short) ( ( 3 + ( olast % 3 ) - ( last % 3 ) ) % 3 );
		st.add( (short) ( ( diff + 1 ) % 3 ), (short) 1 ); // will_beat...
		st.add( (short) ( ( diff + 2 ) % 3 ), (short) -1 ); // will loose
		st.next();
	}
	last = guess;
}

void scan( short age, result res ) {
	short f;
	short storemove;
	for ( f = 0; f < freq; f++ ) {
		storemove = res.move;
		if ( st.max( age, f, res ) ) /* if this stat is better over this age than res(.score) */{
			/* if (pi_index%300==0) System.out.println(pi_index+" new winner, score: "+res.score); */
			res.move = (short) ( ( last + res.move ) % 3 );
		}
		else
			res.move = storemove;
	}
}

} // end inner class predict

// History algorithm

short match_single( short i, short num, short[] history ) {
	short high = num;
	short low = i;
	while ( low > 0 && history[ low ] == history[ high ] ) {
		low--;
		high--;
	}
	return (short) ( num - high );
}

short match_both( short i, short num ) {
	short j;
	for ( j = 0; ( j < i ) && ( opp_history[ num - j ] == opp_history[ i - j ] )
			&& ( my_history[ num - j ] == my_history[ i - j ] ); ++j ) {
		// Philip Tucker - all logic is in for loop
	}
	return j;
}

short match_single_cutoff( short i, short num, short cutoff, short[] history ) {
	short high = num;
	short low = i;
	while ( ( low > 0 ) && ( history[ low ] == history[ high ] ) && ( ( num - high ) < cutoff ) ) {
		low--;
		high--;
	}
	return (short) ( num - high );
}

short match_both_cutoff( short i, short num, short cutoff ) {
	short j;
	for ( j = 0; ( j < i ) && ( opp_history[ num - j ] == opp_history[ i - j ] )
			&& ( my_history[ num - j ] == my_history[ i - j ] ) && ( j < cutoff ); ++j ) {
		// Philip Tucker - all logic is in for loop
	}
	return j;
}

/**
 * @see java.lang.Object#toString()
 */
public String toString() {
	return getPlayerId();
}

void do_history( short age, short best[] ) {
	short num = my_history[ 0 ];
	for ( short w = 0; w < 3; ++w )
		best[ w ] = best_length[ w ] = 0;
	for ( short i = (short) ( num - 1 ); i > (short) ( num - age ) && i > best_length[ MY_HIST ]; --i ) {
		short j = match_single( i, num, my_history );
		if ( j > best_length[ MY_HIST ] ) {
			best_length[ MY_HIST ] = j;
			best[ MY_HIST ] = i;
			if ( j > num / 2 )
				break;
		}
	}

	for ( short i = (short) ( num - 1 ); i > (short) ( num - age ) && i > best_length[ OPP_HIST ]; --i ) {
		short j = match_single( i, num, opp_history );
		if ( j > best_length[ OPP_HIST ] ) {
			best_length[ OPP_HIST ] = j;
			best[ OPP_HIST ] = i;
			if ( j > num / 2 )
				break;
		}
	}

	for ( short i = (short) ( num - 1 ); i > (short) ( num - age ) && i > best_length[ BOTH_HIST ]; --i ) {
		short j = match_both( i, num );
		if ( j > best_length[ BOTH_HIST ] ) {
			best_length[ BOTH_HIST ] = j;
			best[ BOTH_HIST ] = i;
			if ( j > num / 2 )
				break;
		}
	}
}

void do_stathistory( short n_sages, short sages[], short aBestscorer[][][],
		short aShortbestplay[][][], short aShortflatplay[][][], short aShortflatbugplay[][][] ) {
	short topscore, topscorer;
	short scores[][][][];
	scores = new short[ n_sages ][ 3 ][ 2 ][ 3 ];
	short pint, qint, rint;
	short sum;

	short num = my_history[ 0 ];

	for ( short a = 0; a < n_sages; a++ )
		for ( short w = 0; w < 3; w++ )
			for ( short v = 0; v < 2; v++ )
				for ( short x = 0; x < 2; x++ )
					scores[ a ][ w ][ v ][ x ] = 0;

	for ( short i = num; i > 0; i-- ) {
		short j = match_single_cutoff( i, num, sages[ 0 ], my_history );
		for ( short a = 0; a < n_sages; a++ ) {
			if ( j >= sages[ a ] ) {
				scores[ a ][ MY_HIST ][ 0 ][ my_history[ i ] ]++;
				scores[ a ][ MY_HIST ][ 1 ][ opp_history[ i ] ]++;
			}
		}
	}

	for ( short i = num; i > 0; i-- ) {
		short j = match_single_cutoff( i, num, sages[ 0 ], opp_history );
		for ( short a = 0; a < n_sages; a++ ) {
			if ( j >= sages[ a ] ) {
				scores[ a ][ OPP_HIST ][ 0 ][ my_history[ i ] ]++;
				scores[ a ][ OPP_HIST ][ 1 ][ opp_history[ i ] ]++;
			}
		}
	}

	for ( short i = num; i > 0; i-- ) {
		short j = match_both_cutoff( i, num, sages[ 0 ] );
		for ( short a = 0; a < n_sages; a++ ) {
			if ( j >= sages[ a ] ) {
				scores[ a ][ BOTH_HIST ][ 0 ][ my_history[ i ] ]++;
				scores[ a ][ BOTH_HIST ][ 1 ][ opp_history[ i ] ]++;
			}
		}
	}

	/* now figure out which of the [x] is highest, and return that as the best (most freq) */

	if ( DEBUG > 2 )
		for ( short x = 0; x < 3; x++ )
			System.out.println( "w:0 v:0 x:" + x + " score:" + scores[ 0 ][ 0 ][ 0 ][ x ] );

	for ( short a = 0; a < n_sages; a++ ) {
		for ( short w = 0; w < 3; w++ ) {
			for ( short v = 0; v < 2; v++ ) {
				topscore = 0;
				topscorer = -1;
				sum = 0;
				for ( short x = 0; x < 3; x++ ) {
					if ( DEBUG > 1 )
						System.out.println( "s(a): " + sages[ a ] + " w:" + w + " v:" + v + " x:" + x
								+ " score:" + scores[ a ][ w ][ v ][ x ] );
					sum += scores[ a ][ w ][ v ][ x ];
					if ( scores[ a ][ w ][ v ][ x ] > topscore ) {
						topscorer = x;
						topscore = scores[ a ][ w ][ v ][ x ];
					}
				}
				/* gives most frequent outcome */
				aBestscorer[ a ][ w ][ v ] = topscorer;

				if ( sum == 0 ) {
					aShortbestplay[ a ][ w ][ v ] = -1;
					aShortflatplay[ a ][ w ][ v ] = -1;
					aShortflatbugplay[ a ][ w ][ v ] = -1;
					/*
					 * for (sing1=0; sing1 <3; sing1++) for (sing2=0; sing2 <3; sing2++) for (sing3=0;
					 * sing3 <3; sing3++) for (sing4=0; sing4 <3; sing4++)
					 * bestplay[a][w][v][sing1][sing2][sing3][sing4] = -1;
					 */
				}
				else {
					/* Gives "best" outcome for triple frequency distribution... see bot called HAVOC */
					/*
					 * However I've modified it so that if all 3 probabilities are 1/3, then there is a
					 * different "predictor" for each outcome. No actually, I've commented this out for
					 * now, it was taking too long, so I've just fixed it to play anti-Havoc when
					 * confronted with a singularity. By the way, this doesn't play optimally vs Havoc
					 * because Havoc has a bug in it meaning that the last play doesn't count in its
					 * stats. I didn't really want to play anti-bug, because Havoc may not even be in
					 * this.
					 * 
					 * Also there are singularities for probabilities like (0.667-2x, 3x, 0.333-x). also
					 * commented out :-)
					 * 
					 * Maybe need a rand one also?
					 */
					pint = scores[ a ][ w ][ v ][ 0 ];
					qint = scores[ a ][ w ][ v ][ 2 ]; /*
																											 * nb out of order to match havoc
																											 * convention
																											 */
					rint = scores[ a ][ w ][ v ][ 1 ];

					if ( ( pint < qint ) && ( pint < rint ) ) {
						aShortflatplay[ a ][ w ][ v ] = 0;
					}
					else if ( ( qint < pint ) && ( qint < rint ) ) {
						aShortflatplay[ a ][ w ][ v ] = 2;
					}
					else if ( ( rint < pint ) && ( rint < qint ) ) {
						aShortflatplay[ a ][ w ][ v ] = 1;
					}
					else if ( ( pint < qint ) && ( pint == rint ) ) {
						aShortflatplay[ a ][ w ][ v ] = 0;
					}
					else if ( ( rint < pint ) && ( rint == qint ) ) {
						aShortflatplay[ a ][ w ][ v ] = 1;
					}
					else if ( ( pint == qint ) && ( pint < rint ) ) {
						aShortflatplay[ a ][ w ][ v ] = 0;
					}
					else if ( ( rint == pint ) && ( rint == qint ) ) {
						//shortflatplay[a][w][v]=1;
					}

					pint++; /* this was the bug in the flat dummy posted */

					if ( ( pint < qint ) && ( pint < rint ) ) {
						aShortflatbugplay[ a ][ w ][ v ] = 0;
					}
					else if ( ( qint < pint ) && ( qint < rint ) ) {
						aShortflatbugplay[ a ][ w ][ v ] = 2;
					}
					else if ( ( rint < pint ) && ( rint < qint ) ) {
						aShortflatbugplay[ a ][ w ][ v ] = 1;
					}
					else if ( ( pint < qint ) && ( pint == rint ) ) {
						aShortflatbugplay[ a ][ w ][ v ] = 0;
					}
					else if ( ( rint < pint ) && ( rint == qint ) ) {
						aShortflatbugplay[ a ][ w ][ v ] = 1;
					}
					else if ( ( pint == qint ) && ( pint < rint ) ) {
						aShortflatbugplay[ a ][ w ][ v ] = 2;
					}
					else if ( ( rint == pint ) && ( rint == qint ) ) {
						aShortflatbugplay[ a ][ w ][ v ] = 0; /* ... not defined ... */
					}

					pint--;

					if ( ( 3 * pint <= sum ) & ( 3 * qint > sum ) ) {
						if ( 3 * ( pint + qint ) > 2 * sum ) {
							aShortbestplay[ a ][ w ][ v ] = 0;
							/*
							 * for (sing1=0; sing1 <3; sing1++) for (sing2=0; sing2 <3; sing2++) for (sing3=0;
							 * sing3 <3; sing3++) for (sing4=0; sing4 <3; sing4++)
							 * bestplay[a][w][v][sing1][sing2][sing3][sing4] = 0;
							 */
						}
						else if ( 3 * ( pint + qint ) < 2 * sum ) {
							aShortbestplay[ a ][ w ][ v ] = 2;
							/*
							 * for (sing1=0; sing1 <3; sing1++) for (sing2=0; sing2 <3; sing2++) for (sing3=0;
							 * sing3 <3; sing3++) for (sing4=0; sing4 <3; sing4++)
							 * bestplay[a][w][v][sing1][sing2][sing3][sing4] = 2;
							 */
						}
						else /* (p + q == 2*m) but each not m Sing1 */{
							aShortbestplay[ a ][ w ][ v ] = 2; //SET TO BEAT HAVOC ... not general
							/*
							 * for (sing1=0; sing1 <3; sing1++) for (sing2=0; sing2 <3; sing2++) for (sing3=0;
							 * sing3 <3; sing3++) for (sing4=0; sing4 <3; sing4++)
							 * bestplay[a][w][v][sing1][sing2][sing3][sing4] = sing1;
							 */
						}

					}
					else if ( ( 3 * pint > sum ) & ( 3 * qint > sum ) ) {
						aShortbestplay[ a ][ w ][ v ] = 0;
						/*
						 * for (sing1=0; sing1 <3; sing1++) for (sing2=0; sing2 <3; sing2++) for (sing3=0;
						 * sing3 <3; sing3++) for (sing4=0; sing4 <3; sing4++)
						 * bestplay[a][w][v][sing1][sing2][sing3][sing4] = 0;
						 */
					}
					else if ( ( 3 * pint > sum ) & ( 3 * qint <= sum ) ) {
						if ( 3 * ( pint + rint ) > 2 * sum ) {
							aShortbestplay[ a ][ w ][ v ] = 1;
							/*
							 * for (sing1=0; sing1 <3; sing1++) for (sing2=0; sing2 <3; sing2++) for (sing3=0;
							 * sing3 <3; sing3++) for (sing4=0; sing4 <3; sing4++)
							 * bestplay[a][w][v][sing1][sing2][sing3][sing4] = 1;
							 */
						}
						else /* (p + r == 2*m) but each not m Sing2 */{
							aShortbestplay[ a ][ w ][ v ] = 2; //SET TO BEAT HAVOC ... not general
							/*
							 * for (sing1=0; sing1 <3; sing1++) for (sing2=0; sing2 <3; sing2++) for (sing3=0;
							 * sing3 <3; sing3++) for (sing4=0; sing4 <3; sing4++)
							 * bestplay[a][w][v][sing1][sing2][sing3][sing4] = sing2;
							 */
						}
					}
					else if ( ( 3 * pint < sum ) & ( 3 * qint <= sum ) ) {
						aShortbestplay[ a ][ w ][ v ] = 2;
						/*
						 * for (sing1=0; sing1 <3; sing1++) for (sing2=0; sing2 <3; sing2++) for (sing3=0;
						 * sing3 <3; sing3++) for (sing4=0; sing4 <3; sing4++)
						 * bestplay[a][w][v][sing1][sing2][sing3][sing4] = 2;
						 */
					}
					else /* Sing3 Sing4 */{
						if ( ( pint == qint ) && ( qint == rint ) ) /* (q=r=p=m) */{
							aShortbestplay[ a ][ w ][ v ] = 2; //SET TO BEAT HAVOC ... not general
							/*
							 * for (sing1=0; sing1 <3; sing1++) for (sing2=0; sing2 <3; sing2++) for (sing3=0;
							 * sing3 <3; sing3++) for (sing4=0; sing4 <3; sing4++)
							 * bestplay[a][w][v][sing1][sing2][sing3][sing4] = sing4;
							 */
						}
						else /* ( q + r == 2*m) but each not m Sing3 */{
							aShortbestplay[ a ][ w ][ v ] = 2; //SET TO BEAT HAVOC ... not general
							/*
							 * for (sing1=0; sing1 <3; sing1++) for (sing2=0; sing2 <3; sing2++) for (sing3=0;
							 * sing3 <3; sing3++) for (sing4=0; sing4 <3; sing4++)
							 * bestplay[a][w][v][sing1][sing2][sing3][sing4] = sing3;
							 */
						}
					}
				}
			}
		}
	}
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

	my_history = new short[ aTrials + 1 ];
	opp_history = new short[ aTrials + 1 ];
	my_history[ 0 ] = opp_history[ 0 ] = 0;
	lastmove = 0;
	pi_index = 0;
	e_index = 0;
	sqrt_2_index = 0;
	/*
	 * sqrt_3_index=0; sqrt_5_index=0; sqrt_6_index=0; sqrt_7_index=0; sqrt_8_index=0;
	 * sqrt_10_index=0;
	 */

	this.trials = (short) aTrials;

	pr_history = new predict[ NUM_AGES ][ 3 ][ 2 ];
	pr_stathistory = new predict[ NUM_STATAGES ][ 3 ][ 2 ];
	/* pr_bstathistory = new predict[NUM_STATAGES][3][2][3][3][3][3]; */
	pr_shortbstathistory = new predict[ NUM_STATAGES ][ 3 ][ 2 ];
	pr_shortbflathistory = new predict[ NUM_STATAGES ][ 3 ][ 2 ];
	pr_shortbflatbughistory = new predict[ NUM_STATAGES ][ 3 ][ 2 ];
	pr_freq = new predict[ NUM_AGES ][ 2 ];
	pr_meta = new predict[ NUM_AGES ][ NUM_FREQ_PERIODS ];
	stats = new stat[ 2 ];

	for ( short i = 0; i < NUM_AGES; i++ ) {
		for ( short f = 0; f < NUM_FREQ_PERIODS; f++ )
			pr_meta[ i ][ f ] = new predict();
		for ( short j = 0; j < 2; j++ ) {
			pr_freq[ i ][ j ] = new predict();
			for ( short k = 0; k < 3; k++ )
				pr_history[ i ][ k ][ j ] = new predict(); // order i-k-j!
		}
	}

	for ( short i = 0; i < NUM_STATAGES; i++ ) {
		for ( short j = 0; j < 2; j++ ) {
			for ( short k = 0; k < 3; k++ ) {
				pr_stathistory[ i ][ k ][ j ] = new predict(); // order i-k-j!
				pr_shortbstathistory[ i ][ k ][ j ] = new predict();
				pr_shortbflathistory[ i ][ k ][ j ] = new predict();
				pr_shortbflatbughistory[ i ][ k ][ j ] = new predict();
				/*
				 * for (short s1=0; s1 <3; s1++) for (short s2=0; s2 <3; s2++) for (short s3=0; s3 <3;
				 * s3++) for (short s4=0; s4 <3; s4++) pr_bstathistory[i][k][j][s1][s2][s3][s4] = new
				 * predict;
				 */
			}
		}
	}

	stats[ 0 ] = new stat();
	stats[ 1 ] = new stat();
	pr_fixed = new predict();
	pr_foxtrot_my = new predict();
	pr_foxtrot_opp = new predict();
	pr_random = new predict();
	pr_pi = new predict();
	pr_e = new predict();
	pr_db = new predict();
	pr_db_2 = new predict();
	pr_sqrt_2 = new predict();
	/*
	 * pr_sqrt_3 = new predict(); pr_sqrt_5 = new predict(); pr_sqrt_6 = new predict(); pr_sqrt_7 =
	 * new predict(); pr_sqrt_8 = new predict(); pr_sqrt_10 = new predict();
	 */
	mres = new result();

	bestmove = new short[ 3 ];
	bestscorer = new short[ NUM_STATAGES ][ 3 ][ 2 ];
	//bestplay = new short[NUM_STATAGES][3][2][3][3][3][3];
	shortbestplay = new short[ NUM_STATAGES ][ 3 ][ 2 ];
	shortflatplay = new short[ NUM_STATAGES ][ 3 ][ 2 ];
	shortflatbugplay = new short[ NUM_STATAGES ][ 3 ][ 2 ];
	best_length = new short[ 3 ];

}

/**
 * @see com.anji.roshambo.RoshamboPlayer#storeMove(int, int)
 */
public void storeMove( int move, int score ) {

	opp_history[ 0 ]++;
	my_history[ 0 ]++;

	opp_history[ opp_history[ 0 ] ] = (short) move;
	my_history[ my_history[ 0 ] ] = lastmove;
}

    private int getRandomMove() {
        int randomMove = (int) Math.round(Math.random() * 2);
        return randomMove;
    }


/**
 * @see com.anji.roshambo.RoshamboPlayer#nextMove()
 */
public int nextMove() {

	short num = my_history[ 0 ];
	short last = ( num > 0 ) ? opp_history[ num ] : -1;
	short guess = (short)getRandomMove();

	if ( num > 0 ) {
		stats[ 0 ].add( my_history[ num ], (short) 1 );
		stats[ 1 ].add( opp_history[ num ], (short) 1 );
	}

	for ( short a = 0; a < NUM_AGES; ++a ) {
		do_history( AGES[ a ], bestmove );
		for ( short w = 0; w < 3; ++w ) {
			short b = bestmove[ w ];
			if ( 0 == b ) {
				pr_history[ a ][ w ][ 0 ].do_predict( last, guess );
				pr_history[ a ][ w ][ 1 ].do_predict( last, guess );
				continue;
			}
			pr_history[ a ][ w ][ 0 ].do_predict( last, my_history[ b + 1 ] );
			pr_history[ a ][ w ][ 1 ].do_predict( last, opp_history[ b + 1 ] );
		}

		for ( short p = 0; p < 2; ++p ) {
			mres.score = -1;
			if ( stats[ p ].max( AGES[ a ], (short) 0, mres ) ) /*
																																				  * TSH put in 0, should
																																				  * it be all f?
																																				  */
				pr_freq[ a ][ p ].do_predict( last, mres.move );
			else
				pr_freq[ a ][ p ].do_predict( last, guess );
		}
	}

	do_stathistory( NUM_STATAGES, STATAGES, bestscorer, shortbestplay, shortflatplay,
			shortflatbugplay/* ,bestplay */);
	for ( short a = 0; a < NUM_STATAGES; a++ ) {
		for ( short w = 0; w < 3; ++w ) {
			for ( short v = 0; v < 2; v++ ) {
				if ( bestscorer[ a ][ w ][ v ] == -1 ) {
					pr_stathistory[ a ][ w ][ v ].do_predict( last, guess );
				}
				else {
					pr_stathistory[ a ][ w ][ v ].do_predict( last, bestscorer[ a ][ w ][ v ] );
				}

				if ( shortbestplay[ a ][ w ][ v ] == -1 ) {
					pr_shortbstathistory[ a ][ w ][ v ].do_predict( last, guess );
				}
				else {
					pr_shortbstathistory[ a ][ w ][ v ].do_predict( last, shortbestplay[ a ][ w ][ v ] );
				}

				if ( shortflatplay[ a ][ w ][ v ] == -1 ) {
					pr_shortbflathistory[ a ][ w ][ v ].do_predict( last, guess );
				}
				else {
					pr_shortbflathistory[ a ][ w ][ v ].do_predict( last, shortflatplay[ a ][ w ][ v ] );
				}

				if ( shortflatbugplay[ a ][ w ][ v ] == -1 ) {
					pr_shortbflatbughistory[ a ][ w ][ v ].do_predict( last, guess );
				}
				else {
					pr_shortbflatbughistory[ a ][ w ][ v ].do_predict( last,
							shortflatbugplay[ a ][ w ][ v ] );
				}

				/*
				 * for (short s1=0; s1 <3; s1++) for (short s2=0; s2 <3; s2++) for (short s3=0; s3 <3;
				 * s3++) for (short s4=0; s4 <3; s4++) { if (bestplay[a][w][v][s1][s2][s3][s4]==-1) {
				 * pr_bstathistory[a][w][v][s1][s2][s3][s4].do_predict(last,guess); } else {
				 * pr_bstathistory[a][w][v][s1][s2][s3][s4].do_predict(last,bestplay[a][w][v][s1][s2][s3][s4]); } }
				 */

			}
		}
	}

	pr_random.do_predict( last, guess );
	pr_fixed.do_predict( last, (short) 0 );

	pr_foxtrot_my.do_predict( last, (short) ( ( my_history[ num ] + ( num / 2 ) ) % 3 ) );
	/* this explicitly catches me playing foxtrot */
	/* however this is actually kindof redundant if a freqperiod of 3 or 6 is included */
	pr_foxtrot_opp.do_predict( last, (short) ( ( opp_history[ num ] + ( num / 2 ) ) % 3 ) );
	/* this explicitly catches Foxtrot players */
	/* however this is actually kindof redundant if a freqperiod of 3 or 6 is included */

	pr_pi.do_predict( last, (short) ( pi_table[ pi_index ] % 3 ) );
	pi_index++;
	pi_index %= 1200;
	while ( pi_table[ pi_index ] == 0 ) {
		pi_index++;
		pi_index %= 1200;
	}

	pr_e.do_predict( last, (short) ( e_table[ e_index ] % 3 ) );
	e_index++;
	e_index %= 1200;
	while ( e_table[ e_index ] == 0 ) {
		e_index++;
		e_index %= 1200;
	}

	pr_sqrt_2.do_predict( last, (short) ( sqrt_2_table[ sqrt_2_index ] % 3 ) );
	sqrt_2_index++;
	sqrt_2_index %= 1200;
	while ( sqrt_2_table[ sqrt_2_index ] == 0 ) {
		sqrt_2_index++;
		sqrt_2_index %= 1200;
	}

	/*
	 * pr_sqrt_3.do_predict(last,(short)(sqrt_3_table[sqrt_3_index]%3)); sqrt_3_index++;
	 * sqrt_3_index %= 1200; while (sqrt_3_table[sqrt_3_index] == 0) { sqrt_3_index++;
	 * sqrt_3_index %= 1200; }
	 * 
	 * pr_sqrt_5.do_predict(last,(short)(sqrt_5_table[sqrt_5_index]%3)); sqrt_5_index++;
	 * sqrt_5_index %= 1200; while (sqrt_5_table[sqrt_5_index] == 0) { sqrt_5_index++;
	 * sqrt_5_index %= 1200; }
	 * 
	 * pr_sqrt_6.do_predict(last,(short)(sqrt_6_table[sqrt_6_index]%3)); sqrt_6_index++;
	 * sqrt_6_index %= 1200; while (sqrt_6_table[sqrt_6_index] == 0) { sqrt_6_index++;
	 * sqrt_6_index %= 1200; }
	 * 
	 * pr_sqrt_7.do_predict(last,(short)(sqrt_7_table[sqrt_7_index]%3)); sqrt_7_index++;
	 * sqrt_7_index %= 1200; while (sqrt_7_table[sqrt_7_index] == 0) { sqrt_7_index++;
	 * sqrt_7_index %= 1200; }
	 * 
	 * pr_sqrt_8.do_predict(last,(short)(sqrt_8_table[sqrt_8_index]%3)); sqrt_8_index++;
	 * sqrt_8_index %= 1200; while (sqrt_8_table[sqrt_8_index] == 0) { sqrt_8_index++;
	 * sqrt_8_index %= 1200; }
	 * 
	 * pr_sqrt_10.do_predict(last,(short)(sqrt_10_table[sqrt_10_index]%3)); sqrt_10_index++;
	 * sqrt_10_index %= 1200; while (sqrt_10_table[sqrt_10_index] == 0) { sqrt_10_index++;
	 * sqrt_10_index %= 1200; }
	 */

	pr_db.do_predict( last, db_table[ num % 1000 ] );
	pr_db_2.do_predict( last, db_table[ ( num + 1 ) % 1000 ] ); /*
																																											  * for
																																											  * the
																																											  * dummybot
																																											  * which
																																											  * starts
																																											  * its
																																											  * index
																																											  * at
																																											  * db_table[1]
																																											  * not
																																											  * db_table[0]
																																											  * ...
																																											  * it's a
																																											  * strange
																																											  * way of
																																											  * coding
																																											  */

	for ( short a = 0; a < NUM_AGES; ++a ) {
		mres.score = -1;
		mres.move = -1;

		for ( short aa = 0; aa < NUM_AGES; ++aa ) {
			for ( short p = 0; p < 2; ++p ) {
				for ( short w = 0; w < 3; ++w ) {
					pr_history[ aa ][ w ][ p ].scan( AGES[ a ], mres );
				}
				pr_freq[ aa ][ p ].scan( AGES[ a ], mres );
			}
		}

		for ( short aa = 0; aa < NUM_STATAGES; ++aa ) {
			for ( short p = 0; p < 2; ++p ) {
				for ( short w = 0; w < 3; ++w ) {
					pr_stathistory[ aa ][ w ][ p ].scan( AGES[ a ], mres );
					pr_shortbstathistory[ aa ][ w ][ p ].scan( AGES[ a ], mres );
					pr_shortbflathistory[ aa ][ w ][ p ].scan( AGES[ a ], mres );
					pr_shortbflatbughistory[ aa ][ w ][ p ].scan( AGES[ a ], mres );

					/*
					 * for (short s1=0; s1 <3; s1++) for (short s2=0; s2 <3; s2++) for (short s3=0; s3 <3;
					 * s3++) for (short s4=0; s4 <3; s4++)
					 * pr_bstathistory[aa][w][p][s1][s2][s3][s4].scan(AGES[a],mres);
					 */
				}
			}
		}

		pr_random.scan( AGES[ a ], mres );
		pr_fixed.scan( AGES[ a ], mres );
		pr_foxtrot_my.scan( AGES[ a ], mres );
		pr_foxtrot_opp.scan( AGES[ a ], mres );

		pr_pi.scan( AGES[ a ], mres );
		pr_e.scan( AGES[ a ], mres );

		pr_sqrt_2.scan( AGES[ a ], mres );
		/*
		 * pr_sqrt_3.scan(AGES[a],mres); pr_sqrt_5.scan(AGES[a],mres); pr_sqrt_6.scan(AGES[a],mres);
		 * pr_sqrt_7.scan(AGES[a],mres); pr_sqrt_8.scan(AGES[a],mres);
		 * pr_sqrt_10.scan(AGES[a],mres);
		 */

		pr_db.scan( AGES[ a ], mres );
		pr_db_2.scan( AGES[ a ], mres );

		for ( short f = 0; f < NUM_FREQ_PERIODS; f++ ) {
			pr_meta[ a ][ f ].do_predict( last, mres.move );
		}
	}

	mres.score = -1;
	mres.move = -1;

	for ( short a = 0; a < NUM_AGES; ++a ) {
		for ( short f = 0; f < NUM_FREQ_PERIODS; f++ ) {
			pr_meta[ a ][ f ].scan( num, mres );
		}
	}

	lastmove = mres.move;
	return mres.move;
}

/**
 * @see com.anji.tournament.Player#getPlayerId()
 */
public String getPlayerId() {
	return "GnoBot";
}

/**
 * @see com.anji.roshambo.RoshamboPlayer#getAuthor()
 */
public String getAuthor() {
	return "Toby Hudson";
}

/**
 * @see java.lang.Object#hashCode()
 */
public int hashCode() {
	return getPlayerId().hashCode();
}

}
