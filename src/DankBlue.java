import reversi.*;
import java.util.Vector;
import java.util.Random;

public class DankBlue implements ReversiAlgorithm
{
      // Constants
  private final static int DEPTH_LIMIT = 5; // Just an example value.

  // Variables
  boolean initialized;
  volatile boolean running; // Note: volatile for synchronization issues.
  GameController controller;
  GameState initialState;
  int myIndex;
  Move selectedMove;
  int[][] gameboard = new int[][]{
		  { 0, 0, 0, 0, 0, 0, 0, 0},
		  { 0, 0, 0, 0, 0, 0, 0, 0},
		  { 0, 0, 0, 0, 0, 0, 0, 0},
		  { 0, 0, 0, 0, 0, 0, 0, 0},
		  { 0, 0, 0, 0, 0, 0, 0, 0},
		  { 0, 0, 0, 0, 0, 0, 0, 0},
		  { 0, 0, 0, 0, 0, 0, 0, 0},
		  { 0, 0, 0, 0, 0, 0, 0, 0}
  };

  public DankBlue() {} //the constructor
  
  public void requestMove(GameController requester)
  {
      running = false;
      requester.doMove(selectedMove);
  }

  public void init(GameController game, GameState state, int playerIndex, int turnLength)
  {
      initialState = state;
      myIndex = playerIndex;
      controller = game;
      initialized = true;
  }

  public String getName() { return "Dankblue"; }

  public void cleanup() {} //This will be called after game.

  public void run()
  {
      //implementation of the actual algorithm
      while(!initialized);
      initialized = false;
      running = true;
      selectedMove = null;

      int currentDepth = 1; 
      
      while (running && currentDepth < DEPTH_LIMIT)
      {
          Move newMove = searchToDepth(++currentDepth);
          
          // Check that there's a new move available.
          if (newMove != null)
              selectedMove = newMove;
      }
  
      if (running) // Make a move if there is still time left.
      {
    	  System.out.println("DankBlue chose move: " + selectedMove);
          controller.doMove(selectedMove);
      }
  }
  
  
  
  Move searchToDepth(int depth)
  {
      // - Create the tree of depth d (breadth first, depth first, beam search, alpha beta pruning, ...)
      // - Evaluate the leaf nodes
      // - If you think normal minimax search is enough, call the propagateScore method of the parent node
      //   of each leaf node
      // - Call the getOptimalChild method of the root node
      // - Return the move in the optimal child of the root node
      // - Don't forget the time constraint! -> Stop the algorithm when variable "running" becomes "false"
      //   or when you have reached the maximum search depth.
	  
	  int i = 0;
	  Node parent;
	  Node optimalNode;
      Move optimalMove;
      boolean maximize;
      Vector childparent = new Vector();
      Vector moves = initialState.getPossibleMoves(myIndex);
      
      //create root node
      Move rootmove = new Move(4,4,0);
      Node root = new Node(initialState, rootmove);
      Vector nodes_list = new Vector();
      nodes_list.add(root);
      
      // Create child nodes for tree to given depth
      createTree(0, depth, nodes_list, myIndex);   
      
      //long startTime = System.currentTimeMillis();
      // Propagate score
      find_leaf_parents(root, childparent);
      if (depth % 2 == 1){
    	  maximize = true; // bottom level maximation level
      } else {
    	  maximize = false; // bottom level minimization level
      }
      for (i = 0; i < childparent.size(); i++) {
    	  parent = (Node) childparent.elementAt(i);
    	  parent.propagateScore(maximize);
      }
      //long endTime   = System.currentTimeMillis();
      //long totalTime = endTime - startTime;
      //System.out.println("Time to propagate: " + totalTime);
      
      //root.print();
      // Print tree
      if (depth == 1){
    	  printTree(root, 0, 0); // Print tree to check something
      }
      
      // Select move
      if (moves.size() > 0) {
    	  optimalNode = root.getOptimalChild();
          optimalMove = optimalNode.getMove(); // Optimal child
          
          /*//print debug info
          System.out.println(moves);
          System.out.println("DankBlue chose move: " + optimalMove);
          String field = root.getState().toString();
		  System.out.println(field);*/
      } else {
              optimalMove = null;
      }
      return optimalMove;
  }
  
  
  
  void find_leaf_parents(Node noodi, Vector childparent) // This function will find nodes to propagate score
  {
	  Vector childit = noodi.getChildren(); // Take children
	  int childCount = childit.size();		// Calc number of children
	  
	  if (childCount == 0) {
		  // leaf node
		  Node parent = noodi.getParent();
		  if (!childparent.contains(parent)) {
			  childparent.add(parent);
		  }
		  return;
	  } else {
		  for (int i = 0; i < childCount; i++) {
			  Node child = (Node) childit.elementAt(i); 
			  find_leaf_parents(child, childparent); // Has to check if more Node child has more childs
		  }
	  }
  }
  

  void createTree(int depth, int depth_lim, Vector nodes, int pl_index) 
  {
	  // Makes tree with breadth first search to depth depth_lim
	  int i = 0;
	  int counter = 0;
	  Vector children = new Vector();
	  Node node;
	  
	  while (!nodes.isEmpty()) {
		  node = (Node)nodes.elementAt(0);
		  Vector moves = node.getState().getPossibleMoves(pl_index);
		  counter++;
		  if ((depth == depth_lim) || (moves.size() == 0)) {
			  // Now we are at leaf node
			  // here comes the scoring
			  double score = calc_scores(node);
			  node.setScore(score);
		  } else {
		      for (i = 0; i < moves.size(); i++) {
		    	  Move game_move = (Move) moves.elementAt(i);
		    	  GameState new_state = node.getState().getNewInstance(game_move);
		    	  Node child = new Node(new_state, game_move);
		    	  if (!children.contains(child)) {
		    		  node.addChild(child);
		    	  	  children.add(child);
		    	  }
		      }
		  }
	      nodes.removeElementAt(0);
	  }
	  //System.out.println("dep:" + depth);
	  //System.out.println("counter:" + counter);
      if (depth < depth_lim) {
    	  pl_index++;
      	  pl_index = pl_index % 2;
    	  createTree(depth + 1, depth_lim, children, pl_index);
    	  return;
      }
  }

  private double calc_scores(Node node) {
	// Calculates score for a given node
	
	double score = 0;
	if((node.getMove().getX() == 0 || node.getMove().getX() == 7) &&
	   (node.getMove().getY() == 0 || node.getMove().getY() == 7)) {
		score = 100000000.0;
	}
	else {
		Random rnd = new Random();
	
		score = rnd.nextDouble();
		score = score * 100;
	}
	
	return score;
  }

  void printTree(Node noodi, int dep, int mode) // This function will print the tree
  {
	  String field;
	  String move_string;
	  Vector childit = noodi.getChildren(); // Take children
	  int childCount = childit.size();		// Calc number of children
	  
	  // Print the wanted info
	  if (mode == 0){
		  String text = String.valueOf(noodi.getScore());
		  text = "        ".substring(0, dep) + text;
		  System.out.println(text); // Print the tree
	  } else {
		  System.out.println("Syvyys: " + dep);
		  move_string = noodi.getMove().toString();
		  System.out.println(move_string);
		  field = noodi.getState().toString();
		  System.out.println(field);
	  }
	  
	  if (childCount == 0) {
		  dep--; // leaf node
		  return;
	  } else {
		  for (int i = 0; i < childCount; i++) {
			  Node child = (Node) childit.elementAt(i); 
			  printTree(child, dep + 1, mode); // Has to check if more Node child has more childs
		  }
	  }
  }
}
  