import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Sergio on 29/03/2016.
 */
public class App2 {

    static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    static Map<Integer, Map<Integer, List<Integer>>> rows = Collections.synchronizedMap(new HashMap<Integer, Map<Integer, List<Integer>>>());
    static Map<Integer, Map<Integer, List<Integer>>> columns = Collections.synchronizedMap(new HashMap<Integer, Map<Integer, List<Integer>>>());

    static Map<Integer, Lock> rowLocks = new ConcurrentHashMap<>();
    static Map<Integer, Lock> columnLocks = new ConcurrentHashMap<>();

    private static void insertOnTable(int row, int column, int value) {
        boolean newValues = false;
        List<Integer> values = null;
        if(rows.containsKey(row)) {
            // lock row
            rowLocks.get(row).lock();

            Map<Integer, List<Integer>> innerColumns = rows.get(row);
            if(innerColumns.containsKey(column)) {
                // lock column
                columnLocks.get(column).lock();

                values = innerColumns.get(column);

                int i = -1;
                while(++i < values.size() && values.get(i) < value);
                values.add(i, value);
            } else {
                // lock column
                Lock columnLock = new ReentrantLock();
                columnLock.lock();
                columnLocks.put(column, columnLock);

                values = Collections.synchronizedList(new ArrayList<Integer>());
                values.add(value);
                innerColumns.put(column, values);
                rows.put(row, innerColumns);
                newValues = true;
            }
        } else {
            // lock row and column
            Lock rowLock = new ReentrantLock();
            rowLock.lock();
            rowLocks.put(row, rowLock);
            Lock columnLock = new ReentrantLock();
            columnLock.lock();
            columnLocks.put(column, columnLock);

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
        rowLocks.get(row).unlock();
        columnLocks.get(column).unlock();
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

                        boolean newValues = false;
                        List<Integer> values;
                        if(rows.containsKey(edge[0])) {
                            // lock row
                            rowLocks.get(edge[0]).lock();

                            Map<Integer, List<Integer>> innerColumns = rows.get(edge[0]);
                            if(innerColumns.containsKey(edge[1])) {
                                // lock column
                                columnLocks.get(edge[1]).lock();

                                values = innerColumns.get(edge[1]);
                                if(values.get(0) == 1)
                                    return;
                                values.add(0, 1);

                            } else {
                                // lock column
                                Lock lock = new ReentrantLock();
                                lock.lock();
                                columnLocks.put(edge[1], lock);

                                values = Collections.synchronizedList(new ArrayList<Integer>());
                                values.add(1);
                                innerColumns.put(edge[1], values);
                                newValues = true;
                            }

                            // unlock row and column
                            columnLocks.get(edge[1]).unlock();
                            rowLocks.get(edge[0]).unlock();
                        } else {
                            // lock row and column
                            Lock rowLock = new ReentrantLock();
                            rowLock.lock();
                            rowLocks.put(edge[0], rowLock);
                            Lock columnLock = new ReentrantLock();
                            columnLock.lock();
                            columnLocks.put(edge[1], columnLock);

                            Map<Integer, List<Integer>> innerColumns = Collections.synchronizedMap(new HashMap<Integer, List<Integer>>());
                            values = Collections.synchronizedList(new ArrayList<Integer>());
                            values.add(1);
                            innerColumns.put(edge[1], values);
                            rows.put(edge[0], innerColumns);

                            // unlock row and column
                            rowLock.unlock();
                            columnLock.unlock();

                            newValues = true;
                        }
                        if(newValues) {
                            Map<Integer, List<Integer>> innerRows;
                            if(columns.containsKey(edge[1])) {
                                // lock column and row
                                columnLocks.get(edge[1]).lock();
                                rowLocks.get(edge[0]).lock();

                                innerRows = columns.get(edge[1]);
                            } else {
                                // lock column and row
                                Lock columnLock = new ReentrantLock();
                                columnLock.lock();
                                columnLocks.put(edge[1], columnLock);
                                Lock rowLock = new ReentrantLock();
                                rowLock.lock();
                                rowLocks.put(edge[0], rowLock);

                                innerRows = Collections.synchronizedMap(new HashMap<Integer, List<Integer>>());
                            }
                            innerRows.put(edge[0], values);
                            columns.put(edge[1], innerRows);

                            // unlock column and row
                            columnLocks.get(edge[1]).unlock();
                            rowLocks.get(edge[0]).unlock();
                        }


                        if(rows.containsKey(edge[1]) && columns.containsKey(edge[0])) {
                            // lock row and column
                            rowLocks.get(edge[1]).lock();
                            columnLocks.get(edge[0]).lock();

                            Map<Integer, List<Integer>> innerColumns = rows.get(edge[1]);
                            Map<Integer, List<Integer>> innerRows = columns.get(edge[0]);
                            for(Map.Entry<Integer, List<Integer>> row : innerRows.entrySet()) {
                                if(row.getKey() == edge[1])
                                    continue;
                                // lock row
                                rowLocks.get(row.getKey()).lock();

                                List<Integer> rowValues = Collections.synchronizedList(new ArrayList<Integer>());
                                for(int rowValue : rowValues) {
                                    for(Map.Entry<Integer, List<Integer>> column : innerColumns.entrySet()) {
                                        if(column.getKey() == row.getKey() || column.getKey() == edge[0])
                                            continue;
                                        // lock column
                                        columnLocks.get(column.getKey()).lock();

                                        List<Integer> columnValues = Collections.synchronizedList(new ArrayList<Integer>());
                                        for(int columnValue : columnValues) {
                                            insertOnTable(row.getKey(), column.getKey(), rowValue + columnValue);
                                        }

                                        // unlock column
                                        columnLocks.get(column.getKey()).unlock();
                                    }
                                }

                                // unlock row
                                rowLocks.get(row.getKey()).unlock();

                            }

                            // unlock row and column
                            rowLocks.get(edge[1]).unlock();
                            columnLocks.get(edge[0]).unlock();
                        }

                        if(rows.containsKey(edge[1])) {
                            // lock row
                            rowLocks.get(edge[1]).lock();

                            Map<Integer, List<Integer>> innerColumns = rows.get(edge[1]);
                            for(Map.Entry<Integer, List<Integer>> column : innerColumns.entrySet()) {
                                if(column.getKey() == edge[0])
                                    continue;

                                // lock column
                                columnLocks.get(column.getKey()).lock();

                                List<Integer> list = column.getValue();
                                for(int value : list)
                                    insertOnTable(edge[0], column.getKey(), value + 1);

                                // unlock column
                                columnLocks.get(column.getKey()).unlock();

                            }

                            // unlock row
                            rowLocks.get(edge[1]).unlock();
                        }

                        if(columns.containsKey(edge[0])) {
                            // lock column
                            columnLocks.get(edge[0]).lock();

                            Map<Integer, List<Integer>> innerRows = columns.get(edge[0]);
                            for(Map.Entry<Integer, List<Integer>> row : innerRows.entrySet()) {
                                if(row.getKey() == edge[1])
                                    continue;
                                // lock row
                                rowLocks.get(row.getKey()).lock();

                                List<Integer> list = row.getValue();
                                for(int value : list)
                                    insertOnTable(row.getKey(), edge[1], value + 1);

                                // unlock row
                                rowLocks.get(row.getKey()).unlock();
                            }

                            // unlock column
                            columnLocks.get(edge[0]).unlock();
                        }

                    }
                });

                count++;
                System.err.println("Number of edges processed: " + count);
            }
            executor.shutdown();
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
