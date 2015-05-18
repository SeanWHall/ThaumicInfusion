package drunkmafia.thaumicinfusion.common.util;

import java.lang.reflect.Array;

@SuppressWarnings("unchecked")
public class Coordinate2List<T> {

    private T[][] posPos, negNeg, posNeg, negPos;

    private Class<T> tClass;

    /**
     * @param initalSize of arrays, this will increase put Times
     * @param tClass used to create the arrays
     */
    public Coordinate2List(Class<T> tClass, int initalSize){
        this.tClass = tClass;

        posPos =  (T[][]) Array.newInstance(tClass, initalSize, initalSize);
        negNeg =  (T[][]) Array.newInstance(tClass, initalSize, initalSize);
        posNeg =  (T[][]) Array.newInstance(tClass, initalSize, initalSize);
        negPos =  (T[][]) Array.newInstance(tClass, initalSize, initalSize);
    }

    public Coordinate2List(Class<T> tClass){
        this(tClass, 1);
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
        x = Math.abs(x);
        z = Math.abs(z);

        T[][] array = (xPos && zPos) ? posPos : (!xPos && zPos) ? negPos : (xPos && !zPos) ? posNeg : negNeg;
        if(x >= array.length) array = changeArraySize(array, x + 100);
        if(z >= array[x].length) array[x] = changeArraySize(array[x], z + 100);

        array[x][z] = element;

        if(xPos && zPos) posPos = array;
        if(!xPos && zPos) negPos = array;
        if(xPos && !zPos) posNeg = array;
        if(!xPos && !zPos) negNeg = array;
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
