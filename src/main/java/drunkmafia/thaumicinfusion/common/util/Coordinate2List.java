/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.util;

import java.lang.reflect.Array;

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
 */
@SuppressWarnings("unchecked")
public class Coordinate2List<T> {

    /**
     * Arrays for each type of position, they hold the index of the data stored in the elementData array
     */
    private Integer[][] posPos, negNeg, posNeg, negPos;

    private int initialSize;

    /**
     * All elements stored in this list are in here, the data is constent and stays at is position during resize
     */
    private T[] elementData;

    /**
     * The margin which the arrays are shifted by when being resized
     */
    private int shitMargin;
    private int attempt = 0, maxAttempts;

    private Class<T> tClass;

    /**
     * @param initialSize of arrays, this will increase put Times
     * @param tClass used to create the arrays
     * @param shitMargin used to push the array size up to give some leeway
     * @param maxAttempts used to ensure that the list does not cause an infinite loop when adding data
     */
    public Coordinate2List(Class<T> tClass, int initialSize, int shitMargin, int maxAttempts){
        if(tClass == null || initialSize < 0 || shitMargin < 0)
            throw new IllegalArgumentException("Bad Arguments, Failed to create list. Class: " + tClass + " Size: " + initialSize + " Shift: " + shitMargin);

        this.tClass = tClass;
        this.shitMargin = shitMargin;
        this.initialSize = initialSize;
        this.maxAttempts = maxAttempts;

        removeAll();
    }

    public Coordinate2List(Class<T> tClass){
        this(tClass, 1, 100, 5);
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
        Integer[][] array = getArray(xPos, zPos);

        x = (x < 0) ? -x : x;
        z = (z < 0) ? -z : z;

        if(x >= array.length) array = changeArraySize(array, x + shitMargin, new Integer[x + shitMargin], Integer.class);
        if(array[x] != null && z >= array[x].length) array[x] = changeArraySize(array[x], z + shitMargin, Integer.class);

        if(element != null)
            array[x][z] = addElement(element);
        else if (array[x][z] != null) {
            elementData[array[x][z]] = null;
            array[x][z] = null;
        }

        setArray(xPos, zPos, array);
        return element;
    }

    /**
     * Attempts to add the element to the list and then returns the index of it, to be placed in the
     * position arrays.
     *
     * This has the possibility to cause an infinite loop, so there is a max amount of attempts before erroring
     *
     * @param element the element to be added
     **/
    public int addElement(T element){
        if(attempt++ > maxAttempts)
            throw new IllegalArgumentException("Exceeded max amount of attempts to add data to list");

        for(int i = 0; i < elementData.length; i++){
            if(elementData[i] == null){
                elementData[i] = element;
                attempt = 0;
                return i;
            }
        }
        elementData = changeArraySize(elementData, elementData.length + shitMargin + 1, tClass);
        return addElement(element);
    }

    /**
     * Directly accesses the array at the specified look up, nominal lookup due to this
     * @param x pos
     * @param z pos
     * @return Element at that position, can be null
     */
    public T get(int x, int z){
        Integer[][] array = getArray(x > 0, z > 0);

        x = (x < 0) ? -x : x;
        z = (z < 0) ? -z : z;
        return (x < array.length && z < array[x].length && array[x][z] != null) ? elementData[array[x][z]] : null;
    }

    /**
     * Gets the appropriate array
     * @param xPos is the x positive or negative
     * @param zPos is the z positive or negative
     **/
    private Integer[][] getArray(boolean xPos, boolean zPos){
        return xPos && zPos ? posPos : !xPos && zPos ? negPos : xPos ? posNeg : negNeg;
    }

    public T[] toArray() {
        return elementData;
    }

    /**
     * Sets the changes of an array to its appropriate global array
     * @param xPos is the x positive or negative
     * @param zPos is the z positive or negative
     * @param array the array to set
     **/
    private void setArray(boolean xPos, boolean zPos, Integer[][] array){
        if(xPos && zPos) posPos = array;
        if(!xPos && zPos) negPos = array;
        if(xPos && !zPos) posNeg = array;
        if(!xPos && !zPos) negNeg = array;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " Type: " + tClass.getSimpleName();
    }

    /**
     * Recreates the list, removing all stored objects
     */
    public void removeAll(){
        elementData = (T[]) Array.newInstance(tClass, initialSize);

        posPos = new Integer[initialSize][initialSize];
        negNeg = new Integer[initialSize][initialSize];
        posNeg = new Integer[initialSize][initialSize];
        negPos = new Integer[initialSize][initialSize];
    }

    /**
     * Resize a 1 dimensional array
     * @param old array
     * @param newSize of the array to create
     * @return New array with old elements and new size
     */
    protected <E>E[] changeArraySize(E[] old, int newSize, Class<E> type){
        E[] newArray = (E[]) Array.newInstance(type, newSize);
        System.arraycopy(old, 0, newArray, 0, old.length);
        return newArray;
    }
    /**
     * Resize a 2 dimensional array
     * @param old array
     * @param newSize of the array to create
     * @return New array with old elements and new size
     */
    protected <E>E[][] changeArraySize(E[][] old, int newSize, E[] defaultVal, Class<E> type){
        E[][] newArray = (E[][]) Array.newInstance(type, newSize, newSize);
        System.arraycopy(old, 0, newArray, 0, old.length);
        for(int i = 0; i < newArray.length; i++)
            if(newArray[i] == null) newArray[i] = defaultVal;
        return newArray;
    }
}