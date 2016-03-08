import java.io.*;
import java.util.*;



public class App {

  private static Map<Integer, List<Integer>> readGraph(BufferedReader in) {
    Map<Integer, List<Integer>> graph = new TreeMap<Integer, List<Integer>>(); 
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

  private static void populateShortestPathTable(Map<Integer, List<Integer>> graph, Map<Pair<Integer, Integer>, ShortestPath> shortestPaths, Map<Integer, List<ShortestPath>> rows, Map<Integer, List<ShortestPath>> columns, Map<Pair<Integer, Integer>, List<ShortestPath>> pathIndex) {
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
      searchShortestPath(graph, shortestPaths, pathIndex, node, node, new HashMap<Integer, Integer>(), 0);    
  
  }


  private static void addPathIndexEntries(Map<Pair<Integer, Integer>, List<ShortestPath>> pathIndex, Map<Integer, Integer> path) {

    for(Integer orig : path.keySet()) {
      int n = orig;
      while(true) {
        if(!newPath.containsKey(n))
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


  private static void searchShortestPath(Map<Integer, List<Integer>> graph, Map<Pair<Integer, Integer>, ShortestPath> shortestPaths, Map<Integer, List<ShortestPath>> rows, Map<Integer, List<ShortestPath>> columns, Map<Pair<Integer, Integer>, List<ShortestPath>> pathIndex, int origNode, int node, Map<Integer, Integer> path, int distance) {

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
        ShortestPath shortestPath = new ShortestPath(distance, newPath);
        shortestPaths.put(pair, shortestPath);

        addPathIndexEntries(pathIndex, newPath);
      }

      searchShortestPath(graph, shortestPaths, rows, columns, pathIndex, origNode, destNode, newPath, distance); 
    }
  }

  
  private static int processQuery(Map<Pair<Integer, Integer>, ShortestPath> shortestPaths, Pair<Integer, Integer> pair) {
    if(pair.left == pair.right)
      return 0;
    ShortestPath sp = shortestPaths.get(pair);
    return sp == null ? -1 : sp.value;
  }

  private static void processAdd(Map<Integer, List<Integer>> graph, Map<Pair<Integer, Integer>, ShortestPath> shortestPaths, Map<Integer, List<ShortestPath>> rows,  Map<Integer, List<ShortestPath>> columns, Map<Pair<Integer, Integer>, List<ShortestPath>> pathIndex, Pair<Integer, Integer> pair) {

    // check if edge already exists   
    if(shortestPaths.containsKey(pair) && shortestPaths.get(pair).value == 1) {
      return;
    }

    // if pathIndex contains pair it means that both vertices already exist and that pair represents a shortcut
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

      // if both vertices already exist
      if(graph.containsKey(pair.left) && graph.containsKey(pair.right)) {

        // add new edge to shortest paths
        Map<Integer, Integer> path = new HashMap<Integer, Integer>();
        path.put(pair.left, pair.right);
        ShortestPath newSp = new ShortestPath(1, path);

        List<ShortestNode> piList = new ArrayList<ShortestNode>();
        piList.add(newSp);

        shortestPaths.put(new Pair<Integer, Integer>(pair.left, pair.right), newSp) ;
        pathIndex.put(new Pair<Integer, Integer>(pair.left, pair.right), piList);

        // copy dest row
        List<ShortestPath> list = rows.get(pair.right);
        for(ShortestPath sp : list) {
          if(!sp.path.contains(pair.left)) {
            Map<Integer, Integer> newPath = new HashMap<Integer, Integer>(sp.path);        
            newPath.put(pair.left, pair.right);        
            shortestPaths.put(new Pair<Integer, Integer>(pair.left, sp.last) , new ShortestPath(sp.value + 1, newPath));
            
            // update path index
            addPathIndexEntries(pathIndex, newPath);

          }
        }
        	

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

  private static void readBatches(BufferedReader in, Map<Integer, List<Integer>> graph, Map<Pair<Integer, Integer>, ShortestPath> shortestPaths, Map<Integer, List<ShortestPath>> rows, Map<Integer, List<ShortestPath>> columns, Map<Pair<Integer, Integer>, List<ShortestPath>> pathIndex)  {

    try {
      String s;
      while ((s = in.readLine()) != null && s.length() != 0) {

        char opCode = s.charAt(0);
        switch(opCode) {
          case 'Q':
            String[] elements = s.substring(2).split(" ");
            int result = processQuery(shortestPaths, new Pair<Integer, Integer>(Integer.parseInt(elements[0]), Integer.parseInt(elements[1])));
            System.out.println(result);
            break;
          case 'A':
            elements = s.substring(2).split(" ");
            processAdd(graph, shortestPaths, pathIndex, new Pair<Integer, Integer>(Integer.parseInt(elements[0]), Integer.parseInt(elements[1])));
            break;
          case 'D':
        }

      }
    } catch(Exception e) {
      e.printStackTrace();
    } 
  }


  public static void main(String[] args) {

    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));    
    Map<Integer, List<Integer>> graph = readGraph(in);

    // debug
    for(Map.Entry<Integer, List<Integer>> entry : graph.entrySet()) {
      System.out.print(entry.getKey() + " : ");
      for(Integer i : entry.getValue()) {
        System.out.print(i + ",");
      }
      System.out.println();
    }
   
    Map<Pair<Integer, Integer>, ShortestPath> shortestPaths = new HashMap<Pair<Integer, Integer>, ShortestPath>();
    Map<Integer, List<ShortestPath>> rows = new HashMap<Integer, List<ShortestPath>>();
    Map<Integer, List<ShortestPath>> columns = new HashMap<Integer, List<ShortestPath>>();

    Map<Pair<Integer, Integer>, List<ShortestPath>> pathIndex = new HashMap<Pair<Integer, Integer>, List<ShortestPath>>();
 
    populateShortestPathTable(graph, shortestPaths, rows, columns, pathIndex); 

    // debug
    for(Map.Entry<Pair<Integer, Integer>,ShortestPath> entry : shortestPaths.entrySet())
      System.out.println("key: " + entry.getKey() + " :: value: " + entry.getValue());

    readBatches(in, graph, shortestPaths, rows, columns, pathIndex);
  }


  private static class ShortestPath {
    int head, last;
    int value;
    Map<Integer, Integer> path = new HashMap<Integer, Integer>();

    public ShortestPath(int head, int last, int value, Map<Integer, Integer> path) {
      this.value = value;
      this.path = path;
    }

    @Override
    public String toString() {

      StringBuilder sb = new StringBuilder();
      for(Map.Entry<Integer,Integer> entry : path.entrySet()) {
        sb.append(entry.getKey() + " : " + entry.getValue() + "\n");    
      }

      return value + " :: " + sb.toString();
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
