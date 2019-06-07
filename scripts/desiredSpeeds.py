import os
import subprocess
import numpy
from numpy import arange, zeros
from oct2py import octave
octave.addpath('./scripts/')

dirName='./output/desiredSpeeds'

if not os.path.exists(dirName):
        os.mkdir(dirName)
        print("Directory ", dirName, " Created ")

#deltaT=0.0001

numberOfPedestrians = 200

# speed_values = arange(1.0, 7)
# speed_values = [1.0, 2.0, 2.2, 2.4, 3.0, 4.0, 5.0, 6.0]
# speed_values = [1.0, 2.0, 2.4, 2.7, 3.0, 4.0, 5.0, 6.0]
speed_values = [1.0, 2.0, 3.0, 4.0, 5.0, 6.0]
print(speed_values)

times = 10

open(dirName + '/means.txt', 'w').close()
open(dirName + '/stds.txt', 'w').close()

processes = []

graph = True

if not graph:
	os.system('mvn clean package')

# For each desired speed value
for i in range(0, len(speed_values)):
	for k in arange(0, times):
		if not graph:
			p = subprocess.Popen(['java', '-jar', './target/pedestrian-dynamics-1.0-SNAPSHOT.jar',
				'--index={index}'.format(index = k),
				'--numberOfPedestrians={numberOfPedestrians}'.format(numberOfPedestrians = numberOfPedestrians),
				'--desiredSpeed={desiredSpeed}'.format(desiredSpeed = speed_values[i])]);
			processes.append(p);
			# subprocess.call(['java', '-jar', './target/pedestrian-dynamics-1.0-SNAPSHOT.jar',
			# '--index={index}'.format(index = k+1),
			# '--numberOfPedestrians={numberOfPedestrians}'.format(numberOfPedestrians = numberOfPedestrians),
			# '--desiredSpeed={desiredSpeed}'.format(desiredSpeed = speed_values[i])])
		#else:
			#fileName = '_DS={desiredSpeed}_{index}.txt'.format(desiredSpeed = speed_values[i], index = k)
			#exists = os.path.isfile('./output/flow_file' + fileName)
			#if exists:
			#	os.rename('./output/flow_file' + fileName, dirName + '/flow_file' + fileName)
			#exists = os.path.isfile('./output/ovito_file' + fileName)
			#if exists:
			#	os.rename('./output/ovito_file' + fileName, dirName + '/ovito_file' + fileName)
			#func = 'flowWithDesiredSpeed(' + str(speed_values[i]) + ',' + str(k) + ',' + str(times) + ')';
			#octave.eval(func);
	if graph:
        #func = 'caudalWithDesiredSpeed(' + str(speed_values[i]) + ',' + str(0) + ')';
        #octave.eval(func);
	    func = 'flowWithDesiredSpeedMeans(' + str(speed_values[i]) + ',' + str(times) + ')';
	    octave.eval(func);
if not graph:
    # wait
    for process in processes:
        process.wait()