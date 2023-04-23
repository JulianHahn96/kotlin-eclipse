package com.intellij.util.containers;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.*;
import com.intellij.util.*;
import gnu.trove.THashSet;
import gnu.trove.TObjectHashingStrategy;
import org.jetbrains.annotations.*;
import java.util.HashSet;

import java.util.WeakHashMap;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SuppressWarnings("ALL")
public final class ContainerUtil {
  private static final int INSERTION_SORT_THRESHOLD = 10;

  @SafeVarargs
  @Contract(pure=true)
  public static <T> T  [] ar(T  ... elements) {
    return elements;
  }

  /**
   * @deprecated Use {@link HashMap#HashMap()}
   */
  @Contract(pure = true)
  @Deprecated
  public static  <K, V> HashMap<K, V> newHashMap() {
    return new HashMap<>();
  }

  /**
   * @deprecated use {@link Map#of()} or {@link Map#ofEntries(Map.Entry[])} instead
   */
  @SafeVarargs
  @Contract(pure = true)
  @Deprecated
  public static  <K, V> Map<K, V> newHashMap( Pair<? extends K, ? extends V> first, Pair<? extends K,? extends V>  ... entries) {
    Map<K, V> map = new HashMap<>(entries.length + 1);
    map.put(first.getFirst(), first.getSecond());
    for (Pair<? extends K, ? extends V> entry : entries) {
      map.put(entry.getFirst(), entry.getSecond());
    }
    return map;
  }

  @Contract(pure = true)
  public static  <K, V> Map<K, V> newHashMap( List<? extends K> keys,  List<? extends V> values) {
    if (keys.size() != values.size()) {
      throw new IllegalArgumentException(keys + " must have same length as " + values);
    }

    Map<K, V> map = new HashMap<>(keys.size());
    for (int i = 0; i < keys.size(); ++i) {
      map.put(keys.get(i), values.get(i));
    }
    return map;
  }

  /**
   * @deprecated Use {@link LinkedHashMap#LinkedHashMap()}
   */
  @Contract(pure = true)
  @Deprecated
  public static  <K, V> LinkedHashMap<K, V> newLinkedHashMap() {
    return new LinkedHashMap<>();
  }

  @SafeVarargs
  @Contract(pure = true)
  public static  <K, V> LinkedHashMap<K, V> newLinkedHashMap( Pair<? extends K, ? extends V> first, Pair<? extends K,? extends V>  ... entries) {
    LinkedHashMap<K, V> map = new LinkedHashMap<>();
    map.put(first.getFirst(), first.getSecond());
    for (Pair<? extends K, ? extends V> entry : entries) {
      map.put(entry.getFirst(), entry.getSecond());
    }
    return map;
  }

  /**
   * @deprecated Use {@link LinkedList#LinkedList()}<br>
   *
   * DO NOT REMOVE this method until {@link ContainerUtil#newLinkedList(Object[])} is removed.
   * The former method is here to highlight incorrect usages of the latter.
   */
  @Contract(pure = true)
  @Deprecated
  public static  <T> LinkedList<T> newLinkedList() {
    return new LinkedList<>();
  }

  @SafeVarargs
  @Contract(pure = true)
  public static  <T> LinkedList<T> newLinkedList(T  ... elements) {
    LinkedList<T> list = new LinkedList<>();
    Collections.addAll(list, elements);
    return list;
  }

  /**
   * @deprecated Use {@link ArrayList#ArrayList()}<br>
   *
   * DO NOT REMOVE this method until {@link ContainerUtil#newArrayList(Object[])} is removed.
   * The former method is here to highlight incorrect usages of the latter.
   */
  @Deprecated
  @Contract(pure = true)
  public static  <T> ArrayList<T> newArrayList() {
    return new ArrayList<>();
  }

  /**
   * @deprecated Use more immutable {@link List#of(Object[])} or {@link Arrays#asList(Object[])} instead
   */
  @SafeVarargs
  @Contract(pure = true)
  @Deprecated
  public static  <E> ArrayList<E> newArrayList(E  ... array) {
    return new ArrayList<>(Arrays.asList(array));
  }

  /**
   * @deprecated Use {@link ArrayList#ArrayList(Collection)} instead<br>
   *
   * DO NOT REMOVE this method until {@link ContainerUtil#newArrayList(Iterable)} is removed.
   * The former method is here to highlight incorrect usages of the latter.
   */
  @Deprecated
  @Contract(pure = true)
  public static  <E> ArrayList<E> newArrayList( Collection<? extends E> iterable) {
    Logger.getInstance(ContainerUtil.class).error("Use `new ArrayList(Collection)` instead. "+iterable.getClass());
    return new ArrayList<>(iterable);
  }

  @Contract(pure = true)
  public static  <E> ArrayList<E> newArrayList( Iterable<? extends E> iterable) {
    ArrayList<E> collection = new ArrayList<>();
    for (E element : iterable) {
      collection.add(element);
    }
    return collection;
  }

  /**
   * @deprecated Use {@link ArrayList#ArrayList(int)}
   */
  @Contract(pure = true)
  @Deprecated
  public static  <T> ArrayList<T> newArrayListWithCapacity(int size) {
    return new ArrayList<>(size);
  }

  /**
   * create an immutable {@link List} with elements from {@code elements[start]...elements[end-1]} sub-array.
   */
  @Contract(pure = true)
  
  public static  <T> List<T> subArrayAsList(T  [] elements, int start, int end) {
    if (start < 0 || start > end || end > elements.length) {
      throw new IllegalArgumentException("start:" + start + " end:" + end + " length:" + elements.length);
    }

    return new AbstractList<T>() {
      @Override
      public T get(int index) {
        if (index < 0 || index >= end - start) throw new IndexOutOfBoundsException("index:" + index + " size:" + (end - start));
        return elements[start + index];
      }

      @Override
      public int size() {
        return end - start;
      }
    };
  }

  /**
   * @return read-only list consisting of the elements from the input collection
   * @deprecated use {@link List#copyOf(Collection)}
   */
  @Deprecated
  @Contract(pure = true)
  
  public static  <T> List<T> newUnmodifiableList( List<? extends T> originalList) {
    int size = originalList.size();
    if (size == 0) {
      return emptyList();
    }
    if (size == 1) {
      return Collections.singletonList(originalList.get(0));
    }
    return Collections.unmodifiableList(new ArrayList<>(originalList));
  }

  /**
   * @return read-only list consisting of the elements from the input collection (or {@link Collections#emptyList()} when the original list is empty)
   */
  @Contract(pure = true)
  
  public static  <T> List<T> unmodifiableOrEmptyList( List<? extends T> original) {
    int size = original.size();
    if (size == 0) {
      return emptyList();
    }
    return Collections.unmodifiableList(original);
  }

  /**
   * @return read-only set consisting of the elements from the input collection (or {@link Collections#emptySet()} when the original collection is empty)
   */
  @Contract(pure = true)
  
  public static  <T> Set<T> unmodifiableOrEmptySet( Set<? extends T> original) {
    int size = original.size();
    if (size == 0) {
      return Collections.emptySet();
    }
    return Collections.unmodifiableSet(original);
  }

  /**
   * @return read-only map consisting of the elements from the input collection (or {@link Collections#emptyMap()} when the original collection is empty)
   */
  @Contract(pure = true)
  
  public static  <K,V> Map<K,V> unmodifiableOrEmptyMap( Map<? extends K, ? extends V> original) {
    int size = original.size();
    if (size == 0) {
      return Collections.emptyMap();
    }
    return Collections.unmodifiableMap(original);
  }

  /**
   * @deprecated Use {@link SmartList()}
   */
  @Deprecated
  public static  <T> List<T> newSmartList() {
    return new SmartList<>();
  }

  /**
   * @deprecated Use {@link HashSet#HashSet()}<br>
   *
   * DO NOT REMOVE this method until {@link ContainerUtil#newHashSet(Object[])} is removed.
   * The former method is here to highlight incorrect usages of the latter.
   */
  @Contract(pure = true)
  @Deprecated
  public static  <T> HashSet<T> newHashSet() {
    return new HashSet<>();
  }

  @SafeVarargs
  @Contract(pure = true)
  public static  <T> HashSet<T> newHashSet(T  ... elements) {
    //noinspection SSBasedInspection
    return new HashSet<>(Arrays.asList(elements));
  }

  @Contract(pure = true)
  public static  <T> HashSet<T> newHashSet( Iterable<? extends T> iterable) {
    HashSet<T> set = new HashSet<>();
    for (T t : iterable) {
      set.add(t);
    }
    return set;
  }

  /**
   * @deprecated use {@link HashSet#HashSet(Collection)}<br>
   *
   * DO NOT remove this method until {@link #newHashSet(Iterable)} is removed
   * The former method is here to highlight incorrect usages of the latter.
   */
  @Deprecated
  @Contract(pure = true)
  public static  <T> HashSet<T> newHashSet( Collection<? extends T> iterable) {
    Logger.getInstance(ContainerUtil.class).error("use HashSet#HashSet(Collection) instead");
    return new HashSet<>(iterable);
  }

  public static  <T> HashSet<T> newHashSet( Iterator<? extends T> iterator) {
    HashSet<T> set = new HashSet<>();
    while (iterator.hasNext()) set.add(iterator.next());
    return set;
  }

  /**
   * @deprecated use {@link #unmodifiableOrEmptySet(Set)} instead
   */
  @Contract(pure = true)
  
  @Deprecated
  public static  <T> Set<T> newHashOrEmptySet( Iterable<? extends T> iterable) {
    boolean isEmpty = iterable == null || iterable instanceof Collection && ((Collection<?>)iterable).isEmpty();
    if (isEmpty) {
      return Collections.emptySet();
    }

    return newHashSet(iterable);
  }

  /**
   * @deprecated Use {@link LinkedHashSet#LinkedHashSet()}<br>
   *
   * DO NOT REMOVE this method until {@link ContainerUtil#newLinkedHashSet(Object[])} is removed.
   * The former method is here to highlight incorrect usages of the latter.
   */
  @Contract(pure = true)
  @Deprecated
  public static  <T> LinkedHashSet<T> newLinkedHashSet() {
    return new LinkedHashSet<>();
  }

  @Contract(pure = true)
  public static  <T> LinkedHashSet<T> newLinkedHashSet( Iterable<? extends T> elements) {
    LinkedHashSet<T> collection = new LinkedHashSet<>();
    for (T element : elements) {
      collection.add(element);
    }
    return collection;
  }

  /**
   * @deprecated use {@link LinkedHashSet#LinkedHashSet(Collection)}<br>
   *
   * DO NOT remove this method until {@link #newLinkedHashSet(Iterable)} is removed
   * The former method is here to highlight incorrect usages of the latter.
   */
  @Deprecated
  @Contract(pure = true)
  public static  <T> LinkedHashSet<T> newLinkedHashSet( Collection<? extends T> iterable) {
    Logger.getInstance(ContainerUtil.class).error("use LinkedHashSet#LinkedHashSet(Collection) instead");
    return new LinkedHashSet<>(iterable);
  }

  @SafeVarargs
  @Contract(pure = true)
  public static  <T> LinkedHashSet<T> newLinkedHashSet(T  ... elements) {
    return new LinkedHashSet<>(Arrays.asList(elements));
  }

  /**
   * @deprecated Use {@link HashSet#HashSet()}
   */
  @Deprecated
  @Contract(pure = true)
  public static  <T> THashSet<T> newTroveSet() {
    return new THashSet<>();
  }

  @Contract(pure = true)
  public static  <T> Set<T> newConcurrentSet() {
    //noinspection SSBasedInspection
    return Collections.newSetFromMap(new ConcurrentHashMap<>());
  }

  /**
   * @deprecated Use {@link ConcurrentHashMap#ConcurrentHashMap()}
   */
  @Deprecated
  @Contract(pure = true)
  public static  <K, V> ConcurrentMap<K, V> newConcurrentMap() {
    return new ConcurrentHashMap<>();
  }

  /**
   * @return read-only list consisting of the elements from the input collection in reverse order
   */
  @Contract(pure = true)
  
  public static  <E> List<E> reverse( List<? extends E> elements) {
    if (elements.isEmpty()) {
      return emptyList();
    }

    return new AbstractList<E>() {
      @Override
      public E get(int index) {
        return elements.get(elements.size() - 1 - index);
      }

      @Override
      public int size() {
        return elements.size();
      }
    };
  }

  @Contract(pure = true)
  
  public static  <K, V> Map<K, V> union( Map<? extends K, ? extends V> map,  Map<? extends K, ? extends V> map2) {
    Map<K, V> result = new HashMap<>(map.size() + map2.size());
    result.putAll(map);
    result.putAll(map2);
    return result;
  }

  @Contract(pure = true)
  
  public static  <T> Set<T> union( Set<? extends T> set,  Set<? extends T> set2) {
    return union((Collection<? extends T>)set, set2);
  }

  @Contract(pure = true)
  
  public static  <T> Set<T> union( Collection<? extends T> set,  Collection<? extends T> set2) {
    Set<T> result = new HashSet<>(set.size() + set2.size());
    result.addAll(set);
    result.addAll(set2);
    return result;
  }

  /**
   * @return read-only set consisting of the elements from the input collection
   * @deprecated use {@link Set#of(Object[])}
   */
  @SafeVarargs
  @Contract(pure = true)
  
  @Deprecated
  public static  <E> Set<E> immutableSet(E  ... elements) {
    switch (elements.length) {
      case 0:
        return Collections.emptySet();
      case 1:
        return Collections.singleton(elements[0]);
      default:
        //noinspection SSBasedInspection
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(elements)));
    }
  }

  /**
   * @deprecated use {@link List#of} or {@link Collections#unmodifiableList(List)}
   * @return unmodifiable list (in which mutation methods throw {@link UnsupportedOperationException}) which contains elements from {@code array}.
   * When contents of {@code array} changes (e.g. via {@code array[0] = null}), this collection contents changes accordingly.
   * This collection doesn't contain {@link Collections.UnmodifiableList#list} and {@link Collections.UnmodifiableCollection#c} fields,
   * unlike the {@link Collections#unmodifiableList(List)} (Subject to change in subsequent JDKs).
   */
  @SafeVarargs
  @Contract(pure = true)
  
  @Deprecated
  public static  <E> ImmutableList<E> immutableList(E  ... array) {
    return new ImmutableListBackedByArray<>(array);
  }

  /**
   * @deprecated use {@link Collections#emptyList()}<br>
   *
   * DO NOT REMOVE this method until {@link ContainerUtil#immutableList(Object[])} is removed.
   * The former method is here to highlight incorrect usages of the latter.
   */
  @Contract(pure = true)
  @Deprecated
  
  public static  <E> List<E> immutableList() {
    return Collections.emptyList();
  }

  /**
   * @deprecated use more standard/memory-conscious alternatives {@link List#of(Object)} or {@link Collections#singletonList(Object)} instead.<br>
   *
   * DO NOT REMOVE this method until {@link ContainerUtil#immutableList(Object[])} is removed.
   * The former method is here to highlight incorrect usages of the latter.
   */
  @Contract(pure = true)
  @Deprecated
  
  public static  <E> List<E> immutableList(E element) {
    return Collections.singletonList(element);
  }

  /**
   * @return unmodifiable list (mutation methods throw UnsupportedOperationException) which contains {@code element}.
   * This collection doesn't contain {@code modCount} field, unlike the {@link Collections#singletonList(Object)}, so it might be useful in extremely space-conscious places.
   * @deprecated prefer {@link Collections#singletonList(Object)} or {@link List#of(Object)}.
   */
  @Contract(pure = true)
  
  @Deprecated
  public static  <E> ImmutableList<E> immutableSingletonList(E element) {
    return ImmutableList.singleton(element);
  }

  /**
   * @deprecated use {@link List#copyOf(Collection)} or {@link Collections#unmodifiableList(List)}
   * @return unmodifiable list (mutation methods throw UnsupportedOperationException) which contains {@code list} elements.
   * When contents of {@code list} changes (e.g. via {@code list.set(0, null)}), this collection contents changes accordingly.
   * This collection doesn't contain {@link Collections.UnmodifiableList#list} and {@link Collections.UnmodifiableCollection#c} fields,
   * unlike the {@link Collections#unmodifiableList(List)} (Subject to change in subsequent JDKs).
   */
  @Contract(pure = true)
  
  @Deprecated
  public static  <E> ImmutableList<E> immutableList( List<? extends E> list) {
    //noinspection unchecked
    return list instanceof ImmutableList ? (ImmutableList<E>)list : new ImmutableListBackedByList<>(list);
  }

  /**
   * @deprecated Use {@link Map#of}
   */
  @Deprecated
  @Contract(pure = true)
  public static  <K, V> ImmutableMapBuilder<K, V> immutableMapBuilder() {
    return new ImmutableMapBuilder<>();
  }

  @Contract(pure = true)
  public static  <K, V> MultiMap<K, V> groupBy( Iterable<? extends V> collection,  NullableFunction<? super V, ? extends K> grouper) {
    MultiMap<K, V> result = MultiMap.createLinked();
    for (V data : collection) {
      K key = grouper.fun(data);
      if (key == null) {
        continue;
      }
      result.putValue(key, data);
    }

    if (!result.isEmpty() && result.keySet().iterator().next() instanceof Comparable) {
      //noinspection unchecked,rawtypes
      return new KeyOrderedMultiMap(result);
    }
    return result;
  }

  @Contract(pure = true)
  public static <T> T getOrElse( List<? extends T> elements, int i, T defaultValue) {
    return elements.size() > i ? elements.get(i) : defaultValue;
  }

  /**
   * @deprecated Use {@link Map#of}
   */
  @Deprecated
  public static final class ImmutableMapBuilder<K, V> {
    private final Map<K, V> myMap = new HashMap<>();

    public  ImmutableMapBuilder<K, V> put(K key, V value) {
      myMap.put(key, value);
      return this;
    }
    public  ImmutableMapBuilder<K, V> putAll( Map<? extends K, ? extends V> fromMap) {
      myMap.putAll(fromMap);
      return this;
    }

    /**
     * @return read-only map consisting of the elements passed to {@link #put(Object, Object)} methods
     */
    @Contract(pure = true)
    
    public  Map<K, V> build() {
      return Collections.unmodifiableMap(myMap);
    }
  }

  
  @Deprecated
  private static final class ImmutableListBackedByList<E> extends ImmutableList<E> {
    private final List<? extends E> myStore;

    private ImmutableListBackedByList( List<? extends E> list) {
      myStore = list;
    }

    @Override
    public E get(int index) {
      return myStore.get(index);
    }

    @Override
    public int size() {
      return myStore.size();
    }

    @Override
    public void forEach(Consumer<? super E> action) {
      myStore.forEach(action);
    }
  }

  
  @Deprecated
  private static final class ImmutableListBackedByArray<E> extends ImmutableList<E> {
    private final E[] myStore;

    private ImmutableListBackedByArray(E  [] array) {
      myStore = array;
    }

    @Override
    public E get(int index) {
      return myStore[index];
    }

    @Override
    public int size() {
      return myStore.length;
    }

    // overridden for more efficient arraycopy vs. iterator() creation/traversing
    @Override
    public <T> T  [] toArray(T  [] a) {
      int size = size();
      T[] result = a.length >= size ? a : ArrayUtil.newArray(ArrayUtil.getComponentType(a), size);
      //noinspection SuspiciousSystemArraycopy
      System.arraycopy(myStore, 0, result, 0, size);
      if (result.length > size) {
        result[size] = null;
      }
      return result;
    }

    @Override
    public void forEach(Consumer<? super E> action) {
      //noinspection ForLoopReplaceableByForEach
      for (int i = 0, length = myStore.length; i < length; i++) {
        action.accept(myStore[i]);
      }
    }
  }

  /**
   * @return read-only map consisting of the elements containing in the both input collections
   */
  @Contract(pure = true)
  
  public static  <K, V> Map<K, V> intersection( Map<? extends K, ? extends V> map1,  Map<? extends K, ? extends V> map2) {
    if (map1.isEmpty() || map2.isEmpty()) return Collections.emptyMap();

    if (map2.size() < map1.size()) {
      Map<? extends K, ? extends V> t = map1;
      map1 = map2;
      map2 = t;
    }
    Map<K, V> res = new HashMap<>(map1);
    for (Map.Entry<? extends K, ? extends V> entry : map1.entrySet()) {
      K key = entry.getKey();
      V v1 = entry.getValue();
      V v2 = map2.get(key);
      if (!Objects.equals(v1, v2)) {
        res.remove(key);
      }
    }
    return res;
  }

  @Contract(pure = true)
  public static  <K, V> Map<K,Couple<V>> diff( Map<? extends K, ? extends V> map1,  Map<? extends K, ? extends V> map2) {
    Set<K> keys = union(map1.keySet(), map2.keySet());
    Map<K, Couple<V>> res = new HashMap<>();
    for (K k : keys) {
      V v1 = map1.get(k);
      V v2 = map2.get(k);
      if (!Objects.equals(v1, v2)) {
        res.put(k, Couple.of(v1, v2));
      }
    }
    return res;
  }

  /**
   * Process both sorted lists in order defined by {@code comparator}, call {@code processor} for each element in the merged list result.
   * When equal elements occurred, then if {@code mergeEqualItems} then output only the element from the {@code list1} and ignore the second,
   * else output them both in unspecified order.
   * {@code processor} is invoked for each (output element, is the element from {@code list1}) pair.
   * Both {@code list1} and {@code list2} must be sorted according to {@code comparator}
   */
  public static <T> void processSortedListsInOrder( List<? extends T> list1,
                                                    List<? extends T> list2,
                                                    Comparator<? super T> comparator,
                                                   boolean mergeEqualItems,
                                                   // (`element in the result`, `is the element from the list1`)
                                                    PairConsumer<? super T, ? super Boolean> processor) {
    int index1 = 0;
    int index2 = 0;
    while (index1 < list1.size() || index2 < list2.size()) {
      T e;
      if (index1 >= list1.size()) {
        e = list2.get(index2++);
        processor.consume(e, false);
      }
      else if (index2 >= list2.size()) {
        e = list1.get(index1++);
        processor.consume(e, true);
      }
      else {
        T element1 = list1.get(index1);
        T element2 = list2.get(index2);
        int c = comparator.compare(element1, element2);
        if (c == 0) {
          index1++;
          index2++;
          if (mergeEqualItems) {
            e = element1;
            processor.consume(e, true);
          }
          else {
            processor.consume(element1, true);
            e = element2;
            processor.consume(e, false);
          }
        }
        else if (c < 0) {
          e = element1;
          index1++;
          processor.consume(e, true);
        }
        else {
          e = element2;
          index2++;
          processor.consume(e, false);
        }
      }
    }
  }

  @Contract(pure = true)
  
  public static  <T> List<T> mergeSortedLists( List<? extends T> list1,
                                                       List<? extends T> list2,
                                                       Comparator<? super T> comparator,
                                                      boolean mergeEqualItems) {
    List<T> result = new ArrayList<>(list1.size() + list2.size());
    processSortedListsInOrder(list1, list2, comparator, mergeEqualItems, (t, __) -> result.add(t));
    return result;
  }

  @Contract(pure = true)
  public static  <T> List<T> subList( List<T> list, int from) {
    return list.subList(from, list.size());
  }

  
  public static <T> void addAll( Collection<? super T> collection,  Iterable<? extends T> appendix) {
    addAll(collection, appendix.iterator());
  }

  
  public static <T> void addAll( Collection<? super T> collection,  Iterator<? extends T> iterator) {
    while (iterator.hasNext()) {
      T o = iterator.next();
      collection.add(o);
    }
  }

  /**
   * Adds all not-null elements from the {@code elements}, ignoring nulls
   */
  
  public static <T> void addAllNotNull( Collection<? super T> collection,  Iterable<? extends T> elements) {
    addAllNotNull(collection, elements.iterator());
  }

  /**
   * Adds all not-null elements from the {@code elements}, ignoring nulls
   */
  
  public static <T> void addAllNotNull( Collection<? super T> collection,  Iterator<? extends T> elements) {
    while (elements.hasNext()) {
      T o = elements.next();
      if (o != null) {
        collection.add(o);
      }
    }
  }

  @Contract(pure = true)
  public static  <K, V> Map<K, V> newMapFromKeys( Iterator<? extends K> keys,  Convertor<? super K, ? extends V> valueConvertor) {
    Map<K, V> map = new HashMap<>();
    while (keys.hasNext()) {
      K key = keys.next();
      map.put(key, valueConvertor.convert(key));
    }
    return map;
  }

  @Contract(pure = true)
  public static  <K, V> Map<K, V> newMapFromValues( Iterator<? extends V> values,  Convertor<? super V, ? extends K> keyConvertor) {
    Map<K, V> map = new HashMap<>();
    fillMapWithValues(map, values, keyConvertor);
    return map;
  }

  
  public static <K, V> void fillMapWithValues( Map<? super K, ? super V> map,
                                               Iterator<? extends V> values,
                                               Convertor<? super V, ? extends K> keyConvertor) {
    while (values.hasNext()) {
      V value = values.next();
      map.put(keyConvertor.convert(value), value);
    }
  }

  @Contract(pure = true)
  public static  <K, V> Map<K, Set<V>> classify( Iterator<? extends V> iterator,  Convertor<? super V, ? extends K> keyConvertor) {
    Map<K, Set<V>> hashMap = new LinkedHashMap<>();
    while (iterator.hasNext()) {
      V value = iterator.next();
      K key = keyConvertor.convert(value);
      // ordered set!!
      Set<V> set = hashMap.computeIfAbsent(key, __ -> new LinkedHashSet<>());
      set.add(value);
    }
    return hashMap;
  }

  @Contract(pure=true)
  public static <T> T find(T  [] array,  Condition<? super T> condition) {
    for (T element : array) {
      if (condition.value(element)) return element;
    }
    return null;
  }

  public static <T> boolean process( Iterable<? extends T> iterable,  Processor<? super T> processor) {
    for (T t : iterable) {
      if (!processor.process(t)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Call {@code processor} on each element of {@code list} sequentially
   * @return true if all {@link Processor#process(Object)} returned true; false otherwise
   */
  public static <T> boolean process( List<? extends T> list,  Processor<? super T> processor) {
    //noinspection ForLoopReplaceableByForEach
    for (int i = 0, size = list.size(); i < size; i++) {
      T t = list.get(i);
      if (!processor.process(t)) {
        return false;
      }
    }
    return true;
  }

  public static <T> boolean process(T  [] iterable,  Processor<? super T> processor) {
    for (T t : iterable) {
      if (!processor.process(t)) {
        return false;
      }
    }
    return true;
  }

  public static <T> boolean process( Iterator<? extends T> iterator,  Processor<? super T> processor) {
    while (iterator.hasNext()) {
      if (!processor.process(iterator.next())) {
        return false;
      }
    }
    return true;
  }

  @Contract(pure=true)
  public static <T>  T find( Iterable<? extends T> iterable,  Condition<? super T> condition) {
    return find(iterable.iterator(), condition);
  }

  @Contract(pure=true)
  public static <T>  T find( Iterable<? extends T> iterable,  T equalTo) {
    return find(iterable, (Condition<T>)object -> equalTo == object || equalTo.equals(object));
  }

  @Contract(pure=true)
  public static <T>  T find( Iterator<? extends T> iterator,  T equalTo) {
    return find(iterator, (Condition<T>)object -> equalTo == object || equalTo.equals(object));
  }

  public static <T>  T find( Iterator<? extends T> iterator,  Condition<? super T> condition) {
    while (iterator.hasNext()) {
      T value = iterator.next();
      if (condition.value(value)) return value;
    }
    return null;
  }

  @Contract(pure = true)
  public static <T>  T findLast( List<? extends T> list,  Condition<? super T> condition) {
    int index = lastIndexOf(list, condition);
    if (index < 0) return null;
    return list.get(index);
  }

  @Contract(pure = true)
  public static  <T, K, V> Map<K, V> map2Map(T  [] collection,  Function<? super T, ? extends Pair<? extends K, ? extends V>> mapper) {
    return map2Map(Arrays.asList(collection), mapper);
  }

  @Contract(pure = true)
  public static  <T, K, V> Map<K, V> map2Map( Collection<? extends T> collection,
                                                      Function<? super T, ? extends Pair<? extends K, ? extends V>> mapper) {
    Map<K, V> set = new HashMap<>(collection.size());
    for (T t : collection) {
      Pair<? extends K, ? extends V> pair = mapper.fun(t);
      set.put(pair.first, pair.second);
    }
    return set;
  }

  @Contract(pure = true)
  public static  <T, K, V> Map<K, V> map2MapNotNull(T  [] collection,
                                                             Function<? super T, ? extends Pair<? extends K, ? extends V>> mapper) {
    return map2MapNotNull(Arrays.asList(collection), mapper);
  }

  @Contract(pure = true)
  public static  <T, K, V> Map<K, V> map2MapNotNull( Collection<? extends T> collection,
                                                             Function<? super T, ? extends Pair<? extends K, ? extends V>> mapper) {
    Map<K, V> set = new HashMap<>(collection.size());
    for (T t : collection) {
      Pair<? extends K, ? extends V> pair = mapper.fun(t);
      if (pair != null) {
        set.put(pair.first, pair.second);
      }
    }
    return set;
  }

  @Contract(pure = true)
  public static  <K, V> Map<K, V> map2Map( Collection<? extends Pair<? extends K, ? extends V>> collection) {
    Map<K, V> result = new HashMap<>(collection.size());
    for (Pair<? extends K, ? extends V> pair : collection) {
      result.put(pair.first, pair.second);
    }
    return result;
  }

  @Contract(pure=true)
  public static <T> Object  [] map2Array(T  [] array,  Function<? super T, Object> mapper) {
    return map2Array(array, Object.class, mapper);
  }

  @Contract(pure=true)
  public static <T> Object  [] map2Array( Collection<? extends T> array,  Function<? super T, Object> mapper) {
    return map2Array(array, Object.class, mapper);
  }

  @Contract(pure=true)
  public static <T, V> V  [] map2Array(T  [] array,  Class<V> aClass,  Function<? super T, ? extends V> mapper) {
    V[] result = ArrayUtil.newArray(aClass, array.length);
    for (int i = 0; i < array.length; i++) {
      result[i] = mapper.fun(array[i]);
    }
    return result;
  }

  @Contract(pure=true)
  public static <T, V> V  [] map2Array( Collection<? extends T> collection,  Class<V> aClass,  Function<? super T, ? extends V> mapper) {
    V[] result = ArrayUtil.newArray(aClass, collection.size());
    int i = 0;
    for (T t : collection) {
      result[i++] = mapper.fun(t);
    }
    return result;
  }

  
  public static <T, V> V  [] map2Array( Collection<? extends T> collection, V  [] to,  Function<? super T, ? extends V> mapper) {
    return map(collection, mapper).toArray(to);
  }
  
  public static <T, V> V  [] map2Array(T  [] collection, V  [] to,  Function<? super T, ? extends V> mapper) {
    return map(collection, mapper).toArray(to);
  }

  @Contract(pure = true)
  
  public static  <T> List<T> filter(T  [] collection,  Condition<? super T> condition) {
    return findAll(collection, condition);
  }

  /**
   * @return read-only list consisting of the elements from the {@code collection} of the specified {@code aClass}
   */
  @Contract(pure = true)
  
  public static  <T> List<T> filterIsInstance( Collection<?> collection,  Class<? extends T> aClass) {
    //noinspection unchecked
    return filter((Collection<T>)collection, Conditions.instanceOf(aClass));
  }

  /**
   * @return read-only list consisting of the elements from the {@code collection} of the specified {@code aClass}
   */
  @Contract(pure = true)
  
  public static  <T> List<T> filterIsInstance(Object  [] collection,  Class<? extends T> aClass) {
    //noinspection unchecked
    return (List<T>)filter(collection, Conditions.instanceOf(aClass));
  }

  /**
   * @return read-only list consisting of the elements from the {@code collection} for which {@code condition.value} is true
   */
  @Contract(pure = true)
  
  public static  <T> List<T> filter( Collection<? extends T> collection,  Condition<? super T> condition) {
    return findAll(collection, condition);
  }

  /**
   * @return read-only map consisting of the entries from the {@code map} for which {@code keyFilter.value} is true for its key
   */
  @Contract(pure = true)
  public static  <K, V> Map<K, V> filter( Map<? extends K, ? extends V> map,  Condition<? super K> keyFilter) {
    Map<K, V> result = new HashMap<>();
    for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
      if (keyFilter.value(entry.getKey())) {
        result.put(entry.getKey(), entry.getValue());
      }
    }
    return result;
  }

  /**
   * @return read-only list consisting of the elements from the {@code collection} for which {@code condition.value} is true
   */
  @Contract(pure = true)
  
  public static  <T> List<T> findAll( Collection<? extends T> collection,  Condition<? super T> condition) {
    if (collection.isEmpty()) return emptyList();
    List<T> result = new SmartList<>();
    for (T t : collection) {
      if (condition.value(t)) {
        result.add(t);
      }
    }
    return result;
  }

  /**
   * @return read-only list consisting of not null elements from the {@code collection}
   */
  @Contract(pure = true)
  
  public static  <T> List<T> skipNulls( Collection<? extends T> collection) {
    return findAll(collection, Conditions.notNull());
  }

  @Contract(pure = true)
  
  public static  <T, V extends T> List<V> findAll(T  [] array,  Class<V> instanceOf) {
    List<V> result = new SmartList<>();
    for (T t : array) {
      if (instanceOf.isInstance(t)) {
        //noinspection unchecked
        result.add((V)t);
      }
    }
    return Collections.unmodifiableList(result);
  }

  @Contract(pure=true)
  public static <T, V extends T> V  [] findAllAsArray(T  [] collection,  Class<V> instanceOf) {
    List<V> list = findAll(collection, instanceOf);
    V[] array = ArrayUtil.newArray(instanceOf, list.size());
    return list.toArray(array);
  }

  @Contract(pure=true)
  public static <T, V extends T> V  [] findAllAsArray( Collection<? extends T> collection,  Class<V> instanceOf) {
    List<V> list = findAll(collection, instanceOf);
    V[] array = ArrayUtil.newArray(instanceOf, list.size());
    return list.toArray(array);
  }

  @Contract(pure=true)
  public static <T> T  [] findAllAsArray(T  [] collection,  Condition<? super T> condition) {
    List<T> list = findAll(collection, condition);
    if (list.size() == collection.length) {
      return collection;
    }
    T[] array = ArrayUtil.newArray(ArrayUtil.getComponentType(collection), list.size());
    return list.toArray(array);
  }

  @Contract(pure = true)
  
  public static  <T, V extends T> List<V> findAll( Collection<? extends T> collection,  Class<V> instanceOf) {
    List<V> result = new SmartList<>();
    for (T t : collection) {
      if (instanceOf.isInstance(t)) {
        //noinspection unchecked
        result.add((V)t);
      }
    }
    return Collections.unmodifiableList(result);
  }

  @Contract(pure = true)
  
  public static  <T> List<T> findAll(T  [] collection,  Condition<? super T> condition) {
    List<T> result = new SmartList<>();
    for (T t : collection) {
      if (condition.value(t)) {
        result.add(t);
      }
    }
    return Collections.unmodifiableList(result);
  }

  public static <T> boolean all(T  [] array,  Condition<? super T> condition) {
    return and(array, condition);
  }

  public static <T> boolean all( Collection<? extends T> collection,  Condition<? super T> condition) {
    for (T t : collection) {
      if (!condition.value(t)) {
        return false;
      }
    }
    return true;
  }

  
  public static void removeDuplicates( Collection<?> collection) {
    Set<Object> collected = new HashSet<>();
    collection.removeIf(t -> !collected.add(t));
  }

  @Contract(pure = true)
  public static  Map<String, String> stringMap(String  ... keyValues) {
    Map<String, String> result = new HashMap<>();
    for (int i = 0; i < keyValues.length - 1; i+=2) {
      result.put(keyValues[i], keyValues[i+1]);
    }

    return result;
  }

  @Contract(pure = true)
  public static  <T> Iterator<T> iterate(T  [] array) {
    return array.length == 0 ? Collections.emptyIterator() : Arrays.asList(array).iterator();
  }

  @Contract(pure = true)
  public static  <T> Iterator<T> iterate( Enumeration<? extends T> enumeration) {
    return new Iterator<T>() {
      @Override
      public boolean hasNext() {
        return enumeration.hasMoreElements();
      }

      @Override
      public T next() {
        return enumeration.nextElement();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Contract(pure = true)
  public static  <T> Iterable<T> iterateBackward( List<? extends T> list) {
    return () -> new Iterator<T>() {
      private final ListIterator<? extends T> it = list.listIterator(list.size());

      @Override
      public boolean hasNext() {
        return it.hasPrevious();
      }

      @Override
      public T next() {
        return it.previous();
      }

      @Override
      public void remove() {
        it.remove();
      }
    };
  }

  @Contract(pure = true)
  public static  <T, E> Iterable<Pair<T, E>> zip( Iterable<? extends T> iterable1,  Iterable<? extends E> iterable2) {
    return () -> new Iterator<Pair<T, E>>() {
      private final Iterator<? extends T> i1 = iterable1.iterator();
      private final Iterator<? extends E> i2 = iterable2.iterator();

      @Override
      public boolean hasNext() {
        return i1.hasNext() && i2.hasNext();
      }

      @Override
      public Pair<T, E> next() {
        return Pair.create(i1.next(), i2.next());
      }

      @Override
      public void remove() {
        i1.remove();
        i2.remove();
      }
    };
  }

  
  public static void swapElements( List<?> list, int index1, int index2) {
    Object e1 = list.get(index1);
    Object e2 = list.get(index2);
    //noinspection unchecked,rawtypes
    ((List)list).set(index1, e2);
    //noinspection unchecked,rawtypes
    ((List)list).set(index2, e1);
  }

  /**
   * @return read-only list consisting of the elements from the input collection
   */
  
  public static  <T> List<T> collect( Iterator<? extends T> iterator) {
    if (!iterator.hasNext()) return emptyList();
    List<T> list = new ArrayList<>();
    addAll(list, iterator);
    return list;
  }

  /**
   * @return read-only list consisting of the elements from the {@code iterator} of the specified class
   */
  
  public static  <T> List<T> collect( Iterator<?> iterator,  FilteringIterator.InstanceOf<T> instanceOf) {
    //noinspection unchecked
    return collect((Iterator<T>)iterator, t->instanceOf.value(t));
  }
  /**
   * @return read-only list consisting of the elements from the {@code iterator} satisfying the {@code predicate}
   */
  
  public static  <T> List<T> collect( Iterator<? extends T> iterator,  java.util.function.Predicate<? super T> predicate) {
    if (!iterator.hasNext()) return emptyList();
    List<T> list = new ArrayList<>();
    while (iterator.hasNext()) {
      T o = iterator.next();
      if (predicate.test(o)) {
        list.add(o);
      }
    }
    return unmodifiableOrEmptyList(list);
  }

  
  public static <T> void addAll( Collection<? super T> collection,  Enumeration<? extends T> enumeration) {
    while (enumeration.hasMoreElements()) {
      T element = enumeration.nextElement();
      collection.add(element);
    }
  }

  /**
   * Add all supplied elements to the supplied collection and returns the modified collection.
   * Unlike {@link Collections#addAll(Collection, Object[])} this method does not track whether the collection
   * was modified, so it could be marginally faster.
   *
   * @param collection collection to add elements to
   * @param elements elements to add
   * @param <T> type of collection elements
   * @param <C> type of the collection
   * @return the collection passed as first argument
   */
  @SafeVarargs
  
  public static  <T, C extends Collection<? super T>> C addAll( C collection, T  ... elements) {
    //noinspection ManualArrayToCollectionCopy
    for (T element : elements) {
      //noinspection UseBulkOperation
      collection.add(element);
    }
    return collection;
  }

  /**
   * Adds all not-null elements from the {@code elements}, ignoring nulls
   */
  @SafeVarargs
  
  public static  <T, C extends Collection<T>> C addAllNotNull( C collection, T  ... elements) {
    for (T element : elements) {
      if (element != null) {
        collection.add(element);
      }
    }
    return collection;
  }

  @SafeVarargs
  
  public static <T> boolean removeAll( Collection<T> collection, T  ... elements) {
    boolean modified = false;
    for (T element : elements) {
      modified |= collection.remove(element);
    }
    return modified;
  }

  // returns true if the collection was modified
  
  public static <T> boolean retainAll( Collection<T> collection,  Condition<? super T> condition) {
    boolean modified = false;

    for (Iterator<T> iterator = collection.iterator(); iterator.hasNext(); ) {
      T next = iterator.next();
      if (!condition.value(next)) {
        iterator.remove();
        modified = true;
      }
    }

    return modified;
  }

  @Contract(pure=true)
  public static <T, U extends T> U findInstance( Iterable<? extends T> iterable,  Class<? extends U> aClass) {
    return findInstance(iterable.iterator(), aClass);
  }

  public static <T, U extends T> U findInstance( Iterator<? extends T> iterator,  Class<? extends U> aClass) {
    //noinspection unchecked
    return (U)find(iterator, FilteringIterator.instanceOf(aClass));
  }

  @Contract(pure=true)
  public static <T, U extends T> U findInstance(T  [] array,  Class<? extends U> aClass) {
    //noinspection unchecked
    return (U)find(array, FilteringIterator.instanceOf(aClass));
  }

  /**
   * @return read-only list consisting of the elements from the {@code collection} of the specified {@code aClass}
   */
  @Contract(pure = true)
  
  public static  <T, V> List<T> concat(V  [] array,  Function<? super V, ? extends Collection<? extends T>> fun) {
    return concat(Arrays.asList(array), fun);
  }

  /**
   * @return read-only list consisting of all the elements from the collections stored in the list merged together
   */
  @Contract(pure = true)
  
  public static  <T> List<T> concat( Iterable<? extends Collection<? extends T>> list) {
    int totalSize = 0;
    for (Collection<? extends T> ts : list) {
      totalSize += ts.size();
    }
    List<T> result = new ArrayList<>(totalSize);
    for (Collection<? extends T> ts : list) {
      result.addAll(ts);
    }
    return result.isEmpty() ? Collections.emptyList() : result;
  }

  /**
   * @return read-only list consisting of the elements from the {@code list} and {@code values} concatenated
   */
  @SafeVarargs
  @Contract(pure = true)
  
  public static  <T> List<T> append( List<? extends T> list, T  ... values) {
    //noinspection unchecked
    return values.length == 0 ? (List<T>)list : concat(list, Arrays.asList(values));
  }

  /**
   * prepend {@code values} in front of the {@code list}
   * @return read-only list consisting of {@code values} and the {@code list} concatenated
   */
  @SafeVarargs
  @Contract(pure = true)
  
  public static  <T> List<T> prepend( List<? extends T> list, T  ... values) {
    //noinspection unchecked
    return values.length == 0 ? (List<T>)list : concat(Arrays.asList(values), list);
  }

  /**
   * @return read-only list consisting of {@code list1} and {@code list2} added together
   */
  @Contract(pure = true)
  
  public static  <T> List<T> concat( List<? extends T> list1,  List<? extends T> list2) {
    if (list1.isEmpty() && list2.isEmpty()) {
      return Collections.emptyList();
    }
    if (list1.isEmpty()) {
      //noinspection unchecked
      return (List<T>)list2;
    }
    if (list2.isEmpty()) {
      //noinspection unchecked
      return (List<T>)list1;
    }

    int size1 = list1.size();
    int size = size1 + list2.size();

    return new AbstractList<T>() {
      @Override
      public T get(int index) {
        if (index < size1) {
          return list1.get(index);
        }

        return list2.get(index - size1);
      }

      @Override
      public int size() {
        return size;
      }
    };
  }

  @Contract(pure = true)
  public static  <T> Iterable<T> concat( Iterable<? extends T> it1,  Iterable<? extends T> it2) {
    return new Iterable<T>() {
      @Override
      public void forEach(java.util.function.Consumer<? super T> action) {
        it1.forEach(action);
        it2.forEach(action);
      }

      @Override
      public  Iterator<T> iterator() {
        return new Iterator<T>() {
          Iterator<? extends T> it = it1.iterator();
          boolean firstFinished;

          { advance(); }

          @Override
          public boolean hasNext() {
            return !firstFinished || it.hasNext();
          }

          @Override
          public T next() {
            T value = it.next(); // it.next() will throw NSEE if no elements remaining
            advance();
            return value;
          }

          private void advance() {
            if (!firstFinished && !it.hasNext()) {
              it = it2.iterator();
              firstFinished = true;
            }
          }
        };
      }
    };
  }

  @SafeVarargs
  @Contract(pure = true)
  public static  <T> Iterable<T> concat(Iterable<? extends T>  ... iterables) {
    if (iterables.length == 0) return Collections.emptyList();
    if (iterables.length == 1) {
      //noinspection unchecked
      return (Iterable<T>)iterables[0];
    }
    return () -> {
      //noinspection unchecked
      Iterator<? extends T>[] iterators = new Iterator[iterables.length];
      for (int i = 0; i < iterables.length; i++) {
        Iterable<? extends T> iterable = iterables[i];
        iterators[i] = iterable.iterator();
      }
      return concatIterators(iterators);
    };
  }

  @SafeVarargs
  @Contract(pure = true)
  public static  <T> Iterator<T> concatIterators(Iterator<? extends T>  ... iterators) {
    return new SequenceIterator<>(iterators);
  }

  @Contract(pure = true)
  public static  <T> Iterator<T> concatIterators( Collection<? extends Iterator<? extends T>> iterators) {
    return new SequenceIterator<>(iterators.toArray(new Iterator[0]));
  }

  @SafeVarargs
  @Contract(pure = true)
  public static  <T> Iterable<T> concat(T[]  ... arrays) {
    return () -> {
      //noinspection unchecked
      Iterator<T>[] iterators = new Iterator[arrays.length];
      for (int i = 0; i < arrays.length; i++) {
        iterators[i] = iterate(arrays[i]);
      }
      return concatIterators(iterators);
    };
  }

  /**
   * @return read-only list consisting of the lists added together
   */
  @SafeVarargs
  @Contract(pure = true)
  
  public static  <T> List<T> concat(List<? extends T>  ... lists) {
    int size = 0;
    for (List<? extends T> each : lists) {
      size += each.size();
    }
    if (size == 0) return emptyList();
    int finalSize = size;
    return new AbstractList<T>() {
      @Override
      public T get(int index) {
        if (index >= 0 && index < finalSize) {
          int from = 0;
          for (List<? extends T> each : lists) {
            if (from <= index && index < from + each.size()) {
              return each.get(index - from);
            }
            from += each.size();
          }
          if (from != finalSize) {
            throw new ConcurrentModificationException("The list has changed. Its size was " + finalSize + "; now it's " + from);
          }
        }
        throw new IndexOutOfBoundsException("index: " + index + "; size: " + size());
      }

      @Override
      public int size() {
        return finalSize;
      }
    };
  }

  /**
   * @return read-only list consisting of the lists added together
   */
  @Contract(pure = true)
  
  public static  <T> List<T> concat( List<List<? extends T>> lists) {
    //noinspection unchecked
    return concat(lists.toArray(new List[0]));
  }

  /**
   * @return read-only list consisting of the lists (made by listGenerator) added together
   */
  @Contract(pure = true)
  
  public static  <T, V> List<V> concat( Iterable<? extends T> list,  Function<? super T, ? extends Collection<? extends V>> listGenerator) {
    List<V> result = new ArrayList<>();
    for (T v : list) {
      result.addAll(listGenerator.fun(v));
    }
    return result.isEmpty() ? emptyList() : result;
  }

  @Contract(pure=true)
  public static <T> boolean intersects( Collection<? extends T> collection1,  Collection<? extends T> collection2) {
    if (collection1.size() <= collection2.size()) {
      for (T t : collection1) {
        if (collection2.contains(t)) {
          return true;
        }
      }
    }
    else {
      for (T t : collection2) {
        if (collection1.contains(t)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * @return read-only collection consisting of elements from both collections
   */
  @Contract(pure = true)
  
  public static  <T> Collection<T> intersection( Collection<? extends T> collection1,  Collection<? extends T> collection2) {
    if (collection1.isEmpty() || collection2.isEmpty()) return emptyList();

    List<T> result = new ArrayList<>();
    for (T t : collection1) {
      if (collection2.contains(t)) {
        result.add(t);
      }
    }
    return result.isEmpty() ? emptyList() : result;
  }

  @Contract(pure = true)
  public static  <E extends Enum<E>> EnumSet<E> intersection( EnumSet<E> collection1,  EnumSet<E> collection2) {
    if (collection1.isEmpty()) return collection1;
    if (collection2.isEmpty()) return collection2;

    EnumSet<E> result = EnumSet.copyOf(collection1);
    result.retainAll(collection2);
    return result;
  }

  @Contract(pure=true)
  public static <T>  T getFirstItem( Collection<? extends T> items) {
    return getFirstItem(items, null);
  }

  @Contract(pure=true)
  public static <T>  T getFirstItem( List<? extends T> items) {
    return items == null || items.isEmpty() ? null : items.get(0);
  }

  @Contract(pure=true)
  public static <T> T getFirstItem( Collection<? extends T> items,  T defaultResult) {
    return items == null || items.isEmpty() ? defaultResult : items.iterator().next();
  }

  /**
   * Returns the only item from the collection or null if the collection is empty or contains more than one item
   *
   * @param items collection to get the item from
   * @param <T> type of collection element
   * @return the only collection element or null
   */
  @Contract(pure=true)
  public static <T>  T getOnlyItem( Collection<? extends T> items) {
    return getOnlyItem(items, null);
  }

  @Contract(pure=true)
  public static <T> T getOnlyItem( Collection<? extends T> items,  T defaultResult) {
    return items == null || items.size() != 1 ? defaultResult : items.iterator().next();
  }

  /**
   * The main difference from {@code subList} is that {@code getFirstItems} does not
   * throw any exceptions, even if maxItems is greater than size of the list
   *
   * @param items list
   * @param maxItems size of the result will be equal or less than {@code maxItems}
   * @param <T> type of list
   * @return list with no more than {@code maxItems} first elements
   */
  @Contract(pure = true)
  public static  <T> List<T> getFirstItems( List<T> items, int maxItems) {
    if (maxItems < 0) {
      throw new IllegalArgumentException("Expected non-negative maxItems; got: "+maxItems);
    }
    return maxItems >= items.size() ? items : items.subList(0, maxItems);
  }

  @Contract(pure=true)
  public static <T> T iterateAndGetLastItem( Iterable<? extends T> items) {
    Iterator<? extends T> itr = items.iterator();
    T res = null;
    while (itr.hasNext()) {
      res = itr.next();
    }

    return res;
  }
  /**
   * @deprecated use {@link #getLastItem(List)}<br>
   *
   * DO NOT remove this method until {@link #iterateAndGetLastItem(Iterable)} is removed
   * The former method is here to highlight incorrect usages of the latter.
   */
  @Deprecated
  @Contract(pure = true)
  public static <T> T iterateAndGetLastItem( List<? extends T> items) {
    Logger.getInstance(ContainerUtil.class).error("use getLastItem(List) instead");
    return getLastItem(items);
  }

  @Contract(pure = true)
  public static  <T, U> Iterator<U> mapIterator( Iterator<? extends T> iterator,  Function<? super T, ? extends U> mapper) {
    return new Iterator<U>() {
      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public U next() {
        return mapper.fun(iterator.next());
      }

      @Override
      public void remove() {
        iterator.remove();
      }
    };
  }

  /**
   * @return iterator with elements from the original {@code iterator} which are valid according to {@code filter} predicate.
   */
  @Contract(pure = true)
  public static  <T> Iterator<T> filterIterator( Iterator<? extends T> iterator,  Condition<? super T> filter) {
    return new Iterator<T>() {
      T next;
      boolean hasNext;
      {
        findNext();
      }
      @Override
      public boolean hasNext() {
        return hasNext;
      }

      private void findNext() {
        hasNext = false;
        while (iterator.hasNext()) {
          T t = iterator.next();
          if (filter.value(t)) {
            next = t;
            hasNext = true;
            break;
          }
        }
      }

      @Override
      public T next() {
        if (hasNext) {
          T result = next;
          findNext();
          return result;
        }
        else {
          throw new NoSuchElementException();
        }
      }

      @Override
      public void remove() {
        iterator.remove();
      }
    };
  }

  @Contract(pure=true)
  public static <T> T getLastItem( List<? extends T> list,  T def) {
    return isEmpty(list) ? def : list.get(list.size() - 1);
  }

  @Contract(pure=true)
  public static <T>   T getLastItem( List<? extends T> list) {
    return getLastItem(list, null);
  }

  /**
   * @return read-only collection consisting of elements from the 'from' collection which are absent from the 'what' collection
   */
  @Contract(pure = true)
  
  public static  <T> Collection<T> subtract( Collection<? extends T> from,  Collection<? extends T> what) {
    Set<T> set = new HashSet<>(from);
    set.removeAll(what);
    return set.isEmpty() ? emptyList() : set;
  }

  @Contract(pure=true)
  public static <T> T  [] toArray( Collection<T> c,  ArrayFactory<? extends T> factory) {
    T[] a = factory.create(c.size());
    return c.toArray(a);
  }

  @Contract(pure=true)
  public static <T> T  [] toArray( Collection<? extends T> c1,  Collection<? extends T> c2,  ArrayFactory<? extends T> factory) {
    return ArrayUtil.mergeCollections(c1, c2, factory);
  }

  
  public static <T extends Comparable<? super T>> void sort( List<T> list) {
    int size = list.size();

    if (size < 2) return;
    if (size == 2) {
      T t0 = list.get(0);
      T t1 = list.get(1);

      if (t0.compareTo(t1) > 0) {
        list.set(0, t1);
        list.set(1, t0);
      }
    }
    else if (size < INSERTION_SORT_THRESHOLD) {
      for (int i = 0; i < size; i++) {
        for (int j = 0; j < i; j++) {
          T ti = list.get(i);
          T tj = list.get(j);

          if (ti.compareTo(tj) < 0) {
            list.set(i, tj);
            list.set(j, ti);
          }
        }
      }
    }
    else {
      Collections.sort(list);
    }
  }

  
  public static <T> void sort( List<T> list,  Comparator<? super T> comparator) {
    int size = list.size();

    if (size < 2) return;
    if (size == 2) {
      T t0 = list.get(0);
      T t1 = list.get(1);

      if (comparator.compare(t0, t1) > 0) {
        list.set(0, t1);
        list.set(1, t0);
      }
    }
    else if (size < INSERTION_SORT_THRESHOLD) {
      for (int i = 0; i < size; i++) {
        for (int j = 0; j < i; j++) {
          T ti = list.get(i);
          T tj = list.get(j);

          if (comparator.compare(ti, tj) < 0) {
            list.set(i, tj);
            list.set(j, ti);
          }
        }
      }
    }
    else {
      list.sort(comparator);
    }
  }

  
  public static <T extends Comparable<? super T>> void sort(T  [] a) {
    int size = a.length;

    if (size < 2) return;
    if (size == 2) {
      T t0 = a[0];
      T t1 = a[1];

      if (t0.compareTo(t1) > 0) {
        a[0] = t1;
        a[1] = t0;
      }
    }
    else if (size < INSERTION_SORT_THRESHOLD) {
      for (int i = 0; i < size; i++) {
        for (int j = 0; j < i; j++) {
          T ti = a[i];
          T tj = a[j];

          if (ti.compareTo(tj) < 0) {
            a[i] = tj;
            a[j] = ti;
          }
        }
      }
    }
    else {
      Arrays.sort(a);
    }
  }

  @Contract(pure = true)
  
  public static  <T> List<T> sorted( Collection<? extends T> list,  Comparator<? super T> comparator) {
    return sorted((Iterable<? extends T>)list, comparator);
  }

  @Contract(pure = true)
  
  public static  <T> List<T> sorted( Iterable<? extends T> list,  Comparator<? super T> comparator) {
    List<T> sorted = newArrayList(list);
    sort(sorted, comparator);
    return Collections.unmodifiableList(sorted);
  }

  @Contract(pure = true)
  
  public static  <T extends Comparable<? super T>> List<T> sorted( Collection<? extends T> list) {
    List<T> result = new ArrayList<>(list);
    result.sort(null);
    return Collections.unmodifiableList(result);
  }

  /**
   * @apiNote this sort implementation is NOT stable for {@code array.length < INSERTION_SORT_THRESHOLD}
   */
  
  public static <T> void sort(T  [] array,  Comparator<? super T> comparator) {
    int size = array.length;

    if (size < 2) return;
    if (size == 2) {
      T t0 = array[0];
      T t1 = array[1];

      if (comparator.compare(t0, t1) > 0) {
        array[0] = t1;
        array[1] = t0;
      }
    }
    else if (size < INSERTION_SORT_THRESHOLD) {
      for (int i = 0; i < size; i++) {
        for (int j = 0; j < i; j++) {
          T ti = array[i];
          T tj = array[j];

          if (comparator.compare(ti, tj) < 0) {
            array[i] = tj;
            array[j] = ti;
          }
        }
      }
    }
    else {
      Arrays.sort(array, comparator);
    }
  }

  /**
   * @param iterable an input iterable to process
   * @param mapping a side effect-free function which transforms iterable elements
   * @return read-only list consisting of the elements from the iterable converted by mapping
   */
  @Contract(pure = true)
  
  public static  <T, V> List<V> map( Iterable<? extends T> iterable,  Function<? super T, ? extends V> mapping) {
    List<V> result = new ArrayList<>();
    for (T t : iterable) {
      result.add(mapping.fun(t));
    }
    return result.isEmpty() ? emptyList() : result;
  }

  /**
   * @param iterator an input iterator to process
   * @param mapping a side effect-free function which transforms iterable elements
   * @return read-only list consisting of the elements from the iterator converted by mapping
   */
  @Contract(pure = true)
  
  public static  <T, V> List<V> map( Iterator<? extends T> iterator,  Function<? super T, ? extends V> mapping) {
    List<V> result = new ArrayList<>();
    while (iterator.hasNext()) {
      result.add(mapping.fun(iterator.next()));
    }
    return result.isEmpty() ? emptyList() : result;
  }

  /**
   * @param collection an input collection to process
   * @param mapping a side effect-free function which transforms iterable elements
   * @return read-only list consisting of the elements from the input collection converted by mapping
   */
  @Contract(pure = true)
  
  public static  <T, V> List<V> map( Collection<? extends T> collection,  Function<? super T, ? extends V> mapping) {
    if (collection.isEmpty()) return emptyList();
    List<V> list = new ArrayList<>(collection.size());
    for (T t : collection) {
      list.add(mapping.fun(t));
    }
    return list;
  }

  /**
   * @param array an input array to process
   * @param mapping a side effect-free function which transforms array elements
   * @return read-only list consisting of the elements from the input array converted by mapping with nulls filtered out
   */
  @Contract(pure = true)
  
  public static  <T, V> List<V> mapNotNull(T  [] array,
                                                    Function<? super T, ? extends V> mapping) {
    if (array.length == 0) {
      return emptyList();
    }

    List<V> result = new ArrayList<>(array.length);
    for (T t : array) {
      V o = mapping.fun(t);
      if (o != null) {
        result.add(o);
      }
    }
    return result.isEmpty() ? emptyList() : result;
  }

  /**
   * @param array an input array to process
   * @param mapping a side effect-free function which transforms array elements
   * @param emptyArray an empty array of the desired result type (maybe returned if the result is also empty)
   * @return array consisting of the elements from the input array converted by mapping with nulls filtered out
   */
  @Contract(pure=true)
  public static <T, V> V  [] mapNotNull(T  [] array,
                                                          Function<? super T, ? extends V> mapping,
                                                         V  [] emptyArray) {
    assert emptyArray.length == 0 : "You must pass an empty array";
    List<V> result = new ArrayList<>(array.length);
    for (T t : array) {
      V v = mapping.fun(t);
      if (v != null) {
        result.add(v);
      }
    }
    if (result.isEmpty()) {
      return emptyArray;
    }
    return result.toArray(emptyArray);
  }

  /**
   * @param iterable an input iterable to process
   * @param mapping a side effect-free function which transforms iterable elements
   * @return read-only list consisting of the elements from the iterable converted by mapping with nulls filtered out
   */
  @Contract(pure = true)
  
  public static  <T, V> List<V> mapNotNull( Iterable<? extends T> iterable,
                                                    Function<? super T, ? extends V> mapping) {
    List<V> result = new ArrayList<>();
    for (T t : iterable) {
      V o = mapping.fun(t);
      if (o != null) {
        result.add(o);
      }
    }
    return result.isEmpty() ? emptyList() : result;
  }

  /**
   * @param collection an input collection to process
   * @param mapping a side effect-free function which transforms collection elements
   * @return read-only list consisting of the elements from the array converted by mapping with nulls filtered out
   */
  @Contract(pure = true)
  
  public static  <T, V> List<V> mapNotNull( Collection<? extends T> collection,
                                                    Function<? super T, ? extends V> mapping) {
    if (collection.isEmpty()) {
      return emptyList();
    }

    List<V> result = new ArrayList<>(collection.size());
    for (T t : collection) {
      V o = mapping.fun(t);
      if (o != null) {
        result.add(o);
      }
    }
    return result.isEmpty() ? emptyList() : result;
  }

  /**
   * @return read-only list consisting of the elements with nulls filtered out
   */
  @SafeVarargs
  @Contract(pure = true)
  
  public static  <T> List<T> packNullables( T  ... elements) {
    List<T> list = new ArrayList<>();
    for (T element : elements) {
      addIfNotNull(list, element);
    }
    return list.isEmpty() ? emptyList() : list;
  }

  /**
   * @return read-only list consisting of the elements from the array converted by mapping
   */
  @Contract(pure = true)
  
  public static  <T, V> List<V> map(T  [] array,  Function<? super T, ? extends V> mapping) {
    List<V> result = new ArrayList<>(array.length);
    for (T t : array) {
      result.add(mapping.fun(t));
    }
    return result.isEmpty() ? emptyList() : result;
  }

  @Contract(pure=true)
  public static <T, V> V  [] map(T  [] arr,  Function<? super T, ? extends V> mapping, V  [] emptyArray) {
    if (arr.length==0) {
      assert emptyArray.length == 0 : "You must pass an empty array";
      return emptyArray;
    }

    V[] result = emptyArray.length < arr.length ? Arrays.copyOf(emptyArray, arr.length) : emptyArray;

    for (int i = 0; i < arr.length; i++) {
      result[i] = mapping.fun(arr[i]);
    }
    return result;
  }

  /**
   * @deprecated use {@link Collections#emptySet()} or {@link Set#of()} instead<br>
   *
   * DO NOT REMOVE this method until {@link ContainerUtil#set(Object[])} is removed.
   * The former method is here to highlight incorrect usages of the latter.
   */
  @Deprecated
  @Contract(pure = true)
  
  public static  <T> Set<T> set() {
    return Collections.emptySet();
  }

  /**
   * @deprecated use {@link Collections#singleton(Object)} or {@link Set#of} instead<br>
   *
   * DO NOT REMOVE this method until {@link ContainerUtil#set(Object[])} is removed.
   * The former method is here to highlight incorrect usages of the latter.
   */
  @Deprecated
  @Contract(pure = true)
  
  public static  <T> Set<T> set(T t) {
    return Collections.singleton(t);
  }

  /**
   * @deprecated use more standard immutable {@link Set#of(Object[])} instead.
   * If you do need a mutable {@link Set} please use {@link HashSet#HashSet()} or {@link #newHashSet(Object[])}
   */
  @Deprecated
  
  @SafeVarargs
  public static  <T> Set<T> set(T  ... items) {
    //noinspection SSBasedInspection
    return new HashSet<>(Arrays.asList(items));
  }

  
  public static <K, V> void putIfNotNull(K key,  V value,  Map<? super K, ? super V> result) {
    if (value != null) {
      result.put(key, value);
    }
  }

  
  public static <K, V> void putIfNotNull(K key,  Collection<? extends V> value,  MultiMap<? super K, ? super V> result) {
    if (value != null) {
      result.putValues(key, value);
    }
  }

  
  public static <K, V> void putIfNotNull(K key,  V value,  MultiMap<? super K, ? super V> result) {
    if (value != null) {
      result.putValue(key, value);
    }
  }

  
  public static <T> void add(T element,  Collection<? super T> result,  Disposable parentDisposable) {
    if (result.add(element)) {
      Disposer.register(parentDisposable, () -> result.remove(element));
    }
  }

  /**
   * @return read-only list consisting of the only element {@code element}, or empty list if {@code element} is null
   */
  @Contract(pure = true)
  
  public static  <T> List<T> createMaybeSingletonList( T element) {
    return element == null ? emptyList() : Collections.singletonList(element);
  }

  /**
   * @return read-only set consisting of the only element {@code element}, or empty set if {@code element} is null
   */
  @Contract(pure = true)
  
  public static  <T> Set<T> createMaybeSingletonSet( T element) {
    return element == null ? Collections.emptySet() : Collections.singleton(element);
  }

  /**
   * @deprecated Use {@link Map#computeIfAbsent(Object, java.util.function.Function)}
   */
  @Deprecated
  public static  <T, V> V getOrCreate( Map<T, V> result, T key,  V defaultValue) {
    return result.computeIfAbsent(key, __ -> defaultValue);
  }

  /**
   * @deprecated use {@link Map#computeIfAbsent(Object, java.util.function.Function)}
   */
  @Deprecated
  public static <T, V> V getOrCreate( Map<T, V> result, T key,  Factory<? extends V> factory) {
    return result.computeIfAbsent(key, __ -> factory.create());
  }

  /**
   * @deprecated use {@link Map#getOrDefault(Object, Object)}
   */
  @Deprecated
  @Contract(pure = true)
  public static  <T, V> V getOrElse( Map<? extends T, V> map, T key,  V defValue) {
    return map.getOrDefault(key, defValue);
  }

  @Contract(pure=true)
  public static <T> boolean and(T  [] iterable,  Condition<? super T> condition) {
    for (T t : iterable) {
      if (!condition.value(t)) return false;
    }
    return true;
  }

  @Contract(pure=true)
  public static <T> boolean and( Iterable<? extends T> iterable,  Condition<? super T> condition) {
    for (T t : iterable) {
      if (!condition.value(t)) return false;
    }
    return true;
  }

  @Contract(pure=true)
  public static <T> boolean exists(T  [] array,  Condition<? super T> condition) {
    for (T t : array) {
      if (condition.value(t)) return true;
    }
    return false;
  }

  @Contract(pure=true)
  public static <T> boolean exists( Iterable<? extends T> iterable,  Condition<? super T> condition) {
    for (T t : iterable) {
      if (condition.value(t)) return true;
    }
    return false;
  }

  @Contract(pure=true)
  public static <T> boolean or(T  [] iterable,  Condition<? super T> condition) {
    return exists(iterable, condition);
  }

  @Contract(pure=true)
  public static <T> boolean or( Iterable<? extends T> iterable,  Condition<? super T> condition) {
    return exists(iterable, condition);
  }

  @Contract(pure=true)
  public static <T> int count( Iterable<? extends T> iterable,  Condition<? super T> condition) {
    int count = 0;
    for (T t : iterable) {
      if (condition.value(t)) count++;
    }
    return count;
  }

  /**
   * @deprecated Use {@link Arrays#asList(Object[])}
   */
  @SafeVarargs
  @Contract(pure = true)
  @Deprecated
  
  public static  <T> List<T> list(T  ... items) {
    return Arrays.asList(items);
  }

  // Generalized Quick Sort. Does neither array.clone() nor list.toArray()

  
  public static <T> void quickSort( List<? extends T> list,  Comparator<? super T> comparator) {
    quickSort(list, comparator, 0, list.size());
  }

  
  private static <T> void quickSort( List<? extends T> x,  Comparator<? super T> comparator, int off, int len) {
    // Insertion sort on the smallest arrays
    if (len < 7) {
      for (int i = off; i < len + off; i++) {
        for (int j = i; j > off && comparator.compare(x.get(j), x.get(j - 1)) < 0; j--) {
          swapElements(x, j, j - 1);
        }
      }
      return;
    }

    // Choose a partition element, v
    int m = off + (len >> 1);       // Small arrays, middle element
    if (len > 7) {
      int l = off;
      int n = off + len - 1;
      if (len > 40) {        // Big arrays, pseudo-median of 9
        int s = len / 8;
        l = med3(x, comparator, l, l + s, l + 2 * s);
        m = med3(x, comparator, m - s, m, m + s);
        n = med3(x, comparator, n - 2 * s, n - s, n);
      }
      m = med3(x, comparator, l, m, n); // Mid-size, med of 3
    }
    T v = x.get(m);

    // Establish Invariant: v* (<v)* (>v)* v*
    int a = off;
    int b = a;
    int c = off + len - 1;
    int d = c;
    while (true) {
      while (b <= c && comparator.compare(x.get(b), v) <= 0) {
        if (comparator.compare(x.get(b), v) == 0) {
          swapElements(x, a++, b);
        }
        b++;
      }
      while (c >= b && comparator.compare(v, x.get(c)) <= 0) {
        if (comparator.compare(x.get(c), v) == 0) {
          swapElements(x, c, d--);
        }
        c--;
      }
      if (b > c) break;
      swapElements(x, b++, c--);
    }

    // Swap partition elements back to the middle
    int s = Math.min(a - off, b - a);
    vecSwap(x, off, b - s, s);
    int n = off + len;
    s = Math.min(d - c, n - d - 1);
    vecSwap(x, b, n - s, s);

    // Recursively sort non-partition-elements
    if ((s = b - a) > 1) quickSort(x, comparator, off, s);
    if ((s = d - c) > 1) quickSort(x, comparator, n - s, s);
  }

  /*
   * Returns the index of the median of the three indexed longs.
   */
  private static <T> int med3( List<? extends T> x,  Comparator<? super T> comparator, int a, int b, int c) {
    return comparator.compare(x.get(a), x.get(b)) < 0 ? comparator.compare(x.get(b), x.get(c)) < 0
                                                        ? b
                                                        : comparator.compare(x.get(a), x.get(c)) < 0 ? c : a
                                                      : comparator.compare(x.get(c), x.get(b)) < 0
                                                        ? b
                                                        : comparator.compare(x.get(c), x.get(a)) < 0 ? c : a;
  }

  /*
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
   */
  
  private static <T> void vecSwap(List<T> x, int a, int b, int n) {
    for (int i = 0; i < n; i++, a++, b++) {
      swapElements(x, a, b);
    }
  }

  /**
   * @return read-only list consisting of the elements from all collections in order
   */
  @Contract(pure = true)
  
  public static  <E> List<E> flatten(Collection<E>  [] collections) {
    return flatten(Arrays.asList(collections));
  }

  /**
   * Processes the list, remove all duplicates and return the list with unique elements.
   * @param list must be sorted (according to the comparator), all elements must be not-null
   */
  
  public static  <T> List<? extends T> removeDuplicatesFromSorted( List<? extends T> list,  Comparator<? super T> comparator) {
    T prev = null;
    List<T> result = null;
    for (int i = 0; i < list.size(); i++) {
      T t = list.get(i);
      if (t == null) {
        throw new IllegalArgumentException("get(" + i + ") = null");
      }
      int cmp = prev == null ? -1 : comparator.compare(prev, t);
      if (cmp < 0) {
        if (result != null) result.add(t);
      }
      else if (cmp == 0) {
        if (result == null) {
          result = new ArrayList<>(list.size());
          result.addAll(list.subList(0, i));
        }
      }
      else {
        throw new IllegalArgumentException("List must be sorted but get(" + (i - 1) + ")=" + list.get(i - 1) + " > get(" + i + ")=" + t);
      }
      prev = t;
    }
    return result == null ? list : Collections.unmodifiableList(result);
  }

  /**
   * @return read-only list consisting of the elements from all collections in order
   */
  @Contract(pure = true)
  
  public static  <E> List<E> flatten( Iterable<? extends Collection<? extends E>> collections) {
    int totalSize = 0;
    for (Collection<? extends E> list : collections) {
      totalSize += list.size();
    }
    List<E> result = new ArrayList<>(totalSize);
    for (Collection<? extends E> list : collections) {
      result.addAll(list);
    }

    return result.isEmpty() ? emptyList() : result;
  }

  /**
   * @return read-only list consisting of the elements from all collections returned by the mapping function,
   * or a read-only view of the list returned by the mapping function, if it only returned a single list that was not empty
   */
  @Contract(pure = true)
  
  public static  <T, V> List<V> flatMap( Iterable<? extends T> iterable,  Function<? super T, ? extends List<? extends V>> mapping) {
    // GC optimization for critical clients
    List<V> result = null;
    boolean isOriginal = true;

    for (T each : iterable) {
      List<? extends V> toAdd = mapping.fun(each);
      if (toAdd.isEmpty()) continue;

      if (result == null) {
        //noinspection unchecked
        result = (List<V>)toAdd;
        continue;
      }

      if (isOriginal) {
        List<? extends V> original = result;
        result = new ArrayList<>(Math.max(10, result.size() + toAdd.size()));
        result.addAll(original);
        isOriginal = false;
      }

      result.addAll(toAdd);
    }

    return result == null ? emptyList() : Collections.unmodifiableList(result);
  }

  
  public static <K,V> V  [] convert(K  [] from, V  [] to,  Function<? super K, ? extends V> fun) {
    if (to.length < from.length) {
      to = ArrayUtil.newArray(ArrayUtil.getComponentType(to), from.length);
    }
    for (int i = 0; i < from.length; i++) {
      to[i] = fun.fun(from[i]);
    }
    return to;
  }

  @Contract(pure=true)
  public static <T> boolean containsIdentity( Iterable<? extends T> list, T element) {
    for (T t : list) {
      if (t == element) {
        return true;
      }
    }
    return false;
  }

  @Contract(pure=true)
  public static <T> int indexOfIdentity( List<? extends T> list, T element) {
    for (int i = 0, listSize = list.size(); i < listSize; i++) {
      if (list.get(i) == element) {
        return i;
      }
    }
    return -1;
  }

  @Contract(pure=true)
  public static <T> boolean equalsIdentity( List<? extends T> list1,  List<? extends T> list2) {
    int listSize = list1.size();
    if (list2.size() != listSize) {
      return false;
    }

    for (int i = 0; i < listSize; i++) {
      if (list1.get(i) != list2.get(i)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Finds the first element in the list that satisfies given condition.
   *
   * @param list list to scan
   * @param condition condition that should be satisfied
   * @param <T> type of the list elements
   * @return index of the first element in the list that satisfies the condition; -1 if no element in the list satisfies the condition.
   */
  @Contract(pure=true)
  public static <T> int indexOf( List<? extends T> list,  Condition<? super T> condition) {
    for (int i = 0, listSize = list.size(); i < listSize; i++) {
      T t = list.get(i);
      if (condition.value(t)) {
        return i;
      }
    }
    return -1;
  }

  @Contract(pure=true)
  public static <T> int lastIndexOf( List<? extends T> list,  Condition<? super T> condition) {
    for (int i = list.size() - 1; i >= 0; i--) {
      T t = list.get(i);
      if (condition.value(t)) {
        return i;
      }
    }
    return -1;
  }

  @Contract(pure = true)
  public static <T, U extends T> U findLastInstance( List<? extends T> list,  Class<? extends U> clazz) {
    int i = lastIndexOf(list, (Condition<T>)clazz::isInstance);
    //noinspection unchecked
    return i < 0 ? null : (U)list.get(i);
  }

  @Contract(pure = true)
  public static  <A,B> Map<B,A> reverseMap( Map<? extends A, ? extends B> map) {
    Map<B,A> result = new HashMap<>(map.size());
    for (Map.Entry<? extends A, ? extends B> entry : map.entrySet()) {
      result.put(entry.getValue(), entry.getKey());
    }
    return result;
  }

  public static <T> List<T> trimToSize( List<T> list) {
    if (list == null) return null;
    if (list.isEmpty()) return emptyList();

    if (list instanceof ArrayList) {
      ((ArrayList<T>)list).trimToSize();
    }

    return list;
  }

  /**
   * @deprecated Use {@link Stack#Stack()}
   */
  @Contract(value = " -> new", pure = true)
  @Deprecated
  
  public static  <T> Stack<T> newStack() {
    return new Stack<>();
  }

  /**
   * @return read-only empty list
   * The only difference from {@link Collections#emptyList()} is that this list doesn't produce garbage in its {@link List#toArray()} method
   */
  @Contract(pure = true)
  
  public static  <T> List<T> emptyList() {
    //noinspection deprecation
    return ContainerUtilRt.emptyList();
  }

  @Contract(value = " -> new", pure = true)
  public static  <T> CopyOnWriteArrayList<T> createEmptyCOWList() {
    // does not create garbage new Object[0]
    return new CopyOnWriteArrayList<>(emptyList());
  }

  /**
   * Creates List, which is thread-safe to modify and iterate.
   * It differs from the java.util.concurrent.CopyOnWriteArrayList in the following:
   * - faster modification in the uncontended case
   * - less memory
   * - slower modification in highly contented case (which is the kind of situation you shouldn't use COWAL anyway)<br>
   *
   * N.B. Avoid using {@code list.toArray(new T[list.size()])} on this list because it is inherently racey and
   * therefore can return an array with null elements at the end.
   */
  @Contract(value = " -> new", pure = true)
  public static  <T> List<T> createLockFreeCopyOnWriteList() {
    return createConcurrentList();
  }

  /**
   * @see #createLockFreeCopyOnWriteList()
   * @return thread-safe copy-on-write List.
   * <b>Warning!</b> {@code c} collection must have correct {@link Collection#toArray()} method implementation
   * which doesn't leak the underlying array to avoid accidental modification in-place.
   */
  @Contract(value = "_ -> new", pure = true)
  public static  <T> List<T> createLockFreeCopyOnWriteList( Collection<? extends T> c) {
    LockFreeCopyOnWriteArrayList<T> tempList = new LockFreeCopyOnWriteArrayList<>();
    tempList.addAll(c);
    return tempList;
  }

  /**
   * @deprecated Use {@link com.intellij.concurrency.ConcurrentCollectionFactory#createConcurrentLongObjectMap()} instead
   */
  
  @Deprecated
  @Contract(value = " -> new", pure = true)
  public static  <V> ConcurrentLongObjectMap<V> createConcurrentLongObjectMap() {
    return new ConcurrentLongObjectHashMap<>();
  }

  /**
   * @deprecated Use {@link com.intellij.concurrency.ConcurrentCollectionFactory#createConcurrentIntObjectMap()} instead
   */
  
  @Deprecated
  @Contract(value = " -> new", pure = true)
  public static  <V> ConcurrentIntObjectMap<V> createConcurrentIntObjectMap() {
    return new ConcurrentIntObjectHashMap<>();
  }

  public static  <K,V> ConcurrentMap<K, V> createConcurrentWeakValueMap() {
    return new ConcurrentWeakValueHashMap();
  }

  public static  <K, V> ConcurrentMap<K, V> createConcurrentSoftKeySoftValueMap() {
    return new ConcurrentSoftKeySoftValueHashMap<>(100, 0.75f, Runtime.getRuntime().availableProcessors(), HashingStrategy.canonical());
  }

  public static  <K,V> ConcurrentMap<K, V> createConcurrentWeakKeySoftValueMap() {
    return new ConcurrentWeakKeySoftValueHashMap(100, 0.75F, Runtime.getRuntime().availableProcessors(), HashingStrategy.canonical());
  }

  public static  <K,V> ConcurrentMap<K, V> createConcurrentWeakKeyWeakValueMap() {
    return new ConcurrentWeakKeyWeakValueHashMap(100, 0.75F, Runtime.getRuntime().availableProcessors(), HashingStrategy.canonical());
  }

  public static  <K,V> ConcurrentMap<K, V> createConcurrentWeakKeyWeakValueMap( HashingStrategy<? super K> strategy) {
    return new ConcurrentWeakKeyWeakValueHashMap(100, 0.75F, Runtime.getRuntime().availableProcessors(), HashingStrategy.canonical());
  }

  public static  <K,V> ConcurrentMap<K, V> createConcurrentSoftMap() {
    return new ConcurrentSoftHashMap();
  }

  public static  <K,V> Map<K,V> createWeakKeySoftValueMap() {
    return new WeakKeySoftValueHashMap<>();
  }

  public static  <K,V> Map<K,V> createWeakKeyWeakValueMap() {
    return new WeakKeyWeakValueHashMap<>();
  }

  public static  <K,V> Map<K,V> createSoftKeySoftValueMap() {
    return new SoftKeySoftValueHashMap<>();
  }

  @Contract(value = " -> new", pure = true)
  public static  <K,V> Map<K,V> createSoftMap() {
    return new SoftHashMap<>();
  }


  @Contract(value = " -> new", pure = true)
  public static  <K, V> ConcurrentMap<K, V> createConcurrentSoftValueMap() {
    return CollectionFactory.createConcurrentSoftValueMap();
  }

  @Contract(value = " -> new", pure = true)
  public static  <K,V> ConcurrentMap<K, V> createConcurrentWeakMap() {
    return CollectionFactory.createConcurrentWeakMap();
  }

  /**
   * @see #createLockFreeCopyOnWriteList()
   */
  @Contract(value = " -> new", pure = true)
  public static  <T> ConcurrentList<T> createConcurrentList() {
    return new LockFreeCopyOnWriteArrayList<>();
  }

  /**
   * @return thread-safe copy-on-write List.
   * <b>Warning!</b> {@code c} collection must have correct {@link Collection#toArray()} method implementation
   * which doesn't leak the underlying array to avoid accidental modification in-place.
   * @see #createLockFreeCopyOnWriteList()
   */
  @Contract(value = "_ -> new", pure = true)
  public static  <T> ConcurrentList<T> createConcurrentList( Collection <? extends T> c) {
    LockFreeCopyOnWriteArrayList<T> tempList = new LockFreeCopyOnWriteArrayList<>();
    tempList.addAll(c);
    return tempList;
  }

  
  public static <T> void addIfNotNull( Collection<? super T> result,  T element) {
    if (element != null) {
      result.add(element);
    }
  }

  /**
   * @return read-only list consisting of results of {@code mapper.fun} for each element in {@code array}
   * @deprecated use {@link #map(Object[], Function)}
   */
  @Contract(pure = true)
  
  @Deprecated
  public static  <T, V> List<V> map2List(T  [] array,  Function<? super T, ? extends V> mapper) {
    return map(array, mapper);
  }

  /**
   * @deprecated use {@link #map(Collection, Function)}
   */
  @Contract(pure = true)
  
  @Deprecated
  public static  <T, V> List<V> map2List( Collection<? extends T> collection,  Function<? super T, ? extends V> mapper) {
    return map(collection, mapper);
  }

  /**
   * @return read-only set consisting of results of {@code mapper.fun} for each element in {@code array}
   */
  @Contract(pure = true)
  
  public static  <T, V> Set<V> map2Set(T  [] array,  Function<? super T, ? extends V> mapper) {
    if (array.length == 0) return Collections.emptySet();
    Set<V> set = new HashSet<>(array.length);
    for (T t : array) {
      set.add(mapper.fun(t));
    }
    return set;
  }

  /**
   * @return read-only set consisting of results of {@code mapper.fun} for each element in {@code collection}
   */
  @Contract(pure = true)
  
  public static  <T, V> Set<V> map2Set( Collection<? extends T> collection,  Function<? super T, ? extends V> mapper) {
    if (collection.isEmpty()) return Collections.emptySet();
    Set <V> set = new HashSet<>(collection.size());
    for (T t : collection) {
      set.add(mapper.fun(t));
    }
    return set;
  }

  /**
   * @return read-only linked set consisting of results of {@code mapper.fun} for each element in {@code collection}
   */
  @Contract(pure = true)
  
  public static  <T, V> Set<V> map2LinkedSet( Collection<? extends T> collection,  Function<? super T, ? extends V> mapper) {
    if (collection.isEmpty()) return Collections.emptySet();
    Set <V> set = new LinkedHashSet<>(collection.size());
    for (T t : collection) {
      set.add(mapper.fun(t));
    }
    return set;
  }

  /**
   * @return read-only set consisting of not null results of {@code mapper.fun} for each element in {@code collection}
   */
  @Contract(pure = true)
  
  public static  <T, V> Set<V> map2SetNotNull( Collection<? extends T> collection,  Function<? super T, ? extends V> mapper) {
    if (collection.isEmpty()) return Collections.emptySet();
    Set <V> set = new HashSet<>(collection.size());
    for (T t : collection) {
      V value = mapper.fun(t);
      if (value != null) {
        set.add(value);
      }
    }
    return set.isEmpty() ? Collections.emptySet() : set;
  }

  /**
   * @deprecated use {@link List#toArray(Object[])} instead
   */
  @Deprecated
  
  
  public static <T> T  [] toArray( List<T> collection, T  [] array) {
    return collection.toArray(array);
  }

  /**
   * @deprecated use {@link Collection#toArray(Object[])} instead
   */
  @Deprecated
  
  
  public static <T> T  [] toArray( Collection<? extends T> c, T  [] sample) {
    return c.toArray(sample);
  }

  
  public static <T> T  [] copyAndClear( Collection<? extends T> collection,  ArrayFactory<? extends T> factory, boolean clear) {
    int size = collection.size();
    T[] a = factory.create(size);
    if (size > 0) {
      a = collection.toArray(a);
      if (clear) collection.clear();
    }
    return a;
  }

  /**
   * @return read-only list consisting of elements in the input collection
   */
  @Contract(pure = true)
  
  public static  <T> List<T> copyList( List<? extends T> list) {
    if (list == Collections.emptyList()) {
      return Collections.emptyList();
    }
    if (list.size() == 1) {
      return new SmartList<>(list.get(0));
    }
    if (list.isEmpty()) {
      return new SmartList<>();
    }
    return new ArrayList<>(list);
  }

  @Contract(pure = true)
  public static  <T> Collection<T> toCollection( Iterable<? extends T> iterable) {
    //noinspection unchecked
    return iterable instanceof Collection ? (Collection<T>)iterable : newArrayList(iterable);
  }

  /**
   * @deprecated use the argument instead<br>
   *
   * DO NOT remove this method until {@link #toCollection(Iterable)} is removed
   * The former method is here to highlight incorrect usages of the latter.
   */
  @Deprecated
  @Contract(pure = true)
  public static  <T> Collection<T> toCollection( Collection<? extends T> iterable) {
    Logger.getInstance(ContainerUtil.class).error("use the argument, Luke");
    //noinspection unchecked
    return (Collection<T>)iterable;
  }

  /**
   * @return read-only list consisting of elements in the input collection
   */
  @Contract(pure = true)
  
  public static  <T> List<T> toList( Enumeration<? extends T> enumeration) {
    if (!enumeration.hasMoreElements()) {
      return Collections.emptyList();
    }

    List<T> result = new SmartList<>();
    while (enumeration.hasMoreElements()) {
      result.add(enumeration.nextElement());
    }
    return Collections.unmodifiableList(result);
  }

  @Contract(value = "null -> true", pure = true)
  public static <T> boolean isEmpty( Collection<? extends T> collection) {
    return collection == null || collection.isEmpty();
  }

  @Contract(value = "null -> true", pure = true)
  public static boolean isEmpty( Map<?, ?> map) {
    return map == null || map.isEmpty();
  }

  /**
   * @return read-only list consisting of elements in the input collection or {@link #emptyList()} if the {@code list} is null
   */
  @Contract(pure = true)
  
  public static  <T> List<T> notNullize( List<T> list) {
    return list == null ? emptyList() : list;
  }

  /**
   * @return read-only set consisting of elements in the input collection or {@link Collections#emptySet()} if the {@code set} is null
   */
  @Contract(pure = true)
  
  public static  <T> Set<T> notNullize( Set<T> set) {
    return set == null ? Collections.emptySet() : set;
  }

  /**
   * @return read-only map consisting of elements in the input collection or {@link Collections#emptyMap()} if the collection is null
   */
  @Contract(pure = true)
  
  public static  <K, V> Map<K, V> notNullize( Map<K, V> map) {
    return map == null ? Collections.emptyMap() : map;
  }

  @Contract(pure = true)
  public static <T> boolean startsWith( List<? extends T> list,  List<? extends T> prefix) {
    return list.size() >= prefix.size() && list.subList(0, prefix.size()).equals(prefix);
  }

  @Contract(pure = true)
  public static  <C extends Collection<?>> C nullize( C collection) {
    return isEmpty(collection) ? null : collection;
  }

  @Contract(pure=true)
  public static <T extends Comparable<? super T>> int compareLexicographically( List<? extends T> o1,  List<? extends T> o2) {
    for (int i = 0; i < Math.min(o1.size(), o2.size()); i++) {
      int result = Comparing.compare(o1.get(i), o2.get(i));
      if (result != 0) {
        return result;
      }
    }
    return Integer.compare(o1.size(), o2.size());
  }

  @Contract(pure=true)
  public static <T> int compareLexicographically( List<? extends T> o1,  List<? extends T> o2,  Comparator<? super T> comparator) {
    for (int i = 0; i < Math.min(o1.size(), o2.size()); i++) {
      int result = comparator.compare(o1.get(i), o2.get(i));
      if (result != 0) {
        return result;
      }
    }
    return Integer.compare(o1.size(), o2.size());
  }

  public static final class KeyOrderedMultiMap<K extends Comparable<? super K>, V> extends MultiMap<K, V> {
    public KeyOrderedMultiMap() {
      super(new TreeMap<>());
    }

    public KeyOrderedMultiMap( MultiMap<? extends K, ? extends V> toCopy) {
      super(new TreeMap<>());
      putAllValues(toCopy);
    }

    public  NavigableSet<K> navigableKeySet() {
      return ((TreeMap<K, Collection<V>>)myMap).navigableKeySet();
    }
  }

  /**
   * Create a hard-key soft-value hash map.
   * Null keys are NOT allowed
   * Null values are allowed
   */
  @Contract(value = " -> new", pure = true)
  public static  <K,V> Map<K,V> createSoftValueMap() {
    return new SoftValueHashMap<>();
  }

  /**
   * Create a hard-key weak-value hash map.
   * Null keys are NOT allowed
   * Null values are allowed
   */
  @Contract(value = " -> new", pure = true)
  public static  <K,V> Map<K,V> createWeakValueMap() {
    //noinspection deprecation
    return new WeakValueHashMap<>();
  }

  /**
   * @deprecated use {@link java.util.WeakHashMap} instead
   */
  @Contract(value = " -> new", pure = true)
  @Deprecated
  public static  <K,V> Map<K,V> createWeakMap() {
    return new WeakHashMap<>();
  }

  @Contract(value = " -> new", pure = true)
  public static  <T> Set<T> createWeakSet() {
    return new WeakHashSet<>();
  }

  @Contract(value = " -> new", pure = true)
  public static  <T> IntObjectMap<T> createIntKeyWeakValueMap() {
    return new IntKeyWeakValueHashMap<>();
  }

  @Contract(value = " -> new", pure = true)
  public static  <T> ObjectIntMap<T> createWeakKeyIntValueMap() {
    return new WeakKeyIntValueHashMap<>();
  }

  public static <T> T reduce( List<? extends T> list, T identity,  BinaryOperator<T> accumulator) {
    T result = identity;
    for (T t : list) {
      result = accumulator.apply(result, t);
    }
    return result;
  }

  /**
   * Split the {@code list} into several lists containing exactly {@code chunkSize} elements,
   * (except for the last list with maybe fewer elements) and return the stream of these lists
   * @return stream of lists {@code chunkSize} maximum elements each
   */
  public static <T> Stream<List<? extends T>> splitListToChunks( List<? extends T> list, int chunkSize) {
    if (chunkSize <= 0) {
      throw new IllegalArgumentException("invalid chunkSize: " + chunkSize);
    }
    int listSize = list.size();
    if (listSize == 0) {
      return Stream.empty();
    }
    int numberOfChunks = listSize / chunkSize;
    return IntStream.range(0, numberOfChunks * chunkSize == listSize ? numberOfChunks : numberOfChunks + 1)
      .mapToObj(i -> list.subList(i * chunkSize, Math.min(listSize, i * chunkSize + chunkSize)));
  }

  //Inserted for kotlin plugin!

  public static <T> TObjectHashingStrategy<T> canonicalStrategy() {
    return TObjectHashingStrategy.CANONICAL;
  }

  public static <V>  ConcurrentIntObjectMap<V> createConcurrentIntObjectWeakValueMap() {
    return new ConcurrentIntKeyWeakValueHashMap();
  }

  public static <T> List<T> immutableCopy(List<? extends T> list) {
    List var10000;
    if (list.isEmpty()) {
      var10000 = Collections.emptyList();
      return var10000;
    } else if (list.size() == 1) {
      var10000 = Collections.singletonList(list.get(0));
      return var10000;
    } else {
      return (List<T>) immutableList(list.toArray());
    }
  }
}