package CGA.User.DataStructures.Geometry;

/**
 * Simple class that holds all information about a single vertex attribute
 */
public class VertexAttribute
{
    public int n;
    public int type;
    public int stride;
    public int offset;

    /**
     * Creates a VertexAttribute object
     * @param n         Number of components of this attribute
     * @param type      Type of this attribute
     * @param stride    Size in bytes of a whole vertex
     * @param offset    Offset in bytes from the beginning of the vertex to the location of this attribute data
     */
    public VertexAttribute(int n, int type, int stride, int offset)
    {
        this.n = n;
        this.type = type;
        this.stride = stride;
        this.offset = offset;
    }
}