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

speed_values = [1.0, 1.9, 2.0, 2.1, 2.3, 2.4, 2.5, 3.0, 4.0, 5.0, 6.0]

# Prepare plot
f, ax = plt.subplots(1)

# Plot data and best fit curve
ax.errorbar(speed_values, mean_Qs, std_Qs, linestyle='None', marker='o', capsize=3)
ax.grid()
ax.set_ylim(bottom=0)
plt.xlabel("Velocidad deseada [m/s]")
plt.ylabel("Tiempo de evacuación [s]")
plt.xticks(speed_values)

ax.set_xticks([1.0, 2.0, 3.0, 4.0, 5.0, 6.0])

plt.xlim(0.5, 6.5)
plt.ylim(80, 125)

# Save plot
plt.savefig('./output/desiredSpeeds/evacuationTimes.png')