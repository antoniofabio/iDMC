/*
 * iDMC the interactive Dynamical Model Calculator simulates and performs
 * graphical and numerical analysis of systems of differential and
 * difference equations.
 *
 * Copyright (C) 2004 Marji Lines and Alfredo Medio.
 *
 * Written by Daniele Pizzoni <auouo@tin.it>.
 * Extended by Alexei Grigoriev <alexei_grigoriev@libero.it>.
 *
 *
 *
 * The software program was developed within a research project financed
 * by the Italian Ministry of Universities, the Universities of Udine and
 * Ca'Foscari of Venice, the Friuli-Venezia Giulia Region.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 */
package org.tsho.dmc2.core.chart;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;
import org.tsho.dmc2.core.VariableDoubles;
import org.tsho.dmc2.core.dlua.Lua;
import org.tsho.dmc2.core.model.DifferentiableMap;
import org.tsho.dmc2.core.model.InvertibleMap;
import org.tsho.dmc2.core.model.ModelException;
import org.tsho.dmc2.core.model.SimpleMap;

public class ManifoldsRenderer implements DmcPlotRenderer {

    private DmcRenderablePlot plot;
    private DifferentiableMap model;

    // parameters

    private VariableDoubles nodeEsteem;
    private VariableDoubles parameters;
    private double epsilon;
    private Range lockoutHorRange;
    private Range lockoutVerRange;
    private int firstIteration;
    private int lastIteration;

    // options

    public static final byte TYPE_STABLE   = 0x01;
    public static final byte TYPE_UNSTABLE = 0x02;
    public static final byte TYPE_RIGHT    = 0x04;
    public static final byte TYPE_LEFT     = 0x08;
    private byte type;

    //flags

    private boolean stopped;

    // internal state

    private int state;
    private double[] node;
    private static final int MAX_NODE_CHECK_ITERATIONS = 50;


    public ManifoldsRenderer(
            final DifferentiableMap map,
            final DmcRenderablePlot plot) {

        this.model = map;
        this.plot = plot;
    }

    public void initialize() {
        //throw new ModelException();
    }

    public LegendItemCollection getLegendItems() {
        return null;
    }

    public void render(
            final Graphics2D g2, final Rectangle2D dataArea,
            final PlotRenderingInfo info) {

        byte myType = type;
        if ((myType & TYPE_UNSTABLE) != 0) {
            g2.setColor(Color.RED);
            if ((myType & TYPE_RIGHT) != 0) {
                renderManifold(false, true, g2, dataArea);
                if (state == STATE_STOPPED) {
                    myType = 0;
                }
            }
            if ((myType & TYPE_LEFT) != 0) {
                renderManifold(false, false, g2, dataArea);
                if (state == STATE_STOPPED) {
                    myType = 0;                }
            }
        }
        if ((myType & TYPE_STABLE) != 0) {
            g2.setColor(Color.BLUE);
            if ((myType & TYPE_RIGHT) != 0) {
                renderManifold(true, true, g2, dataArea);
                if (state == STATE_STOPPED) {
                    myType = 0;                }
            }
            if ((myType & TYPE_LEFT) != 0) {
                renderManifold(true, false, g2, dataArea);
                if (state == STATE_STOPPED) {
                    myType = 0;                }
            }
        }

        g2.setColor(Color.BLACK);
        plot(g2, dataArea, node, true);

        if (state != STATE_STOPPED) {
            state = STATE_FINISHED;
        }
        else {
            state = STATE_STOPPED;
        }
    }

    private void renderManifold(
            final boolean stable, final boolean right,
            final Graphics2D g2, final Rectangle2D dataArea) {

        stopped = false;
        state = STATE_RUNNING;

        /* the unstable eigenvector */
        //double[] eigenvector = new double[2];
        Eigen e = new Eigen();

        /* the segment to iterate */
        double[] gamma0 = new double[2];
        double[] gamma1 = new double[2];

        // refine fixed point
        node = Lua.findCycles(
                model, parameters, 1, nodeEsteem,
			epsilon, MAX_NODE_CHECK_ITERATIONS, new double[model.getNVar()]);

        DiffEvaluator normalMap = new DiffEvaluator(
                    model, VariableDoubles.toArray(parameters));

        if (!evaluateEigenvector(normalMap, node, e)) {
            throw new ModelException("Not a saddle point.");
        }

        Evaluator map;
        if (stable) {
            // adjust eigenvectors to get right or left manifolds
            if ((right && e.vectorS[0] < 1) || (!right && e.vectorS[0] > 1)) {
                e.vectorS[0] *= -1;
                e.vectorS[1] *= -1;
            }
            gamma0 = findGammaSegment(normalMap, node, e.vectorS, epsilon);

            map = new StraightEvaluator(((InvertibleMap) model).getInverse(),
                    VariableDoubles.toArray(parameters));
            if (e.valueS < -1) {
                map = new SquareEvaluator(model, VariableDoubles.toArray(parameters));
            }
        }
        else {
            if ((right && e.vectorU[0] < 1) || (!right && e.vectorU[0] > 1)) {
                e.vectorU[0] *= -1;
                e.vectorU[1] *= -1;
            }
            gamma0 = findGammaSegment(normalMap, node, e.vectorU, epsilon);

            if (e.valueU < -1) {
                map = new SquareEvaluator(model, VariableDoubles.toArray(parameters));
            }
            else {
                map = new StraightEvaluator(model, VariableDoubles.toArray(parameters));
            }
        }

        map.evaluate(gamma0, gamma1);

        double[] gammaD = subVect(gamma1, gamma0);

        PowerEvaluator pEvaluator = new PowerEvaluator(map);

        for (int i = firstIteration; i <= lastIteration; i++) {
            double[] x = new double[2];
            double[] sk = new double[2];

            pEvaluator.evaluate(i, gamma0, x);  // first point of iterated segment
            pEvaluator.evaluate(i, gamma1, sk); // last point of iterated segment

            plot(g2, dataArea, x, false);

            double sigma = epsilon;
            double delta = 1;
            double deltaStep = 0;
            double lastDelta = 0;
            do {
                if (stopped) {
                    state = STATE_STOPPED;
                    return;
                }

                double[] z = new double[2];
                double dist;

                pEvaluator.evaluate(
                        i, addVect(gamma0, mulVect(delta, gammaD)), z);
                dist = norm(subVect(z, x));

                if (dist < sigma) {
                    x = z;
                    plot(g2, dataArea, x, false);

                    deltaStep = delta - lastDelta;
                    lastDelta = delta;

                    dist = norm(subVect(sk, x));
                    if (dist <  sigma) {
                        if (delta < 1) {
                            plot(g2, dataArea, sk, false);
                        }
                        break;
                    }
                    else {
                        delta = Math.min(delta + 1.25 * deltaStep, 1);
                        dist = minDist(x);

                        if (dist > 0) {
                            sigma = Math.max(epsilon, 0.99 * dist);
                        }
                        else {
                            sigma = epsilon;
                        }
                    }
                }
                else { // do not accept the computed point
                    delta = 0.5 * (delta + lastDelta);
                }

            } while(true);
        }
    }

    private void plot(
            final Graphics2D g2, final Rectangle2D dataArea,
            final double[] v, final boolean big) {

        int x, y;
        x = (int) plot.getDomainAxis().valueToJava2D(
                        v[0], dataArea, RectangleEdge.BOTTOM);

        y = (int) plot.getRangeAxis().valueToJava2D(
                        v[1], dataArea, RectangleEdge.LEFT);

        if (big) {
            g2.fillRect(x - 1, y - 1, 3, 3);
        }
        else {
            g2.fillRect(x, y, 1, 1);
        }
    }

    private double minDist(final double[] v) {

        double lockoutXMin = lockoutHorRange.getLowerBound();
        double lockoutXMax = lockoutHorRange.getUpperBound();
        double lockoutYMin = lockoutVerRange.getLowerBound();
        double lockoutYMax = lockoutVerRange.getUpperBound();

        double dx, dy;

        //123
        //456
        //789
        if (v[0] <= lockoutXMin) {
            //147
            if (v[1] <= lockoutYMin) { //7
                dx = lockoutXMin - v[0];
                dy = lockoutYMin - v[1];
                return Math.sqrt(dx * dx + dy * dy);
            }
            else if (v[1] <= lockoutYMax) { //4
                return lockoutXMin - v[0];
            }
            else {
                dx = lockoutXMin - v[0];
                dy = v[1] - lockoutYMax;
                return Math.sqrt(dx * dx + dy * dy);
            }
        }
        else if (v[0] <= lockoutXMax) {
            //258
            if (v[1] <= lockoutYMin) { //8
                return lockoutYMin - v[1];
            }
            else if (v[1] <= lockoutYMax) { //5
                return 0;
            }
            else { //2
                return v[1] - lockoutYMax;
            }
        }
        else {
            //369
            if (v[1] <= lockoutYMin) { //9
                dx = v[0] - lockoutXMax;
                dy = lockoutYMin - v[1];
                return Math.sqrt(dx * dx + dy * dy);
            }
            else if (v[1] <= lockoutYMax) { //6
                return v[0] - lockoutXMax;
            }
            else { //3
                dx = v[0] - lockoutXMax;
                dy = v[1] - lockoutYMax;
                return Math.sqrt(dx * dx + dy * dy);
            }
        }
    }

    private static class Eigen {
        public double valueU;
        public double valueS;
        public double[] vectorU = new double[2];
        public double[] vectorS = new double[2];
    }

    private static boolean evaluateEigenvector(
            final DiffEvaluator evaluator, final double[] node, final Eigen e) {

        double[] result = new double[4];

        evaluator.evaluateJacobian(node, result);

//        System.out.println("a = " + result[0]);
//        System.out.println("b = " + result[1]);
//        System.out.println("c = " + result[2]);
//        System.out.println("d = " + result[3]);

        double trace = result[0] + result[3];
        double det = result[0] * result[3] - result[1] * result[2];
        double delta = trace * trace - 4 * det;

//        System.out.println("trace = " + trace);
//        System.out.println("determinant = " + det);
//        System.out.println("discriminant = " + delta);

        if (delta < 0) {
            return false;
        }

        double l1 = (trace + Math.sqrt(delta)) / 2;
        double l2 = (trace - Math.sqrt(delta)) / 2;

//        System.out.println("l1 = " + l1);
//        System.out.println("l2 = " + l2);

        if (Math.abs(l1) < 1 && Math.abs(l2) < 1
            || Math.abs(l1) > 1 && Math.abs(l2) > 1) {
            return false;
        }

        if (Math.abs(l1) < 1) {
            e.valueS = l1;
            e.valueU = l2;
        }
        else {
            e.valueS = l2;
            e.valueU = l1;
        }

        /* eigenvectors */
        double norm;

        e.vectorS[0] = 1;
        e.vectorS[1] = (e.valueS - result[0]) / result[1];
        norm = Math.sqrt(e.vectorS[0] * e.vectorS[0] + e.vectorS[1] * e.vectorS[1]);
        e.vectorS[0] /= norm;
        e.vectorS[1] /= norm;

        e.vectorU[0] = 1;
        e.vectorU[1] = (e.valueU - result[0]) / result[1];
        norm = Math.sqrt(e.vectorU[0] * e.vectorU[0] + e.vectorU[1] * e.vectorU[1]);
        e.vectorU[0] /= norm;
        e.vectorU[1] /= norm;

        return true;
    }

    private static double[] findGammaSegment(
            final DiffEvaluator evaluator,
            
            final double[] node, final double[] eigenvector,
            final double epsilon) {

        int counter = 0;

        double alpha = epsilon * 0.1;
        double distance = epsilon;

        //double[] n = VariableDoubles.toArray(nodeEsteem);
        double[] jn = new double[4];

        evaluator.evaluateJacobian(node, jn);

        double[] x = new double[2];
        double[] nl = new double[2];
        double[] l = new double[2];

        while (distance >= epsilon && counter++ < 50) {

            x[0] = node[0] + eigenvector[0] * alpha;
            x[1] = node[1] + eigenvector[1] * alpha;

            evaluator.evaluate(x, nl);

            // n + jn(y - n)
            l = addVect(node, applyMatrix(jn, subVect(x, node)));
            distance = norm(subVect(nl, l));

            alpha /= 2;
        }

        System.out.println("distance: " + distance);
        System.out.println("count: " + counter);
        System.out.println("Initial point: " + x[0] + ", " + x[1]);
        System.out.println("Initial point image: " + nl[0] + ", " + nl[1]);

        return x;
    }

    private static double norm(final double[] v) {
        return Math.sqrt(v[0] * v[0] + v[1] * v[1]);
    }

    private static double[] mulVect(final double a, final double[] v) {
        double[] result = new double[2];
        result[0] = a * v[0];
        result[1] = a * v[1];
        return result;
    }

    private static double[] addVect(final double[] a, final double[] b) {
        double[] result = new double[2];
        result[0] = a[0] + b[0];
        result[1] = a[1] + b[1];
        return result;
    }

    private static double[] subVect(final double[] a, final double[] b) {
        double[] result = new double[2];
        result[0] = a[0] - b[0];
        result[1] = a[1] - b[1];
        return result;
    }

    private static double[] applyMatrix(final double[] m, final double[] x) {
        double[] result = new double[2];
        result[0] = m[0] * x[0] + m[1] * x[1];
        result[1] = m[2] * x[0] + m[3] * x[1];
        return result;
    }

    private interface Evaluator {
        void evaluate(double[] var, double[] result);
        //void evaluateJacobian(double[] x1, double[] result);
    }

    private static class DiffEvaluator {
        private DifferentiableMap model;
        private double[] parameters;

        DiffEvaluator(
                final DifferentiableMap m, final double[] parameters) {

            this.model = m;
            this.parameters = parameters;
        }

        public void evaluate(final double[] var, final double[] result) {
            model.evaluate(parameters, var, result);
        }

        public void evaluateJacobian(final double[] x1, final double[] result) {
            model.evaluateJacobian(parameters, x1, result);
        }
    }

    private static class StraightEvaluator implements Evaluator {
        private SimpleMap evaluator;
        private double[] parameters;

        StraightEvaluator(final SimpleMap m, final double[] parameters) {
            this.evaluator = m;
            this.parameters = parameters;
        }

        public void evaluate(final double[] var, final double[] result) {

            evaluator.evaluate(parameters, var, result);
            evaluator.evaluate(parameters, result, result);
        }
    }

    private static class SquareEvaluator implements Evaluator {
        private SimpleMap evaluator;
        private double[] parameters;

        SquareEvaluator(final SimpleMap m, final double[] parameters) {
            this.evaluator = m;
            this.parameters = parameters;
        }

        public void evaluate(final double[] var, final double[] result) {

            evaluator.evaluate(parameters, var, result);
            evaluator.evaluate(parameters, result, result);
        }
    }

    private static class PowerEvaluator {
        private Evaluator evaluator;

        PowerEvaluator(final Evaluator m) {
            this.evaluator = m;
        }

        public void evaluate(
                final int n, final double[] var, final double[] result) {

            evaluator.evaluate(var, result);
            for (int i = 1; i < n; i++) {
                evaluator.evaluate(result, result);
            }
        }
    }


    public void initialize(
            byte type,
            VariableDoubles nodeEsteem, VariableDoubles parameters,
            double epsilon, Range lockoutHorRange, Range lockoutVerRange,
            int firstIteration, int lastIteration) {

        this.type = type;
        this.nodeEsteem = nodeEsteem;
        this.parameters = parameters;
        this.epsilon = epsilon;
        this.lockoutHorRange = lockoutHorRange;
        this.lockoutVerRange = lockoutVerRange;
        this.firstIteration = firstIteration;
        this.lastIteration = lastIteration;
    }

    public void stop() {
        stopped = true;
    }

    public int getState() {
        return state;
    }

    public void setState(final int i) {
        state = i;
    }
}
