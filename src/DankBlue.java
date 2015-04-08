  import reversi.*;

import java.util.Vector;

  public class DankBlue implements ReversiAlgorithm
  {
      // Constants
  private final static int DEPTH_LIMIT = 6; // Just an example value.

  // Variables
  boolean initialized;
  volatile boolean running; // Note: volatile for synchronization issues.
  GameController controller;
  GameState initialState;
  int myIndex;
  Move selectedMove;

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
          Move newMove = searchToDepth(currentDepth++);
          
          // Check that there's a new move available.
          if (newMove != null)
              selectedMove = newMove;
      }
  
      if (running) // Make a move if there is still time left.
      {
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

      Move optimalMove;
      Vector moves = initialState.getPossibleMoves(myIndex);
      Node root = new Node(initialState, null); //create root node
      Vector nodes_list = new Vector();
      
      nodes_list.add(root);
      // Create child nodes for tree to given depth
      createTree(1, depth, nodes_list, myIndex);
      
      root.print();
      
      if (moves.size() > 0)
          optimalMove = (Move)moves.elementAt(0); // Any movement that just happens to be first.
      	  else
              optimalMove = null;
          
          return optimalMove;
      }
  
  
  void createTree(int depth, int depth_lim, Vector nodes, int pl_index) 
  {
	  int i = 0;
	  Vector children = new vector();
	  Node node;
	  
	  while (!nodes.isEmpty()) {
		  node = (Node)nodes.elementAt(0);
		  Vector moves = node.getState().getPossibleMoves(pl_index);
	      for (i = 0; i < moves.size(); i = i + 1) {
	    	  Move game_move = (Move)moves.elementAt(i);
	    	  GameState new_state = node.getState().getNewInstance(game_move);
	    	  Node child = new Node(new_state, game_move);
	    	  node.addChild(child);
	    	  children.add(child);
	      }
	      nodes.removeElementAt(0);
	  }
      
      if (depth < depth_lim) {
    	  pl_index++;
      	  pl_index = pl_index % 2;
    	  createTree(depth++, depth_lim, children, pl_index);
    	  return;
       }
   	}
  
  }