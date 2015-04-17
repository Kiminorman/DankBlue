import reversi.*;

import java.util.Vector;

public class DankBlue implements ReversiAlgorithm {
      // Constants
  private final static int DEPTH_LIMIT = 8;// Just an example value.

  // Variables
  boolean initialized;
  volatile boolean running; // Note: volatile for synchronization issues.
  GameController controller;
  GameState initialState;
  int myIndex;
  int oppIndex;
  Move selectedMove;
  double[][] gameboard = new double[][]{
		  { 20, -3, 11,  8,  8, 11, -3, 20},
		  { -3, -7, -4,  1,  1, -4, -7, -3},
		  { 11, -4,  2,  2,  2,  2, -4, 11},
	      {  8,  1,  2, -3, -3,  2,  1,  8},
	      {  8,  1,  2, -3, -3,  2,  1,  8},
	      { 11, -4,  2,  2,  2,  2, -4, 11},
	      { -3, -7, -4,  1,  1, -4, -7, -3},
	      { 20, -3, 11,  8,  8, 11, -3, 20},
  };

  public DankBlue() {} //the constructor
  
  public void requestMove(GameController requester)
  {
      running = false;
      System.out.println("DankBlue was forced to select move: " + selectedMove);
      requester.doMove(selectedMove);
  }

  public void init(GameController game, GameState state, int playerIndex, int turnLength)
  {
      initialState = state;
      myIndex = playerIndex;
      oppIndex = (playerIndex ^ 1);
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
          //System.out.println(currentDepth);
      }
  
      if (running) // Make a move if there is still time left.
      {
    	  System.out.println("DankBlue chose move: " + selectedMove);
          controller.doMove(selectedMove);
      }
  }
  
  
  Move searchToDepth(int depth)
  {  
	  int i = 0;
	  Node optimalNode;
      Move optimalMove;
      Vector nodes_list = new Vector();
      Vector moves = initialState.getPossibleMoves(myIndex);
      
      // Takes always corner
      for (i = 0; i < moves.size(); i++) {
    	 Move move = (Move) moves.elementAt(i);
    	 if (((move.getX() == 0) || (move.getX() == 7)) &&
    	     ((move.getY() == 0) || (move.getY() == 7))) {
    		 return move;
    	 }
      }
      
      // Create root node
      Node root = new Node(initialState, null);
      nodes_list.add(root);
      
      // Create tree to given depth and score leafs
      createTree(0, depth, nodes_list, myIndex);
      
      // Propagate score with propagateScore method
      spreadScores(depth, root);
      
      // Print tree info
      /*if (depth == 4) {
    	  printTree(root, 0, 0, 2);
      }*/
      
      System.out.println(depth);
      
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
    	  System.out.println("No moves to do!");
    	  optimalMove = null;
      }
      return optimalMove;
  }
  

  void createTree(int depth, int depth_lim, Vector nodes, int pl_index) {
	  // Makes tree with breadth first search to depth depth_lim
	  int i = 0;
	  int counter = 0;
	  Vector children = new Vector();
	  Node node;
	  
	  // This loop hecks children for every node at certain depth
	  while (!nodes.isEmpty()) {
		  if (running == false){
			  return;
		  }
		  node = (Node)nodes.elementAt(0);
		  Vector moves = node.getState().getPossibleMoves(pl_index);
		  counter++;
		  if ((depth == depth_lim) || (moves.size() == 0)) {
			  // Now we are at leaf node
			  // here comes the scoring
			  double score = calc_scores(node);
			  node.setScore(score);
		  } else { // Check children
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
	  // Continue if not yet at limit
      if (depth < depth_lim) {
    	  createTree(depth + 1, depth_lim, children, pl_index ^ 1);
    	  return;
      }
  }

  
  private void spreadScores(int depth, Node root) {
	  boolean maximize;
	  Vector childparent = new Vector();
	  int i;
	  Node leaf_parent;
	  
	  if (depth > 1){
	      find_leaf_parents(root, childparent);
	      if (depth % 2 == 1){
	    	  maximize = true; // bottom level maximization level
	      } else {
	    	  maximize = false; // bottom level minimization level
	      }
	      for (i = 0; i < childparent.size(); i++) {
	    	  if (running == false) {
	        	  return;
	          }
	    	  leaf_parent = (Node) childparent.elementAt(i);
	    	  if (leaf_parent != null){
	    		  leaf_parent.propagateScore(maximize);
	    	  }
	      }
	  }
  }
  
  
  private void find_leaf_parents(Node noodi, Vector childparent) // This function will find nodes to propagate score
  {
	  if (running == false) {
		  return;
	  }
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
  
  
  private double calc_scores(Node node) {
	// Calculates score for a given node
	int factor = 100;
	int my_marks, opp_marks, total_marks;
	double score = 0;
	
	GameState field = node.getState();
	
	my_marks = field.getMarkCount(myIndex);
	opp_marks = field.getMarkCount(oppIndex);
	total_marks = my_marks + opp_marks;
	
	// 1. Mark count evaluation
	// score += mark_evaluation(my_marks, opp_marks, factor);
	
	// 2. Move count evaluation
	// score += move_evaluation(field, factor);
	
	// 3. Frontier disk evaluation
	// score += frontier_evaluation(field, factor);
	
	// 4. Corner evaluation
	// score += corner_evaluation(field, factor);
	
	// 5. Static evaluation
	// score += static_evaluation(field);
	
	if (total_marks < 25) {
		// Early game
		score += move_evaluation(field, factor);				//2
		score += frontier_evaluation(field, factor);			//3
		score += corner_evaluation(field, factor);				//4
		score += static_evaluation(field);						//5
	} else if (total_marks < 50){
		// Mid game
		score += mark_evaluation(my_marks, opp_marks, factor);	//1
		score += move_evaluation(field, factor);				//2
		score += frontier_evaluation(field, factor);			//3
		score += corner_evaluation(field, factor);				//4
		score += static_evaluation(field);						//5
	} else {
		// End game
		score += mark_evaluation(my_marks, opp_marks, factor);	//1
		score += corner_evaluation(field, factor);				//4
		score += static_evaluation(field);						//5
	}
	
	return score;
  }
  
  
  private double mark_evaluation(double my_marks, double opp_marks, int factor) {
	  // 1. Mark count evaluation
	  double mark_score;
	  
	  mark_score = (factor * ((my_marks - opp_marks) / (my_marks + opp_marks)));
	  
	  return mark_score;
  }

  
  private double move_evaluation(GameState field, int factor) {
	  // 2. Move count evaluation
	  double my_moves, opp_moves, move_score = 0;
	  
	  my_moves = field.getPossibleMoveCount(myIndex);
	  opp_moves = field.getPossibleMoveCount(oppIndex);
	  if ((my_moves + opp_moves) != 0){
		  move_score = (factor * ((my_moves - opp_moves) / (my_moves + opp_moves)));
	  }
	  
	  return move_score;
  }
  
  
  private double frontier_evaluation(GameState field, int factor) {
		// Goes trough all disks on the field and calculates frontier disks score
		int my_frontier = 0, opp_frontier = 0, frontier_score = 0;
		int x, y, mark;
		
		// We check every square for frontier disks
		for (x = 0; x < 8; x++) {
			for (y = 0; y < 8; y++){
				mark = field.getMarkAt(x, y);
				if (mark == -1) { // Here is empty square
					continue; 
				} else { // Here we have disk
					if (isFrontier(x, y, field)){
						if (mark == myIndex) {
							my_frontier += 1;
						} else {
							opp_frontier += 1;
						}
					}
				}
			}
		}
		if ((opp_frontier + my_frontier) != 0){
			frontier_score = (factor * ((opp_frontier - my_frontier) 
									  / (opp_frontier + my_frontier)));
		}
		
		return frontier_score;
	}

  
  boolean isFrontier(int x, int y, GameState field){
	  // Checks if the given disk is frontier disk
	  int i, k;
	  int downx = -1, downy = -1;
	  int upx = 1, upy = 1;
	  
	  // Check edges
	  if (x == 0) {
		  downx = 0;
	  } else if (x == 7) {
		  upx = 0;
	  }
	  if (y == 0) {
		  downy = 0;
	  } else if (y == 7) {
		  upy = 0;
	  }
	  
	  // Check surrounding disks
	  for (i = downx; i < upx; i++) {
			for (k = downy; k < upy; k++) {
				if (field.getMarkAt(x+i, y+k) == -1) {
					return true;
				}
			}
		}
	  
	  return false;
  }
  
  
  private double corner_evaluation(GameState field, int factor) {
	// Calculates corner points
	double my_corner = 0, opp_corner = 0, corner_points = 0;
	int i;
	double[] corners = new double[4];
	
	corners[0] = field.getMarkAt(0, 0);
	corners[1] = field.getMarkAt(0, 7);
	corners[2] = field.getMarkAt(7, 0);
	corners[3] = field.getMarkAt(7, 7);
	
	for (i = 0; i < 4; i++) {
		if (corners[i] == myIndex) {
			my_corner += 1;
		} else if (corners[i] == oppIndex) {
			opp_corner += 1;
		}	
	}

	if ((my_corner + opp_corner) != 0){
		corner_points = (factor * ((my_corner - opp_corner) / (my_corner + opp_corner)));
	}
	
	return corner_points;
}

  
  private int static_evaluation(GameState field){
	int mark;
	int x, y;
	int static_score = 0;
	
	// Static field evaluation
	for (x = 0; x < 8; x++) {
		for (y = 0; y < 8; y++){
			mark = field.getMarkAt(x, y);
			if (mark == myIndex) {
				static_score += gameboard[y][x];
			} else if (mark == oppIndex) {
				static_score -= gameboard[y][x];
			}
		}
	}
	
	return static_score;
  }
  
  
  private int stableDisk(GameState field) {
	  // Check stable Disks
	  int x, y;
	  int my_stable = 0, opp_stable = 0;
	  int mark;
	  int cornerIndex;
	  int xbound;
	  
	  // Start from upper corner
	  y = 0;
	  for (x = 0; x < 8; x++) {
		  xbound = x; // save coordinate
		  mark = field.getMarkAt(x, y);
		  if (x == 0){
			  if (mark != -1){
				  cornerIndex = mark;
			  } else { // Empty
				  break;
			  }
		  }
		  
		  // Empty
		  if (mark == -1) {
			  break;
		  } else if (mark != cornerIndex) { // not corner owner mark
			  continue;
		  }
		  // Points for correct player
		  if (cornerIndex == myIndex) {
			  my_stable += 1;
		  } else {
			  opp_stable += 1;
		  }
	  }
	
	  return;
  }
  
  
  
  void printTree(Node noodi, int dep, int mode, int dep_limit) // This function will print the tree
  {
	  String field;
	  String move_string;
	  Vector childit = noodi.getChildren(); // Take children
	  if (childit == null) {
		  return;
	  }
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
			  if (dep < dep_limit) {
				  printTree(child, dep + 1, mode, dep_limit); // Has to check if more Node child has more childs
			  }
		  }
	  }
  	}
}
  