function flowWithDesiredSpeed(desiredSpeed, index, lastIndex)
    fid = fopen(sprintf("./output/desiredSpeeds/flow_file_DS=%.1f_%d.txt", desiredSpeed, index));

    numberOfParticles = 200

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

    props = {"marker", '.', 'LineStyle', 'none'};
    h = plot((0:numberOfParticles), times, sprintf(";Egreso %d;", index));
    set(h, props{:})
    xlabel("Número de peatones que salieron");
    ylabel("Tiempo (s)");
    legend("location", "eastoutside");
    grid on

    hold all

    print(sprintf("%s/flow-N=%d-DS=%.1f.png", './output/desiredSpeeds', numberOfParticles, desiredSpeed), "-dpngcairo", "-F:12")

    if (index == lastIndex)
        hold off
    endif
end