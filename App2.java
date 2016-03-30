import java.io.*;
import java.util.*;

/**
 * Created by Sergio on 29/03/2016.
 */
public class App2 {

    static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    static Map<Integer, Map<Integer, List<Integer>>> rows = new HashMap<Integer, Map<Integer, List<Integer>>>();
    static Map<Integer, Map<Integer, List<Integer>>> columns = new HashMap<Integer, Map<Integer, List<Integer>>>();

    private static void insertOnTable(int row, int column, int value) {
        boolean newValues = false;
        List<Integer> values = null;
        if(rows.containsKey(row)) {
            Map<Integer, List<Integer>> innerColumns = rows.get(row);
            if(innerColumns.containsKey(column)) {
                values = innerColumns.get(column);

                int i = -1;
                while(++i < values.size() && values.get(i) < value);
                values.add(i, value);
            } else {
                values = new ArrayList<Integer>();
                values.add(value);
                innerColumns.put(column, values);
                rows.put(row, innerColumns);
                newValues = true;
            }
        } else {
            values = new ArrayList<Integer>();
            values.add(value);
            Map<Integer, List<Integer>> innerColumns = new HashMap<Integer, List<Integer>>();
            innerColumns.put(column, values);
            rows.put(row, innerColumns);
            newValues = true;
        }

        if(newValues) {
            Map<Integer, List<Integer>> innerRows;
            if(columns.containsKey(column)) {
                innerRows = columns.get(column);
            } else {
                innerRows = new HashMap<>();
            }
            innerRows.put(row, values);
            columns.put(column, innerRows);
        }
    }

    private static void readGraph() {
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
                        innerRows = new HashMap<>();
                    }
                    innerRows.put(edge[0], values);
                    columns.put(edge[1], innerRows);
                }


                if(rows.containsKey(edge[1]) && columns.containsKey(edge[0])) {

                     Map<Integer, List<Integer>> innerColumns = rows.get(edge[1]);
                     Map<Integer, List<Integer>> innerRows = columns.get(edge[0]);
                     for(Map.Entry<Integer, List<Integer>> row : innerRows.entrySet()) {
                         if(row.getKey() == edge[1])
                             continue;
                         List<Integer> rowValues = new ArrayList(row.getValue());
                         for(int rowValue : rowValues) {
                             for(Map.Entry<Integer, List<Integer>> column : innerColumns.entrySet()) {
                                 if(column.getKey() == row.getKey() || column.getKey() == edge[0])
                                     continue;

                                 List<Integer> columnValues = new ArrayList(column.getValue());
                                 for(int columnValue : columnValues) {
                                     insertOnTable(row.getKey(), column.getKey(), rowValue + columnValue);
                                 }
                             }
                         }
                     }
                }

                if(rows.containsKey(edge[1])) {
                    Map<Integer, List<Integer>> innerColumns = rows.get(edge[1]);
                    for(Map.Entry<Integer, List<Integer>> column : innerColumns.entrySet()) {
                        if(column.getKey() == edge[0])
                            continue;
                        List<Integer> list = column.getValue();
                        for(int value : list)
                            insertOnTable(edge[0], column.getKey(), value + 1);
                    }
                }

                if(columns.containsKey(edge[0])) {
                    Map<Integer, List<Integer>> innerRows = columns.get(edge[0]);
                    for(Map.Entry<Integer, List<Integer>> row : innerRows.entrySet()) {
                        if(row.getKey() == edge[1])
                            continue;
                        List<Integer> list = row.getValue();
                        for(int value : list)
                            insertOnTable(row.getKey(), edge[1], value + 1);
                    }
                }

                count++;
                System.err.println("Number of edges processed: " + count);
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
//                        int result = processQuery(new Pair<Integer, Integer>(Integer.parseInt(elements[0]), Integer.parseInt(elements[1])));
//                        System.out.println(result);
                        break;
                    case 'A':
//                        elements = s.substring(2).split(" ");
//                        processAdd(new Pair<Integer, Integer>(Integer.parseInt(elements[0]), Integer.parseInt(elements[1])));
                        break;
                    case 'D':
//                        elements = s.substring(2).split(" ");
//                        processDelete(new Pair<Integer, Integer>(Integer.parseInt(elements[0]), Integer.parseInt(elements[1])));
                        break;
                }

            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.err.println("Reading graph and populating table...");
        long startTick = System.currentTimeMillis();
        readGraph();
        System.err.println("Took " + (System.currentTimeMillis() - startTick));

        System.err.println("Reading batches...");
        readBatches();
    }

}
