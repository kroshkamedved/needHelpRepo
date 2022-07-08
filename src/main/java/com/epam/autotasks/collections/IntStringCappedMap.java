package com.epam.autotasks.collections;

import java.util.*;

class IntStringCappedMap extends AbstractMap<Integer, String> {

    private final long capacity;
    private long currentFulfillment;
    private IntStringCappedMap.MyEntry<Integer, String> root;
    private IntStringCappedMap.MyEntry<Integer, String> current;
    private int size;

    public IntStringCappedMap(final long capacity) {
        this.capacity = capacity;
    }

    public long getCapacity() {
        return capacity;
    }

    @Override
    public Set<Entry<Integer, String>> entrySet() {
        return new AbstractSet<>() {
            MyEntry<Integer, String> current;

            @Override
            public Iterator<Entry<Integer, String>> iterator() {
                current = IntStringCappedMap.this.root;
                return new Iterator<>() {
                    @Override
                    public boolean hasNext() {
                        return current.next != null;
                    }

                    @Override
                    public Entry<Integer, String> next() {
                        Entry<Integer, String> entry = current.next;
                        if (entry == null) throw new NoSuchElementException();
                        return entry;
                    }
                };
            }

            @Override
            public int size() {
                return IntStringCappedMap.this.size();
            }
        };
    }

    @Override
    public String get(final Object key) {
        Integer serachKey = (Integer) key;
        MyEntry<Integer, String> currentEntry = root;
        do {
            if (currentEntry.getKey().equals(serachKey))
                return currentEntry.getValue();
        } while ((currentEntry = currentEntry.next) != null);
        return null;
    }


    @Override
    public String put(final Integer key, final String value) {
        if (value.length() > capacity) throw new IllegalArgumentException();
        MyEntry<Integer, String> currentEntry = root;
        while ((currentFulfillment + value.length()) > capacity) changeRoot();
        do {
            if (currentEntry == null) {
                root = new IntStringCappedMap.MyEntry<>(key, value);
                current = root;
                currentFulfillment += value.length();
                size++;
                return null;
            } else if (currentEntry.key.equals(key)) {
                String previous = currentEntry.value;
                currentEntry.value = value;
                currentFulfillment -= previous.length();
                currentFulfillment += value.length();
                size++;
                return previous;
            }
        } while ((currentEntry = currentEntry.next) != null);
        currentFulfillment += value.length();
        current.next = new IntStringCappedMap.MyEntry<>(key, value);
        current = current.next;
        size++;
        return null;
    }

    private void changeRoot() {
        currentFulfillment -= root.value.length();
        root = root.next;
    }

    @Override
    public String remove(final Object key) {

        if (root == null) {
            return null;
        }
        MyEntry<Integer, String> currentEntry;
        MyEntry<Integer, String> previous = root;
        if (root.key.equals((Integer) key)) {
            root = previous.next;
            return previous.value;
        }
        while (previous.next != null) {
            currentEntry = previous.next;
            if (currentEntry.key.equals(key)) {
                previous.next = currentEntry.next;
                return currentEntry.value;
            }
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    private static class MyEntry<K, V> implements Entry<K, V> {
        MyEntry<K, V> next;
        K key;
        V value;

        private MyEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }
    }
}
