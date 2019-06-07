import os
import subprocess
import csv
import numpy
import matplotlib.pyplot as plt
import math
from numpy import vstack, zeros, array, ones, linalg, transpose, delete, mean, power
from pylab import plot, show
from oct2py import octave
from oct2py.io import read_file
octave.addpath('./scripts/')

means_file = open("./output/desiredSpeeds/means.txt", "r")
mean_Qs = means_file.read().split();
mean_Qs = [float(i) for i in mean_Qs]
means_file.close()

stds_file = open("./output/desiredSpeeds/stds.txt", "r")
std_Qs = stds_file.read().split();
std_Qs = [float(i) for i in std_Qs]
stds_file.close()

speed_values = numpy.arange(start=1, stop=7, step=1.0);
speed_values = [1.0, 2.0, 2.4, 2.7, 3.0, 4.0, 5.0, 6.0]

# Prepare plot
f, ax = plt.subplots(1)

# Plot data and best fit curve
# [:-1] avoid v = 6.0
ax.errorbar(speed_values[:-1], mean_Qs[:-1], yerr=std_Qs[:-1], linestyle='None', marker='o', capsize=3)
#ax.errorbar(speed_values, mean_Qs, std_Qs, linestyle='None', marker='o', capsize=3)
ax.grid()
ax.set_ylim(bottom=0)
plt.xlabel("Velocidad deseada [m/s]")
plt.ylabel("Tiempo de evacuación [s]")
plt.xticks(speed_values[:-1])
plt.setp(ax.get_xticklabels(), rotation=45, horizontalalignment='right')

# Save plot
plt.savefig('./output/desiredSpeeds/evacuationTimes.png')