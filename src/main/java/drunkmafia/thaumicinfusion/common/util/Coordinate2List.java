package drunkmafia.thaumicinfusion.common.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * This works based on a graph design to allow for negative coordinates:
 *          X
 *          |
 *          |
 *   PosNeg | PosPos
 *  ------------------- Z
 *   NegNeg | NegPos
 *          |
 *          |
 * Making it ideal for coordinate based lists, it gives a faster lookup time than lists and maps
 * since you need to know the coordinates of the element you want to look up.
 *
 * This also works like the list, it will automatically resize itself to fit elements. Run the clean
 * every so often to trim the arrays.
 *
 * @author TheDrunkMafia
 */
@SuppressWarnings("unchecked")
public class Coordinate2List<T>{

    /**
     * The array buffer into which the elements of the ArrayList are stored.
     */
    private transient T[][] posPos, negNeg, posNeg, negPos;

    private int size, initalSize;

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
    public T set(T element, int x, int z) {
        boolean xPos = x > 0, zPos = z > 0;
        x = Math.abs(x);
        z = Math.abs(z);

        T[][] array = (xPos && zPos) ? posPos : (!xPos && zPos) ? negPos : (xPos && !zPos) ? posNeg : negNeg;
        if(x >= array.length) array = changeArraySize(array, x + shitMargin);
        if(z >= array[x].length) array[x] = changeArraySize(array[x], z + shitMargin);

        size += element != null ? 1 : -1;
        array[x][z] = element;

        if(xPos && zPos) posPos = array;
        if(!xPos && zPos) negPos = array;
        if(xPos && !zPos) posNeg = array;
        if(!xPos && !zPos) negNeg = array;

        return array[x][z];
    }

    /**
     * Directly accesses the array at the specfied look up, nominal lookup due to this
     * @param x pos
     * @param z pos
     * @return Element at that position, can be null
     */
    public T get(int x, int z){
        boolean xPos = x > 0, zPos = z > 0;
        x = Math.abs(x);
        z = Math.abs(z);

        T[][] array = (xPos && zPos) ? posPos : (!xPos && zPos) ? negPos : (xPos && !zPos) ? posNeg : negNeg;
        return (x < array.length && z < array[x].length) ? tClass.cast(array[x][z]) : null;
    }

    public List<T> toList() {
        List<T> list = new ArrayList<T>();
        arrayToList(posPos, list);
        arrayToList(negNeg, list);
        arrayToList(negPos, list);
        arrayToList(posNeg, list);
        return list;
    }

    private void arrayToList(T[][] toAdd, List<T> array) {
        for (int x = 0; x < toAdd.length; x++)
            for (int z = 0; z < toAdd[x].length; z++)
                array.add(toAdd[x][z]);
    }

    public void cleanAll(){
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

    public void remove(int x, int z){
        set(null, x, z);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " - Size: " + size + " Type: " + tClass.getSimpleName();
    }

    /**
     * Recreates the list, removing all stored objects
     */
    public void removeAll(){
        size = 0;

        posPos =  (T[][]) Array.newInstance(tClass, initalSize, initalSize);
        negNeg =  (T[][]) Array.newInstance(tClass, initalSize, initalSize);
        posNeg =  (T[][]) Array.newInstance(tClass, initalSize, initalSize);
        negPos =  (T[][]) Array.newInstance(tClass, initalSize, initalSize);
    }

    /**
     * @return is the list empty
     */
    public boolean isEmpty(){
        return size == 0;
    }

    /**
     * @return Total size of the list
     */
    public int size(){
        return size;
    }

    public T[][] getPosPos(){
        return posPos;
    }

    public T[][] getNegNeg(){
        return negNeg;
    }

    public T[][] getNegPos(){
        return negPos;
    }

    public T[][] getPosNeg(){
        return posNeg;
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
}
