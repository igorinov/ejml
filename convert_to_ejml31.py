#!/usr/bin/python

import fnmatch
import os
import sys


def findReplace(directory, find, replace, filePattern):
    changed = 0
    examined = 0
    for path, dirs, files in os.walk(os.path.abspath(directory)):
        for filename in fnmatch.filter(files, filePattern):
            examined += 1
            filepath = os.path.join(path, filename)
            with open(filepath) as f:
                s = f.read()
            c = s.replace(find, replace)
            if s != c:
                changed += 1
                with open(filepath, "w") as f:
                    f.write(c)
    if changed > 0:
        print "changed {:4d} examined {:4d} {:s} -> {:s}".format(changed,examined,find,replace)

if len(sys.argv) < 2:
    print "Need to specify where to apply the script to"
    exit(0)

location = sys.argv[1]

print "Recursively apply search and replace to "+location

def F(find,replace):
    findReplace(location,find,replace,"*.java")

F("DenseMatrix64F","RowMatrix_F64")
F("DenseMatrix32F","RowMatrix_F32")
F("BlockMatrix64F","BlockMatrix_F64")
F("BlockMatrix32F","BlockMatrix_F32")
F("ComplexMatrix64F","Matrix_C64")
F("ComplexMatrix32F","Matrix_C32")
F("CDenseMatrix64F","RowMatrix_C64")
F("CDenseMatrix32F","RowMatrix_C32")
F("Complex64F","Complex_F64")
F("Complex32F","Complex_F32")
F("ComplexPolar64F","ComplexPolar_F64")
F("ComplexPolar32F","ComplexPolar_F32")
F("ComplexMath64F","ComplexMath_F64")
F("ComplexMath32F","ComplexMath_F32")
F("DenseMatrixBool","RowMatrix_B")
F("EigenPair64F","EigenPair_F64")
F("EigenPair32F","EigenPair_F32")

for n in range(2,7):
    suf1 = str(n)
    suf2 = str(n)+"x"+str(n)
    F("FixedMatrix"+suf1+"_64F","FixedMatrix"+suf1+"_F64")
    F("FixedMatrix"+suf2+"_64F","FixedMatrix"+suf2+"_F64")
    F("FixedMatrix"+suf1+"_32F","FixedMatrix"+suf1+"_F32")
    F("FixedMatrix"+suf2+"_32F","FixedMatrix"+suf2+"_F32")

F("_D64","_R64")
F("_D32","_R32")
F("_CD64","_CR64")
F("_CD32","_CR32")

F("CommonOps.","CommonOps_R64")
F("CovarianceOps.","CovarianceOps_R64")
F("EigenOps.","EigenOps_R64")
F("MatrixFeatures.","MatrixFeatures_R64")
F("NormOps.","NormOps_R64")
F("RandomMatrices.","RandomMatrices_R64")
F("SingularOps.","SingularOps_R64")
F("SpecializedOps.","SpecializedOps_R64")

print "Finished!"
