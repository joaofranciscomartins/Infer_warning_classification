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

package edu.ucla.cs.compilers.avrora.jintgen.gen;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;

import edu.ucla.cs.compilers.avrora.cck.text.Printer;
import edu.ucla.cs.compilers.avrora.cck.text.SectionFile;
import edu.ucla.cs.compilers.avrora.cck.text.StringUtil;
import edu.ucla.cs.compilers.avrora.cck.util.Option;
import edu.ucla.cs.compilers.avrora.cck.util.Util;
import edu.ucla.cs.compilers.avrora.jintgen.isdl.AddrModeDecl;
import edu.ucla.cs.compilers.avrora.jintgen.isdl.InstrDecl;
import edu.ucla.cs.compilers.avrora.jintgen.isdl.parser.Token;
import edu.ucla.cs.compilers.avrora.jintgen.jigir.AssignStmt;
import edu.ucla.cs.compilers.avrora.jintgen.jigir.BinOpExpr;
import edu.ucla.cs.compilers.avrora.jintgen.jigir.CallExpr;
import edu.ucla.cs.compilers.avrora.jintgen.jigir.CallStmt;
import edu.ucla.cs.compilers.avrora.jintgen.jigir.CodeVisitor;
import edu.ucla.cs.compilers.avrora.jintgen.jigir.CommentStmt;
import edu.ucla.cs.compilers.avrora.jintgen.jigir.ConversionExpr;
import edu.ucla.cs.compilers.avrora.jintgen.jigir.DeclStmt;
import edu.ucla.cs.compilers.avrora.jintgen.jigir.DotExpr;
import edu.ucla.cs.compilers.avrora.jintgen.jigir.Expr;
import edu.ucla.cs.compilers.avrora.jintgen.jigir.FixedRangeExpr;
import edu.ucla.cs.compilers.avrora.jintgen.jigir.IfStmt;
import edu.ucla.cs.compilers.avrora.jintgen.jigir.IndexExpr;
import edu.ucla.cs.compilers.avrora.jintgen.jigir.Literal;
import edu.ucla.cs.compilers.avrora.jintgen.jigir.ReadExpr;
import edu.ucla.cs.compilers.avrora.jintgen.jigir.ReturnStmt;
import edu.ucla.cs.compilers.avrora.jintgen.jigir.Stmt;
import edu.ucla.cs.compilers.avrora.jintgen.jigir.StmtVisitor;
import edu.ucla.cs.compilers.avrora.jintgen.jigir.UnOpExpr;
import edu.ucla.cs.compilers.avrora.jintgen.jigir.VarExpr;
import edu.ucla.cs.compilers.avrora.jintgen.jigir.WriteStmt;

/**
 * @author Ben L. Titzer
 */
public class CodemapGenerator extends Generator
{

    private Printer printer;
    HashMap<String, Integer> registerMap = new HashMap<String, Integer>();

    protected final Option.Str CODEMAP_FILE = options.newOption(
            "codemap-template", "CodeMap.java",
            "This option specifies the name of the file that contains a template for generating the "
                    + "code map.");


    @Override
    public void generate() throws Exception
    {
        if (CODEMAP_FILE.isBlank())
            Util.userError("No template file specified");
        SectionFile sf = createSectionFile(CODEMAP_FILE.get(),
                "CODEMAP GENERATOR");
        printer = new Printer(new PrintStream(sf));

        initializeRegisterMap();
        printer.indent();
        for (InstrDecl d : arch.instructions)
            visit(d);
        generateHelperMethods();
        printer.unindent();
    }


    public void initializeRegisterMap()
    {
        // TODO: this is not portable
        for (int cntr = 0; cntr < 32; cntr++)
        {
            registerMap.put("R" + cntr, cntr);
        }
        throw Util.unimplemented();
    }

    static class Operand
    {
        boolean integer;
        String name;
    }


    public void visit(InstrDecl d)
    {
        printer.println("public void visit(" + d.getClassName() + " i) {");
        printer.indent();
        printer.println("Stmt stmt;");

        sgen.lastblock = -1;
        egen.operands = new HashMap<String, Operand>();

        for (AddrModeDecl.Operand o : d.getOperands())
        {
            Operand op = new Operand();
            op.name = o.name.image;
            egen.operands.put(o.name.toString(), op);
        }

        String bname = generateBlock(d.code.getStmts(),
                "\"===== \"+i.getName()+\" \"+i.getOperands()+"
                        + "\" ==========================================\"");

        printer.print(
                "result = new CodeRegion(new LinkedList(), " + bname + ");");
        printer.unindent();
        printer.nextln();
        printer.println("}");
    }

    public final ExprGenerator egen = new ExprGenerator();
    public final StmtGenerator sgen = new StmtGenerator();

    protected int biggestList;


    protected String generateBlock(List<Stmt> stmts, String comment)
    {
        String lname = "list" + (++sgen.lastblock);

        printer.println("LinkedList " + lname + " = new LinkedList();");

        if (comment != null)
        {
            printer.println("stmt = new CommentStmt(" + comment + ");");
            printer.println(lname + ".addLast(stmt);");
        }

        for (Stmt s : stmts)
        {
            s.accept(sgen);
            printer.println(lname + ".addLast(stmt);");
        }

        return lname;
    }


    protected void generateHelperMethods()
    {
        for (int cntr = 1; cntr <= biggestList; cntr++)
        {
            printer.print("protected LinkedList tolist" + cntr + '(');
            for (int var = 1; var <= cntr; var++)
            {
                printer.print("Object o" + var);
                if (var < cntr)
                    printer.print(", ");
            }
            printer.println(") {");
            printer.indent();
            printer.println("LinkedList retlist = new LinkedList();");
            for (int var = 1; var <= cntr; var++)
            {
                printer.println("retlist.addLast(o" + var + ");");
            }
            printer.println("return retlist;");
            printer.unindent();
            printer.nextln();
            printer.println("}");
        }
    }


    protected void generateExprList(List<Expr> exprs)
    {
        int len = exprs.size();
        String hname = "tolist" + len;
        if (len > biggestList)
            biggestList = len;
        if (len == 0)
        {
            printer.print("new LinkedList()");
        } else
        {
            printer.print(hname + '(');
            int cntr = 0;
            for (Expr e : exprs)
            {
                if (cntr++ != 0)
                    printer.print(", ");
                e.accept(egen);
            }
            printer.print(")");
        }
    }

    protected class StmtGenerator implements StmtVisitor
    {
        int lastblock;


        @Override
        public void visit(CallStmt s)
        {
            printer.print("stmt = new CallStmt(");
            generate(s.method);
            printer.print(", ");
            generateExprList(s.args);
            printer.println(");");
        }


        @Override
        public void visit(WriteStmt s)
        {
            throw Util.unimplemented();
        }


        @Override
        public void visit(CommentStmt s)
        {
            printer.println(s.toString());
        }


        @Override
        public void visit(DeclStmt s)
        {
            generate("DeclStmt", s.name, s.typeRef, s.init);
        }


        @Override
        public void visit(IfStmt s)
        {
            String ltrue = generateBlock(s.trueBranch, null);
            String lfalse = generateBlock(s.falseBranch, null);
            generate("IfStmt", s.cond, ltrue, lfalse);
        }


        @Override
        public void visit(ReturnStmt s)
        {
            printer.print("stmt = new ReturnStmt(");
            generate(s.expr);
            printer.println(");");
        }


        @Override
        public void visit(AssignStmt s)
        {
            throw Util.unimplemented();
        }


        @Override
        public void visit(AssignStmt.Var s)
        {
            throw Util.unimplemented();
        }


        @Override
        public void visit(AssignStmt.Map s)
        {
            throw Util.unimplemented();
        }


        @Override
        public void visit(AssignStmt.Bit s)
        {
            throw Util.unimplemented();
        }


        @Override
        public void visit(AssignStmt.FixedRange s)
        {
            throw Util.unimplemented();
        }


        // unused
        /*
         * private void generate(String clname, Object o1, Object o2, Object o3,
         * Object o4) { printer.print("stmt = new " + clname + '(');
         * generate(o1); printer.print(", "); generate(o2); printer.print(", ");
         * generate(o3); printer.print(", "); generate(o4);
         * printer.println(");"); }
         */
        private void generate(String clname, Object o1, Object o2, Object o3)
        {
            printer.print("stmt = new " + clname + '(');
            generate(o1);
            printer.print(", ");
            generate(o2);
            printer.print(", ");
            generate(o3);
            printer.println(");");
        }


        // unused
        /*
         * private void generate(String clname, Object o1, Object o2) {
         * printer.print("stmt = new " + clname + '('); generate(o1);
         * printer.print(", "); generate(o2); printer.println(");"); }
         */
        private void generate(Object o)
        {
            if (o instanceof Expr)
                ((Expr) o).accept(egen);
            else if (o instanceof Token)
                printer.print(StringUtil.quote(o));
            else
                printer.print(o.toString());
        }

        // unused
        /*
         * private void generate(int i) { printer.print("" + i); }
         */
    }

    protected class ExprGenerator implements CodeVisitor
    {
        HashMap<String, Operand> operands;


        public void generate(BinOpExpr e, String clname)
        {
            printer.print("new BinOpExpr(");
            e.left.accept(this);
            printer.print(", ");
            e.right.accept(this);
            printer.print(")");
        }


        private void generate(UnOpExpr e, String clname)
        {
            printer.print("new UnOpExpr(");
            e.expr.accept(this);
            printer.print(")");
        }


        // - Begin real visitor code

        @Override
        public void visit(BinOpExpr e)
        {
            generate(e, "AddExpr");
        }


        @Override
        public void visit(IndexExpr e)
        {
            printer.print("new IndexExpr(");
            e.expr.accept(this);
            printer.print(", ");
            e.index.accept(this);
            printer.print(")");
        }


        @Override
        public void visit(FixedRangeExpr e)
        {
            printer.print("new FixedRangeExpr(");
            e.expr.accept(this);
            printer.print(", " + e.low_bit + ", " + e.high_bit + ')');
        }


        @Override
        public void visit(CallExpr e)
        {
            printer.print("new CallExpr(" + StringUtil.quote(e.method) + ", ");
            generateExprList(e.args);
            printer.print(")");
        }


        @Override
        public void visit(ReadExpr e)
        {
            throw Util.unimplemented();
        }


        @Override
        public void visit(ConversionExpr e)
        {
            printer.print("new ConversionExpr(");
            e.expr.accept(this);
            printer.print(", " + StringUtil.quote(e.typeRef) + ", ");
            printer.print(")");
        }


        @Override
        public void visit(Literal.BoolExpr e)
        {
            printer.print("new Literal.BoolExpr(" + e.value + ')');
        }


        @Override
        public void visit(Literal.IntExpr e)
        {
            printer.print("new Literal.IntExpr(" + e.value + ')');
        }


        @Override
        public void visit(Literal.EnumVal e)
        {
            throw Util.unimplemented();
        }


        @Override
        public void visit(UnOpExpr e)
        {
            generate(e, "UnOpExpr");
        }


        @Override
        public void visit(VarExpr e)
        {
            String name = e.variable.toString();
            Operand op = operands.get(name);
            if (op != null)
            {
                generateOperandUse(op);
            } else
            {
                Integer i = getRegister(name);
                if (i != null)
                {
                    printer.print("new Literal.IntExpr(" + i.intValue() + ')');
                } else
                    generateVarUse(e);
            }
        }


        @Override
        public void visit(DotExpr e)
        {
            throw Util.unimplemented();
        }


        private void generateVarUse(VarExpr e)
        {
            if ("nextPC".equals(e.variable.toString()))
                printer.print("new Literal.IntExpr(nextPC)");
            else
                printer.print(
                        "new VarExpr(" + StringUtil.quote(e.variable) + ')');
        }


        private void generateOperandUse(Operand op)
        {
            if (op.integer)
            {
                printer.print("new Literal.IntExpr(i." + op.name + ')');
            } else
            {
                printer.print(
                        "new Literal.IntExpr(i." + op.name + ".getNumber())");
            }
        }


        protected Integer getRegister(String name)
        {
            return registerMap.get(name);
        }

    }
}
