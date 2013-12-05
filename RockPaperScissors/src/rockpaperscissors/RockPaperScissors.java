package rockpaperscissors;

/**
 *
 * @author Nav
 */
public class RockPaperScissors {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int [] p={0,1,2,0,2,2,1,2,0,0};
        int currentNumber=7;
        
        //simple pattern analyzer
        // never use a bestLength of one 
        // remmber the longer the pattern the better it is and the likely hood of it being used increases
        int bestMovetoUseIndex=0;
        int lengthOfPattern=0;
        
        for(int i=currentNumber-1;i>0;i--){
            if(p[i]==p[currentNumber]){
               int indexToStartSearch=i;
               int maxLimit=currentNumber;
               int bestMoveLength=0;
               for(int x=indexToStartSearch;p[x]==p[maxLimit];x--){
                   maxLimit--;
                   bestMoveLength++;
               }
               if(bestMoveLength>lengthOfPattern){
                   lengthOfPattern=bestMoveLength;
                   bestMovetoUseIndex=i;
               }
            }
        }
        System.out.println(bestMovetoUseIndex);
    }
}
