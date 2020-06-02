package edu.ucla.cs.compilers.avrora.avrora.sim.state;

import edu.ucla.cs.compilers.avrora.avrora.sim.state.BooleanView;
import edu.ucla.cs.compilers.avrora.avrora.sim.state.BooleanView.ValueSetListener;

/**
 * Mock implementation of a ValueSetListener
 * 
 * @author Matthias Linder
 */
public class MockBooleanValueSetListener implements ValueSetListener
{
    public boolean wasCalled;
    public boolean newValue;


    @Override
    public void onValueSet(BooleanView view, boolean newValue)
    {
        wasCalled = true;
        this.newValue = newValue;
    }

}
