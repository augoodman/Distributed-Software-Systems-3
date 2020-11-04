package client;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The ClientGui class is a GUI frontend that displays an image grid, an input text box,
 * a button, and a text area for status. 
 *
 * Methods of Interest
 * ----------------------
 * show(boolean modal) - Shows the GUI frame with the current state
 *     -> modal means that it opens the GUI and suspends background processes. Processing 
 *        still happens in the GUI. If it is desired to continue processing in the 
 *        background, set modal to false.
 * newGame(int dimension) - Start a new game with a grid of dimension x dimension size
 * insertImage(String filename, int row, int col) - Inserts an image into the grid
 * submitClicked() - Button handler for the submit button in the output panel
 * inputUpdated() - Tracks the current state of the input text box
 *
 * Notes
 * -----------
 * > Does not show when created. show() must be called to show he GUI.
 *
 */
public class ClientGui implements OutputPanel.EventHandlers {
  JDialog frame;
  PicturePanel picturePanel;
  OutputPanel outputPanel;
  static ClientGui main = new ClientGui();
  static List<String[]> questions = new ArrayList<String[]>();
  static List<Integer[]> clues = new ArrayList<Integer[]>();
  static String solution = "Pineapple Upside Down Cake"; //hard coded for now, will come from server
  static String input;
  static String expectedAnswer = null;
  static int dimension = 1; //default value
  static int lifeCount = 3;
  static int remainingClues;
  static boolean winFlag = false;
  static boolean loseFlag = false;
  static boolean correctFlag = false;
  static boolean answered = true;
  static boolean dimensionFlag = false;


  /**
   * Construct dialog
   */
  public ClientGui() {
    frame = new JDialog();
    frame.setLayout(new GridBagLayout());
    frame.setMinimumSize(new Dimension(500, 500));
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    // setup the top picture frame
    picturePanel = new PicturePanel();
    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 0;
    c.weighty = 0.25;
    frame.add(picturePanel, c);

    // setup the input, button, and output area
    c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 1;
    c.weighty = 0.75;
    c.weightx = 1;
    c.fill = GridBagConstraints.BOTH;
    outputPanel = new OutputPanel();
    outputPanel.addEventHandlers(this);
    frame.add(outputPanel, c);
  }

  /**
   * Shows the current state in the GUI
   * @param makeModal - true to make a modal window, false disables modal behavior
   */
  public void show(boolean makeModal) {
    frame.pack();
    frame.setModal(makeModal);
    frame.setVisible(true);
  }

  /**
   * Creates a new game and set the size of the grid
   */
  public void newGame() {
    //picturePanel.newGame(dimension);
    String[] getDimension = {"Enter dimension of puzzle (2, 3 or 4)."};
    main.askQuestion(getDimension);
  }

  /**
   * Insert an image into the grid at position (col, row)
   *
   * @param filename - filename relative to the root directory
   * @param row - the row to insert into
   * @param col - the column to insert into
   * @return true if successful, false if an invalid coordinate was provided
   * @throws IOException An error occured with your image file
   */
  public boolean insertImage(String filename, int row, int col) throws IOException {
    String error = "";
    try {
      // insert the image
      if (picturePanel.insertImage(filename, row, col)) {
        // put status in output
        //outputPanel.appendOutput("Inserting " + filename + " in position (" + row + ", " + col + ")");
        return true;
      }
      error = "File(\"" + filename + "\") not found.";
    } catch(PicturePanel.InvalidCoordinateException e) {
      // put error in output
      error = e.toString();
    }
    outputPanel.appendOutput(error);
    return false;
  }

  /**
   * Submit button handling
   *
   * Change this to whatever you need
   */
  @Override
  public void submitClicked() throws IOException {
    // Pulls the input box text
    input = outputPanel.getInputText();
    // if has input
    if (input.length() > 0) {
      // append input to the output panel
      outputPanel.appendOutput(input);
      checkAnswer(input);
      // clear input text box
      outputPanel.setInputText("");
    }
  }

  /**
   * Key listener for the input text box
   *
   * Change the behavior to whatever you need
   */
  @Override
  public void inputUpdated(String input) {
    if (input.equals("surprise")) {
      outputPanel.appendOutput("You found me!");
    }
  }

  /**
   * Prints a question to the GUI for the player to answer
   *
   * @param question - an array containing a question and an answer
   */
  public void askQuestion(String[] question){
    if(question.length == 1){
      outputPanel.appendOutput(question[0]);
      //dimension =
    }
    else {
      answered = false;
      outputPanel.appendOutput(question[0]);
      expectedAnswer = question[1];
    }
  }

  /**
   * Logic to advance the game. Takes in a response from a player, checks for correctness and advances the game as
   * appropriate.
   *
   * @param answerGiven - the player's response to a question
   */
  public void checkAnswer(String answerGiven) throws IOException {
    if(!dimensionFlag){
      dimension = Integer.parseInt(answerGiven);
      remainingClues = dimension * dimension;
      picturePanel.newGame(dimension);
      outputPanel.appendOutput("Started new game with a " + dimension + "x" + dimension + " board.");
      dimensionFlag = true;
      //create clue image grid
      for(int i = 0; i < dimension; i++) {
        for (int j = 0; j < dimension; j++) {
          Integer[] clue = {i, j};
          clues.add(clue);
        }
      }
      main.askQuestion(questions.remove((int) (Math.random() * (questions.size()))));
    }
    else if(solution.equalsIgnoreCase((answerGiven)) && !loseFlag){
      outputPanel.appendOutput("You Win!");
      winFlag = true;
      answered = true;
    }
    else if (expectedAnswer.equalsIgnoreCase(answerGiven) && !loseFlag) {
        outputPanel.appendOutput("Correct!");
        correctFlag = true;
        answered = true;
        Integer[] clue;
        if (!clues.isEmpty()) {
          clue = clues.remove((int) (Math.random() * (remainingClues - 1)));
          int i = clue[0];
          int j = clue[1];
          remainingClues--;
          if (remainingClues >= 0) {
            main.insertImage("src/main/java/server/img/Pineapple-Upside-down-cake_" + i + "_" + j + ".jpg", i, j); //image and path will come from server
            if (remainingClues == 0) {
              outputPanel.appendOutput("Out of clues, solve the puzzle!");
            } else {
              outputPanel.appendOutput("Solve the puzzle or answer the next question for another clue!");
              main.askQuestion(questions.remove((int) (Math.random() * (questions.size()))));
            }
          }
        }
    }
    else {
      lifeCount--;
      if(lifeCount == 0){
        loseFlag = true;
        outputPanel.appendOutput(("Incorrect! You Lose!"));
      }
      else if (!loseFlag){
        outputPanel.appendOutput(("Incorrect! You have " + lifeCount + " chance(s) left!"));
        answered = true;
      }
    }
  }

  public static String getResponse() {
    return input;
  }

  public static void main(String[] args) throws IOException {
    // create the frame
    main.newGame();

    //create question bank
    String[] question0 = {"Question: First president (last name)?", "Washington"};
    String[] question1 = {"Question: 13 * 3 =", "39"};
    String[] question2 = {"Question: Capital of California?", "Sacramento"};
    String[] question3 = {"Question: SER 321 Instructor (last name)?", "Mehlhase"};
    String[] question4 = {"Question: Best School? (3 letters)", "ASU"};
    String[] question5 = {"Question: What is Pi? (round nearest hundredth)", "3.14"};
    String[] question6 = {"Question: What letter is missing in 'Softwar_ _ngin__r'?", "E"};
    String[] question7 = {"Question: What animal barks?", "Dog"};
    String[] question8 = {"Question: Number of letters in alphabet?", "26"};
    questions.add(question0);
    questions.add(question1);
    questions.add(question2);
    questions.add(question3);
    questions.add(question4);
    questions.add(question5);
    questions.add(question6);
    questions.add(question7);
    questions.add(question8);


    //ask the first question
    //main.askQuestion(questions.remove((int)(Math.random() * (questions.size()))));

    main.show(true);
  }
}
