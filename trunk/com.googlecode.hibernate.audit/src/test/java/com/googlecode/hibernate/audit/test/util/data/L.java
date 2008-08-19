package com.googlecode.hibernate.audit.test.util.data;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.ListIterator;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class L
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private List<String> strings;
    private List<A> as;

    // The implementation of returned List<B> is proprietary and hiddent, in that it does not
    // allow instantiation, etc.
    private List<B> bs;

    // Constructors --------------------------------------------------------------------------------

    public L()
    {
        strings = new ArrayList<String>();
        as = new ArrayList<A>();
        bs = new ProprietaryList<B>();
    }

    // Public --------------------------------------------------------------------------------------

    public List<String> getStrings()
    {
        return strings;
    }

    public List<A> getAs()
    {
        return as;
    }

    public List<B> getBs()
    {
        return bs;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

    private class ProprietaryList<T> implements List<T>
    {
        private List<T> delegate;

        private ProprietaryList()
        {
            delegate = new ArrayList<T>();
        }

        public int size()
        {
            return delegate.size();
        }

        public boolean isEmpty()
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        public boolean contains(Object o)
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        public Iterator<T> iterator()
        {
            return delegate.iterator();
        }

        public Object[] toArray()
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        public <T> T[] toArray(T[] a)
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        public boolean add(T o)
        {
            return delegate.add(o);
        }

        public boolean remove(Object o)
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        public boolean containsAll(Collection<?> c)
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        public boolean addAll(Collection<? extends T> c)
        {
            return delegate.addAll(c);
        }

        public boolean addAll(int index, Collection<? extends T> c)
        {
            return delegate.addAll(index, c);
        }

        public boolean removeAll(Collection<?> c)
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        public boolean retainAll(Collection<?> c)
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        public void clear()
        {
            delegate.clear();
        }

        public T get(int index)
        {
            return delegate.get(index);
        }

        public T set(int index, T element)
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        public void add(int index, T element)
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        public T remove(int index)
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        public int indexOf(Object o)
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        public int lastIndexOf(Object o)
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        public ListIterator<T> listIterator()
        {
            return delegate.listIterator();
        }

        public ListIterator<T> listIterator(int index)
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        public List<T> subList(int fromIndex, int toIndex)
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }
    }
}