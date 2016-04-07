import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class App5 {

    static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    static Map<Integer, Map<Integer, PriorityBlockingQueue<Integer>>> rows = new ConcurrentHashMap<>();
    static Map<Integer, Map<Integer, PriorityBlockingQueue<Integer>>> columns = new ConcurrentHashMap<>();

    static Map<Integer, Lock> rowLocks = new ConcurrentHashMap<Integer, Lock>();
    static Map<Integer, Lock> columnLocks = new ConcurrentHashMap<Integer, Lock>();

    static ExecutorService executor = Executors.newFixedThreadPool(3);

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
        PriorityBlockingQueue<Integer> values = null;
        if(rows.containsKey(row)) {
            Map<Integer, PriorityBlockingQueue<Integer>> innerColumns = rows.get(row);
            if(innerColumns.containsKey(column)) {
                values = innerColumns.get(column);
                values.add(value);

//                int i = -1;
//                while(++i < values.size() && values.get(i) < value);
//                values.add(i, value);
            } else {
                values = new PriorityBlockingQueue<>();
                values.add(value);
                innerColumns.put(column, values);
                rows.put(row, innerColumns);
                newValues = true;
            }
        } else {
            values = new PriorityBlockingQueue<>();
            values.add(value);
            Map<Integer, PriorityBlockingQueue<Integer>> innerColumns = new ConcurrentHashMap<>();
            innerColumns.put(column, values);
            rows.put(row, innerColumns);
            newValues = true;
        }

        if(newValues) {
            Map<Integer, PriorityBlockingQueue<Integer>> innerRows;
            if(columns.containsKey(column)) {
                innerRows = columns.get(column);
            } else {
                innerRows = new ConcurrentHashMap<>();
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
            ExecutorService executor = Executors.newFixedThreadPool(16);
            String s;
            int count = 0;
            while (!"S".equals(s = in.readLine())) {
                final String[] elements = s.split("\\s+");
//                int[] edge = new int[]{Integer.parseInt(elements[0]), Integer.parseInt(elements[1])};
//                processAdd(edge[0], edge[1]);

                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        int[] edge = new int[]{Integer.parseInt(elements[0]), Integer.parseInt(elements[1])};
                        processAdd(edge[0], edge[1]);
                    }
                });

                count++;
                System.err.println("Number of edges processed: " + count);
            }
//            executor.shutdown();
//            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            System.err.println("Number of edges: " + count);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    private static int processQuery(int orig, int dest) {
        if(orig == dest)
            return 0;
        try {
            return rows.get(orig).get(dest).peek();
        } catch (Exception e) {
            return -1;
        }
    }

    private static void processAdd(int orig, int dest) {



// lock rows and columns
        //generalLock.lock();
        Lock rowLock;
        Set<Integer> innerColumnsSet = null;
        if(rowLocks.containsKey(dest)) {
            rowLock = rowLocks.get(dest);
            rowLock.lock();
            if(rows.containsKey(dest)) {
                innerColumnsSet = rows.get(dest).keySet();
                for (int column : innerColumnsSet)
                    if(column != orig)
                        columnLocks.get(column).lock();
            }
        } else {
            rowLock = new ReentrantLock();
            rowLock.lock();
        }
        rowLocks.put(dest, rowLock);
        Lock rowLock2;
        if(rowLocks.containsKey(orig))
            rowLock2 = rowLocks.get(orig);
        else
            rowLock2 = new ReentrantLock();
        rowLock2.lock();
        rowLocks.put(orig, rowLock2);
        Lock columnLock;
        Set<Integer> innerRowsSet = null;
        if(columnLocks.containsKey(orig)) {
            columnLock = columnLocks.get(orig);
            columnLock.lock();
            if(columns.containsKey(orig)) {
                innerRowsSet = columns.get(orig).keySet();
                for (int row : innerRowsSet)
                    if(row != dest)
                        rowLocks.get(row).lock();
            }
        } else {
            columnLock = new ReentrantLock();
            columnLock.lock();
        }
        columnLocks.put(orig, columnLock);
        Lock columnLock2;
        if(columnLocks.containsKey(dest))
            columnLock2 = columnLocks.get(dest);
        else
            columnLock2 = new ReentrantLock();
        columnLock2.lock();
        columnLocks.put(dest, columnLock2);
        //generalLock.unlock();



        boolean newValues = false;
        PriorityBlockingQueue<Integer> values;
        if(rows.containsKey(orig)) {
            Map<Integer, PriorityBlockingQueue<Integer>> innerColumns = rows.get(orig);
            if(innerColumns.containsKey(dest)) {
                values = innerColumns.get(dest);
                if(values.peek() == 1)
                    return;
                values.add(1);

            } else {
                values = new PriorityBlockingQueue<>();
                values.add(1);
                innerColumns.put(dest, values);
                newValues = true;
            }
        } else {
            Map<Integer, PriorityBlockingQueue<Integer>> innerColumns = new ConcurrentHashMap<>();
            values = new PriorityBlockingQueue<>();
            values.add(1);
            innerColumns.put(dest, values);
            rows.put(orig, innerColumns);
            newValues = true;
        }
        if(newValues) {
            Map<Integer, PriorityBlockingQueue<Integer>> innerRows;
            if(columns.containsKey(dest)) {
                innerRows = columns.get(dest);
            } else {
                innerRows = new ConcurrentHashMap<>();
            }
            innerRows.put(orig, values);
            columns.put(dest, innerRows);
        }

        Set<Integer> interception = new HashSet<Integer>();
        Map<Integer, PriorityBlockingQueue<Integer>> innerColumns = rows.get(dest);
        Map<Integer, PriorityBlockingQueue<Integer>> innerRows = columns.get(orig);
        if(innerColumns != null && innerRows != null) {
            interception.addAll(innerRows.keySet());
            interception.retainAll(innerColumns.keySet());
        }

//        executor.execute(new Runnable() {
//            @Override
//            public void run() {
                if (rows.containsKey(dest) && columns.containsKey(orig)) {

                    innerRows.entrySet().parallelStream().forEach(row -> {
//            for (Map.Entry<Integer, List<Integer>> row : innerRows.entrySet()) {
                        if (row.getKey() != dest && !interception.contains(row.getKey())) {
                            List<Integer> rowValues = new ArrayList<Integer>(row.getValue());
                            for (int rowValue : rowValues) {
                                innerColumns.entrySet().parallelStream().forEach(column -> {
                                    //for (Map.Entry<Integer, PriorityBlockingQueue<Integer>> column : innerColumns.entrySet()) {
                                    if (column.getKey() != orig && !interception.contains(column.getKey())) {
                                        List<Integer> columnValues = new ArrayList<Integer>(column.getValue());
                                        for (int columnValue : columnValues)
                                            insertOnTable(row.getKey(), column.getKey(), rowValue + columnValue + 1);
                                    }
                                });
                            }
                        }
                    });
                }

//            }
//        });

//        executor.execute(new Runnable() {
//            @Override
//            public void run() {
                if(rows.containsKey(dest)) {
                    innerColumns.entrySet().parallelStream().forEach(column -> {
                        //for(Map.Entry<Integer, List<Integer>> column : innerColumns.entrySet()) {
                        if(column.getKey() != orig && !interception.contains(column.getKey())) {
                            PriorityBlockingQueue<Integer> list = column.getValue();
                            for (int value : list)
                                insertOnTable(orig, column.getKey(), value + 1);
                        }
                    });
                }

//            }
//        });

//        executor.execute(new Runnable() {
//            @Override
//            public void run() {
                if(columns.containsKey(orig)) {
                    innerRows.entrySet().parallelStream().forEach(row -> {
                        // for(Map.Entry<Integer, List<Integer>> row : innerRows.entrySet()) {
                        if(row.getKey() != dest && !interception.contains(row.getKey())) {
                            PriorityBlockingQueue<Integer> list = row.getValue();
                            for (int value : list)
                                insertOnTable(row.getKey(), dest, value + 1);
                        }
                    });
                }
//            }
//        });

//        executor.shutdown();
//        try {
//            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // unlock rows and columns
        //generalLock.lock();

        columnLock2.unlock();
        if (innerRowsSet != null)
            for (int row : innerRowsSet)
                if(row != dest)
                    rowLocks.get(row).unlock();
        columnLock.unlock();
        rowLock2.unlock();
        if (innerColumnsSet != null)
            for (int column : innerColumnsSet)
                if(column != orig)
                    columnLocks.get(column).unlock();
        rowLock.unlock();
        //generalLock.unlock();
    }

    private static void removeFromTable(int row, int column) {
        Map<Integer, PriorityBlockingQueue<Integer>> innerColumns = rows.get(row);
        PriorityBlockingQueue<Integer> values = innerColumns.get(column);
        values.remove(0);
        if(values.isEmpty()) {
            innerColumns.remove(column);
            if(innerColumns.isEmpty())
                rows.remove(row);

            Map<Integer, PriorityBlockingQueue<Integer>> innerRows = columns.get(column);
            innerRows.remove(row);
            if(innerRows.isEmpty())
                columns.remove(column);
        }
    }

    private static void processDelete(int orig, int dest) {
        // if edge does not exist
        try {
            if(rows.get(orig).get(dest).peek() != 1)
                return;
        } catch(Exception e) {
            return;
        }

        removeFromTable(orig, dest);

        Set<Integer> interception = new HashSet<Integer>();
        if(columns.containsKey(orig) && rows.containsKey(dest)) {
            Set<Integer> innerRows = columns.get(orig).keySet();
            interception.addAll(innerRows);
            Set<Integer> innerColumns = rows.get(dest).keySet();
            interception.retainAll(innerColumns);

            for (int row : innerRows)
                if (row != dest && !interception.contains(row))
                    for (int column : innerColumns)
                        if (column != orig && !interception.contains(column))
                            removeFromTable(row, column);
        }

        if(columns.containsKey(orig)) {
            Set<Integer> innerRows = columns.get(orig).keySet();
            for(int row : innerRows) {
                if(row != dest && !interception.contains(row))
                    removeFromTable(row, dest);
            }
        }

        if (rows.containsKey(dest)) {
            Set<Integer> innerColumns = rows.get(dest).keySet();
            for (int column : innerColumns)
                if (column != orig && !interception.contains(column))
                    removeFromTable(orig, column);
        }

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
                        elements = s.substring(2).split(" ");
                        processAdd(Integer.parseInt(elements[0]), Integer.parseInt(elements[1]));
                        break;
                    case 'D':
                        elements = s.substring(2).split(" ");
                        processDelete(Integer.parseInt(elements[0]), Integer.parseInt(elements[1]));
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
//        for(Map.Entry<Integer, Map<Integer, List<Integer>>> entry : rows.entrySet())
//            for(Map.Entry<Integer, List<Integer>> entry2 : entry.getValue().entrySet())
//                System.err.println("row: " + entry.getKey() + ", col: " + entry2.getKey() + ", value: " +
//                        entry2.getValue().get(0));



        System.err.println("Reading batches...");
        readBatches();
    }

}
