package com.epam.autotasks.collections;

import java.util.*;

class IntStringCappedMap extends AbstractMap<Integer, String> {

    private static final int DEFAULT_SIZE = 16;
    private final long capacity;
    private int size;
    private int addedQuantity;
    private MyEntry<Integer, String>[] map = new MyEntry[DEFAULT_SIZE];

    private MyEntry<Integer, String>[] orderedArray = new MyEntry[100];
    private int currentFilling;

    public IntStringCappedMap(final long capacity) {
        this.capacity = capacity;
    }

    public long getCapacity() {
        return capacity;
    }

    @Override
    public Set<Entry<Integer, String>> entrySet() {
        return new AbstractSet<>() {
            @Override
            public Iterator<Entry<Integer, String>> iterator() {

                return new Iterator<>() {
                    MyEntry[] set;

                    {
                        set = new MyEntry[size];
                        for (int x = 0, i = 0; i < map.length; i++) {
                            if (map[i] != null) {
                                MyEntry<Integer, String> entry = map[i];
                                while (entry != null) {
                                    set[x] = entry;
                                    x++;
                                    entry = entry.next;
                                }
                            }
                        }
                    }

                    int current = 0;

                    @Override
                    public boolean hasNext() {
                        return current < set.length;
                    }

                    @Override
                    public Entry<Integer, String> next() {
                        if (set[current] == null) return null;
                        MyEntry<Integer, String> next = set[current];
                        current++;
                        return next;
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
        String value = null;
        for (int i = 0; i < map.length; i++) {
            if (map[i] == null) continue;
            if (map[i].key.equals(key)) {
                value = map[i].value;
                break;
            } else {
                var listEntity = map[i];
                while (listEntity.next != null) {
                    listEntity = listEntity.next;
                    if (listEntity.key.equals(key)) {
                        value = listEntity.value;
                        break;
                    }
                }
            }
        }

        return value;
    }

    @Override
    public String put(final Integer key, final String value) {
        if (value == null || capacity < value.length()) throw new IllegalArgumentException();
        MyEntry<Integer, String> newEntry = new MyEntry<>(key, value);

        int position = calcPosition(key);

        if (map[position] == null) {
            map[position] = newEntry;
            currentFilling += value.length();
            while (needMoreSpace(value)) evictOldest();
            orderedArray[addedQuantity] = newEntry;
            addedQuantity++;
            size++;
        } else {
            MyEntry<Integer, String> current = map[position];
            if (current.key.equals(key)) {
                String oldValue = current.value;
                current.value = value;
                currentFilling -= oldValue.length();
                currentFilling += value.length();
                changOrderedPosition(key);
                while (needMoreSpace(value)) evictOldest();
                return oldValue;
            } else if (current.next == null) {
                current.next = newEntry;
                currentFilling += value.length();
            } else {
                while (current.next != null) {

                    if (current.next.key.equals(key)) {
                        String oldValue = current.next.value;
                        current.next.value = value;
                        currentFilling -= oldValue.length();
                        currentFilling += value.length();
                        changOrderedPosition(key);
                        while (needMoreSpace(value)) evictOldest();
                        return oldValue;
                    }
                    current = current.next;
                    if (current.next == null) {
                        current.next = newEntry;
                        currentFilling += value.length();
                        orderedArray[addedQuantity] = newEntry;
                        addedQuantity++;
                        size++;
                        return null;
                    }
                }
            }
        }
        return null;
    }

    private void changOrderedPosition(Integer key) {
        for (int i = 0; i < addedQuantity; i++) {
            if (orderedArray[i] == null) continue;
            if (orderedArray[i].key.equals(key)) {
                MyEntry<Integer, String>[] tmp = new MyEntry[orderedArray.length];
                System.arraycopy(orderedArray, 0, tmp, 0, i);
                System.arraycopy(orderedArray, i + 1, tmp, i, addedQuantity - i);
                tmp[addedQuantity - 1] = orderedArray[i];
                orderedArray = tmp;
                return;
            }
        }
    }

    private void removeFromOrdered(Integer key) {
        for (int i = 0; i < addedQuantity; i++) {
            if (orderedArray[i].key.equals(key)) {
                MyEntry<Integer, String>[] tmp = new MyEntry[orderedArray.length];
                System.arraycopy(orderedArray, 0, tmp, 0, i);
                System.arraycopy(orderedArray, i + 1, tmp, i, addedQuantity - i);
                orderedArray = tmp;
                return;
            }
        }
    }

    private void evictOldest() {
        MyEntry<Integer, String> entry = orderedArray[0];
        for (int i = 0; i < orderedArray.length; i++) {
            if (orderedArray[i] != null) {
                entry = orderedArray[i];
                orderedArray[i] = null;
                MyEntry<Integer, String>[] tmp = new MyEntry[orderedArray.length];
                System.arraycopy(orderedArray, 0, tmp, 0, i);
                System.arraycopy(orderedArray, i + 1, tmp, i, addedQuantity - i);
                orderedArray = tmp;
                break;
            }
        }
        String value = "";
        for (int i = 0; i < map.length; i++) {
            if (map[i] == null) continue;
            if (map[i].key.equals(entry.key)) {
                if (map[i].next != null) {
                    value = map[i].value;
                    map[i] = map[i].next;
                } else {
                    value = map[i].value;
                    map[i] = null;
                }


                currentFilling -= value.length();
                size--;
                addedQuantity--;
                break;
            }
        }
    }

    private boolean needMoreSpace(String value) {
        return currentFilling > capacity;
    }

    private int calcPosition(Integer key) {
        return key.hashCode() % map.length;
    }

    @Override
    public String remove(final Object key) {
        if (!(key instanceof Integer)) throw new IllegalArgumentException();

        String removedValue = null;
        int position = calcPosition((Integer) key);
        var element = map[position];
        MyEntry<Integer, String> elementNext;
        if (map[position] == null) return null;
        if (element.key.equals(key)) {
            removedValue = map[position].value;
            map[position] = element.next;
        } else {
            do {
                elementNext = element.next;

                if (elementNext.key.equals(key)) {
                    removedValue = elementNext.value;
                    element.next = elementNext.next;
                    break;
                }
            } while ((element = element.next) != null);
        }
        removeFromOrdered((Integer) key);
        currentFilling -= removedValue.length();
        size--;
        addedQuantity--;

        return removedValue;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    private static class MyEntry<Integer, String> implements Entry<Integer, String> {

        Integer key;
        String value;

        MyEntry<Integer, String> next;

        public MyEntry(Integer key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public Integer getKey() {
            return key;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String setValue(String value) {
            String old = value;
            this.value = value;
            return old;
        }
    }


}
