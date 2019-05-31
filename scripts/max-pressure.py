import os
import subprocess
import csv
import numpy
import matplotlib.pyplot as plt
import math
from numpy import vstack
from numpy import zeros
from oct2py import octave
octave.addpath('./scripts/')

desiredSpeed = 5.0
func = 'maxPressure(' + str(desiredSpeed) + ')'
octave.eval(func);