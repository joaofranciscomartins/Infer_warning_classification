package edu.ucla.cs.compilers.avrora.avrora.sim.mcu;

import edu.ucla.cs.compilers.avrora.avrora.sim.Simulator;
import edu.ucla.cs.compilers.avrora.avrora.sim.clock.ClockDomain;
import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.MCUProperties;
import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.Microcontroller;
import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.RegisterSet;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.Platform;
import edu.ucla.cs.compilers.avrora.avrora.util.TestUtil;

/**
 * Mock for the {@link Microcontroller} interface.
 * 
 * @author Matthias Linder
 */
public class MockMCU implements Microcontroller
{

    public Simulator simulator;
    public Platform platform;
    private ClockDomain clockDomain;


    public MockMCU()
    {
        clockDomain = new ClockDomain(1000);
        this.simulator = TestUtil.createSimulator(this);
    }


    @Override
    public Simulator getSimulator()
    {
        return simulator;
    }


    @Override
    public Platform getPlatform()
    {
        return platform;
    }


    @Override
    public void setPlatform(Platform p)
    {
        platform = p;
    }


    @Override
    public Pin getPin(String name)
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public Pin getPin(int num)
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public void sleep()
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public int wakeup()
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public ClockDomain getClockDomain()
    {
        return clockDomain;
    }


    @Override
    public RegisterSet getRegisterSet()
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public MCUProperties getProperties()
    {
        return null;
    }
}
