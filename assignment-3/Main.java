import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.ArrayDeque;
import java.util.PriorityQueue;


public class Main {
    static void execute_command(String command, AdjacencyMatrixGraph<Integer, String> g) {
        String s[] = command.split(" ");
        // System.out.println(g.toString() + "\n-----");

        switch(s[0]) {
            case "ADD_VERTEX": {
                String name = s[1];
                g.addVertex(name);
                break;
            }
            case "REMOVE_VERTEX": {
                String name = s[1];
                g.removeVertex(g.findVertex(name));
                break;
            }
            case "ADD_EDGE": {
                Vertex<String> from = g.findVertex(s[1]); 
                Vertex<String> to = g.findVertex(s[2]);
                int weight = Integer.parseInt(s[3]);
                g.addEdge(from, to, weight);
                break;
            }
            case "REMOVE_EDGE": {
                Edge<Integer, String> e = g.findEdge(s[1], s[2]);
                if (!Objects.isNull(e))
                    g.removeEdge(e);
                break;
            }
            case "HAS_EDGE": {
                Vertex<String> from = g.findVertex(s[1]); 
                Vertex<String> to = g.findVertex(s[2]);
                if (g.hasEdge(from, to))
                    System.out.println("TRUE");
                else
                    System.out.println("FALSE");
                break;
            }
            case "TRANSPOSE": {
                g.transpose();
                break;
            }
            case "IS_ACYCLIC": {
                List<Edge<Integer, String>> cycle = g.isAcyclic();
                
                if (Objects.isNull(cycle))
                    System.out.println("ACYCLIC");
                else {
                    int weight = cycle 
                        .stream()
                        .map(x -> x.weight)
                        .reduce(0, (a, b) -> a + b);
                    
                    System.out.println(String.valueOf(weight) + " " + 
                        cycle
                            .stream()
                            .map(x -> x.from)
                            .map(x -> x.value)
                            .collect(Collectors.joining(" "))
                    );
                }
                break;
            }
        }
    }

    static void firstSolution() throws IOException {
        AdjacencyMatrixGraph<Integer, String> g = new AdjacencyMatrixGraph<>();
        
        // FileReader file = new FileReader(new File("/workspaces/dsa_assignments/assignment-2/in.txt"));
        // BufferedReader reader = new BufferedReader(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        for (String line : reader.lines().collect(Collectors.toList()))
            execute_command(line, g);
        
        reader.close();
    }
    
    static void secondSolution() throws IOException {
        firstSolution(); // basically the same as the first solution
    }

    static void thirdSolution() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = reader.readLine();
        String s[] = line.split(" ");
        
        int n = Integer.parseInt(s[0]);
        int m = Integer.parseInt(s[1]);

        Graph<Integer, Integer> g = new AdjacencyMatrixGraph<>();
        for (int i = 0; i < n; ++i)
            g.addVertex(i);

        HashMap<Edge<Integer, Integer>, Integer> bandwidths = new HashMap<>();
        for (int i = 0; i < m; ++i) {
            line = reader.readLine();
            s = line.split(" ");
            Vertex<Integer> v = g.findVertex(Integer.parseInt(s[0]) - 1);
            Vertex<Integer> u = g.findVertex(Integer.parseInt(s[1]) - 1);
            int l_k = Integer.parseInt(s[2]);
            int b_k = Integer.parseInt(s[3]);
            Edge<Integer, Integer> e = g.addEdge(v, u, l_k);
            bandwidths.put(e, b_k);
        }

        line = reader.readLine();
        s = line.split(" ");
        Vertex<Integer> start = g.findVertex(Integer.parseInt(s[0]) - 1);
        Vertex<Integer> finish = g.findVertex(Integer.parseInt(s[1]) - 1);
        int min_bandwidth = Integer.parseInt(s[2]);
        PriorityQueue<PQPair<Integer, Vertex<Integer>>> pq = new PriorityQueue<>();

        List<Integer> dist = new ArrayList<>(Collections.nCopies(n, Integer.MAX_VALUE));
        List<Boolean> used = new ArrayList<>(Collections.nCopies(n, false));
        List<Vertex<Integer>> prev = new ArrayList<>(Collections.nCopies(n, null));
        dist.set(start.value, 0);
        pq.add(new PQPair<>(1, start));

        while (!pq.isEmpty()) {
            Vertex<Integer> v = pq.poll().second;

            if (used.get(v.value))
                continue;
            
            used.set(v.value, true);
            int cur_dist = dist.get(v.value);
            for (Edge<Integer, Integer> e : g.edgesFrom(v)) {
                if (used.get(e.to.value) || bandwidths.get(e) < min_bandwidth) 
                    continue;
                if (dist.get(e.to.value) > cur_dist + e.weight) {
                    dist.set(e.to.value, cur_dist + e.weight);
                    pq.add(new PQPair<>(cur_dist + e.weight, e.to));
                    prev.set(e.to.value, v);
                }
            }
        }

        if (dist.get(finish.count) == Integer.MAX_VALUE) {
            System.out.println("IMPOSSIBLE");
            return;
        }
        
        List<Vertex<Integer>> path = new ArrayList<>();
        int len = 0;
        int w = Integer.MAX_VALUE;
        for (Vertex<Integer> v = finish; v != start; v = prev.get(v.value)) {
            path.add(v);
            Edge<Integer, Integer> e = g.findEdge(prev.get(v.value).value, v.value);
            len += e.weight;
            w = Integer.min(w, bandwidths.get(e));
        }

        path.add(start);
        Collections.reverse(path);
        System.out.println(String.valueOf(path.size()) + " " + String.valueOf(len) + " " + String.valueOf(w) + "\n" +
            path
                .stream()
                .map(x -> String.valueOf(x.value + 1))
                .collect(Collectors.joining(" "))
        );
    }

    public static void main(String[] args) throws IOException {
        thirdSolution();
    }
}

class PQPair<T1 extends Comparable<T1>, T2> implements Comparable<PQPair<T1, T2>> {
    public T1 first;
    public T2 second;

    public PQPair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    public int compareTo(PQPair<T1, T2> other) {
        return first.compareTo(other.first);
    }
}

class Vertex<V extends Comparable<V>> {
    public V value;
    public int count;

    public Vertex(V value, int count) {
        this.value = value;
        this.count = count;
    }
}

class Edge<K extends Comparable<K>, V extends Comparable<V>> {
    public K weight;
    public Vertex<V> from;
    public Vertex<V> to;

    public Edge(Vertex<V> from, Vertex<V> to, K weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    public Edge<K, V> transpose() {
        return new Edge<K, V>(to, from, weight);
    }
}

interface Graph<K extends Comparable<K>, V extends Comparable<V>> {
    public Vertex<V> addVertex(V value);
    public void removeVertex(Vertex<V> v);
    public Edge<K, V> addEdge(Vertex<V> from, Vertex<V> to, K weight);
    public void removeEdge(Edge<K, V> e);
    public List<Edge<K, V>> edgesFrom(Vertex<V> v);
    public List<Edge<K, V>> edgesTo(Vertex<V> v);
    public Vertex<V> findVertex(V value);
    public Edge<K, V> findEdge(V from_value, V to_value);
    public boolean hasEdge(Vertex<V> v, Vertex<V> u);
}

class AdjacencyMatrixGraph<K extends Comparable<K>, V extends Comparable<V>> implements Graph<K, V> {
    private ArrayList<ArrayList<Edge<K, V>>> matrix;
    private ArrayList<Vertex<V>> vertices;
    private HashMap<V, Vertex<V>> vertexFromValue;
    private int size;

    public AdjacencyMatrixGraph() {
        matrix = new ArrayList<>();
        vertices = new ArrayList<>();
        vertexFromValue = new HashMap<>();
        size = 0;
    }

    public Vertex<V> addVertex(V value) {
        if (vertexFromValue.containsKey(value))
            return vertexFromValue.get(value);
        
        Vertex<V> v = new Vertex<>(value, size);
        size++;
        vertices.add(v);
        vertexFromValue.put(value, v);
        matrix.forEach(x -> x.add(null));
        matrix.add(new ArrayList<>(Collections.nCopies(size, null)));
        return v;
    }

    public void removeVertex(Vertex<V> v) {
        if (!vertexFromValue.containsKey(v.value))
            return;
        
        matrix.forEach(x -> x.remove(v.count));
        matrix.remove(v.count);

        vertices.remove(v.count);
        vertexFromValue.remove(v.value);
        size--;

        for (int i = 0; i < size; ++i) {
            Vertex<V> t = vertices.get(i);
            t.count = i;
            vertexFromValue.put(t.value, t);
        }
    }

    public Edge<K, V> addEdge(Vertex<V> from, Vertex<V> to, K weight) {
        Edge<K, V> e = new Edge<>(from, to, weight);
        matrix.get(from.count).set(to.count, e);
        return e;
    }

    public void removeEdge(Edge<K, V> e) {
        matrix.get(e.from.count).set(e.to.count, null);
    }

    public List<Edge<K, V>> edgesFrom(Vertex<V> v) {
        return matrix
            .get(v.count)
            .stream()
            .filter(x -> !Objects.isNull(x))
            .collect(Collectors.toList());
    }

    public List<Edge<K, V>> edgesTo(Vertex<V> v) {
        return matrix
            .stream()
            .map(x -> x.get(v.count))
            .filter(x -> !Objects.isNull(x))
            .collect(Collectors.toList());
    }

    public Vertex<V> findVertex(V value) {
        return vertexFromValue.get(value);
    }

    public Edge<K, V> findEdge(V from_value, V to_value) {
        return matrix.get(findVertex(from_value).count).get(findVertex(to_value).count);
    }

    public boolean hasEdge(Vertex<V> v, Vertex<V> u) {
        return !Objects.isNull(matrix.get(v.count).get(u.count));
    }

    public void transpose() {
        for (int i = 0; i < size; i++)
            for (int j = 0; j < i; j++) {
                Edge<K, V> e1 = matrix.get(i).get(j);
                Edge<K, V> e2 = matrix.get(j).get(i);

                matrix.get(i).set(j, !Objects.isNull(e2) ? e2.transpose() : null);
                matrix.get(j).set(i, !Objects.isNull(e1) ? e1.transpose() : null);
            }
    }

    private List<Edge<K, V>> reconstructCycle(ArrayDeque<Vertex<V>> current_vertices) {
        List<Edge<K, V>> res = new ArrayList<>();
        
        Vertex<V> end = current_vertices.pollFirst();
        Vertex<V> v = end;
        while (current_vertices.size() > 0) {
            Vertex<V> u = current_vertices.pollFirst();
            res.add(matrix.get(u.count).get(v.count));
            
            if (u.value.equals(end.value)) 
                break;

            v = u;
        }
        
        Collections.reverse(res);
        return res;
    }

    private List<Edge<K, V>> isAcyclic_(Vertex<V> start, List<Boolean> used, ArrayDeque<Vertex<V>> current_vertices, List<Boolean> contains) {
        if (Objects.isNull(start))
            return null;

        if (contains.get(start.count)) {
            current_vertices.addFirst(start);
            return reconstructCycle(current_vertices);
        }

        if (used.get(start.count))
            return null;

        used.set(start.count, true);
        current_vertices.addFirst(start);
        
        for (Edge<K, V> e : edgesFrom(start)) {
            contains.set(start.count, true);
            List<Edge<K, V>> res = isAcyclic_(e.to, used, current_vertices, contains);
            if (!Objects.isNull(res))
                return res;
            contains.set(start.count, false);
        }

        current_vertices.pollFirst(); // remove the start vertex from the stack
        return null;
    }

    // returns null if no cycle was found
    public List<Edge<K, V>> isAcyclic() { // would be better if it was named findCycle
        ArrayList<Boolean> used = new ArrayList<>();
        
        for (int i = 0; i < size; ++i) 
            used.add(false);

        for (Vertex<V> start : vertices) {
            ArrayDeque<Vertex<V>> dq = new ArrayDeque<>();
            ArrayList<Boolean> contains = new ArrayList<>();

            for (int i = 0; i < size; ++i)
                contains.add(false);

            List<Edge<K, V>> res = isAcyclic_(start, used, dq, contains);
            if (!Objects.isNull(res))
                return res;
        }
        
        return null;
    }
}