function flowWithDesiredSpeedMeans(desiredSpeed, simulations)
    means = zeros(simulations, 101); % N = 100
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
    h = errorbar((0:100), mean(means), std(means), sprintf(";Vd = %.1f m/s;", desiredSpeed));
    set(h, props{:})
    xlabel("Número de peatones que salieron");
    ylabel("Tiempo (s)");
    legend("location", "eastoutside");
    xlim([0, 110])
    grid on

    m = mean(evacuationTimes);
    means_file_id = fopen('./output/desiredSpeeds/means.txt', 'a');
    fprintf(means_file_id, '%e ', m);
    fclose(means_file_id);

    s = std(evacuationTimes);
    stds_file_id = fopen('./output/desiredSpeeds/stds.txt', 'a');
    fprintf(stds_file_id, '%e ', s);
    fclose(stds_file_id);

    print(sprintf("%s/flow-DS=%.1f-Mean.png", './output/desiredSpeeds', desiredSpeed), "-dpngcairo", "-F:12")

    interval = 10;

    flows = zeros(100 + 1, 1);
    flows(1) = 0.0;

    x = mean(means);

    for i = 1:1:100
        flows(i+1) = max(find(x(i:end) < (x(i) + interval)));
    endfor

    props = {"marker", '.', 'LineStyle', 'none'};
    h = plot(times, flows, sprintf(";Vd = %.1f m/s;", desiredSpeed));
    set(h, props{:})
    xlabel("Tiempo (s)");
    ylabel("Caudal [part./s]");
    legend("location", "eastoutside");
    grid on

    print(sprintf("%s/caudal-DS=%.1f-Mean.png", './output/desiredSpeeds', desiredSpeed), "-dpngcairo", "-F:12")
end

