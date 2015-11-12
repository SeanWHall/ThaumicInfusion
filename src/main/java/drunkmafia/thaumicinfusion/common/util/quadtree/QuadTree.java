package drunkmafia.thaumicinfusion.common.util.quadtree;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Datastructure: A point Quad Tree for representing 2D data. Each
 * region has the same ratio as the bounds for the tree.
 * <p/>
 * The implementation currently requires pre-determined bounds for data as it
 * can not rebalance itself to that degree.
 */
public class QuadTree<T> {

    private final Class<T> tClass;
    private final Node root_;
    private int count_;

    /**
     * Constructs a new quad tree.
     * <p/>
     * {@param double minX Minimum x-value that can be held in tree.}
     * {@param double minY Minimum y-value that can be held in tree.}
     * {@param double maxX Maximum x-value that can be held in tree.}
     * {@param double maxY Maximum y-value that can be held in tree.}
     */
    public QuadTree(Class<T> tClass, double minX, double minY, double maxX, double maxY) {
        this.tClass = tClass;
        root_ = new Node(minX, minY, maxX - minX, maxY - minY, null);
    }

    /**
     * Returns a reference to the tree's root node.  Callers shouldn't modify nodes,
     * directly.  This is a convenience for visualization and debugging purposes.
     *
     * @return {Node} The root node.
     */
    public Node getRootNode() {
        return root_;
    }

    /**
     * Sets the value of an (x, y) point within the quad-tree.
     * <p/>
     * {@param double x The x-coordinate.}
     * {@param double y The y-coordinate.}
     * {@param Object value The value associated with the point.}
     */
    public void set(double x, double y, T value) {

        Node root = root_;
        if (x < root.getX() || y < root.getY() || x > root.getX() + root.getW() || y > root.getY() + root.getH()) {
            throw new QuadTreeException("Out of bounds : (" + x + ", " + y + ")");
        }
        if (insert(root, new Point<T>(x, y, value))) {
            count_++;
        }
    }

    /**
     * Gets the value of the point at (x, y) or null if the point is empty.
     * <p/>
     * {@param double x The x-coordinate.}
     * {@param double y The y-coordinate.}
     * {@param Object opt_default The default value to return if the node doesn't
     * exist.}
     *
     * @return {*} The value of the node, the default value if the node
     * doesn't exist, or undefined if the node doesn't exist and no default
     * has been provided.
     */
    public T get(double x, double y, T opt_default) {
        Node node = find(root_, x, y);
        return node != null ? ((Point<T>) node.getPoint()).getValue() : opt_default;
    }

    /**
     * Removes a point from (x, y) if it exists.
     * <p/>
     * {@param double x The x-coordinate.}
     * {@param double y The y-coordinate.}
     *
     * @return {Object} The value of the node that was removed, or null if the
     * node doesn't exist.
     */
    public Object remove(double x, double y) {
        Node node = find(root_, x, y);
        if (node != null) {
            Object value = node.getPoint().getValue();
            node.setPoint(null);
            node.setNodeType(Node.NodeType.EMPTY);
            balance(node);
            count_--;
            return value;
        } else {
            return null;
        }
    }

    /**
     * Returns true if the point at (x, y) exists in the tree.
     * <p/>
     * {@param double x The x-coordinate.}
     * {@param double y The y-coordinate.}
     *
     * @return {boolean} Whether the tree contains a point at (x, y).
     */
    public boolean contains(double x, double y) {
        return get(x, y, null) != null;
    }

    /**
     * @return {boolean} Whether the tree is empty.
     */
    public boolean isEmpty() {
        return root_.getNodeType() == Node.NodeType.EMPTY;
    }

    /**
     * @return {number} The number of items in the tree.
     */
    public int getCount() {
        return count_;
    }

    /**
     * Removes all items from the tree.
     */
    public void clear() {
        root_.setNw(null);
        root_.setNe(null);
        root_.setSw(null);
        root_.setSe(null);
        root_.setNodeType(Node.NodeType.EMPTY);
        root_.setPoint(null);
        count_ = 0;
    }

    /**
     * Returns an array containing the coordinates of each point stored in the tree.
     *
     * @return {Array.<Point>} Array of coordinates.
     */
    public Point[] getKeys() {
        final List<Point> arr = new ArrayList<Point>();
        traverse(root_, new QuadTree.Func() {
            @Override
            public void call(QuadTree quadTree, Node node) {
                arr.add(node.getPoint());
            }
        });
        return arr.toArray(new Point[arr.size()]);
    }

    /**
     * Returns an array containing all values stored within the tree.
     *
     * @return {Array.<Object>} The values stored within the tree.
     */
    public T[] getValues() {
        final List<Object> arr = new ArrayList<Object>();
        traverse(root_, new QuadTree.Func() {
            @Override
            public void call(QuadTree quadTree, Node node) {
                arr.add(node.getPoint().getValue());
            }
        });

        return arr.toArray((T[]) Array.newInstance(this.tClass, arr.size()));
    }

    public Point<T>[] searchIntersect(final double xmin, final double ymin, final double xmax, final double ymax) {
        final List<Point> arr = new ArrayList<Point>();
        navigate(root_, new QuadTree.Func() {
            @Override
            public void call(QuadTree quadTree, Node node) {
                Point pt = node.getPoint();
                if (pt.getX() >= xmin && pt.getX() <= xmax && pt.getY() >= ymin && pt.getY() <= ymax)
                    arr.add(node.getPoint());
            }
        }, xmin, ymin, xmax, ymax);
        return arr.toArray(new Point[arr.size()]);
    }

    public Point<T>[] searchWithin(final double xmin, final double ymin, final double xmax, final double ymax) {
        final List<Point> arr = new ArrayList<Point>();
        navigate(root_, new QuadTree.Func() {
            @Override
            public void call(QuadTree quadTree, Node node) {
                Point pt = node.getPoint();
                if (pt.getX() > xmin && pt.getX() < xmax && pt.getY() > ymin && pt.getY() < ymax)
                    arr.add(node.getPoint());
            }
        }, xmin, ymin, xmax, ymax);
        return arr.toArray(new Point[arr.size()]);
    }

    public List<T> searchWithinObject(final double xmin, final double ymin, final double xmax, final double ymax) {
        final List<T> arr = new ArrayList<T>();
        navigate(root_, new QuadTree.Func() {
            @Override
            public void call(QuadTree quadTree, Node node) {
                Point pt = node.getPoint();
                if (pt.getX() >= xmin && pt.getX() <= xmax && pt.getY() >= ymin && pt.getY() <= ymax)
                    arr.add((T) node.getPoint().getValue());
            }
        }, xmin, ymin, xmax, ymax);
        return arr;
    }

    public void navigate(Node node, QuadTree.Func func, double xmin, double ymin, double xmax, double ymax) {
        switch (node.getNodeType()) {
            case LEAF:
                func.call(this, node);
                break;

            case POINTER:
                if (this.intersects(xmin, ymax, xmax, ymin, node.getNe()))
                    navigate(node.getNe(), func, xmin, ymin, xmax, ymax);
                if (this.intersects(xmin, ymax, xmax, ymin, node.getSe()))
                    navigate(node.getSe(), func, xmin, ymin, xmax, ymax);
                if (this.intersects(xmin, ymax, xmax, ymin, node.getSw()))
                    navigate(node.getSw(), func, xmin, ymin, xmax, ymax);
                if (this.intersects(xmin, ymax, xmax, ymin, node.getNw()))
                    navigate(node.getNw(), func, xmin, ymin, xmax, ymax);
                break;
        }
    }

    private boolean intersects(double left, double bottom, double right, double top, Node node) {
        return !(node.getX() > right ||
                node.getX() + node.getW() < left ||
                node.getY() > bottom ||
                node.getY() + node.getH() < top);
    }

    /**
     * Clones the quad-tree and returns the new instance.
     *
     * @return {QuadTree} A clone of the tree.
     */
    public QuadTree clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        double x1 = root_.getX();
        double y1 = root_.getY();
        double x2 = x1 + root_.getW();
        double y2 = y1 + root_.getH();
        final QuadTree clone = new QuadTree(this.tClass, x1, y1, x2, y2);
        // This is inefficient as the clone needs to recalculate the structure of the
        // tree, even though we know it already.  But this is easier and can be
        // optimized when/if needed.
        traverse(root_, new QuadTree.Func() {
            @Override
            public void call(QuadTree quadTree, Node node) {
                clone.set(node.getPoint().getX(), node.getPoint().getY(), node.getPoint().getValue());
            }
        });


        return clone;
    }

    /**
     * Traverses the tree depth-first, with quadrants being traversed in clockwise
     * order (NE, SE, SW, NW).  The provided function will be called for each
     * leaf node that is encountered.
     * {@param QuadTree.Node node The current node.}
     * {@param function(QuadTree.Node) fn The function to call}
     * for each leaf node. This function takes the node as an argument, and its
     * return value is irrelevant.
     */
    public void traverse(Node node, QuadTree.Func func) {
        switch (node.getNodeType()) {
            case LEAF:
                func.call(this, node);
                break;

            case POINTER:
                traverse(node.getNe(), func);
                traverse(node.getSe(), func);
                traverse(node.getSw(), func);
                traverse(node.getNw(), func);
                break;
        }
    }

    /**
     * Finds a leaf node with the same (x, y) coordinates as the target point, or
     * null if no point exists.
     * {@param QuadTree.Node node The node to search in.}
     * {@param number x The x-coordinate of the point to search for.}
     * {@param number y The y-coordinate of the point to search for.}
     *
     * @return {QuadTree.Node} The leaf node that matches the target,
     * or null if it doesn't exist.
     */
    public Node find(Node node, double x, double y) {
        Node resposne = null;
        switch (node.getNodeType()) {
            case EMPTY:
                break;

            case LEAF:
                if (node.getPoint() == null) return null;
                resposne = node.getPoint().getX() == x && node.getPoint().getY() == y ? node : null;
                break;

            case POINTER:
                resposne = find(getQuadrantForPoint(node, x, y), x, y);
                break;

            default:
                throw new QuadTreeException("Invalid nodeType");
        }
        return resposne;
    }

    /**
     * Inserts a point into the tree, updating the tree's structure if necessary.
     * {@param .QuadTree.Node parent The parent to insert the point
     * into.}
     * {@param QuadTree.Point} point The point to insert.}
     *
     * @return {boolean} True if a new node was added to the tree; False if a node
     * already existed with the correpsonding coordinates and had its value
     * reset.
     */
    private boolean insert(Node parent, Point point) {
        Boolean result;
        switch (parent.getNodeType()) {
            case EMPTY:
                setPointForNode(parent, point);
                result = true;
                break;
            case LEAF:
                if (parent.getPoint().getX() == point.getX() && parent.getPoint().getY() == point.getY()) {
                    setPointForNode(parent, point);
                    result = false;
                } else {
                    split(parent);
                    result = insert(parent, point);
                }
                break;
            case POINTER:
                result = insert(
                        getQuadrantForPoint(parent, point.getX(), point.getY()), point);
                break;

            default:
                throw new QuadTreeException("Invalid nodeType in parent");
        }
        return result;
    }

    /**
     * Converts a leaf node to a pointer node and reinserts the node's point into
     * the correct child.
     * {@param QuadTree.Node node The node to split.}
     */
    private void split(Node node) {
        Point oldPoint = node.getPoint();
        node.setPoint(null);

        node.setNodeType(Node.NodeType.POINTER);

        double x = node.getX();
        double y = node.getY();
        double hw = node.getW() / 2;
        double hh = node.getH() / 2;

        node.setNw(new Node(x, y, hw, hh, node));
        node.setNe(new Node(x + hw, y, hw, hh, node));
        node.setSw(new Node(x, y + hh, hw, hh, node));
        node.setSe(new Node(x + hw, y + hh, hw, hh, node));

        insert(node, oldPoint);
    }

    /**
     * Attempts to balance a node. A node will need balancing if all its children
     * are empty or it contains just one leaf.
     * {@param QuadTree.Node node The node to balance.}
     */
    private void balance(Node node) {
        switch (node.getNodeType()) {
            case EMPTY:
            case LEAF:
                if (node.getParent() != null) {
                    balance(node.getParent());
                }
                break;

            case POINTER:
                Node nw = node.getNw();
                Node ne = node.getNe();
                Node sw = node.getSw();
                Node se = node.getSe();
                Node firstLeaf = null;

                // Look for the first non-empty child, if there is more than one then we
                // break as this node can't be balanced.
                if (nw.getNodeType() != Node.NodeType.EMPTY) {
                    firstLeaf = nw;
                }
                if (ne.getNodeType() != Node.NodeType.EMPTY) {
                    if (firstLeaf != null) {
                        break;
                    }
                    firstLeaf = ne;
                }
                if (sw.getNodeType() != Node.NodeType.EMPTY) {
                    if (firstLeaf != null) {
                        break;
                    }
                    firstLeaf = sw;
                }
                if (se.getNodeType() != Node.NodeType.EMPTY) {
                    if (firstLeaf != null) {
                        break;
                    }
                    firstLeaf = se;
                }

                if (firstLeaf == null) {
                    // All child nodes are empty: so make this node empty.
                    node.setNodeType(Node.NodeType.EMPTY);
                    node.setNw(null);
                    node.setNe(null);
                    node.setSw(null);
                    node.setSe(null);

                } else if (firstLeaf.getNodeType() == Node.NodeType.POINTER) {
                    // Only child was a pointer, therefore we can't rebalance.
                    break;

                } else {
                    // Only child was a leaf: so update node's point and make it a leaf.
                    node.setNodeType(Node.NodeType.LEAF);
                    node.setNw(null);
                    node.setNe(null);
                    node.setSw(null);
                    node.setSe(null);
                    node.setPoint(firstLeaf.getPoint());
                }

                // Try and balance the parent as well.
                if (node.getParent() != null) {
                    this.balance(node.getParent());
                }
                break;
        }
    }

    /**
     * Returns the child quadrant within a node that contains the given (x, y)
     * coordinate.
     * {@param QuadTree.Node parent The node.}
     * {@param number x The x-coordinate to look for.}
     * {@param number y The y-coordinate to look for.}
     *
     * @return {QuadTree.Node} The child quadrant that contains the
     * point.
     */
    private Node getQuadrantForPoint(Node parent, double x, double y) {
        double mx = parent.getX() + parent.getW() / 2;
        double my = parent.getY() + parent.getH() / 2;
        if (x < mx) {
            return y < my ? parent.getNw() : parent.getSw();
        } else {
            return y < my ? parent.getNe() : parent.getSe();
        }
    }

    /**
     * Sets the point for a node, as long as the node is a leaf or empty.
     * {@param QuadTree.Node node The node to set the point for.}
     * {@param QuadTree.Point point The point to set.}
     */
    private void setPointForNode(Node node, Point point) {
        if (node.getNodeType() == Node.NodeType.POINTER) {
            throw new QuadTreeException("Can not set point for node of type POINTER");
        }
        node.setNodeType(Node.NodeType.LEAF);
        node.setPoint(point);
    }

    public interface Func {
        void call(QuadTree quadTree, Node node);
    }
}
