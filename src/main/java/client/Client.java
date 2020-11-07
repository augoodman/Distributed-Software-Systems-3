package client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * The Client class contains client logic and is combined with the GUI frontend that
 * displays an image grid, an input text box, a button, and a text area for status.
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
 * askQuestion(String[] question) - Prints a question to the GUI for the player to answer
 * checkAnswer(String answerGiver) - Logic to advance the game. Takes in a response from
 * a player, checks for correctness and advances the game as appropriate.
 *
 * Notes
 * -----------
 * > Does not show when created. show() must be called to show he GUI.
 *
 */
public class Client implements OutputPanel.EventHandlers {
  JDialog frame;
  PicturePanel picturePanel;
  OutputPanel outputPanel;
  GridMaker gridMaker;
  static Socket s;
  static Client main = new Client();
  static DataInputStream in;
  static DataOutputStream out;
  static String clientProtocolHeader;
  static String serverProtocolHeader;
  static String delim = "[ ]+";
  static List<Integer[]> clues = new ArrayList<Integer[]>();
  static List<BufferedImage> imageList = new ArrayList<BufferedImage>();
  static String solution;
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
  static int numQuestions = 0;
  static int numQuestionsRight = 0;
  static int numQuestionsWrong = 0;

  /**
   * Construct dialog
   */
  public Client() {
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
      String[] args = new String[2];
      dimension = Integer.parseInt(answerGiven);
      remainingClues = dimension * dimension;
      picturePanel.newGame(dimension);
      outputPanel.appendOutput("Started new game with a " + dimension + "x" + dimension + " board.");
      dimensionFlag = true;
      clientProtocolHeader = "c " + dimension;
      out.writeUTF(clientProtocolHeader);
      serverProtocolHeader = in.readUTF();
      String[] tokens = serverProtocolHeader.split(delim);
      if(tokens[0].equalsIgnoreCase("s")){
        System.out.println("Server data received.");
        numQuestions = Integer.parseInt(tokens[1]);
        System.out.println(numQuestions);
      }
      System.out.println("Reading in image.");
      byte[] sizeArray = new byte[4];
      in.read(sizeArray);
      int size = ByteBuffer.wrap(sizeArray).asIntBuffer().get();
      byte[] imageArray = new byte[size];
      in.read(imageArray);
      BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageArray));
      if(dimension == 2) {
        ImageIO.write(image, "jpg", new File("src/main/java/client/img/image2.jpg"));
        solution = in.readUTF();
        //System.out.println(solution);
        String source = "src/main/java/client/img/image2.jpg";
        args[0] = source;
        args[1] = "2";
        gridMaker.main(args);
      }
      else if(dimension == 3) {
        ImageIO.write(image, "jpg", new File("src/main/java/client/img/image3.jpg"));
        solution = in.readUTF();
        //System.out.println(solution);
        String source = "src/main/java/client/img/image3.jpg";
        args[0] = source;
        args[1] = "3";
        gridMaker.main(args);
      }
      else {
        ImageIO.write(image, "jpg", new File("src/main/java/client/img/image4.jpg"));
        solution = in.readUTF();
        //System.out.println(solution);
        String source = "src/main/java/client/img/image4.jpg";
        args[0] = source;
        args[1] = "4";
        gridMaker.main(args);
      }
      String[] qa = new String[2];
      qa[0] = in.readUTF();
      qa[1] = in.readUTF();
      main.askQuestion(qa);
      imageList.add(image);
      for (int i = 0; i < dimension; i++) {
        for (int j = 0; j < dimension; j++) {
          //create clue image grid
          Integer[] clue = {i, j};
          clues.add(clue);
        }
      }
    }
    else if(solution.equalsIgnoreCase((answerGiven)) && !loseFlag){
      outputPanel.appendOutput("You Win!");
      outputPanel.appendOutput(("You got " + numQuestionsRight + " questions right!"));
      outputPanel.appendOutput(("You got " + numQuestionsWrong + " questions wrong!"));
      winFlag = true;
      answered = true;
      s.close();
    }
    else if (expectedAnswer.equalsIgnoreCase(answerGiven) && !loseFlag) {
        outputPanel.appendOutput("Correct!");
        numQuestionsRight++;
        correctFlag = true;
        answered = true;
        Integer[] clue;
        if (!clues.isEmpty()) {
          clue = clues.remove((int) (Math.random() * (remainingClues - 1)));
          int i = clue[0];
          int j = clue[1];
          remainingClues--;
          if (remainingClues >= 0) {
            if(dimension == 2)
              main.insertImage("src/main/java/client/img/image2_" + i + "_" + j + ".jpg", i, j); //image and path will come from server
            else if (dimension == 3)
              main.insertImage("src/main/java/client/img/image3_" + i + "_" + j + ".jpg", i, j); //image and path will come from server
            else
              main.insertImage("src/main/java/client/img/image4_" + i + "_" + j + ".jpg", i, j); //image and path will come from server
            if (remainingClues == 0) {
              outputPanel.appendOutput("Out of clues, solve the puzzle!");
            } else {
              outputPanel.appendOutput("Solve the puzzle or answer the next question for another clue!");
              String[] qa = new String[2];
              qa[0] = in.readUTF();
              qa[1] = in.readUTF();
              main.askQuestion(qa);
              numQuestions--;
            }
          }
        }
    }
    else {
      lifeCount--;
      if(lifeCount == 0){
        loseFlag = true;
        outputPanel.appendOutput(("The solution was: '" + solution + "'. You Lose!"));
        outputPanel.appendOutput(("You got " + numQuestionsRight + " questions right!"));
        outputPanel.appendOutput(("You got " + numQuestionsWrong + " questions wrong!"));
        s.close();
      }
      else if (!loseFlag){
        outputPanel.appendOutput(("Incorrect! You have " + lifeCount + " chance(s) left!"));
        numQuestionsWrong++;
        answered = true;
      }
    }
  }

  public static void main(String[] args) throws IOException {
    // create the frame
    if (args.length != 2) {
      System.out.println("Wrong number of arguments:\ngradle runClient --args=\"host port\"");
      System.exit(0);
    }
    String host = args[0];
    int port = 9099; // default port
    try {
      port = Integer.parseInt(args[1]);
    } catch (NumberFormatException nfe) {
      System.out.println("port must be integer");
      System.exit(2);
    }
    //2) create a socket using tcp
    try{
      s = new Socket(host, port);
      in = new DataInputStream( s.getInputStream());
      //4) establish connection to server
      out = new DataOutputStream( s.getOutputStream());
      //5) send data
      //6) receive data
      main.newGame();
      main.show(true);
    } catch (UnknownHostException e) {
      System.out.println("Socket:"+e.getMessage());
    } catch (EOFException e) {
      System.out.println("EOF:"+e.getMessage());
    } catch (IOException e) {
      System.out.println("readline:"+e.getMessage());
    } finally {
      if(s!=null)
        try {
          //8) close the socket
          s.close();
        } catch (IOException e) {
          System.out.println("close:"+e.getMessage());
        }
    }
  }
}
