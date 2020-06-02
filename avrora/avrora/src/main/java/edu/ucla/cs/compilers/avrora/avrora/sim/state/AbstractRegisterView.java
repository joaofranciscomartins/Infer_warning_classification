package edu.ucla.cs.compilers.avrora.avrora.sim.state;

import java.util.LinkedList;

/**
 * Abstract implementation of the {@link RegisterView} which implements event
 * handling.
 */
public abstract class AbstractRegisterView implements RegisterView
{
    private LinkedList<RegisterValueSetListener> listeners;

    /**
     * Notifies all registered listeners of a changed value.
     *
     * @param oldValue value before change
     * @param newValue value after change
     */
    protected void notify(int oldValue, int newValue)
    {
        if (listeners != null)
        {
            for (RegisterValueSetListener l : listeners)
            {
                l.onValueSet(this, oldValue, newValue);
            }
        }
    }


    @Override
    public void registerValueSetListener(RegisterValueSetListener listener)
    {
        if (listeners == null)
        {
            listeners = new LinkedList<RegisterValueSetListener>();
        }
        listeners.add(listener);
    }

}
