package drunkmafia.thaumicinfusion.common.util.quadtree;

public class Point<T> implements Comparable {

    private double x;
    private double y;
    private T opt_value;

    /**
     * Creates a new point object.
     *
     * @param {double} x The x-coordinate of the point.
     * @param {double} y The y-coordinate of the point.
     * @param {Object} opt_value Optional value associated with the point.
     */
    public Point(double x, double y, T opt_value) {
        this.x = x;
        this.y = y;
        this.opt_value = opt_value;
    }

    public double getX() {
        return this.x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public T getValue() {
        return this.opt_value;
    }

    public void setValue(T opt_value) {
        this.opt_value = opt_value;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    public int compareTo(Object o) {
        Point tmp = (Point) o;
        if (x < tmp.x) {
            return -1;
        } else if (x > tmp.x) {
            return 1;
        } else {
            if (y < tmp.y) {
                return -1;
            } else if (y > tmp.y) {
                return 1;
            }
            return 0;
        }

    }

}
