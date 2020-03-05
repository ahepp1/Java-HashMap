
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Class to make a hash map using linear probing and meta-data.
 * @param <K> key to put into hash map
 * @param <V> value to put into hash map (associated with key)
 */
public class HashMap<K, V> implements Map<K, V> {

    // Basic inner class to store both the key and value
    private class Entry {
        K key;
        V value;

        Entry(K k, V v) {
            this.key = k;
            this.value = v;
        }

        public String toString() {
            return "Entry<key: " + this.key
                    + "; value: " + this.value
                    + ">";
        }

    }

    private byte[] meta;
    private Entry[] data;
    private int size;
    private int capacity;
    private float maxLoad;

    /**
     * Constructor to make an empty Hash Map.
     */
    public HashMap() {
        this.size = 0;
        this.capacity = 16;
        this.meta = new byte[this.capacity];
        //-128 is tag for empty slot in metadata array (10000000 in binary)
        Arrays.fill(this.meta, (byte) -128);
        this.data = (Entry[]) new HashMap.Entry[this.capacity];
        this.maxLoad = 0.70f;
    }

    // Helper function to return last 7 bits of the hash function.
    private int h2(int hash) {
        return hash & 0x7F;
    }

    @Override
    public void insert(K k, V v) throws IllegalArgumentException {
        if (k == null) {
            throw new IllegalArgumentException("Cannot handle null key.");
        }
        if (this.has(k)) {
            throw new IllegalArgumentException("Cannot handle duplicate keys.");
        }
        // If current load is greater than max load, grow the arrays.
        if (((float) this.size / this.capacity) > this.maxLoad) {
            this.grow();
        }
        this.insert(new Entry(k, v));
    }

    private void insert(Entry e) {
        int hash = e.key.hashCode();
        int pos = hash & (this.capacity - 1);
        while (true) {
            // Check in the meta data if slot is empty or tombstone
            if (this.meta[pos] == (byte) -128 || this.meta[pos] == (byte) -2) {
                // If we can insert, update the data and metadata array.
                this.size++;
                this.data[pos] = e;
                this.meta[pos] = (byte) h2(hash);
                return;
            }
            // Otherwise move probe one to the right (looping at end of array).
            pos = (pos + 1) & (this.capacity - 1);
        }

    }

    // Helper function to double the size of the data and metadata arrays.
    private void grow() {
        this.capacity *= 2;
        Entry[] temp = this.data;
        this.data = (Entry[]) new HashMap.Entry[this.capacity];
        this.meta = new byte[this.capacity];
        Arrays.fill(this.meta, (byte) -128);
        this.size = 0;
        // Reinsert every value that is in the data array. Metadata will be
        // automatically updated.
        for (int i = 0; i < temp.length; i++) {
            if (temp[i] != null) {
                this.insert(temp[i]);
            }
        }
    }

    private int find(K k) throws IllegalArgumentException {
        if (k == null) {
            throw new IllegalArgumentException("Cannot handle null key.");
        }
        int hash = k.hashCode();
        int pos = hash & (this.capacity - 1);
        while (true) {
            // Check first in the meta data if the last 7 bits of the
            // computed hash match that stored in the meta data array. If
            // this is true, then check if the key in the data array at that
            // index matches the key we are trying to find.
            if (h2(hash) == this.meta[pos] && k.equals(this.data[pos].key)) {
                return pos;
            }
            // Once we hit an empty slot, return key not found.
            if (this.meta[pos] == (byte) -128) {
                return -1;
            }
            // Increment position to the right (looping at end of array).
            pos = (pos + 1) & (this.capacity - 1);
        }
    }

    @Override
    public V remove(K k) throws IllegalArgumentException {
        if (k == null) {
            throw new IllegalArgumentException("Cannot handle null key.");
        }
        int f = find(k);
        if (f == -1) {
            throw new IllegalArgumentException("Cannot find key.");
        }
        this.size--;
        // Set metadata value to tombstone value. If this function could
        // return void we woudln't have to touch the data array at all.
        this.meta[f] = (byte) -2;
        return this.data[f].value;
    }

    @Override
    public void put(K k, V v) throws IllegalArgumentException {
        if (k == null) {
            throw new IllegalArgumentException("Cannot handle null key.");
        }
        int f = this.find(k);
        if (f == -1) {
            throw new IllegalArgumentException("Key not found.");
        }
        // Updates the data value for slot of key k.
        this.data[f].value = v;
    }

    @Override
    public V get(K k) throws IllegalArgumentException {
        if (k == null) {
            throw new IllegalArgumentException("Cannot handle null key.");
        }
        int f = this.find(k);
        if (f == -1) {
            throw new IllegalArgumentException("Key not found.");
        }
        return this.data[f].value;
    }

    @Override
    public boolean has(K k) {
        return find(k) != -1;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public Iterator<K> iterator() {
        // Just throws all the non-null keys into an ArrayList.
        ArrayList<K> list = new ArrayList<>();
        for (int i = 0; i < this.capacity; i++) {
            if (this.data[i] != null) {
                list.add(this.data[i].key);
            }
        }
        return list.iterator();
    }


    @Override
    public String toString() {
        // Outputs each Entry on a new line.
        StringBuilder s = new StringBuilder();
        for (Entry e: this.data) {
            if (e != null) {
                s.append(e.key);
                s.append(": ");
                s.append(e.value);
                s.append("\n");
            }
        }
        return s.toString();
    }
}
