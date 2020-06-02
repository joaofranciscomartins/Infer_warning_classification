/**
 * Copyright (c) 2004-2005, Regents of the University of California All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 * <p>
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the
 * following disclaimer.
 * <p>
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 * <p>
 * Neither the name of the University of California, Los Angeles nor the names of its contributors may be used
 * to endorse or promote products derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package edu.ucla.cs.compilers.avrora.avrora;

import edu.ucla.cs.compilers.avrora.avrora.actions.*;
import edu.ucla.cs.compilers.avrora.avrora.arch.ArchitectureRegistry;
import edu.ucla.cs.compilers.avrora.avrora.core.Program;
import edu.ucla.cs.compilers.avrora.avrora.core.ProgramReader;
import edu.ucla.cs.compilers.avrora.avrora.monitors.*;
import edu.ucla.cs.compilers.avrora.avrora.sim.Simulation;
import edu.ucla.cs.compilers.avrora.avrora.sim.Simulator;
import edu.ucla.cs.compilers.avrora.avrora.sim.clock.ClockDomain;
import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.ATMega128;
import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.ATMega16;
import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.ATMega32;
import edu.ucla.cs.compilers.avrora.avrora.sim.mcu.MicrocontrollerFactory;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.*;
import edu.ucla.cs.compilers.avrora.avrora.sim.radio.Topology;
import edu.ucla.cs.compilers.avrora.avrora.sim.radio.TopologyRWP;
import edu.ucla.cs.compilers.avrora.avrora.sim.radio.TopologyStatic;
import edu.ucla.cs.compilers.avrora.avrora.sim.types.SensorSimulation;
import edu.ucla.cs.compilers.avrora.avrora.sim.types.SingleSimulation;
import edu.ucla.cs.compilers.avrora.avrora.sim.types.WiredSimulation;
import edu.ucla.cs.compilers.avrora.avrora.syntax.atmel.AtmelProgramReader;
import edu.ucla.cs.compilers.avrora.avrora.syntax.elf.ELFParser;
import edu.ucla.cs.compilers.avrora.avrora.syntax.objdump.ODPPAction;
import edu.ucla.cs.compilers.avrora.avrora.syntax.objdump.ObjDump2ProgramReader;
import edu.ucla.cs.compilers.avrora.avrora.syntax.objdump.ObjDumpProgramReader;
import edu.ucla.cs.compilers.avrora.avrora.syntax.raw.RAWReader;
import edu.ucla.cs.compilers.avrora.avrora.test.DisassemblerTestHarness;
import edu.ucla.cs.compilers.avrora.avrora.test.InterruptTestHarness;
import edu.ucla.cs.compilers.avrora.avrora.test.ProbeTestHarness;
import edu.ucla.cs.compilers.avrora.avrora.test.SimplifierTestHarness;
import edu.ucla.cs.compilers.avrora.avrora.test.sim.SimTestHarness;
import edu.ucla.cs.compilers.avrora.cck.help.HelpCategory;
import edu.ucla.cs.compilers.avrora.cck.help.HelpSystem;
import edu.ucla.cs.compilers.avrora.cck.test.TestEngine;
import edu.ucla.cs.compilers.avrora.cck.text.StringUtil;
import edu.ucla.cs.compilers.avrora.cck.util.ClassMap;
import edu.ucla.cs.compilers.avrora.cck.util.Util;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * The <code>Defaults</code> class contains the default mappings for
 * microcontrollers, actions, input formats, constants, etc.
 *
 * @author Ben L. Titzer
 */
public class Defaults {
    private static final HashMap<String, HelpCategory> mainCategories = new HashMap<String, HelpCategory>();
    private static ClassMap microcontrollers = new ClassMap("Microcontroller", MicrocontrollerFactory.class);
    private static ClassMap platforms = new ClassMap("Platform", PlatformFactory.class);
    private static ClassMap actions = new ClassMap("Action", Action.class);
    private static ClassMap inputs = new ClassMap("Input Format", ProgramReader.class);
    private static ClassMap harnessMap = new ClassMap("Test Harness", TestEngine.Harness.class);
    private static ClassMap monitorMap = new ClassMap("Monitor", MonitorFactory.class);
    private static ClassMap simMap = new ClassMap("Simulation", Simulation.class);
    private static ClassMap topologies = new ClassMap("Topology", Topology.class);

    /**
     * Adds a new alias to {@link MonitorFactory} mapping.
     *
     * @param alias class alias
     * @param monitorFactory a monitor factory class
     */
    public static synchronized void addMonitor(String alias, Class<? extends MonitorFactory> monitorFactory) {
        monitorMap.addClass(alias, monitorFactory);
    }

    /**
     * Adds a new alias to {@link Action} mapping.
     *
     * @param alias class alias
     * @param action an action class
     */
    public static synchronized void addAction(String alias, Class<? extends Action> action) {
        actions.addClass(alias, action);
    }

    /**
     * Adds a new alias to {@link Simulation} mapping.
     *
     * @param alias class alias
     * @param simulation a simulation class
     */
    public static synchronized void addSimulation(String alias, Class<? extends Simulation> simulation) {
        simMap.addClass(alias, simulation);
    }

    /**
     * Adds a new alias to {@link PlatformFactory} mapping.
     *
     * @param alias class alias
     * @param platform a platform factory class
     */
    public static synchronized void addPlatform(String alias, Class<? extends PlatformFactory> platform) {
        platforms.addClass(alias, platform);
    }

    /**
     * Adds a new alias to {@link MicrocontrollerFactory} mapping.
     *
     * @param alias class alias
     * @param uCFActory a microcontroller factory
     * @throws IllegalArgumentException
     *             if factory's default constructor cannot be invoked
     */
    public static synchronized void addMicrocontroller(String alias, Class<? extends
            MicrocontrollerFactory> uCFActory) throws IllegalArgumentException {
        try {
            microcontrollers.addInstance(alias, uCFActory.getConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new IllegalArgumentException("failed to invoke default constructor of class [" +
                    uCFActory.getName() + "]");
        }
    }

    /**
     * Adds a new alias to {@link Topology} mapping.
     *
     * @param alias class alias
     * @param topology a topology class
     */
    public static synchronized void addTopology(String alias, Class<? extends Topology> topology) {
        topologies.addClass(alias, topology);
    }

    /**
     * The <code>getMicrocontroller()</code> method gets the microcontroller
     * factory corresponding to the given name represented as a string. This
     * string can represent a short name for the class (an alias), or a fully
     * qualified Java class name.
     *
     * @param s
     *            the name of the microcontroller as string; a class name or an
     *            alias such as "atmega128"
     * @return an instance of the <code>MicrocontrollerFactory</code> interface
     *         that is capable of creating repeated instances of the
     *         microcontroller.
     */
    public static MicrocontrollerFactory getMicrocontroller(String s) {
        addMicrocontrollers();
        return (MicrocontrollerFactory) microcontrollers.getObjectOfClass(s);
    }

    /**
     * The <code>getPlatform()</code> method gets the platform factory
     * corresponding to the given name represented as a string. This string can
     * represent a short name for the class (an alias), or a fully qualified
     * Java class name.
     *
     * @param s
     *            the name of the platform as string; a class name or an alias
     *            such as "mica2"
     * @return an instance of the <code>PlatformFactory</code> interface that is
     *         capable of creating repeated instances of the microcontroller.
     */
    public static PlatformFactory getPlatform(String s) {
        addPlatforms();
        return (PlatformFactory) platforms.getObjectOfClass(s);
    }

    /**
     * The <code>getProgramReader()</code> method gets the program reader
     * corresponding to the given name represented as a string. This string can
     * represent a short name for the class (an alias), or a fully qualified
     * Java class name.
     *
     * @param s
     *            the name of the program reader format as a string
     * @return an instance of the <code>ProgramReader</code> class that is
     *         capable of reading a program into the internal program
     *         representation format.
     */
    public static ProgramReader getProgramReader(String s) {
        addInputFormats();
        return (ProgramReader) inputs.getObjectOfClass(s);
    }

    /**
     * The <code>getAction()</code> method gets the action corresponding to the
     * given name represented as a string. This string can represent a short
     * name for the class (an alias), or a fully qualified Java class name.
     *
     * @param s
     *            the name of the action as a string
     * @return an instance of the <code>Action</code> class which can run given
     *         the command line arguments and options provided.
     */
    public static Action getAction(String s) {
        addActions();
        return (Action) actions.getObjectOfClass(s);
    }

    /**
     * The <code>getMonitor()</code> method gets the monitor corresponding to
     * the given name represented as a string. This string can represent a short
     * name for the class (an alias), or a fully qualified Java class name.
     *
     * @param s
     *            the name of the monitor as a string
     * @return an instance of the <code>MonitorFactory</code> class that is
     *         capable of attaching monitors to nodes as they are created
     */
    public static MonitorFactory getMonitor(String s) {
        addMonitors();
        return (MonitorFactory) monitorMap.getObjectOfClass(s);
    }

    public static Simulation getSimulation(String s) {
        addSimulations();
        // TODO: add a simulation factory
        return (Simulation) simMap.getObjectOfClass(s);
    }

    public static Topology getTopology(String s) {
        addTopologies();
        return (Topology) topologies.getObjectOfClass(s);
    }

    /**
     * The <code>getTestHarnessMap()</code> method gets the test harness class
     * map.
     *
     * @return an instance of the <code>ClassMap</code> class that is capable of
     *         creating a test case for a file and running it
     */
    public static ClassMap getTestHarnessMap() {
        addTestHarnesses();
        return harnessMap;
    }

    /**
     * The <code>getActionList()</code> method returns a list of aliases for
     * actions sorted alphabetically.
     *
     * @return a sorted list of known actions
     */
    public static List<String> getActionList() {
        addActions();
        return actions.getSortedList();
    }

    /**
     * The <code>getProgramReaderList()</code> method returns a list of aliases
     * for program readers sorted alphabetically.
     *
     * @return a sorted list of known program readers
     */
    public static List<String> getProgramReaderList() {
        addInputFormats();
        return inputs.getSortedList();
    }

    public static void addSubCategories(ClassMap vals) {
        List<String> l = vals.getSortedList();
        for (String val : l) {
            Class<?> cz = vals.getClass(val);
            if (HelpCategory.class.isAssignableFrom(cz)) HelpSystem.addCategory(val, cz);
        }
    }

    public static void addMainCategory(HelpCategory cat) {
        HelpSystem.addCategory(cat.name, cat);
        mainCategories.put(cat.name, cat);
    }

    public static HelpCategory getHelpCategory(String name) {
        addAll();
        return HelpSystem.getCategory(name);
    }

    public static List<HelpCategory> getMainCategories() {
        addAll();
        List<HelpCategory> list = Collections.list(Collections.enumeration(mainCategories.values()));
        Collections.sort(list, HelpCategory.COMPARATOR);
        return list;
    }

    public static List<HelpCategory> getAllCategories() {
        addAll();
        List<String> l = HelpSystem.getSortedList();
        LinkedList<HelpCategory> nl = new LinkedList<HelpCategory>();
        for (String s : l) {
            nl.addLast(HelpSystem.getCategory(s));
        }
        return nl;
    }

    public static Simulator newSimulator(int id, Program p) {
        return newSimulator(id, "atmega128", 8000000, 8000000, p);
    }

    public static Simulator newSimulator(int id, String mcu, long hz, long exthz, Program p) {
        MicrocontrollerFactory f = getMicrocontroller(mcu);
        ClockDomain cd = new ClockDomain(hz);
        cd.newClock("external", exthz);

        return f.newMicrocontroller(id, new SingleSimulation(), cd, p).getSimulator();
    }

    private static synchronized void addAll() {
        addMicrocontrollers();
        addPlatforms();
        addActions();
        addInputFormats();
        addTestHarnesses();
        addMonitors();
        addSimulations();
        addTopologies();
        ArchitectureRegistry.addArchitectures();
    }

    private static synchronized void addMonitors() {
        // -- DEFAULT MONITORS AVAILABLE
        monitorMap.addClass("calls", CallMonitor.class);
        monitorMap.addClass("break", BreakMonitor.class);
        monitorMap.addClass("c-print", PrintMonitor.class);
        monitorMap.addClass("c-timer", TimerMonitor.class);
        monitorMap.addClass("profile", ProfileMonitor.class);
        monitorMap.addClass("memory", MemoryMonitor.class);
        monitorMap.addClass("sleep", SleepMonitor.class);
        monitorMap.addClass("leds", LEDMonitor.class);
        monitorMap.addClass("stack", StackMonitor.class);
        monitorMap.addClass("energy", EnergyMonitor.class);
        monitorMap.addClass("interrupts", InterruptMonitor.class);
        monitorMap.addClass("interactive", InteractiveMonitor.class);
        monitorMap.addClass("trace", TraceMonitor.class);
        monitorMap.addClass("energy-profile", EnergyProfiler.class);
        monitorMap.addClass("packet", PacketMonitor.class);
        monitorMap.addClass("gdb", GDBServer.class);
        monitorMap.addClass("simperf", SimPerfMonitor.class);
        monitorMap.addClass("serial", SerialMonitor.class);
        monitorMap.addClass("spi", SPIMonitor.class);
        monitorMap.addClass("call-time", CallTimeMonitor.class);
        monitorMap.addClass("call-profile", CallTreeProfiler.class);
        monitorMap.addClass("trip-time", TripTimeMonitor.class);
        monitorMap.addClass("ioregs", IORegMonitor.class);
        monitorMap.addClass("virgil", VirgilMonitor.class);
        monitorMap.addClass("real-time", RealTimeMonitor.class);
        monitorMap.addClass("sniffer", SnifferMonitor.class);
        monitorMap.addClass("retaddr", RetAddrWatch.class);

        HelpCategory hc = new HelpCategory("monitors", "Help for the supported simulation monitors.");
        hc.addOptionValueSection("SIMULATION MONITORS", "Avrora's simulator offers the ability to install " +
                "execution " + "monitors that instrument the program in order to study and analyze its " +
                "behavior. The " + "\"simulate\" action supports this option that allows a monitor class "
                + "to be loaded which will instrument the program before it is run and then generate a " +
                "report " + "after the program has completed execution.", "-monitors", monitorMap);
        addMainCategory(hc);
        addSubCategories(monitorMap);
    }

    private static synchronized void addTestHarnesses() {
        // -- DEFAULT TEST HARNESSES
        harnessMap.addClass("simulator", SimTestHarness.class);
        harnessMap.addClass("simplifier", SimplifierTestHarness.class);
        harnessMap.addClass("probes", ProbeTestHarness.class);
        harnessMap.addClass("disassembler", DisassemblerTestHarness.class);
        harnessMap.addClass("interrupt", InterruptTestHarness.class);
    }

    private static synchronized void addInputFormats() {
        // -- DEFAULT INPUT FORMATS
        inputs.addClass("auto", AutoProgramReader.class);
        inputs.addClass("raw", RAWReader.class);
        inputs.addClass("atmel", AtmelProgramReader.class);
        inputs.addClass("objdump", ObjDumpProgramReader.class);
        inputs.addClass("odpp", ObjDump2ProgramReader.class);
        inputs.addClass("elf", ELFParser.class);

        HelpCategory hc = new HelpCategory("inputs", "Help for the supported program input formats.");
        hc.addOptionValueSection("INPUT FORMATS", "The input format of the program is specified with the " +
                "\"-input\" " + "option supplied at the command line. This input format is used by " +
                "actions that operate on programs to determine how to interpret the " + "input and build a " +
                "program from the files specified. For example, the input format might " + "be Atmel " +
                "syntax, GAS syntax, or the output of a disassembler such as avr-objdump. Currently " + "no" +
                " binary formats are supported.", "-input", inputs);
        addMainCategory(hc);
        addSubCategories(inputs);
    }

    private static synchronized void addActions() {
        // -- DEFAULT ACTIONS
        actions.addClass("disassemble", DisassembleAction.class);
        actions.addClass("simulate", SimAction.class);
        actions.addClass("analyze-stack", AnalyzeStackAction.class);
        actions.addClass("test", TestAction.class);
        actions.addClass("cfg", CFGAction.class);
        actions.addClass("isea", ISEAAction.class);
        actions.addClass("odpp", ODPPAction.class);
        actions.addClass("elf-dump", ELFDumpAction.class);

        // plug in a new help category for actions accesible with "-help
        // actions"
        HelpCategory hc = new HelpCategory("actions", "Help for Avrora actions.");
        hc.addOptionValueSection("ACTIONS", "Avrora accepts the \"-action\" command line option " + "that " +
                        "you can use to select from the available functionality that Avrora " + "provides. " +
                "This " +
                        "action might be to assemble the file, " + "print a listing, perform a simulation, " +
                "or run " +
                        "an analysis tool. This " + "flexibility allows this single frontend to select from" +
                " " +
                        "multiple useful " + "tools. The currently supported actions are given below.", "-action", actions);
        addMainCategory(hc);
        addSubCategories(actions);
    }

    private static synchronized void addSimulations() {
        // -- DEFAULT ACTIONS
        simMap.addClass("single", SingleSimulation.class);
        simMap.addClass("sensor-network", SensorSimulation.class);
        simMap.addClass("wired", WiredSimulation.class);

        // plug in a new help category for simulations accesible with "-help
        // simulations"
        HelpCategory hc = new HelpCategory("simulations", "Help for supported simulation types.");
        hc.addOptionValueSection("SIMULATION TYPES", "When running a simulation, Avrora accepts the " +
                "\"-simulation\" command line option " + "that selects the simulation type from multiple " +
                "different types provided, or a " + "user-supplied Java class of your own. For example, a " +
                "simulation might be for a " + "sensor network application, a single node simulation, or a " +
                "robotics simulation. ", "-simulation", simMap);
        addMainCategory(hc);
        addSubCategories(simMap);
    }

    private static synchronized void addPlatforms() {
        // -- DEFAULT PLATFORMS
        platforms.addClass("mica2", Mica2.Factory.class);
        platforms.addClass("micaz", MicaZ.Factory.class);
        platforms.addClass("seres", Seres.Factory.class);
        platforms.addClass("superbot", Superbot.Factory.class);
        platforms.addClass("telos", Telos.Factory.class);
    }

    private static synchronized void addMicrocontrollers() {
        // -- DEFAULT MICROCONTROLLERS
        microcontrollers.addInstance("atmega128", new ATMega128.Factory());
        microcontrollers.addInstance("atmega32", new ATMega32.Factory());
        microcontrollers.addInstance("atmega16", new ATMega16.Factory());
    }

    private static synchronized void addTopologies() {
        // -- DEFAULT TOPOLOGIES
        topologies.addClass("static", TopologyStatic.class);
        topologies.addClass("rwp", TopologyRWP.class);
        // plug in a new help category for simulations accesible with "-help
        // topologies"
        HelpCategory hc = new HelpCategory("topologies", "Help for supported topology types.");
        hc.addOptionValueSection("TOPOLOGY TYPES", "Avrora supports different types of topolgies using the " +
                        "\"-topology\" command " + "line option. This option works only for sensor-network " +
                        "simulation. Note that a " + "topology must be set to use a specific radio model.",
                "-topology", topologies);
        addMainCategory(hc);
        addSubCategories(topologies);
    }

    public static class AutoProgramReader extends ProgramReader {
        public AutoProgramReader() {
            super("The \"auto\" input format reads a program from a single file at a time. " + "It uses the" +
                    " extension of the filename as a clue to decide what input " + "reader to use for that " +
                    "file. For example, an extension of \".asm\" is " + "considered to be a program in " +
                    "Atmel assembly syntax.");
        }

        @Override
        public Program read(String[] args) throws Exception {
            if (args.length == 0) Util.userError("no input files");
            if (args.length != 1) Util.userError("input type \"auto\" accepts only one file at a time.");

            String n = args[0];
            int offset = n.lastIndexOf('.');
            if (offset < 0) Util.userError("file " + StringUtil.quote(n) + " does not have an extension");

            String extension = n.substring(offset).toLowerCase();

            ProgramReader reader = null;
            if (".asm".equals(extension)) reader = new AtmelProgramReader();
            else if (".od".equals(extension)) reader = new ObjDumpProgramReader();
            else if (".odpp".equals(extension)) reader = new ObjDump2ProgramReader();
            else if (".elf".equals(extension)) reader = new ELFParser();

            if (reader == null) {
                Util.userError("file extension " + StringUtil.quote(extension) + " unknown");
                return null;
            }

            // TODO: this is a hack; all inherited options should be available
            reader.INDIRECT_EDGES.set(INDIRECT_EDGES.stringValue());
            reader.ARCH.set(ARCH.stringValue());
            reader.options.process(options);
            return reader.read(args);
        }
    }
}
