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

    interval = 10;

	flows = zeros(100 + 1, 1);
	flows(1) = 0.0;

    for i = 1:1:100
    	flows(i+1) = max(find(times(i:end) < (times(i) + interval)));
    endfor

    props = {"marker", '.', 'LineStyle', 'none'};
    h = plot(times, flows, sprintf(";Vd = %.1f m/s;", desiredSpeed));
    set(h, props{:})
    xlabel("Tiempo (s)");
    ylabel("Caudal [part./s]");
    legend("location", "eastoutside");
    grid on

    print(sprintf("%s/caudal-DS=%.1f.png", './output/desiredSpeeds', desiredSpeed), "-dpngcairo", "-F:12")
end