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

package edu.ucla.cs.compilers.avrora.cck.stat;

import edu.ucla.cs.compilers.avrora.cck.text.Printer;

/**
 * This models the min, max, mean, accumulation, total, and number of
 * occurrences of min and max in a stream of integers.
 *
 * @author Ben L. Titzer
 */
public class MinMaxMean implements DataItem {
    protected final String name;

    public float mean;
    public int observedMaximum;
    public int observedMinimum;
    public int countMinimum;
    public int countMaximum;
    public int total;
    public int accumulation;
    protected boolean someData;
    protected String totalname; // name to report for total field
    protected String cumulname; // name to report for cumul field

    /**
     * Public constructor initializes the statistics for a sequence of integers.
     *
     * @param newname the name
     */
    public MinMaxMean(String newname) {
        name = newname;
        totalname = "Total";
        cumulname = "Accumulation";
        someData = false;
    }

    /**
     * Public constructor initializes the statistics for a sequence of integers.
     *
     * @param newname see {@link #MinMaxMean(String)}
     * @param tn totan field name
     * @param cn cummulative field name
     */
    public MinMaxMean(String newname, String tn, String cn) {
        name = newname;
        totalname = tn;
        cumulname = cn;
        someData = false;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Update the statistical data for the next input value.
     *
     * @param value the value to be recorded
     */
    public void record(int value) {

        if (!someData) {
            observedMinimum = observedMaximum = value;
            countMinimum = countMaximum = 1;
            mean = value;
            accumulation = value;
            someData = true;
            total = 1;
            return;
        }

        if (value > observedMaximum) {
            observedMaximum = value;
            countMaximum = 1;
        } else if (value == observedMaximum) countMaximum++;

        if (value < observedMinimum) {
            observedMinimum = value;
            countMinimum = 1;
        } else if (value == observedMinimum) countMinimum++;

        accumulation += value;
        total++;
    }

    /**
     * process the data so far and update internal statistics.
     */
    @Override
    public void process() {
        mean = ((float) accumulation) / ((float) total);
    }

    /**
     * Generate a textual report of the data gathered.
     *
     * @param printer the printer to print with
     */
    @Override
    public void print(Printer printer) {
        printer.print("\n " + name);
        printer.print("\n---------------------------------------------------------------------\n");

        if (totalname != null) {
            printer.print("   " + totalname + ": " + total);
        }
        if (cumulname != null) {
            printer.print("   " + cumulname + ": " + accumulation);
        }

        printer.print("\n Statistics: ");
        printer.print("\n   Minimum: " + observedMinimum + ", " + countMinimum + " occurences of min.");
        printer.print("\n   Maximum: " + observedMaximum + ", " + countMaximum + " occurences of max.");
        printer.print("\n   Mean: " + mean + '\n');
    }

    /**
     * Merge the results of two MinMaxMean objects into one.
     *
     * @param m the new model to merge with
     * @return a new result
     */
    public MinMaxMean merge(MinMaxMean m) {
        MinMaxMean result = new MinMaxMean(name);

        if (m.observedMaximum > observedMaximum) {
            result.observedMaximum = m.observedMaximum;
            result.countMaximum = m.countMaximum;
        } else if (m.observedMaximum == observedMaximum) {
            result.observedMaximum = observedMaximum;
            result.countMaximum = countMaximum + m.countMaximum;
        } else {
            result.observedMaximum = observedMaximum;
            result.countMaximum = countMaximum;
        }

        if (m.observedMinimum < observedMinimum) {
            result.observedMinimum = m.observedMinimum;
            result.countMinimum = m.countMinimum;
        } else if (m.observedMinimum == observedMinimum) {
            result.observedMinimum = observedMinimum;
            result.countMinimum = countMinimum + m.countMinimum;
        } else {
            result.observedMinimum = observedMinimum;
            result.countMinimum = countMinimum;
        }

        result.total = total + m.total;
        result.accumulation = accumulation + m.accumulation;
        result.mean = ((m.mean * m.total) + (mean * total)) / result.total;
        return result;
    }

    @Override
    public boolean empty() {
        return !someData;
    }

    @Override
    public String toString() {
        return name;
    }
}
