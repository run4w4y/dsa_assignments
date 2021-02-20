// Author: Vadim Makarov
// University e-mail: va.makarov@innopolis.university

import java.util.List; // need it for the searchRange method 
import java.util.ArrayList; //  need it for the return statement in the searchRange method
import java.io.*; // need it for IO


public class Main {
    // solution to the problem "A. Managing Pawn Shop Items"
    static void first_solution() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int n = Integer.parseInt(reader.readLine());
        ArraySortedList<SoldItem> list = new ArraySortedList();

        for (int i = 0; i < n; ++i) {
            String[] s = reader.readLine().split(" ", 3);
            
            switch (s[0]) {
                case "ADD": {
                    float price = SoldItem.parsePrice(s[1]);
                    String name = s[2];
                    list.add(new SoldItem(price, name));
                    break;
                }
                case "REMOVE": {
                    float price = SoldItem.parsePrice(s[1]);
                    String name = s[2];
                    int index = list.indexOf(new SoldItem(price, name));
                    list.remove(index);
                    break;
                }
                case "LIST": {
                    float price1 = SoldItem.parsePrice(s[1]);
                    float price2 = SoldItem.parsePrice(s[2]);
                    List<SoldItem> items = list.searchRange(new SoldItem(price1, ""), new SoldItem(price2, ""));
                    ArrayList<String> item_strings = new ArrayList();
                    items.forEach(item -> item_strings.add(item.toString()));
                    System.out.println(String.join(", ", item_strings));
                    break;
                }
                default: {
                    throw new IOException(String.format("Got unknown command %s", s[0]));
                }
            }
        }
    }

    static void second_solution() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int n = Integer.parseInt(reader.readLine());
        HashTable<String, HashTable<String, Float>> accounting = new HashTable();

        for (int i = 0; i < n; ++i) {
            String[] s = reader.readLine().split(" ", 5);
            String date = s[0];
            String receipt_id = s[2];
            float price = SoldItem.parsePrice(s[3]);
            
            HashTable<String, Float> existing = accounting.get(date);
            
            if (existing == null) {
                HashTable<String, Float> receipts = new HashTable();
                receipts.put(receipt_id, price);
                accounting.put(date, receipts);
                continue;
            }

            Float receipt = existing.get(receipt_id);
            
            if (receipt == null) {
                existing.put(receipt_id, price);
                continue;
            }

            existing.put(receipt_id, receipt + price);
        }

        List<String> dates = accounting.getKeys();
        for (int i = 0; i < dates.size(); ++i) { 
            HashTable<String, Float> receipts = accounting.get(dates.get(i));
            List<String> ids = receipts.getKeys();
            float sum = 0;
            for (int j = 0; j < ids.size(); ++j)
                sum += receipts.get(ids.get(j));
            
            System.out.println(String.format("%s $%.2f %d", dates.get(i), sum, ids.size()));
        }
    }

    public static void main(String[] args) throws IOException {
        second_solution();
    }
}

// SortedList interface provided in the assignment description
interface SortedList<T extends Comparable<T>> {
    void add(T item); // add a new item to the List
    T least(); // return the least element
    T greatest(); // return the greatest element
    T get(int i); // return the i-th least element
    int indexOf(T item); // return the index of an element (in a sorted sequence)
    void remove(int i); // remove i-th least element from the list
    List<T> searchRange(T from, T to); // find all items between from and to
    int size(); // return the size of the list
    boolean isEmpty(); // return whether the list is empty
}

// my sorted list implementation
class ArraySortedList<T extends Comparable<T>> implements SortedList<T> { // implementation of the SortedList interface
    private static final int DEFAULT_CAPACITY = 1; // default capacity of the underlying array
    private int size_ = 0; // the variable to keep track of the actual size of the list
    private Object array[]; // underlying array

    public ArraySortedList() { // class constructor
        array = new Object[DEFAULT_CAPACITY]; // initialize the underlying array with default capacity
    }

    private void expandCapacity() { // double the capacity of the underlying array when it is not enough
        Object[] new_array = new Object[size_ * 2]; // create a new array with double the previous capacity
        for (int i = 0; i < size_; ++i) { // copy all of the element from the previous array
            new_array[i] = array[i];
        }
        array = new_array; // use array with bigger capacity from now on
    }

    // time coplexity: O(n) where n - size of the list
    public void add(T item) { // add a new item to the list
        if (size_ == array.length) // if we are about to run out of capacity of the array
            expandCapacity(); // we extend the capacity
        
        int position = size_ - 1; // current position where the element is to be placed

        if (position < 0)
            array[0] = item;
        else {
            while (position >= 0 && ((T) item).compareTo((T) array[position]) < 0) // look for the position where the item fits
                array[position + 1] = array[position--]; // move the element on the current position to the next one and decrement the position
            array[position + 1] = item; // put the item into the underlying array
        }
        
        size_++;
    }

    // time complexity: O(1)
    public T least() { // return the least element
        if (this.isEmpty()) // check if the list is empty
            throw new IllegalStateException("Cannot call ArraySortedList.least method on an empty list"); // if it is, throw an exception
        return (T) array[0]; // the least element will basically be the first one in the underlying array, so we return it 
    }

    // time complexity: O(1)
    public T greatest() { // return the greatest element
        if (this.isEmpty()) // check if the list is empty
            throw new IllegalStateException("Cannot call ArraySortedList.greatest method on an empty list"); // if it is, throw an exception
        return (T) array[size_]; // the greatest element will be the last element in the list, so we simply return it
    }

    private void checkIndexValidity(int index) { // checks whether the given index is valid within the context of the list
        if (index < 0 || index >= size_) // check if the index is within the boundaries
            throw new IndexOutOfBoundsException(String.format( // throw an exception if it is not
                "Index %d is invalid within the context of an ArraySortedList with size=%d", index, size_
            ));
    }

    // time complexity: O(1)
    public T get(int index) { // return the i-th least element
        checkIndexValidity(index);
        return (T) array[index]; // return the needed element
    }

    // time complexity: O(n) where n - size of the list
    // NOTE: I'm aware that it can be implemented using binary search, so the time complexity would be O(logn), but O(n) was
    //       enough to solve the problem, so I didn't bother
    public int indexOf(T element) { // return the index of an element (in a sorted sequence)
        for (int i = 0; i < size_; ++i) { // look through all the elements 
            if (((T) array[i]).compareTo(element) == 0) // check if i-th element equals to the one we need to find
                return i; // if it is return the current index
        }
        return -1; // return -1 if element was not found in the list
    }

    // time complexity: O(n) where n - size of the list
    public void remove(int index) { // remove the element at a given index from the list
        checkIndexValidity(index);

        if (index == size_ - 1) { // if the last element in the list is to be removed 
            array[--size_] = null; // we need to do it like this, because the cycle wont get the job done in such case
            return; // job is done, end the method call
        }

        // just shift the elements in the underlying array starting from the one to be removed until the last to the left
        for (int i = index; i < size_ - 1; ++i) { 
            array[i] = array[i + 1];
        }
        array[size_ - 1] = null;
        size_--;
    }

    // NOTE: ArrayList is used in this method just to be able to return an instance of a class that implements the List interface, 
    //       because that's required by the interface provided in the assignment description. Usage of ArrayList does not affect 
    //       the ADT itself anyhow.
    // time complexity: O(n) where n - size of the list
    // NOTE: I believe it can be implemented differently with time complexity of O(logn), but O(n) was enough to solve the problem
    public List<T> searchRange(T from, T to) {
        int position = 0; // position of the current element
        ArrayList<T> res = new ArrayList(); // the list we return as the result
        
        // look for the position to start adding elements to the result list from
        while (position < size_ && ((T) array[position++]).compareTo(from) < 0);
        position--;

        // add all the needed elements to the resulting list
        while (position < size_ && ((T) array[position]).compareTo(to) <= 0)
            res.add((T) array[position++]);

        return res;
    }

    // time complexity: O(1)
    public int size() { // return the size of the list
        return size_; // just return the value of a private class member
    }

    // time complexity: O(1)
    public boolean isEmpty() { // check if the list is empty
        return size_ == 0; // the list will be empty if the size of it is 0
    }
}

class SoldItem implements Comparable<SoldItem> {
    public String name;
    public float price;

    public SoldItem(float price, String name) {
        this.name = name; 
        this.price = price;
    }

    public int compareTo(SoldItem other) {
        if (price < other.price)
            return -1;
        if (price > other.price)
            return 1;
        return 0;
        // return name.compareTo(other.name);
    }

    public static float parsePrice(String s) {
        return Float.parseFloat(s.substring(1));
    }

    public String toString() {
        return String.format("$%.2f %s", price, name);
    }

    public int hashCode() {
        return toString().hashCode();
    }
}

// Map interface required by the assignment description
interface Map<K, V> {
    int size();  // return the amount of key-value pairs in the map
    void put(K key, V value); // add the key-value pair to the map, return true if successful, false otherwise
    V get(K key); // return value by key, if there's no such key in the map, return null
    List<K> getKeys(); // return all the keys in the map
}

// class for usage within HashTable
class MapEntry<K, V> {
    public final K key; // key
    public V value; // value
    public MapEntry<K, V> next; // next entry (needed for collisions) 

    MapEntry(K key, V value, MapEntry<K, V> next) {
        this.key = key;
        this.value = value;
        this.next = next;
    }
}

// my hashtable implementation
class HashTable<K, V> implements Map<K, V> {
    private static final int DEFAULT_CAPACITY = 32768;
    private final int capacity;
    private int size_;
    private MapEntry<K, V> array[];

    HashTable() {
        this(DEFAULT_CAPACITY);
    }

    HashTable(int capacity) {
        this.capacity = capacity;
        array = new MapEntry[capacity];
    }

    int getHash(Object o) {
        int h = o.hashCode();
        return (h ^ (h >>> 16)) & (capacity - 1);
    }

    public V get(K key) {
        int index = getHash(key);
        MapEntry<K, V> entry = array[index];
        
        if (entry == null)
            return null;

        do {
            if (entry.key.equals(key))
                return entry.value;

            entry = entry.next;
        } while (entry != null);
    
        return null;
    }

    public void put(K key, V value) {
        int index = getHash(key);
        MapEntry<K, V> new_entry = new MapEntry(key, value, null);
        MapEntry<K, V> current_entry = array[index];

        if (current_entry == null) {
            array[index] = new_entry;
            size_++;
            return;
        } 

        if (current_entry.next == null) {
            if (current_entry.key.equals(key)) {
                current_entry.value = value;
                return;
            }

            current_entry.next = new_entry;
            size_++;
            return;
        }

        do {
            if (current_entry.key.equals(key)) {
                current_entry.value = value;
                return;
            }

            current_entry = current_entry.next;
        } while (current_entry.next != null);

        current_entry.next = new_entry;
        size_++;
    }

    public int size() {
        return size_;
    }

    public List<K> getKeys() {
        List<K> res = new ArrayList();
        int count = 0;

        for (int i = 0; count < size_; ++i) {
            MapEntry<K, V> entry = array[i];

            if (entry == null)
                continue;
            
            do {
                res.add(entry.key);
                count++;
                entry = entry.next;
            } while (entry != null);
        }

        return res;
    } 
}
