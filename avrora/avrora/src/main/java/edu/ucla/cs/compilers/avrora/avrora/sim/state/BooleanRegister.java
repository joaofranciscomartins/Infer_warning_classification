/**
 * Copyright (c) 2007, Ben L. Titzer
 * See the file "license.txt" for details.
 *
 * Created Nov 4, 2007
 */
package edu.ucla.cs.compilers.avrora.avrora.sim.state;

/**
 * The <code>BooleanRegister</code> definition.
 *
 * @author Ben L. Titzer
 */
public class BooleanRegister implements BooleanView
{

    protected ValueSetListener listener;
    protected boolean value;


    @Override
    public boolean getValue()
    {
        return value;
    }


    @Override
    public void setValue(boolean v)
    {
        value = v;
        if (listener != null)
        {
            listener.onValueSet(this, v);
        }
    }


    @Override
    public void setValueSetListener(ValueSetListener listener)
    {
        this.listener = listener;
    }
}
