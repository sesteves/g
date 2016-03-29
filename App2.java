import java.io.*;
import java.util.*;

/**
 * Created by Sergio on 29/03/2016.
 */
public class App2 {

    static Map<Integer, Map<Integer, List<Integer>>> rows = new HashMap<Integer, Map<Integer, List<Integer>>>();
    static Map<Integer, Map<Integer, List<Integer>>> columns = new HashMap<Integer, Map<Integer, List<Integer>>>();

    private static void insertOnTable(int row, int column, int value) {


    }

    private static void readGraph(BufferedReader in) {
        try {
            String s;
            int count = 0;
            while (!"S".equals(s = in.readLine())) {

                String[] elements = s.split("\\s+");
                int[] edge = new int[]{Integer.parseInt(elements[0]), Integer.parseInt(elements[1])};

                boolean newValues = false;
                List<Integer> values;
                if(rows.containsKey(edge[0])) {
                    Map<Integer, List<Integer>> innerColumns = rows.get(edge[0]);
                    if(innerColumns.containsKey(edge[1])) {
                        values = innerColumns.get(edge[1]);
                        if(values.get(0) == 1)
                            continue;
                        values.add(0, 1);
                    } else {
                        values = new ArrayList<Integer>();
                        values.add(1);
                        innerColumns.put(edge[1], values);
                        newValues = true;
                    }
                } else {
                    Map<Integer, List<Integer>> innerColumns = new HashMap<Integer, List<Integer>>();
                    values = new ArrayList<Integer>();
                    values.add(1);
                    innerColumns.put(edge[1], values);
                    rows.put(edge[0], innerColumns);
                    newValues = true;
                }
                if(newValues) {
                    Map<Integer, List<Integer>> innerRows;
                    if(columns.containsKey(edge[1])) {
                        innerRows = columns.get(edge[1]);
                    } else {
                        innerRows = new HashMap<Integer, List<Integer>>();
                    }
                    innerRows.put(edge[0], values);
                    columns.put(edge[1], innerRows);
                }


                if(rows.containsKey(edge[1])) {

                }

                if(columns.containsKey(edge[0])) {

                }







                count++;
            }
            System.err.println("Number of edges: " + count);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    private static int processQuery() {
        return 0;
    }

    private static void processAdd() {
    }
    private static void processDelete() {
    }


    private static void readBatches()  {

        try {
            System.gc();
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
        System.err.println("Reading graph and populating table...");
        readGraph(in);

        System.err.println("Reading batches...");
        readBatches();
    }

}
