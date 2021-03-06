package util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ReverseIterable<T> implements Iterable<T> {
    private final List<T> list;

    public ReverseIterable(T[] values) {
        this(Arrays.asList(values));
    }

    public ReverseIterable(List<T> list) {
        this.list = list;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private final ListIterator<? extends T> iterator = list.listIterator(list.size());

            @Override
            public boolean hasNext() {
                return iterator.hasPrevious();
            }

            @Override
            public T next() {
                return iterator.previous();
            }
        };
    }
}
