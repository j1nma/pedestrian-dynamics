function flowWithDesiredSpeedMeans(desiredSpeed, simulations)

    numberOfPedestrians = 200;

    means = zeros(simulations, numberOfPedestrians + 1); % N = numberOfPedestrians
    evacuationTimes = zeros(simulations, 1);
    for i = 0:1:simulations-1
        fid = fopen(sprintf("./output/desiredSpeeds/flow_file_DS=%.1f_%d.txt", desiredSpeed, i));

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

        means(i+1, :) = times;

        evacuationTimes(i+1) = times(end);
    endfor

    hold off

    props = {"marker", '.', 'LineStyle', 'none'};
    h = errorbar((0:numberOfPedestrians), mean(means), std(means), sprintf(";Vd = %.1f m/s;", desiredSpeed));
    set(h, props{:})
    xlabel("Número de peatones que salieron");
    ylabel("Tiempo (s)");
    legend("location", "eastoutside");
    xlim([0, numberOfPedestrians + 10])
    grid on

    m = mean(evacuationTimes);
    means_file_id = fopen('./output/desiredSpeeds/means.txt', 'a');
    fprintf(means_file_id, '%e ', m);
    fclose(means_file_id);

    s = std(evacuationTimes);
    stds_file_id = fopen('./output/desiredSpeeds/stds.txt', 'a');
    fprintf(stds_file_id, '%e ', s);
    fclose(stds_file_id);

    % Esto es para el punto B:
    % print(sprintf("%s/flow-DS=%.1f-Mean.png", './output/desiredSpeeds', desiredSpeed), "-dpngcairo", "-F:12")


    % Esto es para el punto C (medias):
    N = 20;
    lowerLimit = 1;
    finalLowerLimit = (size(times,2) - 1) - N + 1;
    
	numberOfFlows = finalLowerLimit - lowerLimit + 1;

    flows = zeros(simulations, numberOfFlows + 1);

    deltaTs = zeros(simulations, numberOfFlows + 1);
    
    for j = 0:1:simulations-1
		for i = lowerLimit:1:finalLowerLimit
        	ti = means(j+1, i);
        	tf = means(j+1, i + N);
        	deltaT = tf - ti;
        	flows(j+1, (i-lowerLimit)+2) = N / deltaT;
        	deltaTs(j+1, (i-lowerLimit)+2) = tf;
		endfor
	endfor
    
    props = {"marker", '.', 'LineStyle', 'none'};
    h = errorbar(mean(deltaTs), mean(flows), std(flows), sprintf(";Vd = %.1f m/s;", desiredSpeed));
    set(h, props{:})
    xlabel("Tiempo (s)");
    ylabel("Caudal [part./s]");
    legend("location", "eastoutside");
    xlim([0, 100])
    ylim([1, 3.5])
    grid on

    print(sprintf("%s/caudal-DS=%.1f-Mean.png", './output/desiredSpeeds', desiredSpeed), "-dpngcairo", "-F:12")
end

