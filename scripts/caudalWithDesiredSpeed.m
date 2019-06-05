function caudalWithDesiredSpeed(desiredSpeed, index)
    fid = fopen(sprintf("./output/desiredSpeeds/flow_file_DS=%.1f_%d.txt", desiredSpeed, index));

    # Read initial out time
    times = [0.0];
    initialT = str2num(fgetl(fid));
    times = [times, initialT];

	# Read file
	lineCounter = 1;
	while (!feof(fid))
		# Parse out time
		times = [times, str2num(fgetl(fid))];
	endwhile

	fclose(fid);

 %    interval = 10;

 %    numberOfParticles = 100;

	% flows = zeros(numberOfParticles + 1, 1);
	% flows(1) = 0.0;

 %    for i = 1:1:numberOfParticles
 %        #if((times(i) + interval) > times(end))
 %        #    break
 %        #endif
 %        evacuatedIPlusInterval = max(find(times(i:end) < (times(i) + interval)));
 %        evacuatedI = max(find(times(i:end) < times(i)));
 %        if(isempty(evacuatedI))
 %            evacuatedI = 0;
 %        endif
 %    	flows(i+1) = (evacuatedIPlusInterval - evacuatedI) / interval;
 %    endfor


    % TP5 Caudal

    N = 15;

    lowerLimit = 1;
    finalLowerLimit = (size(times,2) - 1) - N + 1;

    numberOfFlows = finalLowerLimit - lowerLimit + 1;

    flows = zeros(numberOfFlows + 1, 1);
    flows(1) = 0.0;

    deltaTs = zeros(numberOfFlows + 1, 1);
    deltaTs(1) = 0.0;

    for i = lowerLimit:1:finalLowerLimit
        ti = times(i);
        tf = times(i + N);
        deltaT = tf - ti;
        flows((i-lowerLimit)+2) = N / deltaT;
        deltaTs((i-lowerLimit)+2) = times(i + N);
    endfor

    props = {"marker", '.', 'LineStyle', 'none'};
    h = plot(deltaTs, flows, sprintf(";Vd = %.1f m/s;", desiredSpeed));
    set(h, props{:})
    xlabel("Tiempo (s)");
    ylabel("Caudal [part./s]");
    legend("location", "eastoutside");
    grid on

    print(sprintf("%s/caudal-DS=%.1f.png", './output/desiredSpeeds', desiredSpeed), "-dpngcairo", "-F:12")
end