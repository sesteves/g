import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Sergio on 29/03/2016.
 */
public class App3 {

    static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    static Map<Integer, Map<Integer, List<Integer>>> rows = Collections.synchronizedMap(new HashMap<Integer, Map<Integer, List<Integer>>>());
    static Map<Integer, Map<Integer, List<Integer>>> columns = Collections.synchronizedMap(new HashMap<Integer, Map<Integer, List<Integer>>>());

    static Map<Integer, Lock> rowLocks = new ConcurrentHashMap<>();
    static Map<Integer, Lock> columnLocks = new ConcurrentHashMap<>();

    static Lock generalLock = new ReentrantLock();

    private static void insertOnTable(int row, int column, int value) {

        // lock row and column
//        generalLock.lock();
//        Lock rowLock;
//        if(rowLocks.containsKey(row))
//            rowLock = rowLocks.get(row);
//        else
//            rowLock = new ReentrantLock();
//        rowLock.lock();
//        rowLocks.put(row, rowLock);
//
//        Lock columnLock;
//        if(columnLocks.containsKey(column))
//            columnLock = columnLocks.get(column);
//        else
//            columnLock = new ReentrantLock();
//        columnLock.lock();
//        columnLocks.put(column, columnLock);
//        generalLock.unlock();


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
                values = Collections.synchronizedList(new ArrayList<Integer>());
                values.add(value);
                innerColumns.put(column, values);
                rows.put(row, innerColumns);
                newValues = true;
            }
        } else {
            values = Collections.synchronizedList(new ArrayList<Integer>());
            values.add(value);
            Map<Integer, List<Integer>> innerColumns = Collections.synchronizedMap(new HashMap<Integer, List<Integer>>());
            innerColumns.put(column, values);
            rows.put(row, innerColumns);
            newValues = true;
        }

        if(newValues) {
            Map<Integer, List<Integer>> innerRows;
            if(columns.containsKey(column)) {
                innerRows = columns.get(column);
            } else {
                innerRows = Collections.synchronizedMap(new HashMap<Integer, List<Integer>>());
            }
            innerRows.put(row, values);
            columns.put(column, innerRows);
        }

        // unlock row and column
//        generalLock.lock();
//        columnLock.unlock();
//        rowLock.unlock();
//        generalLock.unlock();
    }

    private static void readGraph() {
        try {
            ExecutorService executor = Executors.newFixedThreadPool(8);
            String s;
            int count = 0;
            while (!"S".equals(s = in.readLine())) {
                final String[] elements = s.split("\\s+");

                executor.execute(new Runnable() {
                    @Override
                    public void run() {

                        int[] edge = new int[]{Integer.parseInt(elements[0]), Integer.parseInt(elements[1])};

                        // lock rows and columns
                        //generalLock.lock();
                        System.err.println("CHECK1");
                        Lock rowLock;
                        Set<Integer> innerColumnsSet = null;
                        if(rowLocks.containsKey(edge[1])) {
                            rowLock = rowLocks.get(edge[1]);
                            rowLock.lock();
                            if(rows.containsKey(edge[1])) {
                                innerColumnsSet = rows.get(edge[1]).keySet();
                                for (int column : innerColumnsSet)
                                    if(column != edge[0])
                                        columnLocks.get(column).lock();
                            }
                        } else {
                            rowLock = new ReentrantLock();
                            rowLock.lock();
                        }
                        rowLocks.put(edge[1], rowLock);
                        System.err.println("CHECK1-1");
                        Lock rowLock2;
                        if(rowLocks.containsKey(edge[0]))
                            rowLock2 = rowLocks.get(edge[0]);
                        else
                            rowLock2 = new ReentrantLock();
                        rowLock2.lock();
                        rowLocks.put(edge[0], rowLock2);
                        System.err.println("CHECK1-2");
                        Lock columnLock;
                        Set<Integer> innerRowsSet = null;
                        if(columnLocks.containsKey(edge[0])) {
                            columnLock = columnLocks.get(edge[0]);
                            columnLock.lock();
                            if(columns.containsKey(edge[0])) {
                                innerRowsSet = columns.get(edge[0]).keySet();
                                for (int row : innerRowsSet)
                                    if(row != edge[1])
                                        rowLocks.get(row).lock();
                            }
                        } else {
                            columnLock = new ReentrantLock();
                            columnLock.lock();
                        }
                        columnLocks.put(edge[0], columnLock);
                        System.err.println("CHECK1-3");
                        Lock columnLock2;
                        if(columnLocks.containsKey(edge[1]))
                            columnLock2 = columnLocks.get(edge[1]);
                        else
                            columnLock2 = new ReentrantLock();
                        columnLock2.lock();
                        columnLocks.put(edge[1], columnLock2);
                        System.err.println("CHECK2");
                        //generalLock.unlock();



                        boolean newValues = false;
                        List<Integer> values;
                        if(rows.containsKey(edge[0])) {
                            Map<Integer, List<Integer>> innerColumns = rows.get(edge[0]);
                            if(innerColumns.containsKey(edge[1])) {
                                values = innerColumns.get(edge[1]);
                                if(values.get(0) == 1)
                                    return;
                                values.add(0, 1);

                            } else {
                                values = Collections.synchronizedList(new ArrayList<Integer>());
                                values.add(1);
                                innerColumns.put(edge[1], values);
                                newValues = true;
                            }
                        } else {
                            Map<Integer, List<Integer>> innerColumns = Collections.synchronizedMap(new HashMap<Integer, List<Integer>>());
                            values = Collections.synchronizedList(new ArrayList<Integer>());
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
                                innerRows = Collections.synchronizedMap(new HashMap<Integer, List<Integer>>());
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

                                List<Integer> rowValues = new ArrayList<Integer>(row.getValue());
                                for(int rowValue : rowValues) {
                                    for(Map.Entry<Integer, List<Integer>> column : innerColumns.entrySet()) {
                                        if(column.getKey() == row.getKey() || column.getKey() == edge[0])
                                            continue;
                                        List<Integer> columnValues = new ArrayList<Integer>(column.getValue());
                                        for(int columnValue : columnValues) {
                                            insertOnTable(row.getKey(), column.getKey(), rowValue + columnValue + 1);
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



                        // unlock rows and columns
                        //generalLock.lock();
                        System.err.println("CHECK3");
                        columnLock2.unlock();
                        if (innerRowsSet != null)
                            for (int row : innerRowsSet)
                                rowLocks.get(row).unlock();
                        columnLock.unlock();
                        rowLock2.unlock();
                        if (innerColumnsSet != null)
                            for (int column : innerColumnsSet)
                                columnLocks.get(column).unlock();
                        rowLock.unlock();
                        System.err.println("CHECK4");
                        //generalLock.unlock();

                    }
                });

                count++;
                System.err.println("Number of edges processed: " + count);
            }
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            System.err.println("Number of edges: " + count);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    private static int processQuery(int orig, int dest) {
        if(orig == dest)
            return 0;
        try {
            return rows.get(orig).get(dest).get(0);
        } catch (Exception e) {
            return -1;
        }
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

                char opCode = s.charAt(0);
                switch(opCode) {
                    case 'Q':
                        String[] elements = s.substring(2).split(" ");
                        int result = processQuery(Integer.parseInt(elements[0]), Integer.parseInt(elements[1]));
                        System.out.println(result);
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

        // debug
        for(Map.Entry<Integer, Map<Integer, List<Integer>>> entry : rows.entrySet())
            for(Map.Entry<Integer, List<Integer>> entry2 : entry.getValue().entrySet())
                System.err.println("row: " + entry.getKey() + ", col: " + entry2.getKey() + ", value: " +
                        entry2.getValue().get(0));



        System.err.println("Reading batches...");
        readBatches();
    }

}
