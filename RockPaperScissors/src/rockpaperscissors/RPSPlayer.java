package rockpaperscissors;

import java.util.Scanner;

/**
 *
 * @author Nav
 */
public class RPSPlayer {

    public static final int ROCK = 0;
    public static final int PAPER = 1;
    public static final int SCISSORS = 2;
    
    public static final int NUM_OF_PREDICTORS=2;
    
    private int numOfTrials;
    private int currentTrialNumber = 0;
    private int myMove;
    private int[] myHistoryOfMoves;
    private int[] oppHistoryOfMoves;
    private int[][] freq;
    private int [][]scores;
    private HistoryPredictor historyPrdiector=new HistoryPredictor();
    private FrequencyPredictor frequencyPredictor=new FrequencyPredictor();
    
    public RPSPlayer(int numOfTrials) {
        this.numOfTrials = numOfTrials;
        this.myHistoryOfMoves = new int[numOfTrials];
        this.oppHistoryOfMoves = new int[numOfTrials];
        this.freq = new int[3][3];
        this.scores=new int[RPSPlayer.NUM_OF_PREDICTORS*6][2];
    }

    class HistoryPredictor implements Predictor {

        @Override
        public int getPrediction() {
            int currentNumber = currentTrialNumber-1;

            //simple pattern analyzer
            // never use a bestLength of one 
            // remmber the longer the pattern the better it is and the likely hood of it being used increases
            int bestMovetoUseIndex = 0;
            int lengthOfPattern = 0;

            for (int i = currentNumber - 1; i > 0; i--) {
                if (oppHistoryOfMoves[i] == oppHistoryOfMoves[currentNumber]) {
                    int indexToStartSearch = i;
                    int maxLimit = currentNumber;
                    int bestMoveLength = 0;
                    for (int x = indexToStartSearch; x>-1 && oppHistoryOfMoves[x] == oppHistoryOfMoves[maxLimit]; x--) {
                        maxLimit--;
                        bestMoveLength++;
                    }
                    if (bestMoveLength>1 && bestMoveLength > lengthOfPattern) {
                        lengthOfPattern = bestMoveLength;
                        bestMovetoUseIndex = i;
                    }
                }
            }
            if(lengthOfPattern>1){
                return oppHistoryOfMoves[bestMovetoUseIndex+1];
            }else{
                return 0;
            }
        }
    }
    
    class FrequencyPredictor implements Predictor {

        @Override
        public int getPrediction() {
            int lastOppMove = oppHistoryOfMoves[currentTrialNumber - 1];
            int predictionOfOppsMove = getArrayMaxIndex(freq[lastOppMove]);
            return (predictionOfOppsMove+1) % 3;
        }
    }
    

    private int getRandomMove() {
        int randomMove = (int) Math.round(Math.random() * 2);
        return randomMove;
    }

    public void storeMove(int oppsMove) {
                
        myHistoryOfMoves[currentTrialNumber] = myMove;
        oppHistoryOfMoves[currentTrialNumber] = oppsMove % 3;

        //update the strategies and their respective scores
        for(int i=0;i<scores.length;i++){
            scores[i][1]+=scoreCalculator(scores[i][0], oppsMove%3);
        }
        
        if (currentTrialNumber > 0) {
            //update frequency table
            int previousMove = oppHistoryOfMoves[currentTrialNumber - 1];
            freq[previousMove][oppsMove % 3] += 1;
        }

        if (currentTrialNumber < numOfTrials) {
            currentTrialNumber++;
        }
    }

    private int scoreCalculator(int myMove,int oppMove){
        int score= (3+(myMove-oppMove))%3;
        
        //translate score to tournament style scoring
        // a win=1, a tie=0, a loss=-1;
        // in our case all the values correspond except 
        //for loss which has a value of 2, therefore change
        // the value to -1
        if(score==2){
            score=-1;
        }
        return score;
    }
    
    public int nextMove() {
        if (currentTrialNumber == 0) {
            myMove = getRandomMove();
        } else {
            updateBestMoves();
            int bestStrategy=-1;
            for(int i=0;i<scores.length;i++){
                if(scores[i][1]>bestStrategy){
                    bestStrategy=i;
                }
            }
            if(bestStrategy>1){
                myMove=scores[bestStrategy][0];
                System.out.println("Best strategy\t"+bestStrategy);
            }else{
                myMove = getRandomMove();
            }
        }
        return myMove;
    }

    private void updateBestMoves(){
        
        //Really shitty coding here, need to optimize , I hate hardcoded variables
        
        int bestFreqMove=frequencyPredictor.getPrediction();
        for(int i=0;i<6;i++){
            if(i<3){
               scores[i][0]=(bestFreqMove+i)%3; 
            }else{
               scores[i][0]=(bestFreqMove+i+1)%3;
            }
        }
        
        int bestHistMove=historyPrdiector.getPrediction();
        for(int i=6;i<12;i++){
            if(i<3){
               scores[i][0]=(bestHistMove+i)%3; 
            }else{
               scores[i][0]=(bestHistMove+i+1)%3;
            }
        }
        
    }
    
    public int getArrayMaxIndex(int[] array) {
        int move = -1;
        int max = -1;
        for (int i = 0; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
                move = i;
            }
        }
        return move;
    }

    public void printArray() {
        for (int i = 0; i < freq.length; i++) {
            for (int x = 0; x < freq[0].length; x++) {
                System.out.print(freq[i][x]);
            }
        }
    }

    public static void main(String args[]) {
        int numofTrials = 100;
        int numOfWins = 0;
        int numOflosses = 0;
        int numOfTies = 0;
        RPSPlayer player = new RPSPlayer(numofTrials);
        MarshalbotPlayer opp = new MarshalbotPlayer();
        opp.reset(numofTrials);
        for (int i = 0; i < numofTrials; i++) {
            System.out.println("Rock=0, Paper=1, Scissors=2\t turn number#" + (i + 1));

            int oppMove = opp.nextMove();
            int myMove = player.nextMove();
            int score = calcScore(myMove, oppMove);
            player.storeMove(oppMove);
            opp.storeMove(myMove, score * -1);
            if (score == 0) {
                System.out.println("****Tied**");
                numOfTies++;
            } else if (score == 1) {
                System.out.println("****My program won, other program lost");
                numOfWins++;
            } else if (score == -1) {
                System.out.println("****My program lost, other program won");
                numOflosses++;
            }
        }
        System.out.println("games won\t" + numOfWins);
        System.out.println("games tied\t" + numOfTies);
        System.out.println("games losses\t" + numOflosses);

        /*Scanner scanner = new Scanner(System.in);
         for (int i = 0; i < numofTrials; i++) {
         System.out.println("Rock=0, Paper=1, Scissors=2\t turn number#"+(i+1));
         int oppMove = scanner.nextInt();
         int myMove = player.nextMove();
         int score = calcScore(myMove, oppMove);
         player.storeMove(oppMove);
         if (score == 0) {
         System.out.println("****Tied**");
         } else if (score == 1) {
         System.out.println("****Computer won, you lost");
         } else if (score == -1) {
         System.out.println("****Computer lost, you won");
         }
         }
         player.printArray();*/
    }

    public static int calcScore(int me, int opp) {

        switch (me) {
            case ROCK:
                System.out.print("Computer ROCK     ");
                if (opp == ROCK) {
                    System.out.println("Me ROCK");
                    return 0;
                } else if (opp == PAPER) {
                    System.out.println("Me PAPER");
                    return -1;
                } else if (opp == SCISSORS) {
                    System.out.println("Me SCISSORS");
                    return 1;
                }
            case PAPER:
                System.out.print("Computer PAPER    ");
                if (opp == ROCK) {
                    System.out.println("Me ROCK");
                    return 1;
                } else if (opp == PAPER) {
                    System.out.println("Me PAPER");
                    return 0;
                } else if (opp == SCISSORS) {
                    System.out.println("Me SCISSORS");
                    return -1;
                }
            case SCISSORS:
                System.out.print("Computer SCISSORS     ");
                if (opp == ROCK) {
                    System.out.println("Me ROCK");
                    return -1;
                } else if (opp == PAPER) {
                    System.out.println("Me PAPER");
                    return 1;
                } else if (opp == SCISSORS) {
                    System.out.println("Me SCISSORS");
                    return 0;
                }
        }
        return -1;
    }
}
