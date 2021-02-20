import java.util.List; // need it for the searchRange method 
import java.util.ArrayList; // need it for the return statement in the searchRange method
import java.io.*; // need it for IO

// TEST
// 3
// ADD $4.0 n
// ADD $5.7 a
// LIST $0 $10


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

    public T least() { // return the least element
        if (this.isEmpty()) // check if the list is empty
            throw new IllegalStateException("Cannot call ArraySortedList.least method on an empty list"); // if it is, throw an exception
        return (T) array[0]; // the least element will basically be the first one in the underlying array, so we return it 
    }

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

    public T get(int index) { // return the i-th least element
        checkIndexValidity(index);
        return (T) array[index]; // return the needed element
    }

    public int indexOf(T element) { // return the index of an element (in a sorted sequence)
        for (int i = 0; i < size_; ++i) { // look through all the elements 
            if (array[i].equals(element)) // check if i-th element equals to the one we need to find
                return i; // if it is return the current index
        }
        return -1; // return -1 if element was not found in the list
    }

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
    }

    // NOTE: ArrayList is used in this method just to be able to return an instance of a class that implements the List interface, 
    //       because that's required by the interface provided in the assignment description. Usage of ArrayList does not affect 
    //       the ADT itself anyhow.
    public List<T> searchRange(T from, T to) {
        int position = 0; // position of the current element
        ArrayList<T> res = new ArrayList<T>(); // the list we return as the result
        
        // look for the position to start adding elements to the result list from
        while (position < size_ && ((T) array[position++]).compareTo(from) < 0) {
            System.out.print(array[position - 1].toString() + " ");
            System.out.println(((T) array[position - 1]).compareTo(from));
        }

        // add all the needed elements to the resulting list
        while (position < size_ && ((T) array[position]).compareTo(to) >= 0) { 
            System.out.println(array[position].toString());
            res.add((T) array[position++]);
        }

        return res;
    }

    public int size() { // return the size of the list
        return size_; // just return the value of a private class member
    }

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
}

class Main {
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
                default: { // LIST
                    float price1 = SoldItem.parsePrice(s[1]);
                    float price2 = SoldItem.parsePrice(s[2]);
                    List<SoldItem> items = list.searchRange(new SoldItem(price1, ""), new SoldItem(price2, ""));
                    ArrayList<String> item_strings = new ArrayList();
                    items.forEach(item -> item_strings.add(item.toString()));
                    System.out.println(String.join(", ", item_strings));
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        first_solution();
    }
}
