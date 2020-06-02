/**
 * Copyright (c) 2004-2005, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * Neither the name of the University of California, Los Angeles nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.ucla.cs.compilers.avrora.avrora.test.sim;

import java.util.List;
import java.util.Properties;

import edu.ucla.cs.compilers.avrora.avrora.Defaults;
import edu.ucla.cs.compilers.avrora.avrora.core.Program;
import edu.ucla.cs.compilers.avrora.avrora.core.ProgramReader;
import edu.ucla.cs.compilers.avrora.avrora.sim.Simulator;
import edu.ucla.cs.compilers.avrora.avrora.sim.State;
import edu.ucla.cs.compilers.avrora.avrora.syntax.Module;
import edu.ucla.cs.compilers.avrora.avrora.test.sim.legacy.LegacyTester;
import edu.ucla.cs.compilers.avrora.avrora.test.sim.msp430.MSP430Tester;
import edu.ucla.cs.compilers.avrora.cck.test.TestCase;
import edu.ucla.cs.compilers.avrora.cck.test.TestEngine;
import edu.ucla.cs.compilers.avrora.cck.test.TestResult;
import edu.ucla.cs.compilers.avrora.cck.text.Terminal;
import edu.ucla.cs.compilers.avrora.cck.util.ClassMap;

/**
 * The <code>SimulatorTestHarness</code> implements a test harness that
 * interfaces the <code>avrora.test.AutomatedTester</code> in order to automate
 * testing of the AVR parser and simulator.
 *
 * @author Ben L. Titzer
 */
public class SimTestHarness implements TestEngine.Harness
{

    protected static final ClassMap testerMap;


    static
    {
        testerMap = new ClassMap("Simulation Tester", Tester.class);
        testerMap.addClass("avr", LegacyTester.class);
        testerMap.addClass("msp430", MSP430Tester.class);
    }

    static class SimulatorTest extends TestCase
    {

        Module module;
        Simulator simulator;
        List<Predicate> predicates;
        List<Predicate> inits;
        Tester tester;
        StateAccessor access;


        SimulatorTest(String fname, Properties props) throws Exception
        {
            super(fname, props);
            String result = trimString(props.getProperty("Result"));
            PredicateParser pp = new PredicateParser();
            predicates = pp.parseResult(result);
            String init = props.getProperty("Init");
            if (init != null)
                inits = pp.parseInitializers(trimString(init));
            String arch = expectProperty("Arch");
            tester = (Tester) testerMap.getObjectOfClass(arch);
        }


        @Override
        public void run() throws Exception
        {
            Program p = readProgram();
            simulator = initSimulator(p);
            simulator.start();
        }


        private Program readProgram() throws Exception
        {
            String format = expectProperty("Format");
            ProgramReader r = Defaults.getProgramReader(format);
            String arch = expectProperty("Arch");
            r.ARCH.set(arch);
            String[] args = { filename };
            return r.read(args);
        }


        private Simulator initSimulator(Program program)
        {
            Simulator sim = tester.newSimulator(program);
            access = tester.getAccessor(sim);
            if (inits != null)
                access.init(inits);
            return sim;
        }


        @Override
        public TestResult match(Throwable t)
        {
            if (t != null)
                return super.match(t);

            State state = simulator.getState();

            try
            {
                for (Predicate p : predicates)
                {
                    if (!p.check(access))
                        return new StateMismatch(p, state);
                }
            }
            catch (TestExpr.UnknownLabel l)
            {
                return new TestResult.TestFailure(
                        "unknown label specified in predicate: " + l.name);
            }
            return new TestResult.TestSuccess();
        }
    }

    static class StateMismatch extends TestResult.TestFailure
    {
        Predicate predicate;
        State state;


        StateMismatch(Predicate pred, State st)
        {
            super("incorrect result: (" + pred.left + " -> " + pred.leftvalue
                    + ") != (" + pred.right + " -> " + pred.rightvalue + ')');
            state = st;
            predicate = pred;
        }


        @Override
        public void longReport()
        {
            Terminal.println(message);
            // TODO: print out complete state
        }
    }


    @Override
    public TestCase newTestCase(String fname, Properties props) throws Exception
    {
        return new SimulatorTest(fname, props);
    }

}
