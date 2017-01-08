/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decomposition.svd;

import org.ejml.data.RowMatrix_F64;
import org.ejml.factory.DecompositionFactory_R64;
import org.ejml.interfaces.decomposition.SingularValueDecomposition;
import org.ejml.ops.CommonOps_R64;
import org.ejml.ops.RandomMatrices_R64;

import java.util.Random;


/**
 * Compare the speed of various algorithms at inverting square matrices
 *
 * @author Peter Abeles
 */
public class StabilitySvdlDecomposition {

    private static boolean compact = false;
    // These two options need to be true to compute the quality
    private static boolean computeU = true;
    private static boolean computeV = true;


    public static double evaluate(SingularValueDecomposition<RowMatrix_F64> alg , RowMatrix_F64 orig ) {

        RowMatrix_F64 U=null;
        RowMatrix_F64 W;
        RowMatrix_F64 Vt=null;

        if( !alg.decompose(orig.copy())) {
            return Double.NaN;
        }

        W = alg.getW(null);
        if( computeU )
            U = alg.getU(null,false).copy();
        if( computeV )
            Vt = alg.getV(null,true).copy();

        // I'm not sure how to test quality of U or V is not computed.

        return DecompositionFactory_R64.quality(orig, U,W,Vt);
    }

    private static void runAlgorithms( RowMatrix_F64 mat )
    {
        System.out.println("qr               = "+ evaluate(new SvdImplicitQrDecompose_R64(compact,computeU,computeV,false),mat));
        System.out.println("qr ult           = "+ evaluate(new SvdImplicitQrDecompose_Ultimate(compact,computeU,computeV),mat));
    }

    public static void main( String args [] ) {
        Random rand = new Random(239454923);

        int numCols = 10;
        int numRows = 30;
        double scales[] = new double[]{1,0.1,1e-20,1e-100,1e-200,1e-300,1e-304,1e-308,1e-310,1e-312,1e-319,1e-320,1e-321,Double.MIN_VALUE};


        RowMatrix_F64 orig = RandomMatrices_R64.createRandom(numRows,numCols,-1,1,rand);
        RowMatrix_F64 mat = orig.copy();
        // results vary significantly depending if it starts from a small or large matrix
        for( int i = 0; i < scales.length; i++ ) {
            System.out.printf("  Decomposition size %3d %d for %e scale\n",numRows,numCols,scales[i]);
            CommonOps_R64.scale(scales[i],orig,mat);
            runAlgorithms(mat);
        }

        System.out.println();
        System.out.println("Done.");
    }
}