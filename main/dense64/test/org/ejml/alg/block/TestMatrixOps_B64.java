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

package org.ejml.alg.block;

import org.ejml.UtilEjml;
import org.ejml.alg.generic.GenericMatrixOps_F64;
import org.ejml.data.BlockMatrix_F64;
import org.ejml.data.D1Submatrix_F64;
import org.ejml.data.RowMatrix_F64;
import org.ejml.ops.CommonOps_R64;
import org.ejml.ops.RandomMatrices_R64;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public class TestMatrixOps_B64 {

    final static int BLOCK_LENGTH = 10;

    Random rand = new Random(234);

    @Test
    public void convert_dense_to_block() {
        checkConvert_dense_to_block(10,10);
        checkConvert_dense_to_block(5,8);
        checkConvert_dense_to_block(12,16);
        checkConvert_dense_to_block(16,12);
        checkConvert_dense_to_block(21,27);
        checkConvert_dense_to_block(28,5);
        checkConvert_dense_to_block(5,28);
        checkConvert_dense_to_block(20,20);
    }

    private void checkConvert_dense_to_block( int m , int n ) {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(m,n,rand);
        BlockMatrix_F64 B = new BlockMatrix_F64(A.numRows,A.numCols,BLOCK_LENGTH);

        MatrixOps_B64.convert(A,B);

        assertTrue( GenericMatrixOps_F64.isEquivalent(A,B, UtilEjml.TEST_F64));
    }

    @Test
    public void convertInline_dense_to_block() {
        for( int i = 2; i < 30; i += 5 ) {
            for( int j = 2; j < 30; j += 5 ) {
                checkConvertInline_dense_to_block(i,j);
            }
        }
    }

    private void checkConvertInline_dense_to_block( int m , int n ) {
        double tmp[] = new double[BLOCK_LENGTH*n];
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(m,n,rand);
        RowMatrix_F64 A_orig = A.copy();

        MatrixOps_B64.convertRowToBlock(m,n,BLOCK_LENGTH,A.data,tmp);
        BlockMatrix_F64 B = BlockMatrix_F64.wrap(A.data,A.numRows,A.numCols,BLOCK_LENGTH);

        assertTrue( GenericMatrixOps_F64.isEquivalent(A_orig,B,UtilEjml.TEST_F64));
    }

    @Test
    public void convert_block_to_dense() {
        checkBlockToDense(10,10);
        checkBlockToDense(5,8);
        checkBlockToDense(12,16);
        checkBlockToDense(16,12);
        checkBlockToDense(21,27);
        checkBlockToDense(28,5);
        checkBlockToDense(5,28);
        checkBlockToDense(20,20);
    }

    private void checkBlockToDense( int m , int n ) {
        RowMatrix_F64 A = new RowMatrix_F64(m,n);
        BlockMatrix_F64 B = MatrixOps_B64.createRandom(m,n,-1,1,rand);

        MatrixOps_B64.convert(B,A);

        assertTrue( GenericMatrixOps_F64.isEquivalent(A,B,UtilEjml.TEST_F64));
    }

    @Test
    public void convertInline_block_to_dense() {
        for( int i = 2; i < 30; i += 5 ) {
            for( int j = 2; j < 30; j += 5 ) {
                checkConvertInline_block_to_dense(i,j);
            }
        }
    }

    private void checkConvertInline_block_to_dense( int m , int n ) {
        double tmp[] = new double[BLOCK_LENGTH*n];
        BlockMatrix_F64 A = MatrixOps_B64.createRandom(m,n,-1,1,rand,BLOCK_LENGTH);
        BlockMatrix_F64 A_orig = A.copy();

        MatrixOps_B64.convertBlockToRow(m,n,BLOCK_LENGTH,A.data,tmp);
        RowMatrix_F64 B = RowMatrix_F64.wrap(A.numRows,A.numCols,A.data);

        assertTrue( GenericMatrixOps_F64.isEquivalent(A_orig,B,UtilEjml.TEST_F64));
    }

    /**
     * Makes sure the bounds check on input matrices for mult() is done correctly
     */
    @Test
    public void testMultInputChecks() {
        Method methods[] = MatrixOps_B64.class.getDeclaredMethods();

        int numFound = 0;
        for( Method m : methods) {
            String name = m.getName();

            if( !name.contains("mult"))
                continue;

            boolean transA = false;
            boolean transB = false;

            if( name.contains("TransA"))
                transA = true;

            if( name.contains("TransB"))
                transB = true;

            checkMultInput(m,transA,transB);
            numFound++;
        }

        // make sure all the functions were in fact tested
        assertEquals(3,numFound);
    }

    /**
     * Makes sure exceptions are thrown for badly shaped input matrices.
     */
    private void checkMultInput( Method func, boolean transA , boolean transB ) {
        // bad block size
        BlockMatrix_F64 A = new BlockMatrix_F64(5,4,3);
        BlockMatrix_F64 B = new BlockMatrix_F64(4,6,3);
        BlockMatrix_F64 C = new BlockMatrix_F64(5,6,4);

        invokeErrorCheck(func, transA , transB , A, B, C);
        C.blockLength = 3;
        B.blockLength = 4;
        invokeErrorCheck(func, transA , transB ,A, B, C);
        B.blockLength = 3;
        A.blockLength = 4;
        invokeErrorCheck(func, transA , transB , A, B, C);
        A.blockLength = 3;

        // check for bad size C
        C.numCols = 7;
        invokeErrorCheck(func,transA , transB ,A,B,C);
        C.numCols = 6;
        C.numRows = 4;
        invokeErrorCheck(func,transA , transB ,A,B,C);

        // make A and B incompatible
        A.numCols = 3;
        invokeErrorCheck(func,transA , transB ,A,B,C);
    }

    private void invokeErrorCheck(Method func, boolean transA , boolean transB ,
                                  BlockMatrix_F64 a, BlockMatrix_F64 b, BlockMatrix_F64 c) {

        if( transA )
            a = MatrixOps_B64.transpose(a,null);
        if( transB )
            b = MatrixOps_B64.transpose(b,null);

        try {
            func.invoke(null, a, b, c);
            fail("No exception");
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            if( !(e.getCause() instanceof IllegalArgumentException) )
                fail("Unexpected exception: "+e.getCause().getMessage());
        }
    }

    /**
     * Tests for correctness multiplication of an entire matrix for all multiplication operations.
     */
    @Test
    public void testMultSolution() {
        Method methods[] = MatrixOps_B64.class.getDeclaredMethods();

        int numFound = 0;
        for( Method m : methods) {
            String name = m.getName();

            if( !name.contains("mult"))
                continue;

//            System.out.println("name = "+name);

            boolean transA = false;
            boolean transB = false;

            if( name.contains("TransA"))
                transA = true;

            if( name.contains("TransB"))
                transB = true;

            checkMult(m,transA,transB);
            numFound++;
        }

        // make sure all the functions were in fact tested
        assertEquals(3,numFound);
    }

    /**
     * Test the method against various matrices of different sizes and shapes which have partial
     * blocks.
     */
    private void checkMult( Method func, boolean transA , boolean transB ) {
        // trivial case
        checkMult(func,transA,transB,BLOCK_LENGTH, BLOCK_LENGTH, BLOCK_LENGTH);

        // stuff larger than the block size
        checkMult(func,transA,transB,BLOCK_LENGTH+1, BLOCK_LENGTH, BLOCK_LENGTH);
        checkMult(func,transA,transB,BLOCK_LENGTH, BLOCK_LENGTH+1, BLOCK_LENGTH);
        checkMult(func,transA,transB,BLOCK_LENGTH, BLOCK_LENGTH, BLOCK_LENGTH+1);
        checkMult(func,transA,transB,BLOCK_LENGTH+1, BLOCK_LENGTH+1, BLOCK_LENGTH+1);

        // stuff smaller than the block size
        checkMult(func,transA,transB,BLOCK_LENGTH-1, BLOCK_LENGTH, BLOCK_LENGTH);
        checkMult(func,transA,transB,BLOCK_LENGTH, BLOCK_LENGTH-1, BLOCK_LENGTH);
        checkMult(func,transA,transB,BLOCK_LENGTH, BLOCK_LENGTH, BLOCK_LENGTH-1);
        checkMult(func,transA,transB,BLOCK_LENGTH-1, BLOCK_LENGTH-1, BLOCK_LENGTH-1);

        // stuff multiple blocks
        checkMult(func,transA,transB,BLOCK_LENGTH*2, BLOCK_LENGTH, BLOCK_LENGTH);
        checkMult(func,transA,transB,BLOCK_LENGTH, BLOCK_LENGTH*2, BLOCK_LENGTH);
        checkMult(func,transA,transB,BLOCK_LENGTH, BLOCK_LENGTH, BLOCK_LENGTH*2);
        checkMult(func,transA,transB,BLOCK_LENGTH*2, BLOCK_LENGTH*2, BLOCK_LENGTH*2);
        checkMult(func,transA,transB,BLOCK_LENGTH*2+4, BLOCK_LENGTH*2+3, BLOCK_LENGTH*2+2);
    }

    private void checkMult( Method func, boolean transA , boolean transB ,
                            int m, int n, int o) {
        RowMatrix_F64 A_d = RandomMatrices_R64.createRandom(m, n,rand);
        RowMatrix_F64 B_d = RandomMatrices_R64.createRandom(n, o,rand);
        RowMatrix_F64 C_d = new RowMatrix_F64(m, o);

        BlockMatrix_F64 A_b = MatrixOps_B64.convert(A_d,BLOCK_LENGTH);
        BlockMatrix_F64 B_b = MatrixOps_B64.convert(B_d,BLOCK_LENGTH);
        BlockMatrix_F64 C_b = MatrixOps_B64.createRandom(m, o, -1 , 1 , rand , BLOCK_LENGTH);

        if( transA )
            A_b= MatrixOps_B64.transpose(A_b,null);

        if( transB )
            B_b= MatrixOps_B64.transpose(B_b,null);

        CommonOps_R64.mult(A_d,B_d,C_d);
        try {
            func.invoke(null,A_b,B_b,C_b);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }

//        C_d.print();
//        C_b.print();
        assertTrue( GenericMatrixOps_F64.isEquivalent(C_d,C_b,UtilEjml.TEST_F64));
    }


    @Test
    public void convertTranSrc_block_to_dense() {
        checkTranSrcBlockToDense(10,10);
        checkTranSrcBlockToDense(5,8);
        checkTranSrcBlockToDense(12,16);
        checkTranSrcBlockToDense(16,12);
        checkTranSrcBlockToDense(21,27);
        checkTranSrcBlockToDense(28,5);
        checkTranSrcBlockToDense(5,28);
        checkTranSrcBlockToDense(20,20);
    }

    private void checkTranSrcBlockToDense( int m , int n ) {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(m,n,rand);
        RowMatrix_F64 A_t = new RowMatrix_F64(n,m);
        BlockMatrix_F64 B = new BlockMatrix_F64(n,m,BLOCK_LENGTH);

        CommonOps_R64.transpose(A,A_t);
        MatrixOps_B64.convertTranSrc(A,B);

        assertTrue( GenericMatrixOps_F64.isEquivalent(A_t,B,UtilEjml.TEST_F64));
    }

    @Test
    public void transpose() {
        checkTranspose(10,10);
        checkTranspose(5,8);
        checkTranspose(12,16);
        checkTranspose(16,12);
        checkTranspose(21,27);
        checkTranspose(28,5);
        checkTranspose(5,28);
        checkTranspose(20,20);
    }

    private void checkTranspose( int m , int n ) {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(m,n,rand);
        RowMatrix_F64 A_t = new RowMatrix_F64(n,m);

        BlockMatrix_F64 B = new BlockMatrix_F64(A.numRows,A.numCols,BLOCK_LENGTH);
        BlockMatrix_F64 B_t = new BlockMatrix_F64(n,m,BLOCK_LENGTH);

        MatrixOps_B64.convert(A,B);

        CommonOps_R64.transpose(A,A_t);
        MatrixOps_B64.transpose(B,B_t);

        assertTrue( GenericMatrixOps_F64.isEquivalent(A_t,B_t,UtilEjml.TEST_F64));
    }

    @Test
    public void zeroTriangle_upper() {
        int r = 3;

        for( int numRows = 2; numRows <= 6; numRows += 2 ){
            for( int numCols = 2; numCols <= 6; numCols += 2 ){
                BlockMatrix_F64 B = MatrixOps_B64.createRandom(numRows,numCols,-1,1,rand,r);
                MatrixOps_B64.zeroTriangle(true,B);

                for( int i = 0; i < B.numRows; i++ ) {
                    for( int j = 0; j < B.numCols; j++ ) {
                        if( j <= i )
                            assertTrue(B.get(i,j) != 0 );
                        else
                            assertTrue(B.get(i,j) == 0 );
                    }
                }
            }
        }
    }

    @Test
    public void zeroTriangle_lower() {

        int r = 3;

        for( int numRows = 2; numRows <= 6; numRows += 2 ){
            for( int numCols = 2; numCols <= 6; numCols += 2 ){
                BlockMatrix_F64 B = MatrixOps_B64.createRandom(numRows,numCols,-1,1,rand,r);

                MatrixOps_B64.zeroTriangle(false,B);

                for( int i = 0; i < B.numRows; i++ ) {
                    for( int j = 0; j < B.numCols; j++ ) {
                        if( j >= i )
                            assertTrue(B.get(i,j) != 0 );
                        else
                            assertTrue(B.get(i,j) == 0 );
                    }
                }
            }
        }
    }

    @Test
    public void copyTriangle() {

        int r = 3;

        // test where src and dst are the same size
        for( int numRows = 2; numRows <= 6; numRows += 2 ){
            for( int numCols = 2; numCols <= 6; numCols += 2 ){
                BlockMatrix_F64 A = MatrixOps_B64.createRandom(numRows,numCols,-1,1,rand,r);
                BlockMatrix_F64 B = new BlockMatrix_F64(numRows,numCols,r);

                MatrixOps_B64.copyTriangle(true,A,B);

                for( int i = 0; i < numRows; i++) {
                    for( int j = 0; j < numCols; j++ ) {
                        if( j >= i )
                            assertTrue(A.get(i,j) == B.get(i,j));
                        else
                            assertTrue( 0 == B.get(i,j));
                    }
                }

                CommonOps_R64.fill(B, 0);
                MatrixOps_B64.copyTriangle(false,A,B);
                
                for( int i = 0; i < numRows; i++) {
                    for( int j = 0; j < numCols; j++ ) {
                        if( j <= i )
                            assertTrue(A.get(i,j) == B.get(i,j));
                        else
                            assertTrue( 0 == B.get(i,j));
                    }
                }
            }
        }

        // now the dst will be smaller than the source
        BlockMatrix_F64 B = new BlockMatrix_F64(r+1,r+1,r);
        for( int numRows = 4; numRows <= 6; numRows += 1 ){
            for( int numCols = 4; numCols <= 6; numCols += 1 ){
                BlockMatrix_F64 A = MatrixOps_B64.createRandom(numRows,numCols,-1,1,rand,r);
                CommonOps_R64.fill(B, 0);

                MatrixOps_B64.copyTriangle(true,A,B);

                for( int i = 0; i < B.numRows; i++) {
                    for( int j = 0; j < B.numCols; j++ ) {
                        if( j >= i )
                            assertTrue(A.get(i,j) == B.get(i,j));
                        else
                            assertTrue( 0 == B.get(i,j));
                    }
                }

                CommonOps_R64.fill(B, 0);
                MatrixOps_B64.copyTriangle(false,A,B);

                for( int i = 0; i < B.numRows; i++) {
                    for( int j = 0; j < B.numCols; j++ ) {
                        if( j <= i )
                            assertTrue(A.get(i,j) == B.get(i,j));
                        else
                            assertTrue( 0 == B.get(i,j));
                    }
                }
            }
        }
    }

    @Test
    public void setIdentity() {
        int r = 3;

        for( int numRows = 2; numRows <= 6; numRows += 2 ){
            for( int numCols = 2; numCols <= 6; numCols += 2 ){
                BlockMatrix_F64 A = MatrixOps_B64.createRandom(numRows,numCols,-1,1,rand,r);

                MatrixOps_B64.setIdentity(A);

                for( int i = 0; i < numRows; i++ ) {
                    for( int j = 0; j < numCols; j++ ) {
                        if( i == j )
                            assertEquals(1.0,A.get(i,j),UtilEjml.TEST_F64);
                        else
                            assertEquals(0.0,A.get(i,j),UtilEjml.TEST_F64);
                    }
                }
            }
        }
    }

    @Test
    public void convertSimple() {
        BlockMatrix_F64 A = MatrixOps_B64.createRandom(4,6,-1,1,rand,3);

        SimpleMatrix S = new SimpleMatrix(A);

        assertEquals(A.numRows,S.numRows());
        assertEquals(A.numCols,S.numCols());

        for( int i = 0; i < A.numRows; i++ ) {
            for( int j = 0; j < A.numCols; j++ ) {
                assertEquals(A.get(i,j),S.get(i,j),UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void identity() {
        // test square
        BlockMatrix_F64 A = MatrixOps_B64.identity(4,4,3);
        assertTrue(GenericMatrixOps_F64.isIdentity(A,UtilEjml.TEST_F64));

        // test wide
        A = MatrixOps_B64.identity(4,5,3);
        assertTrue(GenericMatrixOps_F64.isIdentity(A,UtilEjml.TEST_F64));

        // test tall
        A = MatrixOps_B64.identity(5,4,3);
        assertTrue(GenericMatrixOps_F64.isIdentity(A,UtilEjml.TEST_F64));
    }

    @Test
    public void extractAligned() {
        BlockMatrix_F64 A = MatrixOps_B64.createRandom(10,11,-1,1,rand,3);
        BlockMatrix_F64 B = new BlockMatrix_F64(9,11,3);

        MatrixOps_B64.extractAligned(A,B);

        for( int i = 0; i < B.numRows; i++ ) {
            for( int j = 0; j < B.numCols; j++ ) {
                assertEquals(A.get(i,j),B.get(i,j),UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void blockAligned() {
        int r = 3;
        BlockMatrix_F64 A = MatrixOps_B64.createRandom(10,11,-1,1,rand,r);

        D1Submatrix_F64 S = new D1Submatrix_F64(A);

        assertTrue(MatrixOps_B64.blockAligned(r,S));

        S.row0 = r;
        S.col0 = 2*r;

        assertTrue(MatrixOps_B64.blockAligned(r,S));

        // test negative cases
        S.row0 = r-1;
        assertFalse(MatrixOps_B64.blockAligned(r,S));
        S.row0 = 0;
        S.col0 = 1;
        assertFalse(MatrixOps_B64.blockAligned(r,S));
        S.col0 = 0;
        S.row1 = 8;
        assertFalse(MatrixOps_B64.blockAligned(r,S));
        S.row1 = 10;
        S.col0 = 10;
        assertFalse(MatrixOps_B64.blockAligned(r,S));
    }

}
