package de.dfki.lt.mdparser.data;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO asap:

// given a flatSentence and linearized dependency tree, create a CONLL output
// Make sure to follow MDParser version so that eval method can be used

/*
* Aim: create a proper CONLL tree from a given pair of aligned sentence and dependency tree
* MDParser conll format uses column 6 and 7 for head and label,
* and 8 and 9 for predicted (have to check) in order to run evaluation script
*    1, 2, and 3 for ID, word and POS, example:
*      ID  word  POS _ _ head  label _ _
*      1 The _ DT  _ _ 2 NMOD  _ _
* a linearized dependency tree is created in left-to-right top-down way
*
* I should first create a specific order based on the tree.
* a problem is that from the sequence of daughters, I do not know where to insert in the head element
* that means: where to cut the daughter sequence.
* Anyway, I could use an initial indexing, by placing the head at the end/front.
* Then use the position of a word in the aligned sentence for adjusting the initial sequence
* I could also search for the head index, by searching the word form of the head in left to right order
* (and also remember, which index has been used - in case a word occurs several times).
* If I do this for all words, I get at least some sort of relative ordering.
* But maybe, I have to take care about possible matches of a head element in the string.
* And it might only work for projective trees.
*
* NOTE: basically this means that I have to define an aligner, but can assume that has an implicit order
* since it was created automatically from a sequence.
*
* (_RT intends (_SBJ bill (_NMOD The )_NMOD )_SBJ (_OPRD to (_IM restrict (_OBJ RTC (_NMOD the )_NMOD )_OBJ
* (_ADV to (_PMOD borrowings (_NMOD Treasury )_NMOD (_NMOD only )_NMOD )_PMOD )_ADV )_IM )_OPRD (_P , )_P
* (_ADV unless (_SUB receives (_SBJ agency (_NMOD the )_NMOD )_SBJ (_OBJ authorization (_NMOD specific )_NMOD
* (_NMOD congressional )_NMOD )_OBJ )_SUB )_ADV (_P . )_P )_RT
*
* The|DT bill|NN intends|VBZ to|TO restrict|VB the|DT RTC|NNP to|TO Treasury|NNP borrowings|NNS only|RB ,|,
* unless|IN the|DT agency|NN receives|VBZ specific|JJ congressional|JJ authorization|NN .|.
*
* Define a Sentence class which as a sentArray.
* Initialize it with sequence
* Also create a parallel hash, which maps a word to its index and pos (as a tuple).
*

*
* Initialize labelStack = push()
* Initialize headIdStack = push(0)
* nextElem=pop(sequence)
* if newElem=) then
*  if newElem=top(labelStack) then pop(labelStack) & pop(headIdStack) break
* if newElem=( then
*   push(LABEL, Labelstack) break
* if newElem = WORD then
*  - identify word index using hash
*  - add headID = top(headIdStack) & label = top(labelStack)
*  - push(wordID, headIdStack)
*
*
*/
public class DeLinearizedSentence {
  private List<String> linearizedSentence;
  private int infoSize = 11; // Number of CONLL columns -1
  private Sentence conllSentence = null;
  private Map<String, Deque<Integer>> wordIndexPos = new HashMap<String,Deque<Integer>>();
  private Deque<Integer> headIdStack = new ArrayDeque<Integer>();
  private Deque<String> labelStack = new ArrayDeque<String>();


  private List<String> makeSequenceFromString(String string){
    return new ArrayList<String>(Arrays.asList(string.split(" ")));
  }

  /**
   * This is used to define a mapping from a word to its different mentioning positions in a sentence.
   * The order in the stack/deque is from left to right.
   * @param word
   * @param index
   */
  private void adddWordIndextoHash(String word, int index) {
    if (this.wordIndexPos.containsKey(word)) {
      this.wordIndexPos.get(word).addLast(index);
    }
    else {
      Deque<Integer> indexStack = new ArrayDeque<Integer>();
      indexStack.add(index);
      this.wordIndexPos.put(word, indexStack);
    }
  }

  public void printHashMap() {
    for(String key : this.wordIndexPos.keySet()) {
      System.out.println(key + ": " + this.wordIndexPos.get(key));
  }
  }

  /**
   * Receives a wordPosSequence of tokens of form word|POS
   * and creates an initial Conll sentence object with filled
   * columns for index, word, pos.
   * As a side effect, it creates a mapping from word to all possible indexes in the sentence, e.g.,
   * the word "a" can occur several times in a sentence at several positions, so we insert the indices
   * in a stack from left to right.
   */

  private Sentence createInitialConllSentence(List<String> wordPosSequence) {
    String[][] sentArray = new String[wordPosSequence.size()][this.infoSize];
    for (int i = 0; i < wordPosSequence.size(); i++) {
     String[] wordPostoken = wordPosSequence.get(i).split("\\|");

     String word = wordPostoken[0];
      String pos = wordPostoken[1];


      sentArray[i][0] = String.valueOf(i);
      sentArray[i][1] = word;
      sentArray[i][3] = pos;

      this.adddWordIndextoHash(word, i);
    }

    return new Sentence(sentArray);

  }

  //TODO
  //HIERIX:
  // loop over linearizedSentence and add index and label information to the conllSentence

  public static void main(String[] args) {
    DeLinearizedSentence testix = new DeLinearizedSentence();

    List<String> sequence = testix.makeSequenceFromString(
        "The|DT bill|NN intends|VBZ to|TO restrict|VB the|DT RTC|NNP to|TO Treasury|NNP borrowings|NNS only|RB ,|,"
        + "unless|IN the|DT agency|NN receives|VBZ specific|JJ congressional|JJ authorization|NN .|."
            );

    testix.linearizedSentence = testix.makeSequenceFromString(
        "(_RT intends (_SBJ bill (_NMOD The )_NMOD )_SBJ (_OPRD to (_IM restrict (_OBJ RTC (_NMOD the )_NMOD )_OBJ\n "
        + "(_ADV to (_PMOD borrowings (_NMOD Treasury )_NMOD (_NMOD only )_NMOD )_PMOD )_ADV )_IM )_OPRD (_P , )_P\n "
        + "(_ADV unless (_SUB receives (_SBJ agency (_NMOD the )_NMOD )_SBJ (_OBJ authorization (_NMOD specific )_NMOD\n "
        + "NMOD congressional )_NMOD )_OBJ )_SUB )_ADV (_P . )_P )_RT"
            );
    System.out.println(testix.linearizedSentence.toString());

    testix.conllSentence = testix.createInitialConllSentence(sequence);

   System.out.println(testix.conllSentence.toString());
   testix.printHashMap();


  }

}