import java.io.*;
import java.util.*;



public class App {

  static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

  static Map<Integer, List<Integer>> graph = new TreeMap<Integer, List<Integer>>();

  static Map<Pair<Integer, Integer>, ShortestPath> shortestPaths = new HashMap<Pair<Integer, Integer>, ShortestPath>();
  static Map<Integer, List<ShortestPath>> rows = new HashMap<Integer, List<ShortestPath>>();
  static Map<Integer, List<ShortestPath>> columns = new HashMap<Integer, List<ShortestPath>>();

  static Map<Pair<Integer, Integer>, List<ShortestPath>> pathIndex = new HashMap<Pair<Integer, Integer>, List<ShortestPath>>();


  private static Map<Integer, List<Integer>> readGraph(BufferedReader in) {

    try {
      String s;
      while (!"S".equals(s = in.readLine())) {

        String[] elements = s.split(" ");
        int[] edge = new int[]{Integer.parseInt(elements[0]), Integer.parseInt(elements[1])};

        List<Integer> list; 
        if(graph.containsKey(edge[0]))
          list = graph.get(edge[0]);
        else
          list = new ArrayList<Integer>();
        list.add(edge[1]);
        graph.put(edge[0], list);

      }
    } catch(Exception e) {
      e.printStackTrace();
    }
    return graph;
  }

  private static void populateShortestPathTable() {
    /* non-recursive
    for(Map.Entry<Integer, List<Integer>> entry : graph.entrySet()) {    
      int origNode = entry.getKey();           
      Queue queue = new LinkedList<Integer>();
      queue.add(origNode);
      int distance = 0; 
      while(!queue.isEmpty()) {
        int node = queue.remove();

        List<Intger, Integer> destNodes; 
        if(graph.containsKey(node)) {
          destNodes = graph.get(node);
        } else {
          continue;
        }
        distance += 1; 
        for(int destNode : destNodes) {       
          Map<Integer, Integer> path = new HashMap<Integer, Integer>();        
          shortestPaths.put(new Pair(orig, destNode), new ShortestPath(  , path));
          queue.add(destNode);
        }
      }
    }
    */


    for(Integer node : graph.keySet())
      searchShortestPath(node, node, new HashMap<Integer, Integer>(), 0);
  
  }


  private static void addPathIndexEntries(ShortestPath shortestPath) {

    Map<Integer, Integer> path = shortestPath.path;
    for(Integer orig : path.keySet()) {
      int n = orig;
      while(true) {
        if(!path.containsKey(n))
          break;
        int dest = path.get(n);
        Pair<Integer, Integer> p = new Pair<Integer, Integer>(orig, dest);
        List<ShortestPath> list;
        if(pathIndex.containsKey(p))
          list = pathIndex.get(p);
        else
          list = new ArrayList<ShortestPath>();
        list.add(shortestPath);
        pathIndex.put(p, list);
        n = dest;
      }
    }

  }

  private static void updateRowsAndColumns(ShortestPath shortestPath) {
    // update rows
    List<ShortestPath> row;
    if(rows.containsKey(shortestPath.head))
      row = rows.get(shortestPath.head);
    else
      row = new ArrayList<ShortestPath>();
    row.add(shortestPath);
    rows.put(shortestPath.head, row);

    // update columns
    List<ShortestPath> column;
    if(columns.containsKey(shortestPath.last))
      column = columns.get(shortestPath.last);
    else
      column = new ArrayList<ShortestPath>();
    column.add(shortestPath);
    columns.put(shortestPath.last, column);

  }

  private static void searchShortestPath(int origNode, int node, Map<Integer, Integer> path, int distance) {

    List<Integer> destNodes; 
    if(graph.containsKey(node))
      destNodes = graph.get(node);
    else    
      return;    

    distance += 1;
    for(int destNode : destNodes) {
      if(path.containsKey(destNode))
        continue;
     
      Pair<Integer, Integer> pair = new Pair<Integer, Integer>(origNode, destNode);
      Map<Integer, Integer> newPath = new HashMap<Integer, Integer>(path);
      if(!shortestPaths.containsKey(pair) || shortestPaths.get(pair).value > distance) {        
        newPath.put(node, destNode);
        ShortestPath shortestPath = new ShortestPath(pair.left, pair.right, distance, newPath);
        shortestPaths.put(pair, shortestPath);

        updateRowsAndColumns(shortestPath);

        addPathIndexEntries(shortestPath);
      }

      searchShortestPath(origNode, destNode, newPath, distance);
    }
  }

  
  private static int processQuery(Pair<Integer, Integer> pair) {
    if(pair.left == pair.right)
      return 0;
    ShortestPath sp = shortestPaths.get(pair);
    return sp == null ? -1 : sp.value;
  }

  private static void processAdd(Pair<Integer, Integer> pair) {

    // check if edge already exists   
    if(shortestPaths.containsKey(pair) && shortestPaths.get(pair).value == 1) {
      return;
    }

    // if pathIndex contains pair it means that both nodes already exist and that pair represents a shortcut
    if(pathIndex.containsKey(pair)) { // this should check for dirty nodes

      List<ShortestPath> list = pathIndex.get(pair);

      for(ShortestPath sp : list) {
        Map<Integer, Integer> path = sp.path;

        int node = path.get(pair.left);
        path.put(pair.left, pair.right);
	    int count = 0;
        while(node != pair.right) {
          count++;
          node = path.remove(node);
        }
        sp.value -= count; 
      }

      // add to graph
      graph.get(pair.left).add(pair.right);

    } else {

      // add new edge to shortest paths
      Map<Integer, Integer> path = new HashMap<Integer, Integer>();
      path.put(pair.left, pair.right);
      ShortestPath newSp = new ShortestPath(pair.left, pair.right, 1, path);

      List<ShortestPath> piList = new ArrayList<ShortestPath>();
      piList.add(newSp);

      shortestPaths.put(new Pair<Integer, Integer>(pair.left, pair.right), newSp) ;
      pathIndex.put(new Pair<Integer, Integer>(pair.left, pair.right), piList);

      updateRowsAndColumns(newSp);

      // if right node exists
      if(graph.containsKey(pair.right)) {

        // copy dest row
        if(rows.containsKey(pair.right)) {
          List<ShortestPath> list = rows.get(pair.right);
          for (ShortestPath sp : list) {
            if (!graph.containsKey(pair.left) || (!sp.path.containsKey(pair.left) && sp.last != pair.left)) {
              Map<Integer, Integer> newPath = new HashMap<Integer, Integer>(sp.path);
              newPath.put(pair.left, pair.right);
              ShortestPath newShortestPath = new ShortestPath(pair.left, sp.last, sp.value + 1, newPath);
              shortestPaths.put(new Pair<Integer, Integer>(pair.left, sp.last), newShortestPath);

              // update path index
              addPathIndexEntries(newShortestPath);

              updateRowsAndColumns(newShortestPath);
            }
          }
        }

        // update graph
        if(!graph.containsKey(pair.left)) {
          List<Integer> list = new ArrayList<Integer>();
          list.add(pair.right);
          graph.put(pair.left, list);
        }

      } else if(graph.containsKey(pair.left)) {

        // copy origin column
        if(columns.containsKey(pair.left)) {
          List<ShortestPath> list = columns.get(pair.left);
          for(ShortestPath sp : list) {
            Map<Integer, Integer> newPath = new HashMap<Integer, Integer>(sp.path);
            newPath.put(pair.left, pair.right);
            ShortestPath newShortestPath = new ShortestPath(sp.head, pair.right, sp.value + 1, newPath);
            shortestPaths.put(new Pair<Integer, Integer>(sp.head, pair.right), newShortestPath);

            // update path index
            addPathIndexEntries(newShortestPath);

            updateRowsAndColumns(newShortestPath);
          }
        }

        // update graph
        List<Integer> list;
        if(graph.containsKey(pair.left))
          list = graph.get(pair.left);
        else
          list = new ArrayList<Integer>();
        list.add(pair.right);
        graph.put(pair.left, list);

      } else {
        // update graph in case both nodes do not exist
        List<Integer> list = new ArrayList<Integer>();
        list.add(pair.right);
        graph.put(pair.left, list);
      }

    }



/*
    // check if both edge's nodes do not exist
    if(!graph.containsKey(pair.left) && !graph.containsKey(pair.right)) {

      // insert in shortestPaths

      shortestPaths.put();
     
      // insert in path index

    }

    if(!graph.containsKey(pair.left) && graph.containsKey(pair.right) || !pathIndex.containsKey(pair)) {

    }


    if(graph.containsKey(pair.left) && !graph.containsKey(pair.right)) {


    }
*/
  }

  private static void processDelete() {

  }

  private static void readBatches()  {

    try {
      String s;
      while ((s = in.readLine()) != null && s.length() != 0) {

        char opCode = s.charAt(0);
        switch(opCode) {
          case 'Q':
            String[] elements = s.substring(2).split(" ");
            int result = processQuery(new Pair<Integer, Integer>(Integer.parseInt(elements[0]), Integer.parseInt(elements[1])));
            System.out.println(result);
            break;
          case 'A':
            elements = s.substring(2).split(" ");
            processAdd(new Pair<Integer, Integer>(Integer.parseInt(elements[0]), Integer.parseInt(elements[1])));
            break;
          case 'D':
        }

      }
    } catch(Exception e) {
      e.printStackTrace();
    } 
  }


  public static void main(String[] args) {

    readGraph(in);

    // debug
    for(Map.Entry<Integer, List<Integer>> entry : graph.entrySet()) {
      System.out.print(entry.getKey() + " : ");
      for(Integer i : entry.getValue()) {
        System.out.print(i + ",");
      }
      System.out.println();
    }

    populateShortestPathTable();

    // debug
    for(Map.Entry<Pair<Integer, Integer>,ShortestPath> entry : shortestPaths.entrySet())
      System.out.println("key: " + entry.getKey() + " :: value: " + entry.getValue());

    readBatches();
  }


  private static class ShortestPath {
    int head, last;
    int value;
    Map<Integer, Integer> path = new HashMap<Integer, Integer>();

    public ShortestPath(int head, int last, int value, Map<Integer, Integer> path) {
      this.head = head;
      this.last = last;
      this.value = value;
      this.path = path;
    }

    @Override
    public String toString() {

      StringBuilder sb = new StringBuilder();
      for(Map.Entry<Integer,Integer> entry : path.entrySet()) {
        sb.append(entry.getKey() + " : " + entry.getValue() + "\n");    
      }

      return "size: " + value + ", path: " + sb.toString();
    }
  }


  private static class Pair<L, R> {
    L left;
    R right;

    Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pair< ? , ? >) {
            Pair< ? , ? > pair = (Pair< ? , ? >)obj;
            return left.equals(pair.left) && right.equals(pair.right);
        }
        return false;
    }

    @Override
    public int hashCode() {
      return Objects.hash(left, right);
    }

    @Override
    public String toString() {
        return "Pair " + Integer.toHexString(hashCode()) + ": (" + left.toString() + ", " + right.toString() + ")";
    }
  }
}
