package submit;

import java.util.HashSet;
import java.util.List;

import graph.FindState;
import graph.Finder;
import graph.FleeState;
import graph.Node;
import graph.NodeStatus;

/** A solution with find-the-Orb optimized and flee getting out as fast as possible. */
public class Pollack extends Finder {

    // field used to stores nodes with gold
    Heap<Node> goldNode;

    /** Get to the orb in as few steps as possible. <br>
     * Once you get there, you must return from the function in order to pick it up. <br>
     * If you continue to move after finding the orb rather than returning, it will not count.<br>
     * If you return from this function while not standing on top of the orb, it will count as <br>
     * a failure.
     *
     * There is no limit to how many steps you can take, but you will receive<br>
     * a score bonus multiplier for finding the orb in fewer steps.
     *
     * At every step, you know only your current tile's ID and the ID of all<br>
     * open neighbor tiles, as well as the distance to the orb at each of <br>
     * these tiles (ignoring walls and obstacles).
     *
     * In order to get information about the current state, use functions<br>
     * currentLoc(), neighbors(), and distanceToOrb() in FindState.<br>
     * You know you are standing on the orb when distanceToOrb() is 0.
     *
     * Use function moveTo(long id) in FindState to move to a neighboring<br>
     * tile by its ID. Doing this will change state to reflect your new position.
     *
     * A suggested first implementation that will always find the orb, but <br>
     * likely won't receive a large bonus multiplier, is a depth-first search. <br>
     * Some modification is necessary to make the search better, in general. */
    @Override
    public void find(FindState state) {
        // TODO 1: Walk to the orb
        HashSet<Long> visited= new HashSet<>();

        OPdfsWalk(state, visited);
    }

    /** The walker is standing on standingOn given by FindState state.<br>
     * Visit every node reachable along paths of unvisited id from standingOn. <br>
     * When an id is visited, record the id in HashSet visited.<br>
     * If the orb is found, end with walker standing at the orb. <br>
     * If the orb is not found, go back to the previous node till there is an unvisited neighbor.
     * <br>
     * Precondition: standingOn is unvisited. */
    public static void dfsWalk(FindState state, HashSet<Long> visited) {
        // get the current location and mark it visited
        long standingOn= state.currentLoc();
        visited.add(standingOn);

        long wId;
        // visit each neighbor w of standingOn if not visited
        for (NodeStatus w : state.neighbors()) {
            wId= w.getId();
            if (state.distanceToOrb() != 0 && !visited.contains(wId)) {
                state.moveTo(wId);
                dfsWalk(state, visited);
                if (state.distanceToOrb() != 0)
                    state.moveTo(standingOn);
            }
        }
    }

    /** Optimized dfs walk <br>
     * The walker is standing on standingOn given by FindState state.<br>
     * Visit every unvisited and reachable node along paths from standingOn, <br>
     * with the order of going to id closest to orb first.<br>
     * When an id is visited, record the id in HashSet visited.<br>
     * If the orb is found, end with walker standing at the orb.<br>
     * If the orb is not found, go back to the previous node till there is an unvisited neighbor.
     * <br>
     * Precondition: standingOn is unvisited. */
    public static void OPdfsWalk(FindState state, HashSet<Long> visited) {
        // get the current location and mark it visited
        long standingOn= state.currentLoc();
        visited.add(standingOn);

        // for each neighbor of the standingon,
        // insert the id and distance to orb into a min heap
        Heap<NodeStatus> minDistanceId= new Heap<>(true);
        Integer wDis;
        for (NodeStatus w : state.neighbors()) {
            wDis= w.getDistanceToTarget();
            minDistanceId.insert(w, wDis);
        }

        // visit each neighbor w of standingOn if not visited
        // if the orb is found, stand on the orb and don't move anymore
        long wId;
        while (minDistanceId.size() != 0) {
            wId= minDistanceId.poll().getId();
            if (state.distanceToOrb() != 0 && !visited.contains(wId)) {
                state.moveTo(wId);
                OPdfsWalk(state, visited);
                if (state.distanceToOrb() != 0)
                    state.moveTo(standingOn);
            }
        }
    }

    /** Get out the cavern before the ceiling collapses, trying to collect as <br>
     * much gold as possible along the way. Your solution must ALWAYS get out <br>
     * before steps runs out, and this should be prioritized above collecting gold.
     *
     * You now have access to the entire underlying graph, which can be accessed <br>
     * through FleeState state. <br>
     * currentNode() and exit() will return Node objects of interest, and <br>
     * allsNodes() will return a collection of all nodes on the graph.
     *
     * Note that the cavern will collapse in the number of steps given by <br>
     * stepsLeft(), and for each step this number is decremented by the <br>
     * weight of the edge taken. <br>
     * Use stepsLeft() to get the steps still remaining, and <br>
     * moveTo() to move to a destination node adjacent to your current node.
     *
     * You must return from this function while standing at the exit. <br>
     * Failing to do so before steps runs out or returning from the wrong <br>
     * location will be considered a failed run.
     *
     * You will always have enough steps to flee using the shortest path from the <br>
     * starting position to the exit, although this will not collect much gold. <br>
     * For this reason, using Dijkstra's to plot the shortest path to the exit <br>
     * is a good starting solution
     *
     * Here's another hint. Whatever you do you will need to traverse a given path. It makes sense
     * to write a method to do this, perhaps with this specification:
     *
     * // Traverse the nodes in moveOut sequentially, starting at the node<br>
     * // pertaining to state <br>
     * // public void moveAlong(FleeState state, List<Node> moveOut) */
    @Override
    public void flee(FleeState state) {
        // TODO 2. Get out of the cavern in time, picking up as much gold as possible.

        // directToExit(state);
        // toTarget(state, state.exit());
        // grabClosestGold(state);
        // grabMostGold(state);
        grabCombine(state);
    }

    /** use the shortestpath to go directly from the node defined by state to the exit,<br>
     * don't care about the gold. end with walker standing at the exit. */
    public void directToExit(FleeState state) {
        Node start= state.currentNode();
        Node end= state.exit();
        List<Node> routeNode= Path.shortestPath(start, end);
        for (int i= 1; i < routeNode.size(); i++ ) state.moveTo(routeNode.get(i));
    }

    /** use the shortestpath to go directly from the node defined by state <br>
     * to the targetNode, don't care about the gold. end with walker standing at the target. */
    public void toTarget(FleeState state, Node target) {
        Node start= state.currentNode();
        List<Node> routeNode= Path.shortestPath(start, target);
        for (int i= 1; i < routeNode.size(); i++ ) state.moveTo(routeNode.get(i));
    }

    /** return the number of steps required to follow the shortest path from node start to node
     * end. */
    public int stepUsedtoTarget(Node start, Node target) {
        List<Node> route= Path.shortestPath(start, target);
        int step= Path.pathSum(route);
        return step;
    }

    /** In the min heap, nodes with gold on the map are inserted <br>
     * based on the steps taken from start to that gold Node.<br>
     * The closest node that has gold sits at the top. */
    public Heap<Node> closestGoldNode(Node start, FleeState state) {
        Heap<Node> goldNode= new Heap<>(true);
        for (Node tileNode : state.allNodes()) if (tileNode.getTile().gold() != 0) {
            List<Node> routeGoldNode= Path.shortestPath(start, tileNode);
            Integer step= Path.pathSum(routeGoldNode);
            goldNode.insert(tileNode, step);
        }
        return goldNode;
    }

    /** In the max heap, nodes with gold on the map are inserted <br>
     * based on the amount of gold at that gold Node.<br>
     * The node with most gold sits at the top. */
    public Heap<Node> mostGoldNode(Node start, FleeState state) {
        Heap<Node> goldNode= new Heap<>(false);
        for (Node tileNode : state.allNodes()) if (tileNode.getTile().gold() != 0) {
            Integer amount= tileNode.getTile().gold();
            goldNode.insert(tileNode, amount);
        }
        return goldNode;
    }

    /** This method is a mix of closest node with gold and node with most gold. In the max heap,
     * nodes with gold on the map are inserted <br>
     * based on the amount of gold at divide by number of steps taken to that node. *<br>
     * The node with most "value" sits at the top. */
    public Heap<Node> factorGoldNode(Node start, FleeState state) {
        Heap<Node> goldNode= new Heap<>(false);
        for (Node tileNode : state.allNodes()) if (tileNode.getTile().gold() != 0) {
            List<Node> routeGoldNode= Path.shortestPath(start, tileNode);
            Integer step= Path.pathSum(routeGoldNode);

            float factor= tileNode.getTile().gold() / step;
            goldNode.insert(tileNode, factor);
        }
        return goldNode;
    }

    /** The walker starts from the node defined by the state. If steps left are enough to go from
     * current location defined by state <br>
     * to the closest goldNode and then to exit, go and grab the gold.<br>
     * Otherwise go directly to the exit. Once the closest gold is found, the walker goes directly
     * to that node, regardless of any adjacent nodes with gold along its way. <br>
     * The walker ends with standing at the node with gold. */
    public void grabClosestGold(FleeState state) {
        Node currentNode= state.currentNode();
        Node exit= state.exit();

        goldNode= closestGoldNode(state.currentNode(), state);
        if (goldNode.size != 0) {
            Node nextNode= goldNode.poll();
            int heretoNext= stepUsedtoTarget(currentNode, nextNode);
            int nextToExit= stepUsedtoTarget(nextNode, exit);

            if (heretoNext + nextToExit <= state.stepsLeft()) {
                toTarget(state, nextNode);
                grabClosestGold(state);
            } else toTarget(state, exit);
        } else toTarget(state, exit);
    }

    /** The walker starts from the node defined by the state. If steps left are enough to go from
     * current location defined by state <br>
     * to the Node with most gold and then to exit, go and grab the gold.<br>
     * Otherwise go directly to the exit. The walker ends with standing at the node with gold. */
    public void grabMostGold(FleeState state) {
        Node currentNode= state.currentNode();
        Node exit= state.exit();

        goldNode= mostGoldNode(state.currentNode(), state);
        if (goldNode.size != 0) {
            Node nextNode= goldNode.poll();
            int heretoNext= stepUsedtoTarget(currentNode, nextNode);
            int nextToExit= stepUsedtoTarget(nextNode, exit);

            if (heretoNext + nextToExit <= state.stepsLeft()) {
                toTarget(state, nextNode);
                grabMostGold(state);
            } else grabClosestGold(state);
        } else toTarget(state, exit);
    }

    /** The walker starts from the node defined by the state. If steps left are enough to go from
     * current location defined by state <br>
     * to the Node with maximum of (most gold divide by steps needed)<br>
     * and then to exit, go and grab the gold.<br>
     * Otherwise go directly to the exit. Once the most valuable node is found, the walker goes
     * directly to that node. <br>
     * The walker ends with standing at the node with gold. */
    public void grabCombine(FleeState state) {
        Node currentNode= state.currentNode();
        Node exit= state.exit();
        goldNode= factorGoldNode(state.currentNode(), state);

        if (goldNode.size != 0) {
            Node nextNode= goldNode.poll();
            int heretoNext= stepUsedtoTarget(currentNode, nextNode);
            int nextToExit= stepUsedtoTarget(nextNode, exit);
            if (heretoNext + nextToExit <= state.stepsLeft()) {
                toTarget(state, nextNode);
                grabCombine(state);
            } else grabClosestGold(state);
        } else toTarget(state, exit);
    }
}
