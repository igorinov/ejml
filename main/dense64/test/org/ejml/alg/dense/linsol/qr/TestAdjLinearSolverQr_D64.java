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

package org.ejml.alg.dense.linsol.qr;

import org.ejml.UtilEjml;
import org.ejml.alg.dense.linsol.AdjustableLinearSolver_D64;
import org.ejml.alg.dense.linsol.GenericLinearSolverChecks_D64;
import org.ejml.alg.dense.mult.SubmatrixOps_D64;
import org.ejml.data.DenseMatrix64F;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.ops.CommonOps_D64;
import org.ejml.ops.MatrixFeatures_D64;
import org.ejml.ops.RandomMatrices_D64;
import org.junit.Test;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestAdjLinearSolverQr_D64 extends GenericLinearSolverChecks_D64 {


    @Test
    public void addRowToA() {
        int insert = 2;
        int m = 5;
        int n = 3;

        DenseMatrix64F A = RandomMatrices_D64.createRandom(m,n,rand);
        double row[] = new double[]{1,2,3};

        // create the modified A
        DenseMatrix64F A_e = RandomMatrices_D64.createRandom(m+1,n,rand);
        SubmatrixOps_D64.setSubMatrix(A,A_e,0,0,0,0,insert,n);
        System.arraycopy(row, 0, A_e.data, insert * n, n);
        SubmatrixOps_D64.setSubMatrix(A,A_e,insert,0,insert+1,0,m-insert,n);

        // Compute the solution to the modified  system
        DenseMatrix64F X = RandomMatrices_D64.createRandom(n,2,rand);
        DenseMatrix64F Y = new DenseMatrix64F(A_e.numRows,X.numCols);
        CommonOps_D64.mult(A_e,X,Y);

        // create the solver from A then add a A.  The solver
        // should be equivalent to one created from A_e
        AdjustableLinearSolver_D64 adjSolver = new AdjLinearSolverQr_D64();

        assertTrue(adjSolver.setA(A));
        adjSolver.addRowToA(row,insert);

        // solve the system and see if it gets the expected solution
        DenseMatrix64F X_found = RandomMatrices_D64.createRandom(X.numRows,X.numCols,rand);
        adjSolver.solve(Y,X_found);

        // see if they produce the same results
        assertTrue(MatrixFeatures_D64.isIdentical(X_found,X, UtilEjml.TEST_64F));
    }

    @Test
    public void removeRowFromA() {
        int remove = 2;
        int m = 5;
        int n = 3;

        DenseMatrix64F A = RandomMatrices_D64.createRandom(m,n,rand);

        // create the modified A
        DenseMatrix64F A_e = RandomMatrices_D64.createRandom(m-1,n,rand);
        SubmatrixOps_D64.setSubMatrix(A,A_e,0,0,0,0,remove,n);
        SubmatrixOps_D64.setSubMatrix(A,A_e,remove+1,0,remove,0,m-remove-1,n);

        // Compute the solution to the modified system
        DenseMatrix64F X = RandomMatrices_D64.createRandom(n,2,rand);
        DenseMatrix64F Y = new DenseMatrix64F(A_e.numRows,X.numCols);
        CommonOps_D64.mult(A_e,X,Y);

        // create the solver from the original system then modify it
        AdjustableLinearSolver_D64 adjSolver = new AdjLinearSolverQr_D64();

        adjSolver.setA(A);
        adjSolver.removeRowFromA(remove);

        // see if it produces the epected results

        // solve the system and see if it gets the expected solution
        DenseMatrix64F X_found = RandomMatrices_D64.createRandom(X.numRows,X.numCols,rand);
        adjSolver.solve(Y,X_found);

        // see if they produce the same results
        assertTrue(MatrixFeatures_D64.isIdentical(X_found,X,UtilEjml.TEST_64F));
    }

    @Override
    protected LinearSolver createSolver( DenseMatrix64F A ) {
        return new AdjLinearSolverQr_D64();
    }
}
