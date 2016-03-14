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

  private static void processAdd(Pair<Integer, Integer> pair) {

    // check if edge already exists   
    if (shortestPaths.containsKey(pair) && shortestPaths.get(pair).get(0).value == 1) {
      return;
    }

    // if pathIndex contains pair it means that both nodes already exist and that pair represents a shortcut
    if (pathIndex.containsKey(pair)) { // this should check for dirty nodes

      List<ShortestPath> newSps = new ArrayList<ShortestPath>();
      List<ShortestPath> list = pathIndex.get(pair);
      for (ShortestPath sp : list) {

        // copy shortestPath
        ShortestPath newSp = new ShortestPath(sp);

        Map<Integer, Integer> path = newSp.path;

        int node = path.get(pair.left);
        path.put(pair.left, pair.right);
        int count = 0;
        while (node != pair.right) {
          count++;
          node = path.remove(node);
        }
        newSp.value -= count;

        // add new node to shortestPaths
        List<ShortestPath> paths = shortestPaths.get(newSp.pair);
        int index = -1;
        for (int i = 0; i < paths.size(); i++) {
          if (newSp.value < paths.get(i).value) {
            index = i;
            break;
          }
        }
        if (index < 0)
          paths.add(newSp);
        else
          paths.add(index, newSp);

        newSps.add(newSp);

        // TODO: necessary to update rows and columns? NO
      }

      // add to pathIndex
      for(ShortestPath sp : newSps)
        addPathIndexEntries(sp);

      // add to graph
      graph.get(pair.left).add(pair.right);

    } else {

      // add new edge to shortest paths
      Map<Integer, Integer> path = new HashMap<Integer, Integer>();
      path.put(pair.left, pair.right);
      ShortestPath newSp = new ShortestPath(pair.left, pair.right, 1, path);

      // List<ShortestPath> piList = new ArrayList<ShortestPath>();
      // piList.add(newSp);

      List<ShortestPath> paths = new ArrayList<ShortestPath>();
      paths.add(newSp);

      shortestPaths.put(new Pair<Integer, Integer>(pair.left, pair.right), paths);
      pathIndex.put(new Pair<Integer, Integer>(pair.left, pair.right), paths);

      updateRowsAndColumns(pair.left, pair.right, paths);


      // copy dest row
      if (rows.containsKey(pair.right)) {
        List<List<ShortestPath>> list = rows.get(pair.right);
        for (List<ShortestPath> lSp : list) {

          List<ShortestPath> newLSP = new ArrayList<ShortestPath>();
          for (ShortestPath sp : lSp) {
            if (!sp.path.containsKey(pair.left) && sp.last != pair.left) {
              Map<Integer, Integer> newPath = new HashMap<Integer, Integer>(sp.path);
              newPath.put(pair.left, pair.right);
              ShortestPath newShortestPath = new ShortestPath(pair.left, sp.last, sp.value + 1, newPath);
              newLSP.add(newShortestPath);

              // update path index
              addPathIndexEntries(newShortestPath);

            }
          }
          if (!newLSP.isEmpty()) {
            shortestPaths.put(new Pair<Integer, Integer>(pair.left, lSp.get(0).last), newLSP);

            // update rows and columns
            updateRowsAndColumns(pair.left, lSp.get(0).last, newLSP);
          }
        }
      }

      // copy origin column
      if (columns.containsKey(pair.left)) {
        List<List<ShortestPath>> list = columns.get(pair.left);
        for (List<ShortestPath> lSp : list) {

          List<ShortestPath> newLSP = new ArrayList<ShortestPath>();
          for (ShortestPath sp : lSp) {


            // FIXME confirm condition
            if (!sp.path.containsKey(pair.right) && sp.head != pair.right) {



              Map<Integer, Integer> newPath = new HashMap<Integer, Integer>(sp.path);
              newPath.put(pair.left, pair.right);
              ShortestPath newShortestPath = new ShortestPath(sp.head, pair.right, sp.value + 1, newPath);
              newLSP.add(newShortestPath);

              // update path index
              addPathIndexEntries(newShortestPath);
            }
          }
          if(!newLSP.isEmpty()) {
            shortestPaths.put(new Pair<Integer, Integer>(lSp.get(0).head, pair.right), newLSP);
            updateRowsAndColumns(lSp.get(0).head, pair.right, newLSP);
          }
        }
      }

      // update graph
      List<Integer> list;
      if (graph.containsKey(pair.left))
        list = graph.get(pair.left);
      else
        list = new ArrayList<Integer>();
      if(!list.contains(pair.right)) {
        list.add(pair.right);
        graph.put(pair.left, list);
      }


    }


//      // if right node exists
//      if (graph.containsKey(pair.right) && !graph.containsKey(pair.left)) {
//
//        // copy dest row
//        if (rows.containsKey(pair.right)) {
//          List<List<ShortestPath>> list = rows.get(pair.right);
//          for (List<ShortestPath> lSp : list) {
//
//            List<ShortestPath> newLSP = new ArrayList<ShortestPath>();
//            for(ShortestPath sp : lSp) {
//              if (!sp.path.containsKey(pair.left) && sp.last != pair.left) {
//                Map<Integer, Integer> newPath = new HashMap<Integer, Integer>(sp.path);
//                newPath.put(pair.left, pair.right);
//                ShortestPath newShortestPath = new ShortestPath(pair.left, sp.last, sp.value + 1, newPath);
//                newLSP.add(newShortestPath);
//
//                // update path index
//                addPathIndexEntries(newShortestPath);
//
//              }
//            }
//            if(!newLSP.isEmpty()) {
//              shortestPaths.put(new Pair<Integer, Integer>(pair.left, lSp.get(0).last), newLSP);
//
//              // update rows and columns
//              updateRowsAndColumns(pair.left, lSp.get(0).last, newLSP);
//            }
//          }
//        }
//
//        // update graph
//        if (!graph.containsKey(pair.left)) {
//          List<Integer> list = new ArrayList<Integer>();
//          list.add(pair.right);
//          graph.put(pair.left, list);
//        }

//      } else if (graph.containsKey(pair.left)) {
//
//        // copy origin column
//        if (columns.containsKey(pair.left)) {
//          List<List<ShortestPath>> list = columns.get(pair.left);
//          for (List<ShortestPath> lSp : list) {
//
//            List<ShortestPath> newLSP = new ArrayList<ShortestPath>();
//            for(ShortestPath sp : lSp) {
//              Map<Integer, Integer> newPath = new HashMap<Integer, Integer>(sp.path);
//              newPath.put(pair.left, pair.right);
//              ShortestPath newShortestPath = new ShortestPath(sp.head, pair.right, sp.value + 1, newPath);
//              newLSP.add(newShortestPath);
//
//              // update path index
//              addPathIndexEntries(newShortestPath);
//            }
//            shortestPaths.put(new Pair<Integer, Integer>(lSp.get(0).head, pair.right), newLSP);
//            updateRowsAndColumns(lSp.get(0).head, pair.right, newLSP);
//          }
//        }
//
//        // update graph
//        List<Integer> list;
//        if (graph.containsKey(pair.left))
//          list = graph.get(pair.left);
//        else
//          list = new ArrayList<Integer>();
//        list.add(pair.right);
//        graph.put(pair.left, list);

//      } else {
//        // update graph in case both nodes do not exist
//        List<Integer> list = new ArrayList<Integer>();
//        list.add(pair.right);
//        graph.put(pair.left, list);
//        graph.put(pair.right, new ArrayList<Integer>());
//
//        // update shortest path
//        path = new HashMap<Integer, Integer>();
//        path.put(pair.left, pair.right);
//        ShortestPath sp = new ShortestPath(pair.left, pair.right, 1, path);
//        List<ShortestPath> lSp = new ArrayList<ShortestPath>();
//        lSp.add(sp);
//        shortestPaths.put(sp.pair, lSp);
//
//        // update path index
//        lSp = new ArrayList<ShortestPath>();
//        lSp.add(sp);
//        pathIndex.put(sp.pair, lSp);
//
//        // update rows and columns
//        updateRowsAndColumns(pair.left, pair.right, lSp);
//      }
//    }
  }


  private static void processDelete(Pair<Integer, Integer> pair) {

    if(pathIndex.containsKey(pair)) {

      // update graph
      List<Integer> succNodes = graph.get(pair.left);
      succNodes.remove(pair.right);
      if(succNodes.size() == 0)
        graph.remove(pair.left);

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
          if (paths.size() == 0)
            shortestPaths.remove(sp.pair);

        }
        // TODO update rows and columns
      }
      for(ShortestPath sp : toRemove)
        pathIndex.remove(sp);
    }
  }

  private static void readBatches()  {

    // debug
    for(Map.Entry<Integer, List<List<ShortestPath>>> entry : rows.entrySet())
      for(List<ShortestPath> list : entry.getValue()) {
        ShortestPath sp = list.get(0);
        System.out.println("(" + sp.head + ", " + sp.last + ", " + sp.value + ")");
      }

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
    for(Map.Entry<Pair<Integer, Integer>,List<ShortestPath>> entry : shortestPaths.entrySet())
      System.out.println("key: " + entry.getKey() + " :: value: " + entry.getValue().get(0));

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
