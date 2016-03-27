import java.io.*;
import java.util.*;
import java.util.ArrayList;


public class App {

  static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

  static Map<Integer, List<Integer>> graph = new TreeMap<Integer, List<Integer>>();

  static Map<Pair<Integer, Integer>, List<ShortestPath>> shortestPaths =
          new HashMap<Pair<Integer, Integer>, List<ShortestPath>>();
  static Map<Integer, List<List<ShortestPath>>> rows = new HashMap<Integer, List<List<ShortestPath>>>();
  static Map<Integer, List<List<ShortestPath>>> columns = new HashMap<Integer, List<List<ShortestPath>>>();

  static Map<Pair<Integer, Integer>, List<ShortestPath>> pathIndex =
          new HashMap<Pair<Integer, Integer>, List<ShortestPath>>();


  private static Map<Integer, List<Integer>> readGraph(BufferedReader in) {

    try {
      String s;
      while (!"S".equals(s = in.readLine())) {

        String[] elements = s.split("\\s+");
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

    // update rows and columns
    for(Map.Entry<Pair<Integer, Integer>, List<ShortestPath>> entry : shortestPaths.entrySet()) {
      Pair<Integer, Integer> pair = entry.getKey();

      List<List<ShortestPath>> list;
      if(rows.containsKey(pair.left))
        list = rows.get(pair.left);
      else
        list = new ArrayList<List<ShortestPath>>();
      list.add(entry.getValue());
      rows.put(pair.left, list);

      if(columns.containsKey(pair.right))
        list = columns.get(pair.right);
      else
        list = new ArrayList<List<ShortestPath>>();
      list.add(entry.getValue());
      columns.put(pair.right, list);
    }
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

  private static void removePathIndexEntries(ShortestPath shortestPath) {
    Map<Integer, Integer> path = shortestPath.path;
    for(Integer orig : path.keySet()) {
      int n = orig;
      while(true) {
        if(!path.containsKey(n))
          break;
        int dest = path.get(n);
        Pair<Integer, Integer> p = new Pair<Integer, Integer>(orig, dest);
        List<ShortestPath> list = pathIndex.get(p);
        list.remove(shortestPath);
        if(list.isEmpty())
          pathIndex.remove(p);
        n = dest;
      }
    }
  }

  private static void updateRowsAndColumns(int orig, int dest, List<ShortestPath> paths) {
    // update rows
    List<List<ShortestPath>> row;
    if(rows.containsKey(orig))
      row = rows.get(orig);
    else
      row = new ArrayList<List<ShortestPath>>();
    row.add(paths);
    rows.put(orig, row);

    // update columns
    List<List<ShortestPath>> column;
    if(columns.containsKey(dest))
      column = columns.get(dest);
    else
      column = new ArrayList<List<ShortestPath>>();
    column.add(paths);
    columns.put(dest, column);
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
      newPath.put(node, destNode);
      ShortestPath shortestPath = new ShortestPath(pair.left, pair.right, distance, newPath);
      addPathIndexEntries(shortestPath);

      List<ShortestPath> paths;
      if(shortestPaths.containsKey(pair))
        paths = shortestPaths.get(pair);
      else
        paths = new ArrayList<ShortestPath>();

      int index = 0;
      for(int i = 0; i < paths.size(); i++)
        if(distance < paths.get(i).value) {
          index = i;
          break;
        }
      paths.add(index, shortestPath);
      shortestPaths.put(pair, paths);

      searchShortestPath(origNode, destNode, newPath, distance);
    }
  }

  
  private static int processQuery(Pair<Integer, Integer> pair) {
    if(pair.left == pair.right)
      return 0;
    List<ShortestPath> paths = shortestPaths.get(pair);
    return paths == null ? -1 : paths.get(0).value;
  }


  private static void updateShortestPath(ShortestPath sp) {
    List<ShortestPath> list;
    if (shortestPaths.containsKey(sp.pair)) {
      list = shortestPaths.get(sp.pair);
      int index = list.size();
      for (int i = 0; i < list.size(); i++) {
        if (sp.value < list.get(i).value) {
          index = i;
          break;
        }
      }
      list.add(index, sp);
    } else {
      list = new ArrayList<ShortestPath>();
      list.add(sp);
      shortestPaths.put(sp.pair, list);
      updateRowsAndColumns(sp.head, sp.last, list);
    }
  }

  private static void processAdd(Pair<Integer, Integer> pair) {

    // check if edge already exists   
    if (shortestPaths.containsKey(pair) && shortestPaths.get(pair).get(0).value == 1) {
      return;
    }

    long startTick = System.currentTimeMillis();

    // add new edge to shortest paths
    Map<Integer, Integer> path = new HashMap<Integer, Integer>();
    path.put(pair.left, pair.right);
    ShortestPath newSp = new ShortestPath(pair.left, pair.right, 1, path);

    List<ShortestPath> piList = new ArrayList<ShortestPath>();
    piList.add(newSp);

    List<ShortestPath> paths = new ArrayList<ShortestPath>();
    paths.add(newSp);

    shortestPaths.put(new Pair<Integer, Integer>(pair.left, pair.right), paths);
    pathIndex.put(new Pair<Integer, Integer>(pair.left, pair.right), piList);

    updateRowsAndColumns(pair.left, pair.right, paths);

    System.err.println("Add new edge to shortest paths: " + (System.currentTimeMillis() - startTick));

    startTick = System.currentTimeMillis();

    // TODO optimize
    if (rows.containsKey(pair.right) && columns.containsKey(pair.left)) {

      List<List<ShortestPath>> listRows = rows.get(pair.right);
      List<List<ShortestPath>> listColumns = columns.get(pair.left);

      for (List<ShortestPath> columnPaths : listColumns) {
        for (ShortestPath columnSP : columnPaths) {
          for (List<ShortestPath> rowPaths : listRows) {
            for (ShortestPath rowSP : rowPaths) {

              // check for loop
              // TODO optimize
              Set<Integer> intersection = new HashSet<Integer>(columnSP.path.keySet());
              intersection.add(columnSP.last);

              Set<Integer> newRowSP = new HashSet<Integer>(rowSP.path.keySet());
              newRowSP.add(rowSP.last);
              intersection.retainAll(newRowSP);

              if (intersection.isEmpty()) {

                Map<Integer, Integer> newPath = new HashMap<Integer, Integer>(columnSP.path);
                newPath.putAll(rowSP.path);
                newPath.put(pair.left, pair.right);

                ShortestPath newShortestPath = new ShortestPath(columnSP.head, rowSP.last,
                        columnSP.value + rowSP.value + 1, newPath);

                // update shortest paths
                updateShortestPath(newShortestPath);

                // update path index
                addPathIndexEntries(newShortestPath);

              }
            }
          }
        }
      }
    }
    System.err.println("Joinining paths: " + (System.currentTimeMillis() - startTick));

    startTick = System.currentTimeMillis();
    // copy dest row
    if (rows.containsKey(pair.right)) {
      List<List<ShortestPath>> list = rows.get(pair.right);
      for (List<ShortestPath> lSp : list) {
        for (ShortestPath sp : lSp) {
          if (!sp.path.containsKey(pair.left) && sp.last != pair.left) {
            Map<Integer, Integer> newPath = new HashMap<Integer, Integer>(sp.path);
            newPath.put(pair.left, pair.right);
            ShortestPath newShortestPath = new ShortestPath(pair.left, sp.last, sp.value + 1, newPath);

            // update shortest paths
            updateShortestPath(newShortestPath);

            // update path index
            addPathIndexEntries(newShortestPath);

          }
        }

      }
    }
    System.err.println("Copying dest row: " + (System.currentTimeMillis() - startTick));

    startTick = System.currentTimeMillis();
    // copy origin column
    if (columns.containsKey(pair.left)) {
      List<List<ShortestPath>> listColumn = columns.get(pair.left);
      for (List<ShortestPath> lSp : listColumn) {
        for (ShortestPath sp : lSp) {

          // FIXME confirm condition
          if (!sp.path.containsKey(pair.right) && sp.head != pair.right) {

            Map<Integer, Integer> newPath = new HashMap<Integer, Integer>(sp.path);
            newPath.put(pair.left, pair.right);
            ShortestPath newShortestPath = new ShortestPath(sp.head, pair.right, sp.value + 1, newPath);

            // update shortest path
            updateShortestPath(newShortestPath);

            // update path index
            addPathIndexEntries(newShortestPath);
          }
        }
      }
    }

    System.err.println("Copy orig column: " + (System.currentTimeMillis() - startTick));

    // update graph
//      List<Integer> list;
//      if (graph.containsKey(pair.left))
//        list = graph.get(pair.left);
//      else
//        list = new ArrayList<Integer>();
//      if(!list.contains(pair.right)) {
//        list.add(pair.right);
//        graph.put(pair.left, list);
//      }
  }


  private static void processDelete(Pair<Integer, Integer> pair) {

    long startTick = System.currentTimeMillis();
    if(pathIndex.containsKey(pair)) {

//      // update graph
//      List<Integer> succNodes = graph.get(pair.left);
//      succNodes.remove(pair.right);
//      if(succNodes.size() == 0)
//        graph.remove(pair.left);

      // TODO mark as dirty shortestPath that contain edge 2->4
      // or use hash by shortPathId
      List<ShortestPath> toRemove = new ArrayList<ShortestPath>();
      List<ShortestPath> list = pathIndex.get(pair);
      for(ShortestPath sp : list) {

        int next = sp.path.get(pair.left);
        if(next == pair.right) {

          toRemove.add(sp);
          List<ShortestPath> paths = shortestPaths.get(sp.pair);
          paths.remove(sp);
          if (paths.isEmpty()) {
            shortestPaths.remove(sp.pair);

            // update rows and columns
            // TODO
            rows.remove(paths);
            columns.remove(paths);
          }
        }
      }
      for(ShortestPath sp : toRemove)
        removePathIndexEntries(sp);
    }
    System.err.println("processDelete time: " + (System.currentTimeMillis() - startTick));
  }

  private static void readBatches()  {

//    // debug
//    for(Map.Entry<Integer, List<List<ShortestPath>>> entry : rows.entrySet())
//      for(List<ShortestPath> list : entry.getValue()) {
//        ShortestPath sp = list.get(0);
//        System.out.println("(" + sp.head + ", " + sp.last + ", " + sp.value + ")");
//      }

    try {
      System.out.println("R");
      String s;
      while ((s = in.readLine()) != null && s.length() != 0) {

        System.err.println("Rows size: " + rows.size());
        System.err.println("Columns size: " + columns.size());

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
            elements = s.substring(2).split(" ");
            processDelete(new Pair<Integer, Integer>(Integer.parseInt(elements[0]), Integer.parseInt(elements[1])));
            break;
        }

      }
    } catch(Exception e) {
      e.printStackTrace();
    } 
  }


  public static void main(String[] args) {

    readGraph(in);
/*
    // debug
    for(Map.Entry<Integer, List<Integer>> entry : graph.entrySet()) {
      System.out.print(entry.getKey() + " : ");
      for(Integer i : entry.getValue()) {
        System.out.print(i + ",");
      }
      System.out.println();
    }
*/
    populateShortestPathTable();

/*
    // debug
    for(Map.Entry<Pair<Integer, Integer>,List<ShortestPath>> entry : shortestPaths.entrySet())
      System.out.println("key: " + entry.getKey() + " :: value: " + entry.getValue().get(0));
*/

    readBatches();
  }


  private static class ShortestPath {
    int head, last;
    Pair<Integer, Integer> pair;
    int value;
    Map<Integer, Integer> path = new HashMap<Integer, Integer>();
    UUID id = UUID.randomUUID();

    public ShortestPath(int head, int last, int value, Map<Integer, Integer> path) {
      this.head = head;
      this.last = last;
      this.pair = new Pair<Integer, Integer>(head, last);
      this.value = value;
      this.path = path;
    }

    public ShortestPath(ShortestPath sp) {
      this.head = sp.head;
      this.last = sp.last;
      this.pair = new Pair<Integer, Integer>(head, last);
      this.value = sp.value;
      this.path = new HashMap<Integer, Integer>(sp.path);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      ShortestPath that = (ShortestPath) o;

      return id.equals(that.id);

    }

    @Override
    public int hashCode() {
      return id.hashCode();
    }

    @Override
    public String toString() {

      StringBuilder sb = new StringBuilder();
      for(Map.Entry<Integer,Integer> entry : path.entrySet()) {
        sb.append(entry.getKey() + " -> " + entry.getValue() + " - ");
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
