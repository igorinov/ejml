/*
 * Copyright (c) 2009-2016, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ejml.ops;

import org.ejml.UtilEjml;
import org.ejml.data.Complex64F;
import org.ejml.data.ComplexPolar64F;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestComplexMath64F {
    @Test
    public void conj() {
        Complex64F a = new Complex64F(2, 3);
        Complex64F b = new Complex64F(-3, 6);

        ComplexMath64F.conj(a, b);

        assertEquals(a.real, b.real, UtilEjml.TEST_64F);
        assertEquals(-a.imaginary, b.imaginary, UtilEjml.TEST_64F);
    }

    @Test
    public void plus() {
        Complex64F a = new Complex64F(2, 3);
        Complex64F b = new Complex64F(-3, 6);
        Complex64F c = new Complex64F();

        ComplexMath64F.plus(a, b, c);

        assertEquals(-1, c.real, UtilEjml.TEST_64F);
        assertEquals(9, c.imaginary, UtilEjml.TEST_64F);
    }

    @Test
    public void minus() {
        Complex64F a = new Complex64F(2, 3);
        Complex64F b = new Complex64F(-3, 6);
        Complex64F c = new Complex64F();

        ComplexMath64F.minus(a, b, c);

        assertEquals(5, c.real, UtilEjml.TEST_64F);
        assertEquals(-3, c.imaginary, UtilEjml.TEST_64F);
    }

    @Test
    public void multiply() {
        Complex64F a = new Complex64F(2, 3);
        Complex64F b = new Complex64F(-3, 6);
        Complex64F c = new Complex64F();

        ComplexMath64F.multiply(a, b, c);

        assertEquals(-24, c.real, UtilEjml.TEST_64F);
        assertEquals(3, c.imaginary, UtilEjml.TEST_64F);
    }

    @Test
    public void divide() {
        Complex64F a = new Complex64F(2, 3);
        Complex64F b = new Complex64F(-3, 6);
        Complex64F c = new Complex64F();

        ComplexMath64F.divide(a, b, c);

        assertEquals(0.26666666666, c.real, UtilEjml.TEST_64F);
        assertEquals(-0.466666666666, c.imaginary, UtilEjml.TEST_64F);
    }

    /**
     * Test conversion to and from polar form by doing just that and see if it gets the original answer again
     */
    @Test
    public void convert() {
        Complex64F a = new Complex64F(2, 3);
        ComplexPolar64F b = new ComplexPolar64F();
        Complex64F c = new Complex64F();

        ComplexMath64F.convert(a, b);
        ComplexMath64F.convert(b, c);

        assertEquals(a.real, c.real, UtilEjml.TEST_64F);
        assertEquals(a.imaginary, c.imaginary, UtilEjml.TEST_64F);
    }

    @Test
    public void mult_polar() {
        Complex64F a = new Complex64F(2, 3);
        Complex64F b = new Complex64F(-3, 6);
        Complex64F expected = new Complex64F();

        ComplexMath64F.multiply(a, b, expected);

        ComplexPolar64F pa = new ComplexPolar64F(a);
        ComplexPolar64F pb = new ComplexPolar64F(b);
        ComplexPolar64F pc = new ComplexPolar64F();

        ComplexMath64F.multiply(pa, pb, pc);

        Complex64F found = pc.toStandard();

        assertEquals(expected.real, found.real, UtilEjml.TEST_64F);
        assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_64F);
    }

    @Test
    public void div_polar() {
        Complex64F a = new Complex64F(2, 3);
        Complex64F b = new Complex64F(-3, 6);
        Complex64F expected = new Complex64F();

        ComplexMath64F.divide(a, b, expected);

        ComplexPolar64F pa = new ComplexPolar64F(a);
        ComplexPolar64F pb = new ComplexPolar64F(b);
        ComplexPolar64F pc = new ComplexPolar64F();

        ComplexMath64F.divide(pa, pb, pc);

        Complex64F found = pc.toStandard();

        assertEquals(expected.real, found.real, UtilEjml.TEST_64F);
        assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_64F);
    }

    @Test
    public void pow() {
        ComplexPolar64F a = new ComplexPolar64F(2, 0.2);
        ComplexPolar64F expected = new ComplexPolar64F();
        ComplexPolar64F found = new ComplexPolar64F();

        ComplexMath64F.multiply(a, a, expected);
        ComplexMath64F.multiply(a, expected, expected);

        ComplexMath64F.pow(a, 3, found);

        assertEquals(expected.r, found.r, UtilEjml.TEST_64F);
        assertEquals(expected.theta, found.theta, UtilEjml.TEST_64F);
    }

    @Test
    public void root_polar() {
        ComplexPolar64F expected = new ComplexPolar64F(2, 0.2);
        ComplexPolar64F root = new ComplexPolar64F();
        ComplexPolar64F found = new ComplexPolar64F();

        // compute the square root of a complex number then see if the
        // roots equal the output
        for (int i = 0; i < 2; i++) {
            ComplexMath64F.root(expected, 2, 0, root);

            ComplexMath64F.multiply(root, root, found);

            Complex64F e = expected.toStandard();
            Complex64F f = found.toStandard();

            assertEquals(e.real, f.real, UtilEjml.TEST_64F);
            assertEquals(e.imaginary, f.imaginary, UtilEjml.TEST_64F);
        }
    }

    @Test
    public void root_standard() {
        Complex64F expected = new Complex64F(2, 0.2);
        Complex64F root = new Complex64F();
        Complex64F found = new Complex64F();

        // compute the square root of a complex number then see if the
        // roots equal the output
        for (int i = 0; i < 2; i++) {
            ComplexMath64F.root(expected, 2, 0, root);

            ComplexMath64F.multiply(root, root, found);

            assertEquals(expected.real, found.real, UtilEjml.TEST_64F);
            assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_64F);
        }
    }

    @Test
    public void sqrt_standard() {
        Complex64F input = new Complex64F(2, 0.2);
        Complex64F root = new Complex64F();
        Complex64F found = new Complex64F();

        ComplexMath64F.sqrt(input, root);
        ComplexMath64F.multiply(root, root, found);

        assertEquals(input.real, found.real, UtilEjml.TEST_64F);
        assertEquals(input.imaginary, found.imaginary, UtilEjml.TEST_64F);

        input = new Complex64F(2, -0.2);

        ComplexMath64F.sqrt(input, root);
        ComplexMath64F.multiply(root, root, found);

        assertEquals(input.real, found.real, UtilEjml.TEST_64F);
        assertEquals(input.imaginary, found.imaginary, UtilEjml.TEST_64F);
    }
}
