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

package org.ejml.alg.dense.decomposition.bidiagonal;

import org.ejml.UtilEjml;
import org.ejml.alg.dense.decomposition.CheckDecompositionInterface_D64;
import org.ejml.data.DenseMatrix64F;
import org.ejml.interfaces.decomposition.BidiagonalDecomposition_F64;
import org.ejml.ops.MatrixFeatures_D64;
import org.ejml.ops.RandomMatrices_D64;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public abstract class GenericBidiagonalCheck {
    protected Random rand = new Random(0xff);

    abstract protected BidiagonalDecomposition_F64<DenseMatrix64F> createQRDecomposition();

    @Test
    public void testModifiedInput() {
        CheckDecompositionInterface_D64.checkModifiedInput(createQRDecomposition());
    }

    @Test
    public void testRandomMatrices() {
        BidiagonalDecomposition_F64<DenseMatrix64F> decomp = createQRDecomposition();

        for( int i = 0; i < 10; i++ ) {
            for( int N = 2;  N <= 10; N++ ) {
                for( int tall = 0; tall <= 2; tall++ ) {
                    DenseMatrix64F A = RandomMatrices_D64.createRandom(N+tall,N,rand);

                    assertTrue(decomp.decompose(A.copy()));

                    checkGeneric(A, decomp);
                }
                for( int wide = 1; wide <= 2; wide++ ) {
                    DenseMatrix64F A = RandomMatrices_D64.createRandom(N,N+wide,rand);

                    assertTrue(decomp.decompose(A.copy()));

                    checkGeneric(A, decomp);
                }
            }
        }
    }

    @Test
    public void testIdentity() {
        SimpleMatrix A = SimpleMatrix.identity(5);

        BidiagonalDecomposition_F64<DenseMatrix64F> decomp = createQRDecomposition();

        assertTrue(decomp.decompose(A.getMatrix().copy()));

        checkGeneric(A.getMatrix(), decomp);
    }

    @Test
    public void testZero() {
        SimpleMatrix A = new SimpleMatrix(5,5);

        BidiagonalDecomposition_F64<DenseMatrix64F> decomp = createQRDecomposition();

        assertTrue(decomp.decompose(A.getMatrix().copy()));

        checkGeneric(A.getMatrix(), decomp);
    }

    /**
     * Checks to see if the decomposition will reconstruct the original input matrix
     */
    protected void checkGeneric(DenseMatrix64F a,
                                BidiagonalDecomposition_F64<DenseMatrix64F> decomp) {
        // check the full version
        SimpleMatrix U = SimpleMatrix.wrap(decomp.getU(null,false,false));
        SimpleMatrix B = SimpleMatrix.wrap(decomp.getB(null,false));
        SimpleMatrix V = SimpleMatrix.wrap(decomp.getV(null,false,false));

        DenseMatrix64F foundA = U.mult(B).mult(V.transpose()).getMatrix();

        assertTrue(MatrixFeatures_D64.isIdentical(a,foundA,UtilEjml.TEST_64F));

        //       check with transpose
        SimpleMatrix Ut = SimpleMatrix.wrap(decomp.getU(null,true,false));

        assertTrue(U.transpose().isIdentical(Ut, UtilEjml.TEST_64F));

        SimpleMatrix Vt = SimpleMatrix.wrap(decomp.getV(null,true,false));

        assertTrue(V.transpose().isIdentical(Vt,UtilEjml.TEST_64F));

//        U.print();
//        V.print();
//        B.print();
//        System.out.println("------------------------");

        // now test compact
        U = SimpleMatrix.wrap(decomp.getU(null,false,true));
        B = SimpleMatrix.wrap(decomp.getB(null,true));
        V = SimpleMatrix.wrap(decomp.getV(null,false,true));

//        U.print();
//        V.print();
//        B.print();

        foundA = U.mult(B).mult(V.transpose()).getMatrix();

        assertTrue(MatrixFeatures_D64.isIdentical(a,foundA,UtilEjml.TEST_64F));

        //       check with transpose
        Ut = SimpleMatrix.wrap(decomp.getU(null,true,true));
        Vt = SimpleMatrix.wrap(decomp.getV(null,true,true));

        assertTrue(U.transpose().isIdentical(Ut,UtilEjml.TEST_64F));
        assertTrue(V.transpose().isIdentical(Vt,UtilEjml.TEST_64F));
    }

}
