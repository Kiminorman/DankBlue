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
  Vector stable = new Vector();
  double[][] gameboard = new double[][]{
		  {  50, -15,  5, 3, 3,  5, -15,  50},
		  { -15, -20,  3, 0, 0,  3, -20, -15},
		  {   5,   3,  0, 0, 0,  0,   3,   5},
		  {   3,   0,  0, 0, 0,  0,   0,   3},
		  {   3,   0,  0, 0, 0,  0,   0,   3},
		  {   5,   3,  0, 0, 0,  0,   3,   5},
		  { -15, -20,  3, 0, 0,  3, -20, -15},
		  {  50, -15,  5, 3, 3,  5, -15,  50}
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
      // - Create the tree of depth d (breadth first, depth first, beam search, alpha beta pruning, ...)
      // - Evaluate the leaf nodes
      // - If you think normal minimax search is enough, call the propagateScore method of the parent node
      //   of each leaf node
      // - Call the getOptimalChild method of the root node
      // - Return the move in the optimal child of the root node
      // - Don't forget the time constraint! -> Stop the algorithm when variable "running" becomes "false"
      //   or when you have reached the maximum search depth.
	  
	  int i = 0;
	  Node leaf_parent;
	  Node optimalNode;
      Move optimalMove;
      boolean maximize;
      Vector childparent = new Vector();
      Vector moves = initialState.getPossibleMoves(myIndex);
      
      // Takes always corner
      for (i = 0; i < moves.size(); i++) {
    	 Move move = (Move) moves.elementAt(i);
    	 if (((move.getX() == 0) || (move.getX() == 7)) &&
    	     ((move.getY() == 0) || (move.getY() == 7))) {
    		 return move;
    	 }
      }
      
      //create root node
      Node root = new Node(initialState, null);
      Vector nodes_list = new Vector();
      nodes_list.add(root);
      
      // Create child nodes for tree to given depth
      createTree(0, depth, nodes_list, myIndex);
      
      // Propagate score
      if (depth > 1){
	      find_leaf_parents(root, childparent);
	      if (depth % 2 == 1){
	    	  maximize = true; // bottom level maximization level
	      } else {
	    	  maximize = false; // bottom level minimization level
	      }
	      for (i = 0; i < childparent.size(); i++) {
	    	  if (running == false) {
	        	  return null;
	          }
	    	  leaf_parent = (Node) childparent.elementAt(i);
	    	  if (leaf_parent != null){
	    		  leaf_parent.propagateScore(maximize);
	    	  }
	      }
      }
      
      if (depth == 4) {
    	  printTree(root, 0, 0, 2);
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
    	  System.out.println("No moves to do!");
    	  optimalMove = null;
      }
      return optimalMove;
  }
  

  void createTree(int depth, int depth_lim, Vector nodes, int pl_index) 
  {
	  // Makes tree with breadth first search to depth depth_lim
	  int i = 0;
	  int counter = 0;
	  Vector children = new Vector();
	  Node node;
	  
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
      if (depth < depth_lim) {
    	  pl_index++;
      	  pl_index = pl_index % 2;
    	  createTree(depth + 1, depth_lim, children, pl_index);
    	  return;
      }
  }

  
  void find_leaf_parents(Node noodi, Vector childparent) // This function will find nodes to propagate score
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
	
	if (total_marks < 25) {
		// Early game
		score += move_evaluation(field, factor);				//2
		//score += frontier_evaluation(field, factor);			//3
		score += corner_evaluation(field, factor);				//4
	} else if (total_marks < 50){
		// Mid game
		score += mark_evaluation(my_marks, opp_marks, factor);	//1
		score += move_evaluation(field, factor);				//2
		//score += frontier_evaluation(field, factor);			//3
		score += corner_evaluation(field, factor);				//4
	} else {
		// End game
		score += mark_evaluation(my_marks, opp_marks, factor);	//1
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
			} else if (mark == -1) {
				continue;
			} else {
				static_score -= gameboard[y][x];
			}
		}
	}
	
	return static_score;
}
  
  
  boolean stableDisk(int x, int y, GameState field) {
	//Check if already stable
	////////////////////////////////////////// EI tomi stable vektori kusee ja pahasti
	/*if (stable.contains(new Slot(x,y))) {
		return true;
	}
	
	Slot check1, check2;

	//left -> right
	check1 = new Slot((x-1), y);
	check2 = new Slot((x+1), y);
	if (!((x == 0) || (x == 7) || stable.contains(check1) || stable.contains(check2))) {
		return false;
	}
	// upleft -> downrigth
	check1 = new Slot((x-1), (y+1));
	check2 = new Slot((x+1), (y-1));
	if (!((x == 0) || (x == 7) || (y == 0) || (y == 7) || stable.contains(check1) || stable.contains(check2))) {
		return false;
	}
	// up -> down
	check1 = new Slot(x, (y+1));
	check2 = new Slot(x, (y-1));
	if (!((y == 0) || (y== 7) || stable.contains(check1) || stable.contains(check2))) {
		return false;
	}
	// downleft -> uprigth
	check1 = new Slot((x+1), (y-1));
	check2 = new Slot((x-1), (y+1));
	if (!((x == 0) || (x == 7) || (y == 0) || (y == 7) || stable.contains(check1) || stable.contains(check2))) {
		return false;
	}
	
	// Stable disk
	stable.add(new Slot(x,y));
	*/
	return true;
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
  