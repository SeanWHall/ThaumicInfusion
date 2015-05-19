package drunkmafia.thaumicinfusion.common.util;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;

/**
 * This works based on a graph design to allow for negative coordinates:
 *
 *        X  | Z
 *      -----------
 *       Pos | Neg
 *       Neg | Pos
 *       Pos | Pos
 *       Neg | Neg
 *
 * Making it ideal for coordinate based lists, it gives a faster lookup time than lists and maps
 * since you need to know the coordinates of the element you want to look up.
 *
 * This also works like the list, it will automatically resize itself to fit elements. Run the clean
 * every so often to trim the arrays.
 *
 * @author TheDrunkMafia
 */
@SuppressWarnings("unchecked")
public class Coordinate2List<T> implements Iterable<T>, RandomAccess, java.io.Serializable{

    /**
     * The array buffer into which the elements of the ArrayList are stored.
     */
    private transient T[][] posPos, negNeg, posNeg, negPos;

    private ArrayList<T> elements;
    private int initalSize;

    /**
     * The margin which the arrays are shifted by when being resized
     */
    private int shitMargin;
    private Class<T> tClass;

    /**
     * @param initalSize of arrays, this will increase put Times
     * @param tClass used to create the arrays
     * @param shitMargin used to push the array size up to give some leeway
     */
    public Coordinate2List(Class<T> tClass, int initalSize, int shitMargin){
        if(tClass == null || initalSize < 0 || shitMargin < 0)
            throw new IllegalArgumentException("Bad Arguments, Failed to create list. Class: " + tClass + " Size: " + initalSize + " Shift: " + shitMargin);

        this.tClass = tClass;
        this.shitMargin = shitMargin;
        this.initalSize = initalSize;

        elements = new ArrayList(initalSize);

        posPos =  (T[][]) Array.newInstance(tClass, initalSize, initalSize);
        negNeg =  (T[][]) Array.newInstance(tClass, initalSize, initalSize);
        posNeg =  (T[][]) Array.newInstance(tClass, initalSize, initalSize);
        negPos =  (T[][]) Array.newInstance(tClass, initalSize, initalSize);
    }

    public Coordinate2List(Class<T> tClass){
        this(tClass, 1, 100);
    }

    public Coordinate2List(){
        this((Class<T>) Object.class);
    }

    /**
     *
     * Puts the element at a certain X & Z position
     *
     * @param element to be added to list
     * @param x pos
     * @param z pos
     */
    public void set(T element, int x, int z){
        boolean xPos = x > 0, zPos = z > 0;

        x = (z < 0) ? -z : z;
        z = (z < 0) ? -z : z;

        T[][] array = getArray(xPos, zPos);

        if(x >= array.length) array = changeArraySize(array, x + shitMargin);
        if(z >= array[x].length) array[x] = changeArraySize(array[x], z + shitMargin);

        if(element != null) elements.add(element);
        else elements.remove(array[x][z]);
        array[x][z] = element;

        setArray(xPos, zPos, array);
    }

    /**
     * Directly accesses the array at the specified look up, nominal lookup due to this
     * @param x pos
     * @param z pos
     * @return Element at that position, can be null
     */
    public T get(int x, int z){
        boolean xPos = x > 0, zPos = z > 0;

        x = (z < 0) ? -z : z;
        z = (z < 0) ? -z : z;

        T[][] array = (xPos && zPos) ? posPos : (!xPos && zPos) ? negPos : (xPos && !zPos) ? posNeg : negNeg;
        return (x < array.length && z < array[x].length) ? array[x][z] : null;
    }

    private T[][] getArray(boolean xPos, boolean zPos){
        return xPos && zPos ? posPos : !xPos && zPos ? negPos : xPos ? posNeg : negNeg;
    }

    private void setArray(boolean xPos, boolean zPos, T[][] array){
        if(xPos && zPos) posPos = array;
        if(!xPos && zPos) negPos = array;
        if(xPos && !zPos) posNeg = array;
        if(!xPos && !zPos) negNeg = array;
    }

    public void cleanAll(){
        elements = new ArrayList(initalSize);

        posPos = clean(posPos);
        negNeg = clean(negNeg);
        negPos = clean(negPos);
        posNeg = clean(posNeg);
    }

    private T[][] clean(T[][] old){
        for(int x = 0; x < old.length; x++){
            int lastZ = 0;

            T[] array = old[x];
            for(int z = 0; z < old[x].length; z++){
                T obj = array[z];
                if(obj != null)
                    lastZ = z;
            }

            if(lastZ != array.length - 1)
                old[x] = changeArraySize(old[x], lastZ);
        }
        return old;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " - Size: " + elements.size() + " Type: " + tClass.getSimpleName();
    }

    /**
     * Recreates the list, removing all stored objects
     */
    public void removeAll(){
        elements = new ArrayList<>();

        posPos =  (T[][]) Array.newInstance(tClass, initalSize, initalSize);
        negNeg =  (T[][]) Array.newInstance(tClass, initalSize, initalSize);
        posNeg =  (T[][]) Array.newInstance(tClass, initalSize, initalSize);
        negPos =  (T[][]) Array.newInstance(tClass, initalSize, initalSize);
    }

    /**
     * @return is the list empty
     */
    public boolean isEmpty(){
        return elements.size() == 0;
    }

    /**
     * @return Total size of the list
     */
    public int size(){
        return elements.size();
    }

    public ArrayList<T> getElements(){
        return elements;
    }

    /**
     * Resize a 1 dimensional array
     * @param old array
     * @param newSize of the array to create
     * @return New array with old elements and new size
     */
    protected T[] changeArraySize(T[] old, int newSize){
        T[] newArray = (T[]) new Object[newSize];
        for(int i = 0; i < newSize; i++){
            if(i < old.length)
                newArray[i] = old[i];
        }
        return newArray;
    }

    /**
     * Resize a 2 dimensional array
     * @param old array
     * @param newSize of the array to create
     * @return New array with old elements and new size
     */
    protected T[][] changeArraySize(T[][] old, int newSize){
        T[][] newArray = (T[][]) new Object[newSize][newSize];
        System.arraycopy(old, 0, newArray, 0, old.length);
        return newArray;
    }

    public Iterator<T> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<T> {
        int cursor;
        int lastRet = -1;

        public T[] elementData;

        public Itr(){
            elementData = (T[]) Coordinate2List.this.elements.toArray();
        }

        public boolean hasNext() {
            return cursor != elementData.length;
        }

        @SuppressWarnings("unchecked")
        public T next() {
            int i = cursor;
            if (i >= elementData.length)
                throw new NoSuchElementException();
            cursor = i + 1;
            return elementData[lastRet = i];
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super T> consumer) {
            Objects.requireNonNull(consumer);
            final int size = elementData.length;
            int i = cursor;
            if (i >= size)
                return;

            if (i >= elementData.length)
                throw new ConcurrentModificationException();

            // update once at end of iteration to reduce heap write traffic
            cursor = i;
            lastRet = i - 1;
        }
    }
}
