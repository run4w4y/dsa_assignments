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

    // solution to the problem "B. Accounting for a café"
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

// class used in the first problem solution to represent sold items
class SoldItem implements Comparable<SoldItem> {
    public String name; // name of the item
    public float price; // price of the item

    public SoldItem(float price, String name) { // class constructor
        this.name = name; 
        this.price = price;
    }

    public int compareTo(SoldItem other) { // method required by Comparable
        if (price < other.price) // comparison by price
            return -1;
        if (price > other.price)
            return 1;
        return 0;
    }

    public static float parsePrice(String s) { // static method used to cut off the $ sign and parse the string to float
        return Float.parseFloat(s.substring(1));
    }

    public String toString() { // representation of the item as a string
        return String.format("$%.2f %s", price, name);
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
    public MapEntry<K, V> next; // next entry (it's a linked list, we need to use it because of possible collisions) 

    MapEntry(K key, V value, MapEntry<K, V> next) { // class constuctor, nothing special going on here
        this.key = key;
        this.value = value;
        this.next = next;
    }
}

// hashtable implementation
class HashTable<K, V> implements Map<K, V> {
    private static final int DEFAULT_CAPACITY = 32768; // default capacity of the hashtable
    private final int capacity; // capacity of the hashtable instance
    private int size_; // size (amount of key-value pairs) of the hashtable
    private MapEntry<K, V> array[]; // internal array with entries

    HashTable() { // class constructor using default capacity
        this(DEFAULT_CAPACITY);
    }

    HashTable(int capacity) { // class constructor using capacity given as an argument
        this.capacity = capacity;
        array = new MapEntry[capacity];
    }

    private int getHash(Object o) { // function used to get a valid index in the internal array by hash of an object
        int h = o.hashCode();
        return (h ^ (h >>> 16)) & (capacity - 1);
    }

    // time complexity: same as the put method
    public V get(K key) { // return value by key
        int index = getHash(key);
        MapEntry<K, V> entry = array[index]; // get the first entry at the index we got by hash
        
        if (entry == null) // if there's no entry by this index, there's no such key in the map
            return null; // meaning that we return null

        do { // go through the linked list
            if (entry.key.equals(key)) // check if the linked list node has the same key as the one we are looking for
                return entry.value; // if it does, return the value, we are done 

            entry = entry.next; // if it doesn't, we go on to the next node in the linked list
        } while (entry != null);
    
        // if we got to this point, it means that there was no node found with such a key, meaning that we should return null
        return null;
    }

    // worst case time complexity: O(n)
    // amortized time complexity: O(1) 
    // NOTE: time complexity actually heavily depends on how many collisions there are, what is their distribution 
    //       and what's the ratio size/capacity, but in most cases i believe it should be O(1)
    public void put(K key, V value) { // put a key-value pair in the map
        int index = getHash(key);
        MapEntry<K, V> new_entry = new MapEntry(key, value, null); // create a new entry, corresponding to the key-value pair we got
        MapEntry<K, V> current_entry = array[index]; // first entry by the index we got

        if (current_entry == null) { // if there's no entry with such an index
            array[index] = new_entry; // we just place our new entry there and we are done
            size_++;
            return;
        } 

        // if there acutally is such an entry
        if (current_entry.next == null) { // check if it's the only node in the linked list
            if (current_entry.key.equals(key)) { // if it is, and has the same key
                current_entry.value = value; // we update the value and we are done
                return;
            }

            current_entry.next = new_entry; // if it's got a different key, we place it to the back of the linked list
            size_++;
            return;
        }

        do { // now, if the entry wasn't the only node in the linked list, we go through all of the list nodes
            if (current_entry.key.equals(key)) { // and look for a node with the same key
                current_entry.value = value; // if we find such a node, we update the value and we are done
                return;
            }

            current_entry = current_entry.next;
        } while (current_entry.next != null);

        // if we couldn't find a node with the same key, we just place the new one to the end of the linked list
        current_entry.next = new_entry;
        size_++;
    }

    // time complexity: O(1)
    public int size() { // amount of key value pair in the map
        return size_;
    }

    // NOTE: once again, List interface and ArrayList from java.util are used here, although they do not have any
    //       direct effect on the data structure itself. i'm using it to keep things similar to how they were in the
    //       ArraySortedList data structure I implemented earlier. another reason is that it's a big pain to deal with
    //       arrays of generic parameter types in java, since generic parameters do not exist in the runtime.
    // time complexity: O(n) where n - the number of key-value pairs in the map
    public List<K> getKeys() {
        List<K> res = new ArrayList(); // create the resulting list
        int count = 0; // keep the count of how many keys have we found so far

        for (int i = 0; count < size_; ++i) { // go through the internal array till we find all the keys
            MapEntry<K, V> entry = array[i]; // current entry

            if (entry == null) // if there's no entry at the current index we go on to the next one
                continue;
            
            // if there is an entry 
            do { // go through the entire linked list
                res.add(entry.key); // and add all of the keys to the resulting list
                count++;
                entry = entry.next;
            } while (entry != null);
        }

        return res;
    } 
}
