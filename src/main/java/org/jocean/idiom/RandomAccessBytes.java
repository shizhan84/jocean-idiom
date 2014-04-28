/**
 * 
 */
package org.jocean.idiom;

/**
 * @author isdom
 *
 */
public interface RandomAccessBytes {
    
    public void setCapacity(final int capacity);
    
    public int getCapacity();
    
    public void writeAt(final int index, final int b);
}
