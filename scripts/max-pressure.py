import os
import sys
from oct2py import octave
octave.addpath('./scripts/')

desiredSpeed = sys.argv[1]
index = sys.argv[2]
func = 'maxPressure(' + str(desiredSpeed) + ',' + str(index) + ')'
octave.eval(func);